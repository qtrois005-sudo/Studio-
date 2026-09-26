package com.geovoice.app.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.geovoice.app.core.location.GeoPosition
import com.geovoice.app.core.location.LocationRequestConfig
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Location Engine (section 9.1). Source unique de vérité pour la position réelle du
 * téléphone. Ne fabrique jamais de position : relaie uniquement ce que retourne
 * FusedLocationProviderClient. La validation des positions (bruit, sauts GPS) est
 * déléguée à LocationQualityEngine (domain/tracking), pas ici.
 */
class LocationRepository(context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val appContext = context.applicationContext

    @SuppressLint("MissingPermission")
    fun observePositions(config: LocationRequestConfig): Flow<GeoPosition> = callbackFlow {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            config.intervalMillis
        )
            .setMinUpdateIntervalMillis(config.minUpdateIntervalMillis)
            .setMinUpdateDistanceMeters(config.minUpdateDistanceMeters)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                trySend(
                    GeoPosition(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracyMeters = location.accuracy,
                        speedMetersPerSecond = if (location.hasSpeed()) location.speed else null,
                        bearingDegrees = if (location.hasBearing()) location.bearing else null,
                        timestampMillis = location.time
                    )
                )
            }
        }

        fusedClient.requestLocationUpdates(request, callback, appContext.mainLooper)

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun lastKnownPosition(): GeoPosition? {
        val location = try {
            com.google.android.gms.tasks.Tasks.await(fusedClient.lastLocation)
        } catch (e: Exception) {
            null
        } ?: return null

        return GeoPosition(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            speedMetersPerSecond = if (location.hasSpeed()) location.speed else null,
            bearingDegrees = if (location.hasBearing()) location.bearing else null,
            timestampMillis = location.time
        )
    }

    /** Demande activement une position fraîche, sans dépendre d'un cache déjà rempli. */
    @SuppressLint("MissingPermission")
    suspend fun currentPosition(): GeoPosition? {
        return try {
            val cancellationSource = com.google.android.gms.tasks.CancellationTokenSource()
            val location = com.google.android.gms.tasks.Tasks.await(
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationSource.token
                )
            )
            location?.let {
                GeoPosition(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracyMeters = it.accuracy,
                    speedMetersPerSecond = if (it.hasSpeed()) it.speed else null,
                    bearingDegrees = if (it.hasBearing()) it.bearing else null,
                    timestampMillis = it.time
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
