package com.geovoice.app.domain.geography

import com.geovoice.app.core.location.GeoPosition
import com.geovoice.app.core.time.TimeProvider
import com.geovoice.app.data.geocoding.GeocodingRepository
import com.geovoice.app.domain.distance.GeoMath

/**
 * Orchestration du géocodage (section 17) avec optimisation (section 36) : évite d'appeler
 * le géocodeur à chaque micro-déplacement, ce qui limiterait inutilement réseau/batterie/coûts.
 */
class GeographyEngine(
    private val geocodingRepository: GeocodingRepository,
    private val timeProvider: TimeProvider,
    private val minMovementBeforeRegeocodeMeters: Double = 120.0,
    private val minIntervalBetweenCallsMillis: Long = 20_000L,
    private val staleAfterMillis: Long = 120_000L
) {
    private var lastResult: GeographyPlace? = null
    private var lastQueryPosition: GeoPosition? = null
    private var lastQueryAtMillis: Long = 0L

    /**
     * Retourne le dernier résultat en cache si le déplacement est trop faible ou l'appel
     * trop récent (section 36), sinon interroge le géocodeur. Ne fabrique jamais de valeur :
     * un échec de géocodage retourne `null` plutôt qu'une estimation inventée (section 18).
     */
    suspend fun resolve(position: GeoPosition): GeographyPlace? {
        val now = timeProvider.nowMillis()
        val previousQuery = lastQueryPosition

        val movedEnough = previousQuery == null || GeoMath.haversineMeters(
            previousQuery.latitude, previousQuery.longitude,
            position.latitude, position.longitude
        ) >= minMovementBeforeRegeocodeMeters

        val enoughTimePassed = (now - lastQueryAtMillis) >= minIntervalBetweenCallsMillis

        if (lastResult != null && !movedEnough) {
            return lastResult
        }
        if (!enoughTimePassed && lastResult != null) {
            return lastResult
        }

        val fresh = geocodingRepository.reverseGeocode(position.latitude, position.longitude)
        lastQueryPosition = position
        lastQueryAtMillis = now
        if (fresh != null) {
            lastResult = fresh
        }
        return fresh ?: lastResult
    }

    fun isCurrentResultStale(): Boolean {
        val result = lastResult ?: return true
        return (timeProvider.nowMillis() - result.resolvedAtMillis) > staleAfterMillis
    }

    fun reset() {
        lastResult = null
        lastQueryPosition = null
        lastQueryAtMillis = 0L
    }
}
