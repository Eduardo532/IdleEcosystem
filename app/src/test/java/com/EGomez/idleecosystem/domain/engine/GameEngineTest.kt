package com.EGomez.idleecosystem.domain.engine

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.EGomez.idleecosystem.data.database.AppDatabase
import com.EGomez.idleecosystem.data.preferences.GamePreferences
import com.EGomez.idleecosystem.data.repository.GameRepository
import com.EGomez.idleecosystem.data.repository.SaveCoordinator
import com.EGomez.idleecosystem.domain.logic.AchievementManager
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import com.EGomez.idleecosystem.domain.logic.BuildingManager
import com.EGomez.idleecosystem.domain.logic.AutomationSettings
import com.EGomez.idleecosystem.domain.logic.BuildingMilestones
import com.EGomez.idleecosystem.domain.logic.BuildingSynergies
import com.EGomez.idleecosystem.domain.logic.PrestigeManager
import com.EGomez.idleecosystem.domain.logic.PrestigeShopManager
import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import com.EGomez.idleecosystem.domain.logic.AchievementStat
import com.EGomez.idleecosystem.domain.logic.RandomEventManager
import com.EGomez.idleecosystem.domain.logic.RitualManager
import com.EGomez.idleecosystem.domain.logic.RuneManager
import com.EGomez.idleecosystem.domain.logic.SaveManager
import com.EGomez.idleecosystem.domain.logic.SoundManager
import com.EGomez.idleecosystem.domain.logic.StatisticsManager
import com.EGomez.idleecosystem.domain.logic.TutorialManager
import com.EGomez.idleecosystem.domain.logic.UpgradeManager
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.domain.model.TidePhase
import com.EGomez.idleecosystem.domain.logic.FullSaveData
import com.EGomez.idleecosystem.domain.model.RitualState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.util.concurrent.Executors

/**
 * El motor completo, con managers reales, una base de datos Room en memoria y su
 * propio hilo, como corre en el teléfono. Comprueba que las acciones se aplican, que
 * la UI recibe copias y que el guardado en segundo plano llega a la base de datos.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class GameEngineTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: GameRepository
    private lateinit var engineDispatcher: ExecutorCoroutineDispatcher
    private lateinit var scope: CoroutineScope
    private lateinit var engine: GameEngine
    private lateinit var stats: StatisticsManager
    private lateinit var shop: PrestigeShopManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = GameRepository(database.gameDao(), database)
        val preferences = GamePreferences(context)
        stats = StatisticsManager()
        val sound = SoundManager(context, preferences)

        engineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        scope = CoroutineScope(SupervisorJob())
        engine = GameEngine(
            buildingManager = BuildingManager(BuildingCalculator()),
            upgradeManager = UpgradeManager(),
            prestigeManager = PrestigeManager(),
            achievementManager = AchievementManager(),
            prestigeShopManager = PrestigeShopManager().also { shop = it },
            saveManager = SaveManager(),
            soundManager = sound,
            statsManager = stats,
            ritualManager = RitualManager(),
            runeManager = RuneManager(stats, sound),
            tutorialManager = TutorialManager(),
            eventScheduler = EventScheduler(RandomEventManager()),
            saveCoordinator = SaveCoordinator(repository, preferences),
            dispatcher = engineDispatcher
        )
        engine.start(scope)
        runBlocking {
            // La carga termina cuando se publica el primer recálculo
            await(engine.achievementProgress) { it.total > 0 }
        }
        // Una partida nueva abre el tutorial y el tutorial pausa el bucle
        engine.setTutorialEnabled(false)
    }

    @After
    fun tearDown() {
        scope.cancel()
        engineDispatcher.close()
        database.close()
    }

    @Test
    fun `clicks add knowledge`() = runBlocking {
        repeat(20) { engine.click(echoFound = false) }

        val economy = await(engine.economy) { it.knowledge >= BigDecimal(20) }
        assertTrue(economy.knowledge >= BigDecimal(20))
    }

    @Test
    fun `buying a building publishes a new copy and is saved`() = runBlocking {
        engine.grantKnowledge(BigDecimal(1_000))
        await(engine.economy) { it.knowledge >= BigDecimal(1_000) }
        val before = engine.buildings.value

        engine.buyBuilding(NOTES, 1)
        await(engine.buildings) { list -> list.any { it.id == NOTES && it.amountOwned == 1 } }

        // La lista que ya tenía la UI no cambió por debajo
        assertEquals(0, before.first { it.id == NOTES }.amountOwned)

        engine.save()
        val saved = eventually { repository.loadBuildings().find { it.buildingId == NOTES }?.takeIf { it.amountOwned == 1 } }
        assertEquals(1, saved.amountOwned)
    }

    @Test
    fun `ascending resets the run and replaces the save`() = runBlocking {
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }
        engine.buyBuilding(NOTES, 1)
        await(engine.buildings) { list -> list.any { it.id == NOTES && it.amountOwned == 1 } }

        engine.ascend()

        val economy = await(engine.economy) { it.sanity.signum() > 0 }
        // 1e7 generados / 1e6 = 10 -> raíz entera 3, x1,25 por terminar la vida sin pacto
        assertEquals(0, BigDecimal("3.75").compareTo(economy.sanity))
        assertEquals(0, engine.buildings.value.first { it.id == NOTES }.amountOwned)

        val state = eventually {
            repository.gameState.first()?.takeIf { BigDecimal(it.prestigeCurrency).compareTo(BigDecimal("3.75")) == 0 }
        }
        assertEquals(0, BigDecimal("3.75").compareTo(BigDecimal(state.prestigeCurrency)))
        val notes = eventually { repository.loadBuildings().find { it.buildingId == NOTES }?.takeIf { it.amountOwned == 0 } }
        assertEquals(0, notes.amountOwned)
    }

    @Test
    fun `the knowledge offering grants two minutes of base production`() = runBlocking {
        engine.grantKnowledge(BigDecimal(1_000))
        await(engine.economy) { it.knowledge >= BigDecimal(1_000) }
        engine.buyBuilding(NOTES, 10)
        val production = await(engine.economy) { it.productionPerSecond.signum() > 0 }.productionPerSecond

        repeat(25) { engine.click(echoFound = true) }
        val before = await(engine.economy) { it.echoes >= BigDecimal(25) }.knowledge

        engine.activateRitual(OFFERING)
        await(engine.rituals) { list -> list.first { it.id == OFFERING }.state != RitualState.READY }
        val after = await(engine.economy) { it.echoes.signum() == 0 }.knowledge

        // Al menos 120 s de producción (el bucle sigue sumando mientras tanto)
        assertTrue(after.subtract(before) >= production.multiply(BigDecimal(120)))
    }

    @Test
    fun `a grimoire rung can only be bought once its building count is reached`() = runBlocking {
        val rungId = "u_notes_at_25"
        engine.grantKnowledge(BigDecimal("1000000"))
        await(engine.economy) { it.knowledge >= BigDecimal("1000000") }

        // Con 0 Notas la mejora no aparece y no se puede comprar
        assertTrue(engine.upgrades.value.none { it.id == rungId })
        engine.buyUpgrade(rungId)
        engine.buyBuilding(NOTES, 25)
        await(engine.upgrades) { list -> list.any { it.id == rungId } }
        assertTrue(engine.upgrades.value.first { it.id == rungId }.isBought.not())

        engine.buyUpgrade(rungId)
        await(engine.upgrades) { list -> list.any { it.id == rungId && it.isBought } }
        Unit // JUnit exige que el test devuelva void
    }

    @Test
    fun `buy all purchases every affordable upgrade from the cheapest`() = runBlocking {
        engine.grantKnowledge(BigDecimal(1_000))
        await(engine.economy) { it.knowledge >= BigDecimal(1_000) }

        // Visibles al inicio: Tinta de Calamar (250) y Pluma de Cuervo (500) suman 750
        engine.buyAllAffordableUpgrades()
        val bought = await(engine.upgrades) { list -> list.count { it.isBought } >= 2 }
        assertTrue(bought.first { it.id == "u_notes_1" }.isBought)
        assertTrue(bought.first { it.id == "u_click_1" }.isBought)
        val economy = await(engine.economy) { it.knowledge < BigDecimal(1_000) }
        assertTrue(economy.knowledge < BigDecimal(300))
    }

    @Test
    fun `buying to the next milestone stops exactly on it`() = runBlocking {
        engine.grantKnowledge(BigDecimal(100_000))
        await(engine.economy) { it.knowledge >= BigDecimal(100_000) }

        // Notas: el primer hito es el logro de 10 y el siguiente, la escalera de 25
        engine.buyBuilding(NOTES, BuildingMilestones.BUY_TO_MILESTONE)
        await(engine.buildings) { list -> list.any { it.id == NOTES && it.amountOwned == 10 } }
        engine.buyBuilding(NOTES, BuildingMilestones.BUY_TO_MILESTONE)
        await(engine.buildings) { list -> list.any { it.id == NOTES && it.amountOwned == 25 } }
        Unit // JUnit exige que el test devuelva void
    }

    @Test
    fun `the next goal follows the most advanced achievement`() = runBlocking {
        // Partida nueva: la primera meta es el primer logro de Conocimiento
        assertEquals("k_1", await(engine.nextGoal) { it != null }!!.achievementId)

        engine.grantKnowledge(BigDecimal(1_000))
        await(engine.economy) { it.knowledge >= BigDecimal(1_000) }
        engine.buyBuilding(NOTES, 9)
        // 9 de 10 Notas (90 %) supera a cualquier meta de Conocimiento
        val goal = await(engine.nextGoal) { it?.achievementId == "b_not_1" }!!
        assertTrue(goal.progress >= 0.9f)
    }

    @Test
    fun `a synergy boosts the next building by one percent per unit`() = runBlocking {
        engine.grantKnowledge(BigDecimal("1e9"))
        await(engine.economy) { it.knowledge >= BigDecimal("1e9") }
        engine.buyBuilding(NOTES, 50)
        engine.buyBuilding(SCROLLS, 50)
        val synergyId = BuildingSynergies.idFor(NOTES, SCROLLS)
        await(engine.upgrades) { list -> list.any { it.id == synergyId } }

        fun ratio(list: List<com.EGomez.idleecosystem.domain.model.Building>): BigDecimal {
            val notes = list.first { it.id == NOTES }.currentMultiplier
            val scrolls = list.first { it.id == SCROLLS }.currentMultiplier
            return scrolls.divide(notes, java.math.MathContext.DECIMAL64)
        }
        // Los Pergaminos se publican al hacerse visibles (en el siguiente segundo)
        val before = ratio(await(engine.buildings) { list -> list.any { it.id == SCROLLS && it.amountOwned == 50 } })

        engine.buyUpgrade(synergyId)
        // 50 Notas -> +50 % solo para los Pergaminos
        val expected = before.multiply(BigDecimal("1.5"))
        await(engine.buildings) { list -> ratio(list).subtract(expected).abs() < BigDecimal("0.0001") }
        Unit // JUnit exige que el test devuelva void
    }

    @Test
    fun `the third ascension keeps click upgrades and starts with ten synapses`() = runBlocking {
        stats.totalPrestiges = 2 // la próxima es la tercera
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }
        engine.buyUpgrade("u_click_1")
        engine.buyUpgrade("u_notes_1")
        await(engine.upgrades) { list -> list.count { it.isBought } == 2 }

        engine.ascend()

        val buildings = await(engine.buildings) { list -> list.any { it.id == SYNAPSE && it.amountOwned == 10 } }
        assertEquals(0, buildings.first { it.id == NOTES }.amountOwned)
        val upgrades = await(engine.upgrades) { list -> list.none { it.id == "u_notes_1" && it.isBought } }
        assertTrue(upgrades.first { it.id == "u_click_1" }.isBought)
    }

    @Test
    fun `the building autobuyer works from five ascensions`() = runBlocking {
        stats.totalPrestiges = 5
        engine.grantKnowledge(BigDecimal(1_000))

        // Sin tocar nada, compra edificios con la mitad del Conocimiento como tope
        val list = await(engine.buildings) { list -> list.any { it.id == NOTES && it.amountOwned > 0 } }
        assertTrue(list.first { it.id == NOTES }.amountOwned > 0)
        assertTrue(engine.economy.value.knowledge > BigDecimal(400))
    }

    @Test
    fun `the autobuyer stays off when the player turns it off`() = runBlocking {
        stats.totalPrestiges = 5
        engine.setAutomation(AutomationSettings(buildings = false, grimoire = false))
        engine.grantKnowledge(BigDecimal(1_000))
        await(engine.economy) { it.knowledge >= BigDecimal(1_000) }

        delay(2_500)
        assertTrue(engine.buildings.value.all { it.amountOwned == 0 })
    }

    @Test
    fun `a chosen ritual activates on its own from fifty ascensions`() = runBlocking {
        stats.totalPrestiges = 50
        engine.setAutomation(AutomationSettings(buildings = false, grimoire = false, ritualId = FRENZY))
        repeat(5) { engine.click(echoFound = true) }

        await(engine.rituals) { list -> list.first { it.id == FRENZY }.state != RitualState.READY }
        Unit // JUnit exige que el test devuelva void
    }

    @Test
    fun `the autobuyer skips the buildings the player unchecked`() = runBlocking {
        stats.totalPrestiges = 5
        engine.setAutomation(AutomationSettings(grimoire = false, excludedBuildings = setOf(NOTES)))
        engine.grantKnowledge(BigDecimal(1_000))

        await(engine.buildings) { list -> list.any { it.id == SYNAPSE && it.amountOwned > 0 } }
        assertEquals(0, engine.buildings.value.first { it.id == NOTES }.amountOwned)
    }

    @Test
    fun `automatic ascension happens when the gain doubles the sanity`() = runBlocking {
        stats.totalPrestiges = 10
        engine.setAutomation(AutomationSettings(buildings = false, grimoire = false, autoAscend = true))
        // 9M generados -> 3 puntos de Cordura (x1,25 sin pacto), más que los 0 que hay
        engine.grantKnowledge(BigDecimal("9000000"))

        val economy = await(engine.economy) { it.sanity.signum() > 0 }
        assertEquals(0, BigDecimal("3.75").compareTo(economy.sanity))
        assertTrue(economy.knowledge < BigDecimal("9000000"))
    }

    @Test
    fun `ascending seals the chosen pact and saves it`() = runBlocking {
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }

        engine.ascend(Pact.HASTUR)

        val economy = await(engine.economy) { it.activePact == Pact.HASTUR }
        assertEquals(Pact.HASTUR, economy.activePact)
        val state = eventually { repository.gameState.first()?.takeIf { it.activePact == "hastur" } }
        assertEquals("hastur", state.activePact)
    }

    @Test
    fun `abandoning the path on ascension refunds its sanity`() = runBlocking {
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }
        // Senda de la Mano a nivel 2 (7.000 de Cordura gastados); el motor la lee al Ascender
        var id: String? = MadnessPaths.HAND
        while (id != null) {
            val node = shop.upgrades.first { it.id == id }
            node.level = if (id == MadnessPaths.HAND) 2 else maxOf(node.level, 1)
            id = node.parentId
        }

        engine.ascend(abandonPath = true)

        val economy = await(engine.economy) { it.sanity.signum() > 0 }
        // 3,75 de esta vida + 2.000 + 5.000 devueltos
        assertEquals(0, BigDecimal("7003.75").compareTo(economy.sanity))
        val hand = await(engine.prestigeUpgrades) { list -> list.any { it.id == MadnessPaths.HAND && it.level == 0 } }
        assertEquals(0, hand.first { it.id == MadnessPaths.HAND }.level)
    }

    @Test
    fun `ascending counts the finished life for the achievements and saves it`() = runBlocking {
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }

        engine.ascend(Pact.HASTUR)

        // La vida que terminó fue Sin Pacto: cuenta para "El Escéptico" y para el primer umbral
        val achievements = await(engine.achievements) { list -> list.any { it.id == "a_asc_1" && it.isUnlocked } }
        assertTrue(achievements.first { it.id == "a_asc_1" }.isUnlocked)
        assertEquals(0, BigDecimal.ONE.compareTo(stats.counter(AchievementStat.life("none"))))
        engine.save()
        val saved = eventually { repository.gameStatistics.first()?.takeIf { it.counters.contains("life.none=1") } }
        assertTrue(saved.counters.contains("life.none=1"))
    }

    @Test
    fun `a trial starts on ascension, changes the rules and its reward stays`() = runBlocking {
        stats.totalPrestiges = 2 // la Ascensión que viene es la tercera: se abren las Pruebas
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }

        engine.ascend(nextTrial = Trial.HUNGER)

        // Hambre: los edificios cuestan x10
        val started = await(engine.economy) { it.activeTrial == Trial.HUNGER }
        assertEquals(10.0, started.buildingCostMultiplier, 1e-9)

        // Al llegar a la meta (1B en esta vida) se supera y la regla se levanta
        engine.grantKnowledge(BigDecimal("2000000000"))
        val passed = await(engine.economy) { Trial.HUNGER in it.completedTrials }
        assertEquals(null, passed.activeTrial)
        assertEquals(0.9, passed.buildingCostMultiplier, 1e-9)
        assertTrue(Pact.SHUB_NIGGURATH in passed.earlyPacts)
    }

    @Test
    fun `a caught rune can be engraved in a slot won by a trial`() = runBlocking {
        // Partida importada: Ceguera superada (1 espacio) y la Runa de Hueso atrapada 10 veces
        val saved = StatisticsManager().apply {
            totalPrestiges = 3
            flag(AchievementStat.trial("blindness"))
            count(AchievementStat.runeCatches("t1_bone"), BigDecimal(10))
            count(AchievementStat.runeCatches("t2_gravity"), BigDecimal(2))
        }.getEntityForSave()
        engine.importSave(
            FullSaveData(
                knowledge = BigDecimal(100), sanity = BigDecimal.ZERO, echoes = BigDecimal.ZERO,
                buildings = emptyList(), upgrades = emptyList(), achievements = emptyList(),
                prestigeUpgrades = emptyList(), timestamp = 0L, statistics = saved
            )
        )
        await(engine.economy) { it.runeSlots == 1 }

        engine.engraveRune("t2_gravity") // solo 2 de 5: no se puede
        engine.engraveRune("t1_bone")

        // La Runa de Hueso grabada: los edificios cuestan un 5 % menos
        val after = await(engine.economy) { it.engravedRunes == listOf("t1_bone") }
        assertEquals(0.95, after.buildingCostMultiplier, 1e-9)

        engine.removeEngraving("t1_bone")
        val removed = await(engine.economy) { it.engravedRunes.isEmpty() }
        assertEquals(1.0, removed.buildingCostMultiplier, 1e-9)
    }

    @Test
    fun `madness rises with clicks and collapses at 100`() = runBlocking {
        // Oculto hasta la quinta Ascensión
        assertEquals(null, engine.economy.value.madness)
        stats.totalPrestiges = 5
        engine.click(false)
        val rising = await(engine.economy) { (it.madness ?: 0.0) > 0.0 }
        assertTrue(rising.globalMultiplier.signum() >= 0)

        // 5.100 clics (x0,02) llenan el medidor aunque baje 0,2 por segundo
        repeat(5_100) { engine.click(false) }
        val collapsed = await(engine.economy) { it.collapseSeconds > 0 }
        assertTrue((collapsed.madness ?: 100.0) < 10.0)
        assertEquals(0, BigDecimal.ONE.compareTo(stats.counter(AchievementStat.MADNESS_COLLAPSES)))
    }

    @Test
    fun `bound hands starts with notes so the life can move without clicks`() = runBlocking {
        stats.totalPrestiges = 2
        // Legado del Culto a nivel 1: +5 Notas en cada vida (la Prueba da 10, se queda el mayor)
        shop.upgrades.first { it.id == "pu_core" }.level = 1
        shop.upgrades.first { it.id == PrestigeShopManager.STARTING_NOTES_ID }.level = 1
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }

        engine.ascend(nextTrial = Trial.BOUND_HANDS)

        val buildings = await(engine.buildings) { list -> list.any { it.id == "b_notes" && it.amountOwned == 10 } }
        assertTrue(buildings.first { it.id == "b_scrolls" }.isVisible)
        val economy = await(engine.economy) { it.activeTrial == Trial.BOUND_HANDS && it.productionPerSecond.signum() > 0 }
        assertTrue(economy.productionPerSecond.signum() > 0)
    }

    @Test
    fun `the legacy of the cult gives notes at the start of every life`() = runBlocking {
        shop.upgrades.first { it.id == "pu_core" }.level = 1
        shop.upgrades.first { it.id == PrestigeShopManager.STARTING_NOTES_ID }.level = 2
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }

        engine.ascend()

        val buildings = await(engine.buildings) { list -> list.any { it.id == "b_notes" && it.amountOwned == 10 } }
        assertEquals(10, buildings.first { it.id == "b_notes" }.amountOwned)
    }

    @Test
    fun `the tide appears with the tenth ascension and follows play time`() = runBlocking {
        assertEquals(null, engine.economy.value.tide)
        stats.totalPrestiges = 10
        stats.totalPlayTimeSeconds = 650 // ya en la Marea Baja
        val economy = await(engine.economy) { it.tide != null }
        assertEquals(TidePhase.LOW, economy.tide)
        assertTrue(economy.tideSecondsLeft in 1..600)
    }

    @Test
    fun `abandoning a trial lifts its rules without a reward`() = runBlocking {
        stats.totalPrestiges = 2
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }
        engine.ascend(nextTrial = Trial.BLINDNESS)
        await(engine.economy) { it.activeTrial == Trial.BLINDNESS }

        engine.abandonTrial()

        val economy = await(engine.economy) { it.activeTrial == null }
        assertTrue(economy.completedTrials.isEmpty())
    }

    @Test
    fun `a locked god cannot be chosen`() = runBlocking {
        engine.grantKnowledge(BigDecimal("10000000"))
        await(engine.economy) { it.canPrestige }

        // Azathoth pide 50 Ascensiones: la primera vida nueva queda sin pacto
        engine.ascend(Pact.AZATHOTH)

        val economy = await(engine.economy) { it.sanity.signum() > 0 }
        assertEquals(Pact.NONE, economy.activePact)
    }

    @Test
    fun `attention boosts the building it watches`() = runBlocking {
        stats.totalPrestiges = 3 // 2 puntos de Atención
        engine.setAutomation(AutomationSettings(buildings = false, grimoire = false))
        engine.grantKnowledge(BigDecimal(10_000))
        await(engine.economy) { it.knowledge >= BigDecimal(10_000) }
        engine.buyBuilding(NOTES, 1)
        engine.buyBuilding(SCROLLS, 1)
        fun ratio(list: List<com.EGomez.idleecosystem.domain.model.Building>) =
            list.first { it.id == NOTES }.currentMultiplier.divide(list.first { it.id == SCROLLS }.currentMultiplier, java.math.MathContext.DECIMAL64)
        val before = ratio(await(engine.buildings) { list -> list.any { it.id == SCROLLS && it.amountOwned == 1 } })

        // Se piden 5 puntos, pero solo hay 2: las Notas reciben +50 %
        engine.setAttention(mapOf(NOTES to 5))
        val expected = before.multiply(BigDecimal("1.5"))
        await(engine.buildings) { list -> ratio(list).subtract(expected).abs() < BigDecimal("0.0001") }
        // Los edificios se publican antes que la economía: se espera a que llegue
        val economy = await(engine.economy) { it.attention == mapOf(NOTES to 2) }
        assertEquals(mapOf(NOTES to 2), economy.attention)
    }

    private suspend fun <T> await(flow: Flow<T>, predicate: (T) -> Boolean): T =
        withTimeout(TIMEOUT_MS) { flow.first(predicate) }

    /** Espera a que el guardado en segundo plano llegue a la base de datos. */
    private suspend fun <T : Any> eventually(read: suspend () -> T?): T =
        withTimeout(TIMEOUT_MS) {
            var value = read()
            while (value == null) {
                delay(50)
                value = read()
            }
            value
        }

    private companion object {
        const val NOTES = "b_notes"
        const val SCROLLS = "b_scrolls"
        const val SYNAPSE = "b_synapse"
        const val FRENZY = "rit_blood_frenzy"
        const val OFFERING = "rit_knowledge_offering"
        const val TIMEOUT_MS = 10_000L
    }
}
