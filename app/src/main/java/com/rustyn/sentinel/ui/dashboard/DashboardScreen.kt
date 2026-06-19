package com.rustyn.sentinel.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustyn.sentinel.ui.components.GlassmorphicCard
import com.rustyn.sentinel.ui.components.InteractiveAnalyticsChart
import com.rustyn.sentinel.ui.components.StandardStatsCard
import com.rustyn.sentinel.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSuggestions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // ── App Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sentinel",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Text(
                        text = "Offline call protection active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .background(DarkSurfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Shield Status Hero Card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F3642), // Dark cyan/teal
                                    Color(0xFF142036), // Deep blue
                                    Color(0xFF101726)  // Very dark
                                ),
                                start = Offset.Zero,
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animated shield
                        val pulseTransition = rememberInfiniteTransition(label = "heroShield")
                        val pulseAlpha by pulseTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "shieldGlow"
                        )

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            PrimarySky.copy(alpha = 0.25f * pulseAlpha),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Protection Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.activeRulesCount} rules · ${state.totalBlocked} calls blocked",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimarySky,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Stats Grid 2×2 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StandardStatsCard(
                    title = "Today",
                    value = "${state.blockedToday}",
                    subtitle = "Screened",
                    accentColor = PrimarySky,
                    modifier = Modifier.weight(1f)
                )
                StandardStatsCard(
                    title = "This Week",
                    value = "${state.blockedThisWeek}",
                    subtitle = "Screened",
                    accentColor = AccentIndigo,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StandardStatsCard(
                    title = "Total",
                    value = "${state.totalBlocked}",
                    subtitle = "Blocked",
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StandardStatsCard(
                    title = "Rules",
                    value = "${state.activeRulesCount}",
                    subtitle = "Active",
                    accentColor = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Analytics Chart ──
            Text(
                text = "Weekly Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassmorphicCard {
                InteractiveAnalyticsChart(
                    dataPoints = state.weeklyStats,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Intelligence Alerts ──
            if (state.suggestions.isNotEmpty()) {
                Text(
                    text = "Intelligence Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    WarningAmber.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        WarningAmber.copy(alpha = 0.08f),
                                        DarkSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(WarningAmber.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚡", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${state.suggestions.size} new pattern${if (state.suggestions.size > 1) "s" else ""} detected",
                                    color = TextLight,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Review suggestions from the pattern engine",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = onNavigateToSuggestions,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = WarningAmber.copy(alpha = 0.15f),
                                    contentColor = WarningAmber
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Review", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
