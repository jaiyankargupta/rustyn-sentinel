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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustyn.sentinel.data.database.entity.RuleEntity
import com.rustyn.sentinel.ui.components.EmptyState
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
            ExtendedFloatingActionButton(
                onClick = { showActionSheet = true },
                containerColor = PrimarySky,
                contentColor = DarkBackground,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+  Add Rule", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Blocking Rules",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
            Text(
                text = "Manage call interception patterns",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Summary Header ──
            if (rulesList.isNotEmpty()) {
                val activeCount = rulesList.count { it.isActive }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", fontSize = 20.sp, color = SuccessGreen)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "$activeCount active rules protecting you",
                                color = TextLight,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${rulesList.size} total configured",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (rulesList.isEmpty()) {
                EmptyState(
                    title = "No Rules Setup",
                    description = "Create rules by tapping 'Add Rule' to start filtering spam calls."
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
                                        .padding(vertical = 2.dp)
                                        .background(BlockRed, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = alignment
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DarkBackground)
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
            containerColor = DarkSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Create New Rule",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Choose how to add a blocking rule",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                ActionItemRow(
                    icon = Icons.Default.List,
                    title = "From Call History",
                    subtitle = "Select a recent caller to block",
                    iconColor = PrimarySky,
                    onClick = {
                        showActionSheet = false
                        showCallHistoryDialog = true
                    }
                )
                ActionItemRow(
                    icon = Icons.Default.Person,
                    title = "From Contacts",
                    subtitle = "Select a saved contact to block",
                    iconColor = AccentIndigo,
                    onClick = {
                        showActionSheet = false
                        contactPickerLauncher.launch(null)
                    }
                )
                ActionItemRow(
                    icon = Icons.Default.Edit,
                    title = "Input Number",
                    subtitle = "Type an exact phone number manually",
                    iconColor = SuccessGreen,
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
                    subtitle = "Block numbers with a specific prefix",
                    iconColor = WarningAmber,
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
                    subtitle = "Block using wildcard pattern matching",
                    iconColor = BlockRed,
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
    iconColor: Color = PrimarySky,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    iconColor.copy(alpha = 0.10f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, color = TextLight, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
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
    val (badgeColor, badgeIcon) = when (rule.type.uppercase()) {
        "EXACT" -> AccentIndigo to Icons.Default.Person
        "PREFIX" -> PrimarySky to Icons.Default.ArrowForward
        else -> BlockRed to Icons.Default.Star
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PrimarySky.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge with gradient tint
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                badgeColor.copy(alpha = 0.15f),
                                badgeColor.copy(alpha = 0.05f)
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.pattern,
                    color = if (rule.isActive) TextLight else TextMuted,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.10f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = rule.type.uppercase(),
                            color = badgeColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    if (!rule.description.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rule.description,
                            color = TextSubtle,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (rule.startTime != null && rule.endTime != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimarySky,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${rule.startTime} – ${rule.endTime}",
                            color = PrimarySky,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            
            // Toggle Switch
            Switch(
                checked = rule.isActive,
                onCheckedChange = onToggle,
                modifier = Modifier.scale(0.8f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkBackground,
                    checkedTrackColor = PrimarySky,
                    uncheckedThumbColor = TextSubtle,
                    uncheckedTrackColor = DarkSurfaceVariant
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
            Text("Configure Rule", color = TextLight, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type selector
                Text("Rule Type", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf("EXACT" to AccentIndigo, "PREFIX" to PrimarySky, "WILDCARD" to BlockRed)
                    types.forEach { (type, color) ->
                        val isSelected = selectedType == type
                        OutlinedButton(
                            onClick = {
                                selectedType = type
                                errorText = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                                contentColor = if (isSelected) color else TextMuted
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp),
                            border = BorderStroke(1.dp, if (isSelected) color.copy(alpha = 0.5f) else BorderSubtle)
                        ) {
                            Text(type, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                // Pattern Input
                OutlinedTextField(
                    value = pattern,
                    onValueChange = {
                        pattern = it
                        errorText = ""
                    },
                    label = { Text("Pattern", color = TextMuted) },
                    placeholder = {
                        val placeholder = when (selectedType) {
                            "EXACT" -> "+918904889067"
                            "PREFIX" -> "140 or 890488"
                            else -> "89048****67"
                        }
                        Text(placeholder, color = TextSubtle)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                // Optional description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Annotation (Optional)", color = TextMuted) },
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

                // Time Constraints
                Text("Schedule (Optional)", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            android.app.TimePickerDialog(context, { _, hourOfDay, minute ->
                                startTime = String.format("%02d:%02d", hourOfDay, minute)
                            }, 22, 0, false).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderSlate),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(startTime ?: "Start", fontSize = 12.sp, color = TextLight)
                    }

                    OutlinedButton(
                        onClick = {
                            android.app.TimePickerDialog(context, { _, hourOfDay, minute ->
                                endTime = String.format("%02d:%02d", hourOfDay, minute)
                            }, 7, 0, false).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderSlate),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(endTime ?: "End", fontSize = 12.sp, color = TextLight)
                    }
                }
                
                if (startTime != null || endTime != null) {
                    TextButton(onClick = { startTime = null; endTime = null }) {
                        Text("Clear Schedule", fontSize = 12.sp, color = BlockRed)
                    }
                }

                AnimatedVisibility(visible = errorText.isNotEmpty()) {
                    Text(errorText, color = BlockRed, fontSize = 12.sp)
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
                    containerColor = PrimarySky,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp)
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
            title = { Text("Recent Calls", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                if (callLogs.isEmpty()) {
                    Text("No recent calls found.", color = TextMuted)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(callLogs) { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelect(log.number) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PrimarySky.copy(alpha = 0.10f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = PrimarySky,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    if (!log.name.isNullOrEmpty()) {
                                        Text(
                                            if (log.count > 1) "${log.name} (${log.count})" else log.name,
                                            color = TextLight,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(log.number, color = TextMuted, fontSize = 12.sp)
                                    } else {
                                        Text(
                                            if (log.count > 1) "${log.number} (${log.count})" else log.number,
                                            color = TextLight,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Text(log.dateStr, color = TextSubtle, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = PrimarySky) }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
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
