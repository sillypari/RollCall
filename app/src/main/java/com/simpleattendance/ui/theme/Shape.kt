package com.simpleattendance.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * RollCall shape tokens per the redesign guide.
 * Use shape tokens consistently — do not invent ad-hoc corner radii.
 */
val RollCallShapes = Shapes(
    // Small chips and compact controls
    extraSmall = RoundedCornerShape(10.dp),
    // Standard buttons and list cards
    small = RoundedCornerShape(12.dp),
    // Standard buttons and list cards (M3 "medium" slot)
    medium = RoundedCornerShape(16.dp),
    // Student card, dialogs, major sections
    large = RoundedCornerShape(20.dp),
    // Sheets, floating navigation, hero surfaces
    extraLarge = RoundedCornerShape(28.dp)
)

/** Pill shape — for filter chips and compact segmented controls */
val PillShape = RoundedCornerShape(50)
