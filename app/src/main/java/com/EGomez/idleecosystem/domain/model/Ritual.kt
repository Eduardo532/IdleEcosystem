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
    INSTANT_KNOWLEDGE,
    /** Mientras dura, los potenciadores temporales se multiplican en vez de sumarse. */
    CONVERGENCE,
    /** Al instante: baja el Medidor de Locura (v0.17.3). */
    CALM
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
    val totalTimeForProgress: Long = 1,
    /** Multiplicador de costo del pacto activo. */
    val costMultiplier: Double = 1.0
) {
    /** Lo que cuesta invocarlo ahora (con el pacto), redondeado hacia arriba. */
    val cost: BigDecimal
        get() = if (costMultiplier == 1.0) baseCost
        else baseCost.multiply(BigDecimal.valueOf(costMultiplier)).setScale(0, java.math.RoundingMode.CEILING)

    /**
     * Producción que entrega, en segundos de producción base: un multiplicador xN
     * durante D segundos aporta (N - 1) x D (los potenciadores se suman); el
     * Conocimiento instantáneo aporta exactamente sus segundos. Los rituales de clic
     * y la Convergencia (que no produce por sí sola) no se miden aquí.
     */
    val productionSeconds: Long
        get() = when (effectType) {
            RitualEffectType.PRODUCTION_MULTIPLIER -> ((effectValue - 1.0) * durationSeconds).toLong()
            RitualEffectType.INSTANT_KNOWLEDGE -> effectValue.toLong()
            RitualEffectType.CLICK_MULTIPLIER, RitualEffectType.CONVERGENCE, RitualEffectType.CALM -> 0L
        }

    fun getProgress(): Float {
        if (totalTimeForProgress <= 0) return 0f
        return timeRemaining.toFloat() / totalTimeForProgress.toFloat()
    }
}