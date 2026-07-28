package com.simpleattendance.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import kotlin.math.roundToInt

/**
 * Window-relative bounds used to originate a hero-style activity transition
 * from the card or control the user actually selected.
 */
data class LaunchOrigin(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
) {
    val isValid: Boolean
        get() = width > 0 && height > 0

    companion object {
        val Unspecified = LaunchOrigin(0, 0, 0, 0)
    }
}

fun Modifier.captureLaunchOrigin(
    onOriginChanged: (LaunchOrigin) -> Unit
): Modifier = onGloballyPositioned { coordinates ->
    val position: Offset = coordinates.positionInWindow()
    onOriginChanged(
        LaunchOrigin(
            left = position.x.roundToInt(),
            top = position.y.roundToInt(),
            width = coordinates.size.width,
            height = coordinates.size.height
        )
    )
}
