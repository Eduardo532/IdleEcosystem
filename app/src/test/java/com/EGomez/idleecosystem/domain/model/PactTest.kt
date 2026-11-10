package com.EGomez.idleecosystem.domain.model

import com.EGomez.idleecosystem.domain.logic.BuildingCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PactTest {

    @Test
    fun `the pantheon has no pact, nine great old ones and four outer gods`() {
        assertEquals(1, Pact.entries.count { it.tier == PactTier.NONE })
        assertEquals(9, Pact.entries.count { it.tier == PactTier.GREAT_OLD_ONE })
        assertEquals(
            setOf("Azathoth", "Yog-Sothoth", "Shub-Niggurath", "Nyarlathotep"),
            Pact.entries.filter { it.tier == PactTier.OUTER_GOD }.map { it.godName }.toSet()
        )
        assertEquals(Pact.entries.size, Pact.entries.map { it.id }.toSet().size)
    }

    @Test
    fun `some great old ones come with the first ascension and the outer gods in the late game`() {
        assertEquals(0, Pact.NONE.unlockAscensions)
        val firstWave = Pact.available(1).filter { it.tier == PactTier.GREAT_OLD_ONE }
        assertTrue(firstWave.size in 2..4)
        assertTrue(Pact.entries.filter { it.tier == PactTier.GREAT_OLD_ONE }.any { it.unlockAscensions > 1 })
        val lastGreatOldOne = Pact.entries.filter { it.tier == PactTier.GREAT_OLD_ONE }.maxOf { it.unlockAscensions }
        assertTrue(Pact.entries.filter { it.tier == PactTier.OUTER_GOD }.all { it.unlockAscensions > lastGreatOldOne })
    }

    @Test
    fun `every pact but no pact has an advantage and a price`() {
        Pact.entries.filter { it != Pact.NONE }.forEach { pact ->
            val e = pact.effects
            val numbers = listOf(e.production, e.click, e.offline, e.runeFrequency, e.echoChance, e.sanityGain, e.eventFrequency, e.eventBuffPower, e.ritualDuration) +
                e.buildings.values
            val costs = listOf(e.ritualCost, e.ritualCooldown, e.buildingCost)
            val hasUpside = e.chaos || numbers.any { it > 1.0 } || costs.any { it < 1.0 }
            val hasDownside = e.chaos || numbers.any { it < 1.0 } || costs.any { it > 1.0 }
            assertTrue("${pact.godName} sin ventaja", hasUpside)
            assertTrue("${pact.godName} sin precio", hasDownside)
        }
        // Sin pacto solo da Cordura
        assertEquals(1.25, Pact.NONE.effects.sanityGain, 0.0)
    }

    @Test
    fun `pacts only name buildings that exist`() {
        val ids = BuildingCatalog.create().map { it.id }.toSet()
        Pact.entries.forEach { pact -> assertTrue(pact.godName, ids.containsAll(pact.effects.buildings.keys)) }
    }

    @Test
    fun `unknown or empty ids fall back to no pact`() {
        assertEquals(Pact.NONE, Pact.fromId(""))
        assertEquals(Pact.NONE, Pact.fromId(null))
        assertEquals(Pact.HASTUR, Pact.fromId("hastur"))
    }

    @Test
    fun `event boosts scale with the pact`() {
        assertEquals(9.0, Pact.GHROTH.effects.eventBuff(5.0), 0.0001)
        assertEquals(5.0, Pact.NONE.effects.eventBuff(5.0), 0.0001)
    }
}
