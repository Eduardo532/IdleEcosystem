package com.EGomez.idleecosystem.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_statistics")
data class GameStatisticsEntity(
    @PrimaryKey val id: Int = 1,

    // TIEMPO
    val totalPlayTimeSeconds: Long,
    val startTime: Long,
    val runTimeSeconds: Long,
    val totalOfflineSeconds: Long,

    // ECONOMÍA
    val lifetimeKnowledge: String,
    val runKnowledge: String,
    val maxKnowledgePerSecond: String,

    // CLICS
    val totalClicks: Long,
    val manualKnowledgeGenerated: String,

    // PRESTIGIO
    val totalPrestiges: Int,
    val lifetimeSanity: String,
    val spentSanity: String,

    // RUNE SYSTEM
    val runesSevered: Int = 0,
    val runesStabilized: Int = 0,
    val runesInscribed: Int = 0,

    // OTROS
    val totalEventsFound: Int,
    val totalEchoes: String = "0"

)