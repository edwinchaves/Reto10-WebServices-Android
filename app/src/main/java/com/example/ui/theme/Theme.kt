package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LotoDarkColorScheme =
  darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    primaryContainer = ElectricBlueContainer,
    onPrimaryContainer = ElectricBlueLight,
    secondary = DeepViolet,
    onSecondary = TextPrimary,
    secondaryContainer = VioletContainer,
    onSecondaryContainer = VioletLight,
    tertiary = TurquoiseAccent,
    onTertiary = DeepNavyBg,
    tertiaryContainer = TurquoiseContainer,
    onTertiaryContainer = TurquoiseBright,
    background = DeepNavyBg,
    onBackground = TextPrimary,
    surface = SurfaceNavy,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceNavyElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = DeepNavyBg,
    surfaceContainerLow = SurfaceNavy,
    surfaceContainer = SurfaceNavyElevated,
    surfaceContainerHigh = SurfaceNavyHighlight,
    outline = BorderSubtle,
    outlineVariant = ColorTokens.OutlineFaint,
    error = RoseError,
    onError = TextPrimary,
    errorContainer = RoseContainer,
    onErrorContainer = RoseError,
  )

private object ColorTokens {
  val OutlineFaint = androidx.compose.ui.graphics.Color(0xFF1A243B)
}

val AppShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun LotoAnalyticsTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = LotoDarkColorScheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content,
  )
}
