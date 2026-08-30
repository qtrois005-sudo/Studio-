package com.sleepaudio.app.player

import com.sleepaudio.app.data.AudioFile
import com.sleepaudio.app.data.SleepTimerState

/**
 * État complet et centralisé de l'application. C'est la seule source de
 * vérité — l'UI ne fait que l'observer, jamais l'inverse.
 */
data class PlayerState(
    val audio: AudioFile? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 1f,
    val timer: SleepTimerState = SleepTimerState(),
    val errorMessage: String? = null
)
