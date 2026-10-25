package com.EGomez.idleecosystem.ui

import com.EGomez.idleecosystem.core.math.NumberNotation
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.RandomEvent
import java.math.BigDecimal

data class GameUiState(
    // Recursos Principales
    val rawKnowledge: BigDecimal = BigDecimal.ZERO,
    val knowledgeFormatted: String = "0",
    val knowledgePerSecond: String = "0",

    // NUEVO: Moneda "Ecos" (Echoes)
    val echoes: BigDecimal = BigDecimal.ZERO,
    val echoesFormatted: String = "0",

    // Multiplicadores y Buffs
    val globalMultiplier: BigDecimal = BigDecimal.ONE,
    val activeBuffMultiplier: Double = 1.0,
    val buffTimeRemaining: Int = 0,
    val clickMultiplier: String = "1", // Potencia de click visual

    // Preferencias de UI / Compra
    val buyMultiplier: Int = 1,
    val isMusicEnabled: Boolean = true,
    val isSfxEnabled: Boolean = true,
    val notation: NumberNotation = NumberNotation.SCIENTIFIC,

    // Prestigio / Ascensión
    val prestigeCurrency: BigDecimal = BigDecimal.ZERO, // Cordura (Sanity)
    val prestigeCurrencyFormatted: String = "0",
    val canPrestige: Boolean = false,
    val potentialPrestige: BigDecimal = BigDecimal.ZERO,
    val potentialPrestigeFormatted: String = "0",
    val prestigeRequirementFormatted: String = "1.00e+06",
    val prestigeUpgrades: List<PrestigeUpgrade> = emptyList(),

    // Logros
    val achievementsProgress: String = "0/0",
    val secretsProgress: String? = null,
    val lastUnlockedAchievement: Achievement? = null,

    // Eventos y Diálogos
    val currentEvent: RandomEvent? = null,
    val eventResultText: String? = null,
    // Presagio: evento disponible que el jugador puede abrir cuando quiera
    val pendingEvent: RandomEvent? = null,
    val pendingEventSecondsLeft: Int = 0,
    val showOfflineDialog: Boolean = false,
    val offlineTimeFormatted: String = "",
    val offlineGains: String = "",
    val offlineEchoes: String = "",
    val showRefundDialog: Boolean = false,
    val refundAmountString: String = "",

    // Importar / Exportar
    val saveStringExport: String = "",
    val exportPreview: SavePreview? = null,
    val importPreview: SavePreview? = null,
    val pendingImportString: String = "",
    val importError: String? = null
)

data class SavePreview(
    val date: String,
    val money: String,
    val achievements: String
)