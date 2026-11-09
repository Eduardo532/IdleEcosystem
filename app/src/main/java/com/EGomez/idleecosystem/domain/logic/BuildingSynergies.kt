package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.Upgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import java.math.BigDecimal
import java.math.MathContext

/**
 * Sinergias entre edificios vecinos: una mejora del Grimorio por cada par (Sinapsis →
 * Notas, Notas → Pergaminos... Avatar → Trono). Una vez comprada, cada unidad del
 * edificio anterior suma un 1 % a la producción del siguiente.
 *
 * En un idle, el edificio más nuevo suele dominar y los anteriores dejan de importar.
 * Con las sinergias, los edificios viejos siguen valiendo: 200 Notas triplican los
 * Pergaminos.
 *
 * Aparecen al tener [THRESHOLD] de ambos edificios y cuestan lo mismo que el escalón
 * de 50 de la escalera del Grimorio del edificio potenciado.
 */
object BuildingSynergies {

    const val THRESHOLD = 50

    /** Lo que suma cada unidad del edificio fuente: +1 %. */
    val BONUS_PER_UNIT: BigDecimal = BigDecimal("0.01")

    /** Nombre de cada par, en el orden del catálogo. */
    private val NAMES = listOf(
        "Mente y Mano",
        "Notas al Margen",
        "Inscripciones Rituales",
        "Reliquias de Culto",
        "Jerarquía del Culto",
        "Procesión Subterránea",
        "Cámara de Sacrificios",
        "Sangre en la Piedra",
        "Cimientos Ciclópeos",
        "Geometría No Euclidiana",
        "Mareas del Otro Lado",
        "Pesadillas Vivientes",
        "Soñar con las Estrellas",
        "Cuando las Estrellas Estén en Posición",
        "El Heraldo del Trono"
    )

    fun idFor(source: String, target: String) =
        "u_syn_${source.removePrefix("b_")}_${target.removePrefix("b_")}"

    fun descriptionFor(source: Building, target: Building) =
        "${target.name}: +1% de producción por cada unidad de ${source.name}."

    fun create(buildings: List<Building>): List<Upgrade> =
        buildings.zipWithNext().mapIndexed { index, (source, target) ->
            Upgrade(
                id = idFor(source.id, target.id),
                name = NAMES[index],
                description = descriptionFor(source, target),
                cost = GrimoireLadder.costFor(target, THRESHOLD),
                type = UpgradeType.SYNERGY,
                multiplier = 1.0,
                targetBuildingId = target.id,
                unlockConditionAmount = THRESHOLD,
                sourceBuildingId = source.id
            )
        }

    /** El multiplicador que una sinergia comprada da a su edificio objetivo. */
    fun multiplier(sourceOwned: Int): BigDecimal =
        BigDecimal.ONE.add(BONUS_PER_UNIT.multiply(BigDecimal(sourceOwned)), MathContext.DECIMAL64)
}
