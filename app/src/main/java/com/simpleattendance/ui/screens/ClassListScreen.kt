package com.simpleattendance.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.ui.classlist.ClassGroup
import com.simpleattendance.ui.classlist.ClassListViewModel
import com.simpleattendance.ui.components.ClassCard
import com.simpleattendance.ui.components.EmptyState
import com.simpleattendance.ui.components.LaunchOrigin
import com.simpleattendance.ui.components.RollCallSurface
import com.simpleattendance.ui.components.classCardBrush
import com.simpleattendance.ui.theme.RollCallMotion
import com.simpleattendance.ui.theme.RollCallSpacing

/**
 * Compose ClassList screen — Phase D.
 * Displays all classes grouped by batch. Single subjects render directly
 * without group expand/collapse, matching design directives.
 */
@Composable
fun ClassListScreen(
    viewModel: ClassListViewModel,
    onClassClick: (ClassEntity, LaunchOrigin) -> Unit,
    onClassOptionsClick: (ClassEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.isEmpty) {
            EmptyState(
                icon = Icons.Rounded.School,
                title = "No Classes Yet",
                message = "Create a class to start tracking attendance"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = RollCallSpacing.screenHorizontal,
                    top = RollCallSpacing.xl,
                    end = RollCallSpacing.screenHorizontal,
                    bottom = 104.dp
                ),
                verticalArrangement = Arrangement.spacedBy(RollCallSpacing.md)
            ) {
                items(state.groupedClasses, key = { it.batchKey }) { group ->
                    if (group.classes.size == 1) {
                        val classEntity = group.classes.first()
                        ClassCard(
                            classEntity = classEntity,
                            studentCount = state.studentCounts[classEntity.id] ?: 0,
                            onClick = { origin -> onClassClick(classEntity, origin) },
                            onLongClick = { onClassOptionsClick(classEntity) },
                            onMenuClick = { onClassOptionsClick(classEntity) }
                        )
                    } else {
                        ClassGroupSection(
                            group = group,
                            onHeaderClick = { viewModel.toggleGroupExpansion(group.batchKey) },
                            onClassClick = onClassClick,
                            onClassOptionsClick = onClassOptionsClick,
                            studentCounts = state.studentCounts
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassGroupSection(
    group: ClassGroup,
    onHeaderClick: () -> Unit,
    onClassClick: (ClassEntity, LaunchOrigin) -> Unit,
    onClassOptionsClick: (ClassEntity) -> Unit,
    studentCounts: Map<Long, Int>,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (group.isExpanded) 180f else 0f,
        animationSpec = RollCallMotion.standardTween(),
        label = "arrow_rotation"
    )
    val isDark = isSystemInDarkTheme()
    val groupBase = if (isDark) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surface
    }

    RollCallSurface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f),
                shape = MaterialTheme.shapes.large
            ),
        shape = MaterialTheme.shapes.large,
        brush = Brush.linearGradient(
            listOf(
                groupBase,
                lerp(
                    groupBase,
                    MaterialTheme.colorScheme.primaryContainer,
                    if (isDark) 0.24f else 0.1f
                )
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = RollCallSpacing.minTouchTarget)
                    .clickable { onHeaderClick() }
                    .padding(RollCallSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RollCallSurface(
                        modifier = Modifier.size(44.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = if (group.isExpanded) {
                                Icons.Rounded.FolderOpen
                            } else {
                                Icons.Rounded.Folder
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(23.dp)
                        )
                    }
                    Spacer(Modifier.width(RollCallSpacing.md))
                    Column {
                        Text(
                            text = group.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${group.classes.size} subjects",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = if (group.isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = group.isExpanded,
                enter = expandVertically(
                    animationSpec = RollCallMotion.standardTween(),
                    expandFrom = Alignment.Top
                ) + fadeIn(RollCallMotion.fastTween()),
                exit = shrinkVertically(
                    animationSpec = RollCallMotion.standardTween(),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(RollCallMotion.fastTween())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RollCallSpacing.lg)
                        .padding(bottom = RollCallSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(RollCallSpacing.sm)
                ) {
                    group.classes.forEach { classEntity ->
                        ClassCard(
                            classEntity = classEntity,
                            studentCount = studentCounts[classEntity.id] ?: 0,
                            onClick = { origin -> onClassClick(classEntity, origin) },
                            onLongClick = { onClassOptionsClick(classEntity) },
                            onMenuClick = { onClassOptionsClick(classEntity) },
                            showBorder = false
                        )
                    }
                }
            }
        }
    }
}
