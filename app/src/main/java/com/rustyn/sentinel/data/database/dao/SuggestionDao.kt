package com.rustyn.sentinel.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rustyn.sentinel.data.database.entity.SuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionDao {
    @Query("SELECT * FROM suggestions ORDER BY createdAt DESC")
    fun getAllSuggestions(): Flow<List<SuggestionEntity>>

    @Query("SELECT * FROM suggestions WHERE status = 'PENDING' ORDER BY triggerCount DESC")
    fun getPendingSuggestions(): Flow<List<SuggestionEntity>>

    @Query("SELECT * FROM suggestions WHERE suggestedPattern = :pattern LIMIT 1")
    suspend fun getSuggestionByPattern(pattern: String): SuggestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuggestion(suggestion: SuggestionEntity): Long

    @Query("UPDATE suggestions SET status = :status WHERE id = :id")
    suspend fun updateSuggestionStatus(id: Int, status: String)

    @Delete
    suspend fun deleteSuggestion(suggestion: SuggestionEntity)

    @Query("DELETE FROM suggestions")
    suspend fun clearSuggestions()
}
