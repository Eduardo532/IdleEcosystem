package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsecrationTest {

    @Test
    fun `each level needs more units and more ether`() {
        assertEquals(100, Consecration.requiredOwned(0))
        assertEquals(500, Consecration.requiredOwned(4))
        assertEquals(1, Consecration.etherCost(0))
        assertEquals(5, Consecration.etherCost(4))
        // Los cinco niveles de un edificio: 15 de Éter
        assertEquals(15, (0 until Consecration.MAX_LEVEL).sumOf { Consecration.etherCost(it) })
    }

    @Test
    fun `consecration triples production per level and stops at five`() {
        assertEquals(1.0, Consecration.multiplier(0), 1e-9)
        assertEquals(9.0, Consecration.multiplier(2), 1e-9)
        assertEquals(243.0, Consecration.multiplier(5), 1e-9)
        assertEquals(243.0, Consecration.multiplier(9), 1e-9)
    }

    @Test
    fun `it needs the units and the ether`() {
        assertTrue(Consecration.canConsecrate(0, 100, 1))
        assertFalse(Consecration.canConsecrate(0, 99, 5))
        assertFalse(Consecration.canConsecrate(1, 200, 1))
        assertFalse(Consecration.canConsecrate(5, 1_000, 100))
        assertEquals("III", Consecration.roman(3))
    }
}
