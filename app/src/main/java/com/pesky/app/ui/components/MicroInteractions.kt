package com.pesky.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pesky.app.ui.theme.PeskyColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated favorite star button with particle burst effect.
 */
@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val haptic = LocalHapticFeedback.current
    
    // Animation states
    var showParticles by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "star_scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isFavorite) 360f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "star_rotation"
    )
    
    // Particle animation
    LaunchedEffect(isFavorite) {
        if (isFavorite) {
            showParticles = true
            kotlinx.coroutines.delay(600)
            showParticles = false
        }
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Particle burst
        if (showParticles) {
            ParticleBurst(
                modifier = Modifier.size(size * 2),
                particleCount = 8,
                color = PeskyColors.Warning
            )
        }
        
        // Star button
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (isFavorite) PeskyColors.Warning else PeskyColors.IconSecondary,
                modifier = Modifier
                    .scale(scale)
                    .graphicsLayer { rotationZ = if (isFavorite) rotation else 0f }
            )
        }
    }
}

/**
 * Particle burst effect for favorite animation.
 */
@Composable
private fun ParticleBurst(
    modifier: Modifier = Modifier,
    particleCount: Int = 8,
    color: Color
) {
    val particles = remember {
        List(particleCount) {
            val angle = (360f / particleCount) * it + Random.nextFloat() * 30f
            Particle(angle = angle)
        }
    }
    
    val animatedProgress by rememberInfiniteTransition(label = "particles")
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_progress"
        )
    
    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val progress = animatedProgress
            val distance = size.minDimension * 0.4f * progress
            val alpha = 1f - progress
            
            val x = center.x + cos(particle.angle * PI / 180).toFloat() * distance
            val y = center.y + sin(particle.angle * PI / 180).toFloat() * distance
            
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = 4.dp.toPx() * (1f - progress * 0.5f),
                center = Offset(x, y)
            )
        }
    }
}

private data class Particle(val angle: Float)

/**
 * Animated copy button with success state.
 */
@Composable
fun AnimatedCopyButton(
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val haptic = LocalHapticFeedback.current
    var isCopied by remember { mutableStateOf(false) }
    
    val iconRotation by animateFloatAsState(
        targetValue = if (isCopied) 360f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "copy_rotation"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isCopied) PeskyColors.Success.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(200),
        label = "copy_bg"
    )
    
    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(2000)
            isCopied = false
        }
    }
    
    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            isCopied = true
            onCopy()
        },
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        AnimatedContent(
            targetState = isCopied,
            transitionSpec = {
                scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                fadeIn() togetherWith scaleOut() + fadeOut()
            },
            label = "copy_icon"
        ) { copied ->
            Icon(
                imageVector = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                contentDescription = "Copy",
                tint = if (copied) PeskyColors.Success else PeskyColors.AccentBlue,
                modifier = Modifier.graphicsLayer { rotationZ = iconRotation }
            )
        }
    }
}

/**
 * Apple Maps-style floating action button with gradient and shadow.
 */
@Composable
fun PeskyFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { Icon(Icons.Filled.Add, "Add") }
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "fab_scale"
    )
    
    Box(
        modifier = modifier
            .size(64.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // FAB body with gradient
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PeskyColors.AccentBlue,
                            PeskyColors.AccentBlueDark
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides Color.White) {
                icon()
            }
        }
    }
}

/**
 * Pill-shaped button (Apple Maps category style).
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: @Composable (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) PeskyColors.AccentBlue else PeskyColors.PillButtonBackground,
        animationSpec = tween(200),
        label = "pill_bg"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else PeskyColors.AccentBlue,
        animationSpec = tween(200),
        label = "pill_content"
    )
    
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    icon()
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}
