package com.rustyn.sentinel.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustyn.sentinel.data.database.entity.BlockedCallEntity
import com.rustyn.sentinel.data.database.entity.RuleEntity
import com.rustyn.sentinel.data.database.entity.SuggestionEntity
import com.rustyn.sentinel.domain.repository.BlockedCallRepository
import com.rustyn.sentinel.domain.repository.RuleRepository
import com.rustyn.sentinel.domain.repository.SuggestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val totalBlocked: Int = 0,
    val blockedToday: Int = 0,
    val blockedThisWeek: Int = 0,
    val activeRulesCount: Int = 0,
    val suggestions: List<SuggestionEntity> = emptyList(),
    val weeklyStats: List<Int> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    blockedCallRepository: BlockedCallRepository,
    ruleRepository: RuleRepository,
    suggestionRepository: SuggestionRepository
) : ViewModel() {

    private fun getMidnightTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeekTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<DashboardUiState> = combine(
        blockedCallRepository.getBlockedCallsCount(),
        blockedCallRepository.getBlockedCallsCountSince(getMidnightTimestamp()),
        blockedCallRepository.getBlockedCallsCountSince(getStartOfWeekTimestamp()),
        ruleRepository.getAllRules(),
        suggestionRepository.getPendingSuggestions(),
        blockedCallRepository.getAllBlockedCalls()
    ) { flowsArray ->
        val total = flowsArray[0] as Int
        val today = flowsArray[1] as Int
        val week = flowsArray[2] as Int
        val rules = flowsArray[3] as List<RuleEntity>
        val pendingSugs = flowsArray[4] as List<SuggestionEntity>
        val allLogs = flowsArray[5] as List<BlockedCallEntity>

        // Compile weekly block counts per day (last 7 days)
        val dailyCounts = ArrayList<Int>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = getStartOfDay(dayCal)
            val dayEnd = getEndOfDay(dayCal)
            
            val count = allLogs.count { it.timestamp in dayStart..dayEnd }
            dailyCounts.add(count)
        }

        DashboardUiState(
            totalBlocked = total,
            blockedToday = today,
            blockedThisWeek = week,
            activeRulesCount = rules.count { it.isActive },
            suggestions = pendingSugs,
            weeklyStats = dailyCounts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private fun getStartOfDay(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun getEndOfDay(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }
}
