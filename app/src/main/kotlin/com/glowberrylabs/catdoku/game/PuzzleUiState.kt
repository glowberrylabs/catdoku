package com.glowberrylabs.catdoku.game

data class ProgressSummary(val highestSolvedLevel: Int = 0, val totalSolved: Int = 0, val totalHints: Int = 0)

data class DailySummary(val currentStreak: Int = 0, val totalSolves: Int = 0, val solvedToday: Boolean = false)

data class PuzzleUiState(
    val screen: AppScreen = AppScreen.MENU,
    val puzzleState: PuzzleState? = null,
    val settings: AppSettings = AppSettings(),
    val progress: ProgressSummary = ProgressSummary(),
    val daily: DailySummary = DailySummary(),
    val levelCount: Int = 0,
    val todayEpochDay: Long = 0,
    val showRules: Boolean = false,
    val showSettings: Boolean = false,
    val showPause: Boolean = false
) {

    val nextLevelNumber: Int?
        get() = if (levelCount > 0 && progress.highestSolvedLevel < levelCount) {
            progress.highestSolvedLevel + 1
        } else {
            null
        }

    val allLevelsComplete: Boolean
        get() = levelCount > 0 && progress.highestSolvedLevel >= levelCount
}
