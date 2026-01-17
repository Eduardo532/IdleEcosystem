package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal

data class GameStatistics(
    // TIEMPO
    val totalPlayTimeSeconds: Long = 0,
    val startTime: Long = System.currentTimeMillis(), // Fecha de instalación
    val lastLoginTime: Long = System.currentTimeMillis(),
    val runTimeSeconds: Long = 0, // Tiempo en esta ascensión

    // ECONOMÍA
    val lifetimeKnowledge: BigDecimal = BigDecimal.ZERO, // Todo lo generado jamás
    val runKnowledge: BigDecimal = BigDecimal.ZERO, // Generado en esta run
    val maxKnowledgePerSecond: BigDecimal = BigDecimal.ZERO, // Pico de producción

    // CLICS (Manual)
    val totalClicks: Long = 0,
    val manualKnowledgeGenerated: BigDecimal = BigDecimal.ZERO,

    // PRESTIGIO
    val totalPrestiges: Int = 0,
    val lifetimeSanity: BigDecimal = BigDecimal.ZERO,
    val spentSanity: BigDecimal = BigDecimal.ZERO,

    // OTROS
    val totalEventsFound: Int = 0,
    val secretsFound: Int = 0
)