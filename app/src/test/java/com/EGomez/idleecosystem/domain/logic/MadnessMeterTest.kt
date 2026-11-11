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
        // Baja 0,05 + 0,7 % de la Locura por segundo: de 13 a ~11,64 en 10 s
        assertEquals(11.64, meter.value, 0.01)
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
    fun `holding the click settles around sixty percent`() {
        // Mantener pulsado: 8 clics por segundo durante 10 minutos
        val meter = MadnessMeter()
        repeat(600) {
            repeat(8) { meter.add(MadnessMeter.PER_CLICK) }
            meter.decay(1)
        }
        assertTrue("quedó en ${meter.value}", meter.value in 55.0..65.0)
        // Tocar a mano (unos 4 clics por segundo) queda más abajo, sin presagios
        val tapping = MadnessMeter()
        repeat(600) {
            repeat(4) { tapping.add(MadnessMeter.PER_CLICK) }
            tapping.decay(1)
        }
        assertTrue("quedó en ${tapping.value}", tapping.value in 20.0..40.0)
    }

    @Test
    fun `rituals and runes on top of clicking can reach the collapse`() {
        val meter = MadnessMeter(60.0)
        var collapsed = false
        repeat(30) { second ->
            repeat(8) { collapsed = meter.add(MadnessMeter.PER_CLICK) || collapsed }
            if (second % 5 == 0) collapsed = meter.add(MadnessMeter.PER_RITUAL) || collapsed
            if (second == 10) collapsed = meter.add(MadnessMeter.PER_CONVERGENCE) || collapsed
            if (second == 20) collapsed = meter.add(MadnessMeter.perRune(RuneTier.TIER_3_TRACE)) || collapsed
            meter.decay(1)
        }
        assertTrue(collapsed)
    }

    @Test
    fun `without playing hard it calms down in a few minutes`() {
        val meter = MadnessMeter(60.0)
        meter.decay(60)
        assertTrue(meter.value in 30.0..45.0)
        meter.decay(300)
        assertEquals(0.0, meter.value, 1e-9)
        // Bajar de una vez o de a uno da lo mismo
        val stepwise = MadnessMeter(80.0).apply { repeat(90) { decay(1) } }
        val atOnce = MadnessMeter(80.0).apply { decay(90) }
        assertEquals(atOnce.value, stepwise.value, 1e-6)
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
