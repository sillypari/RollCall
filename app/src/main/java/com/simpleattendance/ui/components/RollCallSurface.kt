package com.simpleattendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.simpleattendance.ui.theme.Surface
import com.simpleattendance.ui.theme.SurfaceContainer
import com.simpleattendance.ui.theme.SurfaceContainerHigh

/**
 * Solid tonal surface. The primary building block for cards and sections.
 * Uses only theme tokens — never pass raw Color values from screens.
 */
@Composable
fun RollCallSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = Surface,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color),
        content = content
    )
}

/** Elevated surface — for grouped content one level up from Surface */
@Composable
fun RollCallContainerSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable BoxScope.() -> Unit
) = RollCallSurface(modifier, shape, SurfaceContainer, content)

/** High surface — for selected, floating or most prominent content */
@Composable
fun RollCallHighSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable BoxScope.() -> Unit
) = RollCallSurface(modifier, shape, SurfaceContainerHigh, content)
