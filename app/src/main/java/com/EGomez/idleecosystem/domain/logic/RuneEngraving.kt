package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.PactEffects
import com.EGomez.idleecosystem.domain.model.RuneCatalog
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.RuneVariant

/**
 * Espacios de Runa (v0.17.2): una runa que ya atrapaste varias veces se puede grabar
 * en un espacio y da un bono pasivo permanente, propio de cada runa. Los espacios se
 * ganan superando Pruebas de Locura; cambiar lo grabado es gratis.
 *
 * Los bonos usan los mismos campos que los pactos ([PactEffects]) y se combinan con
 * el pacto de la vida actual.
 */
object RuneEngraving {

    /** Cuántas veces hay que atrapar una runa para poder grabarla. */
    fun required(tier: RuneTier): Int = when (tier) {
        RuneTier.TIER_1_SWIPE -> 10
        RuneTier.TIER_2_HOLD -> 5
        RuneTier.TIER_3_TRACE -> 3
    }

    /** El bono pasivo de cada runa grabada. */
    private val PASSIVES: Map<String, PactEffects> = mapOf(
        // Nivel 1: bonos pequeños y variados
        "t1_blood" to PactEffects(production = 1.15),
        "t1_ash" to PactEffects(offline = 1.3),
        "t1_salt" to PactEffects(ritualDuration = 1.2),
        "t1_bone" to PactEffects(buildingCost = 0.95),
        "t1_shadow" to PactEffects(runeFrequency = 1.15),
        "t1_whisper" to PactEffects(click = 1.5),
        "t1_rust" to PactEffects(ritualCost = 0.9),
        "t1_frost" to PactEffects(ritualCooldown = 0.9),
        "t1_vermin" to PactEffects(eventFrequency = 1.2),
        "t1_rift" to PactEffects(offline = 1.5),
        "t1_void_minor" to PactEffects(echoChance = 1.2),
        // Nivel 2
        "t2_chronos" to PactEffects(click = 2.0),
        "t2_gravity" to PactEffects(production = 1.3),
        "t2_barrier" to PactEffects(buildingCost = 0.9),
        "t2_tidal" to PactEffects(eventBuffPower = 1.5),
        "t2_psyche" to PactEffects(sanityGain = 1.1),
        "t2_vision" to PactEffects(eventFrequency = 1.4),
        "t2_entropy" to PactEffects(runeFrequency = 1.3),
        // Nivel 3: los sigilos
        "t3_cthulhu" to PactEffects(production = 1.5),
        "t3_nyarlathotep" to PactEffects(click = 4.0),
        "t3_azathoth" to PactEffects(echoChance = 2.0)
    )

    val ALL: List<RuneVariant> = RuneTier.entries.flatMap { RuneCatalog.getVariantsForTier(it) }

    fun variant(id: String): RuneVariant? = ALL.firstOrNull { it.id == id }

    fun passive(id: String): PactEffects = PASSIVES[id] ?: PactEffects()

    /** El bono de todas las runas grabadas juntas. */
    fun combined(ids: Collection<String>): PactEffects =
        ids.fold(PactEffects()) { acc, id -> acc.combinedWith(passive(id)) }

    fun canEngrave(id: String, catches: Int): Boolean {
        val rune = variant(id) ?: return false
        return catches >= required(rune.tier)
    }
}
