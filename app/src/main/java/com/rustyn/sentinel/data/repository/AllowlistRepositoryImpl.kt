package com.rustyn.sentinel.data.repository

import com.rustyn.sentinel.data.database.dao.AllowlistDao
import com.rustyn.sentinel.data.database.entity.AllowlistEntity
import com.rustyn.sentinel.domain.repository.AllowlistRepository
import com.rustyn.sentinel.engine.RuleEngine
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AllowlistRepositoryImpl @Inject constructor(
    private val allowlistDao: AllowlistDao,
    private val ruleEngine: RuleEngine
) : AllowlistRepository {

    override fun getAllAllowlist(): Flow<List<AllowlistEntity>> = allowlistDao.getAllAllowlist()

    override suspend fun insertAllowlist(entry: AllowlistEntity): Long {
        val result = allowlistDao.insertAllowlist(entry)
        syncEngine()
        return result
    }

    override suspend fun deleteAllowlist(entry: AllowlistEntity) {
        allowlistDao.deleteAllowlist(entry)
        syncEngine()
    }

    override suspend fun deleteAllowlistByPattern(pattern: String) {
        allowlistDao.deleteAllowlistByPattern(pattern)
        syncEngine()
    }

    private suspend fun syncEngine() {
        val allowlist = allowlistDao.getAllowlistEntries()
        ruleEngine.updateAllowlist(allowlist)
    }
}
