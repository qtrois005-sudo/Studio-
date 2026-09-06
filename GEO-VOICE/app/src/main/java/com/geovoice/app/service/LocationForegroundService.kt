package com.geovoice.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.geovoice.app.core.audio.AnnouncementPriority
import com.geovoice.app.core.audio.AudioFocusManager
import com.geovoice.app.core.audio.VoiceEngine
import com.geovoice.app.core.audio.VoiceMessage
import com.geovoice.app.core.audio.VoiceQueue
import com.geovoice.app.core.location.BatteryProfile
import com.geovoice.app.core.location.LocationRequestConfig
import com.geovoice.app.core.notification.NotificationEngine
import com.geovoice.app.core.notification.NotificationIds
import com.geovoice.app.core.permission.PermissionManager
import com.geovoice.app.core.time.SystemTimeProvider
import com.geovoice.app.data.database.AppDatabase
import com.geovoice.app.data.geocoding.GeocodingRepository
import com.geovoice.app.data.location.LocationRepository
import com.geovoice.app.data.preferences.PreferencesRepository
import com.geovoice.app.domain.confidence.ConfidenceEngine
import com.geovoice.app.domain.confidence.ConfidenceInputs
import com.geovoice.app.domain.distance.DistanceEngine
import com.geovoice.app.domain.geography.GeographyEngine
import com.geovoice.app.domain.tracking.LocationQualityEngine
import com.geovoice.app.domain.tracking.QualityResult
import com.geovoice.app.domain.trip.TripEngine
import com.geovoice.app.domain.voice.DistanceAnnouncementInput
import com.geovoice.app.domain.voice.GeographyAnnouncementInput
import com.geovoice.app.domain.voice.MessageBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Service de premier plan qui relie tous les moteurs (section 51) :
 * GPS → LocationEngine → LocationQualityEngine → DistanceEngine → GeographyEngine
 * → ConfidenceEngine → MessageBuilder → VoiceQueue/VoiceEngine, en parallèle avec
 * TripEngine → base de données, et NotificationEngine pour les notifications.
 */
class LocationForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var trackingJob: Job? = null

    private lateinit var preferences: PreferencesRepository
    private lateinit var notificationEngine: NotificationEngine
    private lateinit var locationRepository: LocationRepository
    private lateinit var qualityEngine: LocationQualityEngine
    private lateinit var distanceEngine: DistanceEngine
    private lateinit var geographyEngine: GeographyEngine
    private lateinit var confidenceEngine: ConfidenceEngine
    private lateinit var messageBuilder: MessageBuilder
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var voiceQueue: VoiceQueue
    private lateinit var audioFocusManager: AudioFocusManager
    private lateinit var tripEngine: TripEngine

    override fun onCreate() {
        super.onCreate()
        val timeProvider = SystemTimeProvider()

        preferences = PreferencesRepository(applicationContext)
        notificationEngine = NotificationEngine(applicationContext).apply { ensureChannelsCreated() }
        locationRepository = LocationRepository(applicationContext)
        qualityEngine = LocationQualityEngine()
        val geocodingRepository = GeocodingRepository(applicationContext, timeProvider)
        geographyEngine = GeographyEngine(geocodingRepository, timeProvider)
        confidenceEngine = ConfidenceEngine()
        messageBuilder = MessageBuilder()
        voiceEngine = VoiceEngine(applicationContext)
        voiceQueue = VoiceQueue(voiceEngine, serviceScope)
        audioFocusManager = AudioFocusManager(applicationContext)
        distanceEngine = DistanceEngine()

        val tripDao = AppDatabase.getInstance(applicationContext).tripDao()
        tripEngine = TripEngine(
            tripDao = tripDao,
            timeProvider = timeProvider,
            historyEnabled = { runBlockingPreference { preferences.historyEnabled.first() } }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                return START_NOT_STICKY
            }
            else -> startTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        if (!PermissionManager.hasAnyLocation(applicationContext)) {
            // Sécurité : sans permission, on ne démarre jamais le suivi (section 8).
            stopSelf()
            return
        }

        val notification = notificationEngine.buildTrackingNotification(
            getString(com.geovoice.app.R.string.notif_tracking_text)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NotificationIds.TRACKING_FOREGROUND,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NotificationIds.TRACKING_FOREGROUND, notification)
        }

        if (trackingJob?.isActive == true) return

        trackingJob = serviceScope.launch {
            val batteryProfile = preferences.batteryProfile.first()
            val announcementMode = preferences.announcementMode.first()
            val silentMode = preferences.silentMode.first()
            val voiceIsMale = preferences.voiceIsMale.first()
            val voiceSpeed = preferences.voiceSpeed.first()
            val voiceVolume = preferences.voiceVolume.first()

            voiceEngine.preferMaleVoice(voiceIsMale)
            voiceEngine.setSpeechRate(voiceSpeed)
            voiceEngine.setVolume(voiceVolume)

            distanceEngine.reset()
            qualityEngine.reset()
            geographyEngine.reset()
            tripEngine.startSession()
            preferences.setWasTrackingActive(true)

            val config = LocationRequestConfig.forProfile(batteryProfile)

            locationRepository.observePositions(config).collect { position ->
                when (val result = qualityEngine.evaluate(position)) {
                    is QualityResult.Rejected -> {
                        // Une position rejetée n'alimente ni distance ni annonce (section 10).
                    }
                    is QualityResult.Accepted -> {
                        val thresholdEvents = distanceEngine.addValidatedMovement(result.movedMeters)
                        tripEngine.recordValidatedPoint(position, distanceEngine.cumulativeDistanceMeters)

                        val place = geographyEngine.resolve(position)
                        val confidence = place?.let {
                            confidenceEngine.evaluate(
                                ConfidenceInputs(
                                    accuracyMeters = position.accuracyMeters,
                                    hasCityOrCommune = it.hasCityLevel,
                                    hasNeighborhoodOrStreet = it.hasFineGrainLevel,
                                    geocodingResultIsStale = geographyEngine.isCurrentResultStale()
                                )
                            )
                        }

                        val distanceInput = thresholdEvents.lastOrNull()?.let {
                            DistanceAnnouncementInput(it.thresholdMeters / 1000.0)
                        }
                        val geographyInput = if (place != null && confidence != null) {
                            GeographyAnnouncementInput(place, confidence)
                        } else null

                        val message = messageBuilder.buildCombinedMessage(distanceInput, geographyInput, announcementMode)
                        if (message != null) {
                            if (!silentMode) {
                                audioFocusManager.requestDuckingFocus()
                                voiceQueue.enqueue(
                                    VoiceMessage(
                                        text = message,
                                        priority = AnnouncementPriority.NORMAL,
                                        dedupeKey = "combined"
                                    )
                                )
                            }
                            tripEngine.recordAnnouncement(
                                type = if (distanceInput != null) "DISTANCE" else "GEOGRAPHY",
                                message = message,
                                thresholdMeters = thresholdEvents.lastOrNull()?.thresholdMeters,
                                wasSpoken = !silentMode
                            )
                            if (distanceInput != null) {
                                notificationEngine.showDistanceNotification(
                                    NotificationIds.DISTANCE_BASE, getString(com.geovoice.app.R.string.notif_channel_distance), message
                                )
                            } else {
                                notificationEngine.showGeographyNotification(
                                    NotificationIds.GEOGRAPHY_BASE, getString(com.geovoice.app.R.string.notif_channel_geography), message
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        voiceQueue.clear()
        audioFocusManager.abandonFocus()
        serviceScope.launch {
            tripEngine.endSession()
            preferences.setWasTrackingActive(false)
        }
        ServiceCompat.stopForeground(this, Service.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        voiceEngine.shutdown()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Petit utilitaire pour lire une préférence de façon synchrone dans un lambda non suspendu.
    // Utilisé uniquement pour TripEngine.historyEnabled (lecture rapide, DataStore local).
    private fun <T> runBlockingPreference(block: suspend () -> T): T =
        kotlinx.coroutines.runBlocking { block() }

    companion object {
        const val ACTION_START = "com.geovoice.app.action.START"
        const val ACTION_STOP = "com.geovoice.app.action.STOP"

        fun startIntent(context: Context): Intent =
            Intent(context, LocationForegroundService::class.java).setAction(ACTION_START)

        fun stopIntent(context: Context): Intent =
            Intent(context, LocationForegroundService::class.java).setAction(ACTION_STOP)

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, startIntent(context))
        }

        fun stop(context: Context) {
            context.startService(stopIntent(context))
        }
    }
}
