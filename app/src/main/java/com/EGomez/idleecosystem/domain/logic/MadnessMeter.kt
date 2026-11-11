package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.RuneTier
import kotlin.math.exp
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

    /**
     * La Locura baja sola con el tiempo (también sin conexión), y más rápido cuanto más
     * alta está (v0.19.1): cada segundo baja `BASE_DECAY + DECAY_RATE × Locura`.
     * Se resuelve con la fórmula cerrada de esa ecuación, así que da lo mismo bajar
     * 600 segundos de una vez que de a uno.
     */
    fun decay(seconds: Long, multiplier: Double = 1.0) {
        if (seconds <= 0 || value <= 0.0) return
        val k = DECAY_RATE * multiplier
        val floor = BASE_DECAY / DECAY_RATE
        value = ((value + floor) * exp(-k * seconds) - floor).coerceIn(0.0, MAX)
    }

    /**
     * Lo que suma un clic (v0.19.3): menos cuanto más alta está la Locura, así que tocar
     * rápido la sube de verdad sin llevarla sola al colapso.
     */
    fun clickGain(): Double = PER_CLICK * (1.0 - value / CLICK_SOFTCAP).coerceAtLeast(0.0)

    /** Cuánto baja por segundo con la Locura actual (para explicarlo en pantalla). */
    fun decayPerSecond(): Double = BASE_DECAY + DECAY_RATE * value

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
        /**
         * Bajada por segundo: una parte fija y otra proporcional a la Locura. Hasta la
         * v0.19.0 era 0,2 fija y ni manteniendo pulsado subía; en la v0.19.1 tocar a mano
         * se quedaba en ~23 %. Desde la v0.19.3, tocando unas 3–4 veces por segundo se
         * estabiliza en ~50 % y manteniendo pulsado en ~80 %; para llegar al colapso hay
         * que sumar runas, rituales o la Convergencia.
         */
        const val BASE_DECAY = 0.03
        const val DECAY_RATE = 0.004
        const val OMEN_THRESHOLD = 50.0
        const val OMEN_DIVISOR = 500.0

        /** Cuánto sube cada acción. */
        const val PER_CLICK = 0.12
        /** Con esta Locura un clic ya no suma nada (nunca se alcanza: el colapso es a 100). */
        const val CLICK_SOFTCAP = 125.0
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
