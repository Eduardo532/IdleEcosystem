package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.RitualState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class RitualManagerTest {

    private fun RitualManager.ritual(id: String) = rituals.value.first { it.id == id }

    @Test
    fun `activation requires enough echoes`() {
        val manager = RitualManager()
        assertFalse(manager.tryActivateRitual("rit_blood_frenzy", BigDecimal(4)))
        assertTrue(manager.tryActivateRitual("rit_blood_frenzy", BigDecimal(5)))
        assertEquals(listOf(5.0), manager.getActiveClickMultipliers())
    }

    @Test
    fun `ritual goes through active, cooldown and ready`() {
        val manager = RitualManager()
        manager.tryActivateRitual("rit_blood_frenzy", BigDecimal(100)) // 15s active, 60s cooldown
        manager.tick(14)
        assertEquals(RitualState.ACTIVE, manager.ritual("rit_blood_frenzy").state)
        manager.tick(1)
        assertEquals(RitualState.COOLDOWN, manager.ritual("rit_blood_frenzy").state)
        assertEquals(60L, manager.ritual("rit_blood_frenzy").timeRemaining)
        manager.tick(60)
        assertEquals(RitualState.READY, manager.ritual("rit_blood_frenzy").state)
    }

    @Test
    fun `a large time jump crosses several phases`() {
        val manager = RitualManager()
        manager.tryActivateRitual("rit_void_chant", BigDecimal(100)) // 30s active, 120s cooldown
        manager.tick(40)
        val ritual = manager.ritual("rit_void_chant")
        assertEquals(RitualState.COOLDOWN, ritual.state)
        assertEquals(110L, ritual.timeRemaining)
    }

    @Test
    fun `restore subtracts the time the app was closed`() {
        val original = RitualManager()
        original.tryActivateRitual("rit_temporal_rift", BigDecimal(100)) // 15s active, 300s cooldown
        val saved = original.snapshot()

        val restored = RitualManager()
        restored.restore(saved, elapsedSeconds = 105)
        val ritual = restored.ritual("rit_temporal_rift")
        assertEquals(RitualState.COOLDOWN, ritual.state)
        assertEquals(210L, ritual.timeRemaining)
        assertTrue(restored.getActiveProductionMultipliers().isEmpty())
    }

    @Test
    fun `reset leaves every ritual ready`() {
        val manager = RitualManager()
        manager.tryActivateRitual("rit_blood_frenzy", BigDecimal(100))
        manager.reset()
        assertTrue(manager.rituals.value.all { it.state == RitualState.READY })
    }

    @Test
    fun `a pricier production ritual is never worth less per echo`() {
        // Antes la Grieta Temporal (50 Ecos) daba 95 s de producción: menos por Eco
        // que el Cántico del Vacío (15 Ecos, 60 s)
        val byCost = RitualManager().rituals.value
            .filter { it.productionSeconds > 0 }
            .sortedBy { it.baseCost }
        byCost.zipWithNext().forEach { (cheaper, pricier) ->
            val cheaperValue = cheaper.productionSeconds.toDouble() / cheaper.baseCost.toDouble()
            val pricierValue = pricier.productionSeconds.toDouble() / pricier.baseCost.toDouble()
            assertTrue(
                "${pricier.name} rinde $pricierValue s/Eco, menos que ${cheaper.name} ($cheaperValue)",
                pricierValue >= cheaperValue
            )
        }
    }

    @Test
    fun `instant knowledge rituals are not production buffs`() {
        val manager = RitualManager()
        manager.tryActivateRitual("rit_knowledge_offering", BigDecimal(100))
        assertTrue(manager.getActiveProductionMultipliers().isEmpty())
        assertEquals(120L, manager.ritual("rit_knowledge_offering").productionSeconds)
    }
}
