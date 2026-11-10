package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.RuneTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MadnessMeterTest {

    @Test
    fun `madness rises with play, falls with time and stays between 0 and 100`() {
        val meter = MadnessMeter()
        meter.add(MadnessMeter.PER_RITUAL)
        meter.add(MadnessMeter.perRune(RuneTier.TIER_3_TRACE))
        assertEquals(13.0, meter.value, 1e-9)
        meter.decay(10)
        assertEquals(11.0, meter.value, 1e-9)
        meter.decay(1_000)
        assertEquals(0.0, meter.value, 1e-9)
        assertEquals(100.0, MadnessMeter(250.0).value, 1e-9)
    }

    @Test
    fun `reaching 100 collapses the mind`() {
        val meter = MadnessMeter(95.0)
        assertFalse(meter.add(4.0))
        assertTrue(meter.add(MadnessMeter.PER_CONVERGENCE))
        assertEquals(100.0, meter.value, 1e-9)
    }

    @Test
    fun `holding the click alone never fills the meter`() {
        // Mantener pulsado: 8 clics por segundo, por debajo de lo que baja sola
        assertTrue(8 * MadnessMeter.PER_CLICK < MadnessMeter.DECAY_PER_SECOND)
    }

    @Test
    fun `each point is one percent of production`() {
        assertEquals(1.0, MadnessMeter(0.0).productionBonus(), 1e-9)
        assertEquals(1.63, MadnessMeter(63.0).productionBonus(), 1e-9)
        assertEquals(2.0, MadnessMeter(100.0).productionBonus(), 1e-9)
    }

    @Test
    fun `omens only come from halfway up`() {
        assertEquals(0.0, MadnessMeter(49.0).omenChance(), 1e-9)
        assertEquals(0.05, MadnessMeter(75.0).omenChance(), 1e-9)
        assertEquals(0.1, MadnessMeter(100.0).omenChance(), 1e-9)
        assertFalse(MadnessMeter(30.0).rollOmen(Random(1)))
    }

    @Test
    fun `the litany of calm lowers madness`() {
        val meter = MadnessMeter(70.0)
        meter.calm(MadnessMeter.CALM_AMOUNT)
        assertEquals(30.0, meter.value, 1e-9)
        meter.calm(MadnessMeter.CALM_AMOUNT)
        assertEquals(0.0, meter.value, 1e-9)
    }
}
