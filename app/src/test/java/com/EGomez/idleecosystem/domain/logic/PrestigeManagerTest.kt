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
    fun `pacing shows the next point and the doubling point`() {
        // 9,5M generados -> 3 puntos. El 4.º llega con 16M y el doble (6) con 36M.
        val pacing = manager.pacing(BigDecimal("9500000"))
        assertEquals(0, BigDecimal(4).compareTo(pacing.nextGain))
        assertEquals(0, BigDecimal("16000000").compareTo(pacing.nextAt))
        assertEquals(0, BigDecimal(6).compareTo(pacing.doubleGain))
        assertEquals(0, BigDecimal("36000000").compareTo(pacing.doubleAt))
    }

    @Test
    fun `doubling always needs four times the knowledge`() {
        listOf("1000000", "4000000", "2.5e9", "1e40").forEach { knowledge ->
            val points = manager.calculatePotentialPrestige(BigDecimal(knowledge))
            val pacing = manager.pacing(BigDecimal(knowledge))
            val ratio = pacing.doubleAt!!.divide(manager.knowledgeForPoints(points))
            assertEquals(knowledge, 0, BigDecimal(4).compareTo(ratio))
            // Y con ese Conocimiento se gana justo el doble
            assertEquals(knowledge, 0, points.multiply(BigDecimal(2)).compareTo(manager.calculatePotentialPrestige(pacing.doubleAt)))
        }
    }

    @Test
    fun `before the first point there is no doubling`() {
        val pacing = manager.pacing(BigDecimal("250000"))
        assertEquals(0, BigDecimal.ONE.compareTo(pacing.nextGain))
        assertEquals(0, BigDecimal("1000000").compareTo(pacing.nextAt))
        assertEquals(null, pacing.doubleAt)
    }

    @Test
    fun `the shop bonus multiplies the points, not the knowledge needed`() {
        val pacing = manager.pacing(BigDecimal("4000000"), gainMultiplier = BigDecimal("1.5"))
        assertEquals(0, BigDecimal("4.5").compareTo(pacing.nextGain))
        assertEquals(0, BigDecimal("9000000").compareTo(pacing.nextAt))
        assertEquals(0, BigDecimal(6).compareTo(pacing.doubleGain))
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
