package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Upgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class GrimoireShoppingTest {

    private fun upgrade(id: String, cost: Int, bought: Boolean = false) =
        Upgrade(id, id, "", BigDecimal(cost), UpgradeType.GLOBAL_MULTIPLIER, 2.0, isBought = bought)

    @Test
    fun `buys from the cheapest up while the money lasts`() {
        val list = listOf(upgrade("c", 300), upgrade("a", 100), upgrade("b", 200))
        // 100 + 200 = 300 <= 350; la de 300 ya no alcanza
        assertEquals(listOf("a", "b"), GrimoireShopping.plan(list, BigDecimal(350)).map { it.id })
    }

    @Test
    fun `bought upgrades are skipped`() {
        val list = listOf(upgrade("a", 100, bought = true), upgrade("b", 200))
        assertEquals(listOf("b"), GrimoireShopping.plan(list, BigDecimal(250)).map { it.id })
    }

    @Test
    fun `stops at the first unaffordable one`() {
        // Aunque la de 50 alcanzaría, el orden es por costo y se corta al primer faltante
        val list = listOf(upgrade("a", 100), upgrade("b", 50))
        assertEquals(1, GrimoireShopping.affordableCount(list, BigDecimal(60)))
        assertEquals(0, GrimoireShopping.affordableCount(list, BigDecimal(10)))
    }
}
