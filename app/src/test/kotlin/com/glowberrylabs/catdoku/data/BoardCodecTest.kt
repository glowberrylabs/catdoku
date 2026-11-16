package com.glowberrylabs.catdoku.data

import com.glowberrylabs.catdoku.game.CellMark
import com.glowberrylabs.catdoku.game.Position
import com.glowberrylabs.catdoku.game.PuzzleEngine
import com.glowberrylabs.catdoku.game.PuzzleMode
import com.glowberrylabs.catdoku.game.PuzzleState
import com.glowberrylabs.catdoku.game.testPuzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BoardCodecTest {

    private val engine = PuzzleEngine()

    private fun state(mode: PuzzleMode): PuzzleState {
        var state = engine.newState(testPuzzle(), mode, levelNumber = 7)
        state = engine.toggleNote(state, Position(3, 3)).state
        state = engine.toggleCat(state, Position(0, 1)).state
        state = engine.toggleCat(state, Position(0, 0)).state
        return state
    }

    @Test
    fun marksRoundTrip() {
        val marks = listOf(
            CellMark.EMPTY,
            CellMark.NOTE,
            CellMark.MISTAKE,
            CellMark.CAT
        )

        val encoded = BoardCodec.encodeMarks(marks)

        assertEquals("0nmc", encoded)
        assertEquals(marks, BoardCodec.decodeMarks(encoded, 2))
    }

    @Test
    fun levelBoardRoundTrip() {
        val state = state(PuzzleMode.LEVEL)

        val encoded = BoardCodec.encodeLevelBoard(state)
        val decoded = BoardCodec.decodeLevelBoard(encoded.orEmpty())

        assertEquals(7, decoded?.levelNumber)
        assertEquals(PuzzleMode.LEVEL, decoded?.mode)
        assertEquals(4, decoded?.size)
        assertEquals(2, decoded?.hearts)
        assertEquals(0, decoded?.hintsUsed)
        assertEquals(state.marks, decoded?.marks)
    }

    @Test
    fun dailyBoardRoundTrip() {
        val state = state(PuzzleMode.DAILY)

        val encoded = BoardCodec.encodeDailyBoard(state, 20_000L)
        val decoded = BoardCodec.decodeDailyBoard(encoded.orEmpty())

        assertEquals(20_000L, decoded?.epochDay)
        assertEquals(PuzzleMode.DAILY, decoded?.mode)
        assertNull(decoded?.levelNumber)
        assertEquals(state.marks, decoded?.marks)
    }

    @Test
    fun boardEncodingRequiresMatchingMode() {
        assertNull(BoardCodec.encodeLevelBoard(state(PuzzleMode.DAILY)))
        assertNull(BoardCodec.encodeDailyBoard(state(PuzzleMode.LEVEL), 20_000L))
    }

    @Test
    fun rejectsWrongMarkLength() {
        assertNull(BoardCodec.decodeMarks("0nm", 2))
    }

    @Test
    fun rejectsUnknownMarkCharacter() {
        assertNull(BoardCodec.decodeMarks("0nmz", 2))
    }

    @Test
    fun rejectsWrongFieldCount() {
        assertNull(BoardCodec.decodeLevelBoard("7|4|3|0"))
    }

    @Test
    fun rejectsInvalidHearts() {
        assertNull(BoardCodec.decodeLevelBoard("7|4|4|0|0000"))
    }

    @Test
    fun rejectsInvalidSize() {
        assertNull(BoardCodec.decodeLevelBoard("7|10|3|0|0000"))
    }
}
