package com.sleepaudio.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * La programmation avec heure de début + heure de fin (voir SleepScheduler)
 * est architecturée côté logique mais volontairement absente de l'UI V1,
 * pour ne pas surcharger la première version.
 */
@Composable
fun ScheduleScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Accueil") }
        Text("Programmation", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Bientôt disponible : programme une heure de début et de fin récurrente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
