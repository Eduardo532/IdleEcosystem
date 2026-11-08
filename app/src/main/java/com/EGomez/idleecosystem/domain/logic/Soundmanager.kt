package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.preferences.GamePreferences
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.EGomez.idleecosystem.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SoundType {
    CLICK,
    BUY,
    ACHIEVEMENT,
    EVENT_APPEAR,
    ECHO_FOUND,
    RUNE_SPAWN,
    RUNE_SUCCESS,
    TUTORIAL_POPUP
}

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: GamePreferences
) {

    private var mediaPlayer: MediaPlayer? = null
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundType, Int>()

    // Handler para programar la detención de sonidos
    private val handler = Handler(Looper.getMainLooper())

    // Configuración de usuario (persistida entre sesiones). @Volatile: se cambian desde
    // la UI y se leen desde el hilo del motor, que es quien reproduce los efectos.
    @Volatile
    var isMusicEnabled: Boolean = preferences.isMusicEnabled
        private set
    @Volatile
    var isSfxEnabled: Boolean = preferences.isSfxEnabled
        private set

    init {
        // 1. Configurar SoundPool para efectos cortos (SFX)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Máximo 5 sonidos a la vez
            .setAudioAttributes(audioAttributes)
            .build()

        // 2. Cargar los efectos en memoria
        soundMap[SoundType.CLICK] = soundPool.load(context, R.raw.sfx_click, 1)
        soundMap[SoundType.BUY] = soundPool.load(context, R.raw.sfx_buy, 1)
        soundMap[SoundType.ACHIEVEMENT] = soundPool.load(context, R.raw.sfx_achievement, 1)
        soundMap[SoundType.EVENT_APPEAR] = soundPool.load(context, R.raw.sfx_event, 1)
        soundMap[SoundType.ECHO_FOUND] = soundPool.load(context, R.raw.sfx_echos, 1)
        soundMap[SoundType.RUNE_SPAWN] = soundPool.load(context,R.raw.sfx_rune_spawn,1)
        soundMap[SoundType.RUNE_SUCCESS] = soundPool.load(context,R.raw.sfx_rune_success,1)
        soundMap[SoundType.TUTORIAL_POPUP] = soundPool.load(context, R.raw.tutorial_popup,1)

    }

    // --- MÚSICA DE FONDO (BGM) ---
    fun playMusic() {
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

    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun resumeMusic() {
        if (isMusicEnabled && mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
        }
    }

    fun toggleMusic(enabled: Boolean) {
        isMusicEnabled = enabled
        preferences.isMusicEnabled = enabled
        if (enabled) playMusic() else pauseMusic()
    }

    // --- EFECTOS DE SONIDO (SFX) ---
    fun playSound(type: SoundType, maxDurationMs: Long? = null) {
        if (!isSfxEnabled) return

        val soundId = soundMap[type] ?: return
        val pitch = if (type == SoundType.CLICK) (0.9f + Math.random() * 0.2f).toFloat() else 1f

        val streamId = soundPool.play(soundId, 1f, 1f, 1, 0, pitch)

        if (maxDurationMs != null) {
            handler.postDelayed({
                soundPool.stop(streamId)
            }, maxDurationMs)
        }
    }

    fun toggleSfx(enabled: Boolean) {
        isSfxEnabled = enabled
        preferences.isSfxEnabled = enabled
    }

    /**
     * Libera solo el reproductor de música; playMusic() lo vuelve a crear.
     * El SoundPool NO se libera: este objeto es un singleton que vive tanto como el
     * proceso y se reutiliza cuando la Activity se recrea.
     */
    fun stopMusic() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}