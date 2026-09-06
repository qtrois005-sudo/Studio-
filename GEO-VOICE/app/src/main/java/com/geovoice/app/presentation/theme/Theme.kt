package com.geovoice.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = GeoVoicePrimary,
    onPrimary = Color.White,
    secondary = GeoVoiceAccent,
    background = GeoVoiceBackgroundLight,
    surface = GeoVoiceSurfaceLight,
    onSurface = GeoVoiceOnSurfaceLight,
    error = GeoVoiceError
)

private val DarkColors = darkColorScheme(
    primary = GeoVoiceAccent,
    onPrimary = Color.Black,
    secondary = GeoVoicePrimary,
    background = GeoVoiceBackgroundDark,
    surface = GeoVoiceSurfaceDark,
    onSurface = GeoVoiceOnSurfaceDark,
    error = GeoVoiceError
)

@Composable
fun GeoVoiceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = GeoVoiceTypography,
        content = content
    )
}
