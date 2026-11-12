package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class TranscendenceMilestoneTest {

    @Test
    fun `milestones come in order of transcendences`() {
        val needed = TranscendenceMilestone.entries.map { it.transcendences }
        assertEquals(needed.sorted(), needed)
        assertEquals(needed.size, needed.toSet().size)
    }

    @Test
    fun `reached and next follow the transcendences`() {
        assertEquals(0, TranscendenceMilestone.reached(0))
        assertEquals(TranscendenceMilestone.MACHINE_MEMORY, TranscendenceMilestone.next(0))
        assertEquals(3, TranscendenceMilestone.reached(4))
        assertEquals(TranscendenceMilestone.AUTO_TRANSCEND, TranscendenceMilestone.next(4))
        assertEquals(TranscendenceMilestone.entries.size, TranscendenceMilestone.reached(100))
        assertNull(TranscendenceMilestone.next(100))
    }

    @Test
    fun `the shortcut lowers the ascensions transcending asks for`() {
        assertEquals(Transcendence.REQUIRED_ASCENSIONS, TranscendenceMilestone.requiredAscensions(11))
        assertEquals(TranscendenceMilestone.SHORTCUT_ASCENSIONS, TranscendenceMilestone.requiredAscensions(12))
        val sanity = BigDecimal(10_000)
        assertEquals(0, Transcendence.etherGain(40, sanity))
        assertTrue(Transcendence.etherGain(40, sanity, required = TranscendenceMilestone.SHORTCUT_ASCENSIONS) > 0)
    }

    @Test
    fun `automatic transcendence waits until the gain matches the ether held`() {
        assertFalse(TranscendenceMilestone.shouldAutoTranscend(0, 0))
        assertTrue(TranscendenceMilestone.shouldAutoTranscend(1, 0))
        assertFalse(TranscendenceMilestone.shouldAutoTranscend(9, 10))
        assertTrue(TranscendenceMilestone.shouldAutoTranscend(10, 10))
    }

    @Test
    fun `remembered sky lights the core and the first star of each constellation`() {
        val shop = PrestigeShopManager()
        shop.rememberSky()
        val lit = shop.upgrades.filter { it.level > 0 }
        assertTrue(lit.any { it.id == PrestigeShopManager.CORE_NODE_ID })
        assertTrue(lit.none { MadnessPaths.isPath(it.id) })
        assertEquals(
            com.EGomez.idleecosystem.domain.model.Constellation.entries.size,
            lit.count { it.parentId == PrestigeShopManager.CORE_NODE_ID }
        )
    }

    @Test
    fun `a path kept from the last cycle is not refunded`() {
        val shop = PrestigeShopManager()
        val hand = shop.upgrades.first { it.id == MadnessPaths.HAND }
        hand.level = 3
        val full = MadnessPaths.spent(hand)
        val keptTwo = MadnessPaths.spent(hand, 2)
        assertTrue(keptTwo < full)
        assertEquals(0, keptTwo.compareTo(shop.abandonPath(2)))
        assertEquals(0, hand.level)
    }
}
