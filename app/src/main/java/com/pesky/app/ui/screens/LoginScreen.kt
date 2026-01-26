package com.pesky.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pesky.app.R
import com.pesky.app.ui.components.LocalPeskyHaptics
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.ui.animations.PeskyAnimations
import com.pesky.app.viewmodels.VaultEvent
import com.pesky.app.viewmodels.VaultViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Login screen for unlocking or creating a vault.
 */
@Composable
fun LoginScreen(
    onDatabaseOpened: () -> Unit,
    onCreateDatabase: () -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val haptics = LocalPeskyHaptics.current
    
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var masterPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberLocation by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    
    // File picker with persistable permission
    val context = androidx.compose.ui.platform.LocalContext.current
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { 
            // Take persistable permission for the URI
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Permission might not be grantable, continue anyway
            }
            selectedUri = it 
        }
    }
    
    // Shake animation for error
    val shakeOffset by animateFloatAsState(
        targetValue = if (showError) 1f else 0f,
        animationSpec = if (showError) PeskyAnimations.shakeAnimation else tween(0)
    )
    
    LaunchedEffect(showError) {
        if (showError) {
            kotlinx.coroutines.delay(400)
            showError = false
        }
    }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is VaultEvent.DatabaseOpened -> {
                    haptics.success()
                    onDatabaseOpened()
                }
                is VaultEvent.Error -> {
                    haptics.error()
                    showError = true
                }
                else -> {}
            }
        }
    }
    
    // Logo animation
    val logoAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, delayMillis = 100)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PeskyColors.BackgroundPrimary)
            .imePadding() // Add padding when keyboard shows
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Make scrollable
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with animation
            val infiniteTransition = rememberInfiniteTransition(label = "lock_animation")
            val lockScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "lock_scale"
            )
            val lockRotation by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "lock_rotation"
            )
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { alpha = logoAlpha },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pesky_logo),
                    contentDescription = "Pesky Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            scaleX = lockScale
                            scaleY = lockScale
                            rotationZ = lockRotation
                        }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Pesky",
                style = MaterialTheme.typography.displayLarge,
                color = PeskyColors.TextPrimary
            )
            
            Text(
                text = "Secure Offline Password Manager",
                style = MaterialTheme.typography.bodyMedium,
                color = PeskyColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Database selection
            if (selectedUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PeskyColors.CardBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = PeskyColors.AccentBlue
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Database Selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PeskyColors.TextPrimary
                            )
                            Text(
                                text = selectedUri?.lastPathSegment ?: "Unknown",
                                style = MaterialTheme.typography.bodySmall,
                                color = PeskyColors.TextSecondary
                            )
                        }
                        
                        IconButton(onClick = { selectedUri = null }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear selection",
                                tint = PeskyColors.IconSecondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Master password field
                OutlinedTextField(
                    value = masterPassword,
                    onValueChange = { masterPassword = it },
                    label = { Text("Master Password") },
                    placeholder = { Text("Enter your master password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationX = shakeOffset * 10 },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) 
                        VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (masterPassword.isNotEmpty() && selectedUri != null) {
                                viewModel.openDatabase(
                                    selectedUri!!,
                                    masterPassword.toCharArray()
                                )
                            }
                        }
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
                        unfocusedBorderColor = PeskyColors.Divider,
                        focusedLabelColor = PeskyColors.AccentBlue
                    ),
                    isError = uiState.error != null
                )
                
                // Error message
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Text(
                        text = uiState.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = PeskyColors.Error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                if (uiState.remainingAttempts < 5) {
                    Text(
                        text = "${uiState.remainingAttempts} attempts remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = PeskyColors.Warning,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Unlock button
                Button(
                    onClick = {
                        if (masterPassword.isNotEmpty() && selectedUri != null) {
                            viewModel.openDatabase(
                                selectedUri!!,
                                masterPassword.toCharArray()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = masterPassword.isNotEmpty() && !uiState.isLoading,
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
                        Text("Unlock")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Remember location checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberLocation,
                        onCheckedChange = { rememberLocation = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PeskyColors.AccentBlue
                        )
                    )
                    Text(
                        text = "Remember database location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PeskyColors.TextSecondary
                    )
                }
            } else {
                // Open/Create buttons
                Button(
                    onClick = { filePicker.launch(arrayOf("*/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PeskyColors.AccentBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Database")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onCreateDatabase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PeskyColors.AccentBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PeskyColors.AccentBlue)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create New Database")
                }
            }
        }
    }
}
