package com.ehshero.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.ActivityStatus
import com.ehshero.app.data.model.MissionStatus
import com.ehshero.app.ui.theme.ClearanceGreen
import com.ehshero.app.ui.theme.ClearanceGreenDim
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.HazardCoral
import com.ehshero.app.ui.theme.HazardCoralDim
import com.ehshero.app.ui.theme.OnAmber
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.SignalCyanDim
import com.ehshero.app.ui.theme.SteelOutline
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

/** "+15 XP" pill - used on mission cards, the activity type picker, and the
 * approval detail screen. */
@Composable
fun XpPill(xp: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(GuardianAmber)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(Icons.Filled.Bolt, contentDescription = null, tint = OnAmber, modifier = Modifier.size(14.dp))
        Text(
            text = "+$xp XP",
            style = MaterialTheme.typography.labelMedium,
            color = OnAmber,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Composable
fun ActivityStatusPill(status: ActivityStatus, modifier: Modifier = Modifier) {
    val (bg, fg, label) = when (status) {
        ActivityStatus.PENDING -> Triple(SteelOutline, TextMedium, "PENDING APPROVAL")
        ActivityStatus.APPROVED -> Triple(ClearanceGreenDim, ClearanceGreen, "APPROVED")
        ActivityStatus.REJECTED -> Triple(HazardCoralDim, HazardCoral, "REJECTED")
    }
    StatusPillBase(bg, fg, label, modifier)
}

@Composable
fun MissionStatusPill(status: MissionStatus, modifier: Modifier = Modifier) {
    val (bg, fg, label) = when (status) {
        MissionStatus.NOT_STARTED -> Triple(SteelOutline, TextMedium, "NOT STARTED")
        MissionStatus.IN_PROGRESS -> Triple(SignalCyanDim, SignalCyan, "IN PROGRESS")
        MissionStatus.COMPLETED -> Triple(GuardianAmberDim, GuardianAmber, "AWAITING HSE")
        MissionStatus.APPROVED -> Triple(ClearanceGreenDim, ClearanceGreen, "COMPLETE")
        MissionStatus.EXPIRED -> Triple(HazardCoralDim, HazardCoral, "EXPIRED")
    }
    StatusPillBase(bg, fg, label, modifier)
}

@Composable
private fun StatusPillBase(
    bgColor: androidx.compose.ui.graphics.Color,
    fgColor: androidx.compose.ui.graphics.Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = fgColor,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
