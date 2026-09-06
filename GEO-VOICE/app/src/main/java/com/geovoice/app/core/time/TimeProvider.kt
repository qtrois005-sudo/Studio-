package com.geovoice.app.core.time

/**
 * Abstraction du temps système. Permet de tester les moteurs (distance, seuils,
 * anti-répétition) avec une horloge simulée, sans dépendre de System.currentTimeMillis().
 */
interface TimeProvider {
    fun nowMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
