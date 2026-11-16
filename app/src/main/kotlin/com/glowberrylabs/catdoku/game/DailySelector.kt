package com.glowberrylabs.catdoku.game

data class DailyProgress(val lastSolvedEpochDay: Long? = null, val currentStreak: Int = 0, val totalSolves: Int = 0)

object DailySelector {

    fun puzzleFor(epochDay: Long, dailies: List<Puzzle>): Puzzle? {
        if (dailies.isEmpty()) return null
        val index = Math.floorMod(epochDay, dailies.size.toLong()).toInt()
        return dailies[index]
    }

    fun recordSolve(progress: DailyProgress, todayEpochDay: Long): DailyProgress {
        if (progress.lastSolvedEpochDay == todayEpochDay) {
            return progress
        }
        val streak = if (progress.lastSolvedEpochDay == todayEpochDay - 1) {
            progress.currentStreak + 1
        } else {
            1
        }
        return progress.copy(
            lastSolvedEpochDay = todayEpochDay,
            currentStreak = streak,
            totalSolves = progress.totalSolves + 1
        )
    }
}
