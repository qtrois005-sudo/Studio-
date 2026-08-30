package com.sleepaudio.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sleepaudio.app.MainViewModel

enum class AppScreen { HOME, PLAYER, TIMER, SCHEDULE }

/**
 * Navigation volontairement simple (état local + when) : pour une V1 à
 * quatre écrans, une dépendance nav-compose supplémentaire n'apporte rien.
 */
@Composable
fun AppNavHost(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    when (currentScreen) {
        AppScreen.HOME -> HomeScreen(
            state = uiState,
            onPickAudio = { onNavigate(AppScreen.PLAYER) },
            onOpenTimer = { onNavigate(AppScreen.TIMER) },
            onOpenSchedule = { onNavigate(AppScreen.SCHEDULE) },
            onAudioSelected = { uri -> viewModel.onAudioSelected(uri) }
        )

        AppScreen.PLAYER -> PlayerScreen(
            state = uiState,
            onBack = { onNavigate(AppScreen.HOME) },
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onStop = { viewModel.stopPlayback() },
            onOpenTimer = { onNavigate(AppScreen.TIMER) }
        )

        AppScreen.TIMER -> TimerScreen(
            state = uiState,
            onBack = { onNavigate(AppScreen.PLAYER) },
            onStartDuration = { h, m, s, fade -> viewModel.startDurationTimer(h, m, s, fade) },
            onStartEndTime = { h, m, fade -> viewModel.startEndTimeTimer(h, m, fade) },
            onCancelTimer = { viewModel.cancelTimer() }
        )

        AppScreen.SCHEDULE -> ScheduleScreen(
            onBack = { onNavigate(AppScreen.HOME) }
        )
    }
}
