package com.geovoice.app.core.security

import com.geovoice.app.BuildConfig

/**
 * Point d'accès unique à la clé Google Maps Platform. La clé elle-même n'est jamais
 * écrite en dur ici : elle est injectée au moment du build depuis local.properties
 * ou une variable d'environnement (voir app/build.gradle.kts et section 63).
 */
object ApiKeyProvider {

    val mapsApiKey: String
        get() = BuildConfig.MAPS_API_KEY

    val isMapsConfigured: Boolean
        get() = mapsApiKey.isNotBlank()
}
