package com.geovoice.app.domain.voice

import com.geovoice.app.domain.confidence.ConfidenceLevel
import com.geovoice.app.domain.geography.GeographyPlace

/** Un seuil de distance a été franchi (section 13). */
data class DistanceAnnouncementInput(val cumulativeKilometers: Double)

/** Un résultat géographique est disponible, avec son niveau de confiance (section 19). */
data class GeographyAnnouncementInput(
    val place: GeographyPlace,
    val confidence: ConfidenceLevel
)

/**
 * Construit les messages vocaux/textuels (section 21), en appliquant strictement la
 * règle absolue de la section 18 : ne jamais transformer une estimation en certitude.
 * - HIGH   → formulation affirmative ("Vous êtes actuellement à ...")
 * - MEDIUM → formulation prudente ("Vous êtes probablement dans cette zone.")
 * - LOW    → pas d'annonce du lieu (silence sur cette partie du message)
 */
class MessageBuilder {

    fun buildDistanceMessage(input: DistanceAnnouncementInput, mode: AnnouncementMode): String {
        val km = formatKilometers(input.cumulativeKilometers)
        return when (mode) {
            AnnouncementMode.SHORT -> "$km kilomètres."
            AnnouncementMode.STANDARD, AnnouncementMode.DETAILED ->
                "Vous avez parcouru $km kilomètres."
        }
    }

    /** Retourne `null` si la confiance est trop faible pour justifier une annonce (section 19 : LOW). */
    fun buildGeographyMessage(input: GeographyAnnouncementInput, mode: AnnouncementMode): String? {
        val place = input.place
        return when (input.confidence) {
            ConfidenceLevel.HIGH -> {
                val label = place.neighborhood ?: place.city ?: place.commune ?: return null
                when (mode) {
                    AnnouncementMode.SHORT -> label
                    AnnouncementMode.STANDARD -> "Vous êtes actuellement à $label."
                    AnnouncementMode.DETAILED -> buildDetailedLabel(place)?.let {
                        "Vous êtes actuellement à $it."
                    } ?: "Vous êtes actuellement à $label."
                }
            }
            ConfidenceLevel.MEDIUM -> {
                if (place.city == null && place.commune == null) return null
                "Vous êtes probablement dans cette zone."
            }
            ConfidenceLevel.LOW -> null
        }
    }

    /** Combine distance + géographie dans une seule annonce si les deux surviennent ensemble (section 21). */
    fun buildCombinedMessage(
        distance: DistanceAnnouncementInput?,
        geography: GeographyAnnouncementInput?,
        mode: AnnouncementMode
    ): String? {
        val distancePart = distance?.let { buildDistanceMessage(it, mode) }
        val geographyPart = geography?.let { buildGeographyMessage(it, mode) }

        return listOfNotNull(distancePart, geographyPart)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = " ")
    }

    private fun buildDetailedLabel(place: GeographyPlace): String? {
        val parts = listOfNotNull(place.neighborhood, place.city ?: place.commune)
        return parts.takeIf { it.isNotEmpty() }?.joinToString(", ")
    }

    private fun formatKilometers(value: Double): String {
        val rounded = Math.round(value * 10) / 10.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString().replace('.', ',')
        }
    }
}
