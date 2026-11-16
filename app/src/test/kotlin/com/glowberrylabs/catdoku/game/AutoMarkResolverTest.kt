package com.glowberrylabs.catdoku.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoMarkResolverTest {

    private val engine = PuzzleEngine()
    private val puzzle = testPuzzle()

    @Test
    fun catMarksRowColumnRegionAndNeighbors() {
        val state = engine.toggleCat(
            engine.newState(puzzle, PuzzleMode.LEVEL, levelNumber = 1),
            Position(0, 1)
        ).state

        val marks = AutoMarkResolver.autoMarks(state)

        assertTrue(0 in marks)
        assertTrue(2 in marks)
        assertTrue(3 in marks)
        assertTrue(4 in marks)
        assertTrue(5 in marks)
        assertTrue(6 in marks)
        assertTrue(9 in marks)
        assertTrue(13 in marks)
        assertFalse(1 in marks)
        assertFalse(7 in marks)
    }

    @Test
    fun noCatsMeansNoAutoMarks() {
        val state = engine.newState(puzzle, PuzzleMode.LEVEL, levelNumber = 1)

        assertTrue(AutoMarkResolver.autoMarks(state).isEmpty())
    }

    @Test
    fun removingCatClearsAutoMarks() {
        val placed = engine.toggleCat(
            engine.newState(puzzle, PuzzleMode.LEVEL, levelNumber = 1),
            Position(0, 1)
        ).state
        val removed = engine.toggleCat(placed, Position(0, 1)).state

        assertTrue(AutoMarkResolver.autoMarks(removed).isEmpty())
    }

    @Test
    fun mistakeCellsStayMistakes() {
        val state = engine.toggleCat(
            engine.newState(puzzle, PuzzleMode.LEVEL, levelNumber = 1),
            Position(0, 0)
        ).state
        val withCat = engine.toggleCat(state, Position(0, 1)).state

        assertEquals(CellMark.MISTAKE, withCat.markAt(0, 0))
        assertFalse(0 in AutoMarkResolver.autoMarks(withCat))
    }
}
