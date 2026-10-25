package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.BuildingEntity
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.BuildingType
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

data class BuildingPurchase(val amount: Int, val cost: BigDecimal)

@Singleton
class BuildingManager @Inject constructor(
    private val calculator: BuildingCalculator
) {
    val availableBuildings = mutableListOf(
        // Tier 1
        // CAMBIO: amountOwned inicializado en 0 (antes 1)
        Building("b_synapse", "Sinapsis Arcana", "Genera conocimiento básico.", BigDecimal("10"), BigDecimal("1"), BigDecimal("1.15"), tier = 1, type = BuildingType.CLICK, isVisible = true, amountOwned = 0),
        // Tier 2
        Building("b_notes", "Notas Desordenadas", "Garabatos frenéticos.", BigDecimal("15"), BigDecimal("0.5"), BigDecimal("1.15"), tier = 2, isVisible = true),
        // Tier 3
        Building("b_scrolls", "Pergaminos Prohibidos", "Textos antiguos.", BigDecimal("100"), BigDecimal("4"), BigDecimal("1.15"), tier = 3),
        // Tier 4
        Building("b_artifact", "Artefacto Susurrante", "Vibra con energía.", BigDecimal("1100"), BigDecimal("12"), BigDecimal("1.15"), tier = 4),
        // Tier 5
        Building("b_cultist", "Sectario Novato", "Mano de obra barata.", BigDecimal("12000"), BigDecimal("45"), BigDecimal("1.15"), tier = 5),
        // Tier 6
        Building("b_priest", "Sacerdote Demente", "Dirige los rituales.", BigDecimal("130000"), BigDecimal("150"), BigDecimal("1.15"), tier = 6),
        // Tier 7
        Building("b_sanctuary", "Santuario Subterráneo", "Un lugar seguro.", BigDecimal("1400000"), BigDecimal("600"), BigDecimal("1.15"), tier = 7),
        // Tier 8
        Building("b_altar", "Altar de Hueso", "Sacrificios necesarios.", BigDecimal("20000000"), BigDecimal("2500"), BigDecimal("1.15"), tier = 8),
        // Tier 9
        Building("b_monolith", "Monolito de Obsidiana", "Canaliza el vacío.", BigDecimal("330000000"), BigDecimal("12000"), BigDecimal("1.15"), tier = 9),
        // Tier 10
        Building("b_temple", "Templo de R'lyeh", "La ciudad sumergida.", BigDecimal("5100000000"), BigDecimal("65000"), BigDecimal("1.15"), tier = 10),
        // Tier 11
        Building("b_gate", "Portal Dimensional", "Puerta a otros mundos.", BigDecimal("75000000000"), BigDecimal("400000"), BigDecimal("1.15"), tier = 11),
        // Tier 12
        Building("b_shoggoth", "Criadero de Shoggoths", "Masa biológica útil.", BigDecimal("1000000000000"), BigDecimal("2500000"), BigDecimal("1.15"), tier = 12),
        // Tier 13
        Building("b_dream", "Fragmento de Sueño", "Materializado.", BigDecimal("14000000000000"), BigDecimal("15000000"), BigDecimal("1.15"), tier = 13),
        // Tier 14
        Building("b_star", "Alineación Estelar", "El cosmos se ordena.", BigDecimal("170000000000000"), BigDecimal("100000000"), BigDecimal("1.15"), tier = 14),
        // Tier 15
        Building("b_oldone", "Avatar Primigenio", "Un dios camina.", BigDecimal("2100000000000000"), BigDecimal("850000000"), BigDecimal("1.15"), tier = 15),
        // Tier 16
        Building("b_void", "Trono del Vacío", "El fin de todo.", BigDecimal("26000000000000000"), BigDecimal("10000000000"), BigDecimal("1.15"), tier = 16)
    )

    fun loadFromSave(savedBuildings: List<BuildingEntity>) {
        // Reset inicial
        availableBuildings.forEach {
            it.amountOwned = 0
            it.isVisible = false
        }

        // Restaurar visibilidad básica
        availableBuildings[0].isVisible = true
        availableBuildings[1].isVisible = true

        savedBuildings.forEach { saved ->
            val building = availableBuildings.find { it.id == saved.buildingId }
            if (building != null) {
                building.amountOwned = saved.amountOwned
                building.isVisible = saved.isUnlocked
            }
        }

        // CAMBIO: Eliminado el bloque de seguridad que forzaba 1 sinapsis si tenías 0.
        // Ahora respeta exactamente lo que viene del guardado (o 0 si es nueva partida).
    }

    fun getCost(buildingId: String, amountToBuy: Int, currentKnowledge: BigDecimal): BigDecimal {
        val building = availableBuildings.find { it.id == buildingId } ?: return BigDecimal.ZERO
        if (amountToBuy == -1) {
            val max = calculator.calculateMaxAffordable(building, currentKnowledge)
            return if (max > 0) calculator.calculateCostForAmount(building, max) else calculator.calculateNextCost(building)
        }
        return calculator.calculateCostForAmount(building, amountToBuy)
    }

    /**
     * Intenta comprar edificios. Devuelve la cantidad comprada y el costo exacto pagado,
     * o null si no alcanza el dinero. El costo se calcula una sola vez, aquí, para que
     * lo que se descuenta sea exactamente lo que se compró.
     */
    fun tryBuyBuilding(buildingId: String, currentKnowledge: BigDecimal, multiplier: Int): BuildingPurchase? {
        val building = availableBuildings.find { it.id == buildingId } ?: return null

        val amountToBuy = if (multiplier == -1) {
            calculator.calculateMaxAffordable(building, currentKnowledge)
        } else {
            multiplier
        }

        if (amountToBuy <= 0) return null

        val cost = calculator.calculateCostForAmount(building, amountToBuy)
        if (currentKnowledge >= cost) {
            building.amountOwned += amountToBuy
            return BuildingPurchase(amountToBuy, cost)
        }
        return null
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

    /** Revela el siguiente edificio cuando posees al menos uno del anterior. Devuelve true si algo cambió. */
    fun checkUnlocks(): Boolean {
        var changed = false
        for (i in 0 until availableBuildings.size - 1) {
            val current = availableBuildings[i]
            val next = availableBuildings[i + 1]
            if (current.isVisible && current.amountOwned >= 1 && !next.isVisible) {
                next.isVisible = true
                changed = true
            }
        }
        availableBuildings[0].isVisible = true
        availableBuildings[1].isVisible = true
        return changed
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