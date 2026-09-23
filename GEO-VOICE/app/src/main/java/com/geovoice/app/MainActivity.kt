
package com.geovoice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geovoice.app.core.permission.PermissionManager
import com.geovoice.app.data.preferences.PreferencesRepository
import com.geovoice.app.presentation.diagnostics.DiagnosticsScreen
import com.geovoice.app.presentation.home.HomeScreen
import com.geovoice.app.presentation.map.MapScreen
import com.geovoice.app.presentation.navigation.GeoVoiceDestinations
import com.geovoice.app.presentation.onboarding.OnboardingScreen
import com.geovoice.app.presentation.permissions.PermissionsScreen
import com.geovoice.app.presentation.settings.SettingsScreen
import com.geovoice.app.presentation.splash.SplashScreen
import com.geovoice.app.presentation.theme.GeoVoiceTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            GeoVoiceTheme {
                GeoVoiceNavHost()
            }
        }
    }
}

@Composable
private fun GeoVoiceNavHost() {
    val context = LocalContext.current
    val preferences = remember { PreferencesRepository(context) }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        preferences.onboardingDone.collect { done ->
            startDestination = when {
                !done -> GeoVoiceDestinations.ONBOARDING
                !PermissionManager.hasAnyLocation(context) -> GeoVoiceDestinations.PERMISSIONS
                else -> GeoVoiceDestinations.HOME
            }
        }
    }

    val resolvedStart = startDestination
    if (resolvedStart == null) {
        SplashScreen(onFinished = {})
        return
    }

    NavHost(navController = navController, startDestination = resolvedStart) {
        composable(GeoVoiceDestinations.ONBOARDING) {
            OnboardingScreen(onFinished = {
                scope.launch {
                    preferences.setOnboardingDone(true)
                    navController.navigate(
                        if (PermissionManager.hasAnyLocation(context)) GeoVoiceDestinations.HOME
                        else GeoVoiceDestinations.PERMISSIONS
                    ) {
                        popUpTo(GeoVoiceDestinations.ONBOARDING) { inclusive = true }
                    }
                }
            })
        }
        composable(GeoVoiceDestinations.PERMISSIONS) {
            PermissionsScreen(onGranted = {
                navController.navigate(GeoVoiceDestinations.HOME) {
                    popUpTo(GeoVoiceDestinations.PERMISSIONS) { inclusive = true }
                }
            })
        }
        composable(GeoVoiceDestinations.HOME) {
            HomeScreen(
                onOpenSettings = { navController.navigate(GeoVoiceDestinations.SETTINGS) },
                onOpenDiagnostics = { navController.navigate(GeoVoiceDestinations.DIAGNOSTICS) },
                onOpenMap = { navController.navigate(GeoVoiceDestinations.MAP) }
            )
        }
        composable(GeoVoiceDestinations.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(GeoVoiceDestinations.DIAGNOSTICS) {
            DiagnosticsScreen()
        }
        composable(GeoVoiceDestinations.MAP) {
            MapScreen()
        }
    }
}
