package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal

enum class UpgradeType {
    CLICK_MULTIPLIER,   // Multiplica el poder del clic manual
    BUILDING_MULTIPLIER, // Multiplica la producción de un edificio específico
    GLOBAL_MULTIPLIER,   // Multiplica la producción de TODO el juego
    SYNERGY              // Cada unidad de sourceBuildingId suma un % a targetBuildingId
}

data class Upgrade(
    val id: String,
    val name: String,
    val description: String,
    val cost: BigDecimal,
    val type: UpgradeType,
    val multiplier: Double, // x2.0, x3.0, etc.
    val targetBuildingId: String? = null, // Solo si es BUILDING_MULTIPLIER (ej: "b_notes")
    val unlockConditionAmount: Int = 0,   // "Aparece cuando tengas 10 de ese edificio" (Unfolding Gameplay)
    var isBought: Boolean = false,
    /** Solo en las sinergias: el edificio que potencia a [targetBuildingId]. */
    val sourceBuildingId: String? = null
)