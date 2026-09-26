package com.geovoice.app.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.geovoice.app.core.location.GeoPosition
import com.geovoice.app.core.location.LocationRequestConfig
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Location Engine (section 9.1). Source unique de vérité pour la position réelle du
 * téléphone. Ne fabrique jamais de position : relaie uniquement ce que retourne
 * FusedLocationProviderClient.
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
                trySend(location.toGeoPosition())
            }
        }

        fusedClient.requestLocationUpdates(request, callback, appContext.mainLooper)

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    /** Dernière position mise en cache par le système (peut être vide si jamais utilisée). */
    @SuppressLint("MissingPermission")
    suspend fun lastKnownPosition(): GeoPosition? = suspendCancellableCoroutine { continuation ->
        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                if (continuation.isActive) continuation.resumeWith(Result.success(location?.toGeoPosition()))
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resumeWith(Result.success(null))
            }
    }

    /** Demande activement une position fraîche, sans dépendre d'un cache déjà rempli. */
    @SuppressLint("MissingPermission")
    suspend fun currentPosition(): GeoPosition? = suspendCancellableCoroutine { continuation ->
        val cancellationSource = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationSource.token)
            .addOnSuccessListener { location ->
                if (continuation.isActive) continuation.resumeWith(Result.success(location?.toGeoPosition()))
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resumeWith(Result.success(null))
            }
        continuation.invokeOnCancellation { cancellationSource.cancel() }
    }
}

private fun Location.toGeoPosition(): GeoPosition = GeoPosition(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracy,
    speedMetersPerSecond = if (hasSpeed()) speed else null,
    bearingDegrees = if (hasBearing()) bearing else null,
    timestampMillis = time
)
