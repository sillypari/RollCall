package com.pesky.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                is SettingsEvent.BiometricNotAvailable -> {
                    snackbarHostState.showSnackbar("Biometric authentication not available")
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
        
        SettingsSection("Clipboard") {
            SettingsRow(
                icon = Icons.Filled.Timer,
                title = "Clipboard clear timeout",
                subtitle = "${uiState.clipboardClearTimeout} seconds"
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
    
    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { _, new ->
                viewModel.changeMasterPassword(new)
                showPasswordDialog = false
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection("Authentication") {
            SettingsRow(
                icon = Icons.Filled.Fingerprint,
                title = "Biometric unlock",
                subtitle = if (uiState.biometricAvailable) 
                    "Use fingerprint or face to unlock" else "Not available on this device",
                trailing = {
                    Switch(
                        checked = uiState.biometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) },
                        enabled = uiState.biometricAvailable,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = PeskyColors.AccentBlue
                        )
                    )
                }
            )
            
            SettingsRow(
                icon = Icons.Filled.Lock,
                title = "Auto-lock timeout",
                subtitle = "${uiState.autoLockTimeout / 60} minutes of inactivity"
            )
        }
        
        SettingsSection("Master Password") {
            SettingsClickableRow(
                icon = Icons.Filled.Password,
                title = "Change master password",
                subtitle = "Update your vault password",
                onClick = { showPasswordDialog = true }
            )
        }
        
        SettingsSection("Protection") {
            SettingsRow(
                icon = Icons.Filled.Security,
                title = "Require password for sensitive actions",
                subtitle = "Confirm password before viewing/copying",
                trailing = {
                    Switch(
                        checked = uiState.requirePasswordSensitive,
                        onCheckedChange = { viewModel.toggleRequirePasswordSensitive(it) },
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
private fun BackupSettingsTab(
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
        SettingsSection("Backup") {
            SettingsClickableRow(
                icon = Icons.Filled.Upload,
                title = "Create backup",
                subtitle = "Export database to file",
                onClick = { /* TODO: File picker for backup location */ }
            )
            
            SettingsClickableRow(
                icon = Icons.Filled.Download,
                title = "Restore backup",
                subtitle = "Import database from file",
                onClick = { /* TODO: File picker */ }
            )
        }
        
        SettingsSection("Auto Backup") {
            SettingsRow(
                icon = Icons.Filled.Schedule,
                title = "Automatic backups",
                subtitle = "Create backups periodically",
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
            
            if (uiState.autoBackupEnabled) {
                SettingsRow(
                    icon = Icons.Filled.Timer,
                    title = "Backup interval",
                    subtitle = uiState.autoBackupInterval
                )
            }
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
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = PeskyColors.AccentBlue
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Pesky",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PeskyColors.TextPrimary
                )
                
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeskyColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Secure Offline Password Manager",
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
