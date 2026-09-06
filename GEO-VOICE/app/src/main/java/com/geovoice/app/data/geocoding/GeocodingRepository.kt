package com.geovoice.app.data.geocoding

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.geovoice.app.core.time.TimeProvider
import com.geovoice.app.domain.geography.GeographyPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Implémente le géocodage inversé réel (section 35/17). Ne retourne jamais une donnée
 * inventée : si le service ne répond pas ou ne connaît pas un niveau, le champ correspondant
 * reste `null` (voir GeographyPlace).
 */
class GeocodingRepository(
    context: Context,
    private val timeProvider: TimeProvider
) {
    private val geocoder = Geocoder(context, Locale.getDefault())

    suspend fun reverseGeocode(latitude: Double, longitude: Double): GeographyPlace? {
        val address = fetchAddress(latitude, longitude) ?: return null
        return address.toGeographyPlace(timeProvider.nowMillis())
    }

    private suspend fun fetchAddress(latitude: Double, longitude: Double): Address? {
        if (!Geocoder.isPresent()) return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                try {
                    geocoder.getFromLocation(latitude, longitude, 1) { results ->
                        continuation.resume(results.firstOrNull())
                    }
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    private fun Address.toGeographyPlace(resolvedAtMillis: Long): GeographyPlace = GeographyPlace(
        country = countryName,
        region = adminArea,
        city = locality,
        // Le Geocoder Android ne distingue pas "commune" de "ville" (subAdminArea s'en
        // rapproche parfois selon les pays, sans garantie) : exposé séparément mais peut
        // rester équivalent à city selon les données disponibles localement.
        commune = subAdminArea,
        neighborhood = subLocality,
        street = thoroughfare,
        // Pas de source fiable de POI nommé sans Google Places API — volontairement null (P1/P2).
        pointOfInterest = null,
        resolvedAtMillis = resolvedAtMillis
    )
}
