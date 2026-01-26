package com.pesky.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pesky.app.data.models.PasswordEntry
import com.pesky.app.data.models.PasswordStrength
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.ui.animations.PeskyAnimations
import java.time.Duration
import java.time.Instant

/**
 * Apple Maps-style password entry card with micro-interactions.
 * Features: Hover lift, press scale, smooth shadows, haptic feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordEntryCard(
    entry: PasswordEntry,
    passwordStrength: PasswordStrength,
    onClick: () -> Unit,
    onCopyPassword: () -> Unit,
    onToggleFavorite: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptics = LocalPeskyHaptics.current
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    
    // Smooth scale animation on press
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.98f
            isHovered -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )
    
    // Elevation animation
    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 4.dp
            isHovered -> 12.dp
            else -> 2.dp
        },
        animationSpec = tween(150),
        label = "card_elevation"
    )
    
    // Background color animation
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isPressed -> PeskyColors.CardBackgroundPressed
            isHovered -> PeskyColors.CardBackgroundHovered
            else -> PeskyColors.CardBackground
        },
        animationSpec = tween(150),
        label = "card_bg"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PeskyColors.ShadowAmbient,
                spotColor = PeskyColors.ShadowSpot
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        haptics.tick()
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() },
                    onLongPress = {
                        haptics.heavyClick()
                        onLongPress?.invoke()
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon/Avatar with subtle glow on hover
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isHovered) PeskyColors.AccentBlueSubtle 
                        else PeskyColors.BackgroundTertiary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.title.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PeskyColors.AccentBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // Title and username
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = PeskyColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Password strength indicator
                    PasswordStrengthDot(strength = passwordStrength)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = entry.userName.ifEmpty { "No username" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeskyColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Last modified with subtle styling
                Text(
                    text = formatRelativeTime(entry.times.lastModificationTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = PeskyColors.TextTertiary
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Animated favorite button
            AnimatedFavoriteButton(
                isFavorite = entry.isFavorite,
                onClick = onToggleFavorite
            )
            
            // Animated copy button
            AnimatedCopyButton(onCopy = onCopyPassword)
        }
    }
}

/**
 * Small dot indicating password strength.
 */
@Composable
fun PasswordStrengthDot(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val color = when (strength) {
        PasswordStrength.VERY_WEAK -> PeskyColors.StrengthVeryWeak
        PasswordStrength.WEAK -> PeskyColors.StrengthWeak
        PasswordStrength.FAIR -> PeskyColors.StrengthFair
        PasswordStrength.STRONG -> PeskyColors.StrengthStrong
        PasswordStrength.VERY_STRONG -> PeskyColors.StrengthVeryStrong
    }
    
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Formats instant to relative time string.
 */
private fun formatRelativeTime(instant: Instant): String {
    val now = Instant.now()
    val duration = Duration.between(instant, now)
    
    return when {
        duration.toMinutes() < 1 -> "Just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()} min ago"
        duration.toHours() < 24 -> "${duration.toHours()} hours ago"
        duration.toDays() < 7 -> "${duration.toDays()} days ago"
        duration.toDays() < 30 -> "${duration.toDays() / 7} weeks ago"
        duration.toDays() < 365 -> "${duration.toDays() / 30} months ago"
        else -> "${duration.toDays() / 365} years ago"
    }
}
