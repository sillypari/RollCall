package com.simpleattendance.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.ui.theme.Primary
import com.simpleattendance.ui.theme.PrimaryContainer
import com.simpleattendance.ui.theme.RollCallMotion
import com.simpleattendance.ui.theme.RollCallSpacing
import com.simpleattendance.ui.theme.SurfaceContainer

/**
 * Class card — solid tonal surface with subject accent.
 * No permanent border; pressed state adds tonal lift via scale.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassCard(
    classEntity: ClassEntity,
    studentCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) RollCallMotion.PressedScale else RollCallMotion.NormalScale,
        animationSpec = RollCallMotion.SnapSpring,
        label = "class_card_scale"
    )

    val cardDescription = "${classEntity.subject} — ${classEntity.branch} Sem ${classEntity.semester} Sec ${classEntity.section}, $studentCount students"

    RollCallSurface(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .semantics { contentDescription = cardDescription }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.large,
        color = SurfaceContainer
    ) {
        Row(
            modifier = Modifier
                .padding(RollCallSpacing.lg)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container accent
            RollCallSurface(
                modifier = Modifier.size(52.dp),
                shape = MaterialTheme.shapes.medium,
                color = PrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.Groups,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(28.dp)
                )
            }

            Spacer(Modifier.width(RollCallSpacing.lg))

            // Class details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = classEntity.subject,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${classEntity.branch} | Sem ${classEntity.semester} | Sec ${classEntity.section}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(RollCallSpacing.xs))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "$studentCount students",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Overflow menu button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(RollCallSpacing.minTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Class options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
