package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal
import java.text.DecimalFormat

data class PrestigeUpgrade(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: BigDecimal,
    val costMultiplier: BigDecimal,
    val effectMultiplier: BigDecimal,
    val type: UpgradeType,
    var level: Int = 0,

    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val parentId: String? = null
) {
    val isUnlocked: Boolean
        get() = level > 0

    val currentCost: BigDecimal
        get() = baseCost.multiply(costMultiplier.pow(level))

    val currentEffectDisplay: String
        get() {
            if (level == 0) return if (id == "pu_legacy") "+0" else "x1.00"
            // Legado del Culto: no multiplica, da edificios al empezar
            if (id == "pu_legacy") return "+${effectMultiplier.toInt() * level}"

            val bonus = effectMultiplier.multiply(BigDecimal(level))
            val total = BigDecimal.ONE.add(bonus)

            val formatter = DecimalFormat("#,##0.00")
            return "x${formatter.format(total)}"
        }
}