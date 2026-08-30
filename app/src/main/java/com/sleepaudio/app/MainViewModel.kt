package com.sleepaudio.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sleepaudio.app.data.AudioFile
import com.sleepaudio.app.data.FadeOutOption
import com.sleepaudio.app.data.PreferencesRepository
import com.sleepaudio.app.player.AudioController
import com.sleepaudio.app.player.PlayerState
import com.sleepaudio.app.timer.SleepTimerManager
import com.sleepaudio.app.util.AudioUtils
import com.sleepaudio.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Source unique de vérité pour l'UI Compose. Ne contient aucune logique de
 * lecture bas niveau : délègue à [AudioController] et [SleepTimerManager].
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val audioController = AudioController(application)
    private val preferences = PreferencesRepository(application)

    private val _uiState = MutableStateFlow(PlayerState())
    val uiState: StateFlow<PlayerState> = _uiState.asStateFlow()

    private val timerManager = SleepTimerManager(
        scope = viewModelScope,
        onFadeTick = { fraction -> audioController.setVolume(fraction) },
        onExpired = {
            audioController.setVolume(0f)
            audioController.stop()
            audioController.setVolume(1f) // réinitialise pour la prochaine lecture
        }
    )

    init {
        audioController.connect {
            viewModelScope.launch {
                audioController.playerState.collect { state ->
                    _uiState.value = state.copy(timer = timerManager.state.value)
                }
            }
        }
        viewModelScope.launch {
            timerManager.state.collect { timer ->
                _uiState.value = _uiState.value.copy(timer = timer)
            }
        }
    }

    fun onAudioSelected(uri: Uri) {
        val context = getApplication<Application>()
        AudioUtils.tryPersistPermission(context, uri)
        val audioFile: AudioFile = AudioUtils.resolveAudioFile(context, uri)
        audioController.play(audioFile.uri, audioFile.displayName)
        viewModelScope.launch {
            preferences.saveLastAudio(audioFile.uri, audioFile.displayName)
        }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) audioController.pause() else audioController.resume()
    }

    fun stopPlayback() {
        timerManager.cancel()
        audioController.stop()
    }

    fun startDurationTimer(hours: Int, minutes: Int, seconds: Int, fadeOut: FadeOutOption) {
        val durationMillis = TimeUtils.durationMillisFrom(hours, minutes, seconds)
        timerManager.startWithDuration(durationMillis, fadeOut)
        viewModelScope.launch { preferences.saveLastDuration(durationMillis) }
    }

    fun startEndTimeTimer(hour: Int, minute: Int, fadeOut: FadeOutOption) {
        val end = TimeUtils.endTimeMillisForClock(hour, minute, System.currentTimeMillis())
        timerManager.startAtEndTime(end, fadeOut)
    }

    fun cancelTimer() = timerManager.cancel()

    override fun onCleared() {
        audioController.release()
        super.onCleared()
    }
}
