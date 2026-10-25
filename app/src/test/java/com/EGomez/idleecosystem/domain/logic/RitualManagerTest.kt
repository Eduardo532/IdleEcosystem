package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.RitualState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class RitualManagerTest {

    private fun ritual(manager: RitualManager, id: String) = manager.rituals.value.first { it.id == id }

    @Test
    fun `activar requiere ecos suficientes`() {
        val manager = RitualManager()
        assertFalse(manager.tryActivateRitual("rit_blood_frenzy", BigDecimal(4)))
        assertTrue(manager.tryActivateRitual("rit_blood_frenzy", BigDecimal(5)))
        assertEquals(5.0, manager.getActiveClickMultiplier(), 0.0)
    }

    @Test
    fun `un ritual pasa por activo, enfriamiento y listo`() {
        val manager = RitualManager()
        manager.tryActivateRitual("rit_blood_frenzy", BigDecimal(100)) // 15s activo, 60s enfriamiento
        manager.tick(14)
        assertEquals(RitualState.ACTIVE, ritual(manager, "rit_blood_frenzy").state)
        manager.tick(1)
        assertEquals(RitualState.COOLDOWN, ritual(manager, "rit_blood_frenzy").state)
        assertEquals(60, ritual(manager, "rit_blood_frenzy").timeRemaining)
        manager.tick(60)
        assertEquals(RitualState.READY, ritual(manager, "rit_blood_frenzy").state)
    }

    @Test
    fun `un salto grande de tiempo atraviesa varias fases`() {
        val manager = RitualManager()
        manager.tryActivateRitual("rit_void_chant", BigDecimal(100)) // 30s activo, 120s enfriamiento
        manager.tick(40)
        val r = ritual(manager, "rit_void_chant")
        assertEquals(RitualState.COOLDOWN, r.state)
        assertEquals(110, r.timeRemaining)
    }

    @Test
    fun `restaurar descuenta el tiempo que la app estuvo cerrada`() {
        val original = RitualManager()
        original.tryActivateRitual("rit_temporal_rift", BigDecimal(100)) // 5s activo, 300s enfriamiento
        val saved = original.snapshot()

        val restored = RitualManager()
        restored.restore(saved, elapsedSeconds = 105)
        val r = ritual(restored, "rit_temporal_rift")
        assertEquals(RitualState.COOLDOWN, r.state)
        assertEquals(200, r.timeRemaining)
        assertEquals(1.0, restored.getActiveProductionMultiplier(), 0.0)
    }

    @Test
    fun `reiniciar deja todos los rituales listos`() {
        val manager = RitualManager()
        manager.tryActivateRitual("rit_blood_frenzy", BigDecimal(100))
        manager.reset()
        assertTrue(manager.rituals.value.all { it.state == RitualState.READY })
    }
}
