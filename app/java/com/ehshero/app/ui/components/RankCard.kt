package com.ehshero.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ehshero.app.domain.LeaderboardEntry
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.HeroPlateShape
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.SteelPanelElevated
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium
import com.ehshero.app.ui.theme.avatarOptionFor

/** A single ranked row for positions 4+ in the leaderboard list. */
@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(if (isCurrentUser) GuardianAmberDim else SteelPanel)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = "#${entry.rank}",
            style = MaterialTheme.typography.titleMedium,
            color = if (isCurrentUser) GuardianAmber else TextMedium,
            modifier = Modifier.width(40.dp)
        )
        HeroAvatar(option = avatarOptionFor(entry.avatarId), size = 40.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextHigh,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Level ${entry.level}" + if (entry.projectName.isNotBlank()) " • ${entry.projectName}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = TextMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "${entry.xp} XP",
            style = MaterialTheme.typography.titleSmall,
            color = if (isCurrentUser) GuardianAmber else TextHigh
        )
    }
}

/** Top-3 podium row shown above the ranked list (spec section 9: gold /
 * silver / bronze). Rank 1 is drawn taller and centered. */
@Composable
fun PodiumRow(top3: List<LeaderboardEntry>, modifier: Modifier = Modifier) {
    if (top3.isEmpty()) return
    val first = top3.getOrNull(0)
    val second = top3.getOrNull(1)
    val third = top3.getOrNull(2)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        second?.let { PodiumPillar(it, "\uD83E\uDD48", 96.dp, Modifier.weight(1f)) }
        first?.let { PodiumPillar(it, "\uD83E\uDD47", 124.dp, Modifier.weight(1f)) }
        third?.let { PodiumPillar(it, "\uD83E\uDD49", 80.dp, Modifier.weight(1f)) }
    }
}

@Composable
private fun PodiumPillar(
    entry: LeaderboardEntry,
    medal: String,
    pillarHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = medal, style = MaterialTheme.typography.headlineMedium)
        HeroAvatar(option = avatarOptionFor(entry.avatarId), size = 52.dp)
        Text(
            text = entry.name,
            style = MaterialTheme.typography.labelMedium,
            color = TextHigh,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = "${entry.xp} XP",
            style = MaterialTheme.typography.bodySmall,
            color = TextMedium
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(pillarHeight)
                .clip(MaterialTheme.shapes.medium)
                .background(SteelPanelElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#${entry.rank}",
                style = MaterialTheme.typography.headlineSmall,
                color = GuardianAmber,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** The prominent "YOUR RANK" card shown pinned below the podium/list, so a
 * user outside the top ranks can still see where they stand at a glance
 * (spec section 9). */
@Composable
fun YourRankCard(entry: LeaderboardEntry, modifier: Modifier = Modifier) {
    Card(
        shape = HeroPlateShape(),
        colors = CardDefaults.cardColors(containerColor = SteelPanelElevated),
        border = BorderStroke(1.dp, SignalCyan),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(text = "YOUR RANK", style = MaterialTheme.typography.labelMedium, color = SignalCyan)
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.displaySmall,
                    color = TextHigh
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            HeroAvatar(option = avatarOptionFor(entry.avatarId), size = 48.dp)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = entry.name, style = MaterialTheme.typography.titleSmall, color = TextHigh)
                Text(text = "${entry.xp} XP", style = MaterialTheme.typography.bodySmall, color = TextMedium)
            }
        }
    }
}
