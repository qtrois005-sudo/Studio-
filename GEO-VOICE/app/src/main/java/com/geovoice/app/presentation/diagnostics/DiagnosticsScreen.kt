package com.geovoice.app.presentation.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.geovoice.app.core.permission.PermissionManager
import android.speech.tts.TextToSpeech
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

private data class DiagnosticLine(val label: String, val value: String, val ok: Boolean)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen() {
    val context = LocalContext.current

    val lines = remember {
        val hasFine = PermissionManager.hasFineLocation(context)
        val hasCoarse = PermissionManager.hasCoarseLocation(context)
        val hasBackground = PermissionManager.hasBackgroundLocation(context)
        val hasNotif = PermissionManager.hasNotifications(context)

        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        listOf(
            DiagnosticLine("Localisation précise", if (hasFine) "Autorisée" else "Refusée", hasFine),
            DiagnosticLine("Localisation approximative", if (hasCoarse) "Autorisée" else "Refusée", hasCoarse),
            DiagnosticLine("Localisation en arrière-plan", if (hasBackground) "Autorisée" else "Refusée", hasBackground),
            DiagnosticLine("Notifications", if (hasNotif) "Autorisées" else "Refusées", hasNotif),
            DiagnosticLine("Internet", if (hasInternet) "Disponible" else "Indisponible", hasInternet),
            DiagnosticLine("Moteur vocal (TTS)", if (isTtsAvailable(context)) "Disponible" else "Indisponible", isTtsAvailable(context))
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Diagnostics") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            lines.forEach { line ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(line.label, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            line.value,
                            color = if (line.ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

private fun isTtsAvailable(context: android.content.Context): Boolean {
    // Vérification simple et synchrone : un moteur TTS par défaut est déclaré sur le système.
    val intent = android.content.Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
    return intent.resolveActivity(context.packageManager) != null
}
