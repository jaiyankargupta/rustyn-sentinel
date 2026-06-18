package com.rustyn.sentinel.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.rustyn.sentinel.data.database.dao.BlockedCallDao
import com.rustyn.sentinel.data.database.dao.SuggestionDao
import com.rustyn.sentinel.data.database.entity.BlockedCallEntity
import com.rustyn.sentinel.data.database.entity.SuggestionEntity
import com.rustyn.sentinel.engine.PatternEngine
import com.rustyn.sentinel.engine.RuleEngine
import com.rustyn.sentinel.notifications.SentinelNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SentinelCallScreeningService : CallScreeningService() {

    @Inject
    lateinit var ruleEngine: RuleEngine

    @Inject
    lateinit var blockedCallDao: BlockedCallDao

    @Inject
    lateinit var suggestionDao: SuggestionDao

    @Inject
    lateinit var patternEngine: PatternEngine

    @Inject
    lateinit var notificationManager: SentinelNotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle
        if (handle == null) {
            respondWithAllow(callDetails)
            return
        }

        val incomingNumber = handle.schemeSpecificPart
        if (incomingNumber.isNullOrEmpty()) {
            respondWithAllow(callDetails)
            return
        }

        serviceScope.launch {
            // Check if number is in contacts (requires READ_CONTACTS permission)
            var isContact = false
            try {
                val uri = android.net.Uri.withAppendedPath(
                    android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    android.net.Uri.encode(incomingNumber)
                )
                val projection = arrayOf(android.provider.ContactsContract.PhoneLookup._ID)
                contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        isContact = true
                    }
                }
            } catch (e: SecurityException) {
                // Permission not granted, assume false
            } catch (e: Exception) {
                // Cursor errors, assume false
            }

            // Query local memory cached rules
            val result = ruleEngine.evaluateCall(incomingNumber, isContact)

            when (result) {
                is RuleEngine.MatchResult.Allowed -> {
                    respondWithAllow(callDetails)
                }
                is RuleEngine.MatchResult.Blocked -> {
                    // 1. Log blocked call to Local Room Database FIRST
                    // This ensures the process isn't killed by Android before logging finishes
                    val logId = blockedCallDao.insertBlockedCall(
                        BlockedCallEntity(
                            number = incomingNumber,
                            matchedRuleId = result.ruleId,
                            matchedRulePattern = result.rulePattern,
                            blockAction = "REJECT"
                        )
                    ).toInt()

                    // 2. Post Custom Notification with Action Intents
                    notificationManager.showBlockedCallNotification(
                        blockedCallId = logId,
                        number = incomingNumber,
                        matchedRuleId = result.ruleId,
                        matchedRulePattern = result.rulePattern
                    )

                    // 3. Silent block response to Android Telecom
                    respondWithBlock(callDetails)

                    // 4. Run background pattern analysis
                    runPatternDetection()
                }
            }
        }
    }

    private fun respondWithAllow(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)
    }

    private fun respondWithBlock(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(true) // Hides default Android system notification
            .build()
        respondToCall(callDetails, response)
    }

    private suspend fun runPatternDetection() {
        try {
            // Fetch last 50 blocked calls
            val recentCalls = blockedCallDao.getRecentBlockedCalls(50)
            val numbers = recentCalls.map { it.number }

            // Execute local clustering pass
            val suggestions = patternEngine.analyzeBlockedCalls(numbers)

            for (candidate in suggestions) {
                val existing = suggestionDao.getSuggestionByPattern(candidate.pattern)
                if (existing == null) {
                    suggestionDao.insertSuggestion(
                        SuggestionEntity(
                            suggestedPattern = candidate.pattern,
                            type = candidate.type,
                            triggerCount = candidate.triggerCount,
                            exampleNumbers = candidate.sourceNumbers.joinToString(",")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Shield execution loop
        }
    }
}
