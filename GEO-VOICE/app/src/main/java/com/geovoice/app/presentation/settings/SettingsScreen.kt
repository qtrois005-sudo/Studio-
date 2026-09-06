package com.geovoice.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.geovoice.app.data.preferences.PreferencesRepository
import com.geovoice.app.domain.voice.AnnouncementMode
import kotlinx.coroutines.launch

/**
 * Écran des paramètres (section 55) : voix, batterie, notifications, historique.
 * Regroupement volontaire dans un seul écran pour le MVP — la structure en sections
 * séparées (Suivi/Voix/Batterie/Notifications/Historique/Confidentialité) reste à
 * répartir sur plusieurs écrans lors d'une itération suivante si besoin.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) { PreferencesRepository(context) }
    val scope = rememberCoroutineScope()

    val voiceIsMale by preferences.voiceIsMale.collectAsState(initial = true)
    val voiceSpeed by preferences.voiceSpeed.collectAsState(initial = 1.0f)
    val historyEnabled by preferences.historyEnabled.collectAsState(initial = true)
    val silentMode by preferences.silentMode.collectAsState(initial = false)

    Scaffold(topBar = { TopAppBar(title = { Text("Paramètres") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Voix", style = MaterialTheme.typography.titleLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (voiceIsMale) "Voix masculine" else "Voix féminine")
                        Switch(
                            checked = voiceIsMale,
                            onCheckedChange = { scope.launch { preferences.setVoiceIsMale(it) } }
                        )
                    }
                    Text("Vitesse : ${"%.1f".format(voiceSpeed)}x")
                    Slider(
                        value = voiceSpeed,
                        onValueChange = { scope.launch { preferences.setVoiceSpeed(it) } },
                        valueRange = 0.5f..2.0f
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mode silencieux", style = MaterialTheme.typography.titleLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aucune annonce vocale automatique (section 29)")
                        Switch(
                            checked = silentMode,
                            onCheckedChange = { scope.launch { preferences.setSilentMode(it) } }
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Historique", style = MaterialTheme.typography.titleLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enregistrer mes trajets (section 47)")
                        Switch(
                            checked = historyEnabled,
                            onCheckedChange = { scope.launch { preferences.setHistoryEnabled(it) } }
                        )
                    }
                }
            }
        }
    }
}
