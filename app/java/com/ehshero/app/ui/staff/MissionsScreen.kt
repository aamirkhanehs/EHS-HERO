package com.ehshero.app.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.MissionStatus
import com.ehshero.app.ui.components.EmptyState
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.components.MissionCard
import com.ehshero.app.ui.theme.TextHigh

@Composable
fun MissionsScreen(
    uid: String,
    viewModel: MissionsViewModel = remember(uid) { MissionsViewModel(uid) },
    onStartMission: (missionId: String, activityType: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        LoadingState(message = "Loading missions...")
        return
    }
    if (state.missions.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.Flag,
            title = "No active missions",
            message = "HSE and Admin can create daily missions - check back soon, or earn XP by submitting any safety activity."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(text = "Safety Missions", style = MaterialTheme.typography.headlineSmall, color = TextHigh)
        }
        items(state.missions, key = { it.missionId }) { mission ->
            val progress = state.progressByMissionId[mission.missionId]
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
}
