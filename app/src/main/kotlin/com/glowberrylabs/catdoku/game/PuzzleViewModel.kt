package com.glowberrylabs.catdoku.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.glowberrylabs.catdoku.data.BoardCodec
import com.glowberrylabs.catdoku.data.ProgressStore
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class PuzzleViewModel(
    private val store: ProgressStore,
    private val pack: PuzzlePack,
    private val engine: PuzzleEngine = PuzzleEngine(),
    private val todayProvider: () -> Long = {
        LocalDate.now(ZoneOffset.UTC).toEpochDay()
    }
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<Feedback>(extraBufferCapacity = 4)
    val events: SharedFlow<Feedback> = _events.asSharedFlow()

    fun continueLevel() {
        val state = _uiState.value
        val nextLevel = state.nextLevelNumber ?: return
        val current = state.puzzleState
        val puzzleState = if (
            current != null &&
            current.mode == PuzzleMode.LEVEL &&
            current.levelNumber == nextLevel
        ) {
            current
        } else {
            restoreLevelBoard(nextLevel)
                ?: LevelRamp.puzzleFor(pack, nextLevel)?.let {
                    engine.newState(it, PuzzleMode.LEVEL, nextLevel)
                }
                ?: return
        }
        _uiState.value = state.copy(
            screen = AppScreen.GAME,
            puzzleState = puzzleState,
            todayEpochDay = todayProvider(),
            showRules = false,
            showSettings = false,
            showPause = false
        )
        saveBoard(puzzleState)
    }

    fun openDaily() {
        saveCurrentBoard()
        val today = todayProvider()
        val puzzle = DailySelector.puzzleFor(today, pack.dailies) ?: return
        val saved = store.dailyBoard()?.let(BoardCodec::decodeDailyBoard)
        val restored = if (
            saved != null &&
            saved.epochDay == today &&
            saved.size == puzzle.size
        ) {
            engine.restoreState(
                puzzle = puzzle,
                mode = PuzzleMode.DAILY,
                levelNumber = null,
                marks = saved.marks,
                hearts = saved.hearts,
                hintsUsed = saved.hintsUsed
            )
        } else {
            engine.newState(puzzle, PuzzleMode.DAILY, null)
        }
        _uiState.value = _uiState.value.copy(
            screen = AppScreen.GAME,
            puzzleState = restored,
            todayEpochDay = today,
            daily = dailySummary(today),
            showRules = false,
            showSettings = false,
            showPause = false
        )
        saveBoard(restored)
    }

    fun toggleNote(position: Position) {
        val current = currentPuzzle() ?: return
        applyResult(engine.toggleNote(current, position))
    }

    fun toggleCat(position: Position) {
        val current = currentPuzzle() ?: return
        applyResult(engine.toggleCat(current, position))
    }

    fun undo() {
        val current = currentPuzzle() ?: return
        applyResult(engine.undo(current))
    }

    fun hint() {
        val current = currentPuzzle() ?: return
        applyResult(engine.hint(current))
    }

    fun restart() {
        val current = currentPuzzle() ?: return
        applyResult(engine.restart(current))
    }

    fun pause() {
        val state = _uiState.value
        if (state.screen != AppScreen.GAME) return
        _uiState.value = state.copy(showPause = true, showRules = false, showSettings = false)
    }

    fun resume() {
        _uiState.value = _uiState.value.copy(showPause = false)
    }

    fun openRules() {
        _uiState.value = _uiState.value.copy(
            showRules = true,
            showSettings = false,
            showPause = false
        )
    }

    fun closeRules() {
        _uiState.value = _uiState.value.copy(showRules = false)
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(
            showSettings = true,
            showRules = false,
            showPause = false
        )
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false)
    }

    fun toggleSound() {
        updateSettings { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleHaptics() {
        updateSettings { it.copy(hapticsEnabled = !it.hapticsEnabled) }
    }

    fun toggleAutoMark() {
        updateSettings { it.copy(autoMarkEnabled = !it.autoMarkEnabled) }
    }

    fun toggleRegionLetters() {
        updateSettings { it.copy(regionLettersEnabled = !it.regionLettersEnabled) }
    }

    fun nextLevel() {
        val state = _uiState.value
        val current = state.puzzleState
        if (current == null || current.status != PuzzleStatus.SOLVED) return
        if (current.mode == PuzzleMode.DAILY) {
            quitToMenu()
            return
        }
        val levelNumber = (current.levelNumber ?: 0) + 1
        val puzzle = LevelRamp.puzzleFor(pack, levelNumber)
        if (puzzle == null) {
            quitToMenu()
            return
        }
        val puzzleState = engine.newState(puzzle, PuzzleMode.LEVEL, levelNumber)
        _uiState.value = state.copy(
            screen = AppScreen.GAME,
            puzzleState = puzzleState,
            todayEpochDay = todayProvider(),
            showPause = false
        )
        saveBoard(puzzleState)
    }

    fun quitToMenu() {
        saveCurrentBoard()
        _uiState.value = _uiState.value.copy(
            screen = AppScreen.MENU,
            puzzleState = null,
            showPause = false,
            showRules = false,
            showSettings = false,
            daily = dailySummary(todayProvider())
        )
    }

    fun refreshDay() {
        val state = _uiState.value
        val today = todayProvider()
        val current = state.puzzleState
        if (current != null && current.mode == PuzzleMode.DAILY && state.todayEpochDay != today) {
            quitToMenu()
            return
        }
        _uiState.value = state.copy(
            todayEpochDay = today,
            daily = dailySummary(today)
        )
    }

    fun onBack(): Boolean {
        val state = _uiState.value
        when {
            state.showRules -> closeRules()
            state.showSettings -> closeSettings()
            state.showPause -> resume()
            state.screen != AppScreen.GAME -> return false
            state.puzzleState?.status != PuzzleStatus.PLAYING -> quitToMenu()
            else -> pause()
        }
        return true
    }

    private fun initialState(): PuzzleUiState {
        val today = todayProvider()
        val settings = store.settings()
        val progress = progressSummary()
        return PuzzleUiState(
            screen = AppScreen.MENU,
            puzzleState = restoreLevelBoard(progress.highestSolvedLevel + 1),
            settings = settings,
            progress = progress,
            daily = dailySummary(today),
            levelCount = pack.levels.size,
            todayEpochDay = today
        )
    }

    private fun currentPuzzle(): PuzzleState? {
        val state = _uiState.value
        if (state.screen != AppScreen.GAME) return null
        return state.puzzleState
    }

    private fun applyResult(result: EngineResult) {
        val state = _uiState.value
        val next = result.state
        _uiState.value = state.copy(puzzleState = next)
        emitFeedback(result.events)
        if (next.status == PuzzleStatus.SOLVED) {
            handleSolved(next)
        } else {
            saveBoard(next)
        }
    }

    private fun handleSolved(state: PuzzleState) {
        val current = _uiState.value
        store.addHints(state.hintsUsed)
        when (state.mode) {
            PuzzleMode.LEVEL -> {
                val level = state.levelNumber ?: return
                store.saveHighestSolvedLevel(level)
                store.addSolved()
                store.clearLevelBoard()
                _uiState.value = current.copy(
                    puzzleState = state,
                    progress = progressSummary()
                )
            }
            PuzzleMode.DAILY -> {
                val today = current.todayEpochDay
                val progress = DailySelector.recordSolve(store.dailyProgress(), today)
                store.saveDailyProgress(progress)
                saveBoard(state)
                _uiState.value = current.copy(
                    puzzleState = state,
                    daily = DailySummary(
                        currentStreak = progress.currentStreak,
                        totalSolves = progress.totalSolves,
                        solvedToday = progress.lastSolvedEpochDay == today
                    ),
                    progress = progressSummary()
                )
            }
        }
    }

    private fun emitFeedback(events: List<PuzzleEvent>) {
        val feedback = when {
            PuzzleEvent.SOLVED in events -> Feedback.WIN
            PuzzleEvent.WRONG in events -> Feedback.WRONG
            PuzzleEvent.CAT_PLACED in events -> Feedback.CAT
            PuzzleEvent.NOTE_ADDED in events -> Feedback.TAP
            PuzzleEvent.NOTE_REMOVED in events -> Feedback.TAP
            PuzzleEvent.UNDONE in events -> Feedback.TAP
            PuzzleEvent.HINTED in events -> Feedback.TAP
            PuzzleEvent.RESTARTED in events -> Feedback.TAP
            else -> null
        } ?: return
        viewModelScope.launch {
            _events.emit(feedback)
        }
    }

    private fun saveCurrentBoard() {
        _uiState.value.puzzleState?.let { saveBoard(it) }
    }

    private fun saveBoard(state: PuzzleState) {
        when (state.mode) {
            PuzzleMode.LEVEL -> {
                if (state.status == PuzzleStatus.SOLVED) {
                    store.clearLevelBoard()
                } else {
                    BoardCodec.encodeLevelBoard(state)?.let(store::saveLevelBoard)
                }
            }
            PuzzleMode.DAILY -> {
                val today = _uiState.value.todayEpochDay
                BoardCodec.encodeDailyBoard(state, today)?.let(store::saveDailyBoard)
            }
        }
    }

    private fun restoreLevelBoard(levelNumber: Int): PuzzleState? {
        val puzzle = LevelRamp.puzzleFor(pack, levelNumber) ?: return null
        val saved = store.levelBoard()?.let(BoardCodec::decodeLevelBoard) ?: return null
        if (saved.levelNumber != levelNumber || saved.size != puzzle.size) return null
        val restored = engine.restoreState(
            puzzle = puzzle,
            mode = PuzzleMode.LEVEL,
            levelNumber = levelNumber,
            marks = saved.marks,
            hearts = saved.hearts,
            hintsUsed = saved.hintsUsed
        )
        if (restored.status == PuzzleStatus.SOLVED) {
            store.clearLevelBoard()
            return null
        }
        return restored
    }

    private fun progressSummary(): ProgressSummary = ProgressSummary(
        highestSolvedLevel = store.highestSolvedLevel(),
        totalSolved = store.totalSolved(),
        totalHints = store.totalHints()
    )

    private fun dailySummary(today: Long): DailySummary {
        val progress = store.dailyProgress()
        return DailySummary(
            currentStreak = progress.currentStreak,
            totalSolves = progress.totalSolves,
            solvedToday = progress.lastSolvedEpochDay == today
        )
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val settings = transform(_uiState.value.settings)
        store.saveSettings(settings)
        _uiState.value = _uiState.value.copy(settings = settings)
    }
}

class PuzzleViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val store = ProgressStore(appContext)
        val pack = PuzzlePackParser.parse(PuzzleData.RAW)
        return PuzzleViewModel(store, pack) as T
    }
}
