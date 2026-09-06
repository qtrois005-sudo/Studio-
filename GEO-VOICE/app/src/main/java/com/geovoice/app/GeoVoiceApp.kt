package com.geovoice.app

import android.app.Application
import com.geovoice.app.core.notification.NotificationEngine

class GeoVoiceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationEngine(this).ensureChannelsCreated()
    }
}
