package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import com.EGomez.idleecosystem.domain.model.Trial
import java.math.BigDecimal
import java.math.MathContext

/**
 * Romper el Velo (v0.19.6), inspirado en Romper el Infinito (Antimatter Dimensions) y en
 * Revolution Idle. El Velo es un tope suave al Conocimiento de una vida: pasado [THRESHOLD]
 * la producción cae con el cuadrado de lo que lo superas, así que conviene Ascender.
 * Superar las 10 Pruebas deja romperlo para siempre: el tope desaparece y la producción
 * se multiplica por [BROKEN_PRODUCTION].
 */
object Veil {
    /** Conocimiento de una vida donde empieza el Velo (1 septillón). */
    val THRESHOLD: BigDecimal = BigDecimal("1e25")

    /** Multiplicador de producción con el Velo roto. */
    const val BROKEN_PRODUCTION = 100.0

    /** Lo que se rompe: el Velo cae al superar todas las Pruebas. */
    fun canBreak(completedTrials: Int, broken: Boolean): Boolean = !broken && completedTrials >= Trial.entries.size

    /** Si el Velo frena esta vida: Conocimiento por encima del tope y el Velo entero. */
    fun isPressing(knowledgeThisRun: BigDecimal, broken: Boolean, threshold: BigDecimal = THRESHOLD): Boolean = !broken && knowledgeThisRun > threshold

    /**
     * Lo que multiplica la producción: con el Velo roto, [BROKEN_PRODUCTION]; si no, 1
     * por debajo del tope y (tope / Conocimiento)² por encima (x10 de más es /100).
     */
    fun productionFactor(knowledgeThisRun: BigDecimal, broken: Boolean, threshold: BigDecimal = THRESHOLD): BigDecimal = when {
        broken -> BigDecimal.valueOf(BROKEN_PRODUCTION)
        knowledgeThisRun <= threshold -> BigDecimal.ONE
        else -> threshold.divide(knowledgeThisRun, MathContext.DECIMAL64).pow(2, MathContext.DECIMAL64)
    }

    /** Órdenes de magnitud por encima del Velo que alcanzó esta vida (0 si no lo pasó). */
    fun depth(knowledgeThisRun: BigDecimal, threshold: BigDecimal = THRESHOLD): Int =
        if (knowledgeThisRun.signum() <= 0) 0
        else (BigMath.exponent10(knowledgeThisRun) - BigMath.exponent10(threshold)).coerceAtLeast(0)

    /** Cuánto falta para el Velo, de 0 a 1 en escala logarítmica (para la barra). */
    fun approach(knowledgeThisRun: BigDecimal, threshold: BigDecimal = THRESHOLD): Float {
        if (knowledgeThisRun.signum() <= 0) return 0f
        return (BigMath.exponent10(knowledgeThisRun).toFloat() / BigMath.exponent10(threshold)).coerceIn(0f, 1f)
    }

    /**
     * Lo que se gana sin conexión con [rate] por segundo (ya frenado por el Velo de ahora).
     * Por encima del tope el ritmo cae con (tope / K)², así que K³ crece en línea recta:
     * K³ = K₀³ + 3·P·tope²·t, con P el ritmo sin freno. Se resuelve en unidades del tope.
     */
    fun offlineGain(knowledgeThisRun: BigDecimal, rate: BigDecimal, seconds: Long, broken: Boolean, threshold: BigDecimal = THRESHOLD): BigDecimal {
        val linear = rate.multiply(BigDecimal(seconds))
        if (broken || linear.signum() <= 0) return linear
        val start = knowledgeThisRun.max(BigDecimal.ZERO)
        if (start.add(linear) <= threshold) return linear
        // El ritmo sin el freno de ahora
        val raw = rate.divide(productionFactor(start, false, threshold), MathContext.DECIMAL64)
        // Tramo libre hasta el tope
        val free = threshold.subtract(start).max(BigDecimal.ZERO)
        val freeSeconds = free.divide(raw, MathContext.DECIMAL64).toDouble()
        val rest = (seconds - freeSeconds).coerceAtLeast(0.0)
        val x0 = start.max(threshold).divide(threshold, MathContext.DECIMAL64).toDouble()
        val k = raw.divide(threshold, MathContext.DECIMAL64).toDouble()
        val x1 = Math.cbrt(x0 * x0 * x0 + 3.0 * k * rest)
        val end = BigDecimal.valueOf(x1).multiply(threshold, MathContext.DECIMAL64)
        return end.subtract(start).max(BigDecimal.ZERO)
    }
}
