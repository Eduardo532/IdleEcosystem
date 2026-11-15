package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ExpeditionsTest {

    @Test
    fun `horrors get tougher every level`() {
        assertEquals(100.0, Expeditions.health(0), 1e-9)
        assertEquals(125.0, Expeditions.health(1), 1e-9)
        assertTrue(Expeditions.health(41) > Expeditions.health(40))
    }

    @Test
    fun `damage grows with the digits of production`() {
        assertEquals(1.0, Expeditions.damagePerSecond(BigDecimal.ZERO), 1e-9)
        assertEquals(961.0, Expeditions.damagePerSecond(BigDecimal("1e30")), 1e-3)
        assertEquals(1922.0, Expeditions.damagePerSecond(BigDecimal("1e30"), 2.0), 1e-3)
        // Más allá de Double también funciona
        assertTrue(Expeditions.damagePerSecond(BigDecimal("1e400")) > 160_000.0)
    }

    @Test
    fun `a strike can defeat several horrors and keeps the leftover`() {
        // 100 + 125 = 225 vence dos horrores; sobran 25
        val (level, dealt, kills) = Expeditions.strike(0, 0.0, 250.0)
        assertEquals(2, level)
        assertEquals(2, kills)
        assertEquals(25.0, dealt, 1e-9)
        val (same, partial, none) = Expeditions.strike(2, 25.0, 10.0)
        assertEquals(2, same)
        assertEquals(35.0, partial, 1e-9)
        assertEquals(0, none)
        // Un daño enorme no se cuelga
        assertTrue(Expeditions.strike(0, 0.0, Double.MAX_VALUE).third <= Expeditions.MAX_KILLS)
    }

    @Test
    fun `gold grows faster than the level`() {
        assertEquals(0, Expeditions.goldFor(0))
        assertEquals(1, Expeditions.goldFor(1))
        assertEquals(49, Expeditions.goldFor(20))
        assertTrue(Expeditions.goldFor(100) > 300)
    }

    @Test
    fun `relics open by expedition level and cost more each level`() {
        assertFalse(Relic.TRAPEZOHEDRON.isUnlocked(9))
        assertTrue(Relic.TRAPEZOHEDRON.isUnlocked(10))
        assertEquals(10, Relic.TRAPEZOHEDRON.cost(0))
        assertEquals(20, Relic.TRAPEZOHEDRON.cost(1))
        Relic.entries.zipWithNext().forEach { (a, b) -> assertTrue(a.unlockLevel < b.unlockLevel) }
        assertEquals(25.0, Expeditions.productionMultiplier(2), 1e-9)
        assertEquals(2.0, Expeditions.etherMultiplier(2), 1e-9)
        assertEquals(1.3, Expeditions.visionMultiplier(3), 1e-9)
    }

    @Test
    fun `every level has a horror from the bestiary`() {
        (0..50).forEach { assertTrue(Expeditions.horror(it) in Bestiary.beasts) }
        assertFalse(Expeditions.horror(0) == Expeditions.horror(1))
    }
}
