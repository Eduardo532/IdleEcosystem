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
}
