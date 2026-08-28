package com.ehshero.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.SafetyActivity
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium
import com.ehshero.app.util.DateUtils

/** One row for an activity list - type, submitter (optional), location,
 * relative time, XP value and status pill. Used by ActivityHistoryScreen,
 * the HSE Activities tab, and (with [showEmployee] on) approval summaries. */
@Composable
fun ActivityListItem(
    activity: SafetyActivity,
    showEmployee: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        colors = CardDefaults.cardColors(containerColor = SteelPanel),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.activityTypeEnum.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextHigh,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showEmployee) {
                        Text(
                            text = activity.employeeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = listOf(activity.location, DateUtils.relativeTime(activity.submittedAt))
                            .filter { it.isNotBlank() }
                            .joinToString(" \u2022 "),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                XpPill(xp = activity.xpValue)
            }
            Spacer(modifier = Modifier.padding(top = 6.dp))
            ActivityStatusPill(status = activity.statusEnum)
        }
    }
}
