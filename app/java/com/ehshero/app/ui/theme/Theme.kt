package com.ehshero.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * EHS Hero is deliberately a single, fixed dark "command center" theme
 * regardless of the system light/dark setting - a game-HUD safety dashboard
 * doesn't make sense as a light theme, and a fixed identity is part of the
 * brand (spec section 2: "modern, energetic, professional, gamified...
 * slightly futuristic"). This is a one-line place to reintroduce a light
 * variant later if that ever changes.
 */
private val EHSColorScheme = darkColorScheme(
    primary = GuardianAmber,
    onPrimary = OnAmber,
    primaryContainer = GuardianAmberDim,
    onPrimaryContainer = TextHigh,
    secondary = SignalCyan,
    onSecondary = OnCyan,
    secondaryContainer = SignalCyanDim,
    onSecondaryContainer = TextHigh,
    tertiary = ClearanceGreen,
    onTertiary = OnGreen,
    error = HazardCoral,
    onError = OnCoral,
    errorContainer = HazardCoralDim,
    onErrorContainer = TextHigh,
    background = CommandNavy,
    onBackground = TextHigh,
    surface = SteelPanel,
    onSurface = TextHigh,
    surfaceVariant = SteelPanelElevated,
    onSurfaceVariant = TextMedium,
    outline = SteelOutline,
    outlineVariant = SteelOutline,
    scrim = Color(0xCC000000)
)

@Composable
fun EHSHeroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EHSColorScheme,
        typography = EHSTypography,
        shapes = EHSShapes,
        content = content
    )
}
