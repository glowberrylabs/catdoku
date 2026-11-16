package com.glowberrylabs.catdoku.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.glowberrylabs.catdoku.R

class GameSounds(context: Context) {

    enum class Effect(@param:RawRes val resourceId: Int) {
        TAP(R.raw.tap),
        CAT(R.raw.cat),
        WRONG(R.raw.wrong),
        WIN(R.raw.win)
    }

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<Effect, Int>()
    private val loadedIds = mutableSetOf<Int>()

    var muted: Boolean = false

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                synchronized(loadedIds) { loadedIds += sampleId }
            }
        }
        Effect.entries.forEach { effect ->
            soundIds[effect] = soundPool.load(context.applicationContext, effect.resourceId, 1)
        }
    }

    fun play(effect: Effect) {
        if (muted) return
        val id = soundIds[effect] ?: return
        val loaded = synchronized(loadedIds) { id in loadedIds }
        if (!loaded) return
        soundPool.play(id, VOLUME, VOLUME, PRIORITY, 0, 1f)
    }

    fun stopAll() {
        soundIds.values.forEach { id ->
            soundPool.stop(id)
        }
    }

    fun release() {
        soundPool.release()
    }

    private companion object {
        const val MAX_STREAMS = 4
        const val VOLUME = 0.8f
        const val PRIORITY = 1
    }
}
