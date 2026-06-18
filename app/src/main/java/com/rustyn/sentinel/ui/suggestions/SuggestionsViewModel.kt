package com.rustyn.sentinel.ui.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustyn.sentinel.data.database.entity.RuleEntity
import com.rustyn.sentinel.data.database.entity.SuggestionEntity
import com.rustyn.sentinel.domain.repository.RuleRepository
import com.rustyn.sentinel.domain.repository.SuggestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val suggestionRepository: SuggestionRepository,
    private val callLogScanner: com.rustyn.sentinel.engine.CallLogScanner
) : ViewModel() {

    val suggestions: StateFlow<List<SuggestionEntity>> = suggestionRepository.getPendingSuggestions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun acceptSuggestion(suggestion: SuggestionEntity) {
        viewModelScope.launch {
            // 1. Write the new rule to the database (which triggers Hilt syncs to the RuleEngine)
            val rule = RuleEntity(
                pattern = suggestion.suggestedPattern,
                type = "PREFIX",
                description = "AI Suggested rule from ${suggestion.triggerCount} calls"
            )
            ruleRepository.insertRule(rule)

            // 2. Resolve suggestion status
            suggestionRepository.updateSuggestionStatus(suggestion.id, "ACCEPTED")
        }
    }

    fun ignoreSuggestion(suggestionId: Int) {
        viewModelScope.launch {
            suggestionRepository.updateSuggestionStatus(suggestionId, "IGNORED")
        }
    }

    fun scanCallLog() {
        viewModelScope.launch {
            callLogScanner.scanAndAnalyzeCallLog()
        }
    }
}
