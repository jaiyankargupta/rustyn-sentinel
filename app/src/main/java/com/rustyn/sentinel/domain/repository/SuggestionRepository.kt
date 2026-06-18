package com.rustyn.sentinel.domain.repository

import com.rustyn.sentinel.data.database.entity.SuggestionEntity
import kotlinx.coroutines.flow.Flow

interface SuggestionRepository {
    fun getAllSuggestions(): Flow<List<SuggestionEntity>>
    fun getPendingSuggestions(): Flow<List<SuggestionEntity>>
    suspend fun insertSuggestion(suggestion: SuggestionEntity): Long
    suspend fun updateSuggestionStatus(id: Int, status: String)
    suspend fun deleteSuggestion(suggestion: SuggestionEntity)
    suspend fun clearSuggestions()
}
