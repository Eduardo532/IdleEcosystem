package com.EGomez.idleecosystem.domain.logic

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.EGomez.idleecosystem.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SoundType {
    CLICK,
    BUY,
    ACHIEVEMENT,
    EVENT_APPEAR,
    ECHO_FOUND
}

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundType, Int>()
    private var mediaPlayer: MediaPlayer? = null

    var isMusicEnabled: Boolean = true
        private set
    var isSfxEnabled: Boolean = true
        private set

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Carga de sonidos
        soundMap[SoundType.CLICK] = soundPool.load(context, R.raw.sfx_click, 1)
        soundMap[SoundType.BUY] = soundPool.load(context, R.raw.sfx_buy, 1)
        soundMap[SoundType.ACHIEVEMENT] = soundPool.load(context, R.raw.sfx_achievement, 1)
        soundMap[SoundType.EVENT_APPEAR] = soundPool.load(context, R.raw.sfx_event, 1)
        soundMap[SoundType.ECHO_FOUND] = soundPool.load(context, R.raw.sfx_echos, 1)

        startBackgroundMusic()
    }

    fun playSound(type: SoundType, pitch: Int = 0) {
        if (!isSfxEnabled) return

        val soundId = soundMap[type] ?: return

        val playbackRate = if (type == SoundType.CLICK) {
            0.9f + (Math.random().toFloat() * 0.2f)
        } else {
            1.0f
        }

        soundPool.play(soundId, 1f, 1f, 0, 0, playbackRate)
    }

    fun toggleMusic(enabled: Boolean) {
        isMusicEnabled = enabled
        if (enabled) {
            startBackgroundMusic()
        } else {
            mediaPlayer?.pause()
        }
    }

    fun toggleSfx(enabled: Boolean) {
        isSfxEnabled = enabled
    }

    private fun startBackgroundMusic() {
        if (!isMusicEnabled) return

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.bgm_drone).apply {
                isLooping = true
                setVolume(0.5f, 0.5f)
            }
        }

        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }
}