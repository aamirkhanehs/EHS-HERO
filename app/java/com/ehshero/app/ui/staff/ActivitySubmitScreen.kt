package com.ehshero.app.ui.staff

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ehshero.app.data.model.ActivityType
import com.ehshero.app.data.model.HseCategory
import com.ehshero.app.data.model.ObservationType
import com.ehshero.app.ui.components.XpPill
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

@Composable
fun ActivitySubmitScreen(
    uid: String,
    missionId: String?,
    presetActivityType: String?,
    viewModel: ActivitySubmitViewModel = remember(uid, missionId, presetActivityType) {
        ActivitySubmitViewModel(uid, missionId, presetActivityType)
    },
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (state.submitted) {
        SubmittedConfirmation(xp = state.awardedXpPreview, onDone = onDone)
        return
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) viewModel.onPhotoPicked(uri) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Add Safety Activity", style = MaterialTheme.typography.headlineSmall, color = TextHigh)
        Text(
            text = "Submissions go to HSE for approval before XP is credited.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        Text(text = "ACTIVITY TYPE", style = MaterialTheme.typography.labelMedium, color = TextMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ActivityType.entries.toList()) { type ->
                ActivityTypeChip(
                    type = type,
                    selected = type == state.activityType,
                    enabled = !state.lockActivityType,
                    xp = state.pointRules[type.name] ?: type.defaultXp,
                    onClick = { viewModel.onActivityTypeChange(type) }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = state.activityType.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = TextHigh,
                modifier = Modifier.weight(1f)
            )
            XpPill(xp = state.awardedXpPreview)
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (state.activityType == ActivityType.SAFETY_OBSERVATION) {
            Text(text = "OBSERVATION TYPE", style = MaterialTheme.typography.labelMedium, color = TextMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ObservationType.entries.forEach { obs ->
                    FilterPill(
                        label = obs.displayName,
                        selected = obs == state.observationType,
                        onClick = { viewModel.onObservationTypeChange(obs) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            DropdownField(
                label = "Category",
                selectedLabel = state.category?.displayName ?: "Select a category",
                options = HseCategory.entries.toList().map { it.displayName },
                onOptionSelected = { index -> viewModel.onCategoryChange(HseCategory.entries[index]) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        LabeledField("Location *", state.location, viewModel::onLocationChange, singleLine = true)
        Spacer(modifier = Modifier.height(14.dp))
        LabeledField("Description *", state.description, viewModel::onDescriptionChange, minLines = 3)

        if (state.activityType == ActivityType.SAFETY_OBSERVATION) {
            Spacer(modifier = Modifier.height(14.dp))
            LabeledField("Immediate Action", state.immediateAction, viewModel::onImmediateActionChange, minLines = 2)
            Spacer(modifier = Modifier.height(14.dp))
            LabeledField("Recommended Corrective Action", state.correctiveAction, viewModel::onCorrectiveActionChange, minLines = 2)
        }

        Spacer(modifier = Modifier.height(14.dp))
        LabeledField("Remarks", state.remarks, viewModel::onRemarksChange, minLines = 2)

        Spacer(modifier = Modifier.height(18.dp))
        Text(text = "PHOTO", style = MaterialTheme.typography.labelMedium, color = TextMedium)
        Spacer(modifier = Modifier.height(8.dp))
        PhotoPickerBox(
            uri = state.photoUri,
            onPick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onClear = { viewModel.onPhotoPicked(null) }
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage.orEmpty(),
                color = HazardCoral,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = { viewModel.submit(context) },
            enabled = !state.isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(color = OnAmber, modifier = Modifier.height(22.dp), strokeWidth = 2.dp)
            } else {
                Text("SUBMIT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SubmittedConfirmation(xp: Int, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ClearanceGreen, modifier = Modifier.height(64.dp))
        Text(
            text = "MISSION SUBMITTED!",
            style = MaterialTheme.typography.headlineSmall,
            color = TextHigh,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Waiting for HSE approval. You'll earn +$xp XP once it's approved.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber)
        ) {
            Text("DONE")
        }
    }
}

@Composable
private fun ActivityTypeChip(type: ActivityType, selected: Boolean, enabled: Boolean, xp: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(containerColor = if (selected) GuardianAmberDim else SteelPanel)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) GuardianAmber else TextHigh
            )
            Text(text = "+$xp XP", style = MaterialTheme.typography.labelSmall, color = TextMedium)
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (selected) GuardianAmberDim else SteelPanel),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) GuardianAmber else TextMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextHigh,
            unfocusedTextColor = TextHigh,
            focusedBorderColor = GuardianAmber,
            unfocusedBorderColor = SteelOutline,
            focusedLabelColor = GuardianAmber,
            unfocusedLabelColor = TextMedium,
            cursorColor = GuardianAmber
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * A tap-to-open dropdown selector. Built on a plain [DropdownMenu] anchored
 * to a disabled, non-interactive [OutlinedTextField] with a transparent
 * clickable overlay, rather than Compose Material3's newer
 * ExposedDropdownMenuBox - that API's exact modifier signature has changed
 * across library versions, and this simpler composition is stable and easy
 * to reason about without a compiler on hand to verify against.
 */
@Composable
private fun DropdownField(
    label: String,
    selectedLabel: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            enabled = false,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = TextMedium
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = TextHigh,
                disabledBorderColor = SteelOutline,
                disabledLabelColor = TextMedium,
                disabledTrailingIconColor = TextMedium
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(SteelPanelElevated)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, color = TextHigh) },
                    onClick = {
                        onOptionSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoPickerBox(uri: Uri?, onPick: () -> Unit, onClear: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SteelPanelElevated)
    ) {
        if (uri == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onPick),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = "Add photo", tint = TextMedium)
                Text("Attach a photo", style = MaterialTheme.typography.bodySmall, color = TextMedium)
            }
        } else {
            AsyncImage(
                model = uri,
                contentDescription = "Attached photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(SteelPanel, RoundedCornerShape(50))
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Remove photo", tint = TextHigh)
            }
        }
    }
}
