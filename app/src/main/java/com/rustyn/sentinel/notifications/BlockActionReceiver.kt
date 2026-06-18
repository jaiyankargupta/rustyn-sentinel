package com.rustyn.sentinel.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rustyn.sentinel.data.database.dao.AllowlistDao
import com.rustyn.sentinel.data.database.dao.RuleDao
import com.rustyn.sentinel.data.database.entity.AllowlistEntity
import com.rustyn.sentinel.engine.RuleEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BlockActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var ruleDao: RuleDao

    @Inject
    lateinit var allowlistDao: AllowlistDao

    @Inject
    lateinit var ruleEngine: RuleEngine

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra(SentinelNotificationManager.EXTRA_NOTIFICATION_ID, -1)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    SentinelNotificationManager.ACTION_ALLOW_NUMBER -> {
                        val number = intent.getStringExtra(SentinelNotificationManager.EXTRA_NUMBER)
                        if (number != null) {
                            // Insert into allowlist
                            val entry = AllowlistEntity(
                                pattern = number,
                                type = "EXACT",
                                name = "Allowed from notification"
                            )
                            allowlistDao.insertAllowlist(entry)
                            
                            // Re-sync memory caches
                            val allowlist = allowlistDao.getAllowlistEntries()
                            ruleEngine.updateAllowlist(allowlist)
                        }
                    }
                    SentinelNotificationManager.ACTION_DISABLE_RULE -> {
                        val ruleId = intent.getIntExtra(SentinelNotificationManager.EXTRA_RULE_ID, -1)
                        if (ruleId != -1) {
                            // Deactivate in db
                            ruleDao.updateRuleStatus(ruleId, false)

                            // Re-sync memory caches
                            val rules = ruleDao.getActiveRules()
                            ruleEngine.updateRules(rules)
                        }
                    }
                }
            } finally {
                // Cancel notification
                if (notificationId != -1) {
                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(notificationId)
                }
                pendingResult.finish()
            }
        }
    }
}
