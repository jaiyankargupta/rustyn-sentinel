package com.rustyn.sentinel.ui.settings
import androidx.compose.foundation.BorderStroke


import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.rustyn.sentinel.data.database.entity.AllowlistEntity
import com.rustyn.sentinel.ui.components.GlassmorphicCard
import com.rustyn.sentinel.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notifEnabledState by viewModel.notificationsEnabled.collectAsState()
    val strictModeEnabledState by viewModel.strictModeEnabled.collectAsState()
    val allowlistItems by viewModel.allowlist.collectAsState()

    val scrollState = rememberScrollState()

    // State for allowlist inputs
    var numberInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // ── Header ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .offset(x = (-8).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextLight,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
        }
        Text(
            text = "Preferences, allowlist, and data management",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Notifications Section ──
        SectionHeader(title = "Alerts & Notifications")
        Spacer(modifier = Modifier.height(10.dp))
        GlassmorphicCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Block Notifications", color = TextLight, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Show heads-up alert when a call is intercepted", color = TextMuted, fontSize = 12.sp, lineHeight = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = notifEnabledState == "true",
                    onCheckedChange = { viewModel.toggleNotifications(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkBackground,
                        checkedTrackColor = PrimarySky,
                        uncheckedThumbColor = TextSubtle,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Strict Mode Section ──
        SectionHeader(title = "Advanced Protection")
        Spacer(modifier = Modifier.height(10.dp))
        GlassmorphicCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Strict Mode", color = TextLight, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Block ALL calls from numbers not saved in your contacts", color = TextMuted, fontSize = 12.sp, lineHeight = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = strictModeEnabledState == "true",
                    onCheckedChange = { viewModel.toggleStrictMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkBackground,
                        checkedTrackColor = WarningAmber,
                        uncheckedThumbColor = TextSubtle,
                        uncheckedTrackColor = DarkSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Allowlist Section ──
        SectionHeader(title = "Caller Allowlist")
        Spacer(modifier = Modifier.height(10.dp))
        GlassmorphicCard {
            Text("Add Allowed Caller", color = TextLight, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = numberInput,
                onValueChange = { numberInput = it },
                label = { Text("Phone Number", color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    focusedBorderColor = PrimarySky.copy(alpha = 0.5f),
                    unfocusedBorderColor = BorderSlate,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Label (Optional)", color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    focusedBorderColor = PrimarySky.copy(alpha = 0.5f),
                    unfocusedBorderColor = BorderSlate,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (numberInput.isNotBlank()) {
                        viewModel.addAllowlist(numberInput, nameInput.ifBlank { null })
                        numberInput = ""
                        nameInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimarySky,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Allow Number", fontWeight = FontWeight.Bold)
            }

            if (allowlistItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Currently Allowlisted", color = TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allowlistItems.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkBackground.copy(alpha = 0.4f))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.pattern, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                if (!entry.name.isNullOrEmpty()) {
                                    Text(entry.name, color = TextSubtle, fontSize = 11.sp)
                                }
                            }
                            IconButton(
                                onClick = { viewModel.deleteAllowlist(entry) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = BlockRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Data & Storage ──
        SectionHeader(title = "Data & Storage")
        Spacer(modifier = Modifier.height(10.dp))
        GlassmorphicCard {
            val coroutineScope = rememberCoroutineScope()
            val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                if (uri != null) {
                    coroutineScope.launch {
                        val json = viewModel.exportRulesToJson()
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(json.toByteArray())
                        }
                        Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    coroutineScope.launch {
                        try {
                            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                            if (json != null) {
                                val result = viewModel.importRulesFromJson(json)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Imported ${result.getOrNull()} rules", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to import rules", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch(e: Exception) {
                            Toast.makeText(context, "Error parsing file", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { exportLauncher.launch("sentinel_rules_backup.json") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimarySky.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimarySky)
                ) {
                    Text("Export", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentIndigo.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentIndigo)
                ) {
                    Text("Import", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Privacy Section ──
        SectionHeader(title = "Privacy & Security")
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SuccessGreen.copy(alpha = 0.15f),
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
                                SuccessGreen.copy(alpha = 0.06f),
                                DarkSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            start = Offset.Zero,
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Pure On-Device Architecture",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rustyn Sentinel operates completely offline. No servers, cloud databases, analytics scripts, or tracker networks are integrated. Your data never leaves your device.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = TextLight,
        letterSpacing = 0.3.sp
    )
}
