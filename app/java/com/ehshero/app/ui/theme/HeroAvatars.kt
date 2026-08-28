package com.ehshero.app.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Preset "hero" avatars a Staff member can pick on their Profile screen
 * (spec section 17 asks for "a customizable anime-style avatar if
 * feasible"). This environment can't produce illustrated character art (see
 * README "What's simplified"), so instead of a full avatar creator, each
 * option is a procedurally drawn badge - a colour pair plus a Material
 * icon glyph, rendered by HeroAvatar.kt - which needs no image assets at
 * all and is trivial to extend with more entries later.
 */
data class AvatarOption(
    val id: String,
    val label: String,
    val primary: Color,
    val secondary: Color,
    val icon: ImageVector
)

val AVATAR_OPTIONS: List<AvatarOption> by lazy {
    listOf(
        AvatarOption("hero_01", "Amber Guardian", GuardianAmber, CommandNavy, Icons.Filled.Shield),
        AvatarOption("hero_02", "Signal Sentinel", SignalCyan, CommandNavy, Icons.Filled.Bolt),
        AvatarOption("hero_03", "Clearance Defender", ClearanceGreen, CommandNavy, Icons.Filled.Engineering),
        AvatarOption("hero_04", "Hazard Watch", HazardCoral, CommandNavy, Icons.Filled.Star),
        AvatarOption("hero_05", "Steel Ranger", TextHigh, SteelPanelElevated, Icons.Filled.Build),
        AvatarOption("hero_06", "Voltage Warden", SignalCyan, GuardianAmberDim, Icons.Filled.LocalFireDepartment)
    )
}

fun avatarOptionFor(avatarId: String): AvatarOption =
    AVATAR_OPTIONS.firstOrNull { it.id == avatarId } ?: AVATAR_OPTIONS.first()
