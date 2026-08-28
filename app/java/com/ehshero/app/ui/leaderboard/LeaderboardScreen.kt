package com.ehshero.app.ui.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.LeaderboardPeriod
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.components.LeaderboardRow
import com.ehshero.app.ui.components.PodiumRow
import com.ehshero.app.ui.components.YourRankCard
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

@Composable
fun LeaderboardScreen(
    uid: String,
    viewModel: LeaderboardViewModel = remember(uid) { LeaderboardViewModel(uid) }
) {
    val state by viewModel.uiState.collectAsState()
    val myEntry = state.entries.firstOrNull { it.uid == uid }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LeaderboardPeriod.entries.toList()) { period ->
                FilterChip(
                    label = period.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                    selected = period == state.period,
                    onClick = { viewModel.setPeriod(period) }
                )
            }
        }
        if (state.projects.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(label = "All Projects", selected = state.projectFilter == null, onClick = { viewModel.setProjectFilter(null) })
                }
                items(state.projects) { project ->
                    FilterChip(
                        label = project.name,
                        selected = state.projectFilter == project.projectId,
                        onClick = { viewModel.setProjectFilter(project.projectId) }
                    )
                }
            }
        }

        if (state.isLoading) {
            LoadingState(message = "Loading leaderboard...")
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.entries.isEmpty()) {
                item {
                    Text(
                        text = "No activity in this period yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMedium
                    )
                }
            } else {
                item { PodiumRow(top3 = state.entries.take(3)) }
                myEntry?.let { item { YourRankCard(entry = it) } }
                items(state.entries.drop(3), key = { it.uid }) { entry ->
                    LeaderboardRow(entry = entry, isCurrentUser = entry.uid == uid)
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (selected) GuardianAmberDim else SteelPanel)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) GuardianAmber else TextMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
