package com.ehshero.app.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
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
import com.ehshero.app.data.model.ActivityStatus
import com.ehshero.app.ui.components.EmptyState
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.components.ActivityListItem
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

@Composable
fun ActivityHistoryScreen(
    uid: String?,
    showEmployee: Boolean = uid == null,
    viewModel: ActivityHistoryViewModel = remember(uid) { ActivityHistoryViewModel(uid) }
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        FilterRow(
            selected = state.statusFilter,
            onSelect = viewModel::setStatusFilter
        )
        when {
            state.isLoading -> LoadingState(message = "Loading activities...")
            state.activities.isEmpty() -> EmptyState(
                icon = Icons.Filled.Assignment,
                title = "No activities yet",
                message = if (uid != null) "Submissions you make will show up here." else "Nothing matches this filter yet."
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.activities, key = { it.activityId }) { activity ->
                    ActivityListItem(activity = activity, showEmployee = showEmployee)
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: ActivityStatus?, onSelect: (ActivityStatus?) -> Unit) {
    val options = listOf<ActivityStatus?>(null, ActivityStatus.PENDING, ActivityStatus.APPROVED, ActivityStatus.REJECTED)
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            val isSelected = option == selected
            Card(
                onClick = { onSelect(option) },
                colors = CardDefaults.cardColors(containerColor = if (isSelected) GuardianAmberDim else SteelPanel)
            ) {
                Text(
                    text = option?.name ?: "ALL",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) GuardianAmber else TextMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
