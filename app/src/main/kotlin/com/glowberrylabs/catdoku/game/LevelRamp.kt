package com.glowberrylabs.catdoku.game

object LevelRamp {

    fun levelNumber(index: Int): Int = index + 1

    fun index(levelNumber: Int): Int = levelNumber - 1

    fun puzzleFor(pack: PuzzlePack, levelNumber: Int): Puzzle? = pack.levels.getOrNull(index(levelNumber))

    fun nextLevelNumber(pack: PuzzlePack, highestSolvedLevel: Int): Int? {
        val next = (highestSolvedLevel + 1).coerceAtLeast(1)
        return if (next > pack.levels.size) null else next
    }
}
