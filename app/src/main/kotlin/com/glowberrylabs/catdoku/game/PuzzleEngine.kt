package com.glowberrylabs.catdoku.game

class PuzzleEngine {

    fun newState(puzzle: Puzzle, mode: PuzzleMode, levelNumber: Int?): PuzzleState = PuzzleState(
        puzzle = puzzle,
        mode = mode,
        levelNumber = levelNumber,
        marks = List(puzzle.size * puzzle.size) { CellMark.EMPTY },
        hearts = PuzzleState.STARTING_HEARTS,
        hintsUsed = 0,
        status = PuzzleStatus.PLAYING,
        undoStack = emptyList()
    )

    fun restoreState(
        puzzle: Puzzle,
        mode: PuzzleMode,
        levelNumber: Int?,
        marks: List<CellMark>,
        hearts: Int,
        hintsUsed: Int
    ): PuzzleState {
        require(marks.size == puzzle.size * puzzle.size) { "Saved marks do not match puzzle size" }
        val catCount = marks.count { it == CellMark.CAT }
        val status = when {
            catCount == puzzle.size -> PuzzleStatus.SOLVED
            hearts <= 0 -> PuzzleStatus.FAILED
            else -> PuzzleStatus.PLAYING
        }
        return PuzzleState(
            puzzle = puzzle,
            mode = mode,
            levelNumber = levelNumber,
            marks = marks,
            hearts = hearts.coerceIn(0, PuzzleState.STARTING_HEARTS),
            hintsUsed = hintsUsed.coerceAtLeast(0),
            status = status,
            undoStack = emptyList()
        )
    }

    fun toggleNote(state: PuzzleState, position: Position): EngineResult {
        if (state.status != PuzzleStatus.PLAYING || !isInside(state, position)) {
            return EngineResult(state)
        }
        val index = state.puzzle.indexOf(position)
        val current = state.marks[index]
        val (nextMark, event) = when (current) {
            CellMark.EMPTY -> CellMark.NOTE to PuzzleEvent.NOTE_ADDED
            CellMark.NOTE -> CellMark.EMPTY to PuzzleEvent.NOTE_REMOVED
            else -> return EngineResult(state)
        }
        val marks = state.marks.toMutableList().apply { this[index] = nextMark }
        val next = state.copy(
            marks = marks,
            undoStack = state.undoStack + PuzzleMove(index, current),
            hintCell = null
        )
        return EngineResult(next, listOf(event))
    }

    fun toggleCat(state: PuzzleState, position: Position): EngineResult {
        if (state.status != PuzzleStatus.PLAYING || !isInside(state, position)) {
            return EngineResult(state)
        }
        val index = state.puzzle.indexOf(position)
        return when (state.marks[index]) {
            CellMark.CAT -> removeCat(state, index)
            CellMark.MISTAKE -> EngineResult(state)
            CellMark.EMPTY, CellMark.NOTE -> placeCat(state, position, index)
        }
    }

    fun undo(state: PuzzleState): EngineResult {
        if (state.status != PuzzleStatus.PLAYING || state.undoStack.isEmpty()) {
            return EngineResult(state)
        }
        val move = state.undoStack.last()
        val marks = state.marks.toMutableList().apply { this[move.index] = move.previous }
        val solved = marks.count { it == CellMark.CAT } == state.size
        val next = state.copy(
            marks = marks,
            status = if (solved) PuzzleStatus.SOLVED else PuzzleStatus.PLAYING,
            undoStack = state.undoStack.dropLast(1),
            hintCell = null
        )
        return EngineResult(next, listOf(PuzzleEvent.UNDONE))
    }

    fun hint(state: PuzzleState): EngineResult {
        if (state.status != PuzzleStatus.PLAYING) {
            return EngineResult(state)
        }
        val target = state.puzzle.hintOrder.firstOrNull {
            state.marks[state.puzzle.indexOf(it)] != CellMark.CAT
        } ?: return EngineResult(state)
        val next = state.copy(
            hintsUsed = state.hintsUsed + 1,
            hintCell = target
        )
        return EngineResult(next, listOf(PuzzleEvent.HINTED))
    }

    fun restart(state: PuzzleState): EngineResult {
        val next = newState(state.puzzle, state.mode, state.levelNumber)
        return EngineResult(next, listOf(PuzzleEvent.RESTARTED))
    }

    private fun placeCat(state: PuzzleState, position: Position, index: Int): EngineResult {
        val previous = state.marks[index]
        if (!state.puzzle.contains(position)) {
            val hearts = (state.hearts - 1).coerceAtLeast(0)
            val marks = state.marks.toMutableList().apply { this[index] = CellMark.MISTAKE }
            val failed = hearts == 0
            val next = state.copy(
                marks = marks,
                hearts = hearts,
                status = if (failed) PuzzleStatus.FAILED else PuzzleStatus.PLAYING,
                hintCell = null
            )
            val events = if (failed) {
                listOf(PuzzleEvent.WRONG, PuzzleEvent.FAILED)
            } else {
                listOf(PuzzleEvent.WRONG)
            }
            return EngineResult(next, events)
        }

        val marks = state.marks.toMutableList().apply { this[index] = CellMark.CAT }
        val solved = marks.count { it == CellMark.CAT } == state.size
        val next = state.copy(
            marks = marks,
            status = if (solved) PuzzleStatus.SOLVED else PuzzleStatus.PLAYING,
            undoStack = state.undoStack + PuzzleMove(index, previous),
            hintCell = null
        )
        val events = if (solved) {
            listOf(PuzzleEvent.CAT_PLACED, PuzzleEvent.SOLVED)
        } else {
            listOf(PuzzleEvent.CAT_PLACED)
        }
        return EngineResult(next, events)
    }

    private fun removeCat(state: PuzzleState, index: Int): EngineResult {
        val marks = state.marks.toMutableList().apply { this[index] = CellMark.EMPTY }
        val next = state.copy(
            marks = marks,
            undoStack = state.undoStack + PuzzleMove(index, CellMark.CAT),
            hintCell = null
        )
        return EngineResult(next, listOf(PuzzleEvent.CAT_REMOVED))
    }

    private fun isInside(state: PuzzleState, position: Position): Boolean =
        position.row in 0 until state.size && position.col in 0 until state.size
}
