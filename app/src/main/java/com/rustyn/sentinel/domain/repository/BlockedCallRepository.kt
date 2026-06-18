package com.rustyn.sentinel.domain.repository

import com.rustyn.sentinel.data.database.entity.BlockedCallEntity
import kotlinx.coroutines.flow.Flow

interface BlockedCallRepository {
    fun getAllBlockedCalls(): Flow<List<BlockedCallEntity>>
    fun getBlockedCallsCount(): Flow<Int>
    fun getBlockedCallsCountSince(start: Long): Flow<Int>
    fun getBlockedCallsCountBetween(start: Long, end: Long): Flow<Int>
    suspend fun getBlockedCallById(id: Int): BlockedCallEntity?
    suspend fun insertBlockedCall(call: BlockedCallEntity): Long
    suspend fun deleteBlockedCall(call: BlockedCallEntity)
    suspend fun clearAllBlockedCalls()
}
