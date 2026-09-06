package com.geovoice.app.domain.trip

import com.geovoice.app.core.location.GeoPosition
import com.geovoice.app.core.time.TimeProvider
import com.geovoice.app.data.database.AnnouncementEntity
import com.geovoice.app.data.database.LocationPointEntity
import com.geovoice.app.data.database.TripDao
import com.geovoice.app.data.database.TripEntity

enum class SessionState { IDLE, ACTIVE, PAUSED, ENDED }

/**
 * Gère le cycle de vie d'une session de suivi (section 40 : démarrée, suspendue, reprise,
 * terminée, sauvegardée) et persiste les points/annonces si l'historique est activé
 * (section 37). Si l'historique est désactivé, le trajet n'est pas persisté — conformément
 * à la confidentialité (section 47 : l'utilisateur contrôle ce qui est enregistré).
 */
class TripEngine(
    private val tripDao: TripDao,
    private val timeProvider: TimeProvider,
    private val historyEnabled: () -> Boolean
) {
    var state: SessionState = SessionState.IDLE
        private set

    private var currentTripId: Long? = null
    private var startedAtMillis: Long = 0L
    private var pointCount = 0
    private var speedSumMetersPerSecond = 0.0

    suspend fun startSession() {
        state = SessionState.ACTIVE
        startedAtMillis = timeProvider.nowMillis()
        pointCount = 0
        speedSumMetersPerSecond = 0.0
        currentTripId = if (historyEnabled()) {
            tripDao.insertTrip(
                TripEntity(
                    startedAtMillis = startedAtMillis,
                    endedAtMillis = null,
                    distanceMeters = 0.0,
                    averageSpeedMetersPerSecond = null,
                    gpsQualitySummary = "en cours"
                )
            )
        } else {
            null
        }
    }

    fun pauseSession() {
        if (state == SessionState.ACTIVE) state = SessionState.PAUSED
    }

    fun resumeSession() {
        if (state == SessionState.PAUSED) state = SessionState.ACTIVE
    }

    suspend fun recordValidatedPoint(position: GeoPosition, cumulativeDistanceMeters: Double) {
        val tripId = currentTripId ?: return
        pointCount += 1
        position.speedMetersPerSecond?.let { speedSumMetersPerSecond += it }

        tripDao.insertLocationPoint(
            LocationPointEntity(
                tripId = tripId,
                latitude = position.latitude,
                longitude = position.longitude,
                timestampMillis = position.timestampMillis,
                accuracyMeters = position.accuracyMeters,
                speedMetersPerSecond = position.speedMetersPerSecond,
                bearingDegrees = position.bearingDegrees,
                wasValidated = true
            )
        )

        val trip = tripDao.getTrip(tripId) ?: return
        tripDao.updateTrip(trip.copy(distanceMeters = cumulativeDistanceMeters))
    }

    suspend fun recordAnnouncement(type: String, message: String, thresholdMeters: Long?, wasSpoken: Boolean) {
        val tripId = currentTripId ?: return
        tripDao.insertAnnouncement(
            AnnouncementEntity(
                tripId = tripId,
                type = type,
                message = message,
                timestampMillis = timeProvider.nowMillis(),
                thresholdMeters = thresholdMeters,
                wasSpoken = wasSpoken
            )
        )
    }

    suspend fun endSession() {
        val tripId = currentTripId
        if (tripId != null) {
            val trip = tripDao.getTrip(tripId)
            if (trip != null) {
                val averageSpeed = if (pointCount > 0) speedSumMetersPerSecond / pointCount else null
                tripDao.updateTrip(
                    trip.copy(
                        endedAtMillis = timeProvider.nowMillis(),
                        averageSpeedMetersPerSecond = averageSpeed,
                        gpsQualitySummary = "terminé"
                    )
                )
            }
        }
        state = SessionState.ENDED
        currentTripId = null
    }
}
