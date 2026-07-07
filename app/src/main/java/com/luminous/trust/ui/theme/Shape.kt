package com.luminous.trust.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// DESIGN.md "rounded" tokens. Soft & precise: 4px standard, 8px for containers.
val LuminousShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),   // sm  0.125rem
    small = RoundedCornerShape(4.dp),        // DEFAULT 0.25rem — buttons, inputs
    medium = RoundedCornerShape(6.dp),       // md  0.375rem
    large = RoundedCornerShape(8.dp),        // lg  0.5rem — cards, chips
    extraLarge = RoundedCornerShape(12.dp)   // xl  0.75rem
)
