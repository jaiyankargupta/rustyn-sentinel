package com.rustyn.sentinel.engine

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternEngine @Inject constructor() {

    data class SuggestionCandidate(
        val pattern: String,
        val type: String,
        val triggerCount: Int,
        val sourceNumbers: List<String>
    )

    /**
     * Analyzes recent blocked calls to identify potential prefix rules.
     * Ensure that suggestions are generated from K distinct phone numbers,
     * not a single phone number calling K times.
     *
     * @param blockedNumbers List of recently blocked phone numbers
     * @param minThreshold Minimum number of unique calling numbers sharing a prefix (default: 3)
     * @param minPrefixLength Minimum length of the prefix to prevent general blocking (default: 4)
     * @return List of rule suggestion candidates
     */
    fun analyzeBlockedCalls(
        blockedNumbers: List<String>,
        minThreshold: Int = 3,
        minPrefixLength: Int = 4
    ): List<SuggestionCandidate> {
        val sanitizedNumbers = blockedNumbers.map { sanitize(it) }.filter { it.length >= minPrefixLength }.distinct()
        if (sanitizedNumbers.size < minThreshold) return emptyList()

        // Prefix map: prefix -> Set of original sanitized numbers matching it
        val prefixMap = HashMap<String, HashSet<String>>()

        for (number in sanitizedNumbers) {
            // Generate all prefixes of the number from minPrefixLength up to length - 1
            for (len in minPrefixLength until number.length) {
                val prefix = number.substring(0, len)
                prefixMap.getOrPut(prefix) { HashSet() }.add(number)
            }
        }

        val candidates = ArrayList<SuggestionCandidate>()

        // Filter prefixes that meet the threshold
        for ((prefix, matchingNumbers) in prefixMap) {
            if (matchingNumbers.size >= minThreshold) {
                val wildcardPattern = "$prefix*"
                candidates.add(
                    SuggestionCandidate(
                        pattern = wildcardPattern,
                        type = "PREFIX",
                        triggerCount = matchingNumbers.size,
                        sourceNumbers = matchingNumbers.toList()
                    )
                )
            }
        }

        // Filter candidates: if we have nested prefixes (e.g., "890488*" and "89048*"),
        // keep the more specific one (longest prefix) if they share the same trigger set.
        val finalCandidates = ArrayList<SuggestionCandidate>()
        val sortedCandidates = candidates.sortedByDescending { it.pattern.length }

        for (candidate in sortedCandidates) {
            val isRedundant = finalCandidates.any { existing ->
                existing.pattern.replace("*", "").startsWith(candidate.pattern.replace("*", "")) &&
                        existing.sourceNumbers.containsAll(candidate.sourceNumbers)
            }
            if (!isRedundant) {
                finalCandidates.add(candidate)
            }
        }

        return finalCandidates
    }

    private fun sanitize(number: String): String {
        return number.filter { it.isDigit() || it == '+' }
    }
}
