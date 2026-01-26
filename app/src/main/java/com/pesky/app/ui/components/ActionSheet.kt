package com.pesky.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pesky.app.ui.theme.PeskyColors

/**
 * iOS-style action sheet that slides up from the bottom.
 */
@Composable
fun ActionSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    actions: List<ActionSheetItem>,
    showCancel: Boolean = true
) {
    val haptics = LocalPeskyHaptics.current
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        )
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PeskyColors.Scrim)
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { /* Consume tap */ }
                        }
                ) {
                    // Action list
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PeskyColors.BackgroundTertiary
                        )
                    ) {
                        Column {
                            // Title
                            if (title != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PeskyColors.TextSecondary
                                    )
                                }
                                Divider(color = PeskyColors.Divider)
                            }
                            
                            // Actions
                            actions.forEachIndexed { index, action ->
                                ActionSheetButton(
                                    item = action,
                                    onClick = {
                                        if (action.isDestructive) {
                                            haptics.heavyClick()
                                        } else {
                                            haptics.click()
                                        }
                                        action.onClick()
                                        onDismiss()
                                    }
                                )
                                
                                if (index < actions.lastIndex) {
                                    Divider(color = PeskyColors.Divider)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Cancel button
                    if (showCancel) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PeskyColors.BackgroundTertiary
                            )
                        ) {
                            ActionSheetButton(
                                item = ActionSheetItem(
                                    icon = null,
                                    label = "Cancel",
                                    onClick = onDismiss,
                                    isBold = true
                                ),
                                onClick = onDismiss
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Individual action sheet button.
 */
@Composable
private fun ActionSheetButton(
    item: ActionSheetItem,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) PeskyColors.BackgroundSecondary else Color.Transparent,
        animationSpec = tween(100),
        label = "action_bg"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(vertical = 14.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.icon != null) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (item.isDestructive) PeskyColors.Error else PeskyColors.AccentBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        Text(
            text = item.label,
            style = if (item.isBold) MaterialTheme.typography.titleMedium 
                   else MaterialTheme.typography.bodyLarge,
            color = when {
                item.isDestructive -> PeskyColors.Error
                item.isBold -> PeskyColors.AccentBlue
                else -> PeskyColors.AccentBlue
            }
        )
    }
}

/**
 * Action sheet item data.
 */
data class ActionSheetItem(
    val icon: ImageVector? = null,
    val label: String,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false,
    val isBold: Boolean = false
)

/**
 * Pre-built action sheet for password entry context menu.
 */
@Composable
fun EntryActionSheet(
    visible: Boolean,
    entryTitle: String,
    onDismiss: () -> Unit,
    onCopyPassword: () -> Unit,
    onCopyUsername: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ActionSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = entryTitle,
        actions = listOf(
            ActionSheetItem(
                icon = Icons.Filled.Lock,
                label = "Copy Password",
                onClick = onCopyPassword
            ),
            ActionSheetItem(
                icon = Icons.Filled.Person,
                label = "Copy Username",
                onClick = onCopyUsername
            ),
            ActionSheetItem(
                icon = Icons.Filled.Edit,
                label = "Edit Entry",
                onClick = onEdit
            ),
            ActionSheetItem(
                icon = Icons.Filled.Delete,
                label = "Delete",
                onClick = onDelete,
                isDestructive = true
            )
        )
    )
}
