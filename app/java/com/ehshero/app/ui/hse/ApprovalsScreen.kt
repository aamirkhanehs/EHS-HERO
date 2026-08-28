package com.ehshero.app.ui.hse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.components.ActivityListItem
import com.ehshero.app.ui.components.EmptyState
import com.ehshero.app.ui.components.LoadingState

@Composable
fun ApprovalsScreen(
    reviewerUid: String,
    reviewerName: String,
    viewModel: ApprovalsViewModel = remember(reviewerUid) { ApprovalsViewModel(reviewerUid, reviewerName) },
    onOpenActivity: (activityId: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> LoadingState(message = "Loading pending approvals...")
        state.pending.isEmpty() -> EmptyState(
            icon = Icons.Filled.FactCheck,
            title = "All caught up!",
            message = "There are no activities waiting for approval right now."
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.pending, key = { it.activityId }) { activity ->
                ActivityListItem(
                    activity = activity,
                    showEmployee = true,
                    onClick = { onOpenActivity(activity.activityId) }
                )
            }
        }
    }
}
