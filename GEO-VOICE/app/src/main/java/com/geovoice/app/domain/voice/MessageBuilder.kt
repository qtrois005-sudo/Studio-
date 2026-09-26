package com.geovoice.app.domain.voice

import com.geovoice.app.domain.confidence.ConfidenceLevel
import com.geovoice.app.domain.geography.GeographyPlace

data class DistanceAnnouncementInput(val cumulativeKilometers: Double)

data class GeographyAnnouncementInput(
    val place: GeographyPlace,
    val confidence: ConfidenceLevel
)

/**
 * Construit les messages vocaux/textuels (section 21), en respectant la règle
 * absolue (section 18) : ne jamais transformer une estimation en certitude.
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

    fun buildGeographyMessage(input: GeographyAnnouncementInput, mode: AnnouncementMode): String? {
        val place = input.place
        return when (input.confidence) {
            ConfidenceLevel.HIGH -> {
                val cityLabel = place.city ?: place.commune ?: return null
                val precise = buildPreciseLocationPhrase(place)

                when (mode) {
                    AnnouncementMode.SHORT -> precise ?: cityLabel
                    AnnouncementMode.STANDARD -> {
                        if (precise != null) "Vous êtes à $cityLabel, $precise."
                        else "Vous êtes actuellement à $cityLabel."
                    }
                    AnnouncementMode.DETAILED -> {
                        if (precise != null) "Vous êtes à $cityLabel, précisément $precise."
                        else "Vous êtes actuellement à $cityLabel."
                    }
                }
            }
            ConfidenceLevel.MEDIUM -> {
                if (place.city == null && place.commune == null) return null
                "Vous êtes probablement dans cette zone."
            }
            ConfidenceLevel.LOW -> null
        }
    }

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

    /**
     * Construit la partie "précise" de l'annonce (quartier, point de repère, rue),
     * en ne combinant que ce qui est réellement connu — jamais d'invention.
     */
    private fun buildPreciseLocationPhrase(place: GeographyPlace): String? {
        val parts = listOfNotNull(
            place.pointOfInterest,
            place.neighborhood,
            place.street?.let { street ->
                place.streetNumberLabel()?.let { number -> "$street $number" } ?: street
            }
        )
        return parts.firstOrNull()
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

private fun GeographyPlace.streetNumberLabel(): String? = null
