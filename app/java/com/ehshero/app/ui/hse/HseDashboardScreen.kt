package com.ehshero.app.ui.hse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.components.PodiumRow
import com.ehshero.app.ui.components.SimpleBarChart
import com.ehshero.app.ui.components.SimpleDonutChart
import com.ehshero.app.ui.components.StatCard
import com.ehshero.app.ui.theme.ClearanceGreen
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.HazardCoral
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

@Composable
fun HseDashboardScreen(
    viewModel: HseDashboardViewModel = remember { HseDashboardViewModel() },
    onViewApprovals: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        LoadingState(message = "Loading command center...")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "EHS COMMAND CENTER",
                style = MaterialTheme.typography.headlineSmall,
                color = TextHigh,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            if (state.pendingApprovals > 0) {
                Card(
                    onClick = onViewApprovals,
                    colors = CardDefaults.cardColors(containerColor = com.ehshero.app.ui.theme.GuardianAmberDim),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(Icons.Filled.HourglassTop, contentDescription = null, tint = GuardianAmber)
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "${state.pendingApprovals} activities awaiting approval",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextHigh
                            )
                            Text("Tap to review", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                        }
                    }
                }
            }
        }
        item {
            KpiGrid(state)
        }
        if (state.topTen.isNotEmpty()) {
            item {
                Text(text = "Top 10 Employees", style = MaterialTheme.typography.titleLarge, color = TextHigh)
            }
            item {
                PodiumRow(top3 = state.topTen.take(3))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.topTen.drop(3).forEach { entry ->
                        com.ehshero.app.ui.components.LeaderboardRow(entry = entry, isCurrentUser = false)
                    }
                }
            }
        }
        if (state.activityDistribution.isNotEmpty()) {
            item {
                Text(text = "Activity Distribution", style = MaterialTheme.typography.titleLarge, color = TextHigh)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = SteelPanel), modifier = Modifier.fillMaxWidth()) {
                    SimpleDonutChart(slices = state.activityDistribution, modifier = Modifier.padding(16.dp))
                }
            }
        }
        if (state.projectPerformance.isNotEmpty()) {
            item {
                Text(text = "Project-wise Performance (Total XP)", style = MaterialTheme.typography.titleLarge, color = TextHigh)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = SteelPanel), modifier = Modifier.fillMaxWidth()) {
                    SimpleBarChart(
                        data = state.projectPerformance,
                        barColor = SignalCyan,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiGrid(state: HseDashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(Icons.Filled.PeopleAlt, state.totalEmployees.toString(), "Total Employees", modifier = Modifier.weight(1f))
            StatCard(Icons.Filled.Groups, state.activeEmployees.toString(), "Active Employees", modifier = Modifier.weight(1f), accentColor = ClearanceGreen)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(Icons.Filled.Bolt, state.totalXp.toString(), "Total XP Earned", modifier = Modifier.weight(1f))
            StatCard(Icons.Filled.HourglassTop, state.pendingApprovals.toString(), "Pending Approvals", modifier = Modifier.weight(1f), accentColor = HazardCoral)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(Icons.Filled.Campaign, state.tbtCount.toString(), "TBT Conducted", modifier = Modifier.weight(1f))
            StatCard(Icons.Filled.WarningAmber, state.observationCount.toString(), "Observations", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(Icons.Filled.ThumbUp, state.goodPracticeCount.toString(), "Good Practices", modifier = Modifier.weight(1f), accentColor = ClearanceGreen)
            StatCard(Icons.Filled.Assignment, state.nearMissCount.toString(), "Near Misses", modifier = Modifier.weight(1f), accentColor = HazardCoral)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(Icons.Filled.School, state.trainingCount.toString(), "Trainings", modifier = Modifier.weight(1f))
            StatCard(Icons.Filled.WarningAmber, state.hazardCount.toString(), "Hazards Identified", modifier = Modifier.weight(1f), accentColor = SignalCyan)
        }
        state.topPerformer?.let { top ->
            StatCard(Icons.Filled.Groups, top.name, "Top Performer (${top.totalXp} XP)", modifier = Modifier.fillMaxWidth(), accentColor = GuardianAmber)
        }
    }
}
