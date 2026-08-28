package com.ehshero.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.ActivityType
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.OnAmber
import com.ehshero.app.ui.theme.SteelOutline
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

@Composable
fun AdminSettingsScreen(
    viewModel: AdminSettingsViewModel = remember { AdminSettingsViewModel() }
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (state.isLoading) {
        LoadingState(message = "Loading settings...")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionTitle("Point Rules")
        Text(
            "How much XP each approved activity type is worth.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        ActivityType.entries.forEach { type ->
            PointRuleRow(
                label = type.displayName,
                xp = state.pointRules[type] ?: type.defaultXp,
                onChange = { viewModel.updatePointRuleLocally(type, it) }
            )
        }
        Button(
            onClick = viewModel::savePointRules,
            enabled = !state.isBusy,
            colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) { Text("SAVE POINT RULES") }

        Spacer(modifier = Modifier.height(28.dp))
        SectionTitle("Create Mission")
        CreateMissionForm(onCreate = viewModel::createMission)

        Spacer(modifier = Modifier.height(28.dp))
        SectionTitle("Create Project")
        CreateProjectForm(onCreate = viewModel::createProject)

        Spacer(modifier = Modifier.height(28.dp))
        SectionTitle("Demo Data")
        Text(
            "Seeds default levels, badges and point rules. Optionally also creates the four demo " +
                "users (Aamir, Rahul, Pritam, Sachin) from the spec - only meant for a brand new project.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMedium,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { viewModel.seedDemoData(context, includeDemoUsers = false) },
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f)
            ) { Text("Seed Config Only") }
            Button(
                onClick = { viewModel.seedDemoData(context, includeDemoUsers = true) },
                enabled = !state.isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber),
                modifier = Modifier.weight(1f)
            ) { Text("Seed + Demo Users") }
        }

        if (state.message != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SteelPanel),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = state.message.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHigh,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, color = TextHigh, fontWeight = FontWeight.Bold)
}

@Composable
private fun PointRuleRow(label: String, xp: Int, onChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextHigh, modifier = Modifier.weight(1f))
        IconButton(onClick = { onChange((xp - 5).coerceAtLeast(0)) }) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = TextMedium)
        }
        Text(text = "$xp XP", style = MaterialTheme.typography.titleSmall, color = GuardianAmber)
        IconButton(onClick = { onChange(xp + 5) }) {
            Icon(Icons.Filled.Add, contentDescription = "Increase", tint = TextMedium)
        }
    }
}

@Composable
private fun CreateMissionForm(onCreate: (String, String, ActivityType, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ActivityType.HAZARD_IDENTIFICATION) }
    var xp by remember { mutableStateOf(20) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FormField("Title", title) { title = it }
        FormField("Description", description) { description = it }
        Text("Target activity: ${type.displayName}", style = MaterialTheme.typography.bodySmall, color = TextMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ActivityType.entries.toList()) { t ->
                Card(
                    onClick = { type = t; xp = t.defaultXp },
                    colors = CardDefaults.cardColors(containerColor = if (t == type) GuardianAmberDim else SteelPanel)
                ) {
                    Text(
                        t.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (t == type) GuardianAmber else TextMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }
        PointRuleRow("Reward", xp) { xp = it }
        Button(
            onClick = {
                onCreate(title, description, type, xp)
                title = ""
                description = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber),
            modifier = Modifier.fillMaxWidth()
        ) { Text("CREATE MISSION") }
    }
}

@Composable
private fun CreateProjectForm(onCreate: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Transmission Line") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FormField("Project name", name) { name = it }
        FormField("Location", location) { location = it }
        FormField("Type (e.g. Transmission Line, Construction)", type) { type = it }
        Button(
            onClick = {
                onCreate(name, location, type)
                name = ""
                location = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber),
            modifier = Modifier.fillMaxWidth()
        ) { Text("CREATE PROJECT") }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextHigh, unfocusedTextColor = TextHigh,
            focusedBorderColor = GuardianAmber, unfocusedBorderColor = SteelOutline
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
