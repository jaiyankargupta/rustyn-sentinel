package com.rustyn.sentinel.ui.suggestions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.rustyn.sentinel.data.database.entity.SuggestionEntity
import com.rustyn.sentinel.ui.components.EmptyState
import com.rustyn.sentinel.ui.components.GlassmorphicCard
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Smart Suggestions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "On-device pattern engine recommendations",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Button(
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
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Text("Scan Native Call Logs for Spam", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (suggestionList.isEmpty()) {
            EmptyState(
                title = "No New Suggestions",
                description = "Our offline algorithms need more blocking logs to detect recurring patterns. Keep using the app normally!"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
    GlassmorphicCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Block Prefix ${suggestion.suggestedPattern}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Generated from ${suggestion.triggerCount} recent blocks",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Source list
            Text(
                text = "Trace log instances:",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            val numbers = suggestion.exampleNumbers.split(",")
            numbers.forEach { number ->
                Text(
                    text = "• $number",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept Rule", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onIgnore,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ignore", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
