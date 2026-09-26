package com.geovoice.app.presentation.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geovoice.app.R
import com.geovoice.app.core.permission.PermissionManager

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenMap: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    voiceAssistantViewModel: VoiceAssistantViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val assistantState by voiceAssistantViewModel.state.collectAsState()
    val context = LocalContext.current
    var micDenied by remember { mutableStateOf(false) }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceAssistantViewModel.startListening()
        } else {
            micDenied = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.app_name)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.isTracking) "Suivi actif" else "Suivi arrêté",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (state.hasLocationPermission)
                            "Localisation autorisée"
                        else
                            "Localisation non autorisée",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Assistant vocal", style = MaterialTheme.typography.titleLarge)
                    val statusText = when (val s = assistantState) {
                        VoiceAssistantState.Idle -> "Appuyez et demandez : « où suis-je ? »"
                        VoiceAssistantState.Listening -> "Je vous écoute..."
                        VoiceAssistantState.Thinking -> "Je vérifie votre position..."
                        is VoiceAssistantState.Answered -> s.text
                        is VoiceAssistantState.Error -> s.message
                    }
                    Text(statusText, style = MaterialTheme.typography.bodyMedium)
                    if (micDenied) {
                        Text(
                            "Le micro est nécessaire pour cette fonction.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = {
                            if (PermissionManager.hasAnyLocation(context)) {
                                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.hasLocationPermission
                    ) {
                        Text("🎤 Demander où je suis")
                    }
                }
            }

            if (state.canOfferResume && !state.isTracking) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Une session était active avant le dernier redémarrage.")
                        Button(onClick = { viewModel.startTracking() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Reprendre le suivi")
                        }
                    }
                }
            }

            if (state.isTracking) {
                Button(
                    onClick = { viewModel.stopTracking() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.home_stop_tracking))
                }
            } else {
                Button(
                    onClick = { viewModel.startTracking() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.hasLocationPermission
                ) {
                    Text(stringResource(id = R.string.home_start_tracking))
                }
            }

            OutlinedButton(onClick = onOpenMap, modifier = Modifier.fillMaxWidth()) {
                Text("Carte")
            }
            OutlinedButton(onClick = onOpenDiagnostics, modifier = Modifier.fillMaxWidth()) {
                Text("Diagnostics")
            }
            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Paramètres")
            }
        }
    }
}
