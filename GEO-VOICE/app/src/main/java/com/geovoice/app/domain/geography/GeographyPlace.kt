package com.geovoice.app.domain.geography

/**
 * Résultat brut de géocodage inversé, structuré selon la hiérarchie de la section 17 :
 * Pays > Région > Ville > Commune > Quartier > Rue > Point d'intérêt.
 *
 * IMPORTANT (honnêteté technique) : le Geocoder Android natif fournit fiablement pays,
 * région, ville et rue. Il ne fournit PAS de notion fiable de "commune" distincte de la
 * ville, ni de point d'intérêt nommé — ces champs restent `null` tant qu'une source plus
 * riche (Google Places API) n'est pas intégrée (voir section 35, prévu en P1/P2). Conformément
 * à la règle "ne jamais inventer" (section 18), GeographyEngine ne doit jamais combler ces
 * champs manquants par une supposition.
 */
data class GeographyPlace(
    val country: String? = null,
    val region: String? = null,
    val city: String? = null,
    val commune: String? = null,
    val neighborhood: String? = null,
    val street: String? = null,
    val pointOfInterest: String? = null,
    val resolvedAtMillis: Long
) {
    val hasCityLevel: Boolean get() = city != null || commune != null
    val hasFineGrainLevel: Boolean get() = neighborhood != null || street != null
}
