package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow
import kotlin.random.Random

class YuggothCrystalsTest {

    private val empty = List(YuggothCrystals.CELLS) { 0 }

    private fun grid(vararg tiers: Int): List<Int> = empty.toMutableList().also { tiers.forEachIndexed { i, t -> it[i] = t } }

    @Test
    fun `crystals open at the second awakening`() {
        assertFalse(YuggothCrystals.isUnlocked(1))
        assertTrue(YuggothCrystals.isUnlocked(2))
    }

    @Test
    fun `two crystals of the same tier fuse into the next`() {
        val g = grid(2, 2, 3)
        assertTrue(YuggothCrystals.canMerge(g, 0, 1))
        assertFalse(YuggothCrystals.canMerge(g, 0, 2))
        assertFalse(YuggothCrystals.canMerge(g, 0, 0))
        assertFalse(YuggothCrystals.canMerge(g, 3, 4))
        val merged = YuggothCrystals.merge(g, 0, 1)
        assertEquals(0, merged[0])
        assertEquals(3, merged[1])
        // Una fusión imposible no cambia nada
        assertEquals(g, YuggothCrystals.merge(g, 0, 2))
    }

    @Test
    fun `merging everything climbs from the lowest tier`() {
        // 1 + 1 → 2; 2 + 2 → 3; 3 + 3 → 4
        val (merged, count) = YuggothCrystals.mergeAll(grid(1, 1, 2, 3))
        assertEquals(3, count)
        assertEquals(listOf(4), merged.filter { it > 0 })
        assertEquals(0, YuggothCrystals.mergeAll(grid(1, 2, 3)).second)
    }

    @Test
    fun `the mine fills empty cells only`() {
        val random = Random(7)
        var g = empty
        repeat(YuggothCrystals.CELLS) { g = YuggothCrystals.spawn(g, random) }
        assertTrue(g.all { it == 1 })
        assertEquals(g, YuggothCrystals.spawn(g, random))
    }

    @Test
    fun `power, production and lustre`() {
        assertEquals(0.0, YuggothCrystals.power(0), 1e-12)
        assertEquals(81.0, YuggothCrystals.power(5), 1e-12)
        assertEquals(84.0, YuggothCrystals.gridPower(grid(5, 2)), 1e-12)
        assertEquals(168.0, YuggothCrystals.gridPower(grid(5, 2), 10), 1e-12)
        assertEquals(85.0, YuggothCrystals.productionMultiplier(84.0), 1e-12)
        assertEquals(5.0, YuggothCrystals.spawnSeconds(20), 1e-12)
    }

    @Test
    fun `polishing needs a tier five crystal`() {
        assertFalse(YuggothCrystals.canPolish(grid(4, 4)))
        assertEquals(0, YuggothCrystals.lustreGain(grid(4, 4)))
        // √(84 / 10) ≈ 2,9
        assertEquals(2, YuggothCrystals.lustreGain(grid(5, 2)))
        assertEquals(14, YuggothCrystals.lustreGain(grid(8))) // √218,7 ≈ 14,8
    }

    // ---------------------------------------------------------------- v0.22.1

    @Test
    fun `refining needs 25 lustre and pays mi-go essence`() {
        assertFalse(YuggothCrystals.canRefine(24))
        assertEquals(0, YuggothCrystals.essenceGain(24))
        // √(25 / 5) ≈ 2,2; √(125 / 5) = 5
        assertEquals(2, YuggothCrystals.essenceGain(25))
        assertEquals(5, YuggothCrystals.essenceGain(125))
    }

    @Test
    fun `the refining tree opens from the drills and refunds everything`() {
        val none = emptyMap<String, Int>()
        assertTrue(YuggothCrystals.canBuy(RefineNode.DRILLS, none, 1))
        assertFalse(YuggothCrystals.canBuy(RefineNode.DRILLS, none, 0))
        assertFalse(YuggothCrystals.canBuy(RefineNode.FACETS, none, 99))
        val levels = mapOf("drills" to 2, "facets" to 1)
        assertTrue(YuggothCrystals.canBuy(RefineNode.FACETS, levels, 2))
        assertFalse(YuggothCrystals.canBuy(RefineNode.ECHO, levels, 99))
        assertFalse(YuggothCrystals.canBuy(RefineNode.DRILLS, mapOf("drills" to 5), 999))
        // Taladros: 1 + 2; Facetas: 1
        assertEquals(4, YuggothCrystals.refund(levels))
        assertEquals(8, RefineNode.ECHO.cost(0))
        assertEquals(45, RefineNode.CORE.cost(1))
    }

    @Test
    fun `refining nodes change the mine, power, polish and ether`() {
        assertEquals(10.0 / 2.25, YuggothCrystals.spawnSeconds(0, drills = 2), 1e-9)
        assertEquals(84.0 * 4, YuggothCrystals.gridPower(grid(5, 2), facets = 2), 1e-9)
        assertEquals(100.0.pow(1.1), YuggothCrystals.productionMultiplier(99.0, core = 2), 1e-6)
        // √8,4 ≈ 2,9 × 2 = 5,8
        assertEquals(5, YuggothCrystals.lustreGain(grid(5, 2), fungal = 2))
        assertEquals(2.5, YuggothCrystals.etherMultiplier(999.0, lens = 5), 1e-6) // 1 + 0,5 × 3
        assertEquals(1.0, YuggothCrystals.etherMultiplier(999.0, lens = 0), 1e-12)
    }

    @Test
    fun `the seed and the vein drop bigger and more crystals`() {
        val g = YuggothCrystals.spawn(empty, Random(3), tier = 3, count = 2)
        assertEquals(listOf(3, 3), g.filter { it > 0 })
        // Con una casilla libre cae solo uno
        val almost = List(YuggothCrystals.CELLS) { if (it == 0) 0 else 1 }
        assertEquals(1, YuggothCrystals.spawn(almost, Random(3), tier = 2, count = 3).count { it == 2 })
    }

    @Test
    fun `the echo keeps the best crystal two tiers lower`() {
        assertTrue(YuggothCrystals.afterPolish(grid(6, 3), echo = false).all { it == 0 })
        val kept = YuggothCrystals.afterPolish(grid(3, 6), echo = true)
        assertEquals(listOf(0, 4), kept.take(2))
        assertEquals(1, kept.count { it > 0 })
        assertFalse(YuggothCrystals.canPolish(kept))
    }
}
