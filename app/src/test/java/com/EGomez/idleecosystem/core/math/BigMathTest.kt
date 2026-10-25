package com.EGomez.idleecosystem.core.math

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.random.Random

class BigMathTest {

    private fun exactFloorSqrt(x: BigInteger): BigInteger {
        // Búsqueda binaria de referencia
        var lo = BigInteger.ZERO
        var hi = x.add(BigInteger.ONE)
        while (lo.add(BigInteger.ONE) < hi) {
            val mid = lo.add(hi).shiftRight(1)
            if (mid.multiply(mid) <= x) lo = mid else hi = mid
        }
        return lo
    }

    @Test
    fun `raiz cuadrada exacta en valores conocidos`() {
        assertEquals(BigDecimal.ZERO, BigMath.floorSqrt(BigDecimal.ZERO))
        assertEquals(BigDecimal.ONE, BigMath.floorSqrt(BigDecimal("3.99")))
        assertEquals(BigDecimal(2), BigMath.floorSqrt(BigDecimal(4)))
        assertEquals(BigDecimal(1000), BigMath.floorSqrt(BigDecimal(1_000_000)))
    }

    @Test
    fun `raiz cuadrada coincide con la referencia en valores aleatorios`() {
        val random = Random(42)
        repeat(500) {
            val value = BigInteger.valueOf(random.nextLong(1, 1_000_000_000_000L))
            val expected = exactFloorSqrt(value)
            val actual = BigMath.floorSqrt(BigDecimal(value)).toBigInteger()
            // Double tiene ~15 dígitos: se tolera ±1 en los valores más grandes
            val diff = expected.subtract(actual).abs()
            assert(diff <= BigInteger.ONE) { "sqrt($value): esperado $expected, obtenido $actual" }
        }
    }

    @Test
    fun `raiz cuadrada no desborda con numeros enormes`() {
        val result = BigMath.floorSqrt(BigDecimal("1e600"))
        assertEquals(301, result.precision() - result.scale())
    }

    @Test
    fun `logaritmo natural de numeros enormes`() {
        assertEquals(Math.log(1000.0), BigMath.ln(BigDecimal(1000)), 1e-9)
        assertEquals(1000 * Math.log(10.0), BigMath.ln(BigDecimal("1e1000")), 1e-6)
    }
}
