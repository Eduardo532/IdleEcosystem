package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Building
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BuildingCalculatorTest {

    private val calculator = BuildingCalculator()

    private fun building(owned: Int = 0) = Building(
        id = "b_test", name = "Test", description = "",
        baseCost = BigDecimal("15"), baseProduction = BigDecimal("1"),
        growthFactor = BigDecimal("1.15"), amountOwned = owned
    )

    @Test
    fun `el costo de N es la suma de los costos individuales`() {
        val b = building(owned = 3)
        var sum = BigDecimal.ZERO
        val copy = b.copy()
        repeat(10) {
            sum = sum.add(calculator.calculateNextCost(copy))
            copy.amountOwned++
        }
        val bulk = calculator.calculateCostForAmount(b, 10)
        assertEquals(sum.toDouble(), bulk.toDouble(), sum.toDouble() * 1e-12)
    }

    @Test
    fun `MAX compra exactamente lo que alcanza`() {
        for (owned in listOf(0, 5, 50, 200)) {
            for (money in listOf("15", "100", "12345", "9999999", "1e30")) {
                val b = building(owned)
                val m = BigDecimal(money)
                val n = calculator.calculateMaxAffordable(b, m)
                if (n == 0) {
                    assertTrue(calculator.calculateNextCost(b) > m)
                } else {
                    assertTrue("costo($n) debe alcanzar con $money", calculator.calculateCostForAmount(b, n) <= m)
                    assertTrue("costo(${n + 1}) no debe alcanzar con $money", calculator.calculateCostForAmount(b, n + 1) > m)
                }
            }
        }
    }

    @Test
    fun `MAX devuelve cero si no alcanza para uno`() {
        assertEquals(0, calculator.calculateMaxAffordable(building(), BigDecimal("14.99")))
    }

    @Test
    fun `MAX funciona con dinero por encima del rango de Double`() {
        val n = calculator.calculateMaxAffordable(building(), BigDecimal("1e400"))
        assertTrue(n > 6000)
        assertTrue(calculator.calculateCostForAmount(building(), n) <= BigDecimal("1e400"))
    }
}
