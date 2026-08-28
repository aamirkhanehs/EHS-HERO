package com.ehshero.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.User
import com.ehshero.app.data.model.UserRole
import com.ehshero.app.ui.components.HeroAvatar
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.theme.ClearanceGreen
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.HazardCoral
import com.ehshero.app.ui.theme.OnAmber
import com.ehshero.app.ui.theme.SteelOutline
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.SteelPanelElevated
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium
import com.ehshero.app.ui.theme.avatarOptionFor

@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel = remember { UserManagementViewModel() }
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = GuardianAmber, contentColor = OnAmber) {
                Icon(Icons.Filled.Add, contentDescription = "Create user")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search by name, ID, or email") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMedium) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextHigh, unfocusedTextColor = TextHigh,
                    focusedBorderColor = GuardianAmber, unfocusedBorderColor = SteelOutline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            if (state.isLoading) {
                LoadingState(message = "Loading users...")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.users, key = { it.uid }) { user ->
                        UserRow(
                            user = user,
                            onRoleChange = { role -> viewModel.setRole(user.uid, role) },
                            onActiveChange = { active -> viewModel.setActive(user.uid, active) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateUserDialog(
            projects = state.projects,
            isSubmitting = state.isCreatingUser,
            onDismiss = { showCreateDialog = false },
            onCreate = { employeeId, name, email, designation, projectId, projectName, role ->
                showCreateDialog = false
            },
            viewModel = viewModel
        )
    }
}

@Composable
private fun UserRow(user: User, onRoleChange: (UserRole) -> Unit, onActiveChange: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = SteelPanel), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeroAvatar(option = avatarOptionFor(user.avatarId), size = 44.dp)
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)) {
                    Text(text = user.name, style = MaterialTheme.typography.titleSmall, color = TextHigh)
                    Text(
                        text = "${user.employeeId} \u2022 ${user.designation.ifBlank { "-" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium
                    )
                }
                Switch(
                    checked = user.isActive,
                    onCheckedChange = onActiveChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = ClearanceGreen)
                )
            }
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRole.entries.forEach { role ->
                    val selected = role == user.roleEnum
                    Card(
                        onClick = { onRoleChange(role) },
                        colors = CardDefaults.cardColors(containerColor = if (selected) GuardianAmberDim else SteelPanelElevated)
                    ) {
                        Text(
                            text = role.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) GuardianAmber else TextMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateUserDialog(
    projects: List<com.ehshero.app.data.model.Project>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, String, UserRole) -> Unit,
    viewModel: UserManagementViewModel
) {
    val context = LocalContext.current
    var employeeId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.STAFF) }
    val project = projects.firstOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create user") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Creates a real login (temporary password: EhsHero@123) plus their profile. " +
                        "If you'd rather not use in-app account creation, see README \"Creating users\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMedium
                )
                DialogField("Employee ID", employeeId) { employeeId = it }
                DialogField("Full name", name) { name = it }
                DialogField("Email", email) { email = it }
                DialogField("Designation", designation) { designation = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { r ->
                        Card(
                            onClick = { role = r },
                            colors = CardDefaults.cardColors(containerColor = if (role == r) GuardianAmberDim else SteelPanelElevated)
                        ) {
                            Text(
                                r.name,
                                color = if (role == r) GuardianAmber else TextMedium,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    viewModel.createUser(
                        context, employeeId, name, email, designation,
                        project?.projectId.orEmpty(), project?.name.orEmpty(), role
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber)
            ) { Text("Create") }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = SteelPanelElevated, contentColor = TextHigh)) {
                Text("Cancel")
            }
        },
        containerColor = SteelPanelElevated
    )
}

@Composable
private fun DialogField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextHigh, unfocusedTextColor = TextHigh,
            focusedBorderColor = GuardianAmber, unfocusedBorderColor = SteelOutline
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
