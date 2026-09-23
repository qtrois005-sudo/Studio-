package com.geovoice.app.presentation.map

import android.Manifest
import android.content.Context
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.geovoice.app.core.permission.PermissionManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Écran carte (section 34), basé sur OpenStreetMap via osmdroid : gratuit,
 * sans clé API, sans compte. Affiche la position actuelle de l'utilisateur.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(title = { Text("Carte") }) }) { padding ->
        if (!PermissionManager.hasAnyLocation(context)) {
            Text("Localisation non autorisée — activez-la depuis l'écran d'accueil.")
            return@Scaffold
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Configuration.getInstance().load(
                    ctx,
                    PreferenceManager.getDefaultSharedPreferences(ctx)
                )
                Configuration.getInstance().userAgentValue = ctx.packageName

                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)

                    val locationOverlay = MyLocationNewOverlay(
                        GpsMyLocationProvider(ctx), this
                    )
                    locationOverlay.enableMyLocation()
                    locationOverlay.runOnFirstFix {
                        post {
                            controller.animateTo(locationOverlay.myLocation)
                        }
                    }
                    overlays.add(locationOverlay)
                }
            }
        )
    }
}
