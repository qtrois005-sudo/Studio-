package com.sleepaudio.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.sleepaudio.app.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sleepaudio.app.player.PlayerState
import com.sleepaudio.app.util.TimeUtils

@Composable
fun HomeScreen(state: PlayerState, onPickAudio: () -> Unit, onOpenTimer: () -> Unit, onOpenSchedule: () -> Unit, onAudioSelected: (Uri) -> Unit, onOpenFavorites: () -> Unit, onOpenSettings: () -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { onAudioSelected(it); onPickAudio() }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Votre espace sommeil", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Une ambiance calme, simple et sans distraction.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha=.18f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Nightlight, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Box(Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha=.30f), MaterialTheme.colorScheme.surface))).padding(22.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(painterResource(R.drawable.ic_sleepaudio), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                    Text(state.audio?.displayName ?: "Aucun audio sélectionné", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 2)
                    Text(if (state.audio == null) "Choisissez un fichier audio pour commencer." else if (state.isPlaying) "Lecture en cours" else "Prêt à écouter", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state.timer.isActive) AssistChip(onClick = onOpenTimer, label = { Text("Arrêt dans ${TimeUtils.formatDuration(state.timer.remainingMillis(System.currentTimeMillis()))}") }, leadingIcon = { Icon(Icons.Filled.Timer, null) })
                    Button(onClick = { if (state.audio == null) picker.launch(arrayOf("audio/*")) else onPickAudio() }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text(if (state.audio == null) "Choisir un audio" else "Ouvrir le lecteur") }
                }
            }
        }
        Text("Commencer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            QuickAction(Modifier.weight(1f), Icons.Filled.LibraryMusic, "Audio") { picker.launch(arrayOf("audio/*")) }
            QuickAction(Modifier.weight(1f), Icons.Filled.Timer, "Minuterie", onOpenTimer)
            QuickAction(Modifier.weight(1f), Icons.Filled.Schedule, "Programme", onOpenSchedule)
        QuickAction(Modifier.weight(1f), Icons.Filled.Favorite, "Favoris", onOpenFavorites)
        }
        Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Une expérience pensée pour votre sommeil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Image(painter = painterResource(R.drawable.sleep_hero), contentDescription = "Illustration sommeil", modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(20.dp))); Text("Votre audio peut continuer pendant que vous vous détendez. Utilisez la minuterie pour programmer un arrêt automatique.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
}
@Composable private fun QuickAction(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Card(modifier.clickable(onClick=onClick), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(vertical=18.dp).fillMaxWidth(), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.spacedBy(8.dp)) { Icon(icon,null,tint=MaterialTheme.colorScheme.primary); Text(label, style=MaterialTheme.typography.labelLarge) } }
}
