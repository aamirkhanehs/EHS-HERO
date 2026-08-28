package com.ehshero.app.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.MissionStatus
import com.ehshero.app.data.model.UserMission
import com.ehshero.app.ui.components.BadgeUnlockOverlay
import com.ehshero.app.ui.components.EmptyState
import com.ehshero.app.ui.components.HeroShieldRing
import com.ehshero.app.ui.components.LevelUpOverlay
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.components.MissionCard
import com.ehshero.app.ui.components.StatChip
import com.ehshero.app.ui.components.XpProgressBar
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.HeroPlateShape
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.SteelPanelElevated
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

@Composable
fun StaffHomeScreen(
    uid: String,
    viewModel: StaffHomeViewModel = remember(uid) { StaffHomeViewModel(uid) },
    onStartMission: (missionId: String, activityType: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading || state.user == null) {
        LoadingState(message = "Loading your dashboard...")
        return
    }
    val user = state.user!!

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeaderCard(
                    name = user.name.ifBlank { "Hero" },
                    levelNumber = state.levelProgress?.currentLevel?.levelNumber ?: user.level,
                    levelTitle = state.levelProgress?.currentLevel?.title ?: "Safety Rookie",
                    xpIntoLevel = state.levelProgress?.xpIntoLevel ?: 0,
                    xpForNextLevel = state.levelProgress?.xpForNextLevel ?: 0,
                    progress = state.levelProgress?.progress ?: 0f,
                    rank = state.myRank,
                    badgeCount = state.unlockedBadgeIds.size,
                    missionsCompleted = state.userMissionsByMissionId.values.count {
                        it.status == MissionStatus.APPROVED.name
                    },
                    streak = user.currentStreak
                )
            }
            item {
                Text(
                    text = "TODAY'S SAFETY MISSIONS",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextHigh,
                    fontWeight = FontWeight.Bold
                )
            }
            if (state.missions.isEmpty()) {
                item {
                    Text(
                        text = "No missions are active right now - check back soon, or submit any safety activity to earn XP.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMedium
                    )
                }
            } else {
                items(state.missions, key = { it.missionId }) { mission ->
                    val progress = state.userMissionsByMissionId[mission.missionId]
                    val status = progress?.let {
                        runCatching { MissionStatus.valueOf(it.status) }.getOrDefault(MissionStatus.NOT_STARTED)
                    } ?: MissionStatus.NOT_STARTED
                    MissionCard(
                        title = mission.title,
                        description = mission.description,
                        xpReward = mission.xpReward,
                        status = status,
                        onStart = {
                            viewModel.startMission(mission.missionId)
                            onStartMission(mission.missionId, mission.activityType)
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }

    state.pendingLevelUp?.let { level ->
        LevelUpOverlay(
            userName = user.name.ifBlank { "Hero" },
            levelNumber = level.levelNumber,
            levelTitle = level.title,
            onDismiss = viewModel::dismissLevelUp
        )
    } ?: state.badgeUnlockQueue.firstOrNull()?.let { badge ->
        BadgeUnlockOverlay(badge = badge, onDismiss = viewModel::dismissNextBadgeUnlock)
    }
}

@Composable
private fun HomeHeaderCard(
    name: String,
    levelNumber: Int,
    levelTitle: String,
    xpIntoLevel: Int,
    xpForNextLevel: Int,
    progress: Float,
    rank: Int?,
    badgeCount: Int,
    missionsCompleted: Int,
    streak: Int
) {
    androidx.compose.material3.Card(
        shape = HeroPlateShape(cornerRadius = 20.dp, cutCorner = 28.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = SteelPanelElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "WELCOME BACK", style = MaterialTheme.typography.labelMedium, color = SignalCyan)
            Text(
                text = name.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                color = TextHigh,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeroShieldRing(levelNumber = levelNumber, progress = progress, diameter = 96.dp, strokeWidth = 8.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = levelTitle.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = GuardianAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    XpProgressBar(
                        progress = progress,
                        trailingLabel = if (xpForNextLevel > 0) "$xpIntoLevel / $xpForNextLevel XP" else "MAX LEVEL"
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip(
                    icon = Icons.Filled.MilitaryTech,
                    value = rank?.let { "#$it" } ?: "-",
                    label = "Rank",
                    accentColor = GuardianAmber,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Filled.EmojiEvents,
                    value = badgeCount.toString(),
                    label = "Badges",
                    accentColor = SignalCyan,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Filled.Flag,
                    value = missionsCompleted.toString(),
                    label = "Missions",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    icon = Icons.Filled.LocalFireDepartment,
                    value = streak.toString(),
                    label = "Streak",
                    accentColor = com.ehshero.app.ui.theme.HazardCoral,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
