package com.rustyn.sentinel.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSetting(key: String, defaultValue: String): String
    fun getSettingFlow(key: String, defaultValue: String): Flow<String>
    suspend fun saveSetting(key: String, value: String)
    suspend fun deleteSetting(key: String)
}
