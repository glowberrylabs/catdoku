package com.glowberrylabs.catdoku.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.glowberrylabs.catdoku.R
import com.glowberrylabs.catdoku.game.AppScreen
import com.glowberrylabs.catdoku.game.AppSettings
import com.glowberrylabs.catdoku.game.PuzzleMode
import com.glowberrylabs.catdoku.game.PuzzleState
import com.glowberrylabs.catdoku.game.PuzzleStatus
import com.glowberrylabs.catdoku.game.PuzzleUiState

@Composable
fun AppOverlays(
    uiState: PuzzleUiState,
    gameActions: GameActions,
    settingsActions: SettingsActions,
    rulesActions: RulesActions,
    resultActions: ResultActions,
    modifier: Modifier = Modifier
) {
    val puzzleState = uiState.puzzleState
    val inGame = uiState.screen == AppScreen.GAME

    OverlayHost(visible = uiState.showSettings, modifier = modifier) {
        SettingsCard(
            settings = uiState.settings,
            actions = settingsActions
        )
    }
    OverlayHost(visible = uiState.showRules, modifier = modifier) {
        RulesCard(actions = rulesActions)
    }
    OverlayHost(visible = uiState.showPause, modifier = modifier) {
        PauseCard(actions = gameActions)
    }
    OverlayHost(
        visible = inGame && puzzleState?.status == PuzzleStatus.SOLVED,
        modifier = modifier
    ) {
        if (puzzleState != null) {
            SolvedCard(
                uiState = uiState,
                puzzleState = puzzleState,
                actions = resultActions
            )
        }
    }
    OverlayHost(
        visible = inGame && puzzleState?.status == PuzzleStatus.FAILED,
        modifier = modifier
    ) {
        FailedCard(actions = resultActions)
    }
}

@Composable
private fun OverlayHost(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = tween(160)),
            exit = fadeOut(animationSpec = tween(120))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RegionPalette.scrim)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent().changes.forEach { it.consume() }
                            }
                        }
                    }
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(animationSpec = tween(160)) +
                scaleIn(animationSpec = tween(160), initialScale = 0.94f),
            exit = fadeOut(animationSpec = tween(120)) +
                scaleOut(animationSpec = tween(120), targetScale = 0.96f)
        ) {
            content()
        }
    }
}

@Composable
private fun OverlayCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth(0.92f),
        shape = RoundedCornerShape(32.dp),
        color = RegionPalette.surfaceRaised,
        shadowElevation = 18.dp
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsCard(settings: AppSettings, actions: SettingsActions) {
    OverlayCard {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = RegionPalette.ink
        )
        SettingRow(
            icon = if (settings.soundEnabled) {
                Icons.AutoMirrored.Filled.VolumeUp
            } else {
                Icons.AutoMirrored.Filled.VolumeOff
            },
            title = stringResource(R.string.sound),
            description = stringResource(R.string.sound_description),
            checked = settings.soundEnabled,
            onToggle = actions.onToggleSound
        )
        SettingRow(
            icon = Icons.Filled.Vibration,
            title = stringResource(R.string.haptics),
            description = stringResource(R.string.haptics_description),
            checked = settings.hapticsEnabled,
            onToggle = actions.onToggleHaptics
        )
        SettingRow(
            icon = Icons.Filled.Visibility,
            title = stringResource(R.string.auto_mark),
            description = stringResource(R.string.auto_mark_description),
            checked = settings.autoMarkEnabled,
            onToggle = actions.onToggleAutoMark
        )
        SettingRow(
            icon = Icons.AutoMirrored.Filled.Label,
            title = stringResource(R.string.region_letters),
            description = stringResource(R.string.region_letters_description),
            checked = settings.regionLettersEnabled,
            onToggle = actions.onToggleRegionLetters
        )
        PrimaryButton(
            text = stringResource(R.string.done),
            onClick = actions.onClose
        )
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, description: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = { onToggle() }
            )
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(RegionPalette.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RegionPalette.gold,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = RegionPalette.ink
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = RegionPalette.muted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RegionPalette.backgroundTop,
                checkedTrackColor = RegionPalette.gold,
                uncheckedThumbColor = RegionPalette.muted,
                uncheckedTrackColor = RegionPalette.surface
            )
        )
    }
}

@Composable
private fun RulesCard(actions: RulesActions) {
    OverlayCard {
        Text(
            text = stringResource(R.string.how_to_play),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = RegionPalette.ink
        )
        Text(
            text = stringResource(R.string.rules_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = RegionPalette.muted,
            textAlign = TextAlign.Center
        )
        RuleDiagram()
        RuleRow(number = 1, text = stringResource(R.string.rule_row))
        RuleRow(number = 2, text = stringResource(R.string.rule_column))
        RuleRow(number = 3, text = stringResource(R.string.rule_region))
        RuleRow(number = 4, text = stringResource(R.string.rule_touch))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.rule_tap),
                style = MaterialTheme.typography.bodySmall,
                color = RegionPalette.muted
            )
            Text(
                text = stringResource(R.string.rule_double_tap),
                style = MaterialTheme.typography.bodySmall,
                color = RegionPalette.muted
            )
            Text(
                text = stringResource(R.string.rule_hearts),
                style = MaterialTheme.typography.bodySmall,
                color = RegionPalette.muted
            )
        }
        PrimaryButton(
            text = stringResource(R.string.got_it),
            onClick = actions.onClose
        )
    }
}

@Composable
private fun RuleRow(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(RegionPalette.gold.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RegionPalette.gold
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = RegionPalette.ink
        )
    }
}

@Composable
private fun RuleDiagram() {
    val regions = listOf(
        intArrayOf(0, 0, 1, 1),
        intArrayOf(0, 2, 1, 1),
        intArrayOf(2, 2, 3, 1),
        intArrayOf(2, 3, 3, 3)
    )
    val description = stringResource(R.string.rule_diagram_description)
    Column(
        modifier = Modifier
            .width(168.dp)
            .aspectRatio(1f)
            .clearAndSetSemantics { contentDescription = description }
    ) {
        regions.forEachIndexed { row, columns ->
            Row(modifier = Modifier.weight(1f)) {
                columns.forEachIndexed { col, region ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                            .background(
                                color = RegionPalette.region(region),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (row == 0 && col == 1) {
                            Image(
                                painter = painterResource(R.drawable.ic_cat),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(0.72f)
                            )
                        } else if (isRuleNoteCell(row, col)) {
                            Text(
                                text = "X",
                                color = RegionPalette.ink.copy(alpha = 0.45f),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isRuleNoteCell(row: Int, col: Int): Boolean = (row == 1 && col == 1) || (row == 2 && col == 0)

@Composable
private fun PauseCard(actions: GameActions) {
    OverlayCard {
        Text(
            text = stringResource(R.string.paused),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = RegionPalette.ink
        )
        PrimaryButton(
            text = stringResource(R.string.resume),
            onClick = actions.onResume
        )
        SecondaryButton(
            text = stringResource(R.string.restart),
            onClick = actions.onRestart
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = actions.onOpenRules) {
                Text(
                    text = stringResource(R.string.how_to_play),
                    color = RegionPalette.muted
                )
            }
            TextButton(onClick = actions.onOpenSettings) {
                Text(
                    text = stringResource(R.string.settings),
                    color = RegionPalette.muted
                )
            }
        }
        TextButton(onClick = actions.onQuitToMenu) {
            Text(
                text = stringResource(R.string.quit_to_menu),
                color = RegionPalette.coral
            )
        }
    }
}

@Composable
private fun SolvedCard(uiState: PuzzleUiState, puzzleState: PuzzleState, actions: ResultActions) {
    OverlayCard {
        SolvedHeader(uiState = uiState, puzzleState = puzzleState)
        Text(
            text = pluralStringResource(
                R.plurals.hearts_description,
                puzzleState.hearts,
                puzzleState.hearts
            ) + "  ·  " + stringResource(R.string.hints_used, puzzleState.hintsUsed),
            style = MaterialTheme.typography.labelMedium,
            color = RegionPalette.muted
        )
        val showNext = puzzleState.mode == PuzzleMode.LEVEL && uiState.nextLevelNumber != null
        PrimaryButton(
            text = if (showNext) {
                stringResource(R.string.next_level)
            } else {
                stringResource(R.string.back_to_menu)
            },
            onClick = if (showNext) actions.onNext else actions.onQuitToMenu
        )
        if (showNext) {
            TextButton(onClick = actions.onQuitToMenu) {
                Text(
                    text = stringResource(R.string.back_to_menu),
                    color = RegionPalette.muted
                )
            }
        }
    }
}

@Composable
private fun SolvedHeader(uiState: PuzzleUiState, puzzleState: PuzzleState) {
    Image(
        painter = painterResource(R.drawable.ic_cat),
        contentDescription = null,
        modifier = Modifier.size(88.dp)
    )
    Text(
        text = stringResource(R.string.solved_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = RegionPalette.gold
    )
    Text(
        text = if (puzzleState.mode == PuzzleMode.LEVEL) {
            stringResource(
                R.string.level_complete,
                puzzleState.levelNumber ?: 0
            )
        } else {
            stringResource(R.string.daily_complete_title)
        },
        style = MaterialTheme.typography.titleMedium,
        color = RegionPalette.ink,
        textAlign = TextAlign.Center
    )
    val showStreak = puzzleState.mode == PuzzleMode.DAILY && uiState.daily.currentStreak > 0
    if (showStreak) {
        Text(
            text = pluralStringResource(
                R.plurals.daily_streak,
                uiState.daily.currentStreak,
                uiState.daily.currentStreak
            ),
            style = MaterialTheme.typography.labelLarge,
            color = RegionPalette.mint
        )
    }
}

@Composable
private fun FailedCard(actions: ResultActions) {
    OverlayCard {
        Text(
            text = stringResource(R.string.out_of_hearts),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = RegionPalette.coral,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.out_of_hearts_body),
            style = MaterialTheme.typography.bodyMedium,
            color = RegionPalette.muted,
            textAlign = TextAlign.Center
        )
        PrimaryButton(
            text = stringResource(R.string.try_again),
            onClick = actions.onRetry
        )
        SecondaryButton(
            text = stringResource(R.string.back_to_menu),
            onClick = actions.onQuitToMenu
        )
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = RegionPalette.gold,
            contentColor = RegionPalette.backgroundTop
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, RegionPalette.surface),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = RegionPalette.ink
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
