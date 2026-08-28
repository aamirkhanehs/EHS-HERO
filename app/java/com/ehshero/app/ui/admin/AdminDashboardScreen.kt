package com.ehshero.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.components.StatCard
import com.ehshero.app.ui.hse.HseDashboardViewModel
import com.ehshero.app.ui.theme.ClearanceGreen
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.HazardCoral
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

@Composable
fun AdminDashboardScreen(
    viewModel: HseDashboardViewModel = remember { HseDashboardViewModel() },
    onOpenUsers: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenReports: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        LoadingState(message = "Loading dashboard...")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "ADMIN DASHBOARD", style = MaterialTheme.typography.headlineSmall, color = TextHigh, fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction(Icons.Filled.People, "Users", onOpenUsers, Modifier.weight(1f))
                QuickAction(Icons.Filled.Settings, "Settings", onOpenSettings, Modifier.weight(1f))
                QuickAction(Icons.Filled.Assessment, "Reports", onOpenReports, Modifier.weight(1f))
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(Icons.Filled.People, state.totalEmployees.toString(), "Total Employees", modifier = Modifier.weight(1f))
                    StatCard(Icons.Filled.People, state.activeEmployees.toString(), "Active", modifier = Modifier.weight(1f), accentColor = ClearanceGreen)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(Icons.Filled.Assessment, state.totalXp.toString(), "Total XP Awarded", modifier = Modifier.weight(1f))
                    StatCard(Icons.Filled.Assessment, state.pendingApprovals.toString(), "Pending Approvals", modifier = Modifier.weight(1f), accentColor = HazardCoral)
                }
            }
        }
        if (state.projectPerformance.isNotEmpty()) {
            item {
                Text(text = "Projects", style = MaterialTheme.typography.titleLarge, color = TextHigh)
            }
            items(state.projectPerformance, key = { it.first }) { (name, xp) ->
                Card(colors = CardDefaults.cardColors(containerColor = SteelPanel), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, style = MaterialTheme.typography.titleSmall, color = TextHigh)
                        Text("$xp XP", style = MaterialTheme.typography.titleSmall, color = SignalCyan)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SteelPanel),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Icon(icon, contentDescription = label, tint = GuardianAmber)
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextMedium, modifier = Modifier.padding(top = 6.dp))
        }
    }
}
