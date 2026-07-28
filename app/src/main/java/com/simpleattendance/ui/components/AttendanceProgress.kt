package com.simpleattendance.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.simpleattendance.ui.theme.RollCallMotion

/**
 * Segmented attendance progress bar.
 * Shows: [Present=green] | [Absent=red] | [Unmarked=neutral]
 * Animates on each mark action.
 */
@Composable
fun AttendanceProgressBar(
    totalStudents: Int,
    presentCount: Int,
    absentCount: Int,
    modifier: Modifier = Modifier,
    presentColor: Color = MaterialTheme.colorScheme.tertiary,
    absentColor: Color = MaterialTheme.colorScheme.error,
    unmarkedColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    if (totalStudents <= 0) return

    val presentFraction = presentCount.toFloat() / totalStudents
    val absentFraction = absentCount.toFloat() / totalStudents

    val animatedPresent by animateFloatAsState(
        targetValue = presentFraction,
        animationSpec = tween(RollCallMotion.Standard),
        label = "present_progress"
    )
    val animatedAbsent by animateFloatAsState(
        targetValue = absentFraction,
        animationSpec = tween(RollCallMotion.Standard),
        label = "absent_progress"
    )

    val radius = 6.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val cornerPx = radius.toPx()
        val gap = 4.dp.toPx()

        // Background (unmarked)
        drawRoundRect(
            color = unmarkedColor,
            size = Size(canvasWidth, canvasHeight),
            cornerRadius = CornerRadius(cornerPx)
        )

        // Present segment (left)
        val presentWidth = canvasWidth * animatedPresent
        if (presentWidth > 0) {
            drawRoundRect(
                color = presentColor,
                size = Size(presentWidth, canvasHeight),
                cornerRadius = CornerRadius(cornerPx)
            )
        }

        // Absent segment (right-anchored)
        val absentWidth = canvasWidth * animatedAbsent
        if (absentWidth > 0) {
            drawRoundRect(
                color = absentColor,
                topLeft = Offset(x = canvasWidth - absentWidth, y = 0f),
                size = Size(absentWidth, canvasHeight),
                cornerRadius = CornerRadius(cornerPx)
            )
        }
    }
}
