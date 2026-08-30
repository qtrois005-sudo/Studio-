package com.sleepaudio.app.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sleepaudio.app.MainActivity

/**
 * Service de premier plan qui garde la lecture active lorsque l'application
 * passe en arrière-plan ou que l'écran est verrouillé. Toute la logique de
 * lecture réside ici, jamais dans une Activity.
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    lateinit var player: ExoPlayer
        private set

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val sessionActivityIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    /**
     * Arrête proprement la lecture et le service lorsqu'il n'est plus nécessaire.
     * Idempotent : peut être appelé plusieurs fois sans effet secondaire.
     */
    fun stopPlaybackAndService() {
        if (player.isPlaying) {
            player.pause()
        }
        player.stop()
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Si rien n'est en lecture, on peut libérer le service ; sinon Media3
        // gère la persistance tant que la session est active.
        if (!player.isPlaying) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
