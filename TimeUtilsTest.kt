package com.sleepaudio.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TimeUtilsTest {

    @Test
    fun formatDuration_underOneHour_showsMinutesSeconds() {
        assertEquals("05:09", TimeUtils.formatDuration(309_000))
    }

    @Test
    fun formatDuration_overOneHour_showsHoursMinutesSeconds() {
        assertEquals("01:00:05", TimeUtils.formatDuration(3_605_000))
    }

    @Test
    fun durationMillisFrom_combinesUnitsCorrectly() {
        val result = TimeUtils.durationMillisFrom(hours = 1, minutes = 30, seconds = 0)
        assertEquals(5_400_000L, result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun durationMillisFrom_rejectsNegativeValues() {
        TimeUtils.durationMillisFrom(hours = -1, minutes = 0, seconds = 0)
    }

    @Test
    fun endTimeMillisForClock_laterToday_sameDay() {
        val now = calendarAt(hour = 20, minute = 0)
        val end = TimeUtils.endTimeMillisForClock(hour = 23, minute = 0, nowMillis = now)
        assertTrue(end > now)
        assertEquals(3 * 60 * 60 * 1000L, end - now)
    }

    @Test
    fun endTimeMillisForClock_earlierThanNow_rollsOverToNextDay() {
        // Il est 23h30, l'utilisateur choisit 07h00 : doit être demain, jamais négatif.
        val now = calendarAt(hour = 23, minute = 30)
        val end = TimeUtils.endTimeMillisForClock(hour = 7, minute = 0, nowMillis = now)
        assertTrue("L'échéance doit toujours être dans le futur", end > now)
    }

    @Test
    fun endTimeMillisForDuration_neverNegative() {
        val now = System.currentTimeMillis()
        val end = TimeUtils.endTimeMillisForDuration(0, now)
        assertEquals(now, end)
    }

    private fun calendarAt(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
