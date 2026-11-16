package com.glowberrylabs.catdoku.game

object AutoMarkResolver {

    fun autoMarks(state: PuzzleState): Set<Int> {
        val cats = state.cats
        if (cats.isEmpty()) return emptySet()
        val puzzle = state.puzzle
        val marked = mutableSetOf<Int>()

        for (catIndex in cats) {
            val cat = puzzle.positionOf(catIndex)
            markRow(marked, cat, puzzle.size)
            markColumn(marked, cat, puzzle.size)
            markRegion(marked, cat, puzzle)
            markNeighbors(marked, cat, puzzle.size)
        }

        marked.removeAll(cats)
        marked.removeAll { state.marks[it] == CellMark.MISTAKE }
        return marked
    }

    private fun markRow(marked: MutableSet<Int>, cat: Position, size: Int) {
        for (col in 0 until size) {
            marked += cat.row * size + col
        }
    }

    private fun markColumn(marked: MutableSet<Int>, cat: Position, size: Int) {
        for (row in 0 until size) {
            marked += row * size + cat.col
        }
    }

    private fun markRegion(marked: MutableSet<Int>, cat: Position, puzzle: Puzzle) {
        val region = puzzle.regionAt(cat.row, cat.col)
        for (index in puzzle.regions.indices) {
            if (puzzle.regions[index] == region) {
                marked += index
            }
        }
    }

    private fun markNeighbors(marked: MutableSet<Int>, cat: Position, size: Int) {
        for (row in (cat.row - 1)..(cat.row + 1)) {
            for (col in (cat.col - 1)..(cat.col + 1)) {
                if (row in 0 until size && col in 0 until size) {
                    marked += row * size + col
                }
            }
        }
    }
}
