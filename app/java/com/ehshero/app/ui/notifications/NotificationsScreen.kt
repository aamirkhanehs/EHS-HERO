package com.ehshero.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.AppNotification
import com.ehshero.app.data.model.NotificationType
import com.ehshero.app.ui.components.EmptyState
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.theme.ClearanceGreen
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.HazardCoral
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.SteelPanelElevated
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium
import com.ehshero.app.util.DateUtils

private fun iconAndColorFor(type: NotificationType): Pair<ImageVector, androidx.compose.ui.graphics.Color> = when (type) {
    NotificationType.ACTIVITY_APPROVED -> Icons.Filled.CheckCircle to ClearanceGreen
    NotificationType.ACTIVITY_REJECTED -> Icons.Filled.Cancel to HazardCoral
    NotificationType.XP_RECEIVED -> Icons.Filled.EmojiEvents to GuardianAmber
    NotificationType.LEVEL_UP -> Icons.Filled.TrendingUp to GuardianAmber
    NotificationType.BADGE_UNLOCKED -> Icons.Filled.EmojiEvents to GuardianAmber
    NotificationType.MISSION_AVAILABLE -> Icons.Filled.Flag to SignalCyan
    NotificationType.CHALLENGE_STARTED -> Icons.Filled.Campaign to SignalCyan
    NotificationType.RANK_CHANGED -> Icons.Filled.Leaderboard to SignalCyan
    NotificationType.MONTHLY_WINNER -> Icons.Filled.WorkspacePremium to GuardianAmber
}

@Composable
fun NotificationsScreen(
    uid: String,
    viewModel: NotificationsViewModel = remember(uid) { NotificationsViewModel(uid) }
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.notifications.any { !it.read }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = viewModel::markAllRead) {
                    Text("Mark all read", color = SignalCyan)
                }
            }
        }
        when {
            state.isLoading -> LoadingState(message = "Loading notifications...")
            state.notifications.isEmpty() -> EmptyState(
                icon = Icons.Filled.NotificationsNone,
                title = "No notifications yet",
                message = "Approvals, level-ups and badge unlocks will show up here."
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.notifications, key = { it.notificationId }) { notification ->
                    NotificationRow(notification = notification, onClick = { viewModel.markRead(notification.notificationId) })
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, onClick: () -> Unit) {
    val type = runCatching { NotificationType.valueOf(notification.type) }.getOrDefault(NotificationType.XP_RECEIVED)
    val (icon, color) = iconAndColorFor(type)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (notification.read) SteelPanel else SteelPanelElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(text = notification.title, style = MaterialTheme.typography.titleSmall, color = TextHigh)
                Text(text = notification.body, style = MaterialTheme.typography.bodySmall, color = TextMedium)
                Text(
                    text = DateUtils.relativeTime(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .background(GuardianAmber, CircleShape)
                )
            }
        }
    }
}
