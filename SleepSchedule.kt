package com.sleepaudio.app.data

/** Mode de minuterie choisi par l'utilisateur. */
enum class TimerMode {
    NONE,
    DURATION,   // ex : "arrête dans 45 minutes"
    END_TIME    // ex : "arrête à 23:30"
}

/** Durée du fondu de fin avant l'arrêt automatique. */
enum class FadeOutOption(val seconds: Int) {
    NONE(0),
    SHORT(15),
    MEDIUM(30),
    LONG(60);

}

/**
 * État de la minuterie de sommeil, indépendant de l'UI.
 * [endTimeMillis] est la seule source de vérité temporelle : jamais un
 * simple compteur décrémenté dans l'Activity.
 */
data class SleepTimerState(
    val mode: TimerMode = TimerMode.NONE,
    val endTimeMillis: Long? = null,
    val fadeOut: FadeOutOption = FadeOutOption.NONE,
    val isFading: Boolean = false
) {
    fun remainingMillis(nowMillis: Long): Long {
        val end = endTimeMillis ?: return 0L
        return (end - nowMillis).coerceAtLeast(0L)
    }

    val isActive: Boolean get() = mode != TimerMode.NONE && endTimeMillis != null
}

/** Programmation future (heure de début + heure de fin), extensible V2. */
data class ScheduleWindow(
    val startTimeMillis: Long,
    val endTimeMillis: Long
) {
    init {
        require(endTimeMillis > startTimeMillis) {
            "L'heure de fin doit être postérieure à l'heure de début"
        }
    }
}
