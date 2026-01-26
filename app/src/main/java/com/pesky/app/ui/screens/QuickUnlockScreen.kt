package com.pesky.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pesky.app.ui.components.LocalPeskyHaptics
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.viewmodels.QuickUnlockEvent
import com.pesky.app.viewmodels.QuickUnlockViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/**
 * Screen for quick unlock using PIN, then master password.
 */
@Composable
fun QuickUnlockScreen(
    databaseName: String,
    onUnlocked: () -> Unit,
    onSwitchDatabase: () -> Unit,
    viewModel: QuickUnlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = LocalPeskyHaptics.current
    
    var pin by remember { mutableStateOf("") }
    var masterPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    
    // Lock icon animation states
    val lockScale by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "lock_scale"
    )
    
    val lockRotation by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 360f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "lock_rotation"
    )
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Shake animation for error
    val shakeOffset by animateFloatAsState(
        targetValue = if (showError) 10f else 0f,
        animationSpec = if (showError) {
            keyframes {
                durationMillis = 400
                0f at 0
                10f at 50
                -10f at 100
                10f at 150
                -10f at 200
                10f at 250
                -10f at 300
                0f at 400
            }
        } else tween(0),
        label = "shake"
    )
    
    LaunchedEffect(showError) {
        if (showError) {
            delay(400)
            showError = false
        }
    }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is QuickUnlockEvent.PinVerified -> {
                    // PIN verified, now show master password
                    haptics.success()
                    showSuccessAnimation = true
                    delay(600)
                    showSuccessAnimation = false
                }
                is QuickUnlockEvent.DatabaseUnlocked -> {
                    haptics.success()
                    showSuccessAnimation = true
                    delay(400)
                    onUnlocked()
                }
                is QuickUnlockEvent.WrongPin -> {
                    errorMessage = "Wrong PIN"
                    showError = true
                    haptics.error()
                    pin = ""
                }
                is QuickUnlockEvent.WrongPassword -> {
                    errorMessage = "Wrong master password"
                    showError = true
                    haptics.error()
                }
                is QuickUnlockEvent.Error -> {
                    errorMessage = event.message
                    showError = true
                    haptics.error()
                }
            }
        }
    }
    
    // Handle PIN input
    val onDigitClick: (String) -> Unit = { digit ->
        haptics.keyboard()
        if (pin.length < uiState.pinLength) {
            val newPin = pin + digit
            pin = newPin
            // Auto-verify only when PIN length matches the stored PIN length
            if (newPin.length == uiState.pinLength) {
                viewModel.verifyPin(newPin)
            }
        }
    }
    
    val onBackspace: () -> Unit = {
        haptics.tick()
        if (pin.isNotEmpty()) {
            pin = pin.dropLast(1)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PeskyColors.BackgroundPrimary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Lock icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (showSuccessAnimation || uiState.isPinVerified) 
                        PeskyColors.AccentGreen.copy(alpha = 0.1f)
                    else 
                        PeskyColors.Error.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (showSuccessAnimation || uiState.isPinVerified) 
                    Icons.Filled.LockOpen 
                else 
                    Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = lockScale
                        scaleY = lockScale
                        rotationZ = lockRotation
                    },
                tint = if (showSuccessAnimation || uiState.isPinVerified) 
                    PeskyColors.AccentGreen 
                else 
                    PeskyColors.Error
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Database name
        Text(
            text = databaseName.ifEmpty { "My Vault" },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PeskyColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = if (uiState.isPinVerified) 
                "Enter your master password" 
            else if (uiState.quickUnlockEnabled)
                "Enter your PIN to unlock"
            else
                "Enter your master password to unlock",
            style = MaterialTheme.typography.bodyLarge,
            color = PeskyColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Show PIN pad or Master Password input based on state
        AnimatedContent(
            targetState = uiState.isPinVerified || !uiState.quickUnlockEnabled,
            transitionSpec = {
                fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
            },
            label = "unlock_content"
        ) { showMasterPassword ->
            if (showMasterPassword) {
                // Master password input
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = masterPassword,
                        onValueChange = { masterPassword = it },
                        label = { Text("Master Password") },
                        visualTransformation = if (isPasswordVisible) 
                            VisualTransformation.None 
                        else 
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) 
                                        Icons.Filled.VisibilityOff 
                                    else 
                                        Icons.Filled.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.unlockDatabase(masterPassword.toCharArray())
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = shakeOffset.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PeskyColors.AccentBlue,
                            unfocusedBorderColor = PeskyColors.Border,
                            focusedLabelColor = PeskyColors.AccentBlue,
                            cursorColor = PeskyColors.AccentBlue
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { viewModel.unlockDatabase(masterPassword.toCharArray()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = masterPassword.isNotEmpty() && !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PeskyColors.AccentBlue
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PeskyColors.TextOnAccent
                            )
                        } else {
                            Text(
                                text = "Unlock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // PIN input
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // PIN dots
                    Row(
                        modifier = Modifier.offset(x = shakeOffset.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(uiState.pinLength) { index ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < pin.length) PeskyColors.AccentBlue
                                        else PeskyColors.CardBackground
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (index < pin.length) PeskyColors.AccentBlue 
                                               else PeskyColors.Border,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    
                    // Error message
                    AnimatedVisibility(
                        visible = showError,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PeskyColors.AccentRed,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Number pad
                    NumPad(
                        onDigitClick = onDigitClick,
                        onBackspace = onBackspace,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Error message for master password
        AnimatedVisibility(
            visible = showError && (uiState.isPinVerified || !uiState.quickUnlockEnabled),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = PeskyColors.AccentRed,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Switch database button - pill-shaped for better visibility
        Surface(
            onClick = onSwitchDatabase,
            shape = RoundedCornerShape(24.dp),
            color = PeskyColors.CardBackground,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    tint = PeskyColors.AccentBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Switch Database",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeskyColors.AccentBlue
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
