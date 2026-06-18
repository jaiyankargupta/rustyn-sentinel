package com.rustyn.sentinel.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rustyn.sentinel.data.database.entity.AllowlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AllowlistDao {
    @Query("SELECT * FROM allowlist ORDER BY createdAt DESC")
    fun getAllAllowlist(): Flow<List<AllowlistEntity>>

    @Query("SELECT * FROM allowlist")
    suspend fun getAllowlistEntries(): List<AllowlistEntity>

    @Query("SELECT * FROM allowlist WHERE pattern = :pattern LIMIT 1")
    suspend fun getAllowlistByPattern(pattern: String): AllowlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllowlist(entry: AllowlistEntity): Long

    @Delete
    suspend fun deleteAllowlist(entry: AllowlistEntity)

    @Query("DELETE FROM allowlist WHERE pattern = :pattern")
    suspend fun deleteAllowlistByPattern(pattern: String)
}
