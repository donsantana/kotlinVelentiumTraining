package com.velentium.android.platformv.ble.core.common

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.velentium.android.platformv.ui.MainActivity

/**
 * [BroadcastReceiver] which handles scheduled/delayed/repeating notifications,
 * as scheduled by [AlarmManager].
 */
class DelayedNotificationReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = DelayedNotificationReceiver::class.java.simpleName
        const val NOTIFICATION_ID = "NOTIFICATION_ID"
        const val NOTIFICATION = "NOTIFICATION"

        /**
         * Enables/disables the broadcast receiver
         */
        @JvmStatic
        fun toggleReceiver(
            enabled: Boolean,
            context: Context,
            cls: Class<*> = DelayedNotificationReceiver::class.java
        ) {
            try {
                val receiver = ComponentName(context, cls)
                context.packageManager.setComponentEnabledSetting(
                    receiver,
                    if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            } catch(ex: Throwable) {
                Log.e(TAG, "Failed to toggle receiver: $cls", ex)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            if (intent.action != null) {
                if (intent.action.equals(Intent.ACTION_SHUTDOWN, true)) {
                    Log.v(TAG, "Device is being shutdown.")
                } else {
                    Log.w(TAG, "Device has rebooted, all scheduled notifications have been cleared: $intent")
                    // AlarmManager clears all alarms on reboot, may need to re-create them here...
                }
            }

            val manager = NotificationManagerCompat.from(context)
            val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
            intent.getParcelableExtra<Notification>(NOTIFICATION)?.let { notification ->
                Log.v(TAG, "Received delayed notification: $notification")
                manager.notify(notificationId, notification)
            } ?: run {
                Log.w(TAG, "Intent is missing notification: $intent")
                Intent(context, MainActivity::class.java)
                    .apply {
                        action = Intent.ACTION_MAIN
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        putExtra(NOTIFICATION_ID, notificationId)
                    }
                    .also { context.startActivity(it) }
            }
        } else {
            Log.w(TAG, "Failed to get an intent, with a notification.")
        }
    }
}