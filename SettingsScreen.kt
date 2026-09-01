package com.sleepaudio.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun SettingsScreen(onBack: () -> Unit) {
 var notifications by remember { mutableStateOf(true) }
 var gentleFade by remember { mutableStateOf(true) }
 var autoplay by remember { mutableStateOf(false) }
 Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
  Text("Réglages", style=MaterialTheme.typography.headlineMedium)
  Text("Personnalisez votre expérience de sommeil.", color=MaterialTheme.colorScheme.onSurfaceVariant)
  SettingsSwitch("Rappels de sommeil", "Recevoir des rappels lorsque vous programmez une routine.", notifications) { notifications=it }
  SettingsSwitch("Fondu progressif", "Réduire doucement le volume avant l'arrêt automatique.", gentleFade) { gentleFade=it }
  SettingsSwitch("Lecture automatique", "Reprendre le dernier audio sélectionné à l'ouverture.", autoplay) { autoplay=it }
  Card { Column(Modifier.padding(18.dp)) { Text("À propos", style=MaterialTheme.typography.titleMedium); Spacer(Modifier.height(6.dp)); Text("SleepAudio • Version Premium 1.0.0\nUne expérience audio calme, moderne et centrée sur le sommeil.") } }
  TextButton(onClick=onBack) { Text("Retour") }
 }
}
@Composable private fun SettingsSwitch(title:String, subtitle:String, checked:Boolean, change:(Boolean)->Unit) { Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, style=MaterialTheme.typography.titleMedium); Text(subtitle, style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked, change) } } }
