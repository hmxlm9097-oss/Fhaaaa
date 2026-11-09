package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val H4xmlDarkColorScheme = darkColorScheme(
    primary = CobaltPrimary,
    onPrimary = TextPrimary,
    primaryContainer = CobaltContainer,
    onPrimaryContainer = NeonCyan,
    secondary = NeonCyan,
    onSecondary = ObsidianBlack,
    secondaryContainer = ElevatedGlass,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldOnline,
    onTertiary = ObsidianBlack,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = EmeraldOnline,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = DeepNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateGlass,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassBorder.copy(alpha = 0.45f),
    error = CrimsonAlert,
    onError = TextPrimary,
    errorContainer = CrimsonContainer,
    onErrorContainer = CrimsonAlert
)

val H4xmlShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun H4xmlCloudTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = H4xmlDarkColorScheme,
        typography = H4xmlTypography,
        shapes = H4xmlShapes,
        content = content
    )
}
