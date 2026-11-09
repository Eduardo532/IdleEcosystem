package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class AchievementSetsTest {

    @Test
    fun `every building has a set and secrets are left out`() {
        val achievements = AchievementManager().availableAchievements
        val sets = AchievementSets.byBuilding(achievements)

        assertEquals(BuildingCatalog.create().map { it.id }.toSet(), sets.keys)
        assertTrue(sets.values.flatten().none { it.isSecret })
        // Los 666 Sectarios son secretos: no se piden para el conjunto
        assertFalse(sets.getValue("b_cultist").any { it.targetValue.compareTo(BigDecimal(666)) == 0 })
    }

    @Test
    fun `a set doubles its building only when complete`() {
        val achievements = AchievementManager().availableAchievements
        val notes = AchievementSets.byBuilding(achievements).getValue("b_notes")

        notes.dropLast(1).forEach { it.isUnlocked = true }
        assertTrue("b_notes" !in AchievementSets.completed(achievements))
        assertEquals(0, BigDecimal.ONE.compareTo(AchievementSets.multiplier("b_notes", AchievementSets.completed(achievements))))

        notes.last().isUnlocked = true
        val completed = AchievementSets.completed(achievements)
        assertEquals(setOf("b_notes"), completed)
        assertEquals(0, BigDecimal(2).compareTo(AchievementSets.multiplier("b_notes", completed)))
        assertEquals(0, BigDecimal.ONE.compareTo(AchievementSets.multiplier("b_scrolls", completed)))
    }

    @Test
    fun `secret achievements do not block a set`() {
        val achievements = AchievementManager().availableAchievements
        AchievementSets.byBuilding(achievements).getValue("b_cultist").forEach { it.isUnlocked = true }
        // "La Marca de la Bestia" (666 Sectarios) sigue bloqueado
        assertFalse(achievements.first { it.id == "sec_1" }.isUnlocked)
        assertTrue("b_cultist" in AchievementSets.completed(achievements))
    }
}
