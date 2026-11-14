package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class TranscendenceTrialsTest {

    @Test
    fun `the trials open at the sixth transcendence`() {
        assertFalse(TranscendenceTrials.isUnlocked(5))
        assertTrue(TranscendenceTrials.isUnlocked(6))
    }

    @Test
    fun `each level asks for a better life`() {
        assertEquals(0, BigDecimal("1e12").compareTo(TranscendenceTrials.goal(1)))
        assertEquals(0, BigDecimal("1e24").compareTo(TranscendenceTrials.goal(5)))
        (1 until TranscendenceTrials.MAX_LEVEL).forEach { assertTrue(TranscendenceTrials.goal(it + 1) > TranscendenceTrials.goal(it)) }
        assertTrue(TranscendenceTrials.isMet(1, BigDecimal("1e12")))
        assertFalse(TranscendenceTrials.isMet(2, BigDecimal("9.9e14")))
    }

    @Test
    fun `there are five levels and then the trial is mastered`() {
        assertEquals(1, TranscendenceTrials.nextLevel(0))
        assertEquals(5, TranscendenceTrials.nextLevel(4))
        assertNull(TranscendenceTrials.nextLevel(5))
        assertEquals("III", TranscendenceTrials.roman(3))
        assertEquals("V", TranscendenceTrials.roman(5))
    }

    @Test
    fun `rewards grow with every level passed`() {
        assertEquals(1.0, TranscendenceTrials.productionReward(0), 1e-12)
        assertEquals(243.0, TranscendenceTrials.productionReward(5), 1e-9)
        assertEquals(1.5.let { it * it }, TranscendenceTrials.sanityReward(2), 1e-12)
        assertEquals(2.0, TranscendenceTrials.etherReward(5), 1e-12)
    }

    @Test
    fun `silence lowers the ascensions needed but never below ten`() {
        assertEquals(50, TranscendenceTrials.requiredAscensions(50, 0))
        assertEquals(40, TranscendenceTrials.requiredAscensions(50, 5))
        assertEquals(10, TranscendenceTrials.requiredAscensions(14, 5))
        // Un requisito ya menor que 10 no sube
        assertEquals(8, TranscendenceTrials.requiredAscensions(8, 5))
    }

    @Test
    fun `ids and indexes find every trial`() {
        TranscendenceTrial.entries.forEach {
            assertEquals(it, TranscendenceTrial.fromId(it.id))
            assertEquals(it, TranscendenceTrial.fromIndex(it.ordinal + 1))
        }
        assertNull(TranscendenceTrial.fromIndex(0))
        // Las claves no se confunden con las de las Pruebas de Locura
        assertFalse(AchievementStat.ttrial("hunger").startsWith("trial"))
    }
}
