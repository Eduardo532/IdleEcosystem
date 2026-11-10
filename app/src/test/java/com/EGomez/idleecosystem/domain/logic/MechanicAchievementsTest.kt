package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.AchievementType
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.Trial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext

/**
 * Los logros de las mecánicas (v0.17) y la regla del proyecto: **cada mecánica nueva
 * llega con una serie de varios logros**. Si se agrega una mecánica sin sus logros,
 * este test falla.
 */
class MechanicAchievementsTest {

    private val all = AchievementManager().availableAchievements
    private val mechanic = MechanicAchievements.create()

    /** Mecánicas del juego y cómo reconocer sus logros. */
    private val mechanics: Map<String, (Achievement) -> Boolean> = mapOf(
        "Ascensión" to { it.stat == AchievementStat.ASCENSIONS || it.stat == AchievementStat.SANITY || it.stat == AchievementStat.MAX_SANITY_GAIN },
        "Mapa de Locura" to { it.stat?.startsWith("map_") == true },
        "Sendas" to { it.path != null || it.stat == AchievementStat.PATH_LEVEL || it.stat == AchievementStat.PATHS_WALKED || it.stat == AchievementStat.PATHS_ABANDONED },
        "Atención" to { it.stat?.startsWith("attention_") == true },
        "Automatización" to { it.stat == AchievementStat.AUTO_ASCENSIONS || it.stat == AchievementStat.AUTO_UNITS },
        "Panteón" to { it.pact != null || it.stat?.startsWith("pacts_") == true },
        "Rituales" to { it.stat == AchievementStat.RITUALS || it.stat?.startsWith(AchievementStat.RITUAL_PREFIX) == true },
        "Convergencia" to { it.stat == AchievementStat.ritual(RitualManager.CONVERGENCE_ID) || it.stat?.startsWith("max.convergence") == true },
        "Runas" to { it.stat?.startsWith("runes") == true },
        "Eventos" to { it.stat == AchievementStat.EVENTS },
        "Ecos" to { it.stat?.startsWith("echoes_") == true },
        "Sinergias" to { it.stat == AchievementStat.SYNERGIES },
        "Conjuntos" to { it.stat == AchievementStat.SETS },
        "Grimorio" to { it.stat == AchievementStat.GRIMOIRE },
        "Espacios de Runa" to { it.stat?.startsWith("engrave") == true || it.stat == AchievementStat.RUNE_SLOTS || it.stat == AchievementStat.RUNE_KINDS || it.stat?.startsWith(AchievementStat.RUNE_CATCH_PREFIX) == true || it.stat == AchievementStat.ENGRAVED_T3 },
        "Medidor de Locura" to { it.stat == AchievementStat.MADNESS || it.stat?.contains("madness") == true || it.stat == AchievementStat.RUN_COLLAPSES || it.stat == AchievementStat.ritual(RitualManager.CALM_ID) },
        "Fases de la Marea" to { it.stat?.contains("tide") == true || it.stat == AchievementStat.FEAT_TSUNAMI },
        "Pruebas de Locura" to { it.stat?.startsWith("trial") == true || it.stat == AchievementStat.TRIALS_COMPLETED || it.stat == AchievementStat.FEAT_TRIAL_FAST }
    )

    @Test
    fun `every mechanic comes with a series of achievements`() {
        mechanics.forEach { (name, belongs) ->
            val count = mechanic.count(belongs)
            assertTrue("$name tiene $count logros; la regla pide al menos 3", count >= 3)
        }
    }

    @Test
    fun `every god has achievements of their own`() {
        Pact.entries.forEach { pact ->
            val own = mechanic.count { it.pact == pact.id || it.stat == AchievementStat.life(pact.id) }
            assertTrue("${pact.godName} tiene $own logros propios", own >= 2)
        }
        MadnessPaths.IDS.forEach { path ->
            assertTrue("$path sin logros", mechanic.count { it.path == path || it.stat == AchievementStat.lifeOnPath(path) } >= 4)
        }
    }

    @Test
    fun `ids are unique and every stat is understood by the engine`() {
        assertEquals(all.size, all.map { it.id }.toSet().size)
        mechanic.forEach { a ->
            assertEquals(AchievementType.STAT, a.conditionType)
            assertTrue("${a.id}: clave desconocida ${a.stat}", a.stat != null && AchievementStat.isKnown(a.stat!!))
            a.pact?.let { id -> assertTrue("${a.id}: pacto $id", Pact.entries.any { it.id == id }) }
            a.path?.let { assertTrue("${a.id}: Senda $it", it in MadnessPaths.IDS) }
            assertTrue("${a.id} sin requisito", a.requirement.isNotBlank())
        }
    }

    @Test
    fun `the new achievements give a small bonus`() {
        mechanic.forEach { a ->
            val allowed = if (a.isSecret) listOf("1.05") else listOf("1.01", "1.02")
            assertTrue("${a.id} da ${a.bonusMultiplier}", allowed.any { BigDecimal(it).compareTo(a.bonusMultiplier) == 0 })
        }
        // Crece con cada mecánica (la regla pide logros nuevos siempre): se limita el promedio
        val total = mechanic.fold(BigDecimal.ONE) { acc, a -> acc.multiply(a.bonusMultiplier, MathContext.DECIMAL64) }
        val average = Math.pow(total.toDouble(), 1.0 / mechanic.size)
        assertTrue("en promedio cada logro nuevo da x$average", average <= 1.02)
    }

    @Test
    fun `every trial has its own achievement`() {
        Trial.entries.forEach { trial ->
            assertTrue("${trial.title} sin logro", mechanic.any { it.stat == AchievementStat.trial(trial.id) })
        }
        assertEquals(0, BigDecimal(Trial.entries.size).compareTo(mechanic.filter { it.stat == AchievementStat.TRIALS_COMPLETED }.maxOf { it.targetValue }))
    }

    @Test
    fun `targets can actually be reached`() {
        fun target(stat: String) = mechanic.filter { it.stat == stat && it.pact == null }.maxOf { it.targetValue }
        val buildings = BuildingCatalog.create()
        assertTrue(target(AchievementStat.GRIMOIRE) <= BigDecimal(UpgradeManager().availableUpgrades.size))
        assertEquals(0, BigDecimal(PrestigeShopManager().upgrades.count { !MadnessPaths.isPath(it.id) }).compareTo(target(AchievementStat.MAP_NODES)))
        assertEquals(0, BigDecimal(buildings.size).compareTo(target(AchievementStat.BUILDING_TYPES)))
        assertEquals(0, BigDecimal(Pact.entries.size - 1).compareTo(target(AchievementStat.PACTS_UNLOCKED)))
        assertEquals(0, BigDecimal(Pact.entries.size).compareTo(target(AchievementStat.PACTS_LIVED)))
        assertEquals(0, BigDecimal(Attention.MAX_POINTS).compareTo(target(AchievementStat.ATTENTION_POINTS)))
        assertEquals(0, BigDecimal(BuffMath.CONVERGENCE_CAP).compareTo(target(AchievementStat.MAX_CONVERGENCE)))
    }

    @Test
    fun `pact achievements only count during a life with that god`() {
        val manager = AchievementManager()
        val temples = mapOf(AchievementStat.owned("b_temple") to BigDecimal(100))

        manager.checkAchievements(BigDecimal.ZERO, emptyList(), AchievementContext(temples, pactId = "hastur"))
        assertFalse(manager.availableAchievements.first { it.id == "a_cth_2" }.isUnlocked)

        val unlocked = manager.checkAchievements(BigDecimal.ZERO, emptyList(), AchievementContext(temples, pactId = "cthulhu"))
        assertTrue(unlocked.any { it.id == "a_cth_2" })
    }

    @Test
    fun `combo achievements need both the god and the path`() {
        val manager = AchievementManager()
        val clicks = mapOf(StatisticsManager.RUN_CLICKS to BigDecimal(100_000))

        manager.checkAchievements(BigDecimal.ZERO, emptyList(), AchievementContext(clicks, "nyarlathotep", MadnessPaths.GEARS))
        assertFalse(manager.availableAchievements.first { it.id == "a_cmb_1" }.isUnlocked)
        val unlocked = manager.checkAchievements(BigDecimal.ZERO, emptyList(), AchievementContext(clicks, "nyarlathotep", MadnessPaths.HAND))
        assertTrue(unlocked.any { it.id == "a_cmb_1" })
    }

    @Test
    fun `stat goals show progress in the next goal`() {
        val goals = listOf(MechanicAchievements.create().first { it.id == "a_asc_1" }.copy(order = 1))
        val goal = NextGoalFinder.find(goals, BigDecimal.ZERO, emptyList(), AchievementContext(mapOf(AchievementStat.ASCENSIONS to BigDecimal.ZERO)))
        assertEquals(0f, goal!!.progress)
    }
}
