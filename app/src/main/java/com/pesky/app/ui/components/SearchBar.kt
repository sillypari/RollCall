package com.pesky.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pesky.app.ui.theme.PeskyColors
import com.pesky.app.ui.animations.PeskyAnimations

/**
 * Apple Maps-style floating search bar with glass morphism effect.
 * Features: Expand animation, backdrop blur simulation, smooth focus transitions.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search passwords…"
) {
    val haptic = LocalHapticFeedback.current
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Animated width expansion
    val width by animateDpAsState(
        targetValue = if (isFocused) 360.dp else 280.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "search_width"
    )
    
    // Background opacity animation
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0.85f,
        animationSpec = tween(200),
        label = "search_alpha"
    )
    
    // Elevation animation
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 4.dp,
        animationSpec = tween(200),
        label = "search_elevation"
    )
    
    // Border color animation
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) PeskyColors.AccentBlue.copy(alpha = 0.5f) 
                      else PeskyColors.GlassBorder,
        animationSpec = tween(200),
        label = "search_border"
    )
    
    Box(
        modifier = modifier
            .width(width)
            .height(44.dp)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(12.dp),
                ambientColor = PeskyColors.ShadowAmbient,
                spotColor = PeskyColors.ShadowSpot
            )
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFocused) PeskyColors.SearchBackgroundFocused.copy(alpha = backgroundAlpha)
                else PeskyColors.GlassBackground.copy(alpha = backgroundAlpha)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Search icon with color animation
            val iconTint by animateColorAsState(
                targetValue = if (isFocused) PeskyColors.AccentBlue else PeskyColors.IconSecondary,
                animationSpec = tween(200),
                label = "icon_tint"
            )
            
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Box(modifier = Modifier.weight(1f)) {
                // Placeholder with fade animation
                androidx.compose.animation.AnimatedVisibility(
                    visible = query.isEmpty(),
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(100))
                ) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PeskyColors.SearchPlaceholder
                    )
                }
                
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { 
                            val wasFocused = isFocused
                            isFocused = it.isFocused
                            if (!wasFocused && it.isFocused) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = PeskyColors.TextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch()
                            focusManager.clearFocus()
                        }
                    ),
                    cursorBrush = SolidColor(PeskyColors.AccentBlue)
                )
            }
            
            // Clear button with scale animation
            androidx.compose.animation.AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                exit = scaleOut(animationSpec = spring(stiffness = Spring.StiffnessHigh)) + fadeOut()
            ) {
                IconButton(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onQueryChange("") 
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = PeskyColors.IconSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Cancel button (appears when focused)
            androidx.compose.animation.AnimatedVisibility(
                visible = isFocused,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(200)
                ) + fadeIn(),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(150)
                ) + fadeOut()
            ) {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onQueryChange("")
                        focusManager.clearFocus()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = PeskyColors.AccentBlue
                    )
                }
            }
        }
    }
}
