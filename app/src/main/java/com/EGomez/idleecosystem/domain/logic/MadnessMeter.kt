package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.RuneTier
import kotlin.random.Random

/**
 * Medidor de Locura (v0.17.3): sube cuando juegas fuerte (clics, runas, rituales, la
 * Convergencia) y baja solo con el tiempo. Más Locura da más producción, pero a partir
 * de la mitad aparecen presagios malos y al llegar a 100 la mente colapsa.
 *
 * El colapso es leve a propósito: se pierden los potenciadores activos y la producción
 * queda a la mitad un minuto. Nunca se pierde Conocimiento, Ecos ni progreso permanente.
 */
class MadnessMeter(value: Double = 0.0) {

    var value: Double = value.coerceIn(0.0, MAX)
        private set

    /** Suma Locura; devuelve true si con esto la mente colapsa. */
    fun add(amount: Double): Boolean {
        if (amount <= 0.0) return false
        value = (value + amount).coerceAtMost(MAX)
        return value >= MAX
    }

    fun calm(amount: Double) {
        value = (value - amount).coerceAtLeast(0.0)
    }

    /** La Locura baja sola con el tiempo (también sin conexión). */
    fun decay(seconds: Long) {
        if (seconds > 0) calm(DECAY_PER_SECOND * seconds)
    }

    fun reset() {
        value = 0.0
    }

    /** +1 % de producción por punto de Locura. */
    fun productionBonus(): Double = 1.0 + BONUS_PER_POINT * value

    /** Probabilidad por segundo de un presagio malo: 0 hasta la mitad, 10 % al borde. */
    fun omenChance(): Double = if (value < OMEN_THRESHOLD) 0.0 else (value - OMEN_THRESHOLD) / OMEN_DIVISOR

    fun rollOmen(random: Random = Random): Boolean = random.nextDouble() < omenChance()

    companion object {
        const val MAX = 100.0
        const val BONUS_PER_POINT = 0.01
        const val DECAY_PER_SECOND = 0.2
        const val OMEN_THRESHOLD = 50.0
        const val OMEN_DIVISOR = 500.0

        /** Cuánto sube cada acción. */
        const val PER_CLICK = 0.02
        const val PER_RITUAL = 5.0
        const val PER_CONVERGENCE = 20.0
        const val CALM_AMOUNT = 40.0

        fun perRune(tier: RuneTier): Double = when (tier) {
            RuneTier.TIER_1_SWIPE -> 2.0
            RuneTier.TIER_2_HOLD -> 4.0
            RuneTier.TIER_3_TRACE -> 8.0
        }

        /** El medidor aparece con esta cantidad de Ascensiones. */
        const val REVEAL_AT = 5

        /** El colapso: la producción queda a la mitad durante este tiempo. */
        const val COLLAPSE_SECONDS = 60
        const val COLLAPSE_PRODUCTION = 0.5

        /** Presagios: un edificio poseído deja de producir; los Ecos dispersos se pierden en parte. */
        const val POSSESSION_SECONDS = 30
        const val SCATTERED_ECHOES = 0.1
    }
}
