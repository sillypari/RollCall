package com.pesky.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.pesky.app.ui.theme.PeskyColors

/**
 * A row displaying a copyable value with optional protection.
 */
@Composable
fun CopyableValueRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    isProtected: Boolean = false,
    showValue: Boolean = true
) {
    var isValueVisible by remember { mutableStateOf(showValue && !isProtected) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PeskyColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isValueVisible) value else "••••••••••••",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = if (isProtected) FontFamily.Monospace else FontFamily.Default
                ),
                color = PeskyColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            if (isProtected) {
                IconButton(onClick = { isValueVisible = !isValueVisible }) {
                    Icon(
                        imageVector = if (isValueVisible) 
                            Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (isValueVisible) "Hide" else "Show",
                        tint = PeskyColors.IconSecondary
                    )
                }
            }
            
            CopyButton(
                onClick = { onCopy() }
            )
        }
    }
}
