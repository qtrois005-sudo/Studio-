package com.sleepaudio.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import com.sleepaudio.app.MainViewModel

enum class AppScreen { HOME, PLAYER, TIMER, SCHEDULE, FAVORITES, SETTINGS }

@Composable
fun AppNavHost(currentScreen: AppScreen, onNavigate: (AppScreen) -> Unit, viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showBottom by remember { mutableStateOf(true) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottom) NavigationBar {
                NavItem(AppScreen.HOME, currentScreen, Icons.Filled.Home, "Accueil", onNavigate)
                NavItem(AppScreen.FAVORITES, currentScreen, Icons.Filled.Favorite, "Favoris", onNavigate)
                NavItem(AppScreen.TIMER, currentScreen, Icons.Filled.Timer, "Timer", onNavigate)
                NavItem(AppScreen.SETTINGS, currentScreen, Icons.Filled.Settings, "Réglages", onNavigate)
            }
        }
    ) { padding ->
        showBottom = currentScreen != AppScreen.PLAYER
        Surface(Modifier.fillMaxSize()) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(state, { onNavigate(AppScreen.PLAYER) }, { onNavigate(AppScreen.TIMER) }, { onNavigate(AppScreen.SCHEDULE) }, { viewModel.onAudioSelected(it) }, { onNavigate(AppScreen.FAVORITES) }, { onNavigate(AppScreen.SETTINGS) })
                AppScreen.PLAYER -> PlayerScreen(state, { onNavigate(AppScreen.HOME) }, { viewModel.togglePlayPause() }, { viewModel.stopPlayback() }, { onNavigate(AppScreen.TIMER) })
                AppScreen.TIMER -> TimerScreen(state, { onNavigate(AppScreen.HOME) }, { h,m,s,f -> viewModel.startDurationTimer(h,m,s,f) }, { h,m,f -> viewModel.startEndTimeTimer(h,m,f) }, { viewModel.cancelTimer() })
                AppScreen.SCHEDULE -> ScheduleScreen { onNavigate(AppScreen.HOME) }
                AppScreen.FAVORITES -> FavoritesScreen(onBack = { onNavigate(AppScreen.HOME) }, onPick = { viewModel.onAudioSelected(it); onNavigate(AppScreen.PLAYER) })
                AppScreen.SETTINGS -> SettingsScreen(onBack = { onNavigate(AppScreen.HOME) })
            }
        }
    }
}

@Composable private fun RowScope.NavItem(screen: AppScreen, current: AppScreen, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, go: (AppScreen)->Unit) {
    NavigationBarItem(selected = current == screen, onClick = { go(screen) }, icon = { Icon(icon, label) }, label = { Text(label) })
}
