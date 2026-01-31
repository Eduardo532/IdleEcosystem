package com.EGomez.idleecosystem.data.database

import androidx.room.ColumnInfo
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
    val totalPlayTime: Long,

    @ColumnInfo(defaultValue = "1")
    val tutorialEnabled: Boolean = true,

    @ColumnInfo(defaultValue = "")
    val completedTutorialSteps: String = ""
)