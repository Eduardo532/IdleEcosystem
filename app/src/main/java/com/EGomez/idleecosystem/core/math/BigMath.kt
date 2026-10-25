package com.EGomez.idleecosystem.core.math

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utilidades matemáticas para BigDecimal que funcionan con números de cualquier tamaño.
 *
 * Convertir a Double desborda por encima de ~1e308 (Infinity) y BigDecimal.sqrt()
 * solo existe desde Android 13 (API 33), por eso trabajamos con mantisa + exponente.
 */
object BigMath {

    /** Exponente decimal: floor(log10(|x|)). Requiere x != 0. */
    fun exponent10(x: BigDecimal): Int = x.precision() - x.scale() - 1

    /** Logaritmo natural de un BigDecimal positivo, sin desbordar. */
    fun ln(x: BigDecimal): Double {
        require(x.signum() > 0) { "ln requiere un número positivo" }
        val exp = exponent10(x)
        val mantissa = x.movePointLeft(exp).toDouble() // en [1, 10)
        return Math.log(mantissa) + exp * Math.log(10.0)
    }

    /** Raíz cuadrada entera (redondeada hacia abajo) de un BigDecimal no negativo. */
    fun floorSqrt(x: BigDecimal): BigDecimal {
        if (x.signum() <= 0) return BigDecimal.ZERO
        val exp = exponent10(x)
        // Exponente par para que la raíz sea exacta en potencias de 10
        val half = Math.floorDiv(exp, 2)
        val mantissa = x.movePointLeft(half * 2).toDouble() // en [1, 100)
        val root = BigDecimal(Math.sqrt(mantissa)).movePointRight(half)
        return root.setScale(0, RoundingMode.FLOOR)
    }
}
