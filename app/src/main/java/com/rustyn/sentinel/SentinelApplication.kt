package com.rustyn.sentinel

import android.app.Application
import com.rustyn.sentinel.data.database.dao.AllowlistDao
import com.rustyn.sentinel.data.database.dao.RuleDao
import com.rustyn.sentinel.engine.RuleEngine
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SentinelApplication : Application() {

    @Inject
    lateinit var ruleEngine: RuleEngine

    @Inject
    lateinit var ruleDao: RuleDao

    @Inject
    lateinit var allowlistDao: AllowlistDao

    @Inject
    lateinit var settingsDao: com.rustyn.sentinel.data.database.dao.SettingsDao

    override fun onCreate() {
        super.onCreate()

        // Warm up the Rule Engine cache from the database on startup
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rules = ruleDao.getActiveRules()
                ruleEngine.updateRules(rules)

                val allowlist = allowlistDao.getAllowlistEntries()
                ruleEngine.updateAllowlist(allowlist)

                val strictModeSetting = settingsDao.getSetting("strict_mode_enabled")
                val isStrictMode = strictModeSetting == "true"
                ruleEngine.setStrictMode(isStrictMode)
            } catch (e: Exception) {
                // Prevent startup crashes
            }
        }
    }
}
