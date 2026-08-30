package com.sleepaudio.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SleepAudioApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                "Lecture audio",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Contrôles de lecture et de minuterie de sommeil"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val PLAYBACK_CHANNEL_ID = "playback_channel"
    }
}
