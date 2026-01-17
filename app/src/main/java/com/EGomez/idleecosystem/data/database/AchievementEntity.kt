package com.EGomez.idleecosystem.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements_save")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val isUnlocked: Boolean
)