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
    val onQuitToMenu: () -> Unit
)
