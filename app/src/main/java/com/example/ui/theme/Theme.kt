package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NexusColorScheme = darkColorScheme(
    primary = NexusCyan,
    onPrimary = NexusVoidBlack,
    primaryContainer = NexusSurfaceElevated,
    onPrimaryContainer = NexusTextCyan,
    secondary = NexusCyanGlow,
    onSecondary = NexusVoidBlack,
    secondaryContainer = NexusSurfaceDark,
    onSecondaryContainer = NexusCyanGlow,
    tertiary = NexusIndigoQuantum,
    background = NexusVoidBlack,
    onBackground = NexusTextPrimary,
    surface = NexusDeepMidnight,
    onSurface = NexusTextPrimary,
    surfaceVariant = NexusSurfaceDark,
    onSurfaceVariant = NexusTextSecondary,
    outline = NexusBorderCyan,
    outlineVariant = NexusBorderBright,
    error = NexusCrimsonAlert,
    onError = NexusVoidBlack
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexusColorScheme,
        typography = Typography,
        content = content
    )
}
