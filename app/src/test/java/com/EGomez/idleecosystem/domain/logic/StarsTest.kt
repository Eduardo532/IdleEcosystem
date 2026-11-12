package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StarsTest {

    @Test
    fun `a star is born every ten minutes at first and faster with nebula and transcendences`() {
        assertEquals(1.0 / 600.0, Stars.starsPerSecond(0, 0), 1e-12)
        assertEquals(1.25 / 600.0, Stars.starsPerSecond(1, 0), 1e-12)
        assertEquals(2.0 / 600.0, Stars.starsPerSecond(0, 10), 1e-12)
        // Las Trascendencias suman hasta x3
        assertEquals(3.0 / 600.0, Stars.starsPerSecond(0, 100), 1e-12)
    }

    @Test
    fun `the alignment window repeats every half hour`() {
        assertTrue(Stars.isAligned(0, 0))
        assertTrue(Stars.isAligned(179, 0))
        assertFalse(Stars.isAligned(180, 0))
        assertTrue(Stars.isAligned(180, 1))
        assertTrue(Stars.isAligned(1800 + 10, 0))
        assertEquals(170, Stars.secondsLeft(10, 0))
        assertEquals(1800 - 200, Stars.secondsLeft(200, 0))
    }

    @Test
    fun `aligned stars give five times the dust`() {
        assertEquals(1.0, Stars.dustPerSecond(10.0, aligned = false), 1e-12)
        assertEquals(5.0, Stars.dustPerSecond(10.0, aligned = true), 1e-12)
    }

    @Test
    fun `offline dust is the area under the growing stars`() {
        val (stars, dust) = Stars.offline(10.0, 0.01, 100)
        assertEquals(11.0, stars, 1e-9)
        // 0,1 * (10*100 + 0,01*100^2/2) = 0,1 * 1050
        assertEquals(105.0, dust, 1e-9)
    }

    @Test
    fun `upgrade costs grow and some have a cap`() {
        assertEquals(10.0, StarUpgrade.STARLIGHT.cost(0), 0.0)
        assertEquals(30.0, StarUpgrade.STARLIGHT.cost(1), 0.0)
        assertTrue(StarUpgrade.CONJUNCTION.isMaxed(5))
        assertFalse(StarUpgrade.STARLIGHT.isMaxed(1000))
    }

    @Test
    fun `stellar multipliers`() {
        assertEquals(2.25, Stars.productionMultiplier(2, 0, 0.0), 1e-12)
        assertEquals(1.5, Stars.productionMultiplier(0, 5, 10.0), 1e-12)
        assertEquals(1.5, Stars.sanityMultiplier(2), 1e-12)
        assertEquals(1.3, Stars.etherMultiplier(3), 1e-12)
    }
}
