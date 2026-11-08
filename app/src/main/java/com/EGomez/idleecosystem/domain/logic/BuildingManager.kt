package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.BuildingEntity
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.BuildingType
import java.math.BigDecimal
import java.math.MathContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildingManager @Inject constructor(
    private val calculator: BuildingCalculator
) {
    val availableBuildings = BuildingCatalog.create()

    fun loadFromSave(savedBuildings: List<BuildingEntity>) {
        availableBuildings.forEach {
            it.amountOwned = 0
            it.isVisible = false
        }

        availableBuildings[0].isVisible = true
        availableBuildings[1].isVisible = true

        savedBuildings.forEach { saved ->
            val building = availableBuildings.find { it.id == saved.buildingId }
            if (building != null) {
                building.amountOwned = saved.amountOwned
                building.isVisible = saved.isUnlocked
            }
        }
    }

    fun getCost(buildingId: String, amountToBuy: Int, currentKnowledge: BigDecimal, discountFactor: BigDecimal = BigDecimal.ONE): BigDecimal {
        val building = availableBuildings.find { it.id == buildingId } ?: return BigDecimal.ZERO
        val baseCost = if (amountToBuy == -1) {
            val effectiveKnowledge = if (discountFactor > BigDecimal.ZERO) currentKnowledge.divide(discountFactor, MathContext.DECIMAL64) else currentKnowledge
            val max = calculator.calculateMaxAffordable(building, effectiveKnowledge)
            if (max > 0) calculator.calculateCostForAmount(building, max) else calculator.calculateNextCost(building)
        } else {
            calculator.calculateCostForAmount(building, amountToBuy)
        }
        return baseCost.multiply(discountFactor)
    }

    fun tryBuyBuilding(buildingId: String, currentKnowledge: BigDecimal, multiplier: Int, discountFactor: BigDecimal = BigDecimal.ONE): Int {
        val building = availableBuildings.find { it.id == buildingId } ?: return 0

        var amountToBuy = multiplier
        if (amountToBuy == -1) {
            val effectiveKnowledge = if (discountFactor > BigDecimal.ZERO) currentKnowledge.divide(discountFactor, MathContext.DECIMAL64) else currentKnowledge
            amountToBuy = calculator.calculateMaxAffordable(building, effectiveKnowledge)
        }

        if (amountToBuy <= 0) return 0

        val cost = getCost(buildingId, amountToBuy, currentKnowledge, discountFactor)

        if (currentKnowledge >= cost) {
            building.amountOwned += amountToBuy
            return amountToBuy
        }
        return 0
    }

    fun getTotalProductionPerSecond(): BigDecimal {
        var total = BigDecimal.ZERO
        availableBuildings.filter { it.type == BuildingType.PASSIVE }.forEach {
            total = total.add(it.totalProduction)
        }
        return total
    }

    fun getClickBuildingBonus(): BigDecimal {
        val clickBuilding = availableBuildings.find { it.type == BuildingType.CLICK } ?: return BigDecimal.ONE
        return BigDecimal.ONE.add(clickBuilding.totalProduction)
    }

    fun checkUnlocks(currentKnowledge: BigDecimal) {
        for (i in 0 until availableBuildings.size - 1) {
            val current = availableBuildings[i]
            val next = availableBuildings[i + 1]

            if (current.isVisible && (current.amountOwned > 0 || currentKnowledge >= current.baseCost.divide(BigDecimal("2")))) {
                if (!next.isVisible && current.amountOwned >= 1) {
                    next.isVisible = true
                }
            }
        }
        availableBuildings[0].isVisible = true
        availableBuildings[1].isVisible = true
    }

    fun resetForPrestige() {
        availableBuildings.forEach {
            it.amountOwned = 0
            it.isVisible = false
        }
        availableBuildings[0].isVisible = true
        availableBuildings[1].isVisible = true
    }
}