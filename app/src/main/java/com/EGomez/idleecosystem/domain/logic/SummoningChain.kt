package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.pow

/**
 * Un eslabón de la Cadena de Invocación. El 1 produce Poder de Invocación y cada uno de
 * los demás produce unidades del eslabón de abajo. Se compran con Conocimiento.
 */
enum class Invocation(
    val tier: Int,
    val title: String,
    val description: String
) {
    CHALK(1, "Círculo de Sal", "Produce Poder de Invocación."),
    CANDLE(2, "Cirio Negro", "Produce Círculos de Sal."),
    PENTACLE(3, "Pentáculo", "Produce Cirios Negros."),
    ALTAR(4, "Osario", "Produce Pentáculos."),
    CHOIR(5, "Coro de Cultistas", "Produce Osarios."),
    GATE(6, "Puerta de Plata", "Produce Coros de Cultistas."),
    SPHERES(7, "Ritual de las Esferas", "Produce Puertas de Plata."),
    CONJUNCTION(8, "Conjunción Estelar", "Produce Rituales de las Esferas.");

    val id: String get() = tier.toString()

    /** Precio base: 10^(3 + 3·eslabón) de Conocimiento (1e6 el primero, 1e27 el último). */
    val baseCost: BigDecimal get() = BigDecimal.ONE.scaleByPowerOfTen(3 + 3 * tier)

    /** Cada compra encarece el eslabón: x2 el primero, x5,5 el último. */
    val costGrowth: Double get() = 1.5 + 0.5 * tier

    /** Lo que cuesta la próxima unidad tras haber comprado [bought]. */
    fun cost(bought: Int): BigDecimal =
        BigDecimal(costGrowth).pow(bought, MathContext.DECIMAL64).multiply(baseCost, MathContext.DECIMAL64)

    companion object {
        fun fromTier(tier: Int): Invocation? = entries.firstOrNull { it.tier == tier }
    }
}

/**
 * La Cadena de Invocación (v0.20.1), inspirada en las dimensiones de Antimatter Dimensions.
 * Desde la 4ª Trascendencia se compran ocho eslabones con Conocimiento. Cada uno produce
 * al de abajo y el primero produce Poder de Invocación, que multiplica la producción.
 * Las compras sobreviven a las Ascensiones; la Cadena se deshace al Trascender.
 */
object SummoningChain {
    const val UNLOCK_TRANSCENDENCES = 4

    /** Cada [MULTIPLIER_STEP] compras de un eslabón duplican lo que produce. */
    const val MULTIPLIER_STEP = 10

    /** Sin conexión la Cadena avanza en, como mucho, este número de pasos. */
    const val OFFLINE_STEPS = 120

    private val MC = MathContext.DECIMAL64

    fun isUnlocked(transcendences: Int): Boolean = transcendences >= UNLOCK_TRANSCENDENCES

    /** x2 por cada [MULTIPLIER_STEP] compras del eslabón. */
    fun tierMultiplier(bought: Int): Double = 2.0.pow(bought / MULTIPLIER_STEP)

    /** Lo que produce por segundo un eslabón con [amount] unidades y [bought] compras. */
    fun rate(amount: BigDecimal, bought: Int): BigDecimal =
        amount.multiply(BigDecimal.valueOf(tierMultiplier(bought)), MC)

    /**
     * Avanza la Cadena [seconds] segundos. [amounts] y [bought] van del eslabón 1 al 8.
     * Devuelve las unidades nuevas y el Poder ganado. Todos los ritmos se toman antes de
     * sumar, así el orden no importa.
     */
    fun step(amounts: List<BigDecimal>, bought: List<Int>, seconds: Double): Pair<List<BigDecimal>, BigDecimal> {
        val dt = BigDecimal.valueOf(seconds)
        val produced = amounts.indices.map { rate(amounts[it], bought[it]).multiply(dt, MC) }
        val next = amounts.indices.map { i ->
            if (i + 1 < amounts.size) amounts[i].add(produced[i + 1], MC) else amounts[i]
        }
        return next to produced[0]
    }

    /** Avanza [seconds] en varios pasos (para el tiempo sin conexión). */
    fun advance(amounts: List<BigDecimal>, bought: List<Int>, seconds: Long, steps: Int = OFFLINE_STEPS): Pair<List<BigDecimal>, BigDecimal> {
        if (seconds <= 0) return amounts to BigDecimal.ZERO
        val n = steps.coerceAtMost(seconds.toInt().coerceAtLeast(1))
        val dt = seconds.toDouble() / n
        var current = amounts
        var power = BigDecimal.ZERO
        repeat(n) {
            val (next, gained) = step(current, bought, dt)
            current = next
            power = power.add(gained, MC)
        }
        return current to power
    }

    /** El Poder multiplica la producción: (1 + log10(1 + Poder))². */
    fun productionMultiplier(power: BigDecimal): Double {
        if (power.signum() <= 0) return 1.0
        val log = BigMath.ln(power.add(BigDecimal.ONE)) / Math.log(10.0)
        return (1.0 + log).pow(2)
    }
}
