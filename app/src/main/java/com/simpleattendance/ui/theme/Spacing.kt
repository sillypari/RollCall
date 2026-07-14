package com.simpleattendance.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 8dp spacing grid with 4dp half-steps.
 * Use these tokens instead of hard-coded values.
 */
@Immutable
object RollCallSpacing {
    val xs   : Dp = 4.dp
    val sm   : Dp = 8.dp
    val md   : Dp = 12.dp
    val lg   : Dp = 16.dp
    val xl   : Dp = 20.dp
    val xxl  : Dp = 24.dp
    val xxxl : Dp = 32.dp
    val huge : Dp = 40.dp
    val epic : Dp = 48.dp

    /** Standard horizontal screen padding */
    val screenHorizontal: Dp = 16.dp

    /** Attendance screen horizontal padding (slightly wider for focus) */
    val attendanceHorizontal: Dp = 20.dp

    /** Minimum interactive touch target */
    val minTouchTarget: Dp = 48.dp

    /** Primary button height */
    val primaryButtonHeight: Dp = 56.dp

    /** Large action button height (Attendance Present/Absent) */
    val largeButtonHeight: Dp = 72.dp
}
