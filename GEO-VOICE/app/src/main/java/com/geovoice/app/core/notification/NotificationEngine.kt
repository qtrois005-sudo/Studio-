package com.geovoice.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.geovoice.app.MainActivity
import com.geovoice.app.R

object NotificationChannels {
    const val TRACKING = "geovoice_tracking"
    const val DISTANCE = "geovoice_distance"
    const val GEOGRAPHY = "geovoice_geography"
    const val SYSTEM = "geovoice_system"
}

object NotificationIds {
    const val TRACKING_FOREGROUND = 1001
    const val DISTANCE_BASE = 2000
    const val GEOGRAPHY_BASE = 3000
    const val SYSTEM_BASE = 4000
}

/**
 * Construit les notifications décrites section 27 : permanente (suivi), distance,
 * géographique, système. Crée les canaux une seule fois au démarrage de l'application.
 */
class NotificationEngine(private val context: Context) {

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannelsCreated() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channels = listOf(
            NotificationChannel(
                NotificationChannels.TRACKING,
                context.getString(R.string.notif_channel_tracking),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = context.getString(R.string.notif_channel_tracking_desc) },
            NotificationChannel(
                NotificationChannels.DISTANCE,
                context.getString(R.string.notif_channel_distance),
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationChannels.GEOGRAPHY,
                context.getString(R.string.notif_channel_geography),
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationChannels.SYSTEM,
                context.getString(R.string.notif_channel_system),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        channels.forEach { manager.createNotificationChannel(it) }
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    /** Notification persistante requise par le foreground service de localisation (section 43). */
    fun buildTrackingNotification(contentText: String): android.app.Notification {
        return NotificationCompat.Builder(context, NotificationChannels.TRACKING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notif_tracking_title))
            .setContentText(contentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showDistanceNotification(id: Int, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.DISTANCE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(contentIntent())
            .build()
        manager.notify(id, notification)
    }

    fun showGeographyNotification(id: Int, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.GEOGRAPHY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(contentIntent())
            .build()
        manager.notify(id, notification)
    }

    fun showSystemNotification(id: Int, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.SYSTEM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(contentIntent())
            .build()
        manager.notify(id, notification)
    }
}
