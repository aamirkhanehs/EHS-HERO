package com.ehshero.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ehshero.app.ui.theme.ClearanceGreen
import com.ehshero.app.ui.theme.GuardianAmber
import com.ehshero.app.ui.theme.HazardCoral
import com.ehshero.app.ui.theme.SignalCyan
import com.ehshero.app.ui.theme.TextHigh
import com.ehshero.app.ui.theme.TextMedium

/**
 * Three small, dependency-free charts for the HSE Command Center (spec
 * section 13). Hand-drawn with Canvas rather than a third-party charting
 * library, to avoid pulling in a dependency whose Compose-version
 * compatibility can't be verified in this environment.
 */

/** Vertical bar chart - e.g. "Activity Distribution" or "Top 10 Employees". */
@Composable
fun SimpleBarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    barColor: Color = GuardianAmber,
    chartHeight: androidx.compose.ui.unit.Dp = 140.dp
) {
    val maxValue = (data.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            if (data.isEmpty()) return@Canvas
            val slotWidth = size.width / data.size
            val barWidth = (slotWidth * 0.5f).coerceAtMost(48f)
            data.forEachIndexed { index, pair ->
                val fraction = pair.second.toFloat() / maxValue.toFloat()
                val barHeight = size.height * fraction
                val left = index * slotWidth + (slotWidth - barWidth) / 2f
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            data.forEach { pair ->
                Text(
                    text = pair.first,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

/** Simple line/trend chart - e.g. "XP Performance" or "Activity Trend" over
 * the last N days. */
@Composable
fun SimpleTrendChart(
    data: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = SignalCyan,
    chartHeight: androidx.compose.ui.unit.Dp = 120.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        if (data.size < 2) return@Canvas
        val maxValue = (data.maxOrNull() ?: 0)
        val minValue = (data.minOrNull() ?: 0)
        val range = (maxValue - minValue).coerceAtLeast(1)
        val stepX = size.width / (data.size - 1)
        val verticalPadding = 8f

        fun yFor(value: Int): Float {
            val normalized = (value - minValue).toFloat() / range.toFloat()
            return (size.height - verticalPadding) - (normalized * (size.height - 2 * verticalPadding))
        }

        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = yFor(value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
        data.forEachIndexed { index, value ->
            drawCircle(color = lineColor, radius = 6f, center = Offset(index * stepX, yFor(value)))
        }
    }
}

/** Donut chart with a legend - e.g. "Activity Distribution" by type. */
@Composable
fun SimpleDonutChart(
    slices: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(GuardianAmber, SignalCyan, ClearanceGreen, HazardCoral, TextMedium)
) {
    val total = slices.sumOf { it.second }.coerceAtLeast(1)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(110.dp)) {
            var startAngle = -90f
            val strokeW = size.minDimension * 0.24f
            slices.forEachIndexed { index, pair ->
                val sweep = 360f * pair.second.toFloat() / total.toFloat()
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep.coerceAtLeast(0.1f),
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            slices.forEachIndexed { index, pair ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Spacer(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colors[index % colors.size], CircleShape)
                    )
                    Text(
                        text = "${pair.first} (${pair.second})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHigh,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
