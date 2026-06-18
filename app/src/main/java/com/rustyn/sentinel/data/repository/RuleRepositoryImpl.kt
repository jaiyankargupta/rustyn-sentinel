package com.rustyn.sentinel.data.repository

import com.rustyn.sentinel.data.database.dao.RuleDao
import com.rustyn.sentinel.data.database.entity.RuleEntity
import com.rustyn.sentinel.domain.repository.RuleRepository
import com.rustyn.sentinel.engine.RuleEngine
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RuleRepositoryImpl @Inject constructor(
    private val ruleDao: RuleDao,
    private val ruleEngine: RuleEngine
) : RuleRepository {

    override fun getAllRules(): Flow<List<RuleEntity>> = ruleDao.getAllRules()

    override suspend fun getActiveRules(): List<RuleEntity> = ruleDao.getActiveRules()

    override suspend fun getRuleById(id: Int): RuleEntity? = ruleDao.getRuleById(id)

    override suspend fun getRuleByPattern(pattern: String): RuleEntity? = ruleDao.getRuleByPattern(pattern)

    override suspend fun insertRule(rule: RuleEntity): Long {
        val result = ruleDao.insertRule(rule)
        syncEngine()
        return result
    }

    override suspend fun deleteRule(rule: RuleEntity) {
        ruleDao.deleteRule(rule)
        syncEngine()
    }

    override suspend fun deleteRuleById(id: Int) {
        ruleDao.deleteRuleById(id)
        syncEngine()
    }

    override suspend fun updateRuleStatus(id: Int, isActive: Boolean) {
        ruleDao.updateRuleStatus(id, isActive)
        syncEngine()
    }

    private suspend fun syncEngine() {
        val activeRules = ruleDao.getActiveRules()
        ruleEngine.updateRules(activeRules)
    }
}
