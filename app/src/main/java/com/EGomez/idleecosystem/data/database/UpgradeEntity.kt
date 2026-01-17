package com.EGomez.idleecosystem.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upgrades_save")
data class UpgradeEntity(
    @PrimaryKey val upgradeId: String,
    val isBought: Boolean
)