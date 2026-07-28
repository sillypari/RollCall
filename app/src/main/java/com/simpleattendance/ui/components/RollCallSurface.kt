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
import androidx.compose.ui.graphics.Brush

/**
 * Solid tonal surface. The primary building block for cards and sections.
 * Uses only theme tokens — never pass raw Color values from screens.
 */
@Composable
fun RollCallSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color? = null,
    brush: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val resolvedColor = color ?: MaterialTheme.colorScheme.surface
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (brush != null) Modifier.background(brush)
                else Modifier.background(resolvedColor)
            ),
        content = content
    )
}

/** Elevated surface — for grouped content one level up from Surface */
@Composable
fun RollCallContainerSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable BoxScope.() -> Unit
) = RollCallSurface(
    modifier = modifier,
    shape = shape,
    color = MaterialTheme.colorScheme.surfaceContainer,
    content = content
)

/** High surface — for selected, floating or most prominent content */
@Composable
fun RollCallHighSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable BoxScope.() -> Unit
) = RollCallSurface(
    modifier = modifier,
    shape = shape,
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content = content
)
