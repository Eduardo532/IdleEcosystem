package com.EGomez.idleecosystem.data.preferences

import android.content.Context
import com.EGomez.idleecosystem.core.math.NumberNotation
import com.EGomez.idleecosystem.domain.logic.RitualSnapshot
import com.EGomez.idleecosystem.domain.model.RitualState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferencias pequeñas que no necesitan una tabla de Room:
 * audio, notación numérica y el estado de los rituales.
 *
 * Antes la música y los efectos se volvían a activar en cada inicio,
 * y los rituales se perdían al cerrar la app.
 */
@Singleton
class GamePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC, true)
        set(value) { prefs.edit().putBoolean(KEY_MUSIC, value).apply() }

    var isSfxEnabled: Boolean
        get() = prefs.getBoolean(KEY_SFX, true)
        set(value) { prefs.edit().putBoolean(KEY_SFX, value).apply() }

    var notation: NumberNotation
        get() = try {
            NumberNotation.valueOf(prefs.getString(KEY_NOTATION, null) ?: NumberNotation.SCIENTIFIC.name)
        } catch (e: IllegalArgumentException) {
            NumberNotation.SCIENTIFIC
        }
        set(value) { prefs.edit().putString(KEY_NOTATION, value.name).apply() }

    fun saveRituals(snapshots: List<RitualSnapshot>, savedAtMillis: Long) {
        prefs.edit()
            .putString(KEY_RITUALS, encodeRituals(snapshots))
            .putLong(KEY_RITUALS_SAVED_AT, savedAtMillis)
            .apply()
    }

    /** Devuelve los rituales guardados y el momento en que se guardaron (0 si no hay). */
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
        private const val KEY_NOTATION = "number_notation"
        private const val KEY_RITUALS = "rituals_state"
        private const val KEY_RITUALS_SAVED_AT = "rituals_saved_at"

        // Formato: "id:ESTADO:segundos;id:ESTADO:segundos"
        internal fun encodeRituals(snapshots: List<RitualSnapshot>): String =
            snapshots.joinToString(";") { "${it.id}:${it.state.name}:${it.timeRemaining}" }

        internal fun decodeRituals(encoded: String): List<RitualSnapshot> =
            encoded.split(";").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size != 3) return@mapNotNull null
                val state = try { RitualState.valueOf(parts[1]) } catch (e: IllegalArgumentException) { return@mapNotNull null }
                val remaining = parts[2].toLongOrNull() ?: return@mapNotNull null
                RitualSnapshot(parts[0], state, remaining)
            }
    }
}
