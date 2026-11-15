package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.domain.model.TrialRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OathsTest {

    @Test
    fun `every trial of both layers can be sworn`() {
        assertEquals(Trial.entries.size + TranscendenceTrial.entries.size, Oaths.all.size)
        assertEquals(Oaths.all.size, Oaths.all.map { it.id }.toSet().size)
        Oaths.all.forEach { assertEquals(it, Oaths.fromId(it.id)) }
        assertNull(Oaths.fromId("nope"))
    }

    @Test
    fun `oaths multiply visions`() {
        assertEquals(1.0, Oaths.visionMultiplier(emptyList()), 1e-12)
        val two = listOf(Oaths.fromId("m_blindness")!!, Oaths.fromId("t_silence")!!)
        assertEquals(1.7, Oaths.visionMultiplier(two), 1e-12)
        assertEquals(1.0 + 10 * 0.2 + 4 * 0.5, Oaths.visionMultiplier(Oaths.all), 1e-12)
    }

    @Test
    fun `sworn madness trials add their rules to the life`() {
        val hunger = Oaths.fromId("m_hunger")!!
        val insomnia = Oaths.fromId("m_insomnia")!!
        val rules = Oaths.rules(Trial.BLINDNESS.rules, listOf(hunger, insomnia))
        assertTrue(rules.noRunes)
        assertTrue(rules.noOffline)
        assertEquals(10.0, rules.buildingCost, 1e-12)
        assertEquals(0.5, rules.production, 1e-12)
        // Sin Prueba elegida, solo las juradas
        assertFalse(Oaths.rules(null, listOf(hunger)).noRunes)
    }

    @Test
    fun `combining rules keeps the strictest of each`() {
        val a = TrialRules(maxBuildingTypes = 5, startingBuildings = mapOf("b_notes" to 10))
        val b = TrialRules(maxBuildingTypes = 3, startingBuildings = mapOf("b_notes" to 4, "b_x" to 1))
        val both = a.combinedWith(b)
        assertEquals(3, both.maxBuildingTypes)
        assertEquals(mapOf("b_notes" to 10, "b_x" to 1), both.startingBuildings)
        assertNull(TrialRules().combinedWith(TrialRules()).maxBuildingTypes)
    }

    @Test
    fun `sworn transcendence trials restrict every cycle`() {
        val silence = Oaths.fromId("t_silence")!!
        assertTrue(Oaths.restricts(TranscendenceTrial.SILENCE, null, listOf(silence)))
        assertTrue(Oaths.restricts(TranscendenceTrial.HUNGER, TranscendenceTrial.HUNGER, emptyList()))
        assertFalse(Oaths.restricts(TranscendenceTrial.HUNGER, null, listOf(silence)))
    }
}
