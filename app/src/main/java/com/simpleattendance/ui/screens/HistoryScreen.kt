package com.simpleattendance.ui.screens

import android.app.DatePickerDialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.ui.components.ConfirmationDialog
import com.simpleattendance.ui.components.ClassAccentBadge
import com.simpleattendance.ui.components.EmptyState
import com.simpleattendance.ui.components.LaunchOrigin
import com.simpleattendance.ui.components.RollCallSurface
import com.simpleattendance.ui.components.classCardBrush
import com.simpleattendance.ui.components.captureLaunchOrigin
import com.simpleattendance.ui.history.HistoryViewModel
import com.simpleattendance.ui.history.SessionWithClass
import com.simpleattendance.ui.theme.RollCallMotion
import com.simpleattendance.ui.theme.RollCallSpacing
import com.simpleattendance.ui.theme.rollCallColors
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class HistoryContentState {
    Loading,
    Empty,
    FilterEmpty,
    Content
}

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onSessionClick: (AttendanceSessionEntity, LaunchOrigin) -> Unit,
    onSessionDeleteClick: (AttendanceSessionEntity) -> Unit,
    onSearchFocusChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val collapsedDateKeys = remember { mutableStateListOf<Long>() }
    var pendingDeletion by remember { mutableStateOf<AttendanceSessionEntity?>(null) }

    val locale = Locale.getDefault()
    val dateFormatter = remember(locale) { SimpleDateFormat("EEEE, d MMM", locale) }
    val timeFormatter = remember(locale) {
        DateFormat.getTimeInstance(DateFormat.SHORT, locale)
    }
    val groupedSessions = remember(state.sessions) {
        state.sessions
            .groupBy { startOfDay(it.session.date) }
            .toSortedMap(compareByDescending { it })
    }

    val contentState = when {
        state.isLoading -> HistoryContentState.Loading
        state.isEmpty && state.hasAnySessions -> HistoryContentState.FilterEmpty
        state.isEmpty -> HistoryContentState.Empty
        else -> HistoryContentState.Content
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = !state.isLoading && state.hasAnySessions,
            enter = fadeIn(RollCallMotion.standardTween()),
            exit = fadeOut(RollCallMotion.fastTween())
        ) {
            Column {
                if (state.sessions.isNotEmpty()) {
                    HistoryArchiveSummary(
                        sessions = state.sessions,
                        modifier = Modifier.padding(
                            start = RollCallSpacing.screenHorizontal,
                            top = RollCallSpacing.xl,
                            end = RollCallSpacing.screenHorizontal
                        )
                    )
                }
                HistorySearchAndDateRange(
                    query = state.searchQuery,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onQueryChange = viewModel::setSearchQuery,
                    onStartDateSelected = viewModel::setStartDate,
                    onEndDateSelected = viewModel::setEndDate,
                    onClearDateRange = viewModel::clearDateRange,
                    onSearchFocusChanged = onSearchFocusChanged
                )
                HistoryFilters(
                    classes = state.classes,
                    selectedClassId = state.selectedClassId,
                    onClassSelected = viewModel::filterByClass
                )
            }
        }

        AnimatedContent(
            targetState = contentState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            transitionSpec = {
                fadeIn(RollCallMotion.standardTween()) togetherWith
                    fadeOut(RollCallMotion.fastTween())
            },
            label = "history_content_state"
        ) { targetState ->
            when (targetState) {
                HistoryContentState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }

                HistoryContentState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Rounded.History,
                            title = "No attendance history",
                            message = "Completed sessions will appear here"
                        )
                    }
                }

                HistoryContentState.FilterEmpty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Rounded.History,
                            title = "No matching sessions",
                            message = "Try changing the search, class, or date range",
                            action = {
                                TextButton(onClick = viewModel::clearFilters) {
                                    Text("Clear filters")
                                }
                            }
                        )
                    }
                }

                HistoryContentState.Content -> {
                    HistoryTimeline(
                        groupedSessions = groupedSessions,
                        collapsedDateKeys = collapsedDateKeys,
                        dateFormatter = dateFormatter,
                        timeFormatter = timeFormatter,
                        onSessionClick = onSessionClick,
                        onSessionDeleteClick = { pendingDeletion = it }
                    )
                }
            }
        }
    }

    pendingDeletion?.let { session ->
        ConfirmationDialog(
            title = "Delete session?",
            message = "This attendance session and its student records will be permanently deleted.",
            confirmText = "Delete",
            onConfirm = { onSessionDeleteClick(session) },
            onDismiss = { pendingDeletion = null },
            isDestructive = true
        )
    }
}

@Composable
private fun HistoryArchiveSummary(
    sessions: List<SessionWithClass>,
    modifier: Modifier = Modifier
) {
    val totalStudents = sessions.sumOf { it.session.totalCount }
    val totalPresent = sessions.sumOf { it.session.presentCount }
    val averageRate = if (totalStudents == 0) {
        0
    } else {
        ((totalPresent.toFloat() / totalStudents) * 100f).toInt()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
                shape = MaterialTheme.shapes.large
            )
            .padding(RollCallSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RollCallSurface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }

            Spacer(Modifier.width(RollCallSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${sessions.size} session${if (sessions.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Saved attendance archive",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$averageRate%",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "overall",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistorySearchAndDateRange(
    query: String,
    startDate: Long?,
    endDate: Long?,
    onQueryChange: (String) -> Unit,
    onStartDateSelected: (Long) -> Unit,
    onEndDateSelected: (Long) -> Unit,
    onClearDateRange: () -> Unit,
    onSearchFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    var searchFocused by remember { mutableStateOf(false) }
    val locale = Locale.getDefault()
    val compactDate = remember(locale) { SimpleDateFormat("d MMM yyyy", locale) }
    val searchContainerColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    LaunchedEffect(imeVisible) {
        if (!imeVisible) {
            onSearchFocusChanged(false)
        } else if (searchFocused) {
            onSearchFocusChanged(true)
        }
    }

    fun showDatePicker(initialDate: Long?, onSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply {
            if (initialDate != null) timeInMillis = initialDate
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onSelected(
                    Calendar.getInstance().apply {
                        set(year, month, day)
                    }.timeInMillis
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = modifier.padding(
            start = RollCallSpacing.screenHorizontal,
            top = RollCallSpacing.md,
            end = RollCallSpacing.screenHorizontal
        ),
        verticalArrangement = Arrangement.spacedBy(RollCallSpacing.sm)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    searchFocused = it.isFocused
                    onSearchFocusChanged(it.isFocused)
                },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = searchContainerColor,
                unfocusedContainerColor = searchContainerColor,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            leadingIcon = {
                Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
            },
            trailingIcon = if (query.isNotBlank()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            } else {
                null
            },
            placeholder = { Text("Search subject, branch, or section") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RollCallSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateFilterButton(
                label = startDate?.let { "From ${compactDate.format(Date(it))}" } ?: "From date",
                selected = startDate != null,
                onClick = { showDatePicker(startDate, onStartDateSelected) },
                modifier = Modifier.weight(1f)
            )
            DateFilterButton(
                label = endDate?.let { "To ${compactDate.format(Date(it))}" } ?: "To date",
                selected = endDate != null,
                onClick = { showDatePicker(endDate, onEndDateSelected) },
                modifier = Modifier.weight(1f)
            )
            AnimatedVisibility(visible = startDate != null || endDate != null) {
                IconButton(onClick = onClearDateRange) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = "Clear date range"
                    )
                }
            }
        }
    }
}

@Composable
private fun DateFilterButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fill by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = RollCallMotion.fastTween(),
        label = "date_filter_fill"
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = RollCallMotion.fastTween(),
        label = "date_filter_content"
    )
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(fill)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = RollCallSpacing.minTouchTarget)
            .padding(horizontal = RollCallSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RollCallSpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Rounded.DateRange,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HistoryFilters(
    classes: List<ClassEntity>,
    selectedClassId: Long?,
    onClassSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = RollCallSpacing.screenHorizontal,
            vertical = RollCallSpacing.md
        ),
        horizontalArrangement = Arrangement.spacedBy(RollCallSpacing.sm)
    ) {
        item(key = "all_classes") {
            HistoryFilterPill(
                label = "All classes",
                selected = selectedClassId == null,
                onClick = { onClassSelected(null) }
            )
        }
        items(classes, key = { it.id }) { classEntity ->
            HistoryFilterPill(
                label = "${classEntity.subject} · ${classEntity.displayName}",
                selected = selectedClassId == classEntity.id,
                onClick = { onClassSelected(classEntity.id) }
            )
        }
    }
}

@Composable
private fun HistoryFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        animationSpec = RollCallMotion.fastTween(),
        label = "history_filter_color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = RollCallMotion.fastTween(),
        label = "history_filter_content_color"
    )

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = RollCallSpacing.minTouchTarget)
            .clip(CircleShape)
            .background(containerColor)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            }
            .clickable(onClick = onClick)
            .padding(horizontal = RollCallSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RollCallSpacing.sm)
    ) {
        AnimatedVisibility(visible = selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1
        )
    }
}

@Composable
private fun HistoryTimeline(
    groupedSessions: Map<Long, List<SessionWithClass>>,
    collapsedDateKeys: MutableList<Long>,
    dateFormatter: DateFormat,
    timeFormatter: DateFormat,
    onSessionClick: (AttendanceSessionEntity, LaunchOrigin) -> Unit,
    onSessionDeleteClick: (AttendanceSessionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = RollCallSpacing.screenHorizontal,
            end = RollCallSpacing.screenHorizontal,
            top = RollCallSpacing.xs,
            bottom = 104.dp
        ),
        verticalArrangement = Arrangement.spacedBy(RollCallSpacing.md)
    ) {
        groupedSessions.forEach { (dateMillis, sessionsForDate) ->
            item(key = "date_group_$dateMillis") {
                val isExpanded = dateMillis !in collapsedDateKeys

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = RollCallMotion.gentleSpring()
                        )
                ) {
                    HistoryDateHeader(
                        label = relativeDateLabel(dateMillis, dateFormatter),
                        sessionCount = sessionsForDate.size,
                        isExpanded = isExpanded,
                        onClick = {
                            if (isExpanded) {
                                collapsedDateKeys.add(dateMillis)
                            } else {
                                collapsedDateKeys.remove(dateMillis)
                            }
                        }
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
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
                            modifier = Modifier.padding(top = RollCallSpacing.xs),
                            verticalArrangement = Arrangement.spacedBy(RollCallSpacing.sm)
                        ) {
                            sessionsForDate.forEach { sessionWithClass ->
                                HistorySessionCard(
                                    sessionWithClass = sessionWithClass,
                                    timeFormatter = timeFormatter,
                                    onClick = onSessionClick,
                                    onDeleteClick = onSessionDeleteClick
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
        animationSpec = RollCallMotion.standardTween(),
        label = "history_group_rotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = RollCallSpacing.minTouchTarget)
            .clip(MaterialTheme.shapes.small)
            .semantics {
                stateDescription = if (isExpanded) "Expanded" else "Collapsed"
            }
            .clickable(onClick = onClick)
            .padding(horizontal = RollCallSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$sessionCount session${if (sessionCount == 1) "" else "s"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Rounded.ExpandMore,
            contentDescription = if (isExpanded) "Collapse date" else "Expand date",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .rotate(rotation)
        )
    }
}

@Composable
private fun HistorySessionCard(
    sessionWithClass: SessionWithClass,
    timeFormatter: DateFormat,
    onClick: (AttendanceSessionEntity, LaunchOrigin) -> Unit,
    onDeleteClick: (AttendanceSessionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val session = sessionWithClass.session
    val classEntity = sessionWithClass.classEntity
    val rate = session.percentage
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) RollCallMotion.PressedScale else RollCallMotion.NormalScale,
        animationSpec = RollCallMotion.SnapSpring,
        label = "history_card_scale"
    )
    var launchOrigin by remember { mutableStateOf(LaunchOrigin.Unspecified) }
    var menuExpanded by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    val extraColors = MaterialTheme.rollCallColors

    val rateColor: Color
    val rateContainer: Color
    when {
        rate >= 80f -> {
            rateColor = scheme.tertiary
            rateContainer = scheme.tertiaryContainer
        }
        rate >= 60f -> {
            rateColor = extraColors.warning
            rateContainer = extraColors.warningContainer
        }
        else -> {
            rateColor = scheme.error
            rateContainer = scheme.errorContainer
        }
    }

    RollCallSurface(
        modifier = modifier
            .captureLaunchOrigin { launchOrigin = it }
            .scale(scale)
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
                shape = MaterialTheme.shapes.large
            )
            .semantics { role = Role.Button }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick(session, launchOrigin)
        },
        shape = MaterialTheme.shapes.large,
        brush = classCardBrush(classEntity?.id ?: session.classId)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = RollCallSpacing.md,
                    top = RollCallSpacing.md,
                    bottom = RollCallSpacing.md
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClassAccentBadge(
                classId = classEntity?.id ?: session.classId,
                size = 44.dp
            )

            Spacer(Modifier.width(RollCallSpacing.md))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        end = RollCallSpacing.sm
                    )
            ) {
                Text(
                    text = classEntity?.subject ?: "Unknown subject",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildSessionMetadata(classEntity, session, timeFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(RollCallSpacing.sm))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(RollCallSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SessionStat(
                        value = session.presentCount,
                        label = "present",
                        valueColor = scheme.tertiary
                    )
                    SessionStat(
                        value = session.absentCount,
                        label = "absent",
                        valueColor = scheme.error
                    )
                }
            }

            RollCallSurface(
                shape = CircleShape,
                color = rateContainer
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%.0f%%", rate),
                    style = MaterialTheme.typography.labelLarge,
                    color = rateColor,
                    modifier = Modifier.padding(
                        horizontal = RollCallSpacing.md,
                        vertical = RollCallSpacing.sm
                    )
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Session options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete session", color = scheme.error) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = scheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick(session)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionStat(
    value: Int,
    label: String,
    valueColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun startOfDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun relativeDateLabel(
    dateMillis: Long,
    dateFormatter: DateFormat
): String {
    val today = startOfDay(System.currentTimeMillis())
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = today
        add(Calendar.DAY_OF_YEAR, -1)
    }.timeInMillis
    return when (dateMillis) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> dateFormatter.format(Date(dateMillis))
    }
}

private fun buildSessionMetadata(
    classEntity: ClassEntity?,
    session: AttendanceSessionEntity,
    timeFormatter: DateFormat
): String {
    val time = timeFormatter.format(Date(session.date))
    return if (classEntity == null) {
        time
    } else {
        "${classEntity.displayName} · $time"
    }
}
