package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class UpgradeManagerTest {

    private val upgradeManager = UpgradeManager()
    private val buildings = BuildingManager(BuildingCalculator()).availableBuildings

    private fun upgrade(id: String) = upgradeManager.availableUpgrades.first { it.id == id }

    @Test
    fun `mejora de edificio aparece al poseer ese edificio`() {
        val notesUpgrade = upgrade("u_notes_1")
        assertFalse(upgradeManager.isUpgradeVisible(notesUpgrade, buildings, BigDecimal.ZERO))
        buildings.first { it.id == "b_notes" }.amountOwned = 1
        assertTrue(upgradeManager.isUpgradeVisible(notesUpgrade, buildings, BigDecimal.ZERO))
    }

    @Test
    fun `mejora global aparece al generar el 10 por ciento de su costo`() {
        val global = upgrade("u_global_1") // cuesta 10M
        assertFalse(upgradeManager.isUpgradeVisible(global, buildings, BigDecimal("999999")))
        assertTrue(upgradeManager.isUpgradeVisible(global, buildings, BigDecimal("1000000")))
    }

    @Test
    fun `una mejora comprada siempre es visible`() {
        val global = upgrade("u_global_2")
        global.isBought = true
        assertTrue(upgradeManager.isUpgradeVisible(global, buildings, BigDecimal.ZERO))
    }
}
