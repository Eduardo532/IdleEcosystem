package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class BuffMathTest {

    private fun assertBd(expected: String, actual: BigDecimal) =
        assertEquals(0, BigDecimal(expected).compareTo(actual))

    @Test
    fun `no buffs is x1`() {
        assertBd("1", BuffMath.additive(emptyList()))
        assertBd("1", BuffMath.additive(listOf(1.0, 1.0)))
    }

    @Test
    fun `a single buff keeps its value`() {
        assertBd("50", BuffMath.additive(listOf(50.0)))
    }

    @Test
    fun `buffs add their bonuses instead of multiplying`() {
        // Before: Cthulhu x777 and Tidal x500 multiplied to x388500
        assertBd("1276", BuffMath.additive(listOf(777.0, 500.0)))
        // Event x2 + ritual x3 + rune x50 = 1 + 1 + 2 + 49
        assertBd("53", BuffMath.additive(listOf(2.0, 3.0, 50.0)))
    }

    @Test
    fun `values below x1 are ignored`() {
        assertBd("10", BuffMath.additive(listOf(0.5, 10.0)))
    }

    @Test
    fun `convergence multiplies the boosts instead of adding them`() {
        // x20 + x6 + x3 sumados dan x27; multiplicados, x360
        assertEquals(0, BigDecimal(27).compareTo(BuffMath.combine(listOf(20.0, 6.0, 3.0), convergence = false)))
        assertEquals(0, BigDecimal(360).compareTo(BuffMath.combine(listOf(20.0, 6.0, 3.0), convergence = true)))
    }

    @Test
    fun `convergence is capped and never worse than adding`() {
        // x50 × x40 = x2.000 -> tope x1.000
        assertEquals(0, BigDecimal(1_000).compareTo(BuffMath.converged(listOf(50.0, 40.0))))
        // x777 + x500 sumados ya dan x1.276: el tope no baja de la suma
        assertEquals(0, BigDecimal(1_276).compareTo(BuffMath.converged(listOf(777.0, 500.0))))
        // Un solo potenciador (o ninguno) vale lo mismo que sumado
        assertEquals(0, BigDecimal(10).compareTo(BuffMath.converged(listOf(10.0, 1.0))))
        assertEquals(0, BigDecimal.ONE.compareTo(BuffMath.converged(emptyList())))
        // Por encima del tope, la suma manda: x1.500 + x200 no baja a x1.000
        assertEquals(0, BigDecimal(1_700).compareTo(BuffMath.converged(listOf(1_500.0, 201.0))))
    }
}
