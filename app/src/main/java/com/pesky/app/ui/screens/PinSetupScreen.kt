package com.pesky.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.viewmodels.PinSetupViewModel
import kotlinx.coroutines.delay

/**
 * Screen for setting up a quick unlock PIN after creating a database.
 */
@Composable
fun PinSetupScreen(
    onPinSet: () -> Unit,
    onSkip: () -> Unit,
    viewModel: PinSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
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
    
    // Handle PIN input
    val onDigitClick: (String) -> Unit = { digit ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (isConfirming) {
            if (confirmPin.length < 6) {
                confirmPin += digit
                if (confirmPin.length == 6 || (confirmPin.length >= 4 && confirmPin.length == pin.length)) {
                    // Auto-verify when confirmed PIN matches first PIN length
                    if (confirmPin == pin) {
                        viewModel.setupPin(pin)
                        onPinSet()
                    } else if (confirmPin.length == pin.length) {
                        errorMessage = "PINs don't match. Try again."
                        showError = true
                        confirmPin = ""
                    }
                }
            }
        } else {
            if (pin.length < 6) {
                pin += digit
            }
        }
    }
    
    val onBackspace: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (isConfirming) {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            }
        } else {
            if (pin.isNotEmpty()) {
                pin = pin.dropLast(1)
            }
        }
    }
    
    val onContinue: () -> Unit = {
        when {
            !isConfirming && pin.length >= 4 -> {
                isConfirming = true
            }
            isConfirming && confirmPin == pin -> {
                viewModel.setupPin(pin)
                onPinSet()
            }
            isConfirming -> {
                errorMessage = "PINs don't match. Try again."
                showError = true
                confirmPin = ""
            }
            else -> {
                errorMessage = "PIN must be at least 4 digits"
                showError = true
            }
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
                .background(PeskyColors.AccentBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = PeskyColors.AccentBlue
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = if (isConfirming) "Confirm Your PIN" else "Set Up Quick Unlock",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PeskyColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = if (isConfirming) 
                "Enter your PIN again to confirm" 
            else 
                "Create a 4-6 digit PIN for quick access to your vault",
            style = MaterialTheme.typography.bodyLarge,
            color = PeskyColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // PIN dots
        Row(
            modifier = Modifier.offset(x = shakeOffset.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val currentPin = if (isConfirming) confirmPin else pin
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < currentPin.length) PeskyColors.AccentBlue
                            else PeskyColors.CardBackground
                        )
                        .border(
                            width = 2.dp,
                            color = if (index < currentPin.length) PeskyColors.AccentBlue 
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
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Continue button (when PIN >= 4 digits)
        val currentPin = if (isConfirming) confirmPin else pin
        AnimatedVisibility(
            visible = currentPin.length >= 4,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PeskyColors.AccentBlue
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isConfirming) "Confirm" else "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skip button
        if (!isConfirming) {
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip for now",
                    color = PeskyColors.TextSecondary
                )
            }
        } else {
            TextButton(onClick = { 
                isConfirming = false
                confirmPin = ""
            }) {
                Text(
                    text = "Go back",
                    color = PeskyColors.TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Number pad for PIN entry.
 */
@Composable
fun NumPad(
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        digits.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { digit ->
                    if (digit.isEmpty()) {
                        Spacer(modifier = Modifier.size(72.dp))
                    } else if (digit == "⌫") {
                        NumPadButton(
                            content = {
                                Icon(
                                    imageVector = Icons.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = PeskyColors.TextPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            onClick = onBackspace
                        )
                    } else {
                        NumPadButton(
                            content = {
                                Text(
                                    text = digit,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = PeskyColors.TextPrimary
                                )
                            },
                            onClick = { onDigitClick(digit) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumPadButton(
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(PeskyColors.CardBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
