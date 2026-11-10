package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class MadnessPathsTest {

    private val shop = PrestigeShopManager()
    private val lots = BigDecimal("1000000")

    private fun node(id: String) = shop.upgrades.first { it.id == id }

    /** Compra en cadena todos los ancestros de un nodo (un nivel cada uno). */
    private fun unlockParents(id: String) {
        var parentId = node(id).parentId
        while (parentId != null) {
            node(parentId).level = maxOf(node(parentId).level, 1)
            parentId = node(parentId).parentId
        }
    }

    @Test
    fun `each path hangs from the end of a branch`() {
        assertEquals("pu_click_4", node(MadnessPaths.HAND).parentId)
        assertEquals("pu_idle_2", node(MadnessPaths.GEARS).parentId)
        assertEquals("pu_offline_2", node(MadnessPaths.DREAM).parentId)
    }

    @Test
    fun `walking one path seals the other two`() {
        MadnessPaths.IDS.forEach { unlockParents(it) }

        assertTrue(shop.tryBuy(MadnessPaths.HAND, lots))
        assertFalse(shop.tryBuy(MadnessPaths.GEARS, lots))
        assertFalse(shop.tryBuy(MadnessPaths.DREAM, lots))
        // La propia Senda sigue subiendo
        assertTrue(shop.tryBuy(MadnessPaths.HAND, lots))
        assertEquals(2, node(MadnessPaths.HAND).level)

        assertTrue(MadnessPaths.isSealed(node(MadnessPaths.GEARS), shop.upgrades))
        assertFalse(MadnessPaths.isSealed(node(MadnessPaths.HAND), shop.upgrades))
        assertFalse(MadnessPaths.isSealed(node("pu_core"), shop.upgrades))
    }

    @Test
    fun `abandoning refunds everything spent and frees the choice`() {
        MadnessPaths.IDS.forEach { unlockParents(it) }
        shop.tryBuy(MadnessPaths.DREAM, lots)
        shop.tryBuy(MadnessPaths.DREAM, lots)

        // 2000 + 2000 × 2,5
        val refund = shop.abandonPath()
        assertEquals(0, BigDecimal("7000").compareTo(refund))
        assertEquals(0, node(MadnessPaths.DREAM).level)
        assertNull(MadnessPaths.active(shop.upgrades))
        assertTrue(shop.tryBuy(MadnessPaths.GEARS, lots))
        // Sin Senda no hay nada que devolver
        node(MadnessPaths.GEARS).level = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(shop.abandonPath()))
    }

    @Test
    fun `each path boosts its own kind of progress`() {
        MadnessPaths.IDS.forEach { unlockParents(it) }
        val click = shop.getClickPrestigeMultiplier()
        val buildings = shop.getBuildingTierMultiplier(10)
        val offline = shop.getOfflinePrestigeMultiplier()

        node(MadnessPaths.HAND).level = 1
        assertEquals(0, click.multiply(BigDecimal(4)).compareTo(shop.getClickPrestigeMultiplier()))
        node(MadnessPaths.HAND).level = 0

        node(MadnessPaths.GEARS).level = 1
        assertEquals(0, buildings.multiply(BigDecimal(2)).compareTo(shop.getBuildingTierMultiplier(10)))
        node(MadnessPaths.GEARS).level = 0

        node(MadnessPaths.DREAM).level = 1
        assertEquals(0, offline.multiply(BigDecimal(4)).compareTo(shop.getOfflinePrestigeMultiplier()))
    }
}
