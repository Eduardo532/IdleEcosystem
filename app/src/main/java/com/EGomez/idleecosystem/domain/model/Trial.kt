package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal

/**
 * Las reglas alteradas de una Prueba de Locura mientras está activa. Los valores
 * neutros (1.0, false, null) no cambian nada.
 */
data class TrialRules(
    /** No aparecen runas. */
    val noRunes: Boolean = false,
    /** El clic no produce Conocimiento (los Ecos sí se siguen encontrando). */
    val noClickKnowledge: Boolean = false,
    /** No se pueden comprar mejoras del Grimorio. */
    val noGrimoire: Boolean = false,
    /** Multiplica el costo de los edificios. */
    val buildingCost: Double = 1.0,
    /** Cuántos tipos de edificio (sin contar la Sinapsis) se pueden tener; null = sin límite. */
    val maxBuildingTypes: Int? = null,
    /** No hay rituales ni Ecos. */
    val noRituals: Boolean = false,
    /** No hay progreso sin conexión. */
    val noOffline: Boolean = false,
    /** Multiplica toda la producción. */
    val production: Double = 1.0,
    /** Los logros (y sus conjuntos) no dan bono. */
    val noAchievementBonus: Boolean = false,
    /** La producción salta al azar cada 30 s (ver [Trial.DELIRIUM_STEPS]). */
    val delirium: Boolean = false,
    /** La Cordura Residual y el Mapa de Locura no dan bono. */
    val noSanityBonus: Boolean = false
)

/**
 * La recompensa permanente de superar una Prueba. Se multiplica con las demás; los
 * pactos se pueden elegir aunque falten Ascensiones.
 */
data class TrialReward(
    val production: Double = 1.0,
    val click: Double = 1.0,
    val offline: Double = 1.0,
    val runeFrequency: Double = 1.0,
    val buildingCost: Double = 1.0,
    val ritualCooldown: Double = 1.0,
    val sanityGain: Double = 1.0,
    val attentionPoints: Int = 0,
    /** Un dios que se adelanta: se puede elegir sin las Ascensiones que pide. */
    val pact: Pact? = null
)

/**
 * Pruebas de Locura (v0.17.1): vidas voluntarias con reglas alteradas y una meta de
 * Conocimiento generado. Se eligen al Ascender, junto con el pacto. Al llegar a la
 * meta la Prueba se supera, sus reglas se levantan y la recompensa queda para siempre.
 * Se puede abandonar en cualquier momento, sin recompensa.
 */
enum class Trial(
    val id: String,
    val title: String,
    val rule: String,
    val unlockAscensions: Int,
    val goal: BigDecimal,
    val rules: TrialRules,
    val reward: TrialReward
) {
    BLINDNESS(
        "blindness", "Ceguera",
        "No aparece ninguna runa.",
        3, BigDecimal("1e9"),
        TrialRules(noRunes = true),
        TrialReward(runeFrequency = 1.25)
    ),
    BOUND_HANDS(
        "bound_hands", "Manos Atadas",
        "El clic no produce Conocimiento.",
        3, BigDecimal("1e9"),
        TrialRules(noClickKnowledge = true),
        TrialReward(click = 3.0, pact = Pact.NYARLATHOTEP)
    ),
    HUNGER(
        "hunger", "Hambre",
        "Los edificios cuestan x10.",
        3, BigDecimal("1e9"),
        TrialRules(buildingCost = 10.0),
        TrialReward(buildingCost = 0.9, pact = Pact.SHUB_NIGGURATH)
    ),
    BURNED_GRIMOIRE(
        "burned_grimoire", "Grimorio Quemado",
        "No se pueden comprar mejoras del Grimorio.",
        5, BigDecimal("1e9"),
        TrialRules(noGrimoire = true),
        TrialReward(production = 1.2, pact = Pact.YOG_SOTHOTH)
    ),
    SILENCE(
        "silence", "Silencio",
        "No hay rituales ni Ecos.",
        5, BigDecimal("1e9"),
        TrialRules(noRituals = true),
        TrialReward(ritualCooldown = 0.75)
    ),
    INSOMNIA(
        "insomnia", "Insomnio",
        "No hay progreso sin conexión y la producción es la mitad.",
        5, BigDecimal("1e9"),
        TrialRules(noOffline = true, production = 0.5),
        TrialReward(offline = 2.0)
    ),
    MONOMANIA(
        "monomania", "Monomanía",
        "Solo puedes tener 3 tipos de edificio, además de la Sinapsis.",
        10, BigDecimal("1e8"),
        TrialRules(maxBuildingTypes = 3),
        TrialReward(attentionPoints = 1)
    ),
    AMNESIA(
        "amnesia", "Amnesia",
        "Los logros y sus conjuntos no dan bono.",
        10, BigDecimal("1e12"),
        TrialRules(noAchievementBonus = true),
        TrialReward(sanityGain = 1.1)
    ),
    DELIRIUM(
        "delirium", "Delirio",
        "La producción salta al azar entre x0,1 y x3 cada 30 segundos.",
        20, BigDecimal("1e12"),
        TrialRules(delirium = true),
        TrialReward(production = 1.1, pact = Pact.AZATHOTH)
    ),
    MORTALITY(
        "mortality", "Mortalidad",
        "La Cordura Residual y el Mapa de Locura no dan bono.",
        20, BigDecimal("1e12"),
        TrialRules(noSanityBonus = true),
        TrialReward(production = 1.25)
    );

    fun isUnlocked(ascensions: Int): Boolean = ascensions >= unlockAscensions

    companion object {
        /** Las Pruebas se descubren con esta cantidad de Ascensiones. */
        const val REVEAL_AT = 3
        /** Delirio: la producción cambia cada tantos segundos, entre estos valores. */
        const val DELIRIUM_PERIOD_SECONDS = 30
        val DELIRIUM_STEPS = listOf(0.1, 0.25, 0.5, 1.0, 1.5, 2.0, 3.0)

        fun fromId(id: String?): Trial? = entries.firstOrNull { it.id == id }

        /** La recompensa combinada de las Pruebas superadas. */
        fun combinedReward(completed: Collection<Trial>): TrialReward = completed.fold(TrialReward()) { acc, t ->
            val r = t.reward
            TrialReward(
                production = acc.production * r.production,
                click = acc.click * r.click,
                offline = acc.offline * r.offline,
                runeFrequency = acc.runeFrequency * r.runeFrequency,
                buildingCost = acc.buildingCost * r.buildingCost,
                ritualCooldown = acc.ritualCooldown * r.ritualCooldown,
                sanityGain = acc.sanityGain * r.sanityGain,
                attentionPoints = acc.attentionPoints + r.attentionPoints
            )
        }

        /** Los dioses que las Pruebas superadas adelantan. */
        fun earlyPacts(completed: Collection<Trial>): Set<Pact> = completed.mapNotNull { it.reward.pact }.toSet()
    }
}
