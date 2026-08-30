package com.sleepaudio.app.timer

import com.sleepaudio.app.data.FadeOutOption
import com.sleepaudio.app.data.SleepTimerState
import com.sleepaudio.app.data.TimerMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Gère la minuterie de sommeil à partir d'une échéance absolue en
 * millisecondes (endTimeMillis), jamais d'un simple compteur visuel.
 * Si l'utilisateur quitte l'écran et revient, l'état correct est retrouvé
 * en recalculant le temps restant à partir de l'échéance stockée.
 *
 * [now] est injectable pour permettre des tests unitaires déterministes.
 */
class SleepTimerManager(
    private val scope: CoroutineScope,
    private val now: () -> Long = { System.currentTimeMillis() },
    private val onFadeTick: (volumeFraction: Float) -> Unit = {},
    private val onExpired: () -> Unit
) {
    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    private var tickJob: Job? = null

    fun startWithDuration(durationMillis: Long, fadeOut: FadeOutOption) {
        val end = now() + durationMillis
        start(TimerMode.DURATION, end, fadeOut)
    }

    fun startAtEndTime(endTimeMillis: Long, fadeOut: FadeOutOption) {
        start(TimerMode.END_TIME, endTimeMillis, fadeOut)
    }

    private fun start(mode: TimerMode, endTimeMillis: Long, fadeOut: FadeOutOption) {
        cancel()
        _state.value = SleepTimerState(
            mode = mode,
            endTimeMillis = endTimeMillis,
            fadeOut = fadeOut
        )
        tickJob = scope.launch {
            while (isActive) {
                val remaining = _state.value.remainingMillis(now())

                if (remaining <= 0L) {
                    _state.value = _state.value.copy(isFading = false)
                    onExpired()
                    _state.value = SleepTimerState()
                    break
                }

                val fadeMs = fadeOut.seconds * 1000L
                if (fadeMs > 0 && remaining <= fadeMs) {
                    val fraction = (remaining.toFloat() / fadeMs).coerceIn(0f, 1f)
                    _state.value = _state.value.copy(isFading = true)
                    onFadeTick(fraction)
                }

                delay(TICK_INTERVAL_MS)
            }
        }
    }

    /** Annule la minuterie en cours. Idempotent. */
    fun cancel() {
        tickJob?.cancel()
        tickJob = null
        _state.value = SleepTimerState()
    }

    /** Recalcule l'état affichable au retour sur l'écran, à partir de l'échéance stockée. */
    fun remainingMillisNow(): Long = _state.value.remainingMillis(now())

    companion object {
        private const val TICK_INTERVAL_MS = 500L
    }
}
