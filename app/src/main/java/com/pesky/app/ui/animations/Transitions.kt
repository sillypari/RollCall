package com.pesky.app.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animation specifications for Pesky - Apple-style smooth animations.
 */
object PeskyAnimations {
    
    // Easing curves
    val EaseInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val EaseOut = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val EaseIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val EaseInOutBack = CubicBezierEasing(0.68f, -0.55f, 0.265f, 1.55f)
    
    // Duration constants
    const val DURATION_INSTANT = 100
    const val DURATION_SHORT = 150
    const val DURATION_MEDIUM = 250
    const val DURATION_LONG = 350
    const val DURATION_EXTRA_LONG = 500
    const val screenTransitionDuration = 300
    
    // Common animation specs
    fun <T> quickSnap(): TweenSpec<T> = tween(
        durationMillis = DURATION_INSTANT,
        easing = EaseInOut
    )
    
    fun <T> standard(): TweenSpec<T> = tween(
        durationMillis = DURATION_MEDIUM,
        easing = EaseInOut
    )
    
    fun <T> emphasized(): TweenSpec<T> = tween(
        durationMillis = DURATION_LONG,
        easing = EaseInOut
    )
    
    // Entry card animation
    fun entryCardAnimation(): TweenSpec<Float> = tween(
        durationMillis = DURATION_MEDIUM,
        easing = EaseInOut
    )
    
    // Search expand animation - springy feel
    fun searchExpandAnimation(): SpringSpec<Dp> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    // FAB animation
    fun fabAnimation(): SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    // Modal slide up
    fun modalSlideUp(): EnterTransition = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(DURATION_LONG, easing = EaseInOut)
    ) + fadeIn(animationSpec = tween(DURATION_MEDIUM))
    
    fun modalSlideDown(): ExitTransition = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(DURATION_LONG, easing = EaseInOut)
    ) + fadeOut(animationSpec = tween(DURATION_MEDIUM))
    
    // Screen transitions
    fun screenEnter(): EnterTransition = fadeIn(
        animationSpec = tween(DURATION_MEDIUM, easing = EaseInOut)
    ) + slideInHorizontally(
        initialOffsetX = { it / 4 },
        animationSpec = tween(DURATION_MEDIUM, easing = EaseInOut)
    )
    
    fun screenExit(): ExitTransition = fadeOut(
        animationSpec = tween(DURATION_SHORT, easing = EaseInOut)
    ) + slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = tween(DURATION_SHORT, easing = EaseInOut)
    )
    
    fun screenPopEnter(): EnterTransition = fadeIn(
        animationSpec = tween(DURATION_MEDIUM, easing = EaseInOut)
    ) + slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(DURATION_MEDIUM, easing = EaseInOut)
    )
    
    fun screenPopExit(): ExitTransition = fadeOut(
        animationSpec = tween(DURATION_SHORT, easing = EaseInOut)
    ) + slideOutHorizontally(
        targetOffsetX = { it / 4 },
        animationSpec = tween(DURATION_SHORT, easing = EaseInOut)
    )
    
    // List item stagger animation
    fun listItemDelay(index: Int): Int = DURATION_INSTANT * (index % 10)
    
    // Shake animation for errors
    val shakeAnimation: AnimationSpec<Float> = keyframes {
        durationMillis = 400
        0f at 0
        (-10f) at 50
        10f at 100
        (-10f) at 150
        10f at 200
        (-5f) at 250
        5f at 300
        0f at 400
    }
    
    // Pulse animation for copy button
    val pulseAnimation: InfiniteRepeatableSpec<Float> = infiniteRepeatable(
        animation = keyframes {
            durationMillis = 1000
            1f at 0
            1.1f at 500
            1f at 1000
        },
        repeatMode = RepeatMode.Restart
    )
}

/**
 * Extension to create staggered animation for list items.
 */
@Composable
fun <T> AnimatedVisibilityScope.staggeredAnimation(
    index: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = PeskyAnimations.DURATION_MEDIUM,
                delayMillis = PeskyAnimations.listItemDelay(index),
                easing = PeskyAnimations.EaseInOut
            )
        ) + slideInVertically(
            initialOffsetY = { 20 },
            animationSpec = tween(
                durationMillis = PeskyAnimations.DURATION_MEDIUM,
                delayMillis = PeskyAnimations.listItemDelay(index),
                easing = PeskyAnimations.EaseInOut
            )
        )
    ) {
        content()
    }
}
