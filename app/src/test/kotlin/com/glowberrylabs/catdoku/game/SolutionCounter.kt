package com.glowberrylabs.catdoku.game

object SolutionCounter {

    fun count(puzzle: Puzzle, limit: Int = 2): Int {
        val size = puzzle.size

        fun place(row: Int, usedCols: Int, usedRegions: Int, previousCol: Int): Int {
            if (row == size) return 1
            var total = 0
            for (col in 0 until size) {
                val colBit = 1 shl col
                val regionBit = 1 shl puzzle.regions[row * size + col]
                val valid = usedCols and colBit == 0 &&
                    (row == 0 || kotlin.math.abs(col - previousCol) > 1) &&
                    usedRegions and regionBit == 0
                if (valid) {
                    total += place(row + 1, usedCols or colBit, usedRegions or regionBit, col)
                    if (total >= limit) return total
                }
            }
            return total
        }

        return place(0, 0, 0, -2)
    }
}
