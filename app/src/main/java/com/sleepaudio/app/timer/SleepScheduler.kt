package com.sleepaudio.app.timer

import com.sleepaudio.app.data.ScheduleWindow
import com.sleepaudio.app.util.TimeUtils

/**
 * Construit une fenêtre de programmation (heure de début + heure de fin),
 * en gérant correctement le passage de minuit. Prévu pour une évolution
 * future (programmations récurrentes) sans réécrire l'architecture.
 */
object SleepScheduler {

    fun buildWindow(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        nowMillis: Long
    ): ScheduleWindow {
        val start = TimeUtils.endTimeMillisForClock(startHour, startMinute, nowMillis)
        var end = TimeUtils.endTimeMillisForClock(endHour, endMinute, nowMillis)

        // Si l'heure de fin calculée tombe avant l'heure de début (ex : début 23:00,
        // fin 07:00), elle a déjà été reportée au jour suivant par endTimeMillisForClock,
        // mais il faut s'assurer qu'elle reste après le début choisi.
        if (end <= start) {
            end += DAY_MILLIS
        }

        return ScheduleWindow(start, end)
    }

    private const val DAY_MILLIS = 24L * 60 * 60 * 1000
}
