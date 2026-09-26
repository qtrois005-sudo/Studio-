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

    private fun Address.toGeographyPlace(resolvedAtMillis: Long): GeographyPlace {
        // featureName peut contenir le nom d'un repère local connu de la base de données
        // du géocodeur (marché, bâtiment...), mais ce n'est pas garanti. On l'utilise
        // seulement s'il apporte une information différente de la rue/numéro déjà connus.
        val rawFeature = featureName?.takeIf { it.isNotBlank() }
        val poi = rawFeature?.takeIf { it != thoroughfare && it != subThoroughfare }

        return GeographyPlace(
            country = countryName,
            region = adminArea,
            city = locality,
            commune = subAdminArea,
            neighborhood = subLocality,
            street = thoroughfare,
            pointOfInterest = poi,
            resolvedAtMillis = resolvedAtMillis
        )
    }
}
