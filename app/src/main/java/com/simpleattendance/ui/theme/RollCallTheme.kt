package com.simpleattendance.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The canonical Material 3 dark color scheme built from RollCall design tokens.
 * All Compose screens must be wrapped in RollCallTheme.
 */
private val RollCallDarkColorScheme = darkColorScheme(
    primary               = Primary,
    onPrimary             = OnPrimary,
    primaryContainer      = PrimaryContainer,
    onPrimaryContainer    = OnPrimaryContainer,
    secondary             = Primary,
    onSecondary           = OnPrimary,
    secondaryContainer    = PrimaryContainer,
    onSecondaryContainer  = OnPrimaryContainer,
    tertiary              = Success,
    onTertiary            = Color.Black,
    tertiaryContainer     = SuccessContainer,
    onTertiaryContainer   = OnSuccessContainer,
    error                 = RCError,
    onError               = Color.White,
    errorContainer        = ErrorContainer,
    onErrorContainer      = OnErrorContainer,
    background            = Background,
    onBackground          = OnBackground,
    surface               = Surface,
    onSurface             = OnSurface,
    onSurfaceVariant      = OnSurfaceVariant,
    surfaceVariant        = SurfaceContainer,
    surfaceContainer      = SurfaceContainer,
    surfaceContainerHigh  = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline               = OutlineVariant,
    outlineVariant        = Divider,
    scrim                 = Scrim,
    inverseSurface        = TextPrimary,
    inverseOnSurface      = Background,
    inversePrimary        = PrimaryContainer
)

/**
 * All RollCall Compose UI must be wrapped in this theme.
 *
 * - Always dark (brand default). Light theme is a future phase.
 * - Dynamic color is not enabled; brand palette is intentional.
 */
@Composable
fun RollCallTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RollCallDarkColorScheme,
        typography  = RollCallTypography,
        shapes      = RollCallShapes,
        content     = content
    )
}
