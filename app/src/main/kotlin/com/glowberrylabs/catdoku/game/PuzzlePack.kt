package com.glowberrylabs.catdoku.game

data class PuzzlePack(val levels: List<Puzzle>, val dailies: List<Puzzle>) {

    init {
        require(levels.isNotEmpty()) { "Level pack cannot be empty" }
        require(dailies.isNotEmpty()) { "Daily pack cannot be empty" }
    }
}
