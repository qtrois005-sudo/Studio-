package com.sleepaudio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Indigo = Color(0xFF6C63FF)
private val Violet = Color(0xFF8B7CFF)
private val Night = Color(0xFF0B1020)
private val NightSurface = Color(0xFF151B2E)
private val LightBackground = Color(0xFFF7F7FC)

private val DarkColors = darkColorScheme(primary = Violet, secondary = Color(0xFFB9A8FF), background = Night, surface = NightSurface, onPrimary = Color.White)
private val LightColors = lightColorScheme(primary = Indigo, secondary = Color(0xFF6255D9), background = LightBackground, surface = Color.White)

@Composable
fun SleepAudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
