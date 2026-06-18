package com.rustyn.sentinel.domain.repository

import com.rustyn.sentinel.data.database.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

interface RuleRepository {
    fun getAllRules(): Flow<List<RuleEntity>>
    suspend fun getActiveRules(): List<RuleEntity>
    suspend fun getRuleById(id: Int): RuleEntity?
    suspend fun insertRule(rule: RuleEntity): Long
    suspend fun deleteRule(rule: RuleEntity)
    suspend fun deleteRuleById(id: Int)
    suspend fun getRuleByPattern(pattern: String): RuleEntity?
    suspend fun updateRuleStatus(id: Int, isActive: Boolean)
}
