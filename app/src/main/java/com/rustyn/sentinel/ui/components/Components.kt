package com.rustyn.sentinel.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustyn.sentinel.ui.theme.*

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun StandardStatsCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Animated count
            val targetVal = value.toIntOrNull()
            if (targetVal != null) {
                val animatedVal by animateIntAsState(
                    targetValue = targetVal,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    label = "countUp"
                )
                Text(
                    text = "$animatedVal",
                    color = TextLight,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = value,
                    color = TextLight,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = accentColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    // Subtle pulse animation on the shield
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldPulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🛡️",
                fontSize = (48 * scale).sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                color = TextLight,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }
    }
}

@Composable
fun InteractiveAnalyticsChart(
    dataPoints: List<Int>,
    modifier: Modifier = Modifier
) {
    val cleanPoints = if (dataPoints.isEmpty()) listOf(0, 0, 0, 0, 0, 0, 0) else dataPoints
    val maxVal = cleanPoints.maxOrNull()?.coerceAtLeast(1) ?: 1

    // Animate the chart drawing
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
    }

    val primaryColor = PrimarySky
    val glowColor = GlowCyan

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color.Transparent)
                .padding(top = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height - 24.dp.toPx()
                val topPad = 12.dp.toPx()
                val spacing = width / (cleanPoints.size - 1)

                val points = cleanPoints.mapIndexed { index, valInt ->
                    val x = index * spacing
                    val rawY = valInt.toFloat() / maxVal.toFloat()
                    val animatedY = rawY * animProgress.value
                    val y = topPad + height - (animatedY * height)
                    Offset(x, y)
                }

                // Draw grid lines
                for (i in 0..3) {
                    val y = topPad + (height * i / 3f)
                    drawLine(
                        color = BorderSubtle,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                if (points.size >= 2) {
                    // Build smooth Bézier path
                    val linePath = Path()
                    val fillPath = Path()

                    linePath.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, points.first().y)

                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cp1x = prev.x + (curr.x - prev.x) * 0.4f
                        val cp2x = prev.x + (curr.x - prev.x) * 0.6f
                        linePath.cubicTo(cp1x, prev.y, cp2x, curr.y, curr.x, curr.y)
                        fillPath.cubicTo(cp1x, prev.y, cp2x, curr.y, curr.x, curr.y)
                    }

                    // Close fill path
                    fillPath.lineTo(width, topPad + height)
                    fillPath.lineTo(0f, topPad + height)
                    fillPath.close()

                    // Draw gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.25f * animProgress.value),
                                primaryColor.copy(alpha = 0.05f * animProgress.value),
                                Color.Transparent
                            )
                        )
                    )

                    // Glow effect line
                    drawPath(
                        path = linePath,
                        color = primaryColor.copy(alpha = 0.15f),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Main line
                    drawPath(
                        path = linePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(GradientCyanStart, GradientCyanEnd)
                        ),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Data points
                    points.forEach { point ->
                        drawCircle(
                            color = DarkSurface,
                            radius = 5.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(primaryColor, GradientCyanEnd),
                                center = point,
                                radius = 4.dp.toPx()
                            ),
                            radius = 3.5.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }

        // Day labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayLabels.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSubtle,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
