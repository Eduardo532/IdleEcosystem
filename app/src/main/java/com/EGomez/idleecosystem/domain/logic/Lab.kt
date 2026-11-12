package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/**
 * El Laboratorio de Miskatonic (v0.19.9), inspirado en las mejoras de exponente de
 * Revolution Idle y Antimatter Dimensions. Tras la primera Trascendencia, el Éter paga
 * experimentos que suben **exponentes** en lugar de multiplicadores: la producción, el
 * clic, la Cordura y el Éter se elevan a una potencia mayor que 1, y crecen más cuanto más
 * grandes son. Dos experimentos más potencian el Bestiario y alejan el Velo.
 */
enum class LabExperiment(
    val id: String,
    val title: String,
    val description: String,
    val baseCost: Double,
    val costGrowth: Double,
    val maxLevel: Int,
    /** Lo que suma cada nivel al exponente (o, en el Velo, órdenes de magnitud). */
    val perLevel: Double
) {
    ARMITAGE("armitage", "Fórmula de Armitage", "Producción elevada a ^1,01 por nivel.", 3.0, 1.8, 10, 0.01),
    IBN_GHAZI("ibn_ghazi", "Polvo de Ibn Ghazi", "Poder de clic elevado a ^1,02 por nivel.", 2.0, 1.7, 10, 0.02),
    CURWEN("curwen", "Sales Esenciales", "Cordura ganada elevada a ^1,02 por nivel.", 4.0, 1.9, 10, 0.02),
    PNAKOTIC("pnakotic", "Manuscritos Pnakóticos", "Éter ganado elevado a ^1,05 por nivel.", 10.0, 2.5, 5, 0.05),
    DYER("dyer", "Disección de Dyer", "Los bonos del Bestiario elevados a ^1,1 por nivel.", 6.0, 2.2, 5, 0.1),
    VEIL("veil", "Ecuación del Velo", "El Velo empieza 10 veces más lejos por nivel.", 2.0, 1.6, 10, 1.0);

    /** Éter que cuesta el próximo nivel. */
    fun cost(level: Int): Int = ceil(baseCost * costGrowth.pow(level)).toInt()

    fun isMaxed(level: Int): Boolean = level >= maxLevel

    /** El exponente con [level] niveles (1 sin niveles). */
    fun exponent(level: Int): Double = 1.0 + perLevel * level

    companion object {
        fun fromId(id: String): LabExperiment? = entries.firstOrNull { it.id == id }
    }
}

object Lab {
    /**
     * x elevado a [exponent], sin desbordar: se trabaja con log10. Solo eleva lo que es mayor
     * que 1 (elevar algo menor que 1 lo achicaría).
     */
    fun pow(x: BigDecimal, exponent: Double): BigDecimal {
        if (exponent == 1.0 || x <= BigDecimal.ONE) return x
        val log = BigMath.ln(x) / Math.log(10.0) * exponent
        val whole = floor(log)
        val mantissa = 10.0.pow(log - whole)
        return BigDecimal(mantissa, MathContext.DECIMAL64).scaleByPowerOfTen(whole.toInt())
    }

    fun pow(x: Double, exponent: Double): Double = if (exponent == 1.0 || x <= 1.0) x else x.pow(exponent)

    /** Lo que hay que multiplicar a [total] para que quede elevado a [exponent]. */
    fun factorFor(total: BigDecimal, exponent: Double): BigDecimal {
        if (exponent == 1.0 || total <= BigDecimal.ONE) return BigDecimal.ONE
        return pow(total, exponent).divide(total, MathContext.DECIMAL64)
    }

    /** El Velo, 10 veces más lejos por nivel de la Ecuación del Velo. */
    fun veilThreshold(level: Int): BigDecimal = Veil.THRESHOLD.scaleByPowerOfTen(level)
}
