package com.simpleattendance.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simpleattendance.ui.theme.rollCallColors
import kotlin.math.absoluteValue

@Immutable
data class ClassAccentColors(
    val accent: Color,
    val container: Color,
    val onContainer: Color
)

@Composable
fun classAccentColors(classId: Long): ClassAccentColors {
    val scheme = MaterialTheme.colorScheme
    val extra = MaterialTheme.rollCallColors
    val palette = listOf(
        ClassAccentColors(scheme.primary, scheme.primaryContainer, scheme.onPrimaryContainer),
        ClassAccentColors(scheme.secondary, scheme.secondaryContainer, scheme.onSecondaryContainer),
        ClassAccentColors(scheme.tertiary, scheme.tertiaryContainer, scheme.onTertiaryContainer),
        ClassAccentColors(extra.warning, extra.warningContainer, extra.onWarningContainer),
        ClassAccentColors(scheme.error, scheme.errorContainer, scheme.onErrorContainer)
    )
    return palette[(classId.hashCode().absoluteValue) % palette.size]
}

@Composable
fun classCardBrush(
    classId: Long,
    nested: Boolean = false
): Brush {
    val accent = classAccentColors(classId)
    val isDark = isSystemInDarkTheme()
    val base = when {
        nested && isDark -> MaterialTheme.colorScheme.surfaceContainerHighest
        nested -> MaterialTheme.colorScheme.surfaceContainerHigh
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surface
    }
    val gradientColors = if (nested) {
        val startStrength = if (isDark) 0.28f else 0.34f
        val endStrength = if (isDark) 0.58f else 0.64f
        listOf(
            lerp(base, accent.container, startStrength),
            lerp(base, accent.container, endStrength)
        )
    } else {
        listOf(
            base,
            lerp(base, accent.container, if (isDark) 0.18f else 0.2f)
        )
    }
    return Brush.linearGradient(
        colors = gradientColors
    )
}

@Composable
fun ClassAccentBadge(
    classId: Long,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Groups,
    size: Dp = 52.dp
) {
    val colors = classAccentColors(classId)
    RollCallSurface(
        modifier = modifier.size(size),
        shape = MaterialTheme.shapes.medium,
        color = colors.container
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onContainer,
                modifier = Modifier
                    .padding(12.dp)
                    .size(size * 0.5f)
            )
        }
    }
}
