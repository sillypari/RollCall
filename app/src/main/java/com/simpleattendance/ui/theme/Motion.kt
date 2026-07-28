package com.simpleattendance.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable

/**
 * RollCall centralized motion specification.
 * Use these constants and specs for all animations.
 * Do not invent per-composable duration values.
 */
@Immutable
object RollCallMotion {
    /** 80-100ms — press response, icon swap */
    const val Instant: Int = 90

    /** 150-200ms — small fades, icon transitions */
    const val Fast: Int = 180

    /** 250-320ms — content and container changes */
    const val Standard: Int = 300

    /** 400-500ms — screen-level or major state transition */
    const val Emphasized: Int = 450

    /** 600-800ms — rare completion moment only */
    const val Celebration: Int = 700

    // ── Easing curves ────────────────────────────────────────────────────────
    /** Standard emphasized ease — for most element transitions */
    val EmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** Decelerate — content entering the screen */
    val DecelerateEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

    /** Accelerate — content leaving the screen */
    val AccelerateEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

    // ── Spring specs ─────────────────────────────────────────────────────────
    /** Responsive spring for scale/position settling — press feedback */
    val SnapSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

    /** Standard spring for content transitions */
    val StandardSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** Gentle spring for large surface transitions */
    val GentleSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Type-safe gentle spring for bounds, offsets and other non-Float values. */
    fun <T> gentleSpring(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // ── Press scale ──────────────────────────────────────────────────────────
    /** Components scale to this when pressed */
    const val PressedScale: Float = 0.96f

    /** Released/normal scale */
    const val NormalScale: Float = 1.0f

    // ── Tween helpers ────────────────────────────────────────────────────────
    fun <T> instantTween() = tween<T>(Instant)
    fun <T> fastTween() = tween<T>(Fast, easing = DecelerateEasing)
    fun <T> standardTween() = tween<T>(Standard, easing = EmphasizedEasing)
    fun <T> emphasizedTween() = tween<T>(Emphasized, easing = EmphasizedEasing)
}
