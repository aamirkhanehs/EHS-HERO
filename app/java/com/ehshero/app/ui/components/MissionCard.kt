package com.ehshero.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.MissionStatus
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.HeroPlateShape
import com.ehshero.app.ui.theme.OnAmber
import com.ehshero.app.ui.theme.SteelPanelElevated
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

/**
 * A single mission tile for "TODAY'S SAFETY MISSIONS" (spec section 5) and
 * the Missions tab. Shows the reward, current status, and a Start/Continue
 * action that's hidden once the mission is done or expired.
 */
@Composable
fun MissionCard(
    title: String,
    description: String,
    xpReward: Int,
    status: MissionStatus,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = HeroPlateShape(),
        colors = CardDefaults.cardColors(containerColor = SteelPanelElevated),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextHigh)
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                XpPill(xp = xpReward)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MissionStatusPill(status = status)
                Spacer(modifier = Modifier.weight(1f))
                when (status) {
                    MissionStatus.NOT_STARTED -> Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber)
                    ) { Text("START MISSION") }

                    MissionStatus.IN_PROGRESS -> Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber)
                    ) { Text("CONTINUE") }

                    else -> {}
                }
            }
        }
    }
}
