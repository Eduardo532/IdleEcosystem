package com.EGomez.idleecosystem.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prestige_upgrades")
data class PrestigeUpgradeEntity(
    @PrimaryKey val id: String,
    val level: Int
)