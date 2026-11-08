package com.EGomez.idleecosystem.data.repository

import com.EGomez.idleecosystem.domain.logic.StatisticsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class SaveRequestTest {

    private fun request(knowledge: Int, replace: Boolean = false) = SaveRequest(
        snapshot = GameSnapshot(
            knowledge = BigDecimal(knowledge),
            sanity = BigDecimal.ZERO,
            echoes = BigDecimal.ZERO,
            tutorialEnabled = true,
            completedTutorialSteps = "",
            buildings = emptyList(),
            upgrades = emptyList(),
            unlockedAchievements = emptyList(),
            prestigeUpgrades = emptyList(),
            statistics = StatisticsManager().getEntityForSave()
        ),
        rituals = emptyList(),
        takenAtMillis = knowledge.toLong(),
        replace = replace
    )

    @Test
    fun `a burst of saves keeps only the latest snapshot`() {
        val merged = SaveRequest.coalesce(listOf(request(1), request(2), request(3)))
        assertEquals(BigDecimal(3), merged.snapshot.knowledge)
        assertEquals(3L, merged.takenAtMillis)
        assertFalse(merged.replace)
    }

    @Test
    fun `a replace followed by normal saves is still a replace`() {
        // Ascender y seguir jugando: la foto nueva debe borrar las filas de la vida anterior
        val merged = SaveRequest.coalesce(listOf(request(1, replace = true), request(2), request(3)))
        assertEquals(BigDecimal(3), merged.snapshot.knowledge)
        assertTrue(merged.replace)
    }

    @Test
    fun `a single request is returned as is`() {
        val single = request(7, replace = true)
        assertEquals(single, SaveRequest.coalesce(listOf(single)))
    }
}
