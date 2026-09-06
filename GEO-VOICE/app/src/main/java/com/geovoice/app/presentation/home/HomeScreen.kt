package com.geovoice.app.presentation.home

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geovoice.app.R

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

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

            OutlinedButton(onClick = onOpenDiagnostics, modifier = Modifier.fillMaxWidth()) {
                Text("Diagnostics")
            }
            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Paramètres")
            }
        }
    }
}
