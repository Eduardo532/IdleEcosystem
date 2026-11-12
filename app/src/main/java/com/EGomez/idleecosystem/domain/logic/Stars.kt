package com.EGomez.idleecosystem.domain.logic

import kotlin.math.ceil
import kotlin.math.pow

/**
 * Cuando las Estrellas Estén en Posición (v0.19.7), inspirado en las Estrellas y el
 * Polvo Estelar de Revolution Idle. Tras la primera Trascendencia nacen Estrellas poco a
 * poco (también sin conexión) y nunca se pierden. Cada Estrella da Polvo Estelar cada
 * segundo, que compra mejoras estelares. Cada media hora las estrellas se alinean unos
 * minutos: mientras tanto el Polvo cae x[Stars.ALIGNED_DUST].
 */
enum class StarUpgrade(
    val id: String,
    val title: String,
    val description: String,
    val baseCost: Double,
    val costGrowth: Double,
    val maxLevel: Int?
) {
    STARLIGHT("light", "Luz Estelar", "Producción x1,5 por nivel.", 10.0, 3.0, null),
    NEBULA("nebula", "Nebulosa", "Las Estrellas nacen un 25 % más rápido por nivel.", 30.0, 4.0, null),
    STAR_SANITY("sanity", "Cordura de las Estrellas", "Cordura ganada al Ascender +25 % por nivel.", 50.0, 3.5, null),
    DARK_STARS("dark", "Estrellas Negras", "Cada Estrella da +1 % de producción por nivel.", 200.0, 4.0, null),
    CONJUNCTION("conjunction", "Gran Conjunción", "La alineación dura 1 minuto más por nivel.", 100.0, 6.0, 5),
    ETHER_DUST("ether", "Polvo de Éter", "Éter ganado al Trascender +10 % por nivel.", 500.0, 5.0, 10);

    /** Costo en Polvo Estelar del próximo nivel. */
    fun cost(level: Int): Double = ceil(baseCost * costGrowth.pow(level))

    fun isMaxed(level: Int): Boolean = maxLevel != null && level >= maxLevel

    companion object {
        fun fromId(id: String): StarUpgrade? = entries.firstOrNull { it.id == id }
    }
}

object Stars {
    /** Segundos que tarda en nacer una Estrella sin mejoras. */
    const val SECONDS_PER_STAR = 600.0

    /** Polvo Estelar por Estrella y por segundo. */
    const val DUST_PER_STAR = 0.1

    /** Cada cuánto se alinean las estrellas y cuánto dura la alineación (sin mejoras). */
    const val ALIGNMENT_PERIOD_SECONDS = 1800L
    const val ALIGNMENT_BASE_SECONDS = 180L

    /** Lo que multiplica el Polvo durante la alineación. */
    const val ALIGNED_DUST = 5.0

    /** Estrellas que nacen por segundo: Nebulosa y un 10 % por Trascendencia (hasta x3). */
    fun starsPerSecond(nebula: Int, transcendences: Int): Double =
        (1.0 + 0.25 * nebula) * (1.0 + 0.1 * transcendences).coerceAtMost(3.0) / SECONDS_PER_STAR

    /** Polvo por segundo con [stars] Estrellas. */
    fun dustPerSecond(stars: Double, aligned: Boolean): Double =
        stars * DUST_PER_STAR * if (aligned) ALIGNED_DUST else 1.0

    /**
     * Estrellas y Polvo tras [seconds] sin conexión: las Estrellas crecen en línea recta, así
     * que el Polvo es el área bajo esa recta. La alineación no cuenta sin conexión.
     */
    fun offline(stars: Double, rate: Double, seconds: Long): Pair<Double, Double> {
        val t = seconds.coerceAtLeast(0).toDouble()
        val newStars = stars + rate * t
        val dust = DUST_PER_STAR * (stars * t + rate * t * t / 2.0)
        return newStars to dust
    }

    fun alignmentSeconds(conjunction: Int): Long = ALIGNMENT_BASE_SECONDS + 60L * conjunction

    /** Si las estrellas están en posición en [epochSeconds]. */
    fun isAligned(epochSeconds: Long, conjunction: Int): Boolean =
        Math.floorMod(epochSeconds, ALIGNMENT_PERIOD_SECONDS) < alignmentSeconds(conjunction)

    /**
     * Segundos que faltan: hasta que termine la alineación si están en posición, o hasta
     * la próxima si no.
     */
    fun secondsLeft(epochSeconds: Long, conjunction: Int): Long {
        val phase = Math.floorMod(epochSeconds, ALIGNMENT_PERIOD_SECONDS)
        val window = alignmentSeconds(conjunction)
        return if (phase < window) window - phase else ALIGNMENT_PERIOD_SECONDS - phase
    }

    fun productionMultiplier(starlight: Int, darkStars: Int, stars: Double): Double =
        1.5.pow(starlight) * (1.0 + 0.01 * darkStars * stars)

    fun sanityMultiplier(level: Int): Double = 1.0 + 0.25 * level

    fun etherMultiplier(level: Int): Double = 1.0 + 0.1 * level
}
