package com.pesky.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import android.view.HapticFeedbackConstants
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val listState = rememberLazyListState()
    
    // Selected tab for bottom navigation
    var selectedTab by remember { mutableStateOf(BottomNavTab.ALL) }
    
    // Action sheet state
    var showActionSheet by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<PasswordEntry?>(null) }
    
    // Toast state
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    
    // Loading state simulation (for shimmer demo)
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1500) // Simulate loading
        isLoading = false
    }
    
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
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
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
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PeskyColors.SidebarBackground
            ) {
                SidebarNavigation(
                    selectedItem = uiState.selectedSidebarItem,
                    onItemSelected = { item ->
                        viewModel.selectSidebarItem(item)
                        scope.launch { drawerState.close() }
                    },
                    itemCounts = uiState.sidebarCounts,
                    groups = uiState.groups,
                    onAddCategory = { /* TODO: Add category dialog */ }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PeskyColors.BackgroundPrimary)
        ) {
            // Main content
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Floating Search Bar with parallax
                FloatingSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = { /* Search is reactive */ },
                    modifier = Modifier
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
                
                // Content area with crossfade
                Crossfade(
                    targetState = isLoading,
                    animationSpec = tween(300),
                    label = "content_crossfade"
                ) { loading ->
                    if (loading) {
                        // Shimmer loading skeleton
                        VaultLoadingSkeleton(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp)
                        )
                    } else if (uiState.entries.isEmpty()) {
                        // Apple-style empty state
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
                        // Entry list with staggered animation
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
                            ) { index, entry ->
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
                            
                            // Bottom padding for FAB and bottom nav
                            item {
                                Spacer(modifier = Modifier.height(100.dp))
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
        }
    }
}
