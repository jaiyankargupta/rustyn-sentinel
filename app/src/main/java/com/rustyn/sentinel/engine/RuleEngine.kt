package com.rustyn.sentinel.engine

import com.rustyn.sentinel.data.database.entity.AllowlistEntity
import com.rustyn.sentinel.data.database.entity.RuleEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleEngine @Inject constructor() {

    private val mutex = Mutex()
    private val initDeferred = kotlinx.coroutines.CompletableDeferred<Unit>()

    private var isStrictModeEnabled: Boolean = false

    // In-memory caches for rules
    private val exactRules = HashSet<String>()
    private val exactRuleMetadata = HashMap<String, RuleEntity>()
    private val prefixTrieRoot = TrieNode()
    private val wildcardRules = ArrayList<RuleEntity>()

    // In-memory caches for allowlist
    private val exactAllowlist = HashSet<String>()
    private val prefixAllowlistTrieRoot = TrieNode()

    class TrieNode {
        val children = HashMap<Char, TrieNode>()
        var isTerminal = false
        var rule: RuleEntity? = null
    }

    sealed class MatchResult {
        object Allowed : MatchResult()
        data class Blocked(val ruleId: Int, val rulePattern: String) : MatchResult()
    }

    /**
     * Sanitizes the phone number by removing spaces, hyphens, and parenthesis.
     */
    fun sanitizeNumber(number: String): String {
        return number.filter { it.isDigit() || it == '+' }
    }

    /**
     * Exposes candidates of phone numbers to check (e.g. raw, with country code, without country code).
     */
    fun getNumberCandidates(sanitizedNumber: String): List<String> {
        val candidates = ArrayList<String>()
        candidates.add(sanitizedNumber)

        // If it has a country code prefix like "+91", check the localized number too (e.g. "8904889067")
        if (sanitizedNumber.startsWith("+")) {
            // Remove '+' and leading country code digits (1 to 3 digits)
            // A simple heuristic is: if it starts with +91 (length 3), check without +91
            if (sanitizedNumber.length > 3) {
                candidates.add(sanitizedNumber.substring(3))
            }
            if (sanitizedNumber.length > 2) {
                candidates.add(sanitizedNumber.substring(2))
            }
            if (sanitizedNumber.length > 4) {
                candidates.add(sanitizedNumber.substring(4))
            }
        }
        return candidates.distinct()
    }

    /**
     * Update the rule sets in memory.
     */
    suspend fun updateRules(rules: List<RuleEntity>) = mutex.withLock {
        exactRules.clear()
        exactRuleMetadata.clear()
        wildcardRules.clear()
        
        // Reset Prefix Trie
        prefixTrieRoot.children.clear()
        prefixTrieRoot.isTerminal = false

        for (rule in rules) {
            if (!rule.isActive) continue

            val pattern = sanitizeRulePattern(rule.pattern)
            when (rule.type.uppercase()) {
                "EXACT" -> {
                    exactRules.add(pattern)
                    exactRuleMetadata[pattern] = rule
                }
                "PREFIX" -> {
                    insertPrefixIntoTrie(prefixTrieRoot, pattern, rule)
                }
                "WILDCARD" -> {
                    wildcardRules.add(rule.copy(pattern = pattern))
                }
            }
        }
        initDeferred.complete(Unit)
    }

    /**
     * Update the allowlist in memory.
     */
    suspend fun updateAllowlist(allowlist: List<AllowlistEntity>) = mutex.withLock {
        exactAllowlist.clear()
        prefixAllowlistTrieRoot.children.clear()
        prefixAllowlistTrieRoot.isTerminal = false

        for (entry in allowlist) {
            val pattern = sanitizeRulePattern(entry.pattern)
            when (entry.type.uppercase()) {
                "EXACT" -> {
                    exactAllowlist.add(pattern)
                }
                "PREFIX" -> {
                    // Create a dummy RuleEntity to reuse the Trie node logic
                    val dummyRule = RuleEntity(id = entry.id, pattern = entry.pattern, type = entry.type)
                    insertPrefixIntoTrie(prefixAllowlistTrieRoot, pattern, dummyRule)
                }
            }
        }
    }

    /**
     * Enable or disable strict mode (Contacts-only block).
     */
    fun setStrictMode(enabled: Boolean) {
        isStrictModeEnabled = enabled
    }

    /**
     * Checks if a rule is currently active based on its time bounds and days of week.
     */
    private fun isRuleActiveNow(rule: RuleEntity): Boolean {
        // Simple logic for now: if no start/end time is set, it's always active.
        // We will need to implement actual time checking against the device's clock.
        // For now, if either is null, we assume it's active.
        if (rule.startTime == null || rule.endTime == null) return true

        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(java.util.Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        try {
            val startParts = rule.startTime.split(":")
            val startTotalMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()

            val endParts = rule.endTime.split(":")
            val endTotalMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()

            if (startTotalMinutes <= endTotalMinutes) {
                return currentTotalMinutes in startTotalMinutes..endTotalMinutes
            } else {
                // Wraps around midnight
                return currentTotalMinutes >= startTotalMinutes || currentTotalMinutes <= endTotalMinutes
            }
        } catch (e: Exception) {
            return true // Fail open if time parsing is corrupted
        }
    }

    /**
     * Evaluates whether an incoming call should be blocked or allowed.
     */
    suspend fun evaluateCall(phoneNumber: String, isContact: Boolean = false): MatchResult {
        // Ensure rules are loaded before evaluating
        initDeferred.await()
        
        return mutex.withLock {
            val sanitized = sanitizeNumber(phoneNumber)
            if (sanitized.isEmpty()) return MatchResult.Allowed

        val candidates = getNumberCandidates(sanitized)

        // 1. Check Allowlist Priority
        for (candidate in candidates) {
            if (exactAllowlist.contains(candidate)) {
                return MatchResult.Allowed
            }
            if (checkPrefixInTrie(prefixAllowlistTrieRoot, candidate)) {
                return MatchResult.Allowed
            }
        }

        // Strict Mode Check: If enabled and number is not in contacts, block it immediately
        if (isStrictModeEnabled && !isContact) {
            return MatchResult.Blocked(-1, "STRICT_MODE")
        }

        // 2. Check Exact Rules
        for (candidate in candidates) {
            if (exactRules.contains(candidate)) {
                val rule = exactRuleMetadata[candidate]
                if (rule != null && isRuleActiveNow(rule)) {
                    return MatchResult.Blocked(rule.id, rule.pattern)
                }
            }
        }

        // 3. Check Prefix Rules (Trie-based lookup)
        for (candidate in candidates) {
            val matchedRule = getMatchedPrefixFromTrie(prefixTrieRoot, candidate)
            if (matchedRule != null && isRuleActiveNow(matchedRule)) {
                return MatchResult.Blocked(matchedRule.id, matchedRule.pattern)
            }
        }

        // 4. Check Wildcard Rules
        for (candidate in candidates) {
            for (rule in wildcardRules) {
                if (matchWildcard(candidate, rule.pattern) && isRuleActiveNow(rule)) {
                    return MatchResult.Blocked(rule.id, rule.pattern)
                }
            }
        }

        return MatchResult.Allowed
        }
    }

    // Helper functions

    private fun sanitizeRulePattern(pattern: String): String {
        return pattern.filter { it.isDigit() || it == '+' || it == '*' || it == '?' }
    }

    private fun insertPrefixIntoTrie(root: TrieNode, prefixPattern: String, rule: RuleEntity) {
        val cleanPrefix = prefixPattern.replace("*", "")
        var current = root
        for (char in cleanPrefix) {
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isTerminal = true
        current.rule = rule
    }

    private fun checkPrefixInTrie(root: TrieNode, number: String): Boolean {
        var current = root
        for (char in number) {
            val next = current.children[char] ?: return current.isTerminal
            current = next
            if (current.isTerminal) return true
        }
        return current.isTerminal
    }

    private fun getMatchedPrefixFromTrie(root: TrieNode, number: String): RuleEntity? {
        var current = root
        for (char in number) {
            val next = current.children[char]
            if (next == null) {
                return if (current.isTerminal) current.rule else null
            }
            current = next
            if (current.isTerminal && current.rule != null) {
                return current.rule
            }
        }
        return if (current.isTerminal) current.rule else null
    }

    private fun matchWildcard(number: String, wildcardPattern: String): Boolean {
        // We now support '*' and '?'. 
        // '*' matches zero or more of any character.
        // '?' matches exactly one of any character.
        // This regex conversion allows complex wildcard matching.
        try {
            var regexStr = wildcardPattern
                .replace("+", "\\+")
                .replace("?", ".")
                .replace("*", ".*")
            regexStr = "^$regexStr\$"
            val regex = Regex(regexStr)
            return regex.matches(number)
        } catch (e: Exception) {
            // Fallback for simple equal length ? wildcard
            if (number.length != wildcardPattern.length) return false
            for (i in number.indices) {
                val patternChar = wildcardPattern[i]
                if (patternChar != '?' && patternChar != number[i]) {
                    return false
                }
            }
            return true
        }
    }
}
