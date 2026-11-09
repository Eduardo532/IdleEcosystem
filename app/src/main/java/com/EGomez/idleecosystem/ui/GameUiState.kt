package com.EGomez.idleecosystem.ui

import com.EGomez.idleecosystem.core.locale.AppLanguage
import com.EGomez.idleecosystem.core.math.NumberNotation
import com.EGomez.idleecosystem.domain.logic.NextGoal
import com.EGomez.idleecosystem.domain.logic.SystemReveal
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