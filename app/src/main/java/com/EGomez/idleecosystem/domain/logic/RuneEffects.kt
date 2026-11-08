package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.RuneTier

/**
 * Lo que hace cada runa al reclamarla.
 *
 * Antes estos valores estaban dentro de un `when` en RuneManager y no había forma de
 * revisarlos en conjunto: el Sigilo de Cthulhu (x777 durante 60 s) equivalía a unas
 * 13 horas de producción y la Runa de Sangre a 12 minutos, con una runa cada ~2 min.
 *
 * Ahora cada runa de producción se mide en "segundos de producción equivalentes"
 * (ver [productionSeconds]) y cada nivel tiene un rango fijo ([budgetFor]), que un
 * test unitario comprueba para todo el catálogo.
 */
data class RuneEffect(
    val multiplier: Double = 1.0,
    val clickMultiplier: Double = 1.0,
    val durationMs: Long = 0,
    val isAutoClick: Boolean = false,
    val costReduction: Double = 0.0,
    val echoChance: Double = 0.0,
    val freezesBuffs: Boolean = false,
    val reward: RuneReward? = null
) {
    /**
     * Producción extra que entrega, en segundos de producción base. Los potenciadores
     * se SUMAN (BuffMath), así que x10 durante 15 s aporta 9 x 15 = 135 s extra.
     * Un salto de tiempo aporta exactamente sus segundos.
     */
    val productionSeconds: Long
        get() {
            val fromBuff = if (multiplier > 1.0) ((multiplier - 1.0) * durationMs / 1000.0).toLong() else 0L
            val fromWarp = (reward as? RuneReward.TimeWarp)?.seconds ?: 0L
            return fromBuff + fromWarp
        }

    /** True si la runa entrega producción (multiplicador o salto de tiempo). */
    val givesProduction: Boolean get() = productionSeconds > 0

    /**
     * Valor de una runa de clic, en "clics equivalentes": el autoclic pulsa
     * [AUTO_CLICKS_PER_SECOND] veces por segundo, y cada clic vale
     * [clickMultiplier] clics normales. Sin autoclic, el multiplicador solo
     * potencia los clics del jugador y no se cuenta aquí.
     */
    val clickEquivalents: Long
        get() {
            if (!isAutoClick) return 0L
            val clicks = durationMs / 1000 * AUTO_CLICKS_PER_SECOND
            return (clicks * clickMultiplier.coerceAtLeast(1.0)).toLong()
        }

    companion object {
        /** El motor aplica un clic automático en cada tick de 100 ms. */
        const val AUTO_CLICKS_PER_SECOND = 10L
    }
}

object RuneEffects {

    /**
     * Rango de clics equivalentes por nivel para las runas que SOLO dan clics.
     * El poder de clic no se puede convertir a segundos de producción (depende de
     * cuántas Sinapsis tenga el jugador), así que tiene su propia escala.
     */
    fun clickBudgetFor(tier: RuneTier): LongRange = when (tier) {
        RuneTier.TIER_1_SWIPE -> 300L..600L
        RuneTier.TIER_2_HOLD -> 1_200L..2_400L
        RuneTier.TIER_3_TRACE -> 4_000L..8_000L
    }

    /** Rango de producción equivalente por nivel, en segundos. */
    fun budgetFor(tier: RuneTier): LongRange = when (tier) {
        RuneTier.TIER_1_SWIPE -> 90L..180L    // 1,5 a 3 minutos
        RuneTier.TIER_2_HOLD -> 360L..600L    // 6 a 10 minutos
        RuneTier.TIER_3_TRACE -> 1_200L..2_400L // 20 a 40 minutos
    }

    fun forRune(id: String): RuneEffect = when (id) {
        // --- TIER 1 (deslizar) ---
        "t1_blood" -> RuneEffect(multiplier = 10.0, durationMs = 15_000)        // 135 s
        "t1_ash" -> RuneEffect(multiplier = 8.0, durationMs = 20_000)           // 140 s
        "t1_salt" -> RuneEffect(multiplier = 15.0, durationMs = 10_000)         // 140 s
        "t1_bone" -> RuneEffect(reward = RuneReward.TimeWarp(120))              // 120 s
        "t1_shadow" -> RuneEffect(multiplier = 5.0, durationMs = 30_000)        // 120 s
        "t1_whisper" -> RuneEffect(clickMultiplier = 2.0, durationMs = 20_000, isAutoClick = true)   // 400 clics
        "t1_rust" -> RuneEffect(multiplier = 9.0, durationMs = 18_000)          // 144 s
        "t1_frost" -> RuneEffect(durationMs = 5_000, freezesBuffs = true)
        "t1_vermin" -> RuneEffect(multiplier = 12.0, durationMs = 12_000)       // 132 s
        "t1_rift" -> RuneEffect(reward = RuneReward.TimeWarp(180))              // 180 s
        "t1_void_minor" -> RuneEffect(durationMs = 30_000, echoChance = 0.01)

        // --- TIER 2 (mantener) ---
        "t2_chronos" -> RuneEffect(clickMultiplier = 5.0, durationMs = 30_000, isAutoClick = true)   // 1.500 clics
        "t2_gravity" -> RuneEffect(multiplier = 25.0, durationMs = 20_000)      // 480 s
        "t2_barrier" -> RuneEffect(durationMs = 30_000, costReduction = 0.15)
        "t2_tidal" -> RuneEffect(multiplier = 50.0, durationMs = 10_000)        // 490 s
        "t2_psyche" -> RuneEffect(reward = RuneReward.SanityBonus(0.05))
        "t2_vision" -> RuneEffect(reward = RuneReward.TriggerRandomEvent)
        "t2_entropy" -> RuneEffect() // RuneManager fuerza la siguiente aparición

        // --- TIER 3 (trazar) ---
        "t3_cthulhu" -> RuneEffect(multiplier = 30.0, durationMs = 60_000, reward = RuneReward.InstantEchoes(1)) // 1.740 s
        "t3_nyarlathotep" -> RuneEffect(multiplier = 25.0, durationMs = 60_000, isAutoClick = true)              // 1.440 s
        "t3_azathoth" -> RuneEffect(reward = RuneReward.InstantEchoes(50))

        else -> RuneEffect()
    }
}
