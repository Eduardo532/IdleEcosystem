package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Trial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BestiaryTest {

    @Test
    fun `there are 156 creatures in 13 families of 12`() {
        assertEquals(156, Bestiary.beasts.size)
        assertEquals(13, Bestiary.families.size)
        Bestiary.families.forEach { family ->
            val beasts = Bestiary.beastsOf(family.id)
            assertEquals(family.id, 12, beasts.size)
            assertEquals(family.id, 1, beasts.count { it.rarity == BeastRarity.PRIMORDIAL })
            assertEquals(family.id, 1, beasts.count { it.rarity == BeastRarity.MYTHIC })
        }
        assertEquals(Bestiary.beasts.size, Bestiary.beasts.map { it.id }.toSet().size)
        assertEquals(Bestiary.beasts.size, Bestiary.beasts.map { it.name }.toSet().size)
        assertTrue(Bestiary.beasts.all { Bestiary.family(it.familyId) != null })
    }

    @Test
    fun `each creature adds its rarity bonus per level to its family kind`() {
        assertEquals(1.0, Bestiary.bonuses(emptyMap()).getValue(BeastKind.PRODUCTION), 1e-12)
        val dagon = Bestiary.beast("deep_dagon")!!
        assertEquals(BeastKind.PRODUCTION, Bestiary.familyOf(dagon).kind)
        assertEquals(1.15, Bestiary.bonuses(mapOf(dagon.id to 1)).getValue(BeastKind.PRODUCTION), 1e-12)
        assertEquals(1.30, Bestiary.bonuses(mapOf(dagon.id to 2)).getValue(BeastKind.PRODUCTION), 1e-12)
        assertEquals(1.15, Bestiary.effects(mapOf(dagon.id to 1)).production, 1e-12)
    }

    @Test
    fun `a complete family doubles its bonus`() {
        val deep = Bestiary.beastsOf("deep").associate { it.id to 1 }
        assertTrue(Bestiary.isFamilyComplete("deep", deep))
        val single = Bestiary.beastsOf("deep").sumOf { it.rarity.bonusPerLevel }
        assertEquals(1.0 + 2 * single, Bestiary.bonuses(deep).getValue(BeastKind.PRODUCTION), 1e-9)
        assertFalse(Bestiary.isFamilyComplete("deep", deep - "deep_dagon"))
    }

    @Test
    fun `essence costs and rewards`() {
        val common = Bestiary.beasts.first { it.rarity == BeastRarity.COMMON }
        assertEquals(2, Bestiary.upgradeCost(common, 1))
        assertEquals(18, Bestiary.upgradeCost(common, 9))
        assertEquals(9, Bestiary.trialEssence(Trial.entries.first(), firstTime = true))
        assertEquals(3 + 2 * (Trial.entries.size - 1), Bestiary.trialEssence(Trial.entries.last(), firstTime = false))
    }

    @Test
    fun `encounters pick a creature and come sooner with the winged`() {
        val random = Random(7)
        repeat(200) { assertTrue(Bestiary.beasts.contains(Bestiary.rollEncounter(emptyMap(), random))) }
        repeat(50) {
            val normal = Bestiary.nextInterval(1.0, random)
            assertTrue(normal in Bestiary.MIN_INTERVAL..Bestiary.MAX_INTERVAL)
            assertTrue(Bestiary.nextInterval(2.0, random) <= Bestiary.MAX_INTERVAL / 2)
        }
    }

    @Test
    fun `commons are far more frequent than primordials`() {
        val random = Random(42)
        val rolls = (1..20_000).map { Bestiary.rollEncounter(emptyMap(), random).rarity }
        val commons = rolls.count { it == BeastRarity.COMMON }
        val primordials = rolls.count { it == BeastRarity.PRIMORDIAL }
        assertTrue(primordials in 1 until commons / 10)
    }
}
