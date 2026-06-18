package com.rustyn.sentinel.data.repository

import com.rustyn.sentinel.data.database.dao.BlockedCallDao
import com.rustyn.sentinel.data.database.entity.BlockedCallEntity
import com.rustyn.sentinel.domain.repository.BlockedCallRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BlockedCallRepositoryImpl @Inject constructor(
    private val blockedCallDao: BlockedCallDao
) : BlockedCallRepository {

    override fun getAllBlockedCalls(): Flow<List<BlockedCallEntity>> = blockedCallDao.getAllBlockedCalls()

    override fun getBlockedCallsCount(): Flow<Int> = blockedCallDao.getBlockedCallsCount()

    override fun getBlockedCallsCountSince(start: Long): Flow<Int> = blockedCallDao.getBlockedCallsCountSince(start)

    override fun getBlockedCallsCountBetween(start: Long, end: Long): Flow<Int> = blockedCallDao.getBlockedCallsCountBetween(start, end)

    override suspend fun getBlockedCallById(id: Int): BlockedCallEntity? = blockedCallDao.getBlockedCallById(id)

    override suspend fun insertBlockedCall(call: BlockedCallEntity): Long = blockedCallDao.insertBlockedCall(call)

    override suspend fun deleteBlockedCall(call: BlockedCallEntity) = blockedCallDao.deleteBlockedCall(call)

    override suspend fun clearAllBlockedCalls() = blockedCallDao.clearAllBlockedCalls()
}
