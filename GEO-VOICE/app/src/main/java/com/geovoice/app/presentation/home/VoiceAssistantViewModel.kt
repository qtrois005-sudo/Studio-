package com.geovoice.app.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geovoice.app.core.audio.SpeechRecognitionEngine
import com.geovoice.app.core.audio.SpeechResult
import com.geovoice.app.core.audio.VoiceEngine
import com.geovoice.app.core.permission.PermissionManager
import com.geovoice.app.core.time.SystemTimeProvider
import com.geovoice.app.data.geocoding.GeocodingRepository
import com.geovoice.app.data.location.LocationRepository
import com.geovoice.app.domain.confidence.ConfidenceEngine
import com.geovoice.app.domain.confidence.ConfidenceInputs
import com.geovoice.app.domain.geography.GeographyEngine
import com.geovoice.app.domain.voice.AnnouncementMode
import com.geovoice.app.domain.voice.GeographyAnnouncementInput
import com.geovoice.app.domain.voice.MessageBuilder
import com.geovoice.app.domain.voice.VoiceCommand
import com.geovoice.app.domain.voice.VoiceCommandInterpreter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class VoiceAssistantState {
    object Idle : VoiceAssistantState()
    object Listening : VoiceAssistantState()
    object Thinking : VoiceAssistantState()
    data class Answered(val text: String) : VoiceAssistantState()
    data class Error(val message: String) : VoiceAssistantState()
}

/**
 * Assistant vocal minimal : écoute une commande ("où suis-je"), réutilise les
 * moteurs déjà existants (localisation, géographie, confiance) et répond en TTS.
 */
class VoiceAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val speechEngine = SpeechRecognitionEngine(application)
    private val voiceEngine = VoiceEngine(application)
    private val locationRepository = LocationRepository(application)
    private val timeProvider = SystemTimeProvider()
    private val geocodingRepository = GeocodingRepository(application, timeProvider)
    private val geographyEngine = GeographyEngine(geocodingRepository, timeProvider)
    private val confidenceEngine = ConfidenceEngine()
    private val messageBuilder = MessageBuilder()

    private val _state = MutableStateFlow<VoiceAssistantState>(VoiceAssistantState.Idle)
    val state: StateFlow<VoiceAssistantState> = _state.asStateFlow()

    fun startListening() {
        val context = getApplication<Application>()
        if (!PermissionManager.hasAnyLocation(context)) {
            _state.value = VoiceAssistantState.Error("Localisation non autorisée.")
            return
        }

        _state.value = VoiceAssistantState.Listening
        viewModelScope.launch {
            speechEngine.listenOnce().collect { result ->
                when (result) {
                    is SpeechResult.Error -> {
                        _state.value = VoiceAssistantState.Error(result.message)
                    }
                    is SpeechResult.Recognized -> {
                        handleCommand(result.text)
                    }
                }
            }
        }
    }

    private suspend fun handleCommand(spokenText: String) {
        when (VoiceCommandInterpreter.interpret(spokenText)) {
            VoiceCommand.WhereAmI -> answerWhereAmI()
            VoiceCommand.Unknown -> {
                val message = "Je n'ai pas compris cette demande. Essayez : où suis-je ?"
                _state.value = VoiceAssistantState.Answered(message)
                speak(message)
            }
        }
    }

    private suspend fun answerWhereAmI() {
        _state.value = VoiceAssistantState.Thinking
        val position = locationRepository.currentPosition() ?: locationRepository.lastKnownPosition()

        if (position == null) {
            val message = "Je n'ai pas encore de position fiable. Réessayez dans un instant."
            _state.value = VoiceAssistantState.Answered(message)
            speak(message)
            return
        }

        val place = geographyEngine.resolve(position)
        val confidence = place?.let {
            confidenceEngine.evaluate(
                ConfidenceInputs(
                    accuracyMeters = position.accuracyMeters,
                    hasCityOrCommune = it.hasCityLevel,
                    hasNeighborhoodOrStreet = it.hasFineGrainLevel,
                    geocodingResultIsStale = geographyEngine.isCurrentResultStale()
                )
            )
        }

        val message = if (place != null && confidence != null) {
            messageBuilder.buildGeographyMessage(
                GeographyAnnouncementInput(place, confidence),
                AnnouncementMode.DETAILED
            ) ?: "Je ne suis pas assez sûr de votre position pour l'annoncer précisément."
        } else {
            "Je ne parviens pas à déterminer votre position pour le moment."
        }

        _state.value = VoiceAssistantState.Answered(message)
        speak(message)
    }

    private suspend fun speak(text: String) {
        val ready = voiceEngine.awaitReady()
        if (!ready) {
            _state.value = VoiceAssistantState.Error("Moteur vocal indisponible.")
            return
        }
        voiceEngine.speak(text).first()
    }

    override fun onCleared() {
        voiceEngine.shutdown()
        super.onCleared()
    }
}
