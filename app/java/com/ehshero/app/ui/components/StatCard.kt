package com.ehshero.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

/** A single KPI tile for the HSE/Admin dashboard grids - "Total Employees",
 * "Pending Approvals", etc (spec section 13). */
@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = com.ehshero.app.ui.theme.GuardianAmber
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SteelPanel),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = TextHigh,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextMedium
            )
        }
    }
}
