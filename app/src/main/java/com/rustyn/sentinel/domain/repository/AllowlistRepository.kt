package com.rustyn.sentinel.domain.repository

import com.rustyn.sentinel.data.database.entity.AllowlistEntity
import kotlinx.coroutines.flow.Flow

interface AllowlistRepository {
    fun getAllAllowlist(): Flow<List<AllowlistEntity>>
    suspend fun insertAllowlist(entry: AllowlistEntity): Long
    suspend fun deleteAllowlist(entry: AllowlistEntity)
    suspend fun deleteAllowlistByPattern(pattern: String)
}
