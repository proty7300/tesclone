package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CrimsonColors = darkColorScheme(
    primary = SlatePrimary,
    secondary = SlateSecondary,
    tertiary = SlateTertiary,
    background = SlateBackground,
    surface = SlateSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = SlateSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CrimsonColors,
        typography = Typography,
        content = content
    )
}
