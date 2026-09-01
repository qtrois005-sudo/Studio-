package com.sleepaudio.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sleepaudio.app.R
import com.sleepaudio.app.player.PlayerState
import com.sleepaudio.app.util.TimeUtils

@Composable
fun PlayerScreen(state: PlayerState, onBack: () -> Unit, onTogglePlayPause: () -> Unit, onStop: () -> Unit, onOpenTimer: () -> Unit) {
    val progress = if (state.durationMs > 0) (state.positionMs.toFloat()/state.durationMs).coerceIn(0f,1f) else 0f
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment=Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick=onBack){Icon(Icons.Filled.ArrowBack,"Retour")}
            Text("En lecture", Modifier.weight(1f), style=MaterialTheme.typography.titleLarge, fontWeight=FontWeight.Bold, textAlign=TextAlign.Center)
            IconButton(onClick=onOpenTimer){Icon(Icons.Filled.Timer,"Minuterie")}
        }
        Spacer(Modifier.height(26.dp))
        Card(shape=RoundedCornerShape(36.dp), modifier=Modifier.fillMaxWidth()) {
            Image(painterResource(R.drawable.sleep_hero), "Ambiance sommeil", Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(36.dp)))
        }
        Spacer(Modifier.height(28.dp))
        Text(state.audio?.displayName ?: "Aucun audio sélectionné", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold, textAlign=TextAlign.Center, maxLines=2)
        Text(if(state.isPlaying) "Lecture en cours" else "Prêt à écouter", color=MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        LinearProgressIndicator(progress={progress}, modifier=Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(20.dp)))
        Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) { Text(TimeUtils.formatDuration(state.positionMs)); Text(TimeUtils.formatDuration(state.durationMs)) }
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(28.dp)) {
            FilledTonalIconButton(onClick=onStop, modifier=Modifier.size(56.dp)){Icon(Icons.Filled.Stop,"Arrêter")}
            FilledIconButton(onClick=onTogglePlayPause, modifier=Modifier.size(82.dp), shape=RoundedCornerShape(28.dp)){Icon(if(state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,null,Modifier.size(42.dp))}
            FilledTonalIconButton(onClick=onOpenTimer, modifier=Modifier.size(56.dp)){Icon(Icons.Filled.Timer,"Minuterie")}
        }
        if(state.timer.isActive) { Spacer(Modifier.height(20.dp)); AssistChip(onClick=onOpenTimer,label={Text("Arrêt dans ${TimeUtils.formatDuration(state.timer.remainingMillis(System.currentTimeMillis()))}")},leadingIcon={Icon(Icons.Filled.Timer,null)}) }
        state.errorMessage?.let { Spacer(Modifier.height(12.dp)); Text(it,color=MaterialTheme.colorScheme.error,textAlign=TextAlign.Center) }
    }
}
