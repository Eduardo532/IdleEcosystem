package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Upgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType

/**
 * Hitos de Ascensión: al llegar a cierta cantidad de Ascensiones, cada vida nueva
 * empieza con algo, conserva algo o desbloquea una automatización.
 *
 * Sin ellos, la décima Ascensión obliga a repetir a mano el mismo inicio que la
 * primera. Los hitos quitan lo tedioso del reinicio sin quitarle peso: la Cordura,
 * los edificios y el Conocimiento se siguen perdiendo. (Inspirado en los Eternity
 * Milestones de Antimatter Dimensions.)
 */
enum class AscensionMilestone(val ascensions: Int) {
    /** Cada vida empieza con [STARTING_SYNAPSES] Sinapsis. */
    STARTING_SYNAPSES(1),
    /** Al Ascender se conservan las mejoras de clic del Grimorio. */
    KEEP_CLICK_UPGRADES(3),
    /** Compra edificios solo, con la mitad del Conocimiento como tope. */
    BUILDING_AUTOBUYER(5),
    /** Compra mejoras del Grimorio solo, con el mismo tope. */
    GRIMOIRE_AUTOBUYER(10),
    /** Al Ascender se conservan las sinergias compradas. */
    KEEP_SYNERGIES(25),
    /** Un ritual elegido se activa solo cuando está listo y alcanzan los Ecos. */
    AUTO_RITUAL(50);

    fun isReached(totalAscensions: Int) = totalAscensions >= ascensions

    companion object {
        const val STARTING_SYNAPSES = 10

        fun reached(totalAscensions: Int): Set<AscensionMilestone> =
            entries.filter { it.isReached(totalAscensions) }.toSet()

        /** El próximo hito por alcanzar, o null si ya están todos. */
        fun next(totalAscensions: Int): AscensionMilestone? =
            entries.firstOrNull { !it.isReached(totalAscensions) }

        /** Si una mejora comprada sobrevive a la Ascensión número [totalAscensions]. */
        fun keepsOnAscension(upgrade: Upgrade, totalAscensions: Int): Boolean = when (upgrade.type) {
            UpgradeType.CLICK_MULTIPLIER -> KEEP_CLICK_UPGRADES.isReached(totalAscensions)
            UpgradeType.SYNERGY -> KEEP_SYNERGIES.isReached(totalAscensions)
            else -> false
        }
    }
}

/**
 * Qué automatizaciones tiene encendidas el jugador. Solo funcionan si su hito está
 * alcanzado; mientras tanto se ignoran.
 */
data class AutomationSettings(
    val buildings: Boolean = true,
    val grimoire: Boolean = true,
    /** El ritual que se activa solo (null = ninguno). */
    val ritualId: String? = null
) {
    companion object {
        /** Los autocompradores gastan como máximo esta fracción del Conocimiento. */
        const val BUDGET_FRACTION = 0.5
    }
}
