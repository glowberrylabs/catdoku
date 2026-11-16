package com.glowberrylabs.catdoku.game

data class PuzzleState(
    val puzzle: Puzzle,
    val mode: PuzzleMode,
    val levelNumber: Int?,
    val marks: List<CellMark>,
    val hearts: Int,
    val hintsUsed: Int,
    val status: PuzzleStatus,
    val undoStack: List<PuzzleMove>,
    val hintCell: Position? = null
) {

    val size: Int get() = puzzle.size

    val cats: Set<Int>
        get() = marks.indices.filterTo(mutableSetOf()) { marks[it] == CellMark.CAT }

    fun markAt(position: Position): CellMark = marks[puzzle.indexOf(position)]

    fun markAt(row: Int, col: Int): CellMark = marks[row * size + col]

    fun isSolved(): Boolean = cats.size == size

    companion object {
        const val STARTING_HEARTS = 3
    }
}
