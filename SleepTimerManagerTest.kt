package com.sleepaudio.app.timer

import com.sleepaudio.app.data.FadeOutOption
import com.sleepaudio.app.data.TimerMode
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepTimerManagerTest {

    @Test
    fun startWithDuration_setsActiveStateImmediately() = runTest {
        var expiredCalled = false
        var virtualNow = 0L
        val manager = SleepTimerManager(
            scope = this,
            now = { virtualNow },
            onExpired = { expiredCalled = true }
        )

        manager.startWithDuration(60_000, FadeOutOption.NONE)

        assertTrue(manager.state.value.isActive)
        assertEquals(TimerMode.DURATION, manager.state.value.mode)
        assertFalse(expiredCalled)
    }

    @Test
    fun timerExpires_callsOnExpiredAndResetsState() = runTest {
        var expiredCalled = false
        var virtualNow = 0L
        val manager = SleepTimerManager(
            scope = this,
            now = { virtualNow },
            onExpired = { expiredCalled = true }
        )

        manager.startWithDuration(1_000, FadeOutOption.NONE)
        virtualNow = 1_500
        advanceTimeBy(1_500)

        assertTrue(expiredCalled)
        assertFalse(manager.state.value.isActive)
    }

    @Test
    fun cancel_isIdempotentAndResetsState() = runTest {
        val manager = SleepTimerManager(
            scope = this,
            now = { 0L },
            onExpired = {}
        )
        manager.startWithDuration(60_000, FadeOutOption.NONE)
        manager.cancel()
        manager.cancel() // deuxième appel : ne doit rien casser

        assertFalse(manager.state.value.isActive)
    }
}
