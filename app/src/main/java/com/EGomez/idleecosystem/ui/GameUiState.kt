package com.EGomez.idleecosystem.ui

import com.EGomez.idleecosystem.core.locale.AppLanguage
import com.EGomez.idleecosystem.core.math.NumberNotation
import com.EGomez.idleecosystem.domain.logic.NextGoal
import com.EGomez.idleecosystem.domain.logic.SystemReveal
import com.EGomez.idleecosystem.domain.logic.AutomationSettings
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.domain.model.TidePhase
import com.EGomez.idleecosystem.domain.logic.RuneBuff
import com.EGomez.idleecosystem.domain.logic.RuneState
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.RandomEvent
import com.EGomez.idleecosystem.domain.model.RuneVariant
import com.EGomez.idleecosystem.domain.model.TutorialStep
import java.math.BigDecimal

data class GameUiState(
    // Economía
    val knowledgeFormatted: String = "0",
    val knowledgePerSecond: String = "0/s",
    val rawKnowledge: BigDecimal = BigDecimal.ZERO,
    val clickMultiplier: String = "1.0",
    val prestigeCurrencyFormatted: String = "0",
    val rawSanity: BigDecimal = BigDecimal.ZERO,
    val potentialPrestigeFormatted: String = "0",
    // Ritmo de la Ascensión (ya formateado)
    val knowledgeThisRunFormatted: String = "0",
    val nextSanityGainFormatted: String = "1",
    val nextSanityAtFormatted: String = "0",
    val doubleSanityGainFormatted: String? = null,
    val doubleSanityAtFormatted: String? = null,
    val globalMultiplier: BigDecimal = BigDecimal.ONE,

    // Factor de costo (Runas)
    val buildingCostMultiplier: Double = 1.0,

    // Ecos
    val echoes: BigDecimal = BigDecimal.ZERO,
    val echoesFormatted: String = "0",

    // Lógica de Juego
    val canPrestige: Boolean = false,
    val buyMultiplier: Int = 1,
    val activeBuffMultiplier: Double = 1.0,
    val buffTimeRemaining: Int = 0,

    // SISTEMA DE RUNAS
    val activeRune: RuneVariant? = null,
    val runeState: RuneState = RuneState.HIDDEN,
    val activeRuneBuffs: List<RuneBuff> = emptyList(),

    // SISTEMA DE TUTORIAL (NUEVO)
    val currentTutorialStep: TutorialStep? = null,
    val isTutorialEnabled: Boolean = true,

    // Eventos y Logros
    val currentEvent: RandomEvent? = null,
    val eventResultText: String? = null,
    val lastUnlockedAchievement: Achievement? = null,
    val achievementsProgress: String = "0 / 0",
    val nextGoal: NextGoal? = null,
    /** Pestañas y paneles descubiertos (los demás se ven como "???"). */
    val revealed: SystemReveal = SystemReveal(),
    /** Ascensiones totales (hitos) y automatizaciones elegidas. */
    val ascensions: Int = 0,
    val automation: AutomationSettings = AutomationSettings(),
    /** El pacto de esta vida y el caos de Azathoth. */
    val activePact: Pact = Pact.NONE,
    val chaosMultiplier: Int = 1,
    /** Reparto de la Atención y puntos disponibles. */
    val attention: Map<String, Int> = emptyMap(),
    val attentionPoints: Int = 0,
    /** Cordura ganada en la última Ascensión automática (aviso breve), o null. */
    val autoAscensionNotice: String? = null,
    /** Pruebas de Locura: la de esta vida, su avance, las superadas y el aviso al superar una. */
    val activeTrial: Trial? = null,
    val trialProgress: Float = 0f,
    val completedTrials: Set<Trial> = emptySet(),
    val deliriumMultiplier: Double = 1.0,
    val earlyPacts: Set<Pact> = emptySet(),
    val trialNotice: Trial? = null,
    /** Trascender (v0.18.0): si se ve, el ciclo, el Éter y las mejoras eternas. */
    val transcendRevealed: Boolean = false,
    val cycleAscensions: Int = 0,
    val etherGain: Int = 0,
    val ether: Int = 0,
    val transcendences: Int = 0,
    val eternalLevels: Map<String, Int> = emptyMap(),
    val transcendNotice: Int? = null,
    /** Consagración (v0.18.1): si se ve y el nivel de cada edificio consagrado. */
    val consecrationRevealed: Boolean = false,
    val consecrations: Map<String, Int> = emptyMap(),
    /** Fase de la Marea (null = oculta) y segundos que le quedan. */
    val tide: TidePhase? = null,
    val tideSecondsLeft: Int = 0,
    /** Medidor de Locura (null = oculto), colapso, edificio poseído y el último presagio. */
    val madness: Double? = null,
    val collapseSeconds: Int = 0,
    val possessedBuilding: String? = null,
    val madnessNotice: MadnessNotice? = null,
    /** Espacios de Runa: cuántos hay, las grabadas y cuántas veces se atrapó cada runa. */
    val runeSlots: Int = 0,
    val engravedRunes: List<String> = emptyList(),
    val runeCatches: Map<String, Int> = emptyMap(),
    val runeMastery: Map<String, Int> = emptyMap(),
    val secretsFound: Int = 0,
    val secretsTotal: Int = 0,
    val setsCompleted: Int = 0,
    val setsTotal: Int = 0,

    // Tienda de Prestigio
    val prestigeUpgrades: List<PrestigeUpgrade> = emptyList(),

    // Diálogos y UI
    val showOfflineDialog: Boolean = false,
    val offlineGains: String = "",
    val offlineTimeFormatted: String = "",
    val showRefundDialog: Boolean = false,
    val refundAmountString: String = "",

    // Sistema de Guardado
    val saveStringExport: String = "",
    val pendingImportString: String = "",
    val importFailed: Boolean = false,
    val exportPreview: SavePreview? = null,
    val importPreview: SavePreview? = null,

    // Configuración
    val isMusicEnabled: Boolean = true,
    val isSfxEnabled: Boolean = true,
    val areParticlesEnabled: Boolean = true,
    val isFloatingTextEnabled: Boolean = true,
    val numberNotation: NumberNotation = NumberNotation.SCIENTIFIC,
    val language: AppLanguage = AppLanguage.SPANISH
)

data class SavePreview(
    val date: String,
    val money: String,
    val achievements: String
)

/** Aviso breve de la Locura: un edificio poseído, Ecos dispersos o el colapso. */
data class MadnessNotice(
    val possessedBuilding: String? = null,
    val echoesLost: String? = null,
    val collapse: Boolean = false
)
