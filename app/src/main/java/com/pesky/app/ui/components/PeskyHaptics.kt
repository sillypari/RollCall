package com.pesky.app.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.*
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import com.pesky.app.data.preferences.AppPreferences

/**
 * iPhone-style haptic feedback types for use throughout the app.
 * 
 * Each type provides a different tactile sensation optimized for specific interactions:
 * - TICK: Ultra-light tap for selections, checkboxes, scroll snaps
 * - CLICK: Standard button press feedback  
 * - HEAVY_CLICK: Emphasized feedback for important actions
 * - SUCCESS: Confirmation pattern for successful operations
 * - ERROR: Distinctive shake for errors or failures
 * - KEYBOARD: Extra light for rapid key presses
 */
enum class HapticType {
    /** Ultra-light tap - checkboxes, selections, scroll snaps */
    TICK,
    
    /** Standard click - buttons, navigation, general taps */
    CLICK,
    
    /** Heavy click - important confirmations, long press */
    HEAVY_CLICK,
    
    /** Success pattern - save complete, action successful */
    SUCCESS,
    
    /** Error pattern - validation errors, failed actions */
    ERROR,
    
    /** Keyboard tap - PIN entry, text input */
    KEYBOARD
}

/**
 * Composable haptic feedback controller that respects user preferences.
 */
class PeskyHaptics(
    private val view: View,
    private val hapticFeedback: HapticFeedback,
    private val isEnabled: Boolean
) {
    
    /**
     * Perform haptic feedback of the specified type.
     */
    fun perform(type: HapticType) {
        if (!isEnabled) return
        
        when (type) {
            HapticType.TICK -> performTick()
            HapticType.CLICK -> performClick()
            HapticType.HEAVY_CLICK -> performHeavyClick()
            HapticType.SUCCESS -> performSuccess()
            HapticType.ERROR -> performError()
            HapticType.KEYBOARD -> performKeyboard()
        }
    }
    
    /**
     * Light tick feedback - for selections, toggles, scroll snaps
     */
    fun tick() {
        if (!isEnabled) return
        performTick()
    }
    
    /**
     * Standard click feedback - for button presses, navigation
     */
    fun click() {
        if (!isEnabled) return
        performClick()
    }
    
    /**
     * Heavy click feedback - for important actions, confirmations
     */
    fun heavyClick() {
        if (!isEnabled) return
        performHeavyClick()
    }
    
    /**
     * Success feedback - for completed actions
     */
    fun success() {
        if (!isEnabled) return
        performSuccess()
    }
    
    /**
     * Error feedback - for validation errors, failures
     */
    fun error() {
        if (!isEnabled) return
        performError()
    }
    
    /**
     * Keyboard tap feedback - for PIN/key presses
     */
    fun keyboard() {
        if (!isEnabled) return
        performKeyboard()
    }
    
    private fun performTick() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
    
    private fun performClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
    
    private fun performHeavyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    private fun performSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }
    
    private fun performError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    private fun performKeyboard() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}

/**
 * CompositionLocal for accessing PeskyHaptics throughout the app.
 */
val LocalPeskyHaptics = staticCompositionLocalOf<PeskyHaptics> {
    error("No PeskyHaptics provided")
}

/**
 * Remember a PeskyHaptics instance with the given enabled state.
 */
@Composable
fun rememberPeskyHaptics(isEnabled: Boolean): PeskyHaptics {
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    
    return remember(view, hapticFeedback, isEnabled) {
        PeskyHaptics(view, hapticFeedback, isEnabled)
    }
}

/**
 * Provider composable that makes PeskyHaptics available to all children.
 * 
 * Usage:
 * ```
 * PeskyHapticsProvider(isEnabled = preferences.hapticFeedbackEnabled) {
 *     // Your app content
 * }
 * ```
 */
@Composable
fun PeskyHapticsProvider(
    isEnabled: Boolean,
    content: @Composable () -> Unit
) {
    val haptics = rememberPeskyHaptics(isEnabled)
    
    CompositionLocalProvider(LocalPeskyHaptics provides haptics) {
        content()
    }
}

/**
 * Convenience composable to get PeskyHaptics from the composition.
 * Falls back to a no-op implementation if not provided.
 */
@Composable
fun peskyHaptics(): PeskyHaptics {
    return LocalPeskyHaptics.current
}
