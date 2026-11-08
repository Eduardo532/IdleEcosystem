package com.EGomez.idleecosystem.core.math

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Operaciones sobre BigDecimal que funcionan con números de cualquier tamaño.
 *
 * Convertir a Double desborda por encima de ~1e308 (Infinity) y BigDecimal.sqrt()
 * solo existe desde Android 13 (API 33), así que se trabaja con mantisa + exponente.
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

    /** Raíz cuadrada redondeada hacia abajo de un BigDecimal no negativo. */
    fun floorSqrt(x: BigDecimal): BigDecimal {
        if (x.signum() <= 0) return BigDecimal.ZERO
        val exp = exponent10(x)
        val half = Math.floorDiv(exp, 2) // exponente par para que la raíz sea exacta
        val mantissa = x.movePointLeft(half * 2).toDouble() // en [1, 100)
        var root = BigDecimal(Math.sqrt(mantissa)).movePointRight(half).setScale(0, RoundingMode.FLOOR)
        // Para valores que Double representa con todos sus dígitos (raíz < 1e15),
        // corrige el posible error de ±1 con aritmética exacta. Más arriba la
        // diferencia es despreciable frente al tamaño del número.
        if (exp < 30) {
            while (root.multiply(root) > x) root = root.subtract(BigDecimal.ONE)
            while (root.add(BigDecimal.ONE).pow(2) <= x) root = root.add(BigDecimal.ONE)
        }
        return root
    }
}
