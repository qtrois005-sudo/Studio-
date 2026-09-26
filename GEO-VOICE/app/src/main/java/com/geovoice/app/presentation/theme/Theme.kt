package com.geovoice.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = GeoVoicePrimary,
    onPrimary = Color.White,
    secondary = GeoVoiceSecondary,
    onSecondary = Color.White,
    tertiary = GeoVoiceTertiary,
    background = GeoVoiceBackgroundLight,
    onBackground = GeoVoiceOnSurfaceLight,
    surface = GeoVoiceSurfaceLight,
    onSurface = GeoVoiceOnSurfaceLight,
    surfaceVariant = GeoVoiceSurfaceVariantLight,
    onSurfaceVariant = GeoVoiceOnSurfaceVariantLight,
    outline = GeoVoiceOutlineLight,
    error = GeoVoiceError,
    onError = GeoVoiceOnError
)

private val DarkColors = darkColorScheme(
    primary = GeoVoicePrimary,
    onPrimary = Color(0xFF00332C),
    secondary = GeoVoiceSecondary,
    onSecondary = Color.White,
    tertiary = GeoVoiceTertiary,
    background = GeoVoiceBackgroundDark,
    onBackground = GeoVoiceOnSurfaceDark,
    surface = GeoVoiceSurfaceDark,
    onSurface = GeoVoiceOnSurfaceDark,
    surfaceVariant = GeoVoiceSurfaceVariantDark,
    onSurfaceVariant = GeoVoiceOnSurfaceVariantDark,
    outline = GeoVoiceOutlineDark,
    error = GeoVoiceError,
    onError = GeoVoiceOnError
)

private val GeoVoiceShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
        shapes = GeoVoiceShapes,
        content = content
    )
}
