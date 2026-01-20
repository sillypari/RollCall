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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pesky.app.ui.theme.PeskyColors

/**
 * Floating search bar positioned at the top of the screen with proper spacing.
 * Apple Maps-style with glass morphism and shadow.
 */
@Composable
fun FloatingSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search passwords…",
    topPadding: Dp = 20.dp,
    horizontalPadding: Dp = 16.dp
) {
    val haptic = LocalHapticFeedback.current
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Animated expansion
    val horizontalMargin by animateDpAsState(
        targetValue = if (isFocused) 8.dp else horizontalPadding,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "search_margin"
    )
    
    // Elevation animation
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 6.dp,
        animationSpec = tween(200),
        label = "search_elevation"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding, start = horizontalMargin, end = horizontalMargin)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(14.dp),
                    ambientColor = PeskyColors.ShadowAmbient,
                    spotColor = PeskyColors.ShadowSpot
                )
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isFocused) PeskyColors.BackgroundTertiary
                    else PeskyColors.GlassBackground
                )
                .border(
                    width = 1.dp,
                    color = if (isFocused) PeskyColors.AccentBlue.copy(alpha = 0.4f) 
                           else PeskyColors.GlassBorder,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search icon
                val iconTint by animateColorAsState(
                    targetValue = if (isFocused) PeskyColors.AccentBlue 
                                 else PeskyColors.IconSecondary,
                    animationSpec = tween(200),
                    label = "icon_tint"
                )
                
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Text field
                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = query.isEmpty(),
                        enter = fadeIn(tween(100)),
                        exit = fadeOut(tween(50))
                    ) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = PeskyColors.TextTertiary
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
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
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
                
                // Clear button
                androidx.compose.animation.AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = scaleIn(spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                    exit = scaleOut(spring(stiffness = Spring.StiffnessHigh)) + fadeOut()
                ) {
                    IconButton(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onQueryChange("") 
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = PeskyColors.IconSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // Cancel button when focused
                androidx.compose.animation.AnimatedVisibility(
                    visible = isFocused,
                    enter = slideInHorizontally { it } + fadeIn(),
                    exit = slideOutHorizontally { it } + fadeOut()
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
}
