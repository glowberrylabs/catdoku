package com.glowberrylabs.catdoku.game

object PuzzlePackParser {

    fun parse(raw: String): PuzzlePack {
        val levels = mutableListOf<Puzzle>()
        val dailies = mutableListOf<Puzzle>()

        raw.lineSequence().forEachIndexed { lineIndex, line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                return@forEachIndexed
            }
            val lineNumber = lineIndex + 1
            val parts = trimmed.split('|')
            if (parts.size != FIELD_COUNT) {
                fail(lineNumber, "expected $FIELD_COUNT fields, found ${parts.size}")
            }
            val kind = parts[0]
            val size = parts[1].toIntOrNull() ?: fail(lineNumber, "invalid size")
            if (size !in Puzzle.MIN_SIZE..Puzzle.MAX_SIZE) {
                fail(lineNumber, "unsupported size $size")
            }
            val regions = parseRegions(parts[2], size, lineNumber)
            val hintOrder = parseHintOrder(parts[3], size, lineNumber)
            val difficulty = parts[4].toIntOrNull() ?: fail(lineNumber, "invalid difficulty")
            if (difficulty < 0) {
                fail(lineNumber, "difficulty cannot be negative")
            }
            validateRegions(regions, size, lineNumber)
            val puzzle = Puzzle(
                size = size,
                regions = regions,
                solution = hintOrder.toSet(),
                hintOrder = hintOrder,
                difficulty = difficulty
            )
            validateSolution(puzzle, lineNumber)
            when (kind) {
                "L" -> levels += puzzle
                "D" -> dailies += puzzle
                else -> fail(lineNumber, "unknown record kind '$kind'")
            }
        }

        if (levels.isEmpty()) fail(0, "level pack is empty")
        if (dailies.isEmpty()) fail(0, "daily pack is empty")
        return PuzzlePack(levels = levels, dailies = dailies)
    }

    private fun parseRegions(value: String, size: Int, lineNumber: Int): IntArray {
        val total = size * size
        if (value.length != total) {
            fail(lineNumber, "expected $total region cells, found ${value.length}")
        }
        val regions = IntArray(total)
        for (index in 0 until total) {
            val digit = value[index] - '0'
            if (digit !in 0 until size) {
                fail(lineNumber, "invalid region '${value[index]}' at cell $index")
            }
            regions[index] = digit
        }
        return regions
    }

    private fun parseHintOrder(value: String, size: Int, lineNumber: Int): List<Position> {
        val tokens = value.split(',')
        if (tokens.size != size) {
            fail(lineNumber, "expected $size hint cells, found ${tokens.size}")
        }
        val total = size * size
        val seen = mutableSetOf<Int>()
        return tokens.map { token ->
            val index = token.toIntOrNull() ?: fail(lineNumber, "invalid hint cell '$token'")
            if (index !in 0 until total) {
                fail(lineNumber, "hint cell $index is out of bounds")
            }
            if (!seen.add(index)) {
                fail(lineNumber, "duplicate hint cell $index")
            }
            Position.fromIndex(index, size)
        }
    }

    private fun validateRegions(regions: IntArray, size: Int, lineNumber: Int) {
        val total = size * size
        val cellsByRegion = List(size) { mutableListOf<Int>() }
        regions.forEachIndexed { index, region ->
            cellsByRegion[region].add(index)
        }
        for (region in 0 until size) {
            val cells = cellsByRegion[region]
            if (cells.isEmpty()) {
                fail(lineNumber, "region $region is empty")
            }
            val seen = BooleanArray(total)
            val stack = IntArray(total)
            var top = 0
            stack[top++] = cells[0]
            seen[cells[0]] = true
            var visited = 0
            while (top > 0) {
                val cell = stack[--top]
                visited += 1
                val row = cell / size
                val col = cell % size
                if (row > 0) {
                    top = pushRegionNeighbor(cell - size, region, regions, seen, stack, top)
                }
                if (row < size - 1) {
                    top = pushRegionNeighbor(cell + size, region, regions, seen, stack, top)
                }
                if (col > 0) {
                    top = pushRegionNeighbor(cell - 1, region, regions, seen, stack, top)
                }
                if (col < size - 1) {
                    top = pushRegionNeighbor(cell + 1, region, regions, seen, stack, top)
                }
            }
            if (visited != cells.size) {
                fail(lineNumber, "region $region is not connected")
            }
        }
    }

    private fun pushRegionNeighbor(
        neighbor: Int,
        region: Int,
        regions: IntArray,
        seen: BooleanArray,
        stack: IntArray,
        top: Int
    ): Int {
        if (regions[neighbor] == region && !seen[neighbor]) {
            seen[neighbor] = true
            stack[top] = neighbor
            return top + 1
        }
        return top
    }

    private fun validateSolution(puzzle: Puzzle, lineNumber: Int) {
        val rows = mutableSetOf<Int>()
        val cols = mutableSetOf<Int>()
        val regions = mutableSetOf<Int>()
        for (position in puzzle.hintOrder) {
            if (!rows.add(position.row)) {
                fail(lineNumber, "duplicate row ${position.row}")
            }
            if (!cols.add(position.col)) {
                fail(lineNumber, "duplicate column ${position.col}")
            }
            if (!regions.add(puzzle.regionAt(position.row, position.col))) {
                fail(lineNumber, "duplicate region at row ${position.row}, column ${position.col}")
            }
        }
        if (rows.size != puzzle.size || cols.size != puzzle.size || regions.size != puzzle.size) {
            fail(lineNumber, "solution does not cover every row, column, and region")
        }
        val positions = puzzle.hintOrder
        for (index in positions.indices) {
            val position = positions[index]
            for (otherIndex in index + 1 until positions.size) {
                val other = positions[otherIndex]
                if (
                    kotlin.math.abs(position.row - other.row) <= 1 &&
                    kotlin.math.abs(position.col - other.col) <= 1
                ) {
                    fail(lineNumber, "cats touch at row ${position.row}, column ${position.col}")
                }
            }
        }
    }

    private fun fail(lineNumber: Int, message: String): Nothing {
        val prefix = if (lineNumber > 0) "Line $lineNumber: " else ""
        throw IllegalArgumentException(prefix + message)
    }

    private const val FIELD_COUNT = 5
}
