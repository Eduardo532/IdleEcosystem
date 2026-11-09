package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.UpgradeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AscensionMilestonesTest {

    @Test
    fun `the scale is 1, 3, 5, 10, 25 and 50 ascensions`() {
        assertEquals(listOf(1, 3, 5, 10, 25, 50), AscensionMilestone.entries.map { it.ascensions })
    }

    @Test
    fun `reached and next follow the ascension count`() {
        assertTrue(AscensionMilestone.reached(0).isEmpty())
        assertEquals(AscensionMilestone.STARTING_SYNAPSES, AscensionMilestone.next(0))
        assertEquals(
            setOf(AscensionMilestone.STARTING_SYNAPSES, AscensionMilestone.KEEP_CLICK_UPGRADES, AscensionMilestone.BUILDING_AUTOBUYER),
            AscensionMilestone.reached(7)
        )
        assertEquals(AscensionMilestone.GRIMOIRE_AUTOBUYER, AscensionMilestone.next(7))
        assertNull(AscensionMilestone.next(50))
    }

    @Test
    fun `click upgrades survive from 3 ascensions and synergies from 25`() {
        val upgrades = UpgradeManager().availableUpgrades
        val click = upgrades.first { it.type == UpgradeType.CLICK_MULTIPLIER }
        val synergy = upgrades.first { it.type == UpgradeType.SYNERGY }
        val building = upgrades.first { it.type == UpgradeType.BUILDING_MULTIPLIER }

        assertFalse(AscensionMilestone.keepsOnAscension(click, 2))
        assertTrue(AscensionMilestone.keepsOnAscension(click, 3))
        assertFalse(AscensionMilestone.keepsOnAscension(synergy, 24))
        assertTrue(AscensionMilestone.keepsOnAscension(synergy, 25))
        // Las mejoras de edificio nunca se conservan
        assertFalse(AscensionMilestone.keepsOnAscension(building, 100))
    }

    @Test
    fun `reset for prestige keeps only what the milestones allow`() {
        val manager = UpgradeManager()
        manager.availableUpgrades.forEach { it.isBought = true }
        manager.resetForPrestige(keep = { AscensionMilestone.keepsOnAscension(it, 3) })

        val bought = manager.availableUpgrades.filter { it.isBought }
        assertTrue(bought.isNotEmpty())
        assertTrue(bought.all { it.type == UpgradeType.CLICK_MULTIPLIER })
    }

    @Test
    fun `auto ascension waits until the gain doubles the sanity`() {
        // Sin Cordura: basta con 1 punto
        assertFalse(AutomationSettings.shouldAutoAscend(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO))
        assertTrue(AutomationSettings.shouldAutoAscend(java.math.BigDecimal.ONE, java.math.BigDecimal.ZERO))
        // Con 10 de Cordura hacen falta 10 más
        assertFalse(AutomationSettings.shouldAutoAscend(java.math.BigDecimal(9), java.math.BigDecimal(10)))
        assertTrue(AutomationSettings.shouldAutoAscend(java.math.BigDecimal(10), java.math.BigDecimal(10)))
    }

    @Test
    fun `the budget is a fraction of the knowledge`() {
        assertEquals(0.5, AutomationSettings().budgetFraction, 0.0)
        assertEquals(0.25, AutomationSettings(budgetPercent = 25).budgetFraction, 0.0)
        assertTrue(AutomationSettings.BUDGET_OPTIONS.contains(AutomationSettings.DEFAULT_BUDGET_PERCENT))
    }
}
