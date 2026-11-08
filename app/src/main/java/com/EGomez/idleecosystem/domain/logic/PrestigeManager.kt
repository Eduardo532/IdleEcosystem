package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import java.math.BigDecimal
import java.math.MathContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrestigeManager @Inject constructor() {

    // Moneda de prestigio: "Cordura Residual"
    var residualSanity = BigDecimal.ZERO

    // Configuración
    private val PRESTIGE_THRESHOLD = BigDecimal("1000000") // Mínimo 1M para ascender
    private val BONUS_PER_POINT = BigDecimal("0.10")       // 10% de bono por punto

    /**
     * Calcula cuánta Cordura ganarías si reinicias ahora.
     *
     * Recibe el conocimiento GENERADO en la vida actual, no el saldo: antes se usaba el
     * saldo, y comprar edificios antes de ascender reducía la recompensa.
     */
    fun calculatePotentialPrestige(knowledgeThisRun: BigDecimal): BigDecimal {
        if (knowledgeThisRun < PRESTIGE_THRESHOLD) return BigDecimal.ZERO

        val ratio = knowledgeThisRun.divide(PRESTIGE_THRESHOLD, MathContext.DECIMAL64)
        // Raíz sin pasar por Double: Math.sqrt(Infinity) crasheaba por encima de 1e308
        return BigMath.floorSqrt(ratio)
    }

    // Calcula el multiplicador global actual basado en tu Cordura
    fun getPrestigeMultiplier(): BigDecimal {
        val bonus = residualSanity.multiply(BONUS_PER_POINT)
        return BigDecimal.ONE.add(bonus)
    }

    fun isPrestigeUnlocked(currentKnowledge: BigDecimal): Boolean {
        return residualSanity > BigDecimal.ZERO || currentKnowledge >= PRESTIGE_THRESHOLD
    }

    fun loadFromSave(savedAmount: String) {
        residualSanity = BigDecimal(savedAmount)
    }

    fun addPrestige(amount: BigDecimal) {
        residualSanity = residualSanity.add(amount)
    }
}