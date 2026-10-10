package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AetherColorScheme = darkColorScheme(
    primary = AetherCyan,
    onPrimary = AetherObsidian,
    primaryContainer = AetherSurfaceVariant,
    onPrimaryContainer = AetherCyan,
    secondary = AetherViolet,
    onSecondary = Color.White,
    secondaryContainer = AetherPurpleDeep,
    onSecondaryContainer = Color.White,
    background = AetherObsidian,
    onBackground = AetherTextPrimary,
    surface = AetherSurface,
    onSurface = AetherTextPrimary,
    surfaceVariant = AetherSurfaceVariant,
    onSurfaceVariant = AetherTextSecondary,
    error = AetherAlertRed,
    onError = Color.White
)

@Composable
fun AetherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AetherColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AetherTheme(content = content)
}
