package com.sleepaudio.app.util

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Fonctions temporelles pures (testables sans Android) utilisées par la minuterie.
 * Toute la logique repose sur des millisecondes epoch, jamais sur un simple
 * compteur visuel, afin de rester correcte après changement d'écran ou reprise.
 */
object TimeUtils {

    /** Formatte une durée en millisecondes en "HH:MM:SS" ou "MM:SS". */
    fun formatDuration(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0L) / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /** Convertit heures/minutes/secondes en durée totale en millisecondes. Rejette les valeurs négatives. */
    fun durationMillisFrom(hours: Int, minutes: Int, seconds: Int): Long {
        require(hours >= 0 && minutes >= 0 && seconds >= 0) {
            "Les valeurs de durée doivent être positives ou nulles"
        }
        return TimeUnit.HOURS.toMillis(hours.toLong()) +
            TimeUnit.MINUTES.toMillis(minutes.toLong()) +
            TimeUnit.SECONDS.toMillis(seconds.toLong())
    }

    /**
     * Calcule l'échéance absolue (epoch millis) correspondant à une heure d'arrêt
     * "heure:minute" choisie par l'utilisateur, à partir de [nowMillis].
     * Si l'heure choisie est déjà passée aujourd'hui, elle est reportée au
     * lendemain — gère donc correctement le passage de minuit et ne produit
     * jamais de durée négative.
     */
    fun endTimeMillisForClock(hour: Int, minute: Int, nowMillis: Long): Long {
        require(hour in 0..23 && minute in 0..59) { "Heure ou minute invalide" }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    /** Échéance simple à partir d'une durée relative. */
    fun endTimeMillisForDuration(durationMillis: Long, nowMillis: Long): Long {
        require(durationMillis >= 0) { "La durée ne peut pas être négative" }
        return nowMillis + durationMillis
    }
}
