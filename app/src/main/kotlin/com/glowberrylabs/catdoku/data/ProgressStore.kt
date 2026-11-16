package com.glowberrylabs.catdoku.data

import android.content.Context
import com.glowberrylabs.catdoku.game.AppSettings
import com.glowberrylabs.catdoku.game.DailyProgress

class ProgressStore(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun settings(): AppSettings = AppSettings(
        soundEnabled = preferences.getBoolean(KEY_SOUND_ENABLED, true),
        hapticsEnabled = preferences.getBoolean(KEY_HAPTICS_ENABLED, true),
        autoMarkEnabled = preferences.getBoolean(KEY_AUTO_MARK_ENABLED, false),
        regionLettersEnabled = preferences.getBoolean(KEY_REGION_LETTERS_ENABLED, false)
    )

    fun saveSettings(settings: AppSettings) {
        preferences.edit()
            .putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            .putBoolean(KEY_HAPTICS_ENABLED, settings.hapticsEnabled)
            .putBoolean(KEY_AUTO_MARK_ENABLED, settings.autoMarkEnabled)
            .putBoolean(KEY_REGION_LETTERS_ENABLED, settings.regionLettersEnabled)
            .apply()
    }

    fun highestSolvedLevel(): Int = preferences.getInt(KEY_HIGHEST_SOLVED_LEVEL, 0)

    fun saveHighestSolvedLevel(level: Int) {
        if (level <= highestSolvedLevel()) return
        preferences.edit().putInt(KEY_HIGHEST_SOLVED_LEVEL, level).apply()
    }

    fun totalSolved(): Int = preferences.getInt(KEY_TOTAL_SOLVED, 0)

    fun addSolved() {
        preferences.edit().putInt(KEY_TOTAL_SOLVED, totalSolved() + 1).apply()
    }

    fun totalHints(): Int = preferences.getInt(KEY_TOTAL_HINTS, 0)

    fun addHints(count: Int) {
        if (count <= 0) return
        preferences.edit().putInt(KEY_TOTAL_HINTS, totalHints() + count).apply()
    }

    fun dailyProgress(): DailyProgress {
        val lastSolved = preferences.getLong(KEY_DAILY_LAST_SOLVED, NO_DAY)
        return DailyProgress(
            lastSolvedEpochDay = if (lastSolved == NO_DAY) null else lastSolved,
            currentStreak = preferences.getInt(KEY_DAILY_STREAK, 0),
            totalSolves = preferences.getInt(KEY_DAILY_TOTAL_SOLVES, 0)
        )
    }

    fun saveDailyProgress(progress: DailyProgress) {
        val editor = preferences.edit()
            .putInt(KEY_DAILY_STREAK, progress.currentStreak)
            .putInt(KEY_DAILY_TOTAL_SOLVES, progress.totalSolves)
        if (progress.lastSolvedEpochDay == null) {
            editor.remove(KEY_DAILY_LAST_SOLVED)
        } else {
            editor.putLong(KEY_DAILY_LAST_SOLVED, progress.lastSolvedEpochDay)
        }
        editor.apply()
    }

    fun levelBoard(): String? = preferences.getString(KEY_LEVEL_BOARD, null)

    fun saveLevelBoard(value: String) {
        preferences.edit().putString(KEY_LEVEL_BOARD, value).apply()
    }

    fun clearLevelBoard() {
        preferences.edit().remove(KEY_LEVEL_BOARD).apply()
    }

    fun dailyBoard(): String? = preferences.getString(KEY_DAILY_BOARD, null)

    fun saveDailyBoard(value: String) {
        preferences.edit().putString(KEY_DAILY_BOARD, value).apply()
    }

    fun clearDailyBoard() {
        preferences.edit().remove(KEY_DAILY_BOARD).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "catdoku"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        const val KEY_AUTO_MARK_ENABLED = "auto_mark_enabled"
        const val KEY_REGION_LETTERS_ENABLED = "region_letters_enabled"
        const val KEY_HIGHEST_SOLVED_LEVEL = "highest_solved_level"
        const val KEY_TOTAL_SOLVED = "total_solved"
        const val KEY_TOTAL_HINTS = "total_hints"
        const val KEY_DAILY_LAST_SOLVED = "daily_last_solved"
        const val KEY_DAILY_STREAK = "daily_streak"
        const val KEY_DAILY_TOTAL_SOLVES = "daily_total_solves"
        const val KEY_LEVEL_BOARD = "level_board"
        const val KEY_DAILY_BOARD = "daily_board"
        const val NO_DAY = -1L
    }
}
