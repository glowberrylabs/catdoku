package com.glowberrylabs.catdoku.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzlePackParserTest {

    private val validRecord = "L|4|0001201122312333|1,7,8,14|1"

    @Test
    fun parsesGeneratedPackWithExpectedShape() {
        val pack = PuzzlePackParser.parse(PuzzleData.RAW)

        assertEquals(500, pack.levels.size)
        assertEquals(365, pack.dailies.size)
        assertEquals(
            mapOf(4 to 20, 5 to 60, 6 to 120, 7 to 150, 8 to 150),
            pack.levels.groupingBy { it.size }.eachCount()
        )
        assertTrue(pack.dailies.all { it.size == 7 })
        assertEquals(pack.levels.map { it.size }.sorted(), pack.levels.map { it.size })
        for (size in 4..8) {
            val difficulties = pack.levels.filter { it.size == size }.map { it.difficulty }
            assertEquals(difficulties.sorted(), difficulties)
        }
    }

    @Test
    fun everyGeneratedPuzzleHasOneSolution() {
        val pack = PuzzlePackParser.parse(PuzzleData.RAW)

        pack.levels.forEachIndexed { index, puzzle ->
            assertEquals("level ${index + 1}", 1, SolutionCounter.count(puzzle))
        }
        pack.dailies.forEachIndexed { index, puzzle ->
            assertEquals("daily ${index + 1}", 1, SolutionCounter.count(puzzle))
        }
    }

    @Test
    fun parsesValidLevelAndDailyRecords() {
        val raw = "$validRecord\nD|4|0001201122312333|1,7,8,14|1"

        val pack = PuzzlePackParser.parse(raw)

        assertEquals(1, pack.levels.size)
        assertEquals(1, pack.dailies.size)
        assertEquals(4, pack.levels[0].size)
    }

    @Test
    fun rejectsWrongFieldCount() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            PuzzlePackParser.parse("L|4|0001201122312333|1,7,8,14")
        }

        assertTrue(error.message.orEmpty().contains("expected 5 fields"))
    }

    @Test
    fun rejectsDisconnectedRegion() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            PuzzlePackParser.parse("L|4|0101223322332233|1,7,8,14|1")
        }

        assertTrue(error.message.orEmpty().contains("not connected"))
    }

    @Test
    fun rejectsDuplicateHintCell() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            PuzzlePackParser.parse("L|4|0001201122312333|1,1,8,14|1")
        }

        assertTrue(error.message.orEmpty().contains("duplicate hint cell"))
    }

    @Test
    fun rejectsTouchingCats() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            PuzzlePackParser.parse("L|4|0001201122312333|1,6,8,15|1")
        }

        assertTrue(error.message.orEmpty().contains("cats touch"))
    }

    @Test
    fun rejectsEmptyPack() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            PuzzlePackParser.parse("# empty")
        }

        assertTrue(error.message.orEmpty().contains("level pack is empty"))
    }
}
