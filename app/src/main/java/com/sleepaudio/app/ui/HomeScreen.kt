package com.sleepaudio.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sleepaudio.app.player.PlayerState
import com.sleepaudio.app.util.TimeUtils

@Composable
fun HomeScreen(
    state: PlayerState,
    onPickAudio: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenSchedule: () -> Unit,
    onAudioSelected: (Uri) -> Unit
) {
    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onAudioSelected(uri)
            onPickAudio()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Sleep Audio", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Écoute, verrouille ton téléphone, laisse l'arrêt se faire tout seul.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionCard(title = "Audio actuel") {
            Text(state.audio?.displayName ?: "Aucun audio sélectionné")
            if (state.timer.isActive) {
                Text(
                    "Arrêt dans " + TimeUtils.formatDuration(
                        state.timer.remainingMillis(System.currentTimeMillis())
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        PrimaryButton(text = "Choisir un fichier audio") {
            pickAudioLauncher.launch(arrayOf("audio/*"))
        }

        PrimaryButton(text = "Ouvrir la minuterie") { onOpenTimer() }
        PrimaryButton(text = "Programmation (bientôt)") { onOpenSchedule() }
    }
}
