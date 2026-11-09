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

    @Test
    fun `every auto-click rune stays inside its tier click budget`() {
        // Incluye las runas que además dan producción (El Caos Reptante)
        allRunes.forEach { rune ->
            val effect = RuneEffects.forRune(rune.id)
            if (effect.clickEquivalents == 0L) return@forEach
            val budget = RuneEffects.clickBudgetFor(rune.tier)
            assertTrue(
                "${rune.name} entrega ${effect.clickEquivalents} clics; su nivel permite $budget",
                effect.clickEquivalents in budget
            )
        }
    }

    @Test
    fun `a tier 2 click rune is worth more than a tier 1 one`() {
        // Antes la Runa de Susurro (nivel 1, x10 + autoclic 30 s = 3.000 clics) valía
        // quince veces más que la Runa del Tiempo (nivel 2, autoclic 20 s = 200 clics)
        val whisper = RuneEffects.forRune("t1_whisper").clickEquivalents
        val chronos = RuneEffects.forRune("t2_chronos").clickEquivalents
        assertTrue(chronos > whisper)
    }

    @Test
    fun `click descriptions show the real click multiplier`() {
        allRunes.forEach { rune ->
            val effect = RuneEffects.forRune(rune.id)
            if (effect.clickMultiplier > 1.0) {
                val shown = "x${effect.clickMultiplier.toInt()} Clic"
                assertTrue("'${rune.description}' debería decir $shown", rune.description.contains(shown))
            }
        }
    }

    @Test
    fun `the chaos rune auto-click is boosted by its own production multiplier`() {
        val chaos = RuneEffects.forRune("t3_nyarlathotep")
        assertEquals(20_000L, chaos.effectiveAutoClickMs)
        // 200 clics x25
        assertEquals(5_000L, chaos.clickEquivalents)
    }
}
