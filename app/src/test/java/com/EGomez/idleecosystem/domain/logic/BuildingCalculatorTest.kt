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
    fun `bulk cost equals the sum of single costs`() {
        val b = building(owned = 3)
        val copy = b.copy()
        var sum = BigDecimal.ZERO
        repeat(10) {
            sum = sum.add(calculator.calculateNextCost(copy))
            copy.amountOwned++
        }
        val bulk = calculator.calculateCostForAmount(b, 10)
        assertEquals(sum.toDouble(), bulk.toDouble(), sum.toDouble() * 1e-12)
    }

    @Test
    fun `MAX buys exactly what the money allows`() {
        for (owned in listOf(0, 5, 50, 200)) {
            for (money in listOf("15", "100", "12345", "9999999", "1e30")) {
                val b = building(owned)
                val m = BigDecimal(money)
                val n = calculator.calculateMaxAffordable(b, m)
                if (n == 0) {
                    assertTrue(calculator.calculateNextCost(b) > m)
                } else {
                    assertTrue("cost($n) must fit in $money", calculator.calculateCostForAmount(b, n) <= m)
                    assertTrue("cost(${n + 1}) must not fit in $money", calculator.calculateCostForAmount(b, n + 1) > m)
                }
            }
        }
    }

    @Test
    fun `MAX returns zero when not even one is affordable`() {
        assertEquals(0, calculator.calculateMaxAffordable(building(), BigDecimal("14.99")))
    }

    @Test
    fun `MAX works beyond the Double range`() {
        // Before: Double became Infinity above 1e308 and MAX stopped buying
        val money = BigDecimal("1e400")
        val n = calculator.calculateMaxAffordable(building(), money)
        assertTrue(n > 6000)
        assertTrue(calculator.calculateCostForAmount(building(), n) <= money)
    }
}
