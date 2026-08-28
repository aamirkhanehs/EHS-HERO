package com.ehshero.app.ui.theme

import androidx.compose.ui.graphics.Color

// "Command HUD" palette: a transmission-line/construction safety command
// center at night - steel and hazard-amber, not a generic AI-default
// cream+serif or near-black+neon palette. Every token is named for what it
// represents in that world, not just its hex value.

val CommandNavy = Color(0xFF0B1220)          // app background
val SteelPanel = Color(0xFF141C2E)           // card surface
val SteelPanelElevated = Color(0xFF1B2740)   // raised card / sheet surface
val SteelOutline = Color(0xFF2A3548)         // dividers, card borders

val GuardianAmber = Color(0xFFFFB020)        // primary accent: XP, energy, CTAs
val GuardianAmberDim = Color(0xFF8A5A12)
val SignalCyan = Color(0xFF2FD4D9)           // secondary accent: level/rank, electrical motif
val SignalCyanDim = Color(0xFF15676B)
val ClearanceGreen = Color(0xFF35C97A)       // approved / success
val ClearanceGreenDim = Color(0xFF184A2C)
val HazardCoral = Color(0xFFF0553F)          // rejected / danger / point deduction
val HazardCoralDim = Color(0xFF4A1D16)

val TextHigh = Color(0xFFF4F6FA)
val TextMedium = Color(0xFF9AA7BD)
val TextDisabled = Color(0xFF5B6579)

/** Buttons/badges filled with GuardianAmber always use dark text on top for
 * contrast, even though the rest of the UI is a dark theme. */
val OnAmber = Color(0xFF1A1200)
val OnCyan = Color(0xFF042224)
val OnGreen = Color(0xFF04220F)
val OnCoral = Color(0xFF2A0704)
