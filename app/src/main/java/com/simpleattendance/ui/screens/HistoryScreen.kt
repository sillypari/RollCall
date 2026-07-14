package com.simpleattendance.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.ui.components.EmptyState
import com.simpleattendance.ui.components.RollCallSurface
import com.simpleattendance.ui.history.HistoryViewModel
import com.simpleattendance.ui.history.SessionWithClass
import com.simpleattendance.ui.theme.RCError
import com.simpleattendance.ui.theme.RollCallSpacing
import com.simpleattendance.ui.theme.Success
import com.simpleattendance.ui.theme.SurfaceContainer
import com.simpleattendance.ui.theme.Warning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onSessionClick: (AttendanceSessionEntity) -> Unit,
    onSessionDeleteClick: (AttendanceSessionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Keep track of which date headers are expanded.
    // Default today to expanded.
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = modifier.fillMaxSize()) {
        // ── Filter Chips Row ─────────────────────────────────────────────────
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = RollCallSpacing.sm),
            contentPadding = PaddingValues(horizontal = RollCallSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(RollCallSpacing.sm)
        ) {
            item {
                FilterChip(
                    selected = state.selectedClassId == null,
                    onClick = { viewModel.filterByClass(null) },
                    label = { Text("All Classes") }
                )
            }
            items(state.classes, key = { it.id }) { classEntity ->
                FilterChip(
                    selected = state.selectedClassId == classEntity.id,
                    onClick = { viewModel.filterByClass(classEntity.id) },
                    label = { Text(classEntity.displayName) }
                )
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.isEmpty) {
                EmptyState(
                    icon = Icons.Rounded.History,
                    title = "No Attendance History",
                    message = "Complete a class session to view records"
                )
            } else {
                // Group sessions by date
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val groupedSessions = remember(state.sessions) {
                    state.sessions.groupBy { session ->
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = session.session.date
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        cal.timeInMillis
                    }.toSortedMap(compareByDescending { it })
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = RollCallSpacing.screenHorizontal,
                        vertical = RollCallSpacing.sm
                    ),
                    verticalArrangement = Arrangement.spacedBy(RollCallSpacing.md)
                ) {
                    groupedSessions.forEach { (dateMillis, sessionsForDate) ->
                        val isToday = dateMillis == today
                        val dateKey = dateFormat.format(Date(dateMillis))
                        val dateLabel = if (isToday) "Today" else dateKey

                        // Default expanded state: Expand today, collapse others on first load
                        if (!expandedStates.containsKey(dateKey)) {
                            expandedStates[dateKey] = isToday
                        }
                        val isExpanded = expandedStates[dateKey] ?: false

                        item(key = dateKey) {
                            HistoryDateHeader(
                                label = dateLabel,
                                sessionCount = sessionsForDate.size,
                                isExpanded = isExpanded,
                                onClick = { expandedStates[dateKey] = !isExpanded }
                            )
                        }

                        if (isExpanded) {
                            items(sessionsForDate, key = { it.session.id }) { sessionWithClass ->
                                HistorySessionCard(
                                    sessionWithClass = sessionWithClass,
                                    onClick = { onSessionClick(sessionWithClass.session) },
                                    onDeleteClick = { onSessionDeleteClick(sessionWithClass.session) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryDateHeader(
    label: String,
    sessionCount: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "header_arrow_rotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = RollCallSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(RollCallSpacing.sm))
            Text(
                text = "$sessionCount session${if (sessionCount > 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = if (isExpanded) "Collapse date" else "Expand date",
            modifier = Modifier.rotate(rotation)
        )
    }
}

@Composable
private fun HistorySessionCard(
    sessionWithClass: SessionWithClass,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = sessionWithClass.session
    val classEntity = sessionWithClass.classEntity

    val rate = session.percentage
    val rateColor = when {
        rate >= 80f -> Success
        rate >= 60f -> Warning
        else -> RCError
    }

    RollCallSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = RollCallSpacing.xs),
        shape = MaterialTheme.shapes.large,
        color = SurfaceContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Side performance indicator strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(84.dp)
                    .background(rateColor)
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = RollCallSpacing.lg)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = classEntity?.fullDisplayName ?: "Unknown Class",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = timeFormat.format(Date(session.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(RollCallSpacing.xs))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(RollCallSpacing.md)
                    ) {
                        Text(
                            text = "P: ${session.presentCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Success
                        )
                        Text(
                            text = "A: ${session.absentCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RCError
                        )
                    }
                }

                Spacer(Modifier.width(RollCallSpacing.md))

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format("%.0f%%", rate),
                        style = MaterialTheme.typography.titleMedium,
                        color = rateColor
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete session",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
