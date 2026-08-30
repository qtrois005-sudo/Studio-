package com.sleepaudio.app.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enveloppe autour de [MediaController] : c'est le seul point d'accès au
 * lecteur depuis l'UI. L'UI n'a jamais de référence directe à ExoPlayer.
 */
class AudioController(private val context: Context) {

    private var controller: MediaController? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _playerState.value = _playerState.value.copy(
                isBuffering = playbackState == Player.STATE_BUFFERING,
                durationMs = controller?.duration?.coerceAtLeast(0L)
                    ?: _playerState.value.durationMs
            )
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _playerState.value = _playerState.value.copy(
                errorMessage = "Lecture impossible. Vérifie le fichier sélectionné."
            )
        }
    }

    fun connect(onConnected: () -> Unit = {}) {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            controller = future.get().also { it.addListener(listener) }
            onConnected()
        }, MoreExecutors.directExecutor())
    }

    fun play(uri: String, displayName: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(uri)
            .build()
        controller?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        _playerState.value = _playerState.value.copy(
            audio = com.sleepaudio.app.data.AudioFile(uri, displayName),
            errorMessage = null
        )
    }

    fun pause() = controller?.pause()
    fun resume() = controller?.play()

    fun stop() {
        controller?.stop()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun setVolume(volume: Float) {
        controller?.volume = volume.coerceIn(0f, 1f)
        _playerState.value = _playerState.value.copy(volume = volume)
    }

    fun currentPositionMs(): Long = controller?.currentPosition ?: 0L

    fun release() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }
}
