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

    /**
     * Cuánto falta para el siguiente punto y para duplicar la recompensa. La Cordura es
     * la raíz cuadrada del Conocimiento generado, así que duplicarla exige 4 veces más:
     * mostrarlo convierte "¿cuándo asciendo?" en una meta concreta.
     *
     * [gainMultiplier] es el bono de la Tienda de Cordura (se aplica a los puntos, no
     * al Conocimiento necesario).
     */
    fun pacing(knowledgeThisRun: BigDecimal, gainMultiplier: BigDecimal = BigDecimal.ONE): PrestigePacing {
        val points = calculatePotentialPrestige(knowledgeThisRun)
        val nextPoints = points.add(BigDecimal.ONE)
        val doublePoints = points.multiply(BigDecimal(2))
        return PrestigePacing(
            nextGain = nextPoints.multiply(gainMultiplier),
            nextAt = knowledgeForPoints(nextPoints),
            doubleGain = if (points.signum() > 0) doublePoints.multiply(gainMultiplier) else null,
            doubleAt = if (points.signum() > 0) knowledgeForPoints(doublePoints) else null
        )
    }

    /** El Conocimiento generado en una vida que da exactamente [points] de Cordura. */
    fun knowledgeForPoints(points: BigDecimal): BigDecimal =
        points.multiply(points).multiply(PRESTIGE_THRESHOLD)

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

/**
 * El ritmo de la Ascensión: la Cordura del siguiente punto y la del doble de lo que
 * ganarías ahora, con el Conocimiento generado que hace falta para cada una. Sin
 * puntos todavía no hay "doble" (es null).
 */
data class PrestigePacing(
    val nextGain: BigDecimal = BigDecimal.ONE,
    val nextAt: BigDecimal = BigDecimal("1000000"),
    val doubleGain: BigDecimal? = null,
    val doubleAt: BigDecimal? = null
)
