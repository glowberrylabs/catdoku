package com.glowberrylabs.catdoku.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.glowberrylabs.catdoku.R
import com.glowberrylabs.catdoku.game.PuzzleUiState

@Composable
fun MenuScreen(uiState: PuzzleUiState, actions: MenuActions, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RegionPalette.backgroundTop, RegionPalette.backgroundBottom)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = { MenuContent(uiState = uiState, actions = actions) }
        )
    }
}

@Composable
private fun MenuContent(uiState: PuzzleUiState, actions: MenuActions) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = actions.onOpenSettings) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = RegionPalette.muted
            )
        }
    }
    Mascot()
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
    Spacer(modifier = Modifier.height(24.dp))
    ProgressCard(uiState = uiState)
    Spacer(modifier = Modifier.height(16.dp))
    ContinueButton(uiState = uiState, onContinue = actions.onContinue)
    Spacer(modifier = Modifier.height(12.dp))
    DailyButton(uiState = uiState, onDaily = actions.onDaily)
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = actions.onOpenRules) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.how_to_play),
            color = RegionPalette.muted
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
    if (uiState.allLevelsComplete) {
        CompletionCard()
        Spacer(modifier = Modifier.height(12.dp))
    }
    StatsRow(uiState = uiState)
}

@Composable
private fun Mascot() {
    Box(
        modifier = Modifier
            .size(156.dp)
            .background(RegionPalette.surfaceRaised.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_cat),
            contentDescription = null,
            modifier = Modifier.size(132.dp)
        )
    }
}

@Composable
private fun ProgressCard(uiState: PuzzleUiState) {
    val fraction = if (uiState.levelCount > 0) {
        (uiState.progress.highestSolvedLevel.toFloat() / uiState.levelCount).coerceIn(0f, 1f)
    } else {
        0f
    }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = RegionPalette.surface,
        border = BorderStroke(1.dp, RegionPalette.surfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.level_progress,
                    uiState.progress.highestSolvedLevel,
                    uiState.levelCount
                ),
                style = MaterialTheme.typography.labelLarge,
                color = RegionPalette.muted
            )
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = RegionPalette.gold,
                trackColor = RegionPalette.surfaceRaised,
                drawStopIndicator = {}
            )
        }
    }
}

@Composable
private fun ContinueButton(uiState: PuzzleUiState, onContinue: () -> Unit) {
    val nextLevel = uiState.nextLevelNumber
    Button(
        onClick = onContinue,
        enabled = nextLevel != null,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = RegionPalette.gold,
            contentColor = RegionPalette.backgroundTop,
            disabledContainerColor = RegionPalette.surfaceRaised,
            disabledContentColor = RegionPalette.muted
        )
    ) {
        Text(
            text = if (nextLevel != null) {
                stringResource(R.string.continue_level_number, nextLevel)
            } else {
                stringResource(R.string.all_levels_solved)
            },
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun DailyButton(uiState: PuzzleUiState, onDaily: () -> Unit) {
    val solved = uiState.daily.solvedToday
    OutlinedButton(
        onClick = onDaily,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            if (solved) RegionPalette.mint else RegionPalette.surfaceRaised
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = if (solved) RegionPalette.mint else RegionPalette.gold
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (solved) R.string.daily_complete else R.string.daily_button
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = RegionPalette.ink
                )
                Text(
                    text = dailySubtitle(uiState = uiState, solved = solved),
                    style = MaterialTheme.typography.labelMedium,
                    color = RegionPalette.muted
                )
            }
        }
    }
}

@Composable
private fun dailySubtitle(uiState: PuzzleUiState, solved: Boolean): String {
    if (solved && uiState.daily.currentStreak > 0) {
        return pluralStringResource(
            R.plurals.daily_streak,
            uiState.daily.currentStreak,
            uiState.daily.currentStreak
        )
    }
    return stringResource(R.string.daily_ready)
}

@Composable
private fun CompletionCard() {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = RegionPalette.gold.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, RegionPalette.gold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = RegionPalette.gold
            )
            Text(
                text = stringResource(R.string.all_levels_complete_body),
                style = MaterialTheme.typography.bodyMedium,
                color = RegionPalette.ink
            )
        }
    }
}

@Composable
private fun StatsRow(uiState: PuzzleUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            value = uiState.progress.highestSolvedLevel,
            label = stringResource(R.string.stat_levels),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = uiState.daily.currentStreak,
            label = stringResource(R.string.stat_streak),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = uiState.progress.totalHints,
            label = stringResource(R.string.stat_hints),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(value: Int, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = RegionPalette.surface,
        border = BorderStroke(1.dp, RegionPalette.surfaceRaised)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = RegionPalette.ink
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = RegionPalette.muted
            )
        }
    }
}
