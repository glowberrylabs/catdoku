package com.glowberrylabs.catdoku.ui

import com.glowberrylabs.catdoku.game.Position

data class GameActions(
    val onNote: (Position) -> Unit,
    val onCat: (Position) -> Unit,
    val onUndo: () -> Unit,
    val onHint: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onRestart: () -> Unit,
    val onQuitToMenu: () -> Unit,
    val onOpenRules: () -> Unit,
    val onOpenSettings: () -> Unit
)

data class MenuActions(
    val onContinue: () -> Unit,
    val onDaily: () -> Unit,
    val onOpenRules: () -> Unit,
    val onOpenSettings: () -> Unit
)

data class SettingsActions(
    val onToggleSound: () -> Unit,
    val onToggleHaptics: () -> Unit,
    val onToggleAutoMark: () -> Unit,
    val onToggleRegionLetters: () -> Unit,
    val onClose: () -> Unit
)

data class RulesActions(val onClose: () -> Unit)

data class ResultActions(val onNext: () -> Unit, val onRetry: () -> Unit, val onQuitToMenu: () -> Unit)
