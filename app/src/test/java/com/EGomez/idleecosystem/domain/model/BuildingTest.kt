package com.EGomez.idleecosystem.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class BuildingTest {

    private fun building(owned: Int) = Building(
        id = "b", name = "B", description = "",
        baseCost = BigDecimal.TEN, baseProduction = BigDecimal.ONE,
        growthFactor = BigDecimal("1.15"), amountOwned = owned
    )

    @Test
    fun `los hitos duplican la produccion`() {
        assertEquals(0, BigDecimal(24).compareTo(building(24).totalProduction))
        assertEquals(0, BigDecimal(50).compareTo(building(25).totalProduction))   // 25 * 2
        assertEquals(0, BigDecimal(200).compareTo(building(50).totalProduction))  // 50 * 4
        assertEquals(0, BigDecimal(800).compareTo(building(100).totalProduction)) // 100 * 8
    }

    @Test
    fun `proximo hito`() {
        assertEquals(25, building(0).nextMilestone)
        assertEquals(50, building(25).nextMilestone)
        assertNull(building(500).nextMilestone)
    }
}
