package com.EGomez.idleecosystem.ui

import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.RandomEvent
import java.math.BigDecimal

data class GameUiState(
    // Economía
    val knowledgeFormatted: String = "0",
    val knowledgePerSecond: String = "0/s",
    val rawKnowledge: BigDecimal = BigDecimal.ZERO,
    val clickMultiplier: String = "1.0",
    val prestigeCurrencyFormatted: String = "0",
    val potentialPrestigeFormatted: String = "0",
    val globalMultiplier: BigDecimal = BigDecimal.ONE,

    // Ecos
    val echoes: BigDecimal = BigDecimal.ZERO,
    val echoesFormatted: String = "0",

    // Lógica de Juego
    val canPrestige: Boolean = false,
    val buyMultiplier: Int = 1,
    val activeBuffMultiplier: Double = 1.0,
    val buffTimeRemaining: Int = 0,

    // Eventos y Logros
    val currentEvent: RandomEvent? = null,
    val eventResultText: String? = null,
    val lastUnlockedAchievement: Achievement? = null,
    val achievementsProgress: String = "0 / 0",
    val secretsProgress: String? = null,

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
    val importError: String? = null,
    val exportPreview: SavePreview? = null,
    val importPreview: SavePreview? = null,

    // Configuración
    val isMusicEnabled: Boolean = true,
    val isSfxEnabled: Boolean = true,
    val areParticlesEnabled: Boolean = true,
    val isFloatingTextEnabled: Boolean = true
)

data class SavePreview(
    val date: String,
    val money: String,
    val achievements: String
)