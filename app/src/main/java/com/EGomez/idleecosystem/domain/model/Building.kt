package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal

enum class BuildingType {
    PASSIVE,
    CLICK
}

data class Building(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: BigDecimal,
    val baseProduction: BigDecimal,
    val growthFactor: BigDecimal,
    val tier: Int = 1,
    val type: BuildingType = BuildingType.PASSIVE,
    var amountOwned: Int = 0,
    var currentMultiplier: BigDecimal = BigDecimal.ONE,

    var isVisible: Boolean = false
) {
    val totalProduction: BigDecimal
        get() = baseProduction
            .multiply(BigDecimal(amountOwned))
            .multiply(currentMultiplier)
}