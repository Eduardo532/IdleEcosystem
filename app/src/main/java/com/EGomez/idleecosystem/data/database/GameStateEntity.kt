package com.EGomez.idleecosystem.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_save")
data class GameStateEntity(
    @PrimaryKey val id: Int = 1,
    val money: String,
    val premiumCurrency: String,
    val prestigeCurrency: String,
    val echoes: String,
    val lastLoginTime: Long,
    val totalPlayTime: Long
)