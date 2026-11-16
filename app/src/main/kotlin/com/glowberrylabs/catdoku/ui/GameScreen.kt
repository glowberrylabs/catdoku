package com.glowberrylabs.catdoku.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.glowberrylabs.catdoku.R
import com.glowberrylabs.catdoku.game.PuzzleMode
import com.glowberrylabs.catdoku.game.PuzzleState
import com.glowberrylabs.catdoku.game.PuzzleStatus
import com.glowberrylabs.catdoku.game.PuzzleUiState

@Composable
fun GameScreen(uiState: PuzzleUiState, actions: GameActions, modifier: Modifier = Modifier) {
    val puzzleState = uiState.puzzleState ?: return
    KeepScreenOn()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RegionPalette.backgroundTop, RegionPalette.backgroundBottom)
                )
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (maxWidth > maxHeight) {
                LandscapeLayout(
                    uiState = uiState,
                    puzzleState = puzzleState,
                    actions = actions
                )
            } else {
                PortraitLayout(
                    uiState = uiState,
                    puzzleState = puzzleState,
                    actions = actions
                )
            }
        }
    }
}

@Composable
private fun PortraitLayout(uiState: PuzzleUiState, puzzleState: PuzzleState, actions: GameActions) {
    Column(modifier = Modifier.fillMaxSize()) {
        GameHeader(puzzleState = puzzleState, onPause = actions.onPause)
        Spacer(modifier = Modifier.height(8.dp))
        HintBanner(
            visible = puzzleState.hintCell != null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        BoardArea(
            uiState = uiState,
            puzzleState = puzzleState,
            actions = actions,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        GameActionBar(puzzleState = puzzleState, actions = actions)
    }
}

@Composable
private fun LandscapeLayout(uiState: PuzzleUiState, puzzleState: PuzzleState, actions: GameActions) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoardArea(
            uiState = uiState,
            puzzleState = puzzleState,
            actions = actions,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GameHeader(puzzleState = puzzleState, onPause = actions.onPause)
            Spacer(modifier = Modifier.height(16.dp))
            HintBanner(visible = puzzleState.hintCell != null)
            Spacer(modifier = Modifier.height(24.dp))
            GameActionBar(puzzleState = puzzleState, actions = actions)
        }
    }
}

@Composable
private fun GameHeader(puzzleState: PuzzleState, onPause: () -> Unit) {
    val levelLabel = if (puzzleState.mode == PuzzleMode.LEVEL) {
        stringResource(R.string.level_label, puzzleState.levelNumber ?: 0)
    } else {
        stringResource(R.string.daily_label)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPause,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = stringResource(R.string.menu),
                tint = RegionPalette.ink
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = levelLabel,
                style = MaterialTheme.typography.titleLarge,
                color = RegionPalette.ink
            )
            Text(
                text = stringResource(
                    R.string.board_size,
                    puzzleState.size,
                    puzzleState.size
                ),
                style = MaterialTheme.typography.labelMedium,
                color = RegionPalette.muted
            )
        }
        Hearts(hearts = puzzleState.hearts)
    }
}

@Composable
private fun Hearts(hearts: Int) {
    val description = pluralStringResource(
        R.plurals.hearts_description,
        hearts,
        hearts
    )
    Row(
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (index in 0 until PuzzleState.STARTING_HEARTS) {
            Icon(
                imageVector = if (index < hearts) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                contentDescription = null,
                tint = if (index < hearts) {
                    RegionPalette.coral
                } else {
                    RegionPalette.muted.copy(alpha = 0.4f)
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun HintBanner(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(140)),
        exit = fadeOut(animationSpec = tween(120))
    ) {
        Surface(
            shape = CircleShape,
            color = RegionPalette.gold.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, RegionPalette.gold.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = RegionPalette.gold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.hint_banner),
                    style = MaterialTheme.typography.labelLarge,
                    color = RegionPalette.gold
                )
            }
        }
    }
}

@Composable
private fun BoardArea(
    uiState: PuzzleUiState,
    puzzleState: PuzzleState,
    actions: GameActions,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val boardSize = minOf(maxWidth, maxHeight)
        Surface(
            modifier = Modifier.size(boardSize),
            shape = RoundedCornerShape(28.dp),
            color = RegionPalette.surface,
            shadowElevation = 12.dp
        ) {
            PuzzleBoard(
                state = puzzleState,
                autoMarkEnabled = uiState.settings.autoMarkEnabled,
                regionLettersEnabled = uiState.settings.regionLettersEnabled,
                onTap = actions.onNote,
                onDoubleTap = actions.onCat,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun GameActionBar(puzzleState: PuzzleState, actions: GameActions) {
    val playing = puzzleState.status == PuzzleStatus.PLAYING
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GameActionButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            label = stringResource(R.string.undo),
            enabled = playing && puzzleState.undoStack.isNotEmpty(),
            onClick = actions.onUndo
        )
        GameActionButton(
            icon = Icons.Filled.Lightbulb,
            label = stringResource(R.string.hint),
            enabled = playing,
            onClick = actions.onHint
        )
    }
}

@Composable
private fun GameActionButton(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = RegionPalette.surfaceRaised,
                contentColor = RegionPalette.ink,
                disabledContainerColor = RegionPalette.surfaceRaised.copy(alpha = 0.5f),
                disabledContentColor = RegionPalette.muted.copy(alpha = 0.35f)
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = RegionPalette.muted
        )
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
