package com.glowberrylabs.catdoku.ui

import androidx.compose.ui.graphics.Color

internal object RegionPalette {
    val backgroundTop = Color(0xFF14101E)
    val backgroundBottom = Color(0xFF1E1A2E)
    val surface = Color(0xFF241D31)
    val surfaceRaised = Color(0xFF2E2540)
    val ink = Color(0xFFFFF3E0)
    val muted = Color(0xFFC9B8D8)
    val gold = Color(0xFFF2C46D)
    val coral = Color(0xFFF2777A)
    val scrim = Color(0xCC0E0A17)

    val rose = Color(0xFFE8A0A8)
    val amber = Color(0xFFE8C07A)
    val sage = Color(0xFFA8CBA6)
    val sky = Color(0xFF8FB8DE)
    val lavender = Color(0xFFB8A6D9)
    val peach = Color(0xFFE6B08C)
    val mint = Color(0xFF8FD1C7)
    val mauve = Color(0xFFC99AC7)

    val regions = listOf(
        rose,
        amber,
        sage,
        sky,
        lavender,
        peach,
        mint,
        mauve
    )

    fun region(index: Int): Color = regions[index % regions.size]
}
