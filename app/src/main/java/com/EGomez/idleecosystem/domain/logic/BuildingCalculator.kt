package com.EGomez.idleecosystem.domain.logic

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

        val r = building.growthFactor.toDouble()
        val costNextDouble = costOfNext.toDouble()
        val moneyDouble = currentMoney.toDouble()

        // Formula inversa derivada de la suma geométrica:
        // n = log_r ( 1 + (Money * (r-1) / CostNext) )
        val term = 1 + (moneyDouble * (r - 1) / costNextDouble)
        val n = Math.log(term) / Math.log(r)

        return n.toInt()
    }
}