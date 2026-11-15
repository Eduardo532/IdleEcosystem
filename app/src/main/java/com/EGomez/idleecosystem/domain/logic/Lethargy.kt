package com.EGomez.idleecosystem.domain.logic

import kotlin.math.floor
import kotlin.math.ln

/**
 * El Letargo (v0.20.2), inspirado en las vidas largas de los idle que premian dejar el
 * juego abierto. Desde la 5ª Trascendencia se puede caer en Letargo: mientras dura, la
 * producción baja a una décima parte, pero cada minuto suma Sopor. Al Ascender, el Sopor
 * de la vida multiplica la Cordura ganada y, cada hora, la Ascensión cuenta una vez más.
 * Salir del Letargo antes de Ascender pierde el Sopor de esa vida.
 */
object Lethargy {
    const val UNLOCK_TRANSCENDENCES = 5

    /** La producción mientras dura el Letargo. */
    const val PRODUCTION_FACTOR = 0.1

    /** Minutos de Sopor que duplican la Cordura (la escala del logaritmo). */
    const val SANITY_MINUTES = 10.0

    /** Minutos de Sopor por cada Ascensión extra. */
    const val MINUTES_PER_EXTRA = 60

    /** Ascensiones extra como mucho por vida. */
    const val MAX_EXTRA = 4

    fun isUnlocked(transcendences: Int): Boolean = transcendences >= UNLOCK_TRANSCENDENCES

    /** La Cordura ganada al Ascender con [seconds] de Sopor: x(1 + log2(1 + minutos/10)). */
    fun sanityMultiplier(seconds: Long): Double {
        if (seconds <= 0) return 1.0
        val minutes = seconds / 60.0
        return 1.0 + ln(1.0 + minutes / SANITY_MINUTES) / ln(2.0)
    }

    /** Las Ascensiones de más que cuenta una Ascensión con [seconds] de Sopor. */
    fun extraAscensions(seconds: Long): Int =
        floor(seconds / 60.0 / MINUTES_PER_EXTRA).toInt().coerceIn(0, MAX_EXTRA)

    /** Segundos de Sopor que faltan para la próxima Ascensión extra (0 si ya están todas). */
    fun secondsToNextExtra(seconds: Long): Long {
        if (extraAscensions(seconds) >= MAX_EXTRA) return 0
        val step = MINUTES_PER_EXTRA * 60L
        return step - (seconds.coerceAtLeast(0) % step)
    }
}
