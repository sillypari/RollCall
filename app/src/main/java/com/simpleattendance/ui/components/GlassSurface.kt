package com.simpleattendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Glass surface abstraction per redesign guide section 8.
 *
 * Mode selection:
 * - API 31+: could use RenderEffect blur. Currently falls back to opaque tonal for
 *   safety on first beta. Blur can be added behind a flag once screens are stable.
 * - All APIs: Opaque tonal surface with the same shape, border and layout.
 *
 * Use ONLY for: floating nav, top app bar, modal sheets, dialogs, attendance dock.
 * Do NOT use for normal cards, list items, text fields.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    /** The opaque tonal background color when blur is not used. */
    tonalColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
    /** Show the subtle white border highlight. */
    showBorder: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val borderModifier = if (showBorder) {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            shape = shape
        )
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .background(tonalColor)
            .then(borderModifier),
        content = content
    )
}
