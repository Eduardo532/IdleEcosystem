package com.EGomez.idleecosystem.domain.logic

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.data.preferences.GamePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SoundType {
    CLICK,
    BUY,
    ACHIEVEMENT,
    EVENT_APPEAR
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

    // Configuración de usuario (persistida: antes se reactivaba en cada inicio)
    var isMusicEnabled: Boolean = preferences.isMusicEnabled
        private set
    var isSfxEnabled: Boolean = preferences.isSfxEnabled
        private set

    // Evita que suenen efectos (eventos, logros) con la app en segundo plano
    @Volatile
    private var isAppInForeground: Boolean = true

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
    }

    // --- MÚSICA DE FONDO (BGM) ---
    fun playMusic() {
        if (!isMusicEnabled) return

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.bgm_drone).apply {
                isLooping = true // Bucle infinito
                setVolume(0.5f, 0.5f) // Volumen al 50% para no molestar
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

    fun setAppInForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
    }

    // --- EFECTOS DE SONIDO (SFX) ---
    // MODIFICADO: Ahora acepta un 'maxDurationMs' opcional. Si es null, suena completo.
    fun playSound(type: SoundType, maxDurationMs: Long? = null) {
        if (!isSfxEnabled || !isAppInForeground) return

        val soundId = soundMap[type] ?: return
        // Variación de Pitch (0.9f a 1.1f) para que el click no suene robótico
        val pitch = if (type == SoundType.CLICK) (0.9f + Math.random() * 0.2f).toFloat() else 1f

        // Reproducimos y guardamos el ID del stream
        val streamId = soundPool.play(soundId, 1f, 1f, 1, 0, pitch)

        // Si definiste una duración, programamos el corte
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
     * Libera solo el reproductor de música. El SoundPool NO se libera: este objeto es un
     * singleton que vive tanto como el proceso, y antes liberarlo en onDestroy dejaba la app
     * sin efectos de sonido tras rotar la pantalla o volver a abrirla.
     */
    fun stopMusic() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}