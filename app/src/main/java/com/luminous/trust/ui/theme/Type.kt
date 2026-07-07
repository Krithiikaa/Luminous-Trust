package com.luminous.trust.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// ---- Fonts ----
// DESIGN.md pairs Montserrat (headlines) with Libre Franklin (body/labels).
// These default to the system sans-serif so the project compiles and the
// TYPE SCALE (sizes, weights, spacing) is already correct. To load the real
// typefaces, follow the "Fonts" section of README.md — it's a one-file swap.
val MontserratFamily: FontFamily = FontFamily.SansSerif
val LibreFranklinFamily: FontFamily = FontFamily.SansSerif

val LuminousTypography = Typography(
    // display-lg
    displayLarge = TextStyle(
        fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,
        fontSize = 48.sp, lineHeight = 56.sp, letterSpacing = (-0.02).em
    ),
    // headline-md
    headlineMedium = TextStyle(
        fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp
    ),
    // headline-sm  (used by card headers per DESIGN.md)
    headlineSmall = TextStyle(
        fontFamily = MontserratFamily, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp
    ),
    // body-lg
    bodyLarge = TextStyle(
        fontFamily = LibreFranklinFamily, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 28.sp
    ),
    // body-md
    bodyMedium = TextStyle(
        fontFamily = LibreFranklinFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    // body-sm
    bodySmall = TextStyle(
        fontFamily = LibreFranklinFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    // label-bold  (uppercase labels / metadata)
    labelLarge = TextStyle(
        fontFamily = LibreFranklinFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 16.sp, letterSpacing = 0.05.em
    ),
    // label-sm
    labelSmall = TextStyle(
        fontFamily = LibreFranklinFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp
    )
)
