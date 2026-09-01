package com.sleepaudio.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun FavoritesScreen(onBack: () -> Unit, onPick: (Uri) -> Unit) {
 val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(onPick) }
 Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
  Text("Vos favoris", style = MaterialTheme.typography.headlineMedium)
  Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
   Icon(Icons.Filled.Favorite, null, modifier=Modifier.size(52.dp), tint=MaterialTheme.colorScheme.primary)
   Text("Votre bibliothèque personnelle", style=MaterialTheme.typography.titleLarge)
   Text("Ajoutez vos sons préférés depuis votre téléphone. SleepAudio garde une expérience simple et privée.", style=MaterialTheme.typography.bodyMedium)
   Button(onClick={ picker.launch(arrayOf("audio/*")) }) { Icon(Icons.Filled.Add,null); Spacer(Modifier.width(8.dp)); Text("Ajouter un audio") }
  } }
  TextButton(onClick=onBack) { Text("Retour à l'accueil") }
 }
}
