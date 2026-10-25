package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import java.math.BigDecimal
import java.math.MathContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrestigeManager @Inject constructor() {

    // Moneda de prestigio: "Cordura Residual"
    var residualSanity: BigDecimal = BigDecimal.ZERO

    /**
     * Calcula cuánta Cordura ganarías si reinicias ahora.
     *
     * Recibe el conocimiento GENERADO en esta vida (no el saldo actual), así comprar
     * edificios ya no reduce la Cordura que obtendrás al ascender.
     */
    fun calculatePotentialPrestige(knowledgeThisRun: BigDecimal): BigDecimal {
        if (knowledgeThisRun < PRESTIGE_THRESHOLD) return BigDecimal.ZERO

        val ratio = knowledgeThisRun.divide(PRESTIGE_THRESHOLD, MathContext.DECIMAL64)
        // Raíz cuadrada sin pasar por Double (que desborda por encima de 1e308)
        return BigMath.floorSqrt(ratio)
    }

    // Calcula el multiplicador global actual basado en tu Cordura
    fun getPrestigeMultiplier(): BigDecimal {
        val bonus = residualSanity.multiply(BONUS_PER_POINT)
        return BigDecimal.ONE.add(bonus)
    }

    fun addPrestige(amount: BigDecimal) {
        residualSanity = residualSanity.add(amount)
    }

    companion object {
        /** Mínimo de conocimiento generado en la vida actual para ascender. */
        val PRESTIGE_THRESHOLD = BigDecimal("1000000")

        /** 10% de bono global por punto de Cordura sin gastar. */
        val BONUS_PER_POINT = BigDecimal("0.10")
    }
}
