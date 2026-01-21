package com.pesky.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Apple Maps-inspired dark color palette for Pesky.
 * Features layered depth, frosted glass effects, and vibrant accents.
 */
object PeskyColors {
    
    // ═══════════════════════════════════════════════════════════
    // LAYERED BACKGROUNDS (Depth through layers)
    // ═══════════════════════════════════════════════════════════
    
    // Base layer - deepest background
    val BackgroundPrimary = Color(0xFF1C1C1E)
    
    // Card layer - elevated surface
    val BackgroundSecondary = Color(0xFF2C2C2E)
    
    // Modal layer - highest elevation
    val BackgroundTertiary = Color(0xFF3A3A3C)
    val BackgroundQuaternary = Color(0xFF48484A)
    
    // ═══════════════════════════════════════════════════════════
    // GLASS MORPHISM COLORS
    // ═══════════════════════════════════════════════════════════
    
    // Frosted glass backgrounds (semi-transparent)
    val GlassBackground = Color(0xFF2C2C2E).copy(alpha = 0.85f)
    val GlassBackgroundLight = Color(0xFF3A3A3C).copy(alpha = 0.75f)
    val GlassBackgroundDark = Color(0xFF1C1C1E).copy(alpha = 0.90f)
    
    // Glass border for frosted elements
    val GlassBorder = Color.White.copy(alpha = 0.08f)
    
    // ═══════════════════════════════════════════════════════════
    // TEXT COLORS (Apple-style opacity levels)
    // ═══════════════════════════════════════════════════════════
    
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color.White.copy(alpha = 0.70f)
    val TextTertiary = Color.White.copy(alpha = 0.50f)
    val TextQuaternary = Color.White.copy(alpha = 0.30f)
    val TextDisabled = Color.White.copy(alpha = 0.25f)
    
    // ═══════════════════════════════════════════════════════════
    // ACCENT COLORS (Vibrant Apple blue)
    // ═══════════════════════════════════════════════════════════
    
    val AccentBlue = Color(0xFF0A84FF)
    val AccentBlueDark = Color(0xFF0066CC)
    val AccentBlueLight = Color(0xFF409CFF)
    val AccentBlueSubtle = Color(0xFF0A84FF).copy(alpha = 0.15f)
    
    // Blue gradient for FAB and prominent buttons
    val AccentGradient = Brush.verticalGradient(
        colors = listOf(AccentBlue, AccentBlueDark)
    )
    
    // ═══════════════════════════════════════════════════════════
    // PASSWORD STRENGTH COLORS
    // ═══════════════════════════════════════════════════════════
    
    val StrengthVeryWeak = Color(0xFFFF453A)   // Red
    val StrengthWeak = Color(0xFFFF9F0A)       // Orange
    val StrengthFair = Color(0xFFFFD60A)       // Yellow
    val StrengthStrong = Color(0xFF32D74B)    // Green
    val StrengthVeryStrong = Color(0xFF30D158) // Bright Green
    
    // Ring-style strength colors
    val StrengthRingWeak = Color(0xFFFF453A)
    val StrengthRingMedium = Color(0xFFFF9F0A)
    val StrengthRingStrong = Color(0xFF30D158)
    val StrengthRingBackground = Color.White.copy(alpha = 0.10f)
    
    // ═══════════════════════════════════════════════════════════
    // SEMANTIC COLORS
    // ═══════════════════════════════════════════════════════════
    
    val Error = Color(0xFFFF453A)
    val ErrorSubtle = Color(0xFFFF453A).copy(alpha = 0.15f)
    val Warning = Color(0xFFFFD60A)
    val WarningSubtle = Color(0xFFFFD60A).copy(alpha = 0.15f)
    val Success = Color(0xFF30D158)
    val SuccessSubtle = Color(0xFF30D158).copy(alpha = 0.15f)
    val Info = Color(0xFF0A84FF)
    
    // Accent shortcuts for easy access
    val AccentRed = Error
    val AccentGreen = Success
    val AccentOrange = Warning
    
    // Text on accent colors
    val TextOnAccent = Color.White
    
    // ═══════════════════════════════════════════════════════════
    // UI ELEMENTS
    // ═══════════════════════════════════════════════════════════
    
    val CardBackground = Color(0xFF2C2C2E)
    val CardBackgroundHovered = Color(0xFF3A3A3C)
    val CardBackgroundPressed = Color(0xFF48484A)
    val CardHover = Color(0xFF3A3A3C)
    val CardBorder = Color(0xFF48484A).copy(alpha = 0.3f)
    val Divider = Color.White.copy(alpha = 0.08f)
    val DividerLight = Color.White.copy(alpha = 0.12f)
    val Ripple = Color(0xFFFFFFFF).copy(alpha = 0.1f)
    val Border = Color.White.copy(alpha = 0.10f)
    val BorderFocused = AccentBlue
    
    // ═══════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════
    
    val NavBarBackground = Color(0xFF1C1C1E)
    val NavBarItemActive = Color(0xFF0A84FF)
    val NavBarItemInactive = Color.White.copy(alpha = 0.60f)
    val NavItemActive = AccentBlue
    val NavItemInactive = Color.White.copy(alpha = 0.60f)
    
    // Sidebar
    val SidebarBackground = Color(0xFF1C1C1E)
    val SidebarItemHover = Color(0xFF2C2C2E)
    val SidebarItemSelected = Color(0xFF0A84FF).copy(alpha = 0.15f)
    
    // ═══════════════════════════════════════════════════════════
    // SEARCH
    // ═══════════════════════════════════════════════════════════
    
    val SearchBackground = GlassBackground
    val SearchBackgroundFocused = Color(0xFF3A3A3C)
    val SearchPlaceholder = Color.White.copy(alpha = 0.40f)
    val SearchBarBackground = GlassBackground
    
    // ═══════════════════════════════════════════════════════════
    // FAB & BUTTONS
    // ═══════════════════════════════════════════════════════════
    
    val FABBackground = Color(0xFF0A84FF)
    val FABContent = Color(0xFFFFFFFF)
    val PillButtonBackground = AccentBlueSubtle
    val PillButtonContent = AccentBlue
    
    // ═══════════════════════════════════════════════════════════
    // TAGS & CHIPS
    // ═══════════════════════════════════════════════════════════
    
    val TagBackground = Color(0xFF48484A)
    val TagText = Color.White.copy(alpha = 0.85f)
    
    // ═══════════════════════════════════════════════════════════
    // ICONS
    // ═══════════════════════════════════════════════════════════
    
    val IconPrimary = Color(0xFFFFFFFF)
    val IconSecondary = Color.White.copy(alpha = 0.60f)
    val IconTertiary = Color.White.copy(alpha = 0.40f)
    val IconDisabled = Color.White.copy(alpha = 0.25f)
    
    // ═══════════════════════════════════════════════════════════
    // SHADOWS
    // ═══════════════════════════════════════════════════════════
    
    val ShadowAmbient = Color.Black.copy(alpha = 0.20f)
    val ShadowSpot = Color.Black.copy(alpha = 0.30f)
    val ShadowGlow = AccentBlue.copy(alpha = 0.20f)
    
    // ═══════════════════════════════════════════════════════════
    // SHIMMER / LOADING
    // ═══════════════════════════════════════════════════════════
    
    val ShimmerBase = Color(0xFF2C2C2E)
    val ShimmerHighlight = Color(0xFF3A3A3C)
    
    // ═══════════════════════════════════════════════════════════
    // SPECIAL ELEMENTS
    // ═══════════════════════════════════════════════════════════
    
    val HandleBar = Color.White.copy(alpha = 0.30f)
    val Scrim = Color.Black.copy(alpha = 0.50f)
}
