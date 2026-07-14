package com.simpleattendance.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.ui.classlist.ClassGroup
import com.simpleattendance.ui.classlist.ClassListViewModel
import com.simpleattendance.ui.components.ClassCard
import com.simpleattendance.ui.components.EmptyState
import com.simpleattendance.ui.theme.RollCallSpacing

/**
 * Compose ClassList screen — Phase D.
 * Displays all classes grouped by batch. Single subjects render directly
 * without group expand/collapse, matching design directives.
 */
@Composable
fun ClassListScreen(
    viewModel: ClassListViewModel,
    onClassClick: (ClassEntity) -> Unit,
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
                    horizontal = RollCallSpacing.screenHorizontal,
                    vertical = RollCallSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(RollCallSpacing.md)
            ) {
                items(state.groupedClasses, key = { it.batchKey }) { group ->
                    if (group.classes.size == 1) {
                        val classEntity = group.classes.first()
                        ClassCard(
                            classEntity = classEntity,
                            studentCount = 0, // Count is not displayed on main list per original layout
                            onClick = { onClassClick(classEntity) },
                            onLongClick = { onClassOptionsClick(classEntity) },
                            onMenuClick = { onClassOptionsClick(classEntity) }
                        )
                    } else {
                        ClassGroupSection(
                            group = group,
                            onHeaderClick = { viewModel.toggleGroupExpansion(group.batchKey) },
                            onClassClick = onClassClick,
                            onClassOptionsClick = onClassOptionsClick
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
    onClassClick: (ClassEntity) -> Unit,
    onClassOptionsClick: (ClassEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (group.isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(RollCallSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = group.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${group.classes.size} subjects",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = if (group.isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = group.isExpanded) {
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
                            studentCount = 0,
                            onClick = { onClassClick(classEntity) },
                            onLongClick = { onClassOptionsClick(classEntity) },
                            onMenuClick = { onClassOptionsClick(classEntity) }
                        )
                    }
                }
            }
        }
    }
}
