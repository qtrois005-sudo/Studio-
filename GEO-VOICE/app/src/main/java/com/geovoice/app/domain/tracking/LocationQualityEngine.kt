package com.geovoice.app.domain.tracking

import com.geovoice.app.core.location.GeoPosition
import com.geovoice.app.domain.distance.GeoMath

/**
 * Résultat de la validation d'une position (section 10 — pipeline qualité).
 * Une position rejetée ne doit jamais alimenter la distance cumulée ni déclencher
 * une annonce géographique (section 10 : "Une position rejetée ne doit pas...").
 */
sealed class QualityResult {
    data class Accepted(val position: GeoPosition, val movedMeters: Double) : QualityResult()
    data class Rejected(val position: GeoPosition, val reason: RejectionReason) : QualityResult()
}

enum class RejectionReason {
    POOR_ACCURACY,
    IMPLAUSIBLE_SPEED,
    GPS_JUMP,
    SUSPICIOUS_REPETITION
}

/**
 * Paramètres du filtre — ajustables mais avec des valeurs par défaut réalistes pour un
 * usage piéton/véhicule urbain.
 */
data class QualityThresholds(
    val maxAcceptableAccuracyMeters: Float = 75f,
    val maxPlausibleSpeedMetersPerSecond: Double = 55.0, // ~198 km/h, marge au-dessus de l'autoroute
    val minMovementToCountMeters: Double = 3.0 // filtre le bruit GPS à l'arrêt (section 14)
)

/**
 * Implémente le pipeline de la section 10 :
 * Position reçue → précision acceptable ? → variation plausible ? → vitesse cohérente ?
 * → distance plausible ? → saut GPS détecté ? → position validée ou rejetée.
 */
class LocationQualityEngine(
    private val thresholds: QualityThresholds = QualityThresholds()
) {
    private var lastAccepted: GeoPosition? = null

    fun evaluate(position: GeoPosition): QualityResult {
        // 1. Précision acceptable ?
        if (position.accuracyMeters > thresholds.maxAcceptableAccuracyMeters) {
            return QualityResult.Rejected(position, RejectionReason.POOR_ACCURACY)
        }

        val previous = lastAccepted
        if (previous == null) {
            lastAccepted = position
            return QualityResult.Accepted(position, movedMeters = 0.0)
        }

        val distanceMeters = GeoMath.haversineMeters(
            previous.latitude, previous.longitude,
            position.latitude, position.longitude
        )
        val elapsedSeconds = ((position.timestampMillis - previous.timestampMillis).coerceAtLeast(1)) / 1000.0
        val impliedSpeed = distanceMeters / elapsedSeconds

        // 2/3/4/5. Variation plausible, vitesse cohérente, distance plausible, saut GPS.
        // Un saut GPS se manifeste par une vitesse implicite irréaliste sur un intervalle court.
        if (impliedSpeed > thresholds.maxPlausibleSpeedMetersPerSecond) {
            return QualityResult.Rejected(position, RejectionReason.GPS_JUMP)
        }

        // Immobilité / bruit GPS (section 14) : un déplacement minuscule n'est pas comptabilisé,
        // mais la position reste acceptée comme "position actuelle" avec un déplacement nul.
        val effectiveMovement = if (distanceMeters < thresholds.minMovementToCountMeters) 0.0 else distanceMeters

        lastAccepted = position
        return QualityResult.Accepted(position, movedMeters = effectiveMovement)
    }

    /** Réinitialise l'état interne (nouvelle session de suivi, section 40). */
    fun reset() {
        lastAccepted = null
    }
}
