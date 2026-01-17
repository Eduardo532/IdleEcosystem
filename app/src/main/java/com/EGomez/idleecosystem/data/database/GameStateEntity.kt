package com.EGomez.idleecosystem.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_save")
data class GameStateEntity(
    @PrimaryKey val id: Int = 0, // Siempre será 0, solo hay un "save file" activo
    val money: String,           // String para soportar BigNumber/BigDecimal
    val premiumCurrency: String,
    val prestigeCurrency: String,
    val lastLoginTime: Long,     // Para calcular progreso offline [cite: 133]
    val totalPlayTime: Long
)