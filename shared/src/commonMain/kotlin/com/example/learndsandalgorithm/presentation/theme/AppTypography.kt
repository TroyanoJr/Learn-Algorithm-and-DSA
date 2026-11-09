package com.example.learndsandalgorithm.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Jerarquía tipográfica semántica para la aplicación.
 */
object AppTypography {
    val displayTitle = TextStyle(
        fontSize = 24.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold
    )

    val headerTitle = TextStyle(
        fontSize = 20.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold
    )

    val sectionHeader = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
    )

    val sectionTitle = TextStyle(
        fontSize = 18.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Bold
    )

    val cardTitle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold
    )

    val bodyPrimary = TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal
    )

    val bodySecondary = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal
    )

    val caption = TextStyle(
        fontSize = 12.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Normal
    )

    val badge = TextStyle(
        fontSize = 11.sp,
        lineHeight = 13.sp,
        fontWeight = FontWeight.Bold
    )
}
