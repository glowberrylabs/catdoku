package com.glowberrylabs.catdoku.game

fun testPuzzle(): Puzzle {
    val regions = intArrayOf(
        0, 0, 0, 1,
        2, 0, 1, 1,
        2, 2, 3, 1,
        2, 3, 3, 3
    )
    val solution = listOf(
        Position(0, 1),
        Position(1, 3),
        Position(2, 0),
        Position(3, 2)
    )
    return Puzzle(
        size = 4,
        regions = regions,
        solution = solution.toSet(),
        hintOrder = solution,
        difficulty = 1
    )
}
