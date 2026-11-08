package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.random.Random

class PrestigeManagerTest {

    private val manager = PrestigeManager()

    @Test
    fun `no sanity below the threshold`() {
        assertEquals(BigDecimal.ZERO, manager.calculatePotentialPrestige(BigDecimal("999999")))
    }

    @Test
    fun `sanity is the square root of knowledge over the threshold`() {
        assertEquals(BigDecimal.ONE, manager.calculatePotentialPrestige(BigDecimal("1000000")))
        assertEquals(BigDecimal(2), manager.calculatePotentialPrestige(BigDecimal("4000000")))
        assertEquals(BigDecimal(10), manager.calculatePotentialPrestige(BigDecimal("100000000")))
    }

    @Test
    fun `does not crash with huge numbers`() {
        // Before: Math.sqrt(Infinity) -> BigDecimal(Infinity) threw NumberFormatException
        val result = manager.calculatePotentialPrestige(BigDecimal("1e500"))
        assertEquals(247, BigMath.exponent10(result))
    }

    @Test
    fun `floorSqrt matches exact integer square root`() {
        val random = Random(42)
        repeat(500) {
            val value = BigInteger.valueOf(random.nextLong(1, Long.MAX_VALUE))
            assertEquals("sqrt($value)", value.sqrtExact(), BigMath.floorSqrt(BigDecimal(value)).toBigInteger())
        }
    }

    private fun BigInteger.sqrtExact(): BigInteger {
        var lo = BigInteger.ZERO
        var hi = this.add(BigInteger.ONE)
        while (lo.add(BigInteger.ONE) < hi) {
            val mid = lo.add(hi).shiftRight(1)
            if (mid.multiply(mid) <= this) lo = mid else hi = mid
        }
        return lo
    }
}
