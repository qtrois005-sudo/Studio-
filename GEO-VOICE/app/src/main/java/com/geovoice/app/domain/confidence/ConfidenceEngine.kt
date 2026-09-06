package com.geovoice.app.domain.confidence

/**
 * Niveau de confiance d'une identification géographique (section 19).
 * HIGH   → annonce détaillée possible.
 * MEDIUM → formulation prudente obligatoire.
 * LOW    → annonce générale, ou aucune annonce.
 */
enum class ConfidenceLevel {
    HIGH, MEDIUM, LOW
}

/** Entrées utilisées pour évaluer la confiance d'une position/résultat géographique. */
data class ConfidenceInputs(
    val accuracyMeters: Float,
    val hasCityOrCommune: Boolean,
    val hasNeighborhoodOrStreet: Boolean,
    val geocodingResultIsStale: Boolean
)

/**
 * Calcule un niveau de confiance à partir de la précision GPS et de la richesse du
 * résultat de géocodage. Règle absolue (section 18) : ne jamais transformer une
 * estimation en certitude — c'est ce niveau qui pilote la formulation dans MessageBuilder.
 */
class ConfidenceEngine {

    fun evaluate(inputs: ConfidenceInputs): ConfidenceLevel {
        if (inputs.geocodingResultIsStale) return ConfidenceLevel.LOW
        if (!inputs.hasCityOrCommune) return ConfidenceLevel.LOW

        return when {
            inputs.accuracyMeters <= 30f && inputs.hasNeighborhoodOrStreet -> ConfidenceLevel.HIGH
            inputs.accuracyMeters <= 100f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    }
}
