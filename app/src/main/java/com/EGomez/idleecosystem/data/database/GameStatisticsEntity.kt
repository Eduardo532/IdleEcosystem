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
    val totalOfflineSeconds: Long, // <--- NUEVO CAMPO

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

    // OTROS
    val totalEventsFound: Int
)