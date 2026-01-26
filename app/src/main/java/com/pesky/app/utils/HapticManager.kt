package com.pesky.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import com.pesky.app.data.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * iPhone-style haptic feedback manager for Android.
 * 
 * Provides subtle, satisfying haptic feedback similar to iOS Taptic Engine.
 * Features various feedback types optimized for different interactions:
 * 
 * - LIGHT: Subtle tap for selections, checkboxes, toggles
 * - MEDIUM: Standard tap for button presses
 * - HEAVY: Strong feedback for important actions
 * - SUCCESS: Positive confirmation (like iOS success haptic)
 * - WARNING: Alert feedback for caution
 * - ERROR: Shake pattern for errors/failures
 * - SELECTION: Tick for picker/scroll changes
 * - KEYBOARD: Light tap for keyboard presses
 */
@Singleton
class HapticManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: AppPreferences
) {
    
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    /**
     * Haptic feedback types inspired by iOS Taptic Engine patterns
     */
    enum class HapticType {
        /** Very subtle tap - for selections, checkboxes, minor interactions */
        LIGHT,
        
        /** Standard tap - for buttons, navigation items */
        MEDIUM,
        
        /** Strong tap - for important actions, confirmations */
        HEAVY,
        
        /** Success pattern - double tap with rising intensity */
        SUCCESS,
        
        /** Warning pattern - attention-getting single tap */
        WARNING,
        
        /** Error pattern - distinctive shake feel */
        ERROR,
        
        /** Selection tick - for pickers, scrolling selections */
        SELECTION,
        
        /** Keyboard tap - extra light for key presses */
        KEYBOARD,
        
        /** Rigid click - sharp, mechanical feel */
        CLICK
    }
    
    /**
     * Check if haptic feedback is enabled in user preferences
     */
    fun isEnabled(): Boolean = preferences.hapticFeedbackEnabled
    
    /**
     * Perform haptic feedback if enabled
     */
    fun performHaptic(type: HapticType) {
        if (!isEnabled()) return
        
        vibrator?.let { vib ->
            if (!vib.hasVibrator()) return
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use predefined effects on Android 10+ for best iPhone-like experience
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    HapticType.MEDIUM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    HapticType.HEAVY -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    HapticType.SUCCESS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    HapticType.WARNING -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    HapticType.ERROR -> createErrorPattern()
                    HapticType.SELECTION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    HapticType.KEYBOARD -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    HapticType.CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                }
                vib.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Fallback for Android 8-9
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createOneShot(10, 50)
                    HapticType.MEDIUM -> VibrationEffect.createOneShot(15, 100)
                    HapticType.HEAVY -> VibrationEffect.createOneShot(25, 200)
                    HapticType.SUCCESS -> VibrationEffect.createWaveform(
                        longArrayOf(0, 15, 50, 20),
                        intArrayOf(0, 80, 0, 150),
                        -1
                    )
                    HapticType.WARNING -> VibrationEffect.createOneShot(20, 150)
                    HapticType.ERROR -> createErrorPattern()
                    HapticType.SELECTION -> VibrationEffect.createOneShot(8, 40)
                    HapticType.KEYBOARD -> VibrationEffect.createOneShot(5, 30)
                    HapticType.CLICK -> VibrationEffect.createOneShot(12, 80)
                }
                vib.vibrate(effect)
            } else {
                // Legacy fallback for older devices
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.LIGHT, HapticType.SELECTION, HapticType.KEYBOARD -> vib.vibrate(10)
                    HapticType.MEDIUM, HapticType.CLICK -> vib.vibrate(15)
                    HapticType.HEAVY, HapticType.WARNING -> vib.vibrate(25)
                    HapticType.SUCCESS -> vib.vibrate(longArrayOf(0, 15, 50, 20), -1)
                    HapticType.ERROR -> vib.vibrate(longArrayOf(0, 15, 40, 15, 40, 15), -1)
                }
            }
        }
    }
    
    /**
     * Create an error haptic pattern (distinctive shake feel)
     */
    private fun createErrorPattern(): VibrationEffect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect.createWaveform(
                longArrayOf(0, 12, 30, 12, 30, 12),
                intArrayOf(0, 150, 0, 150, 0, 150),
                -1
            )
        } else {
            throw IllegalStateException("Should not reach here - handled in caller")
        }
    }
    
    /**
     * Convenience method for View-based haptic feedback
     */
    fun performHapticOnView(view: View, type: HapticType) {
        if (!isEnabled()) return
        
        val feedbackConstant = when (type) {
            HapticType.LIGHT, HapticType.SELECTION, HapticType.KEYBOARD -> 
                HapticFeedbackConstants.CLOCK_TICK
            HapticType.MEDIUM, HapticType.CLICK -> 
                HapticFeedbackConstants.CONTEXT_CLICK
            HapticType.HEAVY -> 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) 
                    HapticFeedbackConstants.CONFIRM
                else HapticFeedbackConstants.LONG_PRESS
            HapticType.SUCCESS -> 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) 
                    HapticFeedbackConstants.CONFIRM
                else HapticFeedbackConstants.CONTEXT_CLICK
            HapticType.WARNING, HapticType.ERROR -> 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) 
                    HapticFeedbackConstants.REJECT
                else HapticFeedbackConstants.LONG_PRESS
        }
        
        view.performHapticFeedback(feedbackConstant)
    }
}

/**
 * Composable wrapper for HapticManager that respects preferences
 */
class ComposableHapticManager(
    private val hapticFeedback: HapticFeedback,
    private val view: View,
    private val isEnabled: Boolean
) {
    
    /**
     * Perform light haptic feedback (for selections, toggles)
     */
    fun light() {
        if (!isEnabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
    
    /**
     * Perform medium haptic feedback (for button presses)
     */
    fun medium() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    
    /**
     * Perform heavy haptic feedback (for important actions)
     */
    fun heavy() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    /**
     * Perform success haptic feedback
     */
    fun success() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }
    
    /**
     * Perform error haptic feedback
     */
    fun error() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    /**
     * Perform selection tick haptic feedback
     */
    fun tick() {
        if (!isEnabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
    
    /**
     * Perform keyboard tap haptic feedback
     */
    fun keyPress() {
        if (!isEnabled) return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    
    /**
     * Perform click haptic feedback
     */
    fun click() {
        if (!isEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
}

/**
 * Remember a ComposableHapticManager that respects user preferences
 */
@Composable
fun rememberHapticManager(isEnabled: Boolean = true): ComposableHapticManager {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    
    return remember(hapticFeedback, view, isEnabled) {
        ComposableHapticManager(hapticFeedback, view, isEnabled)
    }
}
