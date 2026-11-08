package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.TutorialStep
import java.math.BigDecimal

/**
 * Decide qué tutoriales ya vio el jugador de una partida importada.
 *
 * Los códigos desde la v0.11.0 traen la lista exacta. Los anteriores no la incluyen,
 * así que se deduce del progreso: si la partida ya ascendió, ya vio el tutorial de
 * Ascensión; si cortó runas, ya vio el de runas, etc. Antes, al importar, todos los
 * tutoriales volvían a aparecer.
 */
object TutorialProgress {

    fun stepsSeenIn(data: FullSaveData): Set<String> =
        data.completedTutorialSteps.toSet() + inferFromProgress(data)

    fun inferFromProgress(data: FullSaveData): Set<String> {
        val stats = data.statistics
        val seen = mutableSetOf<TutorialStep>()

        val totalBuildings = data.buildings.sumOf { it.amount }
        val hasProgress = totalBuildings > 0 || data.knowledge > BigDecimal.ZERO ||
            stats.totalClicks > 0 || stats.totalPrestiges > 0

        if (hasProgress) seen += listOf(TutorialStep.WELCOME, TutorialStep.MENU_INTRO)
        if (totalBuildings >= 1) seen += TutorialStep.FIRST_BUILDING
        if (totalBuildings >= 5) seen += TutorialStep.BUY_MULTIPLIERS
        if (data.upgrades.isNotEmpty()) seen += TutorialStep.UPGRADES_TAB
        if (stats.totalPlayTimeSeconds > 5) seen += TutorialStep.TOP_BAR_INFO

        val echoesFound = stats.totalEchoes.toBigDecimalOrNull()?.signum() == 1 || data.echoes.signum() == 1
        if (echoesFound) seen += listOf(TutorialStep.ECHO_FOUND, TutorialStep.RITUALS_TAB)

        if (stats.totalEventsFound > 0) seen += TutorialStep.EVENT_APPEAR

        if (stats.runesSevered > 0) seen += TutorialStep.RUNE_TUTORIAL_SWIPE
        if (stats.runesStabilized > 0) seen += TutorialStep.RUNE_TUTORIAL_HOLD
        if (stats.runesInscribed > 0) seen += TutorialStep.RUNE_TUTORIAL_TRACE
        if (stats.runesSevered + stats.runesStabilized + stats.runesInscribed > 0) {
            seen += TutorialStep.RUNE_INTRO_GENERIC
        }

        val hasAscended = stats.totalPrestiges > 0 || data.sanity.signum() == 1
        if (hasAscended) {
            seen += listOf(TutorialStep.PRESTIGE_READY, TutorialStep.PRESTIGE_MECHANICS)
        }
        if (hasAscended || data.prestigeUpgrades.any { it.level > 0 }) seen += TutorialStep.PRESTIGE_SHOP

        if (stats.totalOfflineSeconds > 0) seen += TutorialStep.OFFLINE_GAINS

        return seen.map { it.id }.toSet()
    }
}
