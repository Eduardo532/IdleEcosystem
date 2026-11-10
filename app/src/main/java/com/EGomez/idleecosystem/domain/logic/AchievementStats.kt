package com.EGomez.idleecosystem.domain.logic

import java.math.BigDecimal

/**
 * Las claves que pueden medir los logros de tipo STAT (v0.17).
 *
 * Hay dos clases de claves:
 * - **Vivas:** el motor las calcula cada segundo (Ascensiones, Atención, nodos del Mapa…).
 * - **Contadores:** se guardan en [StatisticsManager.counters] (rituales invocados, vidas
 *   terminadas con cada pacto, hazañas…). Las que empiezan con "run." son de la vida actual.
 */
object AchievementStat {
    // --- Vivas ---
    const val ASCENSIONS = "ascensions"
    const val SANITY = "sanity"
    const val LIFETIME_SANITY = "lifetime_sanity"
    const val SYNERGIES = "synergies"
    const val GRIMOIRE = "grimoire_bought"
    const val SETS = "sets"
    const val MAP_LEVELS = "map_levels"
    const val MAP_NODES = "map_nodes"
    const val MAP_MAX_LEVEL = "map_max_level"
    const val MAP_STAR = "map_star"
    const val ATTENTION_POINTS = "attention_points"
    const val ATTENTION_USED = "attention_used"
    const val ATTENTION_DREAM = "attention_dream"
    const val ATTENTION_VIGIL = "attention_vigil"
    const val ATTENTION_FOCUS = "attention_focus"
    const val ATTENTION_TARGETS = "attention_targets"
    const val PACTS_UNLOCKED = "pacts_unlocked"
    const val PACTS_LIVED = "pacts_lived"
    const val PATH_LEVEL = "path_level"
    const val PATHS_WALKED = "paths_walked"
    const val CLICKS = "clicks"
    const val RUNES = "runes"
    const val RUNES_T1 = "runes_t1"
    const val RUNES_T2 = "runes_t2"
    const val RUNES_T3 = "runes_t3"
    const val EVENTS = "events"
    const val ECHOES_FOUND = "echoes_found"
    const val ECHOES_HELD = "echoes_held"
    const val PLAY_TIME = "play_time"
    const val OFFLINE_TIME = "offline_time"
    const val KPS_MAX = "kps_max"
    const val BUILDINGS_TOTAL = "buildings_total"
    const val BUILDING_TYPES = "building_types"
    const val RUN_KNOWLEDGE = "run_knowledge"
    const val CHAOS = "chaos"
    const val ACTIVE_RITUALS = "active_rituals"
    /** "owned.b_temple": unidades de un edificio. */
    const val OWNED_PREFIX = "owned."

    val LIVE = setOf(
        ASCENSIONS, SANITY, LIFETIME_SANITY, SYNERGIES, GRIMOIRE, SETS, MAP_LEVELS, MAP_NODES,
        MAP_MAX_LEVEL, MAP_STAR, ATTENTION_POINTS, ATTENTION_USED, ATTENTION_DREAM, ATTENTION_VIGIL,
        ATTENTION_FOCUS, ATTENTION_TARGETS, PACTS_UNLOCKED, PACTS_LIVED, PATH_LEVEL, PATHS_WALKED,
        CLICKS, RUNES, RUNES_T1, RUNES_T2, RUNES_T3, EVENTS, ECHOES_FOUND, ECHOES_HELD, PLAY_TIME,
        OFFLINE_TIME, KPS_MAX, BUILDINGS_TOTAL, BUILDING_TYPES, RUN_KNOWLEDGE, CHAOS, ACTIVE_RITUALS
    )

    // --- Contadores guardados ---
    const val RITUALS = "rituals"
    /** "ritual.rit_void_chant": veces que se invocó ese ritual. */
    const val RITUAL_PREFIX = "ritual."
    /** "life.hastur", "life.path.pu_path_hand", "life.cthulhu.pu_path_dream": vidas terminadas. */
    const val LIFE_PREFIX = "life."
    const val MAX_SANITY_GAIN = "max.sanity_gain"
    const val MAX_CONVERGENCE = "max.convergence"
    const val MAX_CONVERGENCE_BUFFS = "max.convergence_buffs"
    const val AUTO_ASCENSIONS = "auto_ascensions"
    const val AUTO_UNITS = "auto_units"
    const val PATHS_ABANDONED = "paths_abandoned"
    /** "path_walked.pu_path_hand": esa Senda se recorrió alguna vez. */
    const val PATH_WALKED_PREFIX = "path_walked."
    /** Hazañas: cosas que pasaron al menos una vez ("feat.silent_life"). */
    const val FEAT_PREFIX = "feat."

    const val FEAT_SILENT_LIFE = "feat.silent_life"
    const val FEAT_FAST_15 = "feat.fast_15"
    const val FEAT_FAST_5 = "feat.fast_5"
    const val FEAT_ASCETIC = "feat.ascetic"
    const val FEAT_BLIND = "feat.blind"
    const val FEAT_BLANK_MIND = "feat.blank_mind"
    const val FEAT_LONG_PATH = "feat.long_path"
    const val FEAT_CHAOS_20 = "feat.chaos_20"
    const val FEAT_CHAOS_1 = "feat.chaos_1"

    val COUNTERS = setOf(
        RITUALS, MAX_SANITY_GAIN, MAX_CONVERGENCE, MAX_CONVERGENCE_BUFFS, AUTO_ASCENSIONS, AUTO_UNITS,
        PATHS_ABANDONED, FEAT_SILENT_LIFE, FEAT_FAST_15, FEAT_FAST_5, FEAT_ASCETIC, FEAT_BLIND,
        FEAT_BLANK_MIND, FEAT_LONG_PATH, FEAT_CHAOS_20, FEAT_CHAOS_1,
        StatisticsManager.RUN_CLICKS, StatisticsManager.RUN_EVENTS, StatisticsManager.RUN_ECHOES,
        StatisticsManager.RUN_RUNES, StatisticsManager.RUN_OFFLINE, StatisticsManager.RUN_RITUALS
    )

    private val PREFIXES = listOf(OWNED_PREFIX, RITUAL_PREFIX, LIFE_PREFIX, PATH_WALKED_PREFIX)

    /** ¿La clave la entiende el motor? (un logro con una clave desconocida nunca se desbloquearía) */
    fun isKnown(key: String): Boolean =
        key in LIVE || key in COUNTERS || PREFIXES.any { key.startsWith(it) && key.length > it.length }

    fun owned(buildingId: String) = OWNED_PREFIX + buildingId
    fun ritual(ritualId: String) = RITUAL_PREFIX + ritualId
    fun life(pactId: String) = LIFE_PREFIX + pactId
    fun lifeOnPath(pathId: String) = LIFE_PREFIX + "path." + pathId
    fun lifeWith(pactId: String, pathId: String) = "$LIFE_PREFIX$pactId.$pathId"
    fun walked(pathId: String) = PATH_WALKED_PREFIX + pathId
}

/**
 * Lo que un logro de tipo STAT necesita saber para comprobarse: los valores de las
 * claves, el pacto de la vida actual y la Senda que se recorre.
 */
data class AchievementContext(
    val stats: Map<String, BigDecimal> = emptyMap(),
    val pactId: String = "none",
    val pathId: String? = null
) {
    fun value(key: String): BigDecimal = stats[key] ?: BigDecimal.ZERO

    companion object {
        val EMPTY = AchievementContext()
    }
}
