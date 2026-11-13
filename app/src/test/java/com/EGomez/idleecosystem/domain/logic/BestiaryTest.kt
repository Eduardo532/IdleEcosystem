package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.ui.components.ArtSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BestiaryTest {

    private val archetypes = setOf(
        "humanoid", "figure", "fishman", "ghoul", "blob", "worm", "bat", "bird", "hound", "goat", "cat", "rat", "serpent", "insect",
        "squid", "jelly", "tree", "eye", "flame", "crystal", "star", "cone", "barrel", "mask", "book", "swarm", "golem",
        "colossus", "planet", "vortex", "wraith", "toad", "spider", "mouth", "crab", "fish"
    )

    @Test
    fun `there are 312 creatures in 8 domains of 39 with the same rarities`() {
        assertEquals(312, Bestiary.beasts.size)
        assertEquals(8, Bestiary.domains.size)
        Bestiary.domains.forEach { domain ->
            val beasts = Bestiary.beastsOf(domain.id)
            assertEquals(domain.id, 39, beasts.size)
            assertEquals(domain.id, listOf(16, 11, 7, 3, 2), BeastRarity.entries.map { r -> beasts.count { it.rarity == r } })
        }
        assertEquals(Bestiary.beasts.size, Bestiary.beasts.map { it.id }.toSet().size)
        assertEquals(Bestiary.beasts.size, Bestiary.beasts.map { it.name }.toSet().size)
        assertEquals(BeastKind.entries.toSet(), Bestiary.domains.map { it.kind }.toSet())
    }

    @Test
    fun `every creature has a drawing recipe with a known archetype`() {
        Bestiary.beasts.forEach { beast ->
            val spec = ArtSpec.parse(beast.art)
            assertTrue(beast.id + ": " + beast.art, spec.archetype in archetypes)
        }
        // Las recetas no se repiten demasiado: cada dibujo varía además con el id
        assertTrue(Bestiary.beasts.map { it.art }.toSet().size > 200)
    }

    @Test
    fun `the gacha weights favor commons and the guarantees always pay`() {
        val random = Random(42)
        val rolls = (1..20_000).map { Bestiary.rollRarity(0, 0, random) }
        val commons = rolls.count { it == BeastRarity.COMMON }
        val primordials = rolls.count { it == BeastRarity.PRIMORDIAL }
        assertTrue(primordials in 1 until commons / 10)
        repeat(200) {
            assertTrue(Bestiary.rollRarity(Bestiary.PITY_MYTHIC - 1, 0, random) >= BeastRarity.MYTHIC)
            assertEquals(BeastRarity.PRIMORDIAL, Bestiary.rollRarity(0, Bestiary.PITY_PRIMORDIAL - 1, random))
        }
    }

    @Test
    fun `duplicates level up and give shards at the top`() {
        val beast = Bestiary.beasts.first { it.rarity == BeastRarity.RARE }
        assertEquals(SummonResult(beast.id, true, 1, 0), Bestiary.receive(beast, 0))
        assertEquals(SummonResult(beast.id, false, 4, 0), Bestiary.receive(beast, 3))
        assertEquals(SummonResult(beast.id, false, Bestiary.MAX_LEVEL, BeastRarity.RARE.shards), Bestiary.receive(beast, Bestiary.MAX_LEVEL))
        assertEquals(BeastRarity.RARE.shardCost, Bestiary.shardCost(beast))
    }

    @Test
    fun `each creature adds its rarity bonus per level to its domain kind`() {
        val dagon = Bestiary.beast("deep_dagon")!!
        assertEquals(BeastKind.PRODUCTION, Bestiary.domainOf(dagon).kind)
        assertEquals(1.15, Bestiary.bonuses(mapOf(dagon.id to 1)).getValue(BeastKind.PRODUCTION), 1e-12)
        assertEquals(1.30, Bestiary.bonuses(mapOf(dagon.id to 2)).getValue(BeastKind.PRODUCTION), 1e-12)
        assertEquals(1.15, Bestiary.effects(mapOf(dagon.id to 1)).production, 1e-12)
    }

    @Test
    fun `a complete domain doubles its bonus and seal bonuses are capped`() {
        val deep = Bestiary.beastsOf("deep").associate { it.id to 1 }
        assertTrue(Bestiary.isDomainComplete("deep", deep))
        val single = Bestiary.beastsOf("deep").sumOf { it.rarity.bonusPerLevel }
        assertEquals(1.0 + 2 * single, Bestiary.bonuses(deep).getValue(BeastKind.PRODUCTION), 1e-9)
        assertFalse(Bestiary.isDomainComplete("deep", deep - "deep_dagon"))
        val arcaneMax = Bestiary.beastsOf("arcane").associate { it.id to Bestiary.MAX_LEVEL }
        assertEquals(Bestiary.MAX_SEALS_BONUS, Bestiary.bonuses(arcaneMax).getValue(BeastKind.SEALS), 1e-12)
    }

    @Test
    fun `seal sources`() {
        assertEquals(1e5, Bestiary.sealPrice(0), 0.0)
        assertTrue(Bestiary.sealPrice(10) > Bestiary.sealPrice(9) * 2)
        assertEquals(15, Bestiary.trialSeals(Trial.entries.first(), firstTime = true))
        assertEquals(5 + 2 * (Trial.entries.size - 1), Bestiary.trialSeals(Trial.entries.last(), firstTime = false))
        assertEquals(1, Bestiary.ascensionSeals(7))
        assertEquals(6, Bestiary.ascensionSeals(20))
        assertEquals(Bestiary.DAILY_BASE, Bestiary.dailySeals(1))
        assertEquals(Bestiary.DAILY_MAX, Bestiary.dailySeals(100))
        assertTrue(Bestiary.CLICK_SEAL_CHANCE < com.EGomez.idleecosystem.domain.engine.GameEngine.BASE_ECHO_CHANCE)
    }

    @Test
    fun `encounters come sooner with the skies`() {
        val random = Random(7)
        repeat(50) {
            assertTrue(Bestiary.nextInterval(1.0, random) in Bestiary.MIN_INTERVAL..Bestiary.MAX_INTERVAL)
            assertTrue(Bestiary.nextInterval(2.0, random) <= Bestiary.MAX_INTERVAL / 2)
        }
        assertTrue(Bestiary.beasts.contains(Bestiary.rollEncounter(random)))
    }
}
