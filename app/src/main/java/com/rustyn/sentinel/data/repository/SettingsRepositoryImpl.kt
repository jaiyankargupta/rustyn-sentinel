package com.rustyn.sentinel.data.repository

import com.rustyn.sentinel.data.database.dao.SettingsDao
import com.rustyn.sentinel.data.database.entity.SettingsEntity
import com.rustyn.sentinel.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override suspend fun getSetting(key: String, defaultValue: String): String {
        return settingsDao.getSetting(key) ?: defaultValue
    }

    override fun getSettingFlow(key: String, defaultValue: String): Flow<String> {
        return settingsDao.getSettingFlow(key).map { it ?: defaultValue }
    }

    override suspend fun saveSetting(key: String, value: String) {
        settingsDao.insertSetting(SettingsEntity(key, value))
    }

    override suspend fun deleteSetting(key: String) {
        settingsDao.deleteSetting(key)
    }
}
