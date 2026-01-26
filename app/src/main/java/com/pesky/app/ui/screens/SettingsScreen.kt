package com.pesky.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pesky.app.R
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.viewmodels.SettingsEvent
import com.pesky.app.viewmodels.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Settings screen with tabbed navigation.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState { 4 }
    val scope = rememberCoroutineScope()
    
    val tabs = listOf("General", "Security", "Backup", "About")
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.PasswordChanged -> {
                    snackbarHostState.showSnackbar("Password changed successfully")
                }
                is SettingsEvent.BackupCreated -> {
                    snackbarHostState.showSnackbar("Backup created successfully")
                }
                is SettingsEvent.BackupRestored -> {
                    snackbarHostState.showSnackbar("Backup restored! Please restart the app to unlock with the backup's password.")
                }
                is SettingsEvent.BiometricNotAvailable -> {
                    snackbarHostState.showSnackbar("Biometric authentication not available")
                }
                is SettingsEvent.PinChanged -> {
                    snackbarHostState.showSnackbar("Quick unlock PIN updated")
                }
                is SettingsEvent.PinRemoved -> {
                    snackbarHostState.showSnackbar("Quick unlock PIN removed")
                }
                is SettingsEvent.DataCleared -> {
                    snackbarHostState.showSnackbar("All data cleared")
                }
                is SettingsEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
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
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = PeskyColors.BackgroundPrimary,
                contentColor = PeskyColors.AccentBlue
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) },
                        selectedContentColor = PeskyColors.AccentBlue,
                        unselectedContentColor = PeskyColors.TextSecondary
                    )
                }
            }
            
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> GeneralSettingsTab(viewModel, uiState)
                    1 -> SecuritySettingsTab(viewModel, uiState)
                    2 -> BackupSettingsTab(viewModel, uiState)
                    3 -> AboutTab()
                }
            }
        }
    }
}

@Composable
private fun GeneralSettingsTab(
    viewModel: SettingsViewModel,
    uiState: com.pesky.app.viewmodels.SettingsUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection("Appearance") {
            // Dark mode is always on for Apple Maps style
            SettingsRow(
                icon = Icons.Filled.DarkMode,
                title = "Dark Mode",
                subtitle = "Always enabled",
                trailing = {
                    Switch(
                        checked = true,
                        onCheckedChange = { },
                        enabled = false,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = PeskyColors.AccentBlue
                        )
                    )
                }
            )
        }
        
        SettingsSection("Feedback") {
            SettingsRow(
                icon = Icons.Filled.Vibration,
                title = "Haptic Feedback",
                subtitle = if (uiState.hapticFeedbackEnabled) 
                    "iPhone-style vibrations enabled" 
                else 
                    "Haptic feedback disabled",
                trailing = {
                    Switch(
                        checked = uiState.hapticFeedbackEnabled,
                        onCheckedChange = { viewModel.toggleHapticFeedback(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = PeskyColors.AccentBlue
                        )
                    )
                }
            )
        }
        
        SettingsSection("Clipboard") {
            var showClipboardDialog by remember { mutableStateOf(false) }
            
            if (showClipboardDialog) {
                TimeoutSelectorDialog(
                    title = "Clipboard Clear Timeout",
                    currentValue = uiState.clipboardClearTimeout,
                    options = listOf(15, 30, 60, 120),
                    optionLabels = listOf("15 seconds", "30 seconds", "1 minute", "2 minutes"),
                    onSelect = { viewModel.updateClipboardClearTimeout(it) },
                    onDismiss = { showClipboardDialog = false }
                )
            }
            
            SettingsClickableRow(
                icon = Icons.Filled.Timer,
                title = "Clipboard clear timeout",
                subtitle = "${uiState.clipboardClearTimeout} seconds",
                onClick = { showClipboardDialog = true }
            )
        }
        
        SettingsSection("Database") {
            SettingsRow(
                icon = Icons.Filled.Storage,
                title = "Remember database",
                subtitle = "Remember last opened database",
                trailing = {
                    Switch(
                        checked = uiState.rememberDatabase,
                        onCheckedChange = { viewModel.toggleRememberDatabase(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = PeskyColors.AccentBlue
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun SecuritySettingsTab(
    viewModel: SettingsViewModel,
    uiState: com.pesky.app.viewmodels.SettingsUiState
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showRemovePinDialog by remember { mutableStateOf(false) }
    val isPinEnabled = remember { mutableStateOf(viewModel.isQuickUnlockEnabled()) }
    
    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { _, new ->
                viewModel.changeMasterPassword(new)
                showPasswordDialog = false
            }
        )
    }
    
    if (showPinSetupDialog) {
        PinSetupDialog(
            onDismiss = { showPinSetupDialog = false },
            onConfirm = { pin ->
                viewModel.setupQuickUnlockPin(pin)
                isPinEnabled.value = true
                showPinSetupDialog = false
            }
        )
    }
    
    if (showRemovePinDialog) {
        AlertDialog(
            onDismissRequest = { showRemovePinDialog = false },
            title = { Text("Remove Quick Unlock?") },
            text = { Text("You will need to enter your master password every time you open the app.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeQuickUnlock()
                    isPinEnabled.value = false
                    showRemovePinDialog = false
                }) {
                    Text("Remove", color = PeskyColors.AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemovePinDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = PeskyColors.CardBackground,
            titleContentColor = PeskyColors.TextPrimary,
            textContentColor = PeskyColors.TextSecondary
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection("Quick Unlock") {
            SettingsRow(
                icon = Icons.Filled.Pin,
                title = "PIN Unlock",
                subtitle = if (isPinEnabled.value) "Enabled - Tap to change or remove" else "Set up a PIN for quick access",
                trailing = {
                    Switch(
                        checked = isPinEnabled.value,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showPinSetupDialog = true
                            } else {
                                showRemovePinDialog = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = PeskyColors.AccentBlue
                        )
                    )
                }
            )
        }
        
        // Auto-lock is not implemented for this version - removed to avoid confusion
        
        SettingsSection("Master Password") {
            SettingsClickableRow(
                icon = Icons.Filled.Password,
                title = "Change master password",
                subtitle = "Update your vault password",
                onClick = { showPasswordDialog = true }
            )
        }
        
        SettingsSection("Privacy") {
            SettingsRow(
                icon = Icons.Filled.ScreenshotMonitor,
                title = "Block screenshots",
                subtitle = if (uiState.screenshotProtectionEnabled) 
                    "Screenshots are blocked (requires app restart)" 
                else 
                    "Screenshots are allowed (requires app restart)",
                trailing = {
                    Switch(
                        checked = uiState.screenshotProtectionEnabled,
                        onCheckedChange = { viewModel.toggleScreenshotProtection(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = PeskyColors.AccentBlue
                        )
                    )
                }
            )
        }
        
        // Require password for sensitive actions - not implemented in this version
    }
}

@Composable
private fun BackupSettingsTab(
    viewModel: SettingsViewModel,
    uiState: com.pesky.app.viewmodels.SettingsUiState
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    // File picker for creating backup (export)
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { 
            viewModel.exportDatabase(it)
        }
    }
    
    // File picker for restoring backup (import)
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { 
            pendingRestoreUri = it
            showRestoreConfirmDialog = true
        }
    }
    
    // Restore confirmation dialog
    if (showRestoreConfirmDialog && pendingRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { 
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
            },
            title = { Text("Restore Backup?") },
            text = { 
                Text("This will replace your current database with the backup. You'll need to enter the backup's master password. Continue?") 
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestoreUri?.let { uri ->
                        viewModel.importDatabase(uri)
                    }
                    showRestoreConfirmDialog = false
                    pendingRestoreUri = null
                }) {
                    Text("Restore", color = PeskyColors.AccentBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRestoreConfirmDialog = false
                    pendingRestoreUri = null
                }) {
                    Text("Cancel")
                }
            },
            containerColor = PeskyColors.CardBackground,
            titleContentColor = PeskyColors.TextPrimary,
            textContentColor = PeskyColors.TextSecondary
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PeskyColors.CardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = PeskyColors.AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Backups are encrypted with your master password. Keep your master password safe!",
                    style = MaterialTheme.typography.bodySmall,
                    color = PeskyColors.TextSecondary
                )
            }
        }
        
        SettingsSection("Manual Backup") {
            SettingsClickableRow(
                icon = Icons.Filled.Upload,
                title = "Create backup",
                subtitle = "Export database to a .pesky file",
                onClick = { 
                    val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                        .format(java.util.Date())
                    createBackupLauncher.launch("pesky_backup_$timestamp.pesky")
                }
            )
            
            SettingsClickableRow(
                icon = Icons.Filled.Download,
                title = "Restore backup",
                subtitle = "Import database from a .pesky file",
                onClick = { 
                    restoreBackupLauncher.launch(arrayOf("*/*"))
                }
            )
        }
        
        SettingsSection("Auto Backup") {
            SettingsRow(
                icon = Icons.Filled.Sync,
                title = "Backup on changes",
                subtitle = "Create backup when entries are added/edited/deleted",
                trailing = {
                    Switch(
                        checked = uiState.autoBackupEnabled,
                        onCheckedChange = { viewModel.toggleAutoBackup(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = PeskyColors.AccentBlue
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun AboutTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PeskyColors.CardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pesky_logo),
                    contentDescription = "Pesky Logo",
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Pesky",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PeskyColors.TextPrimary
                )
                
                Text(
                    text = "Version 1.2.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeskyColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Parikshit Singh Bais",
                    style = MaterialTheme.typography.bodySmall,
                    color = PeskyColors.TextTertiary
                )
            }
        }
        
        SettingsSection("Security") {
            InfoRow("Encryption", "AES-256-CBC")
            InfoRow("Key Derivation", "Argon2id")
            InfoRow("HMAC", "SHA-256")
        }
        
        SettingsSection("Legal") {
            SettingsClickableRow(
                icon = Icons.Filled.Description,
                title = "Privacy Policy",
                subtitle = "How we handle your data",
                onClick = { }
            )
            
            SettingsClickableRow(
                icon = Icons.Filled.Gavel,
                title = "Terms of Service",
                subtitle = "Terms and conditions",
                onClick = { }
            )
            
            SettingsClickableRow(
                icon = Icons.Filled.Code,
                title = "Open Source Licenses",
                subtitle = "Third-party libraries",
                onClick = { }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = PeskyColors.AccentBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PeskyColors.CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PeskyColors.IconSecondary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = PeskyColors.TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PeskyColors.TextSecondary
            )
        }
        
        trailing?.invoke()
    }
}

@Composable
private fun SettingsClickableRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = PeskyColors.CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PeskyColors.IconSecondary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeskyColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = PeskyColors.TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = PeskyColors.IconSecondary
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PeskyColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = PeskyColors.TextPrimary
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (currentPassword: CharArray, newPassword: CharArray) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    
    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotEmpty()
    val isValid = currentPassword.isNotEmpty() && newPassword.length >= 8 && passwordsMatch
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Master Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showCurrentPassword) 
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                if (showCurrentPassword) Icons.Filled.VisibilityOff 
                                else Icons.Filled.Visibility,
                                "Toggle"
                            )
                        }
                    }
                )
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showNewPassword) 
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                if (showNewPassword) Icons.Filled.VisibilityOff 
                                else Icons.Filled.Visibility,
                                "Toggle"
                            )
                        }
                    },
                    isError = newPassword.isNotEmpty() && newPassword.length < 8,
                    supportingText = if (newPassword.isNotEmpty() && newPassword.length < 8) {
                        { Text("Minimum 8 characters") }
                    } else null
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                    supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        { Text("Passwords don't match") }
                    } else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onConfirm(currentPassword.toCharArray(), newPassword.toCharArray()) 
                },
                enabled = isValid
            ) {
                Text("Change")
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

/**
 * Dialog for setting up a quick unlock PIN.
 */
@Composable
private fun PinSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (pin: String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isConfirming) "Confirm PIN" else "Set Quick Unlock PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isConfirming) 
                        "Enter your PIN again to confirm" 
                    else 
                        "Enter a 4-6 digit PIN for quick access",
                    color = PeskyColors.TextSecondary
                )
                
                OutlinedTextField(
                    value = if (isConfirming) confirmPin else pin,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() } && value.length <= 6) {
                            if (isConfirming) {
                                confirmPin = value
                                error = null
                            } else {
                                pin = value
                            }
                        }
                    },
                    label = { Text(if (isConfirming) "Confirm PIN" else "PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = PeskyColors.AccentRed) } }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isConfirming) {
                        if (pin.length >= 4) {
                            isConfirming = true
                        } else {
                            error = "PIN must be at least 4 digits"
                        }
                    } else {
                        if (confirmPin == pin) {
                            onConfirm(pin)
                        } else {
                            error = "PINs don't match"
                            confirmPin = ""
                        }
                    }
                },
                enabled = if (isConfirming) confirmPin.length >= 4 else pin.length >= 4
            ) {
                Text(if (isConfirming) "Confirm" else "Next")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isConfirming) {
                    isConfirming = false
                    confirmPin = ""
                    error = null
                } else {
                    onDismiss()
                }
            }) {
                Text(if (isConfirming) "Back" else "Cancel")
            }
        },
        containerColor = PeskyColors.CardBackground,
        titleContentColor = PeskyColors.TextPrimary,
        textContentColor = PeskyColors.TextSecondary
    )
}

@Composable
private fun TimeoutSelectorDialog(
    title: String,
    currentValue: Int,
    options: List<Int>,
    optionLabels: List<String>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEachIndexed { index, value ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onSelect(value)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentValue == value,
                            onClick = { 
                                onSelect(value)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PeskyColors.AccentBlue
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optionLabels[index],
                            style = MaterialTheme.typography.bodyMedium,
                            color = PeskyColors.TextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {},
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
