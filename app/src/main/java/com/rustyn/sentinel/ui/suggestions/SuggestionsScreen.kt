package com.rustyn.sentinel.ui.suggestions

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.rustyn.sentinel.data.database.entity.SuggestionEntity
import com.rustyn.sentinel.ui.components.EmptyState
import com.rustyn.sentinel.ui.theme.*

@Composable
fun SuggestionsScreen(
    viewModel: SuggestionsViewModel,
    modifier: Modifier = Modifier
) {
    val suggestionList by viewModel.suggestions.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Smart Insights",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextLight
        )
        Text(
            text = "On-device pattern engine recommendations",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Scan Hero Card ──
        val context = androidx.compose.ui.platform.LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                android.widget.Toast.makeText(context, "Scanning call logs...", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.scanCallLog()
            } else {
                android.widget.Toast.makeText(context, "Permission denied.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Animated radar pulse
        val pulseTransition = rememberInfiniteTransition(label = "radar")
        val pulseScale by pulseTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radarPulse"
        )
        val pulseAlpha by pulseTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radarAlpha"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = PrimarySky.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            onClick = {
                val permission = android.Manifest.permission.READ_CALL_LOG
                val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (isGranted) {
                    android.widget.Toast.makeText(context, "Scanning call logs...", android.widget.Toast.LENGTH_SHORT).show()
                    viewModel.scanCallLog()
                } else {
                    launcher.launch(permission)
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Radar icon with pulse
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulse ring
                        Box(
                            modifier = Modifier
                                .size((40 * pulseScale).dp)
                                .background(
                                    PrimarySky.copy(alpha = pulseAlpha * 0.3f),
                                    CircleShape
                                )
                        )
                        // Inner circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    PrimarySky.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = PrimarySky,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scan Call Logs",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Analyze native call history for recurring spam patterns",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = PrimarySky.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (suggestionList.isEmpty()) {
            EmptyState(
                title = "No New Suggestions",
                description = "Our offline algorithms need more blocking logs to detect recurring patterns. Keep using the app normally!"
            )
        } else {
            Text(
                text = "Detected Patterns",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(suggestionList, key = { it.id }) { suggestion ->
                    SuggestionItemRow(
                        suggestion = suggestion,
                        onAccept = { viewModel.acceptSuggestion(suggestion) },
                        onIgnore = { viewModel.ignoreSuggestion(suggestion.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionItemRow(
    suggestion: SuggestionEntity,
    onAccept: () -> Unit,
    onIgnore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            WarningAmber.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.suggestedPattern,
                        color = TextLight,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${suggestion.triggerCount} recent blocks matched",
                        color = PrimarySky,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Source list
            Text(
                text = "TRACE LOG",
                color = TextSubtle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            val numbers = suggestion.exampleNumbers.split(",")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBackground.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                numbers.forEach { number ->
                    Text(
                        text = number.trim(),
                        color = TextSubtle,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimarySky,
                        contentColor = DarkBackground
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Accept Rule", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onIgnore,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ignore", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
