package com.ehshero.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.theme.ClearanceGreen
import com.ehshero.app.ui.theme.ClearanceGreenDim
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.HazardCoral
import com.ehshero.app.ui.theme.HazardCoralDim
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff

/** Connectivity/sync state, derived from Firestore's own snapshot metadata
 * (see util/ConnectivityObserver.kt) rather than a hand-rolled queue -
 * Firestore already persists writes offline and syncs automatically, this
 * just surfaces that state to the user per spec section 21. */
enum class SyncState { OFFLINE, SYNCING, JUST_SYNCED, ONLINE_IDLE }

@Composable
fun SyncStatusBanner(state: SyncState, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = state != SyncState.ONLINE_IDLE,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val (bg, fg, label) = when (state) {
            SyncState.OFFLINE -> Triple(HazardCoralDim, HazardCoral, "OFFLINE MODE \u2013 changes will sync when you're back online")
            SyncState.SYNCING -> Triple(GuardianAmberDim, GuardianAmber, "SYNCING...")
            SyncState.JUST_SYNCED -> Triple(ClearanceGreenDim, ClearanceGreen, "SYNC COMPLETE")
            SyncState.ONLINE_IDLE -> Triple(ClearanceGreenDim, ClearanceGreen, "")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (state) {
                SyncState.OFFLINE -> Icon(Icons.Filled.CloudOff, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                SyncState.SYNCING -> CircularProgressIndicator(color = fg, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                SyncState.JUST_SYNCED -> Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                SyncState.ONLINE_IDLE -> {}
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
