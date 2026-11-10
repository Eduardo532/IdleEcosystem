package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.PactEffects
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.Trial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuneEngravingTest {

    @Test
    fun `every rune has its own passive bonus`() {
        assertEquals(21, RuneEngraving.ALL.size)
        RuneEngraving.ALL.forEach { rune ->
            assertNotEquals("${rune.name} sin bono", PactEffects(), RuneEngraving.passive(rune.id))
        }
    }

    @Test
    fun `rarer runes need fewer catches`() {
        assertTrue(RuneEngraving.required(RuneTier.TIER_1_SWIPE) > RuneEngraving.required(RuneTier.TIER_2_HOLD))
        assertTrue(RuneEngraving.required(RuneTier.TIER_2_HOLD) > RuneEngraving.required(RuneTier.TIER_3_TRACE))
        assertFalse(RuneEngraving.canEngrave("t1_blood", 9))
        assertTrue(RuneEngraving.canEngrave("t1_blood", 10))
        assertTrue(RuneEngraving.canEngrave("t3_azathoth", 3))
        assertFalse(RuneEngraving.canEngrave("unknown", 99))
    }

    @Test
    fun `engraved runes combine with the pact`() {
        val combined = RuneEngraving.combined(listOf("t1_blood", "t2_gravity", "t1_bone"))
        assertEquals(1.15 * 1.3, combined.production, 1e-9)
        assertEquals(0.95, combined.buildingCost, 1e-9)
        val withPact = com.EGomez.idleecosystem.domain.model.Pact.HASTUR.effects.combinedWith(combined)
        assertEquals(2.0 * 1.15 * 1.3, withPact.production, 1e-9)
        assertEquals(0.75, withPact.sanityGain, 1e-9)
    }

    @Test
    fun `four trials give the four rune slots`() {
        assertEquals(4, Trial.combinedReward(Trial.entries).runeSlots)
        assertEquals(1, Trial.BLINDNESS.reward.runeSlots)
    }
}
