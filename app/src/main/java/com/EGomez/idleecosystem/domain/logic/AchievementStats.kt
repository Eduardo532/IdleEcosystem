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
    /** Constelaciones del Mapa de Locura completas (v0.19.2). */
    const val CONSTELLATIONS = "constellations"
    /** Hitos de Trascendencia (v0.19.5): cuántos se alcanzaron y Trascendencias automáticas. */
    const val TRANSCEND_MILESTONES = "transcend_milestones"
    const val AUTO_TRANSCENDS = "auto_transcends"
    const val AUTO_TRANSCEND_WITH_PREFIX = "auto_transcend_with."
    /** Romper el Velo (v0.19.6): el Velo roto (para siempre), tocado, cuán lejos y con qué pacto. */
    const val VEIL_BROKEN = "veil_broken"
    const val VEIL_TOUCHED = "veil_touched"
    const val VEIL_DEPTH = "veil_depth"
    const val FEAT_VEIL_IN_TRIAL = "feat.veil_in_trial"
    const val VEIL_BROKEN_WITH_PREFIX = "veil_broken_with."
    /**
     * Cuando las Estrellas Estén en Posición (v0.19.7): Estrellas, Polvo Estelar (actual y
     * ganado), niveles de las mejoras estelares y compras durante la alineación.
     */
    const val STARS = "stars"
    const val STARDUST = "stardust"
    const val STARDUST_EARNED = "stardust_earned"
    const val STAR_LEVELS = "star_levels"
    const val ALIGNED_PURCHASES = "aligned_purchases"
    const val STAR_UPGRADE_PREFIX = "star_up."
    const val STAR_BUY_WITH_PREFIX = "star_buy_with."
    const val STAR_ALIGNED_PATH_PREFIX = "star_aligned_path."
    /**
     * Bestiario (v0.19.8): nivel de cada criatura (0 sin descubrir), Esencia, encuentros
     * catalogados, criaturas vistas, niveles, familias completas y Primigenias.
     */
    const val BEAST_PREFIX = "beast."
    const val ESSENCE = "essence"
    const val ESSENCE_EARNED = "essence_earned"
    const val ENCOUNTERS = "encounters"
    const val BEASTS_SEEN = "beasts_seen"
    const val BEAST_LEVELS = "beast_levels"
    const val DOMAINS_COMPLETE = "beast_domains"
    const val PRIMORDIALS_SEEN = "beast_primordials"
    const val BEAST_CATCH_WITH_PREFIX = "beast_with."
    const val BEAST_CATCH_PATH_PREFIX = "beast_path."
    const val FEAT_PRIMORDIAL_ALIGNED = "feat.primordial_aligned"
    /**
     * Sellos Antiguos (v0.19.10): la moneda del gachapón. Nunca se reinician. Las compras
     * con Conocimiento, las garantías y la racha diaria tampoco.
     */
    const val SEALS = "seals"
    const val SEALS_EARNED = "seals_earned"
    const val SEALS_BOUGHT = "seals_bought"
    const val CLICK_SEALS = "click_seals"
    const val SUMMONS = "summons"
    const val SINCE_MYTHIC = "summon_since_mythic"
    const val SINCE_PRIMORDIAL = "summon_since_primordial"
    const val SHARDS = "shards"
    const val SHARDS_EARNED = "shards_earned"
    const val SHARD_PICKS = "shard_picks"
    const val DAILY_LAST_DAY = "daily_last_day"
    const val DAILY_STREAK = "daily_streak"
    const val DAILY_BEST_STREAK = "daily_best_streak"
    const val BESTIARY_V2 = "bestiary_v2"
    /** Laboratorio de Miskatonic (v0.19.9): niveles, experimentos al máximo y Éter invertido. */
    const val LAB_PREFIX = "lab."
    const val LAB_LEVELS = "lab_levels"
    const val LAB_MAXED = "lab_maxed"
    const val LAB_ETHER_SPENT = "lab_ether_spent"
    const val LAB_BUY_WITH_PREFIX = "lab_with."
    const val LAB_BUY_PATH_PREFIX = "lab_path."
    const val FEAT_LAB_EXACT = "feat.lab_exact"
    /**
     * El Sueño Profundo (v0.20.0): si el próximo ciclo será un Sueño, si este lo es, el
     * mejor Conocimiento de una vida en este Sueño y en todos, Puntos de Sueño (los que hay
     * y los ganados en total), Sueños completados y nodos del árbol.
     */
    const val DREAM_NEXT = "dream_next"
    const val DREAM_ACTIVE = "dream_active"
    const val DREAM_CYCLE_BEST = "dream_cycle_best"
    const val DREAM_BEST = "dream_best"
    const val DREAM_POINTS = "dream_points"
    const val DREAM_POINTS_EARNED = "dream_points_earned"
    const val DREAMS_COMPLETED = "dreams_completed"
    const val DREAM_WAKES = "dream_wakes"
    const val DREAM_RESPECS = "dream_respecs"
    const val DREAM_NODE_PREFIX = "dream."
    const val DREAM_LEVELS = "dream_levels"
    const val DREAM_NODES = "dream_nodes"
    const val DREAM_END_WITH_PREFIX = "dream_with."
    const val DREAM_END_PATH_PREFIX = "dream_path."
    /**
     * La Cadena de Invocación (v0.20.1): unidades y compras de cada eslabón en el ciclo,
     * el Poder del ciclo, el mejor Poder de siempre, compras totales, el eslabón más alto
     * comprado y con qué pacto o Senda se compró.
     */
    const val CHAIN_AMOUNT_PREFIX = "chain.a."
    const val CHAIN_BOUGHT_PREFIX = "chain.b."
    const val CHAIN_CYCLE_PREFIX = "chain."
    const val CHAIN_POWER = "chain_power"
    const val CHAIN_POWER_BEST = "chain_power_best"
    const val CHAIN_BOUGHT = "chain_bought"
    const val CHAIN_TOP_TIER = "chain_top_tier"
    const val CHAIN_FULL = "chain_full"
    const val CHAIN_DREAM = "chain_dream"
    const val CHAIN_BUY_WITH_PREFIX = "chain_with."
    const val CHAIN_BUY_PATH_PREFIX = "chain_path."
    /**
     * El Letargo (v0.20.2): si está activo, el Sopor (segundos) de la vida, Ascensiones en
     * Letargo, Ascensiones extra, el mejor Sopor, cuántas veces se perdió y con qué pacto o
     * Senda se Ascendió en Letargo.
     */
    const val LETHARGY_ACTIVE = "lethargy_active"
    const val LETHARGY_SECONDS = "lethargy_seconds"
    const val LETHARGY_ASCENSIONS = "lethargy_ascensions"
    const val LETHARGY_EXTRA = "lethargy_extra"
    const val LETHARGY_BEST_MINUTES = "lethargy_best_minutes"
    const val LETHARGY_LOST = "lethargy_lost"
    const val LETHARGY_WITH_PREFIX = "lethargy_with."
    const val LETHARGY_PATH_PREFIX = "lethargy_path."
    /**
     * Las Pruebas de Trascendencia (v0.20.3): la elegida para el próximo ciclo y la activa
     * (índice + 1), la mejor vida del ciclo, niveles superados de cada una (ttrial.<id>),
     * el total, cuántas se abandonaron y con qué pacto o Senda se superaron.
     */
    const val TTRIAL_NEXT = "ttrial_next"
    const val TTRIAL_ACTIVE = "ttrial_active"
    const val TTRIAL_CYCLE_BEST = "ttrial_cycle_best"
    const val TTRIAL_PREFIX = "ttrial."
    const val TTRIAL_COMPLETIONS = "ttrial_completions"
    const val TTRIAL_ABANDONED = "ttrial_abandoned"
    const val TTRIAL_WITH_PREFIX = "ttrial_with."
    const val TTRIAL_PATH_PREFIX = "ttrial_path."
    /** Niveles de la Senda que vinieron del ciclo anterior (Senda Eterna): no se reembolsan. */
    const val CYCLE_PATH_KEPT = "cycle_path_kept"
    /** Trascender con exactamente las 40 Ascensiones de El Atajo. */
    const val FEAT_SHORTCUT = "feat.shortcut"
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
        ATTENTION_FOCUS, ATTENTION_TARGETS, PACTS_UNLOCKED, PACTS_LIVED, PATH_LEVEL, PATHS_WALKED, CONSTELLATIONS,
        CLICKS, RUNES, RUNES_T1, RUNES_T2, RUNES_T3, EVENTS, ECHOES_FOUND, ECHOES_HELD, PLAY_TIME,
        OFFLINE_TIME, KPS_MAX, BUILDINGS_TOTAL, BUILDING_TYPES, RUN_KNOWLEDGE, CHAOS, ACTIVE_RITUALS,
        TRIALS_COMPLETED, RUNE_SLOTS, ENGRAVED, ENGRAVED_T3, RUNE_KINDS, ENGRAVED_KINDS, MADNESS, TIDE_PHASE, ETERNAL_LEVELS,
        CONSECRATION_LEVELS, CONSECRATED_BUILDINGS, CONSECRATED_MAX,
        MASTERY_LEVELS, MASTERY_KINDS, MASTERY_MAXED, MUTATIONS_SEEN, TRANSCEND_MILESTONES, VEIL_DEPTH, STAR_LEVELS,
        BEASTS_SEEN, BEAST_LEVELS, DOMAINS_COMPLETE, PRIMORDIALS_SEEN, LAB_LEVELS, LAB_MAXED, DREAM_LEVELS, DREAM_NODES, TTRIAL_COMPLETIONS
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

    // --- Mutaciones (v0.18.4) ---
    /** "run.mutation.feverish_notes": la mutación de la vida actual. */
    const val ACTIVE_MUTATION_PREFIX = "run.mutation."
    /** "mutation_seen.feverish_notes": esa mutación se eligió alguna vez. */
    const val MUTATION_SEEN_PREFIX = "mutation_seen."
    /** "ui.seen.grimoire": el jugador ya abrió esa pestaña (v0.19.0; no es un logro). */
    const val SEEN_TAB_PREFIX = "ui.seen."
    /** Se marcó lo ya visible al pasar a la interfaz por capas: no todo aparece como nuevo. */
    const val SEEN_TABS_INIT = "ui.seen_init"
    /** "mutation_with.cthulhu.rlyeh_rises": vida terminada con ese pacto y esa mutación. */
    const val MUTATION_WITH_PREFIX = "mutation_with."
    /** "mutation_path.pu_path_hand.burning_synapse": vida terminada con esa Senda y esa mutación. */
    const val MUTATION_PATH_PREFIX = "mutation_path."
    /** "mutation_trial.bound_hands.feverish_notes": Prueba superada con esa mutación. */
    const val MUTATION_TRIAL_PREFIX = "mutation_trial."
    const val MUTATION_LIVES = "mutation_lives"
    const val MUTATIONS_SEEN = "mutations_seen"

    fun mutationWith(pactId: String, mutationId: String) = "$MUTATION_WITH_PREFIX$pactId.$mutationId"
    fun mutationPath(pathId: String, mutationId: String) = "$MUTATION_PATH_PREFIX$pathId.$mutationId"
    fun mutationTrial(trialId: String, mutationId: String) = "$MUTATION_TRIAL_PREFIX$trialId.$mutationId"

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
    fun autoTranscendWith(pactId: String) = AUTO_TRANSCEND_WITH_PREFIX + pactId
    fun veilBrokenWith(pactId: String) = VEIL_BROKEN_WITH_PREFIX + pactId
    fun starUpgrade(id: String) = STAR_UPGRADE_PREFIX + id
    fun starBuyWith(pactId: String) = STAR_BUY_WITH_PREFIX + pactId
    fun starAlignedPath(pathId: String) = STAR_ALIGNED_PATH_PREFIX + pathId
    fun beast(id: String) = BEAST_PREFIX + id
    fun beastCatchWith(pactId: String) = BEAST_CATCH_WITH_PREFIX + pactId
    fun beastCatchPath(pathId: String) = BEAST_CATCH_PATH_PREFIX + pathId
    fun lab(id: String) = LAB_PREFIX + id
    fun labBuyWith(pactId: String) = LAB_BUY_WITH_PREFIX + pactId
    fun labBuyPath(pathId: String) = LAB_BUY_PATH_PREFIX + pathId
    fun dreamNode(id: String) = DREAM_NODE_PREFIX + id
    fun dreamEndWith(pactId: String) = DREAM_END_WITH_PREFIX + pactId
    fun dreamEndPath(pathId: String) = DREAM_END_PATH_PREFIX + pathId
    fun chainAmount(tier: Int) = CHAIN_AMOUNT_PREFIX + tier
    fun chainBought(tier: Int) = CHAIN_BOUGHT_PREFIX + tier
    fun chainBuyWith(pactId: String) = CHAIN_BUY_WITH_PREFIX + pactId
    fun chainBuyPath(pathId: String) = CHAIN_BUY_PATH_PREFIX + pathId
    fun lethargyWith(pactId: String) = LETHARGY_WITH_PREFIX + pactId
    fun lethargyPath(pathId: String) = LETHARGY_PATH_PREFIX + pathId
    fun ttrial(id: String) = TTRIAL_PREFIX + id
    fun ttrialWith(pactId: String) = TTRIAL_WITH_PREFIX + pactId
    fun ttrialPath(pathId: String) = TTRIAL_PATH_PREFIX + pathId
    fun engraved(runeId: String) = ENGRAVE_PREFIX + runeId

    fun trial(trialId: String) = TRIAL_PREFIX + trialId
    fun trialWith(trialId: String, pactId: String) = "$TRIAL_WITH_PREFIX$trialId.$pactId"

    val COUNTERS = setOf(
        RITUALS, MAX_SANITY_GAIN, MAX_CONVERGENCE, MAX_CONVERGENCE_BUFFS, AUTO_ASCENSIONS, AUTO_UNITS,
        PATHS_ABANDONED, FEAT_SILENT_LIFE, FEAT_FAST_15, FEAT_FAST_5, FEAT_ASCETIC, FEAT_BLIND,
        FEAT_BLANK_MIND, FEAT_LONG_PATH, FEAT_CHAOS_20, FEAT_CHAOS_1, FEAT_TRIAL_FAST, TRIALS_STARTED,
        MAX_MADNESS, MAX_MADNESS_STREAK, MADNESS_COLLAPSES, MADNESS_OMENS, RUN_COLLAPSES, FEAT_STEEL_NERVES, FEAT_EDGE,
        TIDES_PASSED, RUN_TIDES, TIDE_HIGH_RUNES, TIDE_DEAD_RITUALS, TIDE_HIGH_CONVERGENCE, FEAT_STILL_LOW_TIDE, FEAT_TSUNAMI,
        ETHER, ETHER_EARNED, TRANSCENDENCES, MAX_ETHER_GAIN, CYCLE_SANITY, CYCLE_START, ASCENSIONS_PAST, FEAT_EXACT_THRESHOLD, MUTATION_LIVES,
        AUTO_TRANSCENDS, FEAT_SHORTCUT, VEIL_BROKEN, VEIL_TOUCHED, FEAT_VEIL_IN_TRIAL,
        STARS, STARDUST_EARNED, ALIGNED_PURCHASES, ESSENCE_EARNED, ENCOUNTERS, FEAT_PRIMORDIAL_ALIGNED,
        SEALS_EARNED, SEALS_BOUGHT, CLICK_SEALS, SUMMONS, SHARDS_EARNED, SHARD_PICKS, DAILY_BEST_STREAK,
        LAB_ETHER_SPENT, FEAT_LAB_EXACT,
        DREAM_NEXT, DREAM_ACTIVE, DREAM_CYCLE_BEST, DREAM_BEST, DREAM_POINTS, DREAM_POINTS_EARNED, DREAMS_COMPLETED, DREAM_WAKES, DREAM_RESPECS,
        CHAIN_POWER, CHAIN_POWER_BEST, CHAIN_BOUGHT, CHAIN_TOP_TIER, CHAIN_FULL, CHAIN_DREAM,
        LETHARGY_ACTIVE, LETHARGY_SECONDS, LETHARGY_ASCENSIONS, LETHARGY_EXTRA, LETHARGY_BEST_MINUTES, LETHARGY_LOST,
        TTRIAL_NEXT, TTRIAL_ACTIVE, TTRIAL_CYCLE_BEST, TTRIAL_ABANDONED,
        StatisticsManager.RUN_CLICKS, StatisticsManager.RUN_EVENTS, StatisticsManager.RUN_ECHOES,
        StatisticsManager.RUN_RUNES, StatisticsManager.RUN_OFFLINE, StatisticsManager.RUN_RITUALS
    )

    private val PREFIXES = listOf(OWNED_PREFIX, RITUAL_PREFIX, LIFE_PREFIX, PATH_WALKED_PREFIX, TRIAL_PREFIX, TRIAL_WITH_PREFIX,
        RUNE_CATCH_PREFIX, ENGRAVE_PREFIX, ENGRAVED_EVER_PREFIX, ETERNAL_PREFIX, TRANSCEND_WITH_PREFIX, TRANSCEND_PATH_PREFIX, AUTO_TRANSCEND_WITH_PREFIX, VEIL_BROKEN_WITH_PREFIX,
        STAR_BUY_WITH_PREFIX, STAR_ALIGNED_PATH_PREFIX, STAR_UPGRADE_PREFIX,
        BEAST_PREFIX, BEAST_CATCH_WITH_PREFIX, BEAST_CATCH_PATH_PREFIX,
        LAB_PREFIX, LAB_BUY_WITH_PREFIX, LAB_BUY_PATH_PREFIX,
        DREAM_NODE_PREFIX, DREAM_END_WITH_PREFIX, DREAM_END_PATH_PREFIX,
        CHAIN_CYCLE_PREFIX, CHAIN_BUY_WITH_PREFIX, CHAIN_BUY_PATH_PREFIX,
        LETHARGY_WITH_PREFIX, LETHARGY_PATH_PREFIX,
        TTRIAL_PREFIX, TTRIAL_WITH_PREFIX, TTRIAL_PATH_PREFIX,
        CONSECRATED_PREFIX, CONSECRATED_WITH_PREFIX, MASTERY_PREFIX,
        MUTATION_SEEN_PREFIX, MUTATION_WITH_PREFIX, MUTATION_PATH_PREFIX, MUTATION_TRIAL_PREFIX)

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
