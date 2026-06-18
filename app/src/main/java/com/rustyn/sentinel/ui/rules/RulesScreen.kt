package com.rustyn.sentinel.ui.rules

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustyn.sentinel.data.database.entity.RuleEntity
import com.rustyn.sentinel.ui.components.EmptyState
import com.rustyn.sentinel.ui.components.GlassmorphicCard
import com.rustyn.sentinel.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: RulesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rulesList by viewModel.rules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showActionSheet by remember { mutableStateOf(false) }
    var showCallHistoryDialog by remember { mutableStateOf(false) }
    
    // Pre-fill states for AddDialog
    var prefillType by remember { mutableStateOf("EXACT") }
    var prefillPattern by remember { mutableStateOf("") }
    
    // Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val number = getPhoneNumberFromContactUri(context, uri)
            if (number != null) {
                prefillPattern = number
                prefillType = "EXACT"
                showAddDialog = true
            } else {
                Toast.makeText(context, "Could not find a phone number for that contact.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showActionSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Rule")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Blocking Rules",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Manage matches to intercept calls before they ring",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (rulesList.isEmpty()) {
                EmptyState(
                    title = "No Rules Setup",
                    description = "Create rules by clicking the '+' button to start filtering spam calls."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(rulesList, key = { it.id }) { rule ->
                        val dismissState = rememberDismissState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == DismissValue.DismissedToStart || dismissValue == DismissValue.DismissedToEnd) {
                                    viewModel.deleteRule(rule.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        
                        SwipeToDismiss(
                            state = dismissState,
                            background = {
                                val alignment = if (dismissState.dismissDirection == DismissDirection.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 4.dp)
                                        .background(MaterialTheme.colorScheme.error, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = alignment
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onError)
                                }
                            },
                            dismissContent = {
                                RuleItemRow(
                                    rule = rule,
                                    onToggle = { isActive -> viewModel.toggleRule(rule.id, isActive) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Create New Rule", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                
                ActionItemRow(
                    icon = Icons.Default.List,
                    title = "From Call History",
                    subtitle = "Select a recent caller to block",
                    onClick = {
                        showActionSheet = false
                        showCallHistoryDialog = true
                    }
                )
                ActionItemRow(
                    icon = Icons.Default.Person,
                    title = "From Contacts",
                    subtitle = "Select a saved contact to block",
                    onClick = {
                        showActionSheet = false
                        contactPickerLauncher.launch(null)
                    }
                )
                ActionItemRow(
                    icon = Icons.Default.Edit,
                    title = "Input Number",
                    subtitle = "Type an exact phone number manually",
                    onClick = {
                        prefillType = "EXACT"
                        prefillPattern = ""
                        showActionSheet = false
                        showAddDialog = true
                    }
                )
                ActionItemRow(
                    icon = Icons.Default.ArrowForward,
                    title = "Number Begins With",
                    subtitle = "Block numbers with a specific prefix (e.g. +91 140)",
                    onClick = {
                        prefillType = "PREFIX"
                        prefillPattern = ""
                        showActionSheet = false
                        showAddDialog = true
                    }
                )
                ActionItemRow(
                    icon = Icons.Default.MoreVert,
                    title = "Number Contains",
                    subtitle = "Block numbers containing specific digits",
                    onClick = {
                        prefillType = "WILDCARD"
                        prefillPattern = ""
                        showActionSheet = false
                        showAddDialog = true
                    }
                )
            }
        }
    }

    if (showCallHistoryDialog) {
        CallHistoryDialog(
            onDismiss = { showCallHistoryDialog = false },
            onSelect = { number ->
                prefillPattern = number
                prefillType = "EXACT"
                showCallHistoryDialog = false
                showAddDialog = true
            }
        )
    }

    if (showAddDialog) {
        AddRuleDialog(
            initialType = prefillType,
            initialPattern = prefillPattern,
            onDismiss = { showAddDialog = false },
            onConfirm = { pattern, type, desc, start, end ->
                viewModel.addRule(pattern, type, desc, start, end) {
                    Toast.makeText(context, "Rule already added", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ActionItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

private fun getPhoneNumberFromContactUri(context: Context, contactUri: Uri): String? {
    var phoneNumber: String? = null
    try {
        val cursor = context.contentResolver.query(contactUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                
                if (idIndex != -1 && hasPhoneIndex != -1) {
                    val contactId = it.getString(idIndex)
                    val hasPhone = it.getString(hasPhoneIndex)
                    
                    if (hasPhone.toInt() > 0) {
                        val phonesCursor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )
                        phonesCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                val numIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numIndex != -1) {
                                    phoneNumber = pc.getString(numIndex)
                                }
                            }
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return phoneNumber?.replace(Regex("[^0-9+*#]"), "") // clean up
}

@Composable
fun RuleItemRow(
    rule: RuleEntity,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            val (badgeColor, badgeIcon) = when (rule.type.uppercase()) {
                "EXACT" -> MaterialTheme.colorScheme.secondary to Icons.Default.Person
                "PREFIX" -> MaterialTheme.colorScheme.primary to Icons.Default.ArrowForward
                else -> MaterialTheme.colorScheme.error to Icons.Default.Star
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = badgeIcon, contentDescription = null, tint = badgeColor)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.pattern,
                    color = if (rule.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rule.type.uppercase(),
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    if (!rule.description.isNullOrEmpty()) {
                        Text(
                            text = " • ${rule.description}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (rule.startTime != null && rule.endTime != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Active: ${rule.startTime} - ${rule.endTime}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Actions
            Switch(
                checked = rule.isActive,
                onCheckedChange = onToggle,
                modifier = Modifier.scale(0.8f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun AddRuleDialog(
    initialType: String = "EXACT",
    initialPattern: String = "",
    onDismiss: () -> Unit,
    onConfirm: (pattern: String, type: String, description: String?, startTime: String?, endTime: String?) -> Unit
) {
    var pattern by remember { mutableStateOf(initialPattern) }
    var selectedType by remember { mutableStateOf(initialType) }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf<String?>(null) }
    var endTime by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Configure Rule", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type selector TabRow-like buttons
                Text("Rule Type", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf("EXACT", "PREFIX", "WILDCARD")
                    types.forEach { type ->
                        val isSelected = selectedType == type
                        OutlinedButton(
                            onClick = {
                                selectedType = type
                                errorText = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(type, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                // Input
                OutlinedTextField(
                    value = pattern,
                    onValueChange = {
                        pattern = it
                        errorText = ""
                    },
                    label = { Text("Pattern", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = {
                        val placeholder = when (selectedType) {
                            "EXACT" -> "+918904889067"
                            "PREFIX" -> "140 or 890488"
                            else -> "89048****67"
                        }
                        Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Annotation (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time Constraints
                Text("Time Constraints (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            android.app.TimePickerDialog(context, { _, hourOfDay, minute ->
                                startTime = String.format("%02d:%02d", hourOfDay, minute)
                            }, 22, 0, false).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(startTime ?: "Start Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    OutlinedButton(
                        onClick = {
                            android.app.TimePickerDialog(context, { _, hourOfDay, minute ->
                                endTime = String.format("%02d:%02d", hourOfDay, minute)
                            }, 7, 0, false).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(endTime ?: "End Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                
                if (startTime != null || endTime != null) {
                    TextButton(onClick = { startTime = null; endTime = null }) {
                        Text("Clear Schedule", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }

                AnimatedVisibility(visible = errorText.isNotEmpty()) {
                    Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanPattern = pattern.trim()
                    if (cleanPattern.isEmpty()) {
                        errorText = "Pattern cannot be empty"
                        return@Button
                    }
                    // Validation based on type
                    var finalPattern = cleanPattern
                    if (selectedType == "PREFIX" && !cleanPattern.endsWith("*")) {
                        finalPattern = "$cleanPattern*"
                    }
                    if (selectedType == "WILDCARD" && !cleanPattern.contains("*")) {
                        errorText = "Wildcard rule must contain asterisk (*)"
                        return@Button
                    }
                    if ((startTime != null && endTime == null) || (startTime == null && endTime != null)) {
                        errorText = "Both start and end time must be set"
                        return@Button
                    }
                    onConfirm(finalPattern, selectedType, description.ifBlank { null }, startTime, endTime)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

data class CallLogEntry(val number: String, val name: String?, val dateStr: String, var count: Int = 1)

@Composable
fun CallHistoryDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    var callLogs by remember { mutableStateOf<List<CallLogEntry>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            callLogs = fetchCallLogs(context)
        } else {
            onDismiss()
            Toast.makeText(context, "Call Log permission is required to view history.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
            callLogs = fetchCallLogs(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
    }

    if (hasPermission) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Recent Calls", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                if (callLogs.isEmpty()) {
                    Text("No recent calls found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(callLogs) { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(log.number) }
                                    .padding(vertical = 12.dp)
                            ) {
                                Column {
                                    if (!log.name.isNullOrEmpty()) {
                                        Text(if (log.count > 1) "${log.name} (${log.count})" else log.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                        Text(log.number, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    } else {
                                        Text(if (log.count > 1) "${log.number} (${log.count})" else log.number, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                    }
                                    Text(log.dateStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

private fun fetchCallLogs(context: Context): List<CallLogEntry> {
    val logs = mutableListOf<CallLogEntry>()
    try {
        val cursor = context.contentResolver.query(
            android.provider.CallLog.Calls.CONTENT_URI,
            arrayOf(
                android.provider.CallLog.Calls.NUMBER,
                android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.CACHED_NAME
            ),
            null,
            null,
            android.provider.CallLog.Calls.DATE + " DESC"
        )
        cursor?.use {
            val numIndex = it.getColumnIndex(android.provider.CallLog.Calls.NUMBER)
            val dateIndex = it.getColumnIndex(android.provider.CallLog.Calls.DATE)
            val nameIndex = it.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME)
            var count = 0
            while (it.moveToNext() && count < 20) {
                val number = it.getString(numIndex) ?: "Unknown"
                val name = if (nameIndex != -1) it.getString(nameIndex) else null
                val dateMillis = it.getLong(dateIndex)
                val dateStr = android.text.format.DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS).toString()
                
                if (logs.isNotEmpty() && logs.last().number == number) {
                    logs.last().count++
                } else {
                    logs.add(CallLogEntry(number, name, dateStr))
                    count++
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return logs
}
