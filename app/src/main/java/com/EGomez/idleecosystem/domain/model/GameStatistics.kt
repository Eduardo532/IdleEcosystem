package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal

/**
 * Foto inmutable de las estadísticas para la UI.
 *
 * El Registro Akáshico leía directamente las variables mutables de StatisticsManager:
 * no se actualizaba mientras estaba abierto y, con el motor fuera del hilo principal,
 * habría leído valores a medio escribir. Ahora recibe esta copia.
 */
data class GameStatistics(
    // TIEMPO
    val startTime: Long = 0,
    val totalPlayTimeSeconds: Long = 0,
    val totalOfflineSeconds: Long = 0,
    val runTimeSeconds: Long = 0,

    // ECONOMÍA
    val lifetimeKnowledge: BigDecimal = BigDecimal.ZERO,
    val runKnowledge: BigDecimal = BigDecimal.ZERO,
    val maxKnowledgePerSecond: BigDecimal = BigDecimal.ZERO,

    // CLICS (Manual)
    val totalClicks: Long = 0,
    val manualKnowledgeGenerated: BigDecimal = BigDecimal.ZERO,

    // PRESTIGIO
    val totalPrestiges: Int = 0,
    val lifetimeSanity: BigDecimal = BigDecimal.ZERO,
    val spentSanity: BigDecimal = BigDecimal.ZERO,

    // MISTERIOS
    val totalEventsFound: Int = 0,
    val totalEchoes: BigDecimal = BigDecimal.ZERO,

    // RUNAS
    val runesSevered: Int = 0,    // Tier 1
    val runesStabilized: Int = 0, // Tier 2
    val runesInscribed: Int = 0   // Tier 3
) {
    val activePlayTimeSeconds: Long
        get() = totalPlayTimeSeconds - totalOfflineSeconds
}
