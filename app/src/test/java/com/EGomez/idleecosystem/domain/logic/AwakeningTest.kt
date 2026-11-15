package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AwakeningTest {

    @Test
    fun `the layer shows at ten transcendences and stays after awakening`() {
        assertFalse(Awakening.isRevealed(9, 0))
        assertTrue(Awakening.isRevealed(10, 0))
        assertTrue(Awakening.isRevealed(0, 1))
    }

    @Test
    fun `awakening needs ten transcendences and five trial levels`() {
        assertFalse(Awakening.canAwaken(9, 20))
        assertFalse(Awakening.canAwaken(30, 4))
        assertTrue(Awakening.canAwaken(10, 5))
    }

    @Test
    fun `visions grow with transcendences and ether`() {
        assertEquals(0, Awakening.visionGain(9, 1e6))
        assertEquals(0, Awakening.visionGain(10, 0.0))
        // √(12 × log10(1001)) ≈ 6,0
        assertEquals(6, Awakening.visionGain(12, 1000.0))
        assertEquals(10, Awakening.visionGain(10, 9_999_999_999.0))
        // La Visión del Profeta: +25 % por nivel
        assertEquals(7, Awakening.visionGain(12, 1000.0, Awakening.visionMultiplier(1)))
    }

    @Test
    fun `milestones give transcendence milestones back`() {
        assertEquals(0, Awakening.milestoneBonus(0))
        assertEquals(Awakening.OPEN_EYES_BONUS, Awakening.milestoneBonus(1))
        assertEquals(Awakening.FULL_SIGHT_BONUS, Awakening.milestoneBonus(10))
        assertEquals(2, AwakeningMilestone.reached(2))
        assertTrue(AwakeningMilestone.entries.zipWithNext().all { (a, b) -> a.awakenings < b.awakenings })
        // Con el primer hito, la Trascendencia automática (5) ya está
        assertTrue(TranscendenceMilestone.AUTO_TRANSCEND.isReached(Awakening.milestoneBonus(1)))
    }

    @Test
    fun `the vision tree needs the parent node and refunds everything`() {
        val none = emptyMap<String, Int>()
        assertTrue(Awakening.canBuy(VisionNode.FIRST, none, 1))
        assertFalse(Awakening.canBuy(VisionNode.SANITY, none, 100))
        assertTrue(Awakening.canBuy(VisionNode.SANITY, mapOf("first" to 1), 100))
        val levels = mapOf("first" to 3, "ether" to 1)
        assertEquals(VisionNode.FIRST.spent(3) + VisionNode.ETHER.spent(1), Awakening.refund(levels))
        VisionNode.entries.forEachIndexed { index, node ->
            node.requires?.let { parent ->
                val required = VisionNode.fromId(parent)
                assertNotNull(required)
                assertTrue(required!!.ordinal < index)
            }
        }
    }

    @Test
    fun `vision effects`() {
        assertEquals(1000.0, Awakening.productionMultiplier(3), 1e-9)
        assertEquals(9.0, Awakening.sanityMultiplier(2), 1e-9)
        assertEquals(30, Awakening.startingAscensions(3))
        assertEquals(1.06, Awakening.productionExponent(3), 1e-12)
        assertEquals(30, Awakening.requiredAscensions(50, 4))
        assertEquals(10, Awakening.requiredAscensions(14, 4))
    }
}
