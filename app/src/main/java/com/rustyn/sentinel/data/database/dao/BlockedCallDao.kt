package com.rustyn.sentinel.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rustyn.sentinel.data.database.entity.BlockedCallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedCallDao {
    @Query("SELECT * FROM blocked_calls ORDER BY timestamp DESC")
    fun getAllBlockedCalls(): Flow<List<BlockedCallEntity>>

    @Query("SELECT COUNT(*) FROM blocked_calls")
    fun getBlockedCallsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_calls WHERE timestamp >= :start")
    fun getBlockedCallsCountSince(start: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_calls WHERE timestamp BETWEEN :start AND :end")
    fun getBlockedCallsCountBetween(start: Long, end: Long): Flow<Int>

    @Query("SELECT * FROM blocked_calls ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentBlockedCalls(limit: Int): List<BlockedCallEntity>

    @Query("SELECT * FROM blocked_calls WHERE id = :id")
    suspend fun getBlockedCallById(id: Int): BlockedCallEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedCall(call: BlockedCallEntity): Long

    @Delete
    suspend fun deleteBlockedCall(call: BlockedCallEntity)

    @Query("DELETE FROM blocked_calls")
    suspend fun clearAllBlockedCalls()
}
