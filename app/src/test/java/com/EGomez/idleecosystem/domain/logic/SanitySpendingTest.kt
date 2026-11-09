package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class SanitySpendingTest {

    private fun node(type: UpgradeType, level: Int = 0) = PrestigeUpgrade(
        id = "pu_test", name = "", description = "",
        baseCost = BigDecimal(2), costMultiplier = BigDecimal(2), effectMultiplier = BigDecimal("0.10"),
        type = type, level = level
    )

    @Test
    fun `the preview shows both sides of spending`() {
        // Nivel 1 -> 2 cuesta 4. Con 20 de Cordura: bono x3,0 -> x2,6
        val preview = SanitySpending.preview(node(UpgradeType.GLOBAL_MULTIPLIER, level = 1), BigDecimal(20))

        assertEquals(0, BigDecimal("1.1").compareTo(preview.nodeBefore))
        assertEquals(0, BigDecimal("1.2").compareTo(preview.nodeAfter))
        assertEquals(0, BigDecimal("3.0").compareTo(preview.sanityBonusBefore))
        assertEquals(0, BigDecimal("2.6").compareTo(preview.sanityBonusAfter))
        // 1,2/1,1 x 2,6/3 = 0,945...: este nodo baja la producción
        assertEquals(0.9454, preview.netProduction!!.toDouble(), 0.0001)
    }

    @Test
    fun `only global nodes get a net production figure`() {
        assertNull(SanitySpending.preview(node(UpgradeType.CLICK_MULTIPLIER), BigDecimal(20)).netProduction)
    }

    @Test
    fun `the bonus per point matches the prestige manager`() {
        val manager = PrestigeManager().apply { loadFromSave("7") }
        val expected = BigDecimal.ONE.add(BigDecimal(7).multiply(SanitySpending.BONUS_PER_POINT))
        assertEquals(0, expected.compareTo(manager.getPrestigeMultiplier()))
    }
}
