package com.rustyn.sentinel.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustyn.sentinel.data.database.entity.BlockedCallEntity
import com.rustyn.sentinel.domain.repository.BlockedCallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val query: String = "",
    val filterType: String = "ALL", // ALL, RULE, INTELLIGENCE
    val blockedCalls: List<BlockedCallEntity> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val blockedCallRepository: BlockedCallRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filterType = MutableStateFlow("ALL")

    val state: StateFlow<HistoryUiState> = combine(
        blockedCallRepository.getAllBlockedCalls(),
        searchQuery,
        filterType
    ) { calls, query, filter ->
        val filteredList = calls.filter { call ->
            val matchesQuery = call.number.contains(query, ignoreCase = true) || 
                               (call.matchedRulePattern?.contains(query, ignoreCase = true) ?: false)
            
            val matchesFilter = when (filter) {
                "RULE" -> call.matchedRuleId != null
                "INTELLIGENCE" -> call.matchedRuleId == null
                else -> true
            }

            matchesQuery && matchesFilter
        }

        HistoryUiState(
            query = query,
            filterType = filter,
            blockedCalls = filteredList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onFilterTypeChanged(filter: String) {
        filterType.value = filter
    }

    fun deleteCall(call: BlockedCallEntity) {
        viewModelScope.launch {
            blockedCallRepository.deleteBlockedCall(call)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            blockedCallRepository.clearAllBlockedCalls()
        }
    }
}
