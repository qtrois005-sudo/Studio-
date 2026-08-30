package com.sleepaudio.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette calme, sombre par défaut : pensée pour une utilisation nocturne.
private val DeepNavy = Color(0xFF0E1420)
private val Surface1 = Color(0xFF161D2B)
private val AccentIndigo = Color(0xFF6C7BFF)
private val AccentSoftGold = Color(0xFFE3C27B)
private val TextPrimary = Color(0xFFEDEFF5)
private val TextSecondary = Color(0xFFA3ABC2)

private val DarkColors = darkColorScheme(
    primary = AccentIndigo,
    secondary = AccentSoftGold,
    background = DeepNavy,
    surface = Surface1,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

private val LightColors = lightColorScheme(
    primary = AccentIndigo,
    secondary = AccentSoftGold
)

@Composable
fun SleepAudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
