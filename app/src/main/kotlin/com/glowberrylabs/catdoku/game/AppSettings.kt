package com.glowberrylabs.catdoku.game

data class AppSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val autoMarkEnabled: Boolean = false,
    val regionLettersEnabled: Boolean = false
)
