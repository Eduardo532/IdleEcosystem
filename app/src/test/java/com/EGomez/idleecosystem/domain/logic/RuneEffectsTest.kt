package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.RuneCatalog
import com.EGomez.idleecosystem.domain.model.RuneTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuneEffectsTest {

    private val allRunes = RuneTier.values().flatMap { RuneCatalog.getVariantsForTier(it) }

    @Test
    fun `every production rune stays inside its tier budget`() {
        allRunes.forEach { rune ->
            val effect = RuneEffects.forRune(rune.id)
            if (!effect.givesProduction) return@forEach
            val budget = RuneEffects.budgetFor(rune.tier)
            assertTrue(
                "${rune.name} entrega ${effect.productionSeconds}s; su nivel permite $budget",
                effect.productionSeconds in budget
            )
        }
    }

    @Test
    fun `higher tiers are always worth more than lower ones`() {
        val t1 = RuneEffects.budgetFor(RuneTier.TIER_1_SWIPE)
        val t2 = RuneEffects.budgetFor(RuneTier.TIER_2_HOLD)
        val t3 = RuneEffects.budgetFor(RuneTier.TIER_3_TRACE)
        assertTrue(t1.last < t2.first)
        assertTrue(t2.last < t3.first)
    }

    @Test
    fun `every catalog rune has an effect`() {
        allRunes.forEach { rune ->
            assertTrue("${rune.id} no hace nada", RuneEffects.forRune(rune.id) != RuneEffect() || rune.id == "t2_entropy")
        }
    }

    @Test
    fun `descriptions show the real multiplier`() {
        allRunes.forEach { rune ->
            val effect = RuneEffects.forRune(rune.id)
            if (effect.multiplier > 1.0) {
                val shown = "x${effect.multiplier.toInt()}"
                assertTrue("'${rune.description}' debería decir $shown", rune.description.contains(shown))
            }
        }
    }

    @Test
    fun `production seconds add the bonus over the base`() {
        // x10 durante 15 s = 9 x 15 = 135 s extra (los potenciadores se suman)
        assertEquals(135L, RuneEffect(multiplier = 10.0, durationMs = 15_000).productionSeconds)
        assertEquals(120L, RuneEffect(reward = RuneReward.TimeWarp(120)).productionSeconds)
        assertEquals(0L, RuneEffect(clickMultiplier = 10.0, durationMs = 30_000).productionSeconds)
    }

    @Test
    fun `the old sigil of cthulhu no longer equals hours of production`() {
        val cthulhu = RuneEffects.forRune("t3_cthulhu")
        // Antes: x777 durante 60 s = 46.560 s (casi 13 horas)
        assertTrue(cthulhu.productionSeconds <= 40 * 60)
    }
}
