package com.ehshero.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ehshero.app.data.model.Badge
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.GuardianAmberDim
import com.ehshero.app.ui.theme.OnAmber
import com.ehshero.app.ui.theme.SteelPanelElevated
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

/**
 * Full-screen "BADGE UNLOCKED!" animation (spec section 22). Shown after
 * LevelUpOverlay if both fire from the same approval - see StaffHomeScreen's
 * celebration queue.
 */
@Composable
fun BadgeUnlockOverlay(
    badge: Badge,
    onDismiss: () -> Unit
) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.8f, animationSpec = tween(420))
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SteelPanelElevated),
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BADGE UNLOCKED!",
                        style = MaterialTheme.typography.labelLarge,
                        color = GuardianAmber
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(GuardianAmberDim, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconForBadge(badge.iconId),
                            contentDescription = null,
                            tint = GuardianAmber,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = badge.name.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextHigh,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GuardianAmber, contentColor = OnAmber),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "NICE!", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
