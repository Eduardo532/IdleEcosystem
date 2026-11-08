package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.BuildingType
import com.EGomez.idleecosystem.domain.model.Upgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * La escalera del Grimorio: tres mejoras por edificio que aparecen al tener 25, 50
 * y 100 unidades, y duplican su producción.
 *
 * Antes había 22 mejoras para 16 edificios y casi todos tenían una sola: pasada la
 * mitad de la partida, el Grimorio se quedaba sin metas. Los costos no se escriben
 * a mano, salen de una fórmula ligada al propio edificio:
 *
 *     costo = costo base x crecimiento^umbral x [COST_FACTOR]
 *
 * es decir, [COST_FACTOR] veces lo que cuesta el edificio número "umbral". Así cada
 * escalón cuesta lo mismo en relación con su edificio, en el nivel 1 o en el 16.
 */
object GrimoireLadder {

    data class Rung(val threshold: Int, val suffix: String, val description: String)

    val RUNGS = listOf(
        Rung(25, "Devoción", "Con 25 en tu poder, cada uno rinde el doble."),
        Rung(50, "Obsesión", "Con 50 en tu poder, cada uno rinde el doble."),
        Rung(100, "Trascendencia", "Con 100 en tu poder, cada uno rinde el doble.")
    )

    const val MULTIPLIER = 2.0
    val COST_FACTOR: BigDecimal = BigDecimal.TEN

    fun idFor(buildingId: String, threshold: Int) = "u_${buildingId.removePrefix("b_")}_at_$threshold"

    fun costFor(building: Building, threshold: Int): BigDecimal =
        building.baseCost
            .multiply(building.growthFactor.pow(threshold, MathContext.DECIMAL64))
            .multiply(COST_FACTOR)
            // Dos cifras significativas: precios legibles (4.900 en vez de 4.937,13)
            .round(MathContext(2, RoundingMode.HALF_UP))

    fun create(buildings: List<Building>): List<Upgrade> = buildings.flatMap { building ->
        RUNGS.map { rung ->
            Upgrade(
                id = idFor(building.id, rung.threshold),
                name = "${building.name}: ${rung.suffix}",
                description = rung.description,
                cost = costFor(building, rung.threshold),
                // La Sinapsis es el edificio de clic: su escalera potencia el clic
                type = if (building.type == BuildingType.CLICK) UpgradeType.CLICK_MULTIPLIER else UpgradeType.BUILDING_MULTIPLIER,
                multiplier = MULTIPLIER,
                targetBuildingId = building.id,
                unlockConditionAmount = rung.threshold
            )
        }
    }
}
