package com.rustyn.sentinel.engine

import android.content.Context
import android.provider.CallLog
import com.rustyn.sentinel.data.database.dao.SuggestionDao
import com.rustyn.sentinel.data.database.entity.SuggestionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallLogScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val patternEngine: PatternEngine,
    private val suggestionDao: SuggestionDao
) {
    suspend fun scanAndAnalyzeCallLog() = withContext(Dispatchers.IO) {
        val numbers = mutableListOf<String>()

        try {
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE
            )
            
            // Only fetch Missed, Rejected, and Blocked calls
            val selection = "${CallLog.Calls.TYPE} = ? OR ${CallLog.Calls.TYPE} = ? OR ${CallLog.Calls.TYPE} = ?"
            val selectionArgs = arrayOf(
                CallLog.Calls.MISSED_TYPE.toString(),
                CallLog.Calls.REJECTED_TYPE.toString(),
                CallLog.Calls.BLOCKED_TYPE.toString()
            )

            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CallLog.Calls.DATE} DESC LIMIT 100"
            )?.use { cursor ->
                val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                while (cursor.moveToNext()) {
                    if (numberIndex != -1) {
                        val number = cursor.getString(numberIndex)
                        if (!number.isNullOrBlank()) {
                            numbers.add(number)
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission denied
            return@withContext
        } catch (e: Exception) {
            // Query failed
            return@withContext
        }

        if (numbers.isNotEmpty()) {
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
        }
    }
}
