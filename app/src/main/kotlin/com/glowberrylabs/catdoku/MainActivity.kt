package com.glowberrylabs.catdoku

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowberrylabs.catdoku.audio.GameHaptics
import com.glowberrylabs.catdoku.audio.GameSounds
import com.glowberrylabs.catdoku.game.AppScreen
import com.glowberrylabs.catdoku.game.Feedback
import com.glowberrylabs.catdoku.game.PuzzleStatus
import com.glowberrylabs.catdoku.game.PuzzleUiState
import com.glowberrylabs.catdoku.game.PuzzleViewModel
import com.glowberrylabs.catdoku.game.PuzzleViewModelFactory
import com.glowberrylabs.catdoku.ui.AppOverlays
import com.glowberrylabs.catdoku.ui.CatdokuTheme
import com.glowberrylabs.catdoku.ui.GameActions
import com.glowberrylabs.catdoku.ui.GameScreen
import com.glowberrylabs.catdoku.ui.MenuActions
import com.glowberrylabs.catdoku.ui.MenuScreen
import com.glowberrylabs.catdoku.ui.ResultActions
import com.glowberrylabs.catdoku.ui.RulesActions
import com.glowberrylabs.catdoku.ui.SettingsActions

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            CatdokuTheme {
                CatdokuApp()
            }
        }
    }
}

@Composable
private fun CatdokuApp() {
    val context = LocalContext.current
    val factory = remember { PuzzleViewModelFactory(context.applicationContext) }
    val viewModel: PuzzleViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        if (!viewModel.onBack()) {
            (context as? ComponentActivity)?.finish()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshDay()
    }
    FeedbackEffects(viewModel = viewModel, soundEnabled = state.settings.soundEnabled)
    AppContent(state = state, viewModel = viewModel)
}

@Composable
private fun FeedbackEffects(viewModel: PuzzleViewModel, soundEnabled: Boolean) {
    val context = LocalContext.current.applicationContext
    val view = LocalView.current
    val sounds = remember { GameSounds(context) }
    val haptics = remember { GameHaptics() }

    DisposableEffect(view) {
        haptics.attach(view)
        onDispose { haptics.detach(view) }
    }
    DisposableEffect(Unit) {
        onDispose { sounds.release() }
    }
    LaunchedEffect(soundEnabled) {
        sounds.muted = !soundEnabled
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { feedback ->
            val current = viewModel.uiState.value.settings
            when (feedback) {
                Feedback.TAP -> sounds.play(GameSounds.Effect.TAP)
                Feedback.CAT -> {
                    sounds.play(GameSounds.Effect.CAT)
                    if (current.hapticsEnabled) haptics.tick()
                }
                Feedback.WRONG -> {
                    sounds.play(GameSounds.Effect.WRONG)
                    if (current.hapticsEnabled) haptics.thump()
                }
                Feedback.WIN -> {
                    sounds.play(GameSounds.Effect.WIN)
                    if (current.hapticsEnabled) haptics.thump()
                }
            }
        }
    }
}

private data class AppActionSet(
    val game: GameActions,
    val menu: MenuActions,
    val settings: SettingsActions,
    val rules: RulesActions,
    val result: ResultActions
)

@Composable
private fun rememberAppActions(viewModel: PuzzleViewModel): AppActionSet = AppActionSet(
    game = remember(viewModel) {
        GameActions(
            onNote = viewModel::toggleNote,
            onCat = viewModel::toggleCat,
            onUndo = viewModel::undo,
            onHint = viewModel::hint,
            onPause = viewModel::pause,
            onResume = viewModel::resume,
            onRestart = viewModel::restart,
            onQuitToMenu = viewModel::quitToMenu,
            onOpenRules = viewModel::openRules,
            onOpenSettings = viewModel::openSettings
        )
    },
    menu = remember(viewModel) {
        MenuActions(
            onContinue = viewModel::continueLevel,
            onDaily = viewModel::openDaily,
            onOpenRules = viewModel::openRules,
            onOpenSettings = viewModel::openSettings
        )
    },
    settings = remember(viewModel) {
        SettingsActions(
            onToggleSound = viewModel::toggleSound,
            onToggleHaptics = viewModel::toggleHaptics,
            onToggleAutoMark = viewModel::toggleAutoMark,
            onToggleRegionLetters = viewModel::toggleRegionLetters,
            onClose = viewModel::closeSettings
        )
    },
    rules = remember(viewModel) {
        RulesActions(onClose = viewModel::closeRules)
    },
    result = remember(viewModel) {
        ResultActions(
            onNext = viewModel::nextLevel,
            onRetry = viewModel::restart,
            onQuitToMenu = viewModel::quitToMenu
        )
    }
)

@Composable
private fun AppContent(state: PuzzleUiState, viewModel: PuzzleViewModel) {
    val actions = rememberAppActions(viewModel)
    val status = state.puzzleState?.status
    val resultVisible = state.screen == AppScreen.GAME &&
        (status == PuzzleStatus.SOLVED || status == PuzzleStatus.FAILED)
    val overlayVisible = state.showRules || state.showSettings || state.showPause || resultVisible

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (overlayVisible) {
                        Modifier.semantics { hideFromAccessibility() }
                    } else {
                        Modifier
                    }
                )
        ) {
            when (state.screen) {
                AppScreen.MENU -> MenuScreen(uiState = state, actions = actions.menu)
                AppScreen.GAME -> GameScreen(uiState = state, actions = actions.game)
            }
        }
        AppOverlays(
            uiState = state,
            gameActions = actions.game,
            settingsActions = actions.settings,
            rulesActions = actions.rules,
            resultActions = actions.result
        )
    }
}
