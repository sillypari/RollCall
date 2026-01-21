package com.pesky.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pesky.app.ui.components.PasswordStrengthMeter
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.viewmodels.EntryEvent
import com.pesky.app.viewmodels.EntryViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Screen for adding or editing a password entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    entryUuid: String?, // null for new entry
    onNavigateBack: () -> Unit,
    onOpenGenerator: () -> Unit = {}, // Keep for compatibility, but we handle internally now
    viewModel: EntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showCustomFieldDialog by remember { mutableStateOf(false) }
    var showPasswordGenerator by remember { mutableStateOf(false) }
    
    val isEditing = entryUuid != null
    
    // Load entry if editing
    LaunchedEffect(entryUuid) {
        if (entryUuid != null) {
            viewModel.loadEntry(entryUuid)
        }
    }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EntryEvent.EntrySaved -> onNavigateBack()
                is EntryEvent.Error -> {
                    snackbarHostState.showSnackbar(message = event.message, duration = SnackbarDuration.Short)
                }
                else -> {}
            }
        }
    }
    
    // Password generator dialog
    if (showPasswordGenerator) {
        PasswordGeneratorDialog(
            onDismiss = { showPasswordGenerator = false },
            onPasswordSelected = { password ->
                viewModel.updatePassword(password)
                showPasswordGenerator = false
            }
        )
    }
    
    // Custom field dialog
    if (showCustomFieldDialog) {
        AddCustomFieldDialog(
            onDismiss = { showCustomFieldDialog = false },
            onAdd = { key, value, isProtected ->
                viewModel.addCustomField(key, value, isProtected)
                showCustomFieldDialog = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Entry" else "Add Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.Close, "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveEntry() },
                        enabled = uiState.title.isNotBlank() && !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save", color = PeskyColors.AccentBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PeskyColors.BackgroundPrimary,
                    navigationIconContentColor = PeskyColors.TextPrimary,
                    titleContentColor = PeskyColors.TextPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = PeskyColors.CardBackground,
                    contentColor = PeskyColors.TextPrimary
                )
            }
        },
        containerColor = PeskyColors.BackgroundPrimary
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Title *") },
                placeholder = { Text("e.g., Google Account") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.titleError != null,
                colors = textFieldColors()
            )
            
            // Username
            OutlinedTextField(
                value = uiState.userName,
                onValueChange = { viewModel.updateUserName(it) },
                label = { Text("Username") },
                placeholder = { Text("e.g., john@example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Person, null, tint = PeskyColors.IconSecondary)
                },
                colors = textFieldColors()
            )
            
            // Password
            var isPasswordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Filled.Lock, null, tint = PeskyColors.IconSecondary)
                },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                "Toggle visibility"
                            )
                        }
                        IconButton(onClick = { showPasswordGenerator = true }) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                "Generate password",
                                tint = PeskyColors.AccentBlue
                            )
                        }
                    }
                },
                colors = textFieldColors()
            )
            
            // Password strength meter
            if (uiState.password.isNotEmpty()) {
                PasswordStrengthMeter(
                    analysisResult = uiState.passwordAnalysis,
                    showFeedback = true
                )
            }
            
            // Website URL
            OutlinedTextField(
                value = uiState.url,
                onValueChange = { viewModel.updateUrl(it) },
                label = { Text("Website URL") },
                placeholder = { Text("e.g., https://google.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Language, null, tint = PeskyColors.IconSecondary)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                colors = textFieldColors()
            )
            
            // Notes
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes") },
                placeholder = { Text("Additional information...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                maxLines = 5,
                leadingIcon = {
                    Icon(Icons.Filled.Notes, null, tint = PeskyColors.IconSecondary)
                },
                colors = textFieldColors()
            )
            
            // Advanced Options (collapsed by default)
            var advancedExpanded by remember { mutableStateOf(false) }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PeskyColors.CardBackground)
            ) {
                Column {
                    // Header - clickable to expand/collapse
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { advancedExpanded = !advancedExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = PeskyColors.IconSecondary
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Advanced Options",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PeskyColors.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = if (advancedExpanded) "Collapse" else "Expand",
                            tint = PeskyColors.IconSecondary,
                            modifier = Modifier.rotate(if (advancedExpanded) 180f else 0f)
                        )
                    }
                    
                    // Expandable content
                    AnimatedVisibility(
                        visible = advancedExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Divider(color = PeskyColors.Divider)
                            
                            // Favorite toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                                    contentDescription = null,
                                    tint = if (uiState.isFavorite) PeskyColors.Warning else PeskyColors.IconSecondary
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = "Mark as favorite",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PeskyColors.TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Switch(
                                    checked = uiState.isFavorite,
                                    onCheckedChange = { viewModel.toggleFavorite() },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = PeskyColors.AccentBlue
                                    )
                                )
                            }
                            
                            Divider(color = PeskyColors.Divider)
                            
                            // Group selector
                            val availableGroups = remember { viewModel.getAvailableGroups() }
                            var groupExpanded by remember { mutableStateOf(false) }
                            val selectedGroupName = availableGroups.find { it.first == uiState.selectedGroupUuid }?.second ?: "No Category"
                            
                            ExposedDropdownMenuBox(
                                expanded = groupExpanded,
                                onExpandedChange = { groupExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedGroupName,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Group") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Folder, null, tint = PeskyColors.IconSecondary)
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = textFieldColors()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = groupExpanded,
                                    onDismissRequest = { groupExpanded = false }
                                ) {
                                    availableGroups.forEach { (uuid, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                viewModel.updateSelectedGroup(uuid)
                                                groupExpanded = false
                                            },
                                            leadingIcon = {
                                                if (uuid == null) {
                                                    Icon(Icons.Filled.FolderOff, null, modifier = Modifier.size(20.dp))
                                                } else {
                                                    Icon(Icons.Filled.Folder, null, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Divider(color = PeskyColors.Divider)
                            
                            // Tags
                            OutlinedTextField(
                                value = uiState.tags.joinToString(", "),
                                onValueChange = { 
                                    viewModel.updateTags(it.split(",").map { tag -> tag.trim() }.filter { tag -> tag.isNotEmpty() })
                                },
                                label = { Text("Tags") },
                                placeholder = { Text("Separate with commas") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Filled.Tag, null, tint = PeskyColors.IconSecondary)
                                },
                                colors = textFieldColors()
                            )
                            
                            // Custom fields section
                            if (uiState.customFields.isNotEmpty()) {
                                Text(
                                    text = "Custom Fields",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PeskyColors.TextSecondary
                                )
                                
                                uiState.customFields.forEachIndexed { index, field ->
                                    CustomFieldRow(
                                        label = field.key,
                                        value = field.value,
                                        isProtected = field.isProtected,
                                        onRemove = { viewModel.removeCustomField(index) }
                                    )
                                    if (index < uiState.customFields.lastIndex) {
                                        Divider(color = PeskyColors.Divider)
                                    }
                                }
                            }
                            
                            // Add custom field button
                            OutlinedButton(
                                onClick = { showCustomFieldDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = PeskyColors.AccentBlue
                                )
                            ) {
                                Icon(Icons.Filled.Add, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Custom Field")
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PeskyColors.AccentBlue,
    unfocusedBorderColor = PeskyColors.Divider,
    focusedLabelColor = PeskyColors.AccentBlue
)

@Composable
private fun CustomFieldRow(
    label: String,
    value: String,
    isProtected: Boolean,
    onRemove: () -> Unit
) {
    var showValue by remember { mutableStateOf(!isProtected) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PeskyColors.TextSecondary
            )
            Text(
                text = if (showValue) value else "••••••••",
                style = MaterialTheme.typography.bodyMedium,
                color = PeskyColors.TextPrimary
            )
        }
        
        if (isProtected) {
            IconButton(onClick = { showValue = !showValue }) {
                Icon(
                    if (showValue) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    "Toggle visibility",
                    tint = PeskyColors.IconSecondary
                )
            }
        }
        
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Close, "Remove", tint = PeskyColors.IconSecondary)
        }
    }
}

@Composable
private fun AddCustomFieldDialog(
    onDismiss: () -> Unit,
    onAdd: (key: String, value: String, isProtected: Boolean) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var isProtected by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Field") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Field Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isProtected,
                        onCheckedChange = { isProtected = it }
                    )
                    Text("Protect value (treat as password)")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(key, value, isProtected) },
                enabled = key.isNotEmpty() && value.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = PeskyColors.CardBackground,
        titleContentColor = PeskyColors.TextPrimary,
        textContentColor = PeskyColors.TextSecondary
    )
}
