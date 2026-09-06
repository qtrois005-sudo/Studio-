package com.geovoice.app.domain.distance

/** Seuils par défaut en mètres (section 13). */
object DefaultThresholds {
    val METERS: List<Long> = listOf(1_000, 2_000, 5_000, 10_000, 20_000, 50_000)
}

data class ThresholdEvent(val thresholdMeters: Long)

/**
 * Distance Engine (section 12) : distance CUMULÉE des segments validés, jamais la
 * distance à vol d'oiseau entre deux points éloignés (section 12) — même en cas
 * d'aller-retour (section 15), le trajet réellement parcouru continue de s'additionner.
 *
 * Gère aussi l'état de chaque seuil (section 16) : chaque seuil ne doit être
 * annoncé qu'une seule fois par session, dans l'ordre où il est atteint.
 */
class DistanceEngine(
    customThresholdsMeters: List<Long> = emptyList()
) {
    private val allThresholds: List<Long> =
        (DefaultThresholds.METERS + customThresholdsMeters).distinct().sorted()

    private val announcedThresholds = mutableSetOf<Long>()

    var cumulativeDistanceMeters: Double = 0.0
        private set

    /**
     * À appeler uniquement avec le déplacement d'une position VALIDÉE par
     * LocationQualityEngine (movedMeters), jamais avec une position rejetée.
     * Retourne les nouveaux seuils franchis (généralement 0 ou 1, exceptionnellement plusieurs
     * si une longue portion de trajet a été traitée en une fois).
     */
    fun addValidatedMovement(movedMeters: Double): List<ThresholdEvent> {
        if (movedMeters <= 0.0) return emptyList()
        cumulativeDistanceMeters += movedMeters

        val newlyCrossed = mutableListOf<ThresholdEvent>()
        for (threshold in allThresholds) {
            if (threshold !in announcedThresholds && cumulativeDistanceMeters >= threshold) {
                announcedThresholds += threshold
                newlyCrossed += ThresholdEvent(threshold)
            }
        }
        return newlyCrossed
    }

    fun nextThresholdMeters(): Long? =
        allThresholds.firstOrNull { it !in announcedThresholds }

    /** Nouvelle session (section 16 : "nouvelle session") — remet à zéro distance et seuils. */
    fun reset() {
        cumulativeDistanceMeters = 0.0
        announcedThresholds.clear()
    }
}
