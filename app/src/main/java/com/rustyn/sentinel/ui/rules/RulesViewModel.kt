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

    fun addRule(pattern: String, type: String, description: String? = null, startTime: String? = null, endTime: String? = null) {
        viewModelScope.launch {
            if (pattern.isNotBlank()) {
                val newRule = RuleEntity(
                    pattern = pattern.trim(),
                    type = type.uppercase(),
                    description = description,
                    startTime = startTime,
                    endTime = endTime
                )
                ruleRepository.insertRule(newRule)
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
