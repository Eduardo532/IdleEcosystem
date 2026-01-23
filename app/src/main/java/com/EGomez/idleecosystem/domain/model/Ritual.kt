package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal

enum class RitualState {
    READY,
    ACTIVE,
    COOLDOWN
}

enum class RitualEffectType {
    PRODUCTION_MULTIPLIER,
    CLICK_MULTIPLIER,
    INSTANT_KNOWLEDGE
}

data class Ritual(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: BigDecimal,
    val durationSeconds: Long,
    val cooldownSeconds: Long,

    val effectType: RitualEffectType,
    val effectValue: Double,// Ej: 3.0 para x3

    // Estado Dinámico
    val state: RitualState = RitualState.READY,
    val timeRemaining: Long = 0,
    val totalTimeForProgress: Long = 1
) {
    fun getProgress(): Float {
        if (totalTimeForProgress <= 0) return 0f
        return timeRemaining.toFloat() / totalTimeForProgress.toFloat()
    }
}