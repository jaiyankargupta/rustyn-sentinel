package com.rustyn.sentinel.ui.history

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustyn.sentinel.data.database.entity.BlockedCallEntity
import com.rustyn.sentinel.ui.components.EmptyState
import com.rustyn.sentinel.ui.theme.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    highlightedCallId: Int? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Call History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
                Text(
                    text = "Blocked and intercepted calls",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
            if (state.blockedCalls.isNotEmpty()) {
                TextButton(
                    onClick = { showClearConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = BlockRed)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Summary Stats Row ──
        if (state.blockedCalls.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryChip(
                    label = "${state.blockedCalls.size}",
                    subtitle = "Total Logs",
                    color = PrimarySky,
                    modifier = Modifier.weight(1f)
                )
                SummaryChip(
                    label = "${state.blockedCalls.count {
                        DateUtils.isToday(it.timestamp)
                    }}",
                    subtitle = "Today",
                    color = AccentIndigo,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Search Bar ──
        OutlinedTextField(
            value = state.query,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search number or rule...", color = TextSubtle) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight,
                focusedBorderColor = PrimarySky.copy(alpha = 0.5f),
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Filter Chips ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL" to "All Logs", "RULE" to "By Rule")
            filters.forEach { (filterVal, label) ->
                val isSelected = state.filterType == filterVal
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onFilterTypeChanged(filterVal) },
                    label = {
                        Text(
                            label,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextMuted,
                        selectedContainerColor = PrimarySky,
                        selectedLabelColor = DarkBackground
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = BorderSubtle,
                        selectedBorderColor = PrimarySky,
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.blockedCalls.isEmpty()) {
            EmptyState(
                title = "No Block History",
                description = if (state.query.isNotEmpty()) "No logs match your search criteria." else "Intercepted calls will be logged here."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.blockedCalls, key = { it.id }) { call ->
                    val isHighlighted = call.id == highlightedCallId

                    @OptIn(ExperimentalMaterial3Api::class)
                    val dismissState = rememberDismissState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == DismissValue.DismissedToStart || dismissValue == DismissValue.DismissedToEnd) {
                                viewModel.deleteCall(call)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    @OptIn(ExperimentalMaterial3Api::class)
                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            val alignment = if (dismissState.dismissDirection == DismissDirection.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 2.dp)
                                    .background(BlockRed, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DarkBackground)
                            }
                        },
                        dismissContent = {
                            HistoryItemRow(
                                call = call,
                                isHighlighted = isHighlighted
                            )
                        }
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear History?", color = TextLight, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently clear all logs of blocked calls. This action is irreversible.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlockRed, contentColor = DarkBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SummaryChip(
    label: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun HistoryItemRow(
    call: BlockedCallEntity,
    isHighlighted: Boolean
) {
    val highlightBorder = if (isHighlighted) {
        Modifier.border(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(PrimarySky, AccentIndigo)
            ),
            shape = RoundedCornerShape(20.dp)
        )
    } else {
        Modifier.border(1.dp, PrimarySky.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(highlightBorder),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Icon Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                BlockRed.copy(alpha = 0.15f),
                                BlockRed.copy(alpha = 0.05f)
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = BlockRed,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = call.number,
                        color = TextLight,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                BlockRed.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "BLOCKED",
                            color = BlockRed,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val reason = when {
                    call.matchedRulePattern == null -> "Spam run detected locally"
                    call.matchedRulePattern.contains("*") -> "Pattern: ${call.matchedRulePattern}"
                    else -> "Exact: ${call.matchedRulePattern}"
                }
                Text(
                    text = reason,
                    color = TextSubtle,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        call.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString(),
                    color = TextSubtle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }
        }
    }
}
