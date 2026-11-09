package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.UpgradeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext

class GrimoireLadderTest {

    private val buildings = BuildingCatalog.create()
    private val ladder = GrimoireLadder.create(buildings)

    @Test
    fun `every building gets three rungs with unique ids`() {
        assertEquals(buildings.size * 3, ladder.size)
        val all = UpgradeManager().availableUpgrades
        assertEquals("Hay ids repetidos en el Grimorio", all.size, all.map { it.id }.toSet().size)
    }

    @Test
    fun `the original upgrades keep their ids`() {
        // Las partidas guardadas y los códigos exportados los referencian por id
        val ids = UpgradeManager().availableUpgrades.map { it.id }
        listOf("u_click_1", "u_notes_1", "u_notes_2", "u_global_1", "u_global_2", "u_void_1")
            .forEach { assertTrue("Falta $it", it in ids) }
    }

    @Test
    fun `each rung costs more than the previous one of the same building`() {
        ladder.groupBy { it.targetBuildingId }.forEach { (buildingId, rungs) ->
            rungs.sortedBy { it.unlockConditionAmount }.zipWithNext().forEach { (lower, higher) ->
                assertTrue("$buildingId: ${higher.name} no cuesta más que ${lower.name}", higher.cost > lower.cost)
            }
        }
    }

    @Test
    fun `a rung costs about ten times the building number it requires`() {
        buildings.forEach { building ->
            GrimoireLadder.RUNGS.forEach { rung ->
                val upgrade = ladder.first { it.id == GrimoireLadder.idFor(building.id, rung.threshold) }
                val buildingPrice = building.baseCost.multiply(building.growthFactor.pow(rung.threshold, MathContext.DECIMAL64))
                val ratio = upgrade.cost.divide(buildingPrice, MathContext.DECIMAL64).toDouble()
                assertTrue("${upgrade.name}: x$ratio", ratio in 9.5..10.5)
            }
        }
    }

    @Test
    fun `the synapse ladder boosts clicks`() {
        val synapseRungs = ladder.filter { it.targetBuildingId == "b_synapse" }
        assertTrue(synapseRungs.all { it.type == UpgradeType.CLICK_MULTIPLIER })
    }

    @Test
    fun `a rung shows up only with enough buildings`() {
        val manager = UpgradeManager()
        val rung = manager.availableUpgrades.first { it.id == GrimoireLadder.idFor("b_notes", 25) }
        val notes = buildings.first { it.id == "b_notes" }

        notes.amountOwned = 24
        assertFalse(manager.isUnlocked(rung, buildings))
        notes.amountOwned = 25
        assertTrue(manager.isUnlocked(rung, buildings))

        // Las mejoras originales no tienen requisito
        val original = manager.availableUpgrades.first { it.id == "u_notes_1" }
        notes.amountOwned = 0
        assertTrue(manager.isUnlocked(original, buildings))
    }

    @Test
    fun `a bought rung stays visible after selling below the threshold`() {
        val manager = UpgradeManager()
        val rung = manager.availableUpgrades.first { it.id == GrimoireLadder.idFor("b_notes", 50) }
        rung.isBought = true
        buildings.first { it.id == "b_notes" }.amountOwned = 0
        assertTrue(manager.isUnlocked(rung, buildings))
    }

    @Test
    fun `prices are rounded to two significant digits`() {
        ladder.forEach { assertTrue(it.cost.precision() <= 2 || it.cost.stripTrailingZeros().precision() <= 2) }
        assertEquals(0, BigDecimal("4900").compareTo(ladder.first { it.id == "u_notes_at_25" }.cost))
    }
}
