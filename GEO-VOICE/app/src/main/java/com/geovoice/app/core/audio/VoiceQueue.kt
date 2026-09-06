package com.geovoice.app.core.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AnnouncementPriority { LOW, NORMAL, HIGH }

data class VoiceMessage(
    val text: String,
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    /** Regroupe des messages qui ne doivent jamais se chevaucher/se répéter coup sur coup (section 24/26). */
    val dedupeKey: String? = null
)

/**
 * File d'attente séquentielle des annonces vocales (section 24). Empêche que plusieurs
 * annonces se chevauchent ("annonce 1 + annonce 2 + annonce 3 en même temps"), en les
 * lisant une par une, dans l'ordre de priorité, et en supprimant les doublons obsolètes
 * (mêmes `dedupeKey` déjà en attente).
 */
class VoiceQueue(
    private val voiceEngine: VoiceEngine,
    private val scope: CoroutineScope
) {
    private val pending = ArrayDeque<VoiceMessage>()
    private var isSpeaking = false
    private var workerJob: Job? = null

    @Synchronized
    fun enqueue(message: VoiceMessage) {
        // Anti-répétition / anti-chevauchement : si un message avec la même clé attend déjà,
        // on le remplace plutôt que d'empiler un doublon (section 24 : "fusionner", "supprimer
        // les messages obsolètes").
        if (message.dedupeKey != null) {
            pending.removeAll { it.dedupeKey == message.dedupeKey }
        }

        when (message.priority) {
            AnnouncementPriority.HIGH -> pending.addFirst(message)
            else -> pending.addLast(message)
        }

        if (!isSpeaking) {
            processNext()
        }
    }

    @Synchronized
    private fun processNext() {
        val next = pending.removeFirstOrNull() ?: run {
            isSpeaking = false
            return
        }
        isSpeaking = true
        workerJob = scope.launch {
            voiceEngine.speak(next.text).first()
            synchronized(this@VoiceQueue) {
                processNext()
            }
        }
    }

    fun clear() {
        synchronized(this) {
            pending.clear()
        }
        voiceEngine.stop()
        isSpeaking = false
    }
}
