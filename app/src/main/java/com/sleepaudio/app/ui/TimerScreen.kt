package com.sleepaudio.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sleepaudio.app.data.FadeOutOption
import com.sleepaudio.app.player.PlayerState

@Composable
fun TimerScreen(
    state: PlayerState,
    onBack: () -> Unit,
    onStartDuration: (Int, Int, Int, FadeOutOption) -> Unit,
    onStartEndTime: (Int, Int, FadeOutOption) -> Unit,
    onCancelTimer: () -> Unit
) {
    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("30") }
    var endHour by remember { mutableStateOf("23") }
    var endMinute by remember { mutableStateOf("30") }
    var fadeOut by remember { mutableStateOf(FadeOutOption.MEDIUM) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Lecteur") }
        Text("Minuterie de sommeil", style = MaterialTheme.typography.headlineSmall)

        SectionCard(title = "Arrêter dans...") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hours, onValueChange = { hours = it },
                    label = { Text("Heures") }, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = minutes, onValueChange = { minutes = it },
                    label = { Text("Minutes") }, modifier = Modifier.weight(1f)
                )
            }
            PrimaryButton(text = "Démarrer") {
                onStartDuration(
                    hours.toIntOrNull() ?: 0,
                    minutes.toIntOrNull() ?: 0,
                    0,
                    fadeOut
                )
            }
        }

        SectionCard(title = "Arrêter à une heure précise") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = endHour, onValueChange = { endHour = it },
                    label = { Text("Heure") }, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endMinute, onValueChange = { endMinute = it },
                    label = { Text("Minute") }, modifier = Modifier.weight(1f)
                )
            }
            PrimaryButton(text = "Confirmer") {
                onStartEndTime(
                    endHour.toIntOrNull() ?: 23,
                    endMinute.toIntOrNull() ?: 0,
                    fadeOut
                )
            }
        }

        SectionCard(title = "Fondu de fin") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FadeOutOption.entries.forEach { option ->
                    TextButton(onClick = { fadeOut = option }) {
                        Text(
                            text = when (option) {
                                FadeOutOption.NONE -> "Aucun"
                                FadeOutOption.SHORT -> "15s"
                                FadeOutOption.MEDIUM -> "30s"
                                FadeOutOption.LONG -> "60s"
                            }
                        )
                    }
                }
            }
        }

        if (state.timer.isActive) {
            TextButton(onClick = onCancelTimer) { Text("Annuler la minuterie") }
        }
    }
}
