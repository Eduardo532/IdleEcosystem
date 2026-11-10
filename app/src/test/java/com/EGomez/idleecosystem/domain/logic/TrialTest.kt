package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.domain.model.TrialReward
import com.EGomez.idleecosystem.domain.model.TrialRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrialTest {

    @Test
    fun `there are several trials, each with a rule, a goal and a reward`() {
        assertTrue(Trial.entries.size >= 8)
        assertEquals(Trial.entries.size, Trial.entries.map { it.id }.toSet().size)
        Trial.entries.forEach { trial ->
            assertNotEquals("${trial.title} no cambia ninguna regla", TrialRules(), trial.rules)
            assertNotEquals("${trial.title} no da recompensa", TrialReward(), trial.reward)
            assertTrue(trial.goal.signum() > 0)
            assertTrue(trial.unlockAscensions >= Trial.REVEAL_AT)
        }
    }

    @Test
    fun `rewards combine and gods come early`() {
        val reward = Trial.combinedReward(listOf(Trial.BOUND_HANDS, Trial.HUNGER, Trial.MONOMANIA, Trial.BURNED_GRIMOIRE, Trial.MORTALITY))
        assertEquals(3.0, reward.click, 1e-9)
        assertEquals(0.9, reward.buildingCost, 1e-9)
        assertEquals(1, reward.attentionPoints)
        assertEquals(1.2 * 1.25, reward.production, 1e-9)
        assertEquals(TrialReward(), Trial.combinedReward(emptyList()))

        val early = Trial.earlyPacts(listOf(Trial.BOUND_HANDS, Trial.DELIRIUM))
        assertEquals(setOf(Pact.NYARLATHOTEP, Pact.AZATHOTH), early)
        assertFalse(Pact.AZATHOTH.isUnlocked(3))
        assertTrue(Pact.AZATHOTH.isUnlocked(3, early))
        assertTrue(Pact.AZATHOTH in Pact.available(3, early))
    }

    @Test
    fun `the four outer gods can be brought early by a trial`() {
        val early = Trial.earlyPacts(Trial.entries)
        assertEquals(Pact.entries.filter { it.tier == com.EGomez.idleecosystem.domain.model.PactTier.OUTER_GOD }.toSet(), early)
    }

    @Test
    fun `harder trials open later`() {
        assertEquals(listOf(Trial.BLINDNESS, Trial.BOUND_HANDS, Trial.HUNGER), Trial.entries.filter { it.isUnlocked(3) })
        assertTrue(Trial.entries.all { it.isUnlocked(20) })
    }
}
