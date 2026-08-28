package com.ehshero.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.Badge
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.SteelOutline
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.SteelPanelElevated
import com.ehshero.app.ui.theme.TextDisabled
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

/** Maps the free-text [Badge.iconId] (set in DefaultConfig / by Admin) to a
 * concrete icon, with a safe fallback so a typo'd or future icon id never
 * crashes the badges screen. */
fun iconForBadge(iconId: String): ImageVector = when (iconId) {
    "eye" -> Icons.Filled.Visibility
    "megaphone" -> Icons.Filled.Campaign
    "hazard" -> Icons.Filled.WarningAmber
    "sword" -> Icons.Filled.MilitaryTech
    "alert" -> Icons.Filled.ReportProblem
    "graduation" -> Icons.Filled.School
    "thumb_up" -> Icons.Filled.ThumbUp
    "life_ring" -> Icons.Filled.HealthAndSafety
    "crown" -> Icons.Filled.EmojiEvents
    "shield_star" -> Icons.Filled.WorkspacePremium
    else -> Icons.Filled.EmojiEvents
}

/**
 * One badge tile for the Badges grid (spec section 8). Unlocked badges show
 * their real icon, name and description in the amber accent; locked badges
 * show a padlock and "???" - the earn condition stays hidden until it's
 * actually achieved, matching the spec's "locked/unlocked states" call-out.
 */
@Composable
fun BadgeCard(
    badge: Badge,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.82f),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) SteelPanelElevated else SteelPanel
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (unlocked) GuardianAmberDim else SteelOutline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (unlocked) GuardianAmberDim else SteelOutline,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (unlocked) iconForBadge(badge.iconId) else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (unlocked) GuardianAmber else TextDisabled
                )
            }
            Spacer(modifier = Modifier.padding(top = 10.dp))
            Text(
                text = if (unlocked) badge.name else "???",
                style = MaterialTheme.typography.titleSmall,
                color = if (unlocked) TextHigh else TextDisabled,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            if (unlocked) {
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Locked",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDisabled,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
