package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class SummoningChainTest {

    private val zeros = List(8) { BigDecimal.ZERO }
    private val none = List(8) { 0 }

    @Test
    fun `the chain opens at the fourth transcendence`() {
        assertFalse(SummoningChain.isUnlocked(3))
        assertTrue(SummoningChain.isUnlocked(4))
    }

    @Test
    fun `prices start at 1e6 and grow faster up the chain`() {
        assertEquals(0, BigDecimal("1e6").compareTo(Invocation.CHALK.cost(0)))
        assertEquals(0, BigDecimal("2e6").compareTo(Invocation.CHALK.cost(1)))
        assertEquals(0, BigDecimal("1e27").compareTo(Invocation.CONJUNCTION.cost(0)))
        Invocation.entries.zipWithNext().forEach { (low, high) ->
            assertTrue(high.baseCost > low.baseCost)
            assertTrue(high.costGrowth > low.costGrowth)
        }
        // Mil compras no desbordan
        assertTrue(Invocation.CONJUNCTION.cost(1000) > BigDecimal("1e700"))
    }

    @Test
    fun `every ten purchases double a link`() {
        assertEquals(1.0, SummoningChain.tierMultiplier(9), 1e-12)
        assertEquals(2.0, SummoningChain.tierMultiplier(10), 1e-12)
        assertEquals(8.0, SummoningChain.tierMultiplier(35), 1e-12)
    }

    @Test
    fun `each link feeds the one below and the first one makes power`() {
        val amounts = zeros.toMutableList().apply { this[0] = BigDecimal(3); this[1] = BigDecimal(2) }
        val bought = none.toMutableList().apply { this[0] = 10 }
        val (next, power) = SummoningChain.step(amounts, bought, 1.0)
        // 3 Círculos con x2 producen 6 de Poder; 2 Cirios producen 2 Círculos
        assertEquals(0, BigDecimal(6).compareTo(power))
        assertEquals(0, BigDecimal(5).compareTo(next[0]))
        assertEquals(0, BigDecimal(2).compareTo(next[1]))
    }

    @Test
    fun `an empty chain makes nothing`() {
        val (next, power) = SummoningChain.step(zeros, none, 1.0)
        assertEquals(0, power.signum())
        assertTrue(next.all { it.signum() == 0 })
        assertEquals(1.0, SummoningChain.productionMultiplier(BigDecimal.ZERO), 1e-12)
    }

    @Test
    fun `offline advance grows the chain like the online ticks`() {
        val amounts = zeros.toMutableList().apply { this[0] = BigDecimal.ONE; this[1] = BigDecimal.ONE }
        val (online, onlinePower) = (1..60).fold(amounts.toList() to BigDecimal.ZERO) { (a, p), _ ->
            val (n, g) = SummoningChain.step(a, none, 1.0)
            n to p.add(g)
        }
        val (offline, offlinePower) = SummoningChain.advance(amounts, none, 60)
        assertEquals(0, online[0].compareTo(offline[0]))
        assertEquals(0, onlinePower.compareTo(offlinePower))
        // Con muchas horas se limita a los pasos de sin conexión y sigue creciendo
        val (_, long) = SummoningChain.advance(amounts, none, 8 * 3600)
        assertTrue(long > offlinePower)
    }

    @Test
    fun `power multiplies production by the square of its digits`() {
        assertEquals(4.0, SummoningChain.productionMultiplier(BigDecimal(9)), 1e-9)
        assertEquals(121.0, SummoningChain.productionMultiplier(BigDecimal("1e10")), 1e-6)
        assertEquals(10201.0, SummoningChain.productionMultiplier(BigDecimal("1e100")), 1e-3)
    }
}
