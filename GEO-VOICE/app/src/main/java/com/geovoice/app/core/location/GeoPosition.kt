package com.geovoice.app.core.location

/**
 * Position brute telle que reçue du capteur de localisation du téléphone.
 * GEO VOICE ne fabrique jamais cet objet : il provient uniquement de FusedLocationProviderClient
 * (section 9.2 — "GEO VOICE ne doit jamais fabriquer une position").
 */
data class GeoPosition(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedMetersPerSecond: Float?,
    val bearingDegrees: Float?,
    val timestampMillis: Long
)

/** Profils batterie influant sur la fréquence et la précision de la localisation (section 44). */
enum class BatteryProfile {
    ECONOMY,
    NORMAL,
    PRECISION
}

/** Paramètres de localisation dérivés du profil batterie choisi par l'utilisateur. */
data class LocationRequestConfig(
    val intervalMillis: Long,
    val minUpdateIntervalMillis: Long,
    val minUpdateDistanceMeters: Float
) {
    companion object {
        fun forProfile(profile: BatteryProfile): LocationRequestConfig = when (profile) {
            BatteryProfile.ECONOMY -> LocationRequestConfig(
                intervalMillis = 30_000L,
                minUpdateIntervalMillis = 15_000L,
                minUpdateDistanceMeters = 50f
            )
            BatteryProfile.NORMAL -> LocationRequestConfig(
                intervalMillis = 10_000L,
                minUpdateIntervalMillis = 5_000L,
                minUpdateDistanceMeters = 15f
            )
            BatteryProfile.PRECISION -> LocationRequestConfig(
                intervalMillis = 3_000L,
                minUpdateIntervalMillis = 1_000L,
                minUpdateDistanceMeters = 3f
            )
        }
    }
}
