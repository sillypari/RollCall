package com.pesky.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pesky.app.data.models.PasswordEntry
import com.pesky.app.ui.components.*
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.viewmodels.EntryEvent
import com.pesky.app.viewmodels.EntryViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Screen showing detailed view of a password entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entryUuid: String,
    onNavigateBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: EntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Load entry
    LaunchedEffect(entryUuid) {
        viewModel.loadEntry(entryUuid)
    }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EntryEvent.PasswordCopied -> {
                    snackbarHostState.showSnackbar(message = "Password copied", duration = SnackbarDuration.Short)
                }
                is EntryEvent.UsernameCopied -> {
                    snackbarHostState.showSnackbar(message = "Username copied", duration = SnackbarDuration.Short)
                }
                is EntryEvent.EntryDeleted -> onNavigateBack()
                is EntryEvent.Error -> {
                    snackbarHostState.showSnackbar(message = event.message, duration = SnackbarDuration.Short)
                }
                else -> {}
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteEntry()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PeskyColors.Error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = PeskyColors.CardBackground,
            titleContentColor = PeskyColors.TextPrimary,
            textContentColor = PeskyColors.TextSecondary
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavorite) PeskyColors.Warning else PeskyColors.IconSecondary
                        )
                    }
                    IconButton(onClick = { onEdit(entryUuid) }) {
                        Icon(Icons.Filled.Edit, "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PeskyColors.BackgroundPrimary,
                    navigationIconContentColor = PeskyColors.TextPrimary,
                    titleContentColor = PeskyColors.TextPrimary,
                    actionIconContentColor = PeskyColors.TextPrimary
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Username
            if (uiState.userName.isNotEmpty()) {
                DetailSection(title = "Username") {
                    CopyableValueRow(
                        label = "",
                        value = uiState.userName,
                        onCopy = { viewModel.copyUsername() }
                    )
                }
            }
            
            // Password
            if (uiState.password.isNotEmpty()) {
                DetailSection(title = "Password") {
                    CopyableValueRow(
                        label = "",
                        value = uiState.password,
                        onCopy = { viewModel.copyPassword() },
                        isProtected = true,
                        showValue = uiState.isPasswordVisible
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    PasswordStrengthMeter(
                        analysisResult = uiState.passwordAnalysis,
                        showFeedback = false
                    )
                }
            }
            
            // Website
            if (uiState.url.isNotEmpty()) {
                DetailSection(title = "Website") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.url,
                            style = MaterialTheme.typography.bodyLarge,
                            color = PeskyColors.AccentBlue,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uiState.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle invalid URL
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = "Open URL",
                                tint = PeskyColors.AccentBlue
                            )
                        }
                    }
                }
            }
            
            // Notes
            if (uiState.notes.isNotEmpty()) {
                DetailSection(title = "Notes") {
                    Text(
                        text = uiState.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PeskyColors.TextPrimary
                    )
                }
            }
            
            // Custom fields
            if (uiState.customFields.isNotEmpty()) {
                DetailSection(title = "Custom Fields") {
                    uiState.customFields.forEach { field ->
                        CopyableValueRow(
                            label = field.key,
                            value = field.value,
                            onCopy = { /* TODO: Copy to clipboard */ },
                            isProtected = field.isProtected
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            
            // Tags
            if (uiState.tags.isNotEmpty()) {
                DetailSection(title = "Tags") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.tags.forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = { Text(tag) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = PeskyColors.TagBackground,
                                    labelColor = PeskyColors.TagText
                                )
                            )
                        }
                    }
                }
            }
            
            // Timestamps
            DetailSection(title = "Information") {
                uiState.entry?.let { entry ->
                    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
                    val zoneId = java.time.ZoneId.systemDefault()
                    
                    InfoRow("Created", entry.times.creationTime.atZone(zoneId).format(formatter))
                    InfoRow("Modified", entry.times.lastModificationTime.atZone(zoneId).format(formatter))
                    InfoRow("Last accessed", entry.times.lastAccessTime.atZone(zoneId).format(formatter))
                    
                    if (entry.times.expires && entry.times.expiryTime != null) {
                        InfoRow("Expires", entry.times.expiryTime.atZone(zoneId).format(formatter))
                    }
                }
            }
            
            // Password history
            if (uiState.entry?.history?.isNotEmpty() == true) {
                var showHistory by remember { mutableStateOf(false) }
                var revealedHistoryIndex by remember { mutableStateOf<Int?>(null) }
                val historyFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
                val historyZoneId = java.time.ZoneId.systemDefault()
                
                DetailSection(title = "Password History (${uiState.entry?.history?.size})") {
                    if (!showHistory) {
                        TextButton(
                            onClick = { showHistory = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tap to view previous passwords")
                        }
                    } else {
                        uiState.entry?.history?.forEachIndexed { index, historyEntry ->
                            val isRevealed = revealedHistoryIndex == index
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isRevealed) historyEntry.password else "••••••••",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PeskyColors.TextPrimary
                                    )
                                    Text(
                                        text = historyEntry.modificationTime.atZone(historyZoneId).format(historyFormatter),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PeskyColors.TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { 
                                        revealedHistoryIndex = if (isRevealed) null else index 
                                    }
                                ) {
                                    Icon(
                                        if (isRevealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (isRevealed) "Hide" else "Show",
                                        tint = PeskyColors.TextSecondary
                                    )
                                }
                            }
                            if (index < (uiState.entry?.history?.size ?: 0) - 1) {
                                HorizontalDivider(
                                    color = PeskyColors.TextSecondary.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { 
                                showHistory = false
                                revealedHistoryIndex = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Hide history")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Delete button
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PeskyColors.Error),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(PeskyColors.Error)
                )
            ) {
                Icon(Icons.Filled.Delete, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Entry")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PeskyColors.CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = PeskyColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = PeskyColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = PeskyColors.TextPrimary
        )
    }
}
