package com.glowberrylabs.catdoku.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

private val CatdokuColors = darkColorScheme(
    primary = RegionPalette.gold,
    onPrimary = RegionPalette.backgroundTop,
    secondary = RegionPalette.rose,
    onSecondary = RegionPalette.backgroundTop,
    tertiary = RegionPalette.mint,
    onTertiary = RegionPalette.backgroundTop,
    background = RegionPalette.backgroundTop,
    onBackground = RegionPalette.ink,
    surface = RegionPalette.surface,
    onSurface = RegionPalette.ink,
    surfaceVariant = RegionPalette.surfaceRaised,
    onSurfaceVariant = RegionPalette.muted,
    error = RegionPalette.coral,
    onError = RegionPalette.backgroundTop
)

private val BaseTypography = Typography()

private val CatdokuTypography = Typography(
    displaySmall = BaseTypography.displaySmall.copy(fontWeight = FontWeight.Black),
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.Black),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.Black),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
    titleSmall = BaseTypography.titleSmall.copy(fontWeight = FontWeight.Bold)
)

@Composable
fun CatdokuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CatdokuColors,
        typography = CatdokuTypography,
        content = content
    )
}
