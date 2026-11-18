package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AzathothDreamTest {

    @Test
    fun `the dream shows after the first card and opens with the king`() {
        assertFalse(AzathothDream.isRevealed(0, 0))
        assertTrue(AzathothDream.isRevealed(1, 0))
        assertTrue(AzathothDream.isRevealed(0, 1))
        assertFalse(AzathothDream.canDescend(kingCompleted = false, awakeningsSince = 20, gates = 0))
        assertFalse(AzathothDream.canDescend(kingCompleted = true, awakeningsSince = 7, gates = 0))
        assertTrue(AzathothDream.canDescend(kingCompleted = true, awakeningsSince = 8, gates = 0))
        assertTrue(AzathothDream.canDescend(kingCompleted = true, awakeningsSince = 5, gates = 3))
    }

    @Test
    fun `chaos grows with every awakening past the minimum`() {
        assertEquals(0, AzathothDream.chaosGain(true, 7, 0, 0))
        assertEquals(2, AzathothDream.chaosGain(true, 8, 0, 0))
        assertEquals(12, AzathothDream.chaosGain(true, 13, 0, 0))
        // Puertas 2: pide 6; Ecos 3: +6
        assertEquals(2 * 3 + 6, AzathothDream.chaosGain(true, 8, 2, 3))
        assertEquals(0, AzathothDream.chaosGain(false, 13, 0, 3))
    }

    @Test
    fun `the chaos tree opens from the flutes`() {
        val none = emptyMap<String, Int>()
        assertTrue(AzathothDream.canBuy(ChaosNode.FLUTES, none, 1))
        assertFalse(AzathothDream.canBuy(ChaosNode.FLUTES, none, 0))
        assertFalse(AzathothDream.canBuy(ChaosNode.DAEMON, none, 99))
        assertTrue(AzathothDream.canBuy(ChaosNode.DAEMON, mapOf("flutes" to 1), 2))
        assertFalse(AzathothDream.canBuy(ChaosNode.FLUTES, mapOf("flutes" to 5), 999))
        assertEquals(listOf(1, 2, 4, 8, 16), (0 until 5).map { ChaosNode.FLUTES.cost(it) })
        assertEquals(40, ChaosNode.THRONE.cost(2))
        // Cada nodo pide uno que existe
        ChaosNode.entries.forEach { node -> node.requires?.let { assertTrue(ChaosNode.fromId(it) != null) } }
    }

    @Test
    fun `effects`() {
        assertEquals(1e4, AzathothDream.productionMultiplier(2), 1e-6)
        assertEquals(16.0, AzathothDream.visionMultiplier(4), 1e-12)
        assertEquals(1000.0, AzathothDream.etherMultiplier(3), 1e-9)
        assertEquals(8.0, AzathothDream.dancersMultiplier(3), 1e-12)
        assertEquals(1.05 * 1.05, AzathothDream.productionExponent(2), 1e-12)
        assertEquals(1, AzathothDream.requiredAwakenings(99))
    }

    @Test
    fun `awakening azathoth needs the throne and doubles its price`() {
        assertEquals(listOf(60, 120, 240), (0 until 3).map { AzathothDream.eyeCost(it) })
        assertFalse(AzathothDream.canAwakenAzathoth(throne = 0, chaos = 999, times = 0))
        assertFalse(AzathothDream.canAwakenAzathoth(throne = 1, chaos = 59, times = 0))
        assertTrue(AzathothDream.canAwakenAzathoth(throne = 1, chaos = 60, times = 0))
        assertFalse(AzathothDream.canAwakenAzathoth(throne = 1, chaos = 60, times = 1))
        assertEquals(1.0, AzathothDream.eyeMultiplier(0), 1e-12)
        assertEquals(1e6, AzathothDream.eyeMultiplier(2), 1e-3)
        assertTrue(AzathothDream.EPILOGUE.size >= 5)
    }

    @Test
    fun `the ending is reachable from the flutes`() {
        // El camino mínimo: Flautas, Caos Nuclear, Ecos, Trono y el Ojo
        val path = listOf(ChaosNode.FLUTES, ChaosNode.NUCLEAR, ChaosNode.ECHOES, ChaosNode.THRONE)
        var levels = emptyMap<String, Int>()
        var spent = 0
        path.forEach { node ->
            assertTrue(node.id, AzathothDream.canBuy(node, levels, 999))
            spent += node.cost(0)
            levels = levels + (node.id to 1)
        }
        assertEquals(77, spent + AzathothDream.eyeCost(0))
    }
}
