package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class DeepSleepTest {

    @Test
    fun `the dream opens at the third transcendence`() {
        assertFalse(DeepSleep.isUnlocked(2))
        assertTrue(DeepSleep.isUnlocked(3))
    }

    @Test
    fun `the seventy steps soften the dream but never past 1`() {
        assertEquals(0.75, DeepSleep.exponent(0), 1e-12)
        assertEquals(0.95, DeepSleep.exponent(10), 1e-12)
        assertEquals(1.0, DeepSleep.exponent(50), 1e-12)
    }

    @Test
    fun `dream points grow with the best life of the dream`() {
        assertEquals(0, DeepSleep.totalPoints(BigDecimal.ZERO))
        assertEquals(0, DeepSleep.totalPoints(BigDecimal("1e10")))
        assertEquals(8, DeepSleep.totalPoints(BigDecimal("1e14")))
        assertEquals(27, DeepSleep.totalPoints(BigDecimal("1e19")))
        // Más allá de Double también funciona
        assertTrue(DeepSleep.totalPoints(BigDecimal("1e400")) > 7000)
        // Los Descarnados de la Noche: +25 %
        assertEquals(10, DeepSleep.totalPoints(BigDecimal("1e14"), DeepSleep.pointsMultiplier(1)))
    }

    @Test
    fun `only what beats the points already earned is paid`() {
        assertEquals(8, DeepSleep.gain(BigDecimal("1e14"), 0))
        assertEquals(3, DeepSleep.gain(BigDecimal("1e14"), 5))
        assertEquals(0, DeepSleep.gain(BigDecimal("1e14"), 20))
    }

    @Test
    fun `the tree needs points and the parent node`() {
        val none = emptyMap<String, Int>()
        assertTrue(DeepSleep.canBuy(DreamNode.STEPS, none, 1))
        assertFalse(DeepSleep.canBuy(DreamNode.STEPS, none, 0))
        assertFalse(DeepSleep.canBuy(DreamNode.FLAME, none, 100))
        assertTrue(DeepSleep.canBuy(DreamNode.FLAME, mapOf("steps" to 1), 100))
        assertFalse(DeepSleep.canBuy(DreamNode.STEPS, mapOf("steps" to DreamNode.STEPS.maxLevel), 1000))
    }

    @Test
    fun `resetting the tree refunds everything spent`() {
        val levels = mapOf("steps" to 3, "flame" to 2)
        val spent = (0 until 3).sumOf { DreamNode.STEPS.cost(it) } + (0 until 2).sumOf { DreamNode.FLAME.cost(it) }
        assertEquals(spent, DeepSleep.refund(levels))
        assertEquals(0, DeepSleep.refund(emptyMap()))
    }

    @Test
    fun `every node points to an earlier node and costs grow`() {
        DreamNode.entries.forEachIndexed { index, node ->
            node.requires?.let { parent ->
                val required = DreamNode.fromId(parent)
                assertNotNull("${node.id} pide $parent", required)
                assertTrue(required!!.ordinal < index)
            }
            assertTrue(node.cost(1) >= node.cost(0))
            assertTrue(node.cost(0) >= 1)
        }
        assertEquals(1, DreamNode.entries.count { it.requires == null })
    }

    @Test
    fun `an exponent below 1 dims huge numbers without overflowing`() {
        val dimmed = Lab.pow(BigDecimal("1e400"), DeepSleep.BASE_EXPONENT)
        assertEquals(300.0, BigMath.ln(dimmed) / Math.log(10.0), 1e-6)
        val factor = Lab.factorFor(BigDecimal("1e20"), DeepSleep.BASE_EXPONENT)
        assertEquals(1e-5, factor.toDouble(), 1e-11)
    }

    @Test
    fun `node effects multiply as described`() {
        assertEquals(8.0, DeepSleep.productionMultiplier(3), 1e-9)
        assertEquals(9.0, DeepSleep.clickMultiplier(2), 1e-9)
        assertEquals(1.5625, DeepSleep.sanityMultiplier(2), 1e-9)
        assertEquals(4.0, DeepSleep.offlineMultiplier(2), 1e-9)
        assertEquals(1.3, DeepSleep.etherMultiplier(2), 1e-9)
        assertEquals(1.03, DeepSleep.kadathExponent(3), 1e-9)
    }
}
