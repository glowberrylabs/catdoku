package com.glowberrylabs.catdoku.game

data class EngineResult(val state: PuzzleState, val events: List<PuzzleEvent> = emptyList())
