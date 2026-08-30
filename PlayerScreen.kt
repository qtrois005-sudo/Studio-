package com.sleepaudio.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sleepaudio.app.player.PlayerState
import com.sleepaudio.app.util.TimeUtils

@Composable
fun PlayerScreen(
    state: PlayerState,
    onBack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onOpenTimer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Accueil") }

        Text(
            text = state.audio?.displayName ?: "Aucun audio",
            style = MaterialTheme.typography.headlineSmall
        )

        val progress = if (state.durationMs > 0) {
            (state.positionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())

        Text(
            "${TimeUtils.formatDuration(state.positionMs)} / ${TimeUtils.formatDuration(state.durationMs)}"
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        PlaybackControls(
            isPlaying = state.isPlaying,
            onTogglePlayPause = onTogglePlayPause,
            onStop = onStop
        )

        if (state.timer.isActive) {
            Text(
                "Minuterie : arrêt dans " +
                    TimeUtils.formatDuration(state.timer.remainingMillis(System.currentTimeMillis()))
            )
        }

        PrimaryButton(text = "Régler la minuterie") { onOpenTimer() }
    }
}
