package com.EGomez.idleecosystem.data.preferences

import android.content.Context
import com.EGomez.idleecosystem.domain.logic.RitualSnapshot
import com.EGomez.idleecosystem.domain.model.RitualState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferencias del dispositivo que no forman parte de la partida exportable:
 * audio, efectos visuales y el estado de los rituales.
 *
 * Se guardan en SharedPreferences para no tocar el esquema de Room (game_save)
 * sin tests de migración. Antes todo esto vivía solo en memoria y se reiniciaba
 * al cerrar la app.
 */
@Singleton
class GamePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC, value).apply()

    var isSfxEnabled: Boolean
        get() = prefs.getBoolean(KEY_SFX, true)
        set(value) = prefs.edit().putBoolean(KEY_SFX, value).apply()

    var areParticlesEnabled: Boolean
        get() = prefs.getBoolean(KEY_PARTICLES, true)
        set(value) = prefs.edit().putBoolean(KEY_PARTICLES, value).apply()

    var isFloatingTextEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLOATING_TEXT, true)
        set(value) = prefs.edit().putBoolean(KEY_FLOATING_TEXT, value).apply()

    /** Continuación pendiente de una cadena de eventos (null si no hay). */
    var pendingFollowUpEventId: String?
        get() = prefs.getString(KEY_PENDING_EVENT, null)
        set(value) = prefs.edit().apply {
            if (value == null) remove(KEY_PENDING_EVENT) else putString(KEY_PENDING_EVENT, value)
        }.apply()

    fun saveRituals(snapshots: List<RitualSnapshot>, savedAtMillis: Long) {
        prefs.edit()
            .putString(KEY_RITUALS, encodeRituals(snapshots))
            .putLong(KEY_RITUALS_SAVED_AT, savedAtMillis)
            .apply()
    }

    /** Rituales guardados y el momento en que se guardaron (0 si no hay datos). */
    fun loadRituals(): Pair<List<RitualSnapshot>, Long> {
        val encoded = prefs.getString(KEY_RITUALS, null) ?: return emptyList<RitualSnapshot>() to 0L
        return decodeRituals(encoded) to prefs.getLong(KEY_RITUALS_SAVED_AT, 0L)
    }

    fun clearRituals() {
        prefs.edit().remove(KEY_RITUALS).remove(KEY_RITUALS_SAVED_AT).apply()
    }

    companion object {
        private const val PREFS_NAME = "idle_ecosystem_prefs"
        private const val KEY_MUSIC = "music_enabled"
        private const val KEY_SFX = "sfx_enabled"
        private const val KEY_PARTICLES = "particles_enabled"
        private const val KEY_FLOATING_TEXT = "floating_text_enabled"
        private const val KEY_RITUALS = "rituals_state"
        private const val KEY_RITUALS_SAVED_AT = "rituals_saved_at"
        private const val KEY_PENDING_EVENT = "pending_follow_up_event"

        // Formato: "id:ESTADO:segundos;id:ESTADO:segundos"
        fun encodeRituals(snapshots: List<RitualSnapshot>): String =
            snapshots.joinToString(";") { "${it.id}:${it.state.name}:${it.timeRemaining}" }

        fun decodeRituals(encoded: String): List<RitualSnapshot> =
            encoded.split(";").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size != 3) return@mapNotNull null
                val state = RitualState.entries.find { it.name == parts[1] } ?: return@mapNotNull null
                val remaining = parts[2].toLongOrNull() ?: return@mapNotNull null
                RitualSnapshot(parts[0], state, remaining)
            }
    }
}
