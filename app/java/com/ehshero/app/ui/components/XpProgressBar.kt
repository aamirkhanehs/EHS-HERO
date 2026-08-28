package com.ehshero.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.SteelOutline
import com.ehshero.app.ui.theme.TextMedium

/**
 * A compact XP bar - used everywhere the circular HeroShieldRing would be
 * too large: list rows, the mission card, the leaderboard "your rank" strip.
 * Animates its fill smoothly on value change (spec section 22: "XP bar
 * fills smoothly").
 */
@Composable
fun XpProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    label: String? = null,
    trailingLabel: String? = null
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "xp_progress"
    )
    Column(modifier = modifier) {
        if (label != null || trailingLabel != null) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (trailingLabel != null) {
                    Text(
                        text = trailingLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMedium,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(CircleShape)
                .background(SteelOutline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated.coerceAtLeast(0.02f))
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(GuardianAmber, SignalCyan)))
            )
        }
    }
}
