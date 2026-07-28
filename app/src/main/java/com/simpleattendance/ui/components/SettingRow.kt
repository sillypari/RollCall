package com.simpleattendance.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.simpleattendance.ui.theme.PillShape
import com.simpleattendance.ui.theme.RollCallMotion
import com.simpleattendance.ui.theme.RollCallSpacing

@Composable
fun SwitchSettingRow(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RollCallSpacing.minTouchTarget)
            .semantics { role = Role.Switch }
            .padding(horizontal = RollCallSpacing.lg, vertical = RollCallSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            if (description != null) {
                Spacer(Modifier.height(RollCallSpacing.xs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(RollCallSpacing.lg))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
fun InfoSettingRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RollCallSpacing.minTouchTarget)
            .padding(horizontal = RollCallSpacing.lg, vertical = RollCallSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SegmentedSettingRow(
    title: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RollCallSpacing.lg, vertical = RollCallSpacing.md)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        if (description != null) {
            Spacer(Modifier.height(RollCallSpacing.xs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(RollCallSpacing.md))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(RollCallSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(RollCallSpacing.xs)
        ) {
            options.forEach { (label, value) ->
                val isSelected = value == selectedValue
                val fill by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                    animationSpec = RollCallMotion.standardTween(),
                    label = "setting_segment_fill"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = RollCallMotion.fastTween(),
                    label = "setting_segment_content"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.98f,
                    animationSpec = RollCallMotion.StandardSpring,
                    label = "setting_segment_scale"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                        .clip(PillShape)
                        .background(fill)
                        .clickable(
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onValueSelected(value) }
                        )
                        .heightIn(min = RollCallSpacing.minTouchTarget)
                        .padding(horizontal = RollCallSpacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
