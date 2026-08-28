package com.ehshero.app.ui.hse

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.SafetyActivity
import com.ehshero.app.data.remote.ActivityRepository
import com.ehshero.app.ui.components.ActivityStatusPill
import com.ehshero.app.ui.components.LoadingState
import com.ehshero.app.ui.components.XpPill
import com.ehshero.app.ui.theme.ClearanceGreen
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.HazardCoral
import com.ehshero.app.ui.theme.OnAmber
import com.ehshero.app.ui.theme.SteelOutline
import com.ehshero.app.ui.theme.SteelPanelElevated
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium
import com.ehshero.app.util.DateUtils

@Composable
fun ApprovalDetailScreen(
    activityId: String,
    reviewerUid: String,
    reviewerName: String,
    viewModel: ApprovalsViewModel = remember(reviewerUid) { ApprovalsViewModel(reviewerUid, reviewerName) },
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val activityRepository = remember { ActivityRepository() }
    var activity by remember { mutableStateOf<SafetyActivity?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var isSubmittingAction by remember { mutableStateOf(false) }

    LaunchedEffect(activityId, state.pending) {
        val fromLiveList = state.pending.firstOrNull { it.activityId == activityId }
        if (fromLiveList != null) {
            activity = fromLiveList
            loading = false
        } else if (activity == null) {
            activity = activityRepository.getActivity(activityId)
            loading = false
        }
    }

    LaunchedEffect(state.actionInProgressId) {
        if (isSubmittingAction && state.actionInProgressId == null) {
            onDone()
        }
    }

    if (loading || activity == null) {
        LoadingState(message = "Loading activity...")
        return
    }
    val current = activity!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = current.activityTypeEnum.displayName,
                style = MaterialTheme.typography.headlineSmall,
                color = TextHigh,
                modifier = Modifier.weight(1f)
            )
            XpPill(xp = current.xpValue)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ActivityStatusPill(status = current.statusEnum)
        Spacer(modifier = Modifier.height(20.dp))

        DetailRow("Employee", current.employeeName)
        DetailRow("Employee ID", current.employeeIdText)
        DetailRow("Project", current.projectName)
        DetailRow("Location", current.location)
        DetailRow("Date", DateUtils.formatDateTime(current.submittedAt))
        if (current.category.isNotBlank()) {
            DetailRow(
                "Category",
                runCatching { com.ehshero.app.data.model.HseCategory.valueOf(current.category).displayName }.getOrDefault(current.category)
            )
        }
        DetailRow("Description", current.description, isBlock = true)
        if (current.immediateAction.isNotBlank()) DetailRow("Immediate Action", current.immediateAction, isBlock = true)
        if (current.correctiveAction.isNotBlank()) DetailRow("Corrective Action", current.correctiveAction, isBlock = true)
        if (current.remarks.isNotBlank()) DetailRow("Remarks", current.remarks, isBlock = true)

        if (current.photoBase64.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Photo", style = MaterialTheme.typography.labelMedium, color = TextMedium)
            Spacer(modifier = Modifier.height(6.dp))
            PhotoFromBase64(current.photoBase64)
        }

        if (current.statusEnum == com.ehshero.app.data.model.ActivityStatus.PENDING) {
            Spacer(modifier = Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    enabled = !isSubmittingAction,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HazardCoral),
                    modifier = Modifier.weight(1f)
                ) { Text("REJECT") }
                Button(
                    onClick = {
                        isSubmittingAction = true
                        viewModel.approve(activityId)
                    },
                    enabled = !isSubmittingAction,
                    colors = ButtonDefaults.buttonColors(containerColor = ClearanceGreen, contentColor = OnAmber),
                    modifier = Modifier.weight(1f)
                ) { Text("APPROVE") }
            }
        } else if (current.rejectionReason.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            DetailRow("Rejection Reason", current.rejectionReason, isBlock = true)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject activity?") },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Reason (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextHigh,
                        unfocusedTextColor = TextHigh,
                        focusedBorderColor = HazardCoral,
                        unfocusedBorderColor = SteelOutline
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubmittingAction = true
                        viewModel.reject(activityId, rejectReason)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HazardCoral)
                ) { Text("Reject") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRejectDialog = false }) { Text("Cancel") }
            },
            containerColor = SteelPanelElevated
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, isBlock: Boolean = false) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextMedium)
        Text(
            text = value.ifBlank { "-" },
            style = if (isBlock) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall,
            color = TextHigh,
            fontWeight = if (isBlock) FontWeight.Normal else FontWeight.SemiBold
        )
    }
}

@Composable
private fun PhotoFromBase64(base64: String) {
    val bitmap = remember(base64) {
        runCatching {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Activity photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    }
}
