package com.glowberrylabs.catdoku.game

data class Position(val row: Int, val col: Int) {

    companion object {
        fun fromIndex(index: Int, size: Int): Position = Position(index / size, index % size)
    }
}
