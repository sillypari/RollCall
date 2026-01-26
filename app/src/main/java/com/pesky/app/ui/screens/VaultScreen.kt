package com.pesky.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.pesky.app.data.models.PasswordEntry
import com.pesky.app.ui.components.*
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.viewmodels.VaultEvent
import com.pesky.app.viewmodels.VaultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main vault screen showing password entries with Apple Maps-inspired UI.
 * Features floating search, shimmer loading, action sheets, and smooth animations.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    onNavigateToEntry: (String?) -> Unit,
    onNavigateToSettings: () -> Unit,
    onVaultLocked: () -> Unit,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val haptics = LocalPeskyHaptics.current
    val listState = rememberLazyListState()
    
    // Selected tab for bottom navigation
    var selectedTab by remember { mutableStateOf(BottomNavTab.ALL) }
    
    // Action sheet state
    var showActionSheet by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<PasswordEntry?>(null) }
    
    // Toast state
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    
    // Create group dialog
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    
    // No artificial loading delay - show content immediately
    val isLoading = false
    
    // Search focus state
    var isSearchFocused by remember { mutableStateOf(false) }
    
    // Calculate scroll progress for parallax effects
    val scrollOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else listState.firstVisibleItemScrollOffset / 200f
        }
    }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is VaultEvent.VaultLocked -> onVaultLocked()
                is VaultEvent.PasswordCopied -> {
                    toastMessage = "Password copied to clipboard"
                    showToast = true
                    haptics.success()
                    delay(2000)
                    showToast = false
                }
                is VaultEvent.EntryDeleted -> {
                    toastMessage = "Entry deleted"
                    showToast = true
                    delay(2000)
                    showToast = false
                }
                is VaultEvent.Error -> {
                    toastMessage = event.message
                    showToast = true
                    delay(3000)
                    showToast = false
                }
                is VaultEvent.GroupCreated -> {
                    toastMessage = "Group '${event.name}' created"
                    showToast = true
                    delay(2000)
                    showToast = false
                }
                else -> {}
            }
        }
    }
    
    // Entry Action Sheet
    EntryActionSheet(
        visible = showActionSheet && selectedEntry != null,
        entryTitle = selectedEntry?.title ?: "",
        onDismiss = { 
            showActionSheet = false
            selectedEntry = null
        },
        onEdit = { 
            selectedEntry?.let { onNavigateToEntry(it.uuid) }
            showActionSheet = false
            selectedEntry = null
        },
        onCopyPassword = {
            selectedEntry?.let { viewModel.copyPassword(it) }
            showActionSheet = false
            selectedEntry = null
        },
        onCopyUsername = {
            // TODO: Copy username
            showActionSheet = false
            selectedEntry = null
        },
        onDelete = {
            // TODO: Delete with confirmation
            showActionSheet = false
            selectedEntry = null
        }
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PeskyColors.BackgroundPrimary)
    ) {
        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search bar (smart search supports keywords like "weak", "duplicate", "expiring")
            FloatingSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = { /* Search is reactive */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp)
                    .graphicsLayer {
                        translationY = -scrollOffset.coerceIn(0f, 1f) * 20f
                        this.alpha = 1f - (scrollOffset * 0.3f).coerceIn(0f, 0.3f)
                    }
                    .zIndex(10f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Category pills row
            AnimatedVisibility(
                visible = !isSearchFocused,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillButton(
                        text = "All",
                        selected = selectedTab == BottomNavTab.ALL,
                        onClick = { selectedTab = BottomNavTab.ALL }
                    )
                    PillButton(
                        text = "Favorites",
                        selected = selectedTab == BottomNavTab.FAVORITES,
                        onClick = { selectedTab = BottomNavTab.FAVORITES },
                        icon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    PillButton(
                        text = "Groups",
                        selected = selectedTab == BottomNavTab.GROUPS,
                        onClick = { selectedTab = BottomNavTab.GROUPS },
                        icon = { Icon(Icons.Filled.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Content based on selected tab
            when (selectedTab) {
                BottomNavTab.GROUPS -> {
                    // Groups view
                    GroupsView(
                        groups = uiState.groups,
                        selectedGroupUuid = uiState.selectedGroupUuid,
                        entries = uiState.entries,
                        onGroupClick = { groupUuid ->
                            viewModel.selectGroup(groupUuid)
                        },
                        onBackToGroups = {
                            viewModel.selectGroup(null)
                        },
                        onCreateGroup = { showCreateGroupDialog = true },
                        onEntryClick = { onNavigateToEntry(it.uuid) },
                        onCopyPassword = { viewModel.copyPassword(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it.uuid) },
                        getPasswordStrength = { viewModel.getPasswordStrength(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp)
                    )
                }
                BottomNavTab.FAVORITES -> {
                    // Favorites - filter entries
                    val favoriteEntries = uiState.entries.filter { it.isFavorite }
                    if (favoriteEntries.isEmpty()) {
                        EmptyState(
                            icon = { Icon(Icons.Filled.StarOutline, contentDescription = null, modifier = Modifier.size(64.dp)) },
                            title = "No Favorites Yet",
                            subtitle = "Star your most used entries for quick access",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp)
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            itemsIndexed(
                                items = favoriteEntries,
                                key = { _, entry -> entry.uuid }
                            ) { _, entry ->
                                PasswordEntryCard(
                                    entry = entry,
                                    passwordStrength = viewModel.getPasswordStrength(entry.password),
                                    onClick = { onNavigateToEntry(entry.uuid) },
                                    onCopyPassword = { viewModel.copyPassword(entry) },
                                    onToggleFavorite = { viewModel.toggleFavorite(entry.uuid) },
                                    onLongPress = {
                                        selectedEntry = entry
                                        showActionSheet = true
                                    },
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
                else -> {
                    // All entries - default view
                    Crossfade(
                        targetState = isLoading,
                        animationSpec = tween(300),
                        label = "content_crossfade"
                    ) { loading ->
                        if (loading) {
                            VaultLoadingSkeleton(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 80.dp)
                            )
                        } else if (uiState.entries.isEmpty()) {
                            EmptyState(
                                icon = { Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(64.dp)) },
                                title = "No Passwords Yet",
                                subtitle = "Add your first password entry to get started with secure storage",
                                action = {
                                    Button(onClick = { onNavigateToEntry(null) }) {
                                        Text("Add Entry")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 80.dp)
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 80.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                itemsIndexed(
                                    items = uiState.entries,
                                    key = { _, entry -> entry.uuid }
                                ) { _, entry ->
                                    PasswordEntryCard(
                                        entry = entry,
                                        passwordStrength = viewModel.getPasswordStrength(entry.password),
                                        onClick = { onNavigateToEntry(entry.uuid) },
                                        onCopyPassword = { viewModel.copyPassword(entry) },
                                        onToggleFavorite = { viewModel.toggleFavorite(entry.uuid) },
                                        onLongPress = {
                                            selectedEntry = entry
                                            showActionSheet = true
                                        },
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(100.dp)) }
                            }
                        }
                    }
                }
            }
        }
        
        // Floating Action Button with gradient
        PeskyFAB(
            onClick = { onNavigateToEntry(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
        )
        
        // Bottom Navigation Bar
        PeskyBottomNavigation(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
                when (tab) {
                    BottomNavTab.SETTINGS -> onNavigateToSettings()
                    else -> {}
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Toast notification
        PeskyToast(
            visible = showToast,
            message = toastMessage,
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PeskyColors.Success) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
        
        // Create Group Dialog
        if (showCreateGroupDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateGroupDialog = false },
                onCreate = { name ->
                    viewModel.createGroup(name)
                    showCreateGroupDialog = false
                }
            )
        }
    }
}

/**
 * Groups view showing list of groups or entries within a selected group.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupsView(
    groups: List<GroupItem>,
    selectedGroupUuid: String?,
    entries: List<PasswordEntry>,
    onGroupClick: (String) -> Unit,
    onBackToGroups: () -> Unit,
    onCreateGroup: () -> Unit,
    onEntryClick: (PasswordEntry) -> Unit,
    onCopyPassword: (PasswordEntry) -> Unit,
    onToggleFavorite: (PasswordEntry) -> Unit,
    getPasswordStrength: (String) -> com.pesky.app.data.models.PasswordStrength,
    modifier: Modifier = Modifier
) {
    if (selectedGroupUuid != null) {
        // Show entries within the selected group
        val selectedGroup = groups.find { it.uuid == selectedGroupUuid }
        Column(modifier = modifier) {
            // Back button and group name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackToGroups() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PeskyColors.AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedGroup?.name ?: "Group",
                    style = MaterialTheme.typography.titleMedium,
                    color = PeskyColors.TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${entries.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = PeskyColors.TextSecondary
                )
            }
            
            if (entries.isEmpty()) {
                EmptyState(
                    icon = { Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(64.dp)) },
                    title = "Empty Group",
                    subtitle = "Add entries to this group when creating or editing passwords",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(
                        items = entries,
                        key = { _, entry -> entry.uuid }
                    ) { _, entry ->
                        PasswordEntryCard(
                            entry = entry,
                            passwordStrength = getPasswordStrength(entry.password),
                            onClick = { onEntryClick(entry) },
                            onCopyPassword = { onCopyPassword(entry) },
                            onToggleFavorite = { onToggleFavorite(entry) },
                            onLongPress = { },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    } else {
        // Show list of groups
        if (groups.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Filled.CreateNewFolder, contentDescription = null, modifier = Modifier.size(64.dp)) },
                title = "No Groups Yet",
                subtitle = "Create groups to organize your passwords",
                action = {
                    Button(onClick = onCreateGroup) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Group")
                    }
                },
                modifier = modifier
            )
        } else {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Create new group button
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCreateGroup() },
                        colors = CardDefaults.cardColors(containerColor = PeskyColors.CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = PeskyColors.AccentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Create New Group",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PeskyColors.AccentBlue
                            )
                        }
                    }
                }
                
                // Group list
                itemsIndexed(groups) { _, group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGroupClick(group.uuid) },
                        colors = CardDefaults.cardColors(containerColor = PeskyColors.CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Folder,
                                contentDescription = null,
                                tint = PeskyColors.AccentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PeskyColors.TextPrimary
                                )
                                Text(
                                    text = "${group.count} ${if (group.count == 1) "item" else "items"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PeskyColors.TextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = PeskyColors.IconSecondary
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

/**
 * Dialog for creating a new group.
 */
@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Group") },
        text = {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                placeholder = { Text("e.g., Work, Personal, Banking") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(groupName) },
                enabled = groupName.isNotBlank()
            ) {
                Text("Create", color = PeskyColors.AccentBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = PeskyColors.CardBackground,
        titleContentColor = PeskyColors.TextPrimary,
        textContentColor = PeskyColors.TextSecondary
    )
}
