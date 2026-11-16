package com.glowberrylabs.catdoku.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleEngineTest {

    private val engine = PuzzleEngine()
    private val puzzle = testPuzzle()

    private fun newState(): PuzzleState = engine.newState(puzzle, PuzzleMode.LEVEL, levelNumber = 1)

    private fun place(state: PuzzleState, row: Int, col: Int): EngineResult =
        engine.toggleCat(state, Position(row, col))

    private fun note(state: PuzzleState, row: Int, col: Int): EngineResult =
        engine.toggleNote(state, Position(row, col))

    @Test
    fun noteTogglesBetweenEmptyAndNote() {
        val first = note(newState(), 3, 3)
        assertEquals(CellMark.NOTE, first.state.markAt(3, 3))
        assertEquals(listOf(PuzzleEvent.NOTE_ADDED), first.events)

        val second = note(first.state, 3, 3)
        assertEquals(CellMark.EMPTY, second.state.markAt(3, 3))
        assertEquals(listOf(PuzzleEvent.NOTE_REMOVED), second.events)
    }

    @Test
    fun correctCatClearsNoteAndKeepsHeart() {
        val noted = note(newState(), 0, 1).state
        val placed = place(noted, 0, 1)

        assertEquals(CellMark.CAT, placed.state.markAt(0, 1))
        assertEquals(3, placed.state.hearts)
        assertTrue(placed.events.contains(PuzzleEvent.CAT_PLACED))
    }

    @Test
    fun wrongCatLosesHeartAndLeavesMistake() {
        val result = place(newState(), 0, 0)

        assertEquals(CellMark.MISTAKE, result.state.markAt(0, 0))
        assertEquals(2, result.state.hearts)
        assertEquals(PuzzleStatus.PLAYING, result.state.status)
        assertEquals(listOf(PuzzleEvent.WRONG), result.events)
    }

    @Test
    fun repeatedWrongCatDoesNotCostAnotherHeart() {
        val first = place(newState(), 0, 0).state
        val second = place(first, 0, 0)

        assertSame(first, second.state)
        assertEquals(2, second.state.hearts)
    }

    @Test
    fun thirdWrongCatFailsBoard() {
        var state = place(newState(), 0, 0).state
        state = place(state, 0, 2).state
        val result = place(state, 0, 3)

        assertEquals(0, result.state.hearts)
        assertEquals(PuzzleStatus.FAILED, result.state.status)
        assertTrue(result.events.contains(PuzzleEvent.FAILED))
    }

    @Test
    fun catCanBeRemoved() {
        val placed = place(newState(), 0, 1).state
        val removed = place(placed, 0, 1)

        assertEquals(CellMark.EMPTY, removed.state.markAt(0, 1))
        assertEquals(3, removed.state.hearts)
        assertEquals(listOf(PuzzleEvent.CAT_REMOVED), removed.events)
    }

    @Test
    fun undoRestoresNoteCatAndRemoval() {
        val noted = note(newState(), 3, 3).state
        val undoneNote = engine.undo(noted).state
        assertEquals(CellMark.EMPTY, undoneNote.markAt(3, 3))

        val placed = place(newState(), 0, 1).state
        val undonePlace = engine.undo(placed).state
        assertEquals(CellMark.EMPTY, undonePlace.markAt(0, 1))

        val removed = place(placed, 0, 1).state
        val undoneRemoval = engine.undo(removed).state
        assertEquals(CellMark.CAT, undoneRemoval.markAt(0, 1))
    }

    @Test
    fun undoDoesNotRefundHeartOrClearMistake() {
        val wrong = place(newState(), 0, 0).state
        val undone = engine.undo(wrong)

        assertSame(wrong, undone.state)
        assertEquals(2, undone.state.hearts)
        assertEquals(CellMark.MISTAKE, undone.state.markAt(0, 0))
    }

    @Test
    fun placingAllCatsSolvesBoardAndDisablesUndo() {
        var state = newState()
        val solution = puzzle.hintOrder
        solution.forEach { position ->
            state = place(state, position.row, position.col).state
        }

        assertEquals(PuzzleStatus.SOLVED, state.status)
        val undone = engine.undo(state)
        assertSame(state, undone.state)
    }

    @Test
    fun hintReturnsFirstUnplacedCellAndCounts() {
        val first = engine.hint(newState())
        assertEquals(puzzle.hintOrder.first(), first.state.hintCell)
        assertEquals(1, first.state.hintsUsed)

        val placed = place(first.state, 0, 1).state
        val second = engine.hint(placed)
        assertEquals(puzzle.hintOrder[1], second.state.hintCell)
        assertEquals(2, second.state.hintsUsed)
    }

    @Test
    fun hintClearsOnBoardAction() {
        val hinted = engine.hint(newState()).state
        val noted = note(hinted, 3, 3)

        assertNull(noted.state.hintCell)
    }

    @Test
    fun restartResetsBoard() {
        var state = place(newState(), 0, 0).state
        state = place(state, 0, 1).state
        state = engine.hint(state).state

        val restarted = engine.restart(state).state

        assertEquals(3, restarted.hearts)
        assertEquals(0, restarted.hintsUsed)
        assertEquals(PuzzleStatus.PLAYING, restarted.status)
        assertTrue(restarted.undoStack.isEmpty())
        assertNull(restarted.hintCell)
        assertTrue(restarted.marks.all { it == CellMark.EMPTY })
        assertEquals(listOf(PuzzleEvent.RESTARTED), engine.restart(state).events)
    }

    @Test
    fun outOfBoundsActionIsIgnored() {
        val state = newState()
        val result = engine.toggleNote(state, Position(4, 0))

        assertSame(state, result.state)
    }
}
