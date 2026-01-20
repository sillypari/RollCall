package com.pesky.app.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Apple Maps-inspired transition specifications.
 * Based on iOS Human Interface Guidelines for fluid animations.
 */
object PeskyTransitions {
    
    // ============================================
    // SPRING PHYSICS CONSTANTS
    // ============================================
    
    /**
     * Default spring for most UI elements.
     * Feels responsive with subtle bounce.
     */
    val DefaultSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Snappy spring for buttons and small elements.
     * Quick response with minimal overshoot.
     */
    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    /**
     * Bouncy spring for playful interactions.
     * Used for favorites, success states.
     */
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    /**
     * Gentle spring for large elements like modals.
     * Smooth, elegant movement.
     */
    val GentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    // ============================================
    // DURATION CONSTANTS (Apple HIG inspired)
    // ============================================
    
    const val INSTANT = 100
    const val FAST = 200
    const val MEDIUM = 300
    const val SLOW = 450
    const val VERY_SLOW = 600
    
    // ============================================
    // EASING CURVES
    // ============================================
    
    val AppleEaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
    val AppleEaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)
    val AppleEaseIn = CubicBezierEasing(0.42f, 0f, 1f, 1f)
    val AppleSpringOut = CubicBezierEasing(0.17f, 0.89f, 0.32f, 1.28f)
    
    // ============================================
    // TWEEN SPECIFICATIONS
    // ============================================
    
    fun <T> fastTween() = tween<T>(
        durationMillis = FAST,
        easing = AppleEaseInOut
    )
    
    fun <T> mediumTween() = tween<T>(
        durationMillis = MEDIUM,
        easing = AppleEaseInOut
    )
    
    fun <T> slowTween() = tween<T>(
        durationMillis = SLOW,
        easing = AppleEaseInOut
    )
    
    fun <T> springOutTween() = tween<T>(
        durationMillis = MEDIUM,
        easing = AppleSpringOut
    )
    
    // ============================================
    // MODAL TRANSITIONS
    // ============================================
    
    /**
     * Bottom sheet enter animation.
     * Slides up from bottom with fade.
     */
    val BottomSheetEnter = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(
            durationMillis = MEDIUM,
            easing = AppleSpringOut
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = FAST,
            easing = AppleEaseOut
        )
    )
    
    /**
     * Bottom sheet exit animation.
     * Slides down with fade.
     */
    val BottomSheetExit = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(
            durationMillis = FAST,
            easing = AppleEaseIn
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = FAST,
            easing = AppleEaseIn
        )
    )
    
    /**
     * Action sheet enter animation.
     * Slightly slower for emphasis.
     */
    val ActionSheetEnter = slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = tween(
            durationMillis = MEDIUM,
            easing = AppleSpringOut
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = FAST,
            easing = AppleEaseOut
        )
    )
    
    /**
     * Action sheet exit animation.
     */
    val ActionSheetExit = slideOutVertically(
        targetOffsetY = { it / 2 },
        animationSpec = tween(
            durationMillis = FAST,
            easing = AppleEaseIn
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = INSTANT,
            easing = LinearEasing
        )
    )
    
    // ============================================
    // CARD TRANSITIONS
    // ============================================
    
    /**
     * Card appear animation (staggered list item).
     */
    fun cardEnter(index: Int) = slideInVertically(
        initialOffsetY = { 50 },
        animationSpec = tween(
            durationMillis = MEDIUM,
            delayMillis = index * 50,
            easing = AppleSpringOut
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = MEDIUM,
            delayMillis = index * 50,
            easing = AppleEaseOut
        )
    )
    
    /**
     * Card exit animation.
     */
    val CardExit = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(
            durationMillis = FAST,
            easing = AppleEaseIn
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = FAST
        )
    )
    
    // ============================================
    // NAVIGATION TRANSITIONS
    // ============================================
    
    /**
     * Push navigation enter (iOS style).
     */
    val PushEnter = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(
            durationMillis = MEDIUM,
            easing = AppleEaseOut
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = FAST
        )
    )
    
    /**
     * Push navigation exit.
     */
    val PushExit = slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = tween(
            durationMillis = MEDIUM,
            easing = AppleEaseOut
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = MEDIUM,
            easing = AppleEaseIn
        ),
        targetAlpha = 0.5f
    )
    
    /**
     * Pop navigation enter.
     */
    val PopEnter = slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(
            durationMillis = MEDIUM,
            easing = AppleEaseOut
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = FAST
        ),
        initialAlpha = 0.5f
    )
    
    /**
     * Pop navigation exit.
     */
    val PopExit = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(
            durationMillis = MEDIUM,
            easing = AppleEaseIn
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = FAST
        )
    )
    
    // ============================================
    // MICRO-INTERACTION ANIMATIONS
    // ============================================
    
    /**
     * Pulse animation for attention.
     */
    val PulseAnimation = infiniteRepeatable<Float>(
        animation = tween(
            durationMillis = 1000,
            easing = AppleEaseInOut
        ),
        repeatMode = RepeatMode.Reverse
    )
    
    /**
     * Shimmer animation for loading states.
     */
    val ShimmerAnimation = infiniteRepeatable<Float>(
        animation = tween(
            durationMillis = 1500,
            easing = LinearEasing
        ),
        repeatMode = RepeatMode.Restart
    )
    
    /**
     * Breathing animation for subtle emphasis.
     */
    val BreathingAnimation = infiniteRepeatable<Float>(
        animation = tween(
            durationMillis = 2000,
            easing = AppleEaseInOut
        ),
        repeatMode = RepeatMode.Reverse
    )
    
    // ============================================
    // SCALE ANIMATIONS
    // ============================================
    
    /**
     * Press scale effect.
     */
    val PressScale = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    /**
     * Hover scale effect.
     */
    val HoverScale = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    // ============================================
    // CONTENT SIZE ANIMATIONS
    // ============================================
    
    /**
     * Expand/collapse animation.
     */
    val ExpandCollapse = spring<Int>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    /**
     * Content size change animation.
     */
    val ContentSizeChange = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

/**
 * Animation durations for consistent timing across the app.
 */
object AnimationDurations {
    const val INSTANT = 100
    const val FAST = 200
    const val MEDIUM = 300
    const val SLOW = 450
    const val VERY_SLOW = 600
    
    // Stagger delays
    const val STAGGER_DELAY = 50
    const val LONG_STAGGER_DELAY = 100
}

/**
 * Spring configurations for different use cases.
 */
object SpringConfigs {
    val Button = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    val Card = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val Modal = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    val Gesture = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
}
