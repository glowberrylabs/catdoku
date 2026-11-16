package com.glowberrylabs.catdoku.data

import com.glowberrylabs.catdoku.game.CellMark
import com.glowberrylabs.catdoku.game.PuzzleMode
import com.glowberrylabs.catdoku.game.PuzzleState

data class SavedBoard(
    val mode: PuzzleMode,
    val levelNumber: Int?,
    val epochDay: Long?,
    val size: Int,
    val marks: List<CellMark>,
    val hearts: Int,
    val hintsUsed: Int
)

object BoardCodec {

    fun encodeLevelBoard(state: PuzzleState): String? {
        val levelNumber = state.levelNumber ?: return null
        if (state.mode != PuzzleMode.LEVEL) return null
        return encode(levelNumber.toString(), state)
    }

    fun encodeDailyBoard(state: PuzzleState, epochDay: Long): String? {
        if (state.mode != PuzzleMode.DAILY) return null
        return encode(epochDay.toString(), state)
    }

    fun decodeLevelBoard(value: String): SavedBoard? {
        val parts = decodeParts(value) ?: return null
        val levelNumber = parts[0].toIntOrNull() ?: return null
        if (levelNumber <= 0) return null
        return decodeBoard(parts, PuzzleMode.LEVEL, levelNumber, null)
    }

    fun decodeDailyBoard(value: String): SavedBoard? {
        val parts = decodeParts(value) ?: return null
        val epochDay = parts[0].toLongOrNull() ?: return null
        return decodeBoard(parts, PuzzleMode.DAILY, null, epochDay)
    }

    fun encodeMarks(marks: List<CellMark>): String = buildString(marks.size) {
        marks.forEach { mark ->
            append(
                when (mark) {
                    CellMark.EMPTY -> '0'
                    CellMark.NOTE -> 'n'
                    CellMark.MISTAKE -> 'm'
                    CellMark.CAT -> 'c'
                }
            )
        }
    }

    fun decodeMarks(value: String, size: Int): List<CellMark>? {
        if (value.length != size * size) return null
        return value.map { character ->
            when (character) {
                '0' -> CellMark.EMPTY
                'n' -> CellMark.NOTE
                'm' -> CellMark.MISTAKE
                'c' -> CellMark.CAT
                else -> return null
            }
        }
    }

    private fun encode(identity: String, state: PuzzleState): String {
        val marks = encodeMarks(state.marks)
        return "$identity|${state.size}|${state.hearts}|${state.hintsUsed}|$marks"
    }

    private fun decodeParts(value: String): List<String>? {
        val parts = value.split('|')
        if (parts.size != FIELD_COUNT) return null
        return parts
    }

    private fun decodeBoard(parts: List<String>, mode: PuzzleMode, levelNumber: Int?, epochDay: Long?): SavedBoard? {
        val size = parts[1].toIntOrNull()
        if (size == null || size !in MIN_SIZE..MAX_SIZE) return null
        val hearts = parts[2].toIntOrNull()
        if (hearts == null || hearts !in 0..MAX_HEARTS) return null
        val hintsUsed = parts[3].toIntOrNull()
        if (hintsUsed == null || hintsUsed < 0) return null
        val marks = decodeMarks(parts[4], size) ?: return null
        return SavedBoard(
            mode = mode,
            levelNumber = levelNumber,
            epochDay = epochDay,
            size = size,
            marks = marks,
            hearts = hearts,
            hintsUsed = hintsUsed
        )
    }

    private const val FIELD_COUNT = 5
    private const val MIN_SIZE = 4
    private const val MAX_SIZE = 8
    private const val MAX_HEARTS = 3
}
