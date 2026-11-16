package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
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
        assertEquals(15, YuggothCrystals.lustreGain(grid(8)))
    }
}
