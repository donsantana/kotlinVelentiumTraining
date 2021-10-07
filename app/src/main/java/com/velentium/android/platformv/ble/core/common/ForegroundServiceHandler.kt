package com.velentium.android.platformv.ble.core.common

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.velentium.android.platformv.R
import java.util.concurrent.atomic.AtomicBoolean

interface ForegroundService {
    /**
     * This should be called when the app is placed into the background.  This will
     * allow the [Service] to continue to run while the app is in the background, by
     * presenting a persistent "foreground" notification, which effectively keeps the service
     * running, while the app is in the background.
     */
    fun startForegroundNotification()

    /**
     * This should be called when the app is brought back to the foreground.
     * This will hide the persistent notification, for the app, since it is now
     * back in the foreground.
     */
    fun stopForegroundNotification()
}

/**
 * Interface that defines the contract a foreground service handler,
 * should have.
 */
interface ForegroundServiceHandler: ForegroundService {
    /**
     * The id assigned to this [Service]'s notification
     */
    val foregroundNotificationId: Int

    /**
     * The [Notification] that will be shown by this foreground service
     */
    val foregroundNotification: Notification

    /**
     * Returns true, if the notification is visible, false otherwise
     */
    val isForegroundNotificationVisible: Boolean
}

/**
 * Encapsulates the logic of foreground services.
 */
class AndroidForegroundServiceHandler<T>(
    private val service: Service,
    private val activityClass: Class<T>,
    @StringRes private val channelNameResId: Int = R.string.app_name,
    @StringRes private val titleResId: Int = R.string.notify_title,
    @StringRes private val contentResId: Int = R.string.notify_message,
    @DrawableRes private val iconResId: Int = R.mipmap.ic_launcher_round,
    @ColorRes private val lightColorResId: Int = R.color.accent_color,
) : ForegroundServiceHandler {
    companion object {
        val TAG: String = ForegroundServiceHandler::class.java.simpleName
        const val FOREGROUND_SERVICE_NOTIFICATION_REQUEST_CODE = 222222
    }

    override val foregroundNotificationId: Int
        get() = service::class.java.name.hashCode()

    private val context: Context
        get() = service.applicationContext

    /**
     * [AtomicBoolean] that tracks whether the the foreground notification is showing or not.
     */
    private val foregroundVisible = AtomicBoolean(false)

    /**
     * Indicates if the foreground notification is showing or not.
     */
    override var isForegroundNotificationVisible: Boolean
        get() = foregroundVisible.get()
        set(value) {
            if (foregroundVisible.get() != value) {
                foregroundVisible.set(value)
            }
        }

    /**
     * Androids [NotificationManager]
     */
    private val notificationManager: NotificationManagerCompat
        get() = NotificationManagerCompat.from(context)

    /**
     * Returns the [Notification] that will be shown the entire time
     * the app is in the background, so that this [IBleService] can stay alive
     * and continue to process BLE events, as well as handle other background
     * notifications.
     */
    override val foregroundNotification: Notification
        get() {
            val intent = Intent(context, activityClass)
            val pendingIntent = PendingIntent.getActivity(
                context,
                FOREGROUND_SERVICE_NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = TAG
                if (notificationManager.getNotificationChannel(channelId) == null) {
                    // setup channel for BleService, to use so it can be a foreground service
                    NotificationChannel(
                        channelId,
                        context.getString(channelNameResId),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                        .apply {
                            enableVibration(false)
                            enableLights(true)
                            setSound(null, null)
                            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                            lightColor = ContextCompat.getColor(context, lightColorResId)
                        }
                        .also { notificationManager.createNotificationChannel(it) }
                }
                NotificationCompat.Builder(context, channelId)
            } else {
                @Suppress("DEPRECATION")
                NotificationCompat.Builder(context)
            }
            return builder
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(context.getString(titleResId))
                .setContentText(context.getString(contentResId))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(iconResId)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build()
        }

    override fun startForegroundNotification() {
        if (!isForegroundNotificationVisible) {
            try {
                val notification = foregroundNotification
                notificationManager.notify(foregroundNotificationId, notification)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    service.startForeground(
                        foregroundNotificationId,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
                    )
                } else {
                    service.startForeground(foregroundNotificationId, notification)
                }
                DelayedNotificationReceiver.toggleReceiver(
                    enabled = true,
                    context = service
                )
                isForegroundNotificationVisible = true
            } catch (ex: Throwable) {
                Log.e(TAG, "Failed to call startForegroundNotification", ex)
            }
        } else {
            Log.v(TAG, "Already showing foreground notification")
        }
    }

    override fun stopForegroundNotification() {
        if (isForegroundNotificationVisible) {
            try {
                notificationManager.cancel(foregroundNotificationId)
            } catch (ex: Throwable) {
                Log.e(TAG, "Failed to cancel foreground notification", ex)
            }
            service.stopForeground(true)
            DelayedNotificationReceiver.toggleReceiver(
                enabled = false,
                context = service
            )
            isForegroundNotificationVisible = false
        } else {
            Log.v(TAG, "Foreground notification is not showing.")
        }
    }
}
