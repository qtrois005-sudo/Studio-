package com.geovoice.app.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class VoiceEngineState {
    object Initializing : VoiceEngineState()
    object Ready : VoiceEngineState()
    object Unavailable : VoiceEngineState()
}

/**
 * Encapsule TextToSpeech (section 23). Signale explicitement l'indisponibilité du moteur
 * vocal plutôt que d'échouer silencieusement (section 46 : "TTS indisponible" doit être géré).
 */
class VoiceEngine(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null

    var state: VoiceEngineState = VoiceEngineState.Initializing
        private set

    private val readyCallbacks = mutableListOf<() -> Unit>()

    init {
        tts = TextToSpeech(appContext) { status ->
            state = if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.FRENCH
                VoiceEngineState.Ready
            } else {
                VoiceEngineState.Unavailable
            }
            readyCallbacks.forEach { it.invoke() }
            readyCallbacks.clear()
        }
    }

    fun preferMaleVoice(preferMale: Boolean) {
        val engine = tts ?: return
        val voices: Set<Voice> = try {
            engine.voices ?: return
        } catch (e: Exception) {
            return
        }
        val target = voices.firstOrNull { voice ->
            val name = voice.name.lowercase(Locale.getDefault())
            voice.locale.language == Locale.FRENCH.language &&
                (if (preferMale) "male" in name && "female" !in name else "female" in name)
        }
        if (target != null) {
            engine.voice = target
        }
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    fun setVolume(volume: Float) {
        // Le volume est appliqué au moment de la lecture via les paramètres d'utterance (speak()).
        currentVolume = volume.coerceIn(0f, 1f)
    }

    private var currentVolume: Float = 1.0f

    /** Émet `true` quand l'énoncé identifié par [utteranceId] est terminé. */
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()): Flow<Boolean> = callbackFlow {
        val engine = tts
        if (engine == null || state != VoiceEngineState.Ready) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val listener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                trySend(true)
                close()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                trySend(false)
                close()
            }
        }
        engine.setOnUtteranceProgressListener(listener)

        val params = android.os.Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, currentVolume)
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

        awaitClose { }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }
}
