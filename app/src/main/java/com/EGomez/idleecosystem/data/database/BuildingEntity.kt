package com.EGomez.idleecosystem.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buildings_save")
data class BuildingEntity(
    @PrimaryKey val buildingId: String, // ej: "b_notes"
    val amountOwned: Int,
    val isUnlocked: Boolean = true // Para el futuro "Unfolding Gameplay"
)