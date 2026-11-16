package com.glowberrylabs.catdoku.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LevelAndDailyTest {

    @Test
    fun levelNumberMapsToPackIndex() {
        assertEquals(1, LevelRamp.levelNumber(0))
        assertEquals(0, LevelRamp.index(1))
    }

    @Test
    fun puzzleForLevelUsesOneBasedNumber() {
        val pack = PuzzlePack(
            levels = listOf(testPuzzle(), testPuzzle()),
            dailies = listOf(testPuzzle())
        )

        assertSame(pack.levels[0], LevelRamp.puzzleFor(pack, 1))
        assertSame(pack.levels[1], LevelRamp.puzzleFor(pack, 2))
        assertNull(LevelRamp.puzzleFor(pack, 3))
    }

    @Test
    fun nextLevelStopsAfterPack() {
        val pack = PuzzlePack(
            levels = listOf(testPuzzle(), testPuzzle()),
            dailies = listOf(testPuzzle())
        )

        assertEquals(1, LevelRamp.nextLevelNumber(pack, highestSolvedLevel = 0))
        assertEquals(2, LevelRamp.nextLevelNumber(pack, highestSolvedLevel = 1))
        assertNull(LevelRamp.nextLevelNumber(pack, highestSolvedLevel = 2))
    }

    @Test
    fun dailyPuzzleWrapsWithEpochDay() {
        val dailies = listOf(testPuzzle(), testPuzzle(), testPuzzle())

        assertSame(dailies[0], DailySelector.puzzleFor(0, dailies))
        assertSame(dailies[1], DailySelector.puzzleFor(1, dailies))
        assertSame(dailies[2], DailySelector.puzzleFor(-1, dailies))
        assertSame(dailies[0], DailySelector.puzzleFor(3, dailies))
    }

    @Test
    fun dailyStreakStartsAtOne() {
        val result = DailySelector.recordSolve(DailyProgress(), todayEpochDay = 10)

        assertEquals(10L, result.lastSolvedEpochDay)
        assertEquals(1, result.currentStreak)
        assertEquals(1, result.totalSolves)
    }

    @Test
    fun repeatedDailySolveDoesNotChangeStreak() {
        val first = DailySelector.recordSolve(DailyProgress(), todayEpochDay = 10)
        val repeat = DailySelector.recordSolve(first, todayEpochDay = 10)

        assertEquals(first, repeat)
    }

    @Test
    fun consecutiveDailySolveIncrementsStreak() {
        val first = DailySelector.recordSolve(DailyProgress(), todayEpochDay = 10)
        val second = DailySelector.recordSolve(first, todayEpochDay = 11)

        assertEquals(2, second.currentStreak)
        assertEquals(2, second.totalSolves)
    }

    @Test
    fun missedDayResetsStreak() {
        val first = DailySelector.recordSolve(DailyProgress(), todayEpochDay = 10)
        val later = DailySelector.recordSolve(first, todayEpochDay = 13)

        assertEquals(1, later.currentStreak)
        assertEquals(2, later.totalSolves)
    }
}
