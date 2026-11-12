package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Constellation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class MadnessPathsTest {

    private val shop = PrestigeShopManager()
    private val lots = BigDecimal("1000000000")

    private fun node(id: String) = shop.upgrades.first { it.id == id }

    /** Enciende toda la constelación de una Senda (un nivel en cada estrella). */
    private fun completeConstellation(pathId: String) {
        val constellation = node(pathId).constellation!!
        shop.upgrades.filter { it.constellation == constellation && !MadnessPaths.isPath(it.id) }.forEach { it.level = maxOf(it.level, 1) }
    }

    @Test
    fun `each constellation has one path at its center and a chain of stars`() {
        assertEquals(7, MadnessPaths.IDS.size)
        Constellation.entries.forEach { c ->
            val paths = shop.upgrades.filter { it.constellation == c && MadnessPaths.isPath(it.id) }
            assertEquals(listOf(MadnessPaths.of(c)), paths.map { it.id })
            val stars = shop.upgrades.filter { it.constellation == c && !MadnessPaths.isPath(it.id) }
            assertTrue("$c necesita estrellas", stars.size >= 3)
            // La primera cuelga de El Despertar; cada una de las demás, de una anterior de
            // su misma constelación (la figura se enciende desde un extremo)
            assertEquals("pu_core", stars.first().parentId)
            stars.drop(1).forEachIndexed { i, star ->
                assertTrue("${star.id}", star.parentId in stars.take(i + 1).map { it.id })
            }
        }
        // v0.19.4: una estrella del Mapa por cada estrella de las siete figuras
        assertEquals(54, shop.upgrades.count { !MadnessPaths.isPath(it.id) })
    }

    @Test
    fun `a path opens only when its whole constellation is lit`() {
        assertFalse(shop.tryBuy(MadnessPaths.MIND, lots))
        val stars = shop.upgrades.filter { it.constellation == Constellation.MIND && !MadnessPaths.isPath(it.id) }
        stars.dropLast(1).forEach { it.level = 1 }
        assertFalse(shop.tryBuy(MadnessPaths.MIND, lots))
        stars.last().level = 1
        assertTrue(MadnessPaths.isComplete(Constellation.MIND, shop.upgrades))
        assertTrue(shop.tryBuy(MadnessPaths.MIND, lots))
        assertEquals(1, shop.completedConstellations())
    }

    @Test
    fun `walking one path seals the others`() {
        MadnessPaths.IDS.forEach { completeConstellation(it) }

        assertTrue(shop.tryBuy(MadnessPaths.HAND, lots))
        MadnessPaths.IDS.filter { it != MadnessPaths.HAND }.forEach { assertFalse(shop.tryBuy(it, lots)) }
        // La propia Senda sigue subiendo
        assertTrue(shop.tryBuy(MadnessPaths.HAND, lots))
        assertEquals(2, node(MadnessPaths.HAND).level)

        assertTrue(MadnessPaths.isSealed(node(MadnessPaths.ARCANE), shop.upgrades))
        assertFalse(MadnessPaths.isSealed(node(MadnessPaths.HAND), shop.upgrades))
        assertFalse(MadnessPaths.isSealed(node("pu_core"), shop.upgrades))
    }

    @Test
    fun `abandoning refunds everything spent and frees the choice`() {
        MadnessPaths.IDS.forEach { completeConstellation(it) }
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
        MadnessPaths.IDS.forEach { completeConstellation(it) }
        val click = shop.getClickPrestigeMultiplier()
        val buildings = shop.getBuildingTierMultiplier(10)
        val offline = shop.getOfflinePrestigeMultiplier()
        val global = shop.getPermanentGlobalMultiplier()
        val sanity = shop.getPrestigeGainMultiplier()

        node(MadnessPaths.HAND).level = 1
        assertEquals(0, click.multiply(BigDecimal(4)).compareTo(shop.getClickPrestigeMultiplier()))
        node(MadnessPaths.HAND).level = 0

        node(MadnessPaths.GEARS).level = 1
        assertEquals(0, buildings.multiply(BigDecimal(2)).compareTo(shop.getBuildingTierMultiplier(10)))
        node(MadnessPaths.GEARS).level = 0

        node(MadnessPaths.DREAM).level = 1
        assertEquals(0, offline.multiply(BigDecimal(4)).compareTo(shop.getOfflinePrestigeMultiplier()))
        node(MadnessPaths.DREAM).level = 0

        node(MadnessPaths.MIND).level = 2
        assertEquals(0, global.multiply(BigDecimal(2)).compareTo(shop.getPermanentGlobalMultiplier()))
        node(MadnessPaths.MIND).level = 0

        node(MadnessPaths.VOID).level = 1
        assertEquals(0, sanity.multiply(BigDecimal("1.5")).compareTo(shop.getPrestigeGainMultiplier()))
        node(MadnessPaths.VOID).level = 0

        // (Diezmo de Sangre, encendido con la constelación, ya da 1 Sacerdote)
        val priests = shop.getStartingBuildings()["b_priest"] ?: 0
        node(MadnessPaths.CULT).level = 2
        assertEquals(priests + 20, shop.getStartingBuildings()["b_priest"])
        node(MadnessPaths.CULT).level = 0

        val runes = shop.getMapEffects().runeFrequency
        node(MadnessPaths.ARCANE).level = 1
        assertEquals(runes * 1.25, shop.getMapEffects().runeFrequency, 1e-9)
    }

    @Test
    fun `the cult and the arcane nodes give their own bonuses`() {
        node("pu_legacy").level = 2
        node("pu_cult_scrolls").level = 3
        node("pu_cult_artifacts").level = 1
        node("pu_cult_flock").level = 2
        assertEquals(mapOf("b_notes" to 10, "b_scrolls" to 15, "b_artifact" to 3, "b_synapse" to 10), shop.getStartingBuildings())

        node("pu_gear_plans").level = 10
        assertEquals(0.70, shop.getMapEffects().buildingCost, 1e-9)
        node(PrestigeShopManager.ATTENTION_NODE_ID).level = 2
        assertEquals(2, shop.getAttentionBonus())
        node(PrestigeShopManager.MADNESS_NODE_ID).level = 5
        assertEquals(0.6, shop.getMadnessGainMultiplier(), 1e-9)

        // Los tramos del Engranaje: Engranajes Dobles solo a los edificios 7 a 11
        node("pu_gear_double").level = 2
        assertEquals(0, BigDecimal("1.5").compareTo(shop.getBuildingTierMultiplier(8)))
        assertEquals(0, BigDecimal.ONE.compareTo(shop.getBuildingTierMultiplier(12)))
        node("pu_void_eye").level = 1
        assertEquals(1.3, shop.getMapEffects().sanityGain, 1e-9)
    }

    @Test
    fun `nodes with a maximum stop there`() {
        shop.upgrades.filter { it.constellation == Constellation.ARCANE && !MadnessPaths.isPath(it.id) }.forEach { it.level = 1 }
        val runes = node("pu_arcane_runes")
        runes.level = runes.maxLevel!!
        assertFalse(shop.tryBuy(runes.id, lots))
        assertEquals(5, runes.level)
    }
}
