package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildingMilestonesTest {

    private val achievements = AchievementManager().availableAchievements
    private val milestones = BuildingMilestones.build(listOf("b_notes", "b_synapse"), achievements)

    @Test
    fun `milestones join the grimoire ladder and the building achievements`() {
        // Logros de Notas: 10, 50, 100, 300, 600. Escalera: 25, 50, 100.
        assertEquals(listOf(10, 25, 50, 100, 300, 600), milestones["b_notes"])
        // La Sinapsis también tiene los logros de clic (1, 100, 500)
        assertTrue(milestones.getValue("b_synapse").containsAll(listOf(1, 25, 50, 100, 500)))
    }

    @Test
    fun `secret achievements are not milestones`() {
        // "La Marca de la Bestia" pide 666 Sectarios y es secreto
        val cultist = BuildingMilestones.build(listOf("b_cultist"), achievements).getValue("b_cultist")
        assertTrue(666 !in cultist)
    }

    @Test
    fun `next is the first milestone above what you own`() {
        val notes = milestones.getValue("b_notes")
        assertEquals(10, BuildingMilestones.next(notes, 0))
        assertEquals(25, BuildingMilestones.next(notes, 10))
        assertEquals(25, BuildingMilestones.next(notes, 19))
        assertEquals(6, BuildingMilestones.amountToNext(notes, 19))
    }

    @Test
    fun `past the last milestone every hundred counts`() {
        val notes = milestones.getValue("b_notes")
        assertEquals(700, BuildingMilestones.next(notes, 600))
        assertEquals(700, BuildingMilestones.next(notes, 642))
        assertEquals(600, BuildingMilestones.previous(notes, 642))
        assertEquals(700, BuildingMilestones.previous(notes, 750))
        assertEquals(800, BuildingMilestones.next(notes, 750))
    }

    @Test
    fun `previous is the last milestone reached`() {
        val notes = milestones.getValue("b_notes")
        assertEquals(0, BuildingMilestones.previous(notes, 9))
        assertEquals(10, BuildingMilestones.previous(notes, 10))
        assertEquals(25, BuildingMilestones.previous(notes, 49))
    }

    @Test
    fun `there is always at least one unit to buy`() {
        val notes = milestones.getValue("b_notes")
        (0..1_000).forEach { owned -> assertTrue(BuildingMilestones.amountToNext(notes, owned) >= 1) }
        assertEquals(10, BuildingMilestones.amountToNext(emptyList(), 90))
    }
}
