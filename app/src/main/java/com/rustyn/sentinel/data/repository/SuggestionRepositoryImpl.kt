package com.rustyn.sentinel.data.repository

import com.rustyn.sentinel.data.database.dao.SuggestionDao
import com.rustyn.sentinel.data.database.entity.SuggestionEntity
import com.rustyn.sentinel.domain.repository.SuggestionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SuggestionRepositoryImpl @Inject constructor(
    private val suggestionDao: SuggestionDao
) : SuggestionRepository {

    override fun getAllSuggestions(): Flow<List<SuggestionEntity>> = suggestionDao.getAllSuggestions()

    override fun getPendingSuggestions(): Flow<List<SuggestionEntity>> = suggestionDao.getPendingSuggestions()

    override suspend fun insertSuggestion(suggestion: SuggestionEntity): Long = suggestionDao.insertSuggestion(suggestion)

    override suspend fun updateSuggestionStatus(id: Int, status: String) = suggestionDao.updateSuggestionStatus(id, status)

    override suspend fun deleteSuggestion(suggestion: SuggestionEntity) = suggestionDao.deleteSuggestion(suggestion)

    override suspend fun clearSuggestions() = suggestionDao.clearSuggestions()
}
