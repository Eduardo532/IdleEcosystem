package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.PactEffects
import com.EGomez.idleecosystem.domain.model.Trial
import kotlin.random.Random

/**
 * El Bestiario (v0.19.8), inspirado en las colecciones de Revolution Idle y en los
 * bestiarios de los juegos de rol. 156 criaturas de los Mitos en 13 familias aparecen en
 * encuentros al azar: catalogarlas las descubre (y repetirlas da Esencia). La Esencia
 * también sale de superar Pruebas y sube de nivel a las criaturas. Cada criatura suma un
 * pequeño bono del tipo de su familia; completar una familia lo duplica.
 */
enum class BeastRarity(val title: String, val weight: Int, val bonusPerLevel: Double, val essenceCost: Int, val duplicateEssence: Int, val color: Long) {
    COMMON("Común", 50, 0.01, 1, 1, 0xFFB0BEC5),
    UNCOMMON("Infrecuente", 28, 0.02, 2, 2, 0xFF81C784),
    RARE("Rara", 14, 0.04, 3, 4, 0xFF64B5F6),
    MYTHIC("Mítica", 6, 0.08, 5, 8, 0xFFBA68C8),
    PRIMORDIAL("Primigenia", 2, 0.15, 8, 16, 0xFFFFB74D)
}

/** Lo que suman las criaturas de una familia. */
enum class BeastKind {
    PRODUCTION, CLICK, SANITY, OFFLINE, ETHER, STARDUST, ESSENCE, ENCOUNTER
}

data class BeastFamily(
    val id: String,
    val title: String,
    val description: String,
    val glyph: String,
    val kind: BeastKind,
    val color: Long
)

data class Beast(
    val id: String,
    val name: String,
    val lore: String,
    val familyId: String,
    val rarity: BeastRarity
)

object Bestiary {
    const val MAX_LEVEL = 10

    /** Un encuentro aparece cada 90 a 240 segundos (menos con los Alados) y dura 45. */
    const val MIN_INTERVAL = 90
    const val MAX_INTERVAL = 240
    const val ENCOUNTER_SECONDS = 45

    /** Una criatura aún sin descubrir pesa el doble al elegir el encuentro. */
    const val UNDISCOVERED_WEIGHT = 2

    /** Completar una familia duplica lo que dan sus criaturas. */
    const val FAMILY_COMPLETE_FACTOR = 2.0

    val families: List<BeastFamily> get() = BeastCatalog.families
    val beasts: List<Beast> get() = BeastCatalog.beasts

    private val byId: Map<String, Beast> by lazy { beasts.associateBy { it.id } }
    private val familyById: Map<String, BeastFamily> by lazy { families.associateBy { it.id } }

    fun beast(id: String): Beast? = byId[id]
    fun family(id: String): BeastFamily? = familyById[id]
    fun familyOf(beast: Beast): BeastFamily = familyById.getValue(beast.familyId)
    fun beastsOf(familyId: String): List<Beast> = beasts.filter { it.familyId == familyId }

    /** Esencia para subir del nivel [level] al siguiente. */
    fun upgradeCost(beast: Beast, level: Int): Int = beast.rarity.essenceCost * level * 2

    /** Esencia por superar una Prueba: más las difíciles, x3 la primera vez. */
    fun trialEssence(trial: Trial, firstTime: Boolean): Int = (3 + 2 * trial.ordinal) * if (firstTime) 3 else 1

    fun isFamilyComplete(familyId: String, levels: Map<String, Int>): Boolean =
        beastsOf(familyId).all { (levels[it.id] ?: 0) > 0 }

    /** Lo que suma cada tipo: 1 + Σ bono·nivel, con las familias completas al doble. */
    fun bonuses(levels: Map<String, Int>): Map<BeastKind, Double> {
        val sums = BeastKind.entries.associateWith { 0.0 }.toMutableMap()
        families.forEach { family ->
            val factor = if (isFamilyComplete(family.id, levels)) FAMILY_COMPLETE_FACTOR else 1.0
            val sum = beastsOf(family.id).sumOf { (levels[it.id] ?: 0) * it.rarity.bonusPerLevel }
            sums[family.kind] = sums.getValue(family.kind) + sum * factor
        }
        return sums.mapValues { 1.0 + it.value }
    }

    /** Los bonos que entran por los efectos de pacto: producción, clic, sin conexión y Cordura. */
    fun effects(levels: Map<String, Int>): PactEffects {
        val b = bonuses(levels)
        return PactEffects(
            production = b.getValue(BeastKind.PRODUCTION),
            click = b.getValue(BeastKind.CLICK),
            offline = b.getValue(BeastKind.OFFLINE),
            sanityGain = b.getValue(BeastKind.SANITY)
        )
    }

    /** Segundos hasta el próximo encuentro: los Alados lo acercan (hasta la mitad). */
    fun nextInterval(encounterBonus: Double, random: Random = Random): Int {
        val base = random.nextInt(MIN_INTERVAL, MAX_INTERVAL + 1)
        return (base / encounterBonus.coerceIn(1.0, 2.0)).toInt().coerceAtLeast(30)
    }

    /** Elige la criatura del encuentro según su rareza; las desconocidas pesan el doble. */
    fun rollEncounter(levels: Map<String, Int>, random: Random = Random): Beast {
        val weights = beasts.map { it.rarity.weight * if ((levels[it.id] ?: 0) > 0) 1 else UNDISCOVERED_WEIGHT }
        var roll = random.nextInt(weights.sum())
        beasts.forEachIndexed { i, beast ->
            roll -= weights[i]
            if (roll < 0) return beast
        }
        return beasts.last()
    }
}
