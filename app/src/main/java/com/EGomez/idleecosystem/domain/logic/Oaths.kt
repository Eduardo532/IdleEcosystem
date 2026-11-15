package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.domain.model.TrialRules

/**
 * Un Juramento (v0.21.1): la restricción de una Prueba de Locura o de una Prueba de
 * Trascendencia que se jura antes de Despertar y queda activa durante todo el Despertar
 * siguiente, en cada vida y cada ciclo. Multiplica las Visiones de ese Despertar.
 */
data class Oath(
    val id: String,
    /** La Prueba de Locura que se jura (sus reglas valen en cada vida). */
    val madness: Trial? = null,
    /** La Prueba de Trascendencia que se jura (su restricción vale en cada ciclo). */
    val transcendence: TranscendenceTrial? = null
) {
    val title: String get() = madness?.title ?: transcendence!!.title
    val rule: String get() = madness?.rule ?: transcendence!!.restriction

    /** Lo que suma a las Visiones: +20 % una de Locura, +50 % una de Trascendencia. */
    val weight: Double get() = if (madness != null) Oaths.MADNESS_WEIGHT else Oaths.TRANSCENDENCE_WEIGHT
}

/**
 * Los Juramentos: las Pruebas de todas las capas combinadas. Se eligen en la pestaña
 * Despertar para el próximo Despertar; la elección se conserva hasta cambiarla.
 */
object Oaths {
    const val MADNESS_WEIGHT = 0.2
    const val TRANSCENDENCE_WEIGHT = 0.5

    val all: List<Oath> =
        Trial.entries.map { Oath("m_" + it.id, madness = it) } +
            TranscendenceTrial.entries.map { Oath("t_" + it.id, transcendence = it) }

    fun fromId(id: String): Oath? = all.firstOrNull { it.id == id }

    /** Visiones x(1 + suma de pesos). */
    fun visionMultiplier(active: Collection<Oath>): Double = 1.0 + active.sumOf { it.weight }

    /** Las reglas de la vida: las de la Prueba de Locura elegida más las juradas. */
    fun rules(trial: TrialRules?, active: Collection<Oath>): TrialRules =
        active.mapNotNull { it.madness?.rules }.fold(trial ?: TrialRules()) { acc, rules -> acc.combinedWith(rules) }

    /** Si [trial] restringe el ciclo: por ser la Prueba en curso o por estar jurada. */
    fun restricts(trial: TranscendenceTrial, current: TranscendenceTrial?, active: Collection<Oath>): Boolean =
        current == trial || active.any { it.transcendence == trial }
}

/** Dos juegos de reglas a la vez: se suma lo más estricto de cada uno. */
fun TrialRules.combinedWith(other: TrialRules): TrialRules = TrialRules(
    noRunes = noRunes || other.noRunes,
    noClickKnowledge = noClickKnowledge || other.noClickKnowledge,
    noGrimoire = noGrimoire || other.noGrimoire,
    buildingCost = buildingCost * other.buildingCost,
    maxBuildingTypes = listOfNotNull(maxBuildingTypes, other.maxBuildingTypes).minOrNull(),
    noRituals = noRituals || other.noRituals,
    noOffline = noOffline || other.noOffline,
    production = production * other.production,
    noAchievementBonus = noAchievementBonus || other.noAchievementBonus,
    delirium = delirium || other.delirium,
    noSanityBonus = noSanityBonus || other.noSanityBonus,
    startingBuildings = (startingBuildings.keys + other.startingBuildings.keys).associateWith {
        maxOf(startingBuildings[it] ?: 0, other.startingBuildings[it] ?: 0)
    }
)
