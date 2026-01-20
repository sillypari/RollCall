package com.pesky.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark color scheme for Pesky - always dark theme (Apple Maps style).
 */
private val PeskyDarkColorScheme = darkColorScheme(
    primary = PeskyColors.AccentBlue,
    onPrimary = PeskyColors.TextPrimary,
    primaryContainer = PeskyColors.AccentBlueDark,
    onPrimaryContainer = PeskyColors.TextPrimary,
    
    secondary = PeskyColors.BackgroundTertiary,
    onSecondary = PeskyColors.TextPrimary,
    secondaryContainer = PeskyColors.BackgroundQuaternary,
    onSecondaryContainer = PeskyColors.TextPrimary,
    
    tertiary = PeskyColors.StrengthStrong,
    onTertiary = PeskyColors.TextPrimary,
    
    background = PeskyColors.BackgroundPrimary,
    onBackground = PeskyColors.TextPrimary,
    
    surface = PeskyColors.BackgroundSecondary,
    onSurface = PeskyColors.TextPrimary,
    surfaceVariant = PeskyColors.BackgroundTertiary,
    onSurfaceVariant = PeskyColors.TextSecondary,
    
    error = PeskyColors.Error,
    onError = PeskyColors.TextPrimary,
    errorContainer = PeskyColors.Error.copy(alpha = 0.2f),
    onErrorContainer = PeskyColors.Error,
    
    outline = PeskyColors.Divider,
    outlineVariant = PeskyColors.CardBorder,
    
    inverseSurface = PeskyColors.TextPrimary,
    inverseOnSurface = PeskyColors.BackgroundPrimary,
    inversePrimary = PeskyColors.AccentBlueDark
)

/**
 * Pesky app theme - always uses dark theme for Apple Maps style.
 */
@Composable
fun PeskyTheme(
    // Pesky is always dark themed
    content: @Composable () -> Unit
) {
    val colorScheme = PeskyDarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PeskyColors.BackgroundPrimary.toArgb()
            window.navigationBarColor = PeskyColors.BackgroundPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PeskyTypography,
        content = content
    )
}
