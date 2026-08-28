package com.ehshero.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.SteelOutline
import com.ehshero.app.ui.theme.TextMedium

/**
 * The app's signature visual motif (see README "Design system"): a circular
 * shield-style ring that sweeps clockwise from the top as the user
 * progresses through their current level, with the level number centered
 * inside it. Appears on the Staff home header, Profile, and inside the
 * full-screen Level Up overlay - deliberately kept to those three places so
 * it stays a signature rather than becoming wallpaper.
 */
@Composable
fun HeroShieldRing(
    levelNumber: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    showLabel: Boolean = true
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = SteelOutline,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (clamped > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(GuardianAmber, SignalCyan, GuardianAmber)),
                    startAngle = -90f,
                    sweepAngle = 360f * clamped,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showLabel) {
                Text(
                    text = "LEVEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium
                )
            }
            Text(
                text = levelNumber.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
