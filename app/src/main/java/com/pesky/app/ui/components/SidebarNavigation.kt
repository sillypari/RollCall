package com.pesky.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesky.app.ui.theme.PeskyColors

/**
 * Sidebar navigation drawer with categories.
 */
@Composable
fun SidebarNavigation(
    selectedItem: SidebarItem,
    onItemSelected: (SidebarItem) -> Unit,
    itemCounts: Map<SidebarItem, Int>,
    groups: List<GroupItem>,
    onAddCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(PeskyColors.SidebarBackground)
            .padding(vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pesky",
                style = MaterialTheme.typography.displaySmall,
                color = PeskyColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = PeskyColors.Divider
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // Main items
            item {
                SidebarNavItem(
                    item = SidebarItem.AllItems,
                    isSelected = selectedItem == SidebarItem.AllItems,
                    count = itemCounts[SidebarItem.AllItems] ?: 0,
                    onClick = { onItemSelected(SidebarItem.AllItems) }
                )
            }
            
            item {
                SidebarNavItem(
                    item = SidebarItem.Favorites,
                    isSelected = selectedItem == SidebarItem.Favorites,
                    count = itemCounts[SidebarItem.Favorites] ?: 0,
                    onClick = { onItemSelected(SidebarItem.Favorites) }
                )
            }
            
            item {
                SidebarNavItem(
                    item = SidebarItem.RecentlyUsed,
                    isSelected = selectedItem == SidebarItem.RecentlyUsed,
                    count = itemCounts[SidebarItem.RecentlyUsed] ?: 0,
                    onClick = { onItemSelected(SidebarItem.RecentlyUsed) }
                )
            }
            
            // Divider
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = PeskyColors.Divider
                )
            }
            
            // Categories header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.labelLarge,
                        color = PeskyColors.TextSecondary
                    )
                    
                    IconButton(
                        onClick = onAddCategory,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add category",
                            tint = PeskyColors.AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Category items
            items(groups) { group ->
                SidebarGroupItem(
                    group = group,
                    isSelected = selectedItem is SidebarItem.Category && 
                                 (selectedItem as SidebarItem.Category).uuid == group.uuid,
                    onClick = { onItemSelected(SidebarItem.Category(group.uuid, group.name)) }
                )
            }
            
            // Divider
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = PeskyColors.Divider
                )
            }
            
            // Security items
            item {
                SidebarNavItem(
                    item = SidebarItem.WeakPasswords,
                    isSelected = selectedItem == SidebarItem.WeakPasswords,
                    count = itemCounts[SidebarItem.WeakPasswords] ?: 0,
                    onClick = { onItemSelected(SidebarItem.WeakPasswords) }
                )
            }
            
            item {
                SidebarNavItem(
                    item = SidebarItem.DuplicatePasswords,
                    isSelected = selectedItem == SidebarItem.DuplicatePasswords,
                    count = itemCounts[SidebarItem.DuplicatePasswords] ?: 0,
                    onClick = { onItemSelected(SidebarItem.DuplicatePasswords) }
                )
            }
            
            item {
                SidebarNavItem(
                    item = SidebarItem.ExpiringSoon,
                    isSelected = selectedItem == SidebarItem.ExpiringSoon,
                    count = itemCounts[SidebarItem.ExpiringSoon] ?: 0,
                    onClick = { onItemSelected(SidebarItem.ExpiringSoon) }
                )
            }
            
            // Divider
            item {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = PeskyColors.Divider
                )
            }
            
            item {
                SidebarNavItem(
                    item = SidebarItem.SecureNotes,
                    isSelected = selectedItem == SidebarItem.SecureNotes,
                    count = itemCounts[SidebarItem.SecureNotes] ?: 0,
                    onClick = { onItemSelected(SidebarItem.SecureNotes) }
                )
            }
            
            item {
                SidebarNavItem(
                    item = SidebarItem.Trash,
                    isSelected = selectedItem == SidebarItem.Trash,
                    count = itemCounts[SidebarItem.Trash] ?: 0,
                    onClick = { onItemSelected(SidebarItem.Trash) }
                )
            }
        }
    }
}

@Composable
private fun SidebarNavItem(
    item: SidebarItem,
    isSelected: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PeskyColors.SidebarItemSelected else PeskyColors.SidebarBackground
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (isSelected) PeskyColors.AccentBlue else PeskyColors.IconSecondary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) PeskyColors.AccentBlue else PeskyColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        
        if (count > 0) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = PeskyColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SidebarGroupItem(
    group: GroupItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PeskyColors.SidebarItemSelected else PeskyColors.SidebarBackground
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = group.name,
            tint = if (isSelected) PeskyColors.AccentBlue else PeskyColors.IconSecondary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = group.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) PeskyColors.AccentBlue else PeskyColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        
        if (group.count > 0) {
            Text(
                text = group.count.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = PeskyColors.TextSecondary
            )
        }
    }
}

/**
 * Sidebar navigation items.
 */
sealed class SidebarItem(
    val title: String,
    val icon: ImageVector
) {
    object AllItems : SidebarItem("All Items", Icons.Outlined.List)
    object Favorites : SidebarItem("Favorites", Icons.Outlined.Star)
    object RecentlyUsed : SidebarItem("Recently Used", Icons.Outlined.History)
    object WeakPasswords : SidebarItem("Weak Passwords", Icons.Outlined.Warning)
    object DuplicatePasswords : SidebarItem("Duplicate Passwords", Icons.Outlined.ContentCopy)
    object ExpiringSoon : SidebarItem("Expiring Soon", Icons.Outlined.Schedule)
    object SecureNotes : SidebarItem("Secure Notes", Icons.Outlined.Note)
    object Trash : SidebarItem("Trash", Icons.Outlined.Delete)
    
    data class Category(val uuid: String, val name: String) : 
        SidebarItem(name, Icons.Outlined.Folder)
}

/**
 * Group item for sidebar.
 */
data class GroupItem(
    val uuid: String,
    val name: String,
    val count: Int
)
