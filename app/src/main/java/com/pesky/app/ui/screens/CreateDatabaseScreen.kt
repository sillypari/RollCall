package com.pesky.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pesky.app.ui.components.PasswordStrengthMeter
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.utils.PasswordStrengthAnalyzer
import com.pesky.app.viewmodels.VaultEvent
import com.pesky.app.viewmodels.VaultViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Screen for creating a new database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDatabaseScreen(
    onDatabaseCreated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val passwordAnalyzer = remember { PasswordStrengthAnalyzer() }
    
    var databaseName by remember { mutableStateOf("My Passwords") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var masterPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    
    val passwordAnalysis = remember(masterPassword) {
        passwordAnalyzer.analyze(masterPassword)
    }
    
    val passwordsMatch = masterPassword == confirmPassword && masterPassword.isNotEmpty()
    val isFormValid = databaseName.isNotBlank() && 
                      selectedUri != null && 
                      masterPassword.length >= 8 && 
                      passwordsMatch
    
    // File picker for save location
    val context = androidx.compose.ui.platform.LocalContext.current
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { 
            // Take persistable permission for the URI
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Permission might already be granted or not needed
            }
            selectedUri = it 
        }
    }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is VaultEvent.DatabaseCreated -> onDatabaseCreated()
                else -> {}
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Database") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PeskyColors.BackgroundPrimary,
                    titleContentColor = PeskyColors.TextPrimary,
                    navigationIconContentColor = PeskyColors.TextPrimary
                )
            )
        },
        containerColor = PeskyColors.BackgroundPrimary,
        modifier = Modifier.imePadding() // Add keyboard padding
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Database name
            OutlinedTextField(
                value = databaseName,
                onValueChange = { databaseName = it },
                label = { Text("Database Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PeskyColors.AccentBlue,
                    unfocusedBorderColor = PeskyColors.Divider
                )
            )
            
            // Save location
            OutlinedCard(
                onClick = { 
                    val fileName = "${databaseName.replace(" ", "_").lowercase()}.pesky"
                    folderPicker.launch(fileName)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = PeskyColors.CardBackground
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(PeskyColors.Divider)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        tint = PeskyColors.AccentBlue
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Save Location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PeskyColors.TextPrimary
                        )
                        Text(
                            text = selectedUri?.lastPathSegment ?: "Tap to select location",
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Master password
            OutlinedTextField(
                value = masterPassword,
                onValueChange = { masterPassword = it },
                label = { Text("Master Password") },
                placeholder = { Text("Minimum 8 characters") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) 
                                Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide" else "Show"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PeskyColors.AccentBlue,
                    unfocusedBorderColor = PeskyColors.Divider
                ),
                isError = masterPassword.isNotEmpty() && masterPassword.length < 8
            )
            
            // Password strength meter
            if (masterPassword.isNotEmpty()) {
                PasswordStrengthMeter(
                    analysisResult = passwordAnalysis,
                    showFeedback = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Confirm password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isConfirmPasswordVisible) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) 
                                Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (isConfirmPasswordVisible) "Hide" else "Show"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PeskyColors.AccentBlue,
                    unfocusedBorderColor = PeskyColors.Divider
                ),
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    { Text("Passwords don't match", color = PeskyColors.Error) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Warning card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PeskyColors.Error.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = PeskyColors.Error,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Your master password cannot be recovered. Store it securely. If you forget it, you will lose access to all your data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PeskyColors.Error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Create button
            Button(
                onClick = {
                    if (isFormValid && selectedUri != null) {
                        viewModel.createDatabase(
                            selectedUri!!,
                            masterPassword.toCharArray(),
                            databaseName
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isFormValid && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PeskyColors.AccentBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PeskyColors.TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Database")
                }
            }
        }
    }
}
