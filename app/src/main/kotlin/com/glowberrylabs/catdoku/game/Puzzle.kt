package com.glowberrylabs.catdoku.game

class Puzzle(
    val size: Int,
    val regions: IntArray,
    val solution: Set<Position>,
    val hintOrder: List<Position>,
    val difficulty: Int = 0
) {

    val solutionIndices: Set<Int> = solution.map { indexOf(it) }.toSet()
    val hintIndices: List<Int> = hintOrder.map { indexOf(it) }

    init {
        require(size in MIN_SIZE..MAX_SIZE) { "Unsupported puzzle size: $size" }
        require(regions.size == size * size) { "Region grid does not match size" }
        require(solution.size == size) { "Solution must contain one cell per row" }
        require(hintOrder.size == size) { "Hint order must contain one cell per row" }
        require(solution == hintOrder.toSet()) { "Hint order must contain the solution" }
    }

    fun regionAt(row: Int, col: Int): Int = regions[row * size + col]

    fun regionAt(index: Int): Int = regions[index]

    fun indexOf(position: Position): Int = position.row * size + position.col

    fun positionOf(index: Int): Position = Position.fromIndex(index, size)

    fun contains(position: Position): Boolean = solution.contains(position)

    companion object {
        const val MIN_SIZE = 4
        const val MAX_SIZE = 8
    }
}
