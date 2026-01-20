package com.pesky.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.ui.animations.PeskyAnimations
import kotlinx.coroutines.delay

/**
 * Copy button with success animation and haptic feedback.
 */
@Composable
fun CopyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Copy",
    tint: Color = PeskyColors.AccentBlue
) {
    var showSuccess by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(1500)
            showSuccess = false
        }
    }
    
    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
            showSuccess = true
        },
        modifier = modifier.size(40.dp)
    ) {
        AnimatedContent(
            targetState = showSuccess,
            transitionSpec = {
                (fadeIn(animationSpec = PeskyAnimations.standard()) + 
                 scaleIn(initialScale = 0.8f, animationSpec = PeskyAnimations.standard()))
                    .togetherWith(
                        fadeOut(animationSpec = PeskyAnimations.standard()) + 
                        scaleOut(targetScale = 0.8f, animationSpec = PeskyAnimations.standard())
                    )
            },
            label = "copy_animation"
        ) { success ->
            if (success) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Copied",
                    tint = PeskyColors.Success
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = contentDescription,
                    tint = tint
                )
            }
        }
    }
}

/**
 * Copy button with text label.
 */
@Composable
fun CopyButtonWithLabel(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSuccess by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(1500)
            showSuccess = false
        }
    }
    
    TextButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
            showSuccess = true
        },
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (showSuccess) PeskyColors.Success else PeskyColors.AccentBlue
        )
    ) {
        AnimatedContent(
            targetState = showSuccess,
            transitionSpec = {
                fadeIn(animationSpec = PeskyAnimations.standard())
                    .togetherWith(fadeOut(animationSpec = PeskyAnimations.standard()))
            },
            label = "copy_label_animation"
        ) { success ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (success) Icons.Filled.Check else Icons.Filled.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (success) "Copied!" else label,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
