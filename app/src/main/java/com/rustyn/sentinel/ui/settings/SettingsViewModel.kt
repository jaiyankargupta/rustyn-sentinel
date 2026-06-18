package com.rustyn.sentinel.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustyn.sentinel.data.database.entity.AllowlistEntity
import com.rustyn.sentinel.domain.repository.AllowlistRepository
import com.rustyn.sentinel.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.rustyn.sentinel.data.database.entity.RuleEntity
import com.rustyn.sentinel.domain.repository.RuleRepository
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.first

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val allowlistRepository: AllowlistRepository,
    private val ruleRepository: RuleRepository
) : ViewModel() {

    val notificationsEnabled: StateFlow<String> = settingsRepository.getSettingFlow("notifications_enabled", "true")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "true")

    val strictModeEnabled: StateFlow<String> = settingsRepository.getSettingFlow("strict_mode_enabled", "false")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "false")

    val allowlist: StateFlow<List<AllowlistEntity>> = allowlistRepository.getAllAllowlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveSetting("notifications_enabled", enabled.toString())
        }
    }

    fun toggleStrictMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveSetting("strict_mode_enabled", enabled.toString())
        }
    }

    fun addAllowlist(number: String, name: String?) {
        viewModelScope.launch {
            if (number.isNotBlank()) {
                val entry = AllowlistEntity(
                    pattern = number.trim(),
                    type = "EXACT",
                    name = name
                )
                allowlistRepository.insertAllowlist(entry)
            }
        }
    }

    fun deleteAllowlist(entry: AllowlistEntity) {
        viewModelScope.launch {
            allowlistRepository.deleteAllowlist(entry)
        }
    }

    suspend fun exportRulesToJson(): String {
        val rules = ruleRepository.getAllRules().first()
        val jsonArray = JSONArray()
        for (rule in rules) {
            val obj = JSONObject()
            obj.put("pattern", rule.pattern)
            obj.put("type", rule.type)
            obj.put("description", rule.description ?: JSONObject.NULL)
            obj.put("startTime", rule.startTime ?: JSONObject.NULL)
            obj.put("endTime", rule.endTime ?: JSONObject.NULL)
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    suspend fun importRulesFromJson(jsonString: String): Result<Int> {
        return try {
            val jsonArray = JSONArray(jsonString)
            var importedCount = 0
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val pattern = obj.getString("pattern")
                val type = obj.getString("type")
                val desc = if (obj.isNull("description")) null else obj.getString("description")
                val start = if (obj.isNull("startTime")) null else obj.getString("startTime")
                val end = if (obj.isNull("endTime")) null else obj.getString("endTime")

                val rule = RuleEntity(
                    pattern = pattern,
                    type = type,
                    description = desc,
                    startTime = start,
                    endTime = end
                )
                ruleRepository.insertRule(rule)
                importedCount++
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
