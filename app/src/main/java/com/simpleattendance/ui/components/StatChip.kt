package com.simpleattendance.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.simpleattendance.ui.theme.RollCallMotion
import com.simpleattendance.ui.theme.RollCallShapes
import com.simpleattendance.ui.theme.RollCallSpacing
import com.simpleattendance.ui.theme.SurfaceContainerHigh

/**
 * Compact stat chip with an animated rolling number.
 * Used in attendance headers and report summaries.
 */
@Composable
fun StatChip(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceContainerHigh,
    countColor: Color = MaterialTheme.colorScheme.onSurface
) {
    RollCallSurface(
        modifier = modifier,
        shape = RollCallShapes.small,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = RollCallSpacing.lg,
                vertical = RollCallSpacing.sm
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { -it } + fadeIn(tween(RollCallMotion.Fast))) togetherWith
                                (slideOutVertically { it } + fadeOut(tween(RollCallMotion.Fast)))
                    } else {
                        (slideInVertically { it } + fadeIn(tween(RollCallMotion.Fast))) togetherWith
                                (slideOutVertically { -it } + fadeOut(tween(RollCallMotion.Fast)))
                    }.using(SizeTransform(clip = false))
                },
                label = "stat_count_$label"
            ) { targetCount ->
                Text(
                    text = targetCount.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = countColor,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
