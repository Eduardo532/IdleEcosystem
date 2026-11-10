package com.EGomez.idleecosystem.domain.model

import kotlin.random.Random

/**
 * Mutaciones (v0.18.4): tras la primera Trascendencia, cada Ascensión ofrece tres
 * mutaciones al azar para la vida siguiente; se elige una (o ninguna). Cada una
 * multiplica mucho algo y cobra un precio en otra cosa, así que cambia qué edificios
 * conviene comprar en esa vida.
 */
enum class Mutation(
    val id: String,
    val title: String,
    val description: String,
    val effects: PactEffects
) {
    FEVERISH_NOTES("feverish_notes", "Notas Febriles", "Notas Desordenadas x10, pero Pergaminos Prohibidos x0,5.",
        PactEffects(buildings = mapOf("b_notes" to 10.0, "b_scrolls" to 0.5))),
    LIVING_SCROLLS("living_scrolls", "Pergaminos Vivos", "Pergaminos Prohibidos x10, pero Artefacto Susurrante x0,5.",
        PactEffects(buildings = mapOf("b_scrolls" to 10.0, "b_artifact" to 0.5))),
    SCREAMING_ARTIFACT("screaming_artifact", "Artefacto que Grita", "Artefacto Susurrante x10, pero el clic x0,5.",
        PactEffects(click = 0.5, buildings = mapOf("b_artifact" to 10.0))),
    CULTIST_FERVOUR("cultist_fervour", "Fervor Sectario", "Sectario Novato x10, pero Sacerdote Demente x0,5.",
        PactEffects(buildings = mapOf("b_cultist" to 10.0, "b_priest" to 0.5))),
    PROPHET_PRIESTS("prophet_priests", "Sacerdotes Profetas", "Sacerdote Demente x10 y eventos x1,5 más seguido, pero Santuario Subterráneo x0,5.",
        PactEffects(eventFrequency = 1.5, buildings = mapOf("b_priest" to 10.0, "b_sanctuary" to 0.5))),
    DEEP_SANCTUARY("deep_sanctuary", "Santuario Profundo", "Santuario Subterráneo x10, pero el progreso sin conexión x0,5.",
        PactEffects(offline = 0.5, buildings = mapOf("b_sanctuary" to 10.0))),
    BLEEDING_ALTAR("bleeding_altar", "Altar Sangrante", "Altar de Hueso x10, pero los rituales cuestan x1,5.",
        PactEffects(ritualCost = 1.5, buildings = mapOf("b_altar" to 10.0))),
    SINGING_MONOLITH("singing_monolith", "Monolito Cantor", "Monolito de Obsidiana x10, pero las runas aparecen x0,75.",
        PactEffects(runeFrequency = 0.75, buildings = mapOf("b_monolith" to 10.0))),
    RLYEH_RISES("rlyeh_rises", "R'lyeh Emerge", "Templo de R'lyeh x10, pero Portal Dimensional x0,5.",
        PactEffects(buildings = mapOf("b_temple" to 10.0, "b_gate" to 0.5))),
    OPEN_GATE("open_gate", "Portal Abierto", "Portal Dimensional x10, pero los edificios cuestan x1,25.",
        PactEffects(buildingCost = 1.25, buildings = mapOf("b_gate" to 10.0))),
    SHOGGOTH_BLOOM("shoggoth_bloom", "Eclosión Shoggoth", "Criadero de Shoggoths x10, pero Monolito de Obsidiana x0,5.",
        PactEffects(buildings = mapOf("b_shoggoth" to 10.0, "b_monolith" to 0.5))),
    LUCID_SHARDS("lucid_shards", "Fragmentos Lúcidos", "Fragmento de Sueño x10, pero la Cordura ganada x0,9.",
        PactEffects(sanityGain = 0.9, buildings = mapOf("b_dream" to 10.0))),
    STAR_STORM("star_storm", "Tormenta Estelar", "Alineación Estelar x10, pero Avatar Primigenio x0,5.",
        PactEffects(buildings = mapOf("b_star" to 10.0, "b_oldone" to 0.5))),
    WAKING_AVATAR("waking_avatar", "Avatar Despierto", "Avatar Primigenio x10, pero Trono del Vacío x0,5.",
        PactEffects(buildings = mapOf("b_oldone" to 10.0, "b_void" to 0.5))),
    HUNGRY_VOID("hungry_void", "Vacío Hambriento", "Trono del Vacío x10, pero toda la producción x0,8.",
        PactEffects(production = 0.8, buildings = mapOf("b_void" to 10.0))),
    BURNING_SYNAPSE("burning_synapse", "Sinapsis Ardiente", "El clic x5, pero toda la producción x0,75.",
        PactEffects(click = 5.0, production = 0.75)),
    RESONANT_FLESH("resonant_flesh", "Carne Resonante", "Probabilidad de Eco x2, pero los edificios cuestan x1,1.",
        PactEffects(echoChance = 2.0, buildingCost = 1.1)),
    RUNE_SKIN("rune_skin", "Piel Rúnica", "Las runas aparecen x1,5, pero los rituales duran x0,75.",
        PactEffects(runeFrequency = 1.5, ritualDuration = 0.75)),
    THICK_TIME("thick_time", "Tiempo Espeso", "Progreso sin conexión x3, pero toda la producción x0,9.",
        PactEffects(offline = 3.0, production = 0.9)),
    MAD_GENIUS("mad_genius", "Genio Loco", "Toda la producción x1,5, pero la Cordura ganada x0,8.",
        PactEffects(production = 1.5, sanityGain = 0.8));

    companion object {
        /** Cuántas mutaciones se ofrecen en cada Ascensión. */
        const val OFFER_SIZE = 3

        fun fromId(id: String?): Mutation? = entries.firstOrNull { it.id == id }

        /**
         * Las tres mutaciones que ofrece una Ascensión. Dependen solo de [seed] (las
         * Ascensiones de siempre), así que no cambian por cerrar y abrir el diálogo.
         */
        fun offer(seed: Long): List<Mutation> = entries.shuffled(Random(seed)).take(OFFER_SIZE)
    }
}
