package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.UpgradeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BuildingSynergiesTest {

    private val buildings = BuildingCatalog.create()
    private val synergies = BuildingSynergies.create(buildings)

    @Test
    fun `one synergy per pair of neighbouring buildings`() {
        assertEquals(buildings.size - 1, synergies.size)
        synergies.zip(buildings.zipWithNext()).forEach { (synergy, pair) ->
            assertEquals(pair.first.id, synergy.sourceBuildingId)
            assertEquals(pair.second.id, synergy.targetBuildingId)
            assertEquals(UpgradeType.SYNERGY, synergy.type)
        }
    }

    @Test
    fun `synergies are part of the grimoire with unique ids`() {
        val all = UpgradeManager().availableUpgrades
        assertTrue(all.count { it.type == UpgradeType.SYNERGY } == synergies.size)
        assertEquals(all.size, all.map { it.id }.toSet().size)
    }

    @Test
    fun `a synergy costs the same as the target building's 50 rung`() {
        synergies.forEach { synergy ->
            val target = buildings.first { it.id == synergy.targetBuildingId }
            assertEquals(0, GrimoireLadder.costFor(target, 50).compareTo(synergy.cost))
        }
    }

    @Test
    fun `it shows up only with 50 of both buildings`() {
        val manager = UpgradeManager()
        val synergy = manager.availableUpgrades.first { it.id == BuildingSynergies.idFor("b_notes", "b_scrolls") }
        val list = BuildingCatalog.create()
        val notes = list.first { it.id == "b_notes" }
        val scrolls = list.first { it.id == "b_scrolls" }

        notes.amountOwned = 50; scrolls.amountOwned = 49
        assertFalse(manager.isUnlocked(synergy, list))
        notes.amountOwned = 49; scrolls.amountOwned = 50
        assertFalse(manager.isUnlocked(synergy, list))
        notes.amountOwned = 50; scrolls.amountOwned = 50
        assertTrue(manager.isUnlocked(synergy, list))
    }

    @Test
    fun `each source unit adds one percent to the target once bought`() {
        val manager = UpgradeManager()
        val list = BuildingCatalog.create()
        list.first { it.id == "b_notes" }.amountOwned = 200

        // Sin comprar no suma nada
        assertEquals(0, BigDecimal.ONE.compareTo(manager.getSynergyMultiplier("b_scrolls", list)))

        manager.availableUpgrades.first { it.id == BuildingSynergies.idFor("b_notes", "b_scrolls") }.isBought = true
        // 200 Notas -> +200 % -> x3
        assertEquals(0, BigDecimal(3).compareTo(manager.getSynergyMultiplier("b_scrolls", list)))
        // Solo afecta al edificio objetivo
        assertEquals(0, BigDecimal.ONE.compareTo(manager.getSynergyMultiplier("b_notes", list)))
    }

    @Test
    fun `synergies do not touch the old building multipliers`() {
        val manager = UpgradeManager()
        manager.availableUpgrades.filter { it.type == UpgradeType.SYNERGY }.forEach { it.isBought = true }
        assertEquals(0, BigDecimal.ONE.compareTo(manager.getSpecificBuildingMultiplier("b_scrolls")))
    }
}
