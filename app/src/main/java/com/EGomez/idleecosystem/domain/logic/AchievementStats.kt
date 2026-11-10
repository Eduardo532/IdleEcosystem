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
    const val TRIALS_COMPLETED = "trials_completed"
    const val RUNE_SLOTS = "rune_slots"
    const val ENGRAVED = "engraved"
    const val ENGRAVED_T3 = "engraved_t3"
    const val RUNE_KINDS = "rune_kinds"
    const val ENGRAVED_KINDS = "engraved_kinds"
    /** Medidor de Locura (v0.17.3): el valor actual (se guarda como contador). */
    const val MADNESS = "madness"
    /** Fase de la Marea actual: 0 alta, 1 baja, 2 muerta (v0.17.4). */
    const val TIDE_PHASE = "tide_phase"
    /** "owned.b_temple": unidades de un edificio. */
    const val OWNED_PREFIX = "owned."

    val LIVE = setOf(
        ASCENSIONS, SANITY, LIFETIME_SANITY, SYNERGIES, GRIMOIRE, SETS, MAP_LEVELS, MAP_NODES,
        MAP_MAX_LEVEL, MAP_STAR, ATTENTION_POINTS, ATTENTION_USED, ATTENTION_DREAM, ATTENTION_VIGIL,
        ATTENTION_FOCUS, ATTENTION_TARGETS, PACTS_UNLOCKED, PACTS_LIVED, PATH_LEVEL, PATHS_WALKED,
        CLICKS, RUNES, RUNES_T1, RUNES_T2, RUNES_T3, EVENTS, ECHOES_FOUND, ECHOES_HELD, PLAY_TIME,
        OFFLINE_TIME, KPS_MAX, BUILDINGS_TOTAL, BUILDING_TYPES, RUN_KNOWLEDGE, CHAOS, ACTIVE_RITUALS,
        TRIALS_COMPLETED, RUNE_SLOTS, ENGRAVED, ENGRAVED_T3, RUNE_KINDS, ENGRAVED_KINDS, MADNESS, TIDE_PHASE, ETERNAL_LEVELS,
        CONSECRATION_LEVELS, CONSECRATED_BUILDINGS, CONSECRATED_MAX,
        MASTERY_LEVELS, MASTERY_KINDS, MASTERY_MAXED
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
    const val FEAT_TRIAL_FAST = "feat.trial_fast"

    // --- Pruebas de Locura (v0.17.1) ---
    /** "trial.blindness": la Prueba se superó. */
    const val TRIAL_PREFIX = "trial."
    /** "trialwith.hunger.abhoth": se superó con ese pacto. */
    const val TRIAL_WITH_PREFIX = "trialwith."
    /** "run.trial.blindness": la Prueba de la vida actual (se borra al Ascender). */
    const val ACTIVE_TRIAL_PREFIX = "run.trial."
    const val TRIALS_STARTED = "trials_started"

    // --- Espacios de Runa (v0.17.2) ---
    /** "rune.t1_blood": veces que se atrapó esa runa. */
    const val RUNE_CATCH_PREFIX = "rune."
    /** "engrave.t1_blood": la runa está grabada (el valor ordena los espacios). */
    const val ENGRAVE_PREFIX = "engrave."
    /** "engraved_ever.t1_blood": la runa se grabó alguna vez. */
    const val ENGRAVED_EVER_PREFIX = "engraved_ever."

    fun runeCatches(runeId: String) = RUNE_CATCH_PREFIX + runeId

    // --- Maestría de runas (v0.18.3) ---
    /** "mastery.t1_blood": capturas de esa runa desde siempre (nunca se reinician). */
    const val MASTERY_PREFIX = "mastery."
    const val MASTERY_LEVELS = "mastery_levels"
    const val MASTERY_KINDS = "mastery_kinds"
    const val MASTERY_MAXED = "mastery_maxed"

    fun mastery(runeId: String) = MASTERY_PREFIX + runeId

    // --- Medidor de Locura (v0.17.3) ---
    const val MAX_MADNESS = "max.madness"
    const val MAX_MADNESS_STREAK = "max.madness_streak"
    const val MADNESS_COLLAPSES = "madness_collapses"
    const val MADNESS_OMENS = "madness_omens"
    const val RUN_COLLAPSES = "run.collapses"
    const val FEAT_STEEL_NERVES = "feat.steel_nerves"
    const val FEAT_EDGE = "feat.edge99"

    // --- Fases de la Marea (v0.17.4) ---
    const val TIDES_PASSED = "tides_passed"
    const val RUN_TIDES = "run.tides"
    const val TIDE_HIGH_RUNES = "tide.high_runes"
    const val TIDE_DEAD_RITUALS = "tide.dead_rituals"
    const val TIDE_HIGH_CONVERGENCE = "tide.high_convergence"
    const val FEAT_STILL_LOW_TIDE = "feat.still_low_tide"
    const val FEAT_TSUNAMI = "feat.tsunami"

    // --- Trascender (v0.18.0) ---
    /** Éter disponible, Éter ganado en total y veces que se Trascendió. */
    const val ETHER = "ether"
    const val ETHER_EARNED = "ether_earned"
    const val TRANSCENDENCES = "transcendences"
    const val MAX_ETHER_GAIN = "max.ether_gain"
    /** Cordura ganada en el ciclo y Ascensiones con las que empezó (se reinician al Trascender). */
    const val CYCLE_SANITY = "cycle.sanity"
    const val CYCLE_START = "cycle.start"
    /** Ascensiones de los ciclos anteriores (para que los logros de Ascensiones no retrocedan). */
    const val ASCENSIONS_PAST = "ascensions_past"
    const val ETERNAL_PREFIX = "eternal."
    const val TRANSCEND_WITH_PREFIX = "transcend_with."
    const val TRANSCEND_PATH_PREFIX = "transcend_path."
    const val FEAT_EXACT_THRESHOLD = "feat.exact_threshold"
    /** Niveles de mejoras eternas en total (viva). */
    const val ETERNAL_LEVELS = "eternal_levels"

    fun eternal(id: String) = ETERNAL_PREFIX + id

    // --- Consagrar edificios (v0.18.1) ---
    /** "consecrated.b_temple": nivel de consagración del edificio (eterno). */
    const val CONSECRATED_PREFIX = "consecrated."
    /** "consecrated_with.cthulhu.b_temple": se consagró ese edificio con ese pacto. */
    const val CONSECRATED_WITH_PREFIX = "consecrated_with."
    /** Vivas: niveles en total, edificios consagrados y edificios al máximo. */
    const val CONSECRATION_LEVELS = "consecration_levels"
    const val CONSECRATED_BUILDINGS = "consecrated_buildings"
    const val CONSECRATED_MAX = "consecrated_max"

    fun consecrated(buildingId: String) = CONSECRATED_PREFIX + buildingId
    fun consecratedWith(pactId: String, buildingId: String) = "$CONSECRATED_WITH_PREFIX$pactId.$buildingId"
    fun transcendWith(pactId: String) = TRANSCEND_WITH_PREFIX + pactId
    fun transcendPath(pathId: String) = TRANSCEND_PATH_PREFIX + pathId
    fun engraved(runeId: String) = ENGRAVE_PREFIX + runeId

    fun trial(trialId: String) = TRIAL_PREFIX + trialId
    fun trialWith(trialId: String, pactId: String) = "$TRIAL_WITH_PREFIX$trialId.$pactId"

    val COUNTERS = setOf(
        RITUALS, MAX_SANITY_GAIN, MAX_CONVERGENCE, MAX_CONVERGENCE_BUFFS, AUTO_ASCENSIONS, AUTO_UNITS,
        PATHS_ABANDONED, FEAT_SILENT_LIFE, FEAT_FAST_15, FEAT_FAST_5, FEAT_ASCETIC, FEAT_BLIND,
        FEAT_BLANK_MIND, FEAT_LONG_PATH, FEAT_CHAOS_20, FEAT_CHAOS_1, FEAT_TRIAL_FAST, TRIALS_STARTED,
        MAX_MADNESS, MAX_MADNESS_STREAK, MADNESS_COLLAPSES, MADNESS_OMENS, RUN_COLLAPSES, FEAT_STEEL_NERVES, FEAT_EDGE,
        TIDES_PASSED, RUN_TIDES, TIDE_HIGH_RUNES, TIDE_DEAD_RITUALS, TIDE_HIGH_CONVERGENCE, FEAT_STILL_LOW_TIDE, FEAT_TSUNAMI,
        ETHER, ETHER_EARNED, TRANSCENDENCES, MAX_ETHER_GAIN, CYCLE_SANITY, CYCLE_START, ASCENSIONS_PAST, FEAT_EXACT_THRESHOLD,
        StatisticsManager.RUN_CLICKS, StatisticsManager.RUN_EVENTS, StatisticsManager.RUN_ECHOES,
        StatisticsManager.RUN_RUNES, StatisticsManager.RUN_OFFLINE, StatisticsManager.RUN_RITUALS
    )

    private val PREFIXES = listOf(OWNED_PREFIX, RITUAL_PREFIX, LIFE_PREFIX, PATH_WALKED_PREFIX, TRIAL_PREFIX, TRIAL_WITH_PREFIX,
        RUNE_CATCH_PREFIX, ENGRAVE_PREFIX, ENGRAVED_EVER_PREFIX, ETERNAL_PREFIX, TRANSCEND_WITH_PREFIX, TRANSCEND_PATH_PREFIX,
        CONSECRATED_PREFIX, CONSECRATED_WITH_PREFIX, MASTERY_PREFIX)

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
