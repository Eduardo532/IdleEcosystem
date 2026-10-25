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
    /** Cantidad de hitos alcanzados (cada uno duplica la producción de este edificio). */
    val milestonesReached: Int
        get() = MILESTONES.count { amountOwned >= it }

    /** Próximo hito a alcanzar, o null si ya se alcanzaron todos. */
    val nextMilestone: Int?
        get() = MILESTONES.firstOrNull { amountOwned < it }

    val milestoneMultiplier: BigDecimal
        get() = BigDecimal(2).pow(milestonesReached)

    val totalProduction: BigDecimal
        get() = baseProduction
            .multiply(BigDecimal(amountOwned))
            .multiply(milestoneMultiplier)
            .multiply(currentMultiplier)

    companion object {
        /** Al poseer estas cantidades, la producción del edificio se duplica. */
        val MILESTONES = listOf(25, 50, 100, 200, 300, 400, 500)
    }
}
