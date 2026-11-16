package com.glowberrylabs.catdoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.glowberrylabs.catdoku.R

@Composable
fun PlaceholderScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RegionPalette.backgroundTop, RegionPalette.backgroundBottom)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            tint = RegionPalette.ink,
            modifier = Modifier.size(160.dp)
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            color = RegionPalette.ink
        )
        Text(
            text = stringResource(R.string.tagline),
            style = MaterialTheme.typography.titleMedium,
            color = RegionPalette.muted
        )
        Text(
            text = stringResource(R.string.scaffold_ready),
            style = MaterialTheme.typography.bodyMedium,
            color = RegionPalette.gold
        )
    }
}
