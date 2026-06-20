package com.rustyn.sentinel.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rustyn.sentinel.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SentinelNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "blocked_calls_channel"
        private const val CHANNEL_NAME = "Blocked Calls Log"
        private const val CHANNEL_DESC = "Displays alerts when calls are screened and blocked"
        private const val NOTIFICATION_ID_BASE = 1000

        const val ACTION_ALLOW_NUMBER = "com.rustyn.sentinel.ACTION_ALLOW_NUMBER"
        const val ACTION_DISABLE_RULE = "com.rustyn.sentinel.ACTION_DISABLE_RULE"
        const val EXTRA_NUMBER = "extra_number"
        const val EXTRA_RULE_ID = "extra_rule_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(false) // Silent block - no vibration
                setSound(null, null) // Silent block - no ringtone
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showBlockedCallNotification(
        blockedCallId: Int,
        number: String,
        matchedRuleId: Int?,
        matchedRulePattern: String?
    ) {
        val notificationId = NOTIFICATION_ID_BASE + blockedCallId

        // 1. View Details Action (Deep link to App UI)
        val detailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sentinel://history_detail/$blockedCallId")).apply {
            setClass(context, MainActivity::class.java)
            // Use SINGLE_TOP so if MainActivity is running, it routes via onNewIntent instead of restarting
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val detailPendingIntent = PendingIntent.getActivity(
            context,
            notificationId * 3,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Unblock Number Action (Broadcast to BlockActionReceiver)
        val allowIntent = Intent(context, BlockActionReceiver::class.java).apply {
            action = ACTION_ALLOW_NUMBER
            putExtra(EXTRA_NUMBER, number)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val allowPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 3 + 1,
            allowIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Disable Rule Action (Broadcast to BlockActionReceiver)
        val disableIntent = Intent(context, BlockActionReceiver::class.java).apply {
            action = ACTION_DISABLE_RULE
            putExtra(EXTRA_RULE_ID, matchedRuleId ?: -1)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val disablePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 3 + 2,
            disableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Call Back Action
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val dialPendingIntent = PendingIntent.getActivity(
            context,
            notificationId * 3 + 3,
            dialIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 5. Add to Contacts Action
        val addContactIntent = Intent(android.provider.ContactsContract.Intents.Insert.ACTION).apply {
            type = android.provider.ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, number)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val addContactPendingIntent = PendingIntent.getActivity(
            context,
            notificationId * 3 + 4,
            addContactIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.rustyn.sentinel.R.drawable.ic_nav_dashboard)
            .setContentTitle("Call Intercepted")
            .setContentText(number)
            .setColor(0xFF38BDF8.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(detailPendingIntent)
            .addAction(android.R.drawable.ic_menu_call, "Call Back", dialPendingIntent)
            .addAction(android.R.drawable.ic_menu_add, "Add to Contacts", addContactPendingIntent)
            .addAction(android.R.drawable.ic_input_add, "Unblock", allowPendingIntent)

        if (matchedRuleId != null && matchedRuleId != -1) {
            builder.addAction(android.R.drawable.ic_delete, "Disable Rule", disablePendingIntent)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            // Note: Permissions should be handled, try-catch protects in case post permission isn't granted yet
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Log or handle missing notification permission gracefully
        }
    }
}
