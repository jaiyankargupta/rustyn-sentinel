package com.rustyn.sentinel.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustyn.sentinel.data.database.entity.RuleEntity
import com.rustyn.sentinel.domain.repository.RuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val ruleRepository: RuleRepository
) : ViewModel() {

    val rules: StateFlow<List<RuleEntity>> = ruleRepository.getAllRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun getCandidates(pattern: String): List<String> {
        val sanitized = pattern.replace("*", "").replace("?", "")
        val candidates = mutableListOf(sanitized)
        if (sanitized.startsWith("+")) {
            if (sanitized.length > 3) candidates.add(sanitized.substring(3))
            if (sanitized.length > 2) candidates.add(sanitized.substring(2))
            if (sanitized.length > 4) candidates.add(sanitized.substring(4))
        }
        return candidates.distinct()
    }

    private fun arePatternsEquivalent(p1: String, p2: String): Boolean {
        if (p1 == p2) return true
        val c1 = getCandidates(p1)
        val c2 = getCandidates(p2)
        return c1.intersect(c2.toSet()).isNotEmpty()
    }

    fun addRule(pattern: String, type: String, description: String? = null, startTime: String? = null, endTime: String? = null, onDuplicate: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (pattern.isNotBlank()) {
                val cleanPattern = pattern.trim()
                val currentRules = rules.value
                val isDuplicate = currentRules.any { existingRule ->
                    arePatternsEquivalent(existingRule.pattern, cleanPattern) && existingRule.type.equals(type, ignoreCase = true)
                }
                
                if (!isDuplicate) {
                    val newRule = RuleEntity(
                        pattern = cleanPattern,
                        type = type.uppercase(),
                        description = description,
                        startTime = startTime,
                        endTime = endTime
                    )
                    ruleRepository.insertRule(newRule)
                } else {
                    onDuplicate?.invoke()
                }
            }
        }
    }

    fun toggleRule(ruleId: Int, isActive: Boolean) {
        viewModelScope.launch {
            ruleRepository.updateRuleStatus(ruleId, isActive)
        }
    }

    fun deleteRule(ruleId: Int) {
        viewModelScope.launch {
            ruleRepository.deleteRuleById(ruleId)
        }
    }
}
