package com.pesky.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pesky.app.ui.theme.PeskyColors

/**
 * Apple Maps-style bottom navigation bar.
 */
@Composable
fun PeskyBottomNavigation(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = PeskyColors.BackgroundPrimary,
        contentColor = PeskyColors.NavItemInactive,
        tonalElevation = 0.dp
    ) {
        BottomNavTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PeskyColors.NavItemActive,
                    selectedTextColor = PeskyColors.NavItemActive,
                    unselectedIconColor = PeskyColors.NavItemInactive,
                    unselectedTextColor = PeskyColors.NavItemInactive,
                    indicatorColor = PeskyColors.AccentBlueSubtle
                )
            )
        }
    }
}

/**
 * Bottom navigation tabs.
 */
enum class BottomNavTab(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ALL(
        label = "All",
        icon = Icons.Filled.Key,
        selectedIcon = Icons.Filled.Key
    ),
    GROUPS(
        label = "Groups",
        icon = Icons.Filled.Folder,
        selectedIcon = Icons.Filled.Folder
    ),
    FAVORITES(
        label = "Favorites",
        icon = Icons.Filled.StarOutline,
        selectedIcon = Icons.Filled.Star
    ),
    SETTINGS(
        label = "Settings",
        icon = Icons.Filled.Settings,
        selectedIcon = Icons.Filled.Settings
    )
}

/**
 * Toast notification that slides up from the bottom.
 */
@Composable
fun PeskyToast(
    visible: Boolean,
    message: String,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.slideInVertically(
            initialOffsetY = { it }
        ) + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.slideOutVertically(
            targetOffsetY = { it }
        ) + androidx.compose.animation.fadeOut(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = PeskyColors.BackgroundTertiary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PeskyColors.TextPrimary
                )
            }
        }
    }
}

/**
 * Empty state component with Apple-style illustration placeholder.
 */
@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides PeskyColors.IconTertiary
            ) {
                icon()
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = PeskyColors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = PeskyColors.TextTertiary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

/**
 * Password visibility toggle with animation.
 */
@Composable
fun PasswordVisibilityToggle(
    isVisible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 180f else 0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "eye_rotation"
    )
    
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            contentDescription = if (isVisible) "Hide password" else "Show password",
            tint = PeskyColors.IconSecondary,
            modifier = Modifier.graphicsLayer { 
                rotationY = rotation 
            }
        )
    }
}
