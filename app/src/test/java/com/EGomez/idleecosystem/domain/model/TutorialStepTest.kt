package com.EGomez.idleecosystem.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialStepTest {

    @Test
    fun `tutorial ids are unique`() {
        val ids = TutorialStep.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `the v0_14 mechanics have their tutorial`() {
        val ids = TutorialStep.entries.map { it.id }.toSet()
        assertTrue(ids.containsAll(setOf("hold_to_click", "next_goal", "achievement_sets", "synergies")))
    }

    @Test
    fun `tutorials describe the current interface`() {
        // El selector de compra incluye HITO y la Ascensión ya no es una pestaña
        assertTrue(TutorialStep.BUY_MULTIPLIERS.message.contains("'HITO'"))
        assertFalse(TutorialStep.PRESTIGE_READY.message.contains("pestaña"))
    }
}
