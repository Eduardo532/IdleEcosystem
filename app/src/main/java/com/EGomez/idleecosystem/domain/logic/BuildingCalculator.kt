package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import com.EGomez.idleecosystem.domain.model.Building
import java.math.BigDecimal
import java.math.MathContext
import javax.inject.Inject

class BuildingCalculator @Inject constructor() {

    // Costo del SIGUIENTE edificio (x1)
    fun calculateNextCost(building: Building): BigDecimal {
        val factor = building.growthFactor.pow(building.amountOwned, MathContext.DECIMAL64)
        return building.baseCost.multiply(factor)
    }

    // Costo de N edificios (Suma Geométrica)
    // Formula: CostoActual * ( (r^n - 1) / (r - 1) )
    fun calculateCostForAmount(building: Building, amountToBuy: Int): BigDecimal {
        if (amountToBuy <= 1) return calculateNextCost(building)

        val r = building.growthFactor
        val n = amountToBuy

        // Costo del próximo edificio individual (Base * r^k)
        val currentCost = calculateNextCost(building)

        // Factor geométrico: (r^n - 1) / (r - 1)
        val numerator = r.pow(n, MathContext.DECIMAL64).subtract(BigDecimal.ONE)
        val denominator = r.subtract(BigDecimal.ONE)
        val geometricFactor = numerator.divide(denominator, MathContext.DECIMAL64)

        return currentCost.multiply(geometricFactor)
    }

    // Calcular cuántos puedo comprar con mi dinero actual (MAX)
    fun calculateMaxAffordable(building: Building, currentMoney: BigDecimal): Int {
        val costOfNext = calculateNextCost(building)
        if (currentMoney < costOfNext) return 0

        val r = building.growthFactor

        // Formula inversa derivada de la suma geométrica:
        // n = log_r ( 1 + (Money * (r-1) / CostNext) )
        // Se calcula con logaritmos de BigDecimal para no desbordar Double (> 1e308).
        val term = BigDecimal.ONE.add(
            currentMoney.multiply(r.subtract(BigDecimal.ONE)).divide(costOfNext, MathContext.DECIMAL64)
        )
        val estimate = BigMath.ln(term) / BigMath.ln(r)
        var n = estimate.toLong().coerceIn(1L, MAX_BULK_PURCHASE.toLong()).toInt()

        // La estimación con decimales puede desviarse en ±1: se corrige con el costo exacto
        // (antes, MAX a veces no compraba nada porque el costo calculado superaba el dinero).
        while (n > 1 && calculateCostForAmount(building, n) > currentMoney) n--
        while (n < MAX_BULK_PURCHASE && calculateCostForAmount(building, n + 1) <= currentMoney) n++

        return n
    }

    companion object {
        /** Límite de una compra masiva para evitar desbordes de Int. */
        const val MAX_BULK_PURCHASE = 100_000
    }
}
