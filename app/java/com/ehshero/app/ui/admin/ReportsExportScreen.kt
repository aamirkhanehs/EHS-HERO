package com.ehshero.app.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.ActivityStatus
import com.ehshero.app.ui.theme.ClearanceGreen
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.OnAmber
import com.ehshero.app.ui.theme.SteelPanel
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsExportScreen(
    viewModel: ReportsExportViewModel = remember { ReportsExportViewModel() }
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingCsv by remember { mutableStateOf<String?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        val csv = pendingCsv
        if (uri != null && csv != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
        }
        pendingCsv = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Export Activity Data", style = MaterialTheme.typography.headlineSmall, color = TextHigh, fontWeight = FontWeight.Bold)
        Text(
            "Choose filters, then export a CSV you can open in Excel or Google Sheets.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Text("DATE RANGE", style = MaterialTheme.typography.labelMedium, color = TextMedium)
        FilterRow(
            options = listOf(7 to "7 days", 30 to "30 days", 90 to "90 days", null to "All time"),
            selected = state.rangeDays,
            onSelect = viewModel::setRangeDays
        )

        Spacer()
        Text("STATUS", style = MaterialTheme.typography.labelMedium, color = TextMedium)
        FilterRow(
            options = listOf(
                null to "All",
                ActivityStatus.APPROVED to "Approved",
                ActivityStatus.PENDING to "Pending",
                ActivityStatus.REJECTED to "Rejected"
            ),
            selected = state.statusFilter,
            onSelect = viewModel::setStatusFilter
        )

        if (state.projects.isNotEmpty()) {
            Spacer()
            Text("PROJECT", style = MaterialTheme.typography.labelMedium, color = TextMedium)
            FilterRow(
                options = listOf(null to "All Projects") + state.projects.map { it.projectId to it.name },
                selected = state.projectFilter,
                onSelect = viewModel::setProjectFilter
            )
        }

        Spacer()
        Button(
            onClick = {
                scope.launch {
                    val csv = viewModel.buildCsv()
                    if (csv != null) {
                        pendingCsv = csv
                        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                        saveLauncher.launch("ehs_hero_export_$stamp.csv")
                    }
                }
            },
            enabled = !state.isExporting,
            colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            if (state.isExporting) {
                CircularProgressIndicator(color = OnAmber, modifier = Modifier.padding(2.dp), strokeWidth = 2.dp)
            } else {
                Text("EXPORT CSV")
            }
        }

        if (state.lastExportRowCount != null) {
            Card(colors = CardDefaults.cardColors(containerColor = SteelPanel), modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)) {
                Text(
                    "Exported ${state.lastExportRowCount} activities.",
                    color = ClearanceGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
        if (state.message != null) {
            Text(
                text = state.message.orEmpty(),
                color = TextMedium,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun Spacer() {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 18.dp))
}

@Composable
private fun <T> FilterRow(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
        items(options) { (value, label) ->
            val isSelected = value == selected
            Card(
                onClick = { onSelect(value) },
                colors = CardDefaults.cardColors(containerColor = if (isSelected) GuardianAmberDim else SteelPanel)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) GuardianAmber else TextMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
