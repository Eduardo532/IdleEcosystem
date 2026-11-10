package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class TranscendenceTest {

    @Test
    fun `ether needs fifty ascensions in the cycle`() {
        assertEquals(0, Transcendence.etherGain(49, BigDecimal("1e6")))
        // √(50 × log10(10.001)) ≈ √200 ≈ 14,1
        assertEquals(14, Transcendence.etherGain(50, BigDecimal(10_000)))
        assertEquals(0, Transcendence.etherGain(50, BigDecimal.ZERO))
    }

    @Test
    fun `more lives and better lives give more ether`() {
        val base = Transcendence.etherGain(50, BigDecimal(10_000))
        assertTrue(Transcendence.etherGain(100, BigDecimal(10_000)) > base)
        assertTrue(Transcendence.etherGain(50, BigDecimal("1e12")) > base)
        assertTrue(Transcendence.etherGain(50, BigDecimal(10_000), 1.25) > base)
    }

    @Test
    fun `eternal upgrades cost more each level and some have a cap`() {
        val fire = EternalUpgrade.ETERNAL_FIRE
        assertEquals(1, fire.cost(0))
        assertTrue(fire.cost(5) > fire.cost(4))
        assertFalse(fire.isMaxed(100))
        assertTrue(EternalUpgrade.REMEMBERED_TRIALS.isMaxed(1))
        assertTrue(EternalUpgrade.MEMORY.isMaxed(10))
        assertEquals(8.0, Transcendence.productionMultiplier(3), 1e-9)
    }
}
