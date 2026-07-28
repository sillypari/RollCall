package com.simpleattendance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val RollCallLightColorScheme = lightColorScheme(
    primary = RollCallLightColors.Primary,
    onPrimary = RollCallLightColors.OnPrimary,
    primaryContainer = RollCallLightColors.PrimaryContainer,
    onPrimaryContainer = RollCallLightColors.OnPrimaryContainer,
    secondary = RollCallLightColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = RollCallLightColors.SecondaryContainer,
    onSecondaryContainer = RollCallLightColors.OnSecondaryContainer,
    tertiary = RollCallLightColors.Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = RollCallLightColors.TertiaryContainer,
    onTertiaryContainer = RollCallLightColors.OnTertiaryContainer,
    error = RollCallLightColors.Error,
    onError = Color.White,
    errorContainer = RollCallLightColors.ErrorContainer,
    onErrorContainer = RollCallLightColors.OnErrorContainer,
    background = RollCallLightColors.Background,
    onBackground = RollCallLightColors.OnSurface,
    surface = RollCallLightColors.Surface,
    onSurface = RollCallLightColors.OnSurface,
    onSurfaceVariant = RollCallLightColors.OnSurfaceVariant,
    surfaceVariant = RollCallLightColors.SurfaceContainer,
    surfaceContainer = RollCallLightColors.SurfaceContainer,
    surfaceContainerHigh = RollCallLightColors.SurfaceContainerHigh,
    surfaceContainerHighest = RollCallLightColors.SurfaceContainerHighest,
    outline = RollCallLightColors.Outline,
    outlineVariant = RollCallLightColors.OutlineVariant,
    scrim = Color(0x80000000)
)

private val RollCallDarkColorScheme = darkColorScheme(
    primary = RollCallDarkColors.Primary,
    onPrimary = RollCallDarkColors.OnPrimary,
    primaryContainer = RollCallDarkColors.PrimaryContainer,
    onPrimaryContainer = RollCallDarkColors.OnPrimaryContainer,
    secondary = RollCallDarkColors.Secondary,
    onSecondary = Color(0xFF082B59),
    secondaryContainer = RollCallDarkColors.SecondaryContainer,
    onSecondaryContainer = RollCallDarkColors.OnSecondaryContainer,
    tertiary = RollCallDarkColors.Tertiary,
    onTertiary = Color(0xFF00391F),
    tertiaryContainer = RollCallDarkColors.TertiaryContainer,
    onTertiaryContainer = RollCallDarkColors.OnTertiaryContainer,
    error = RollCallDarkColors.Error,
    onError = Color(0xFF570C18),
    errorContainer = RollCallDarkColors.ErrorContainer,
    onErrorContainer = RollCallDarkColors.OnErrorContainer,
    background = RollCallDarkColors.Background,
    onBackground = RollCallDarkColors.OnSurface,
    surface = RollCallDarkColors.Surface,
    onSurface = RollCallDarkColors.OnSurface,
    onSurfaceVariant = RollCallDarkColors.OnSurfaceVariant,
    surfaceVariant = RollCallDarkColors.SurfaceContainer,
    surfaceContainer = RollCallDarkColors.SurfaceContainer,
    surfaceContainerHigh = RollCallDarkColors.SurfaceContainerHigh,
    surfaceContainerHighest = RollCallDarkColors.SurfaceContainerHighest,
    outline = RollCallDarkColors.Outline,
    outlineVariant = RollCallDarkColors.OutlineVariant,
    scrim = Color(0xA0000000)
)

@Immutable
data class RollCallExtendedColors(
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val ambientTeal: Color,
    val ambientBlue: Color,
    val ambientGreen: Color
)

private val LightExtendedColors = RollCallExtendedColors(
    warning = Color(0xFF9A6800),
    warningContainer = Color(0xFFFFF0C7),
    onWarningContainer = Color(0xFF4D3200),
    ambientTeal = Color(0xFF2CCB91),
    ambientBlue = Color(0xFF4D8DFF),
    ambientGreen = Color(0xFF3CCB7F)
)

private val DarkExtendedColors = RollCallExtendedColors(
    warning = Color(0xFFF2C76B),
    warningContainer = Color(0xFF483600),
    onWarningContainer = Color(0xFFFFE5A6),
    ambientTeal = Color(0xFF20B8AA),
    ambientBlue = Color(0xFF90B4FF),
    ambientGreen = Color(0xFF53D38C)
)

private val LocalRollCallExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

val MaterialTheme.rollCallColors: RollCallExtendedColors
    @Composable get() = LocalRollCallExtendedColors.current

@Composable
fun RollCallTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    androidx.compose.runtime.CompositionLocalProvider(
        LocalRollCallExtendedColors provides if (isDark) DarkExtendedColors else LightExtendedColors
    ) {
        MaterialTheme(
            colorScheme = if (isDark) RollCallDarkColorScheme else RollCallLightColorScheme,
            typography = RollCallTypography,
            shapes = RollCallShapes,
            content = content
        )
    }
}
