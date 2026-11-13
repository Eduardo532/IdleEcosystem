package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.PactEffects
import com.EGomez.idleecosystem.domain.model.TidePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TidePhaseTest {

    @Test
    fun `the tide cycles every ten minutes of play`() {
        assertEquals(TidePhase.HIGH, TidePhase.at(0))
        assertEquals(TidePhase.HIGH, TidePhase.at(599))
        assertEquals(TidePhase.LOW, TidePhase.at(600))
        assertEquals(TidePhase.DEAD, TidePhase.at(1_200))
        assertEquals(TidePhase.HIGH, TidePhase.at(1_800))
        assertEquals(600, TidePhase.secondsLeft(0))
        assertEquals(1, TidePhase.secondsLeft(599))
    }

    @Test
    fun `every phase favours a different way to play`() {
        TidePhase.entries.forEach { assertNotEquals(PactEffects(), it.effects) }
        assertTrue(TidePhase.HIGH.effects.runeFrequency > 1.0 && TidePhase.HIGH.madnessGain > 1.0)
        assertTrue(TidePhase.LOW.effects.production > 1.0 && TidePhase.LOW.madnessDecay > 1.0)
        assertTrue(TidePhase.DEAD.effects.ritualCost < 1.0)
    }

    @Test
    fun `low tide calms madness twice as fast`() {
        // 10 segundos de Marea Baja calman lo mismo que 20 de marea normal
        val low = MadnessMeter(50.0).apply { decay(10, TidePhase.LOW.madnessDecay) }
        val normal = MadnessMeter(50.0).apply { decay(20) }
        assertEquals(normal.value, low.value, 1e-9)
        assertTrue(low.value < 50.0)
    }
}
