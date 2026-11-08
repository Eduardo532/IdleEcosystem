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
import com.EGomez.idleecosystem.domain.logic.PrestigeManager
import com.EGomez.idleecosystem.domain.logic.PrestigeShopManager
import com.EGomez.idleecosystem.domain.logic.RandomEventManager
import com.EGomez.idleecosystem.domain.logic.RitualManager
import com.EGomez.idleecosystem.domain.logic.RuneManager
import com.EGomez.idleecosystem.domain.logic.SaveManager
import com.EGomez.idleecosystem.domain.logic.SoundManager
import com.EGomez.idleecosystem.domain.logic.StatisticsManager
import com.EGomez.idleecosystem.domain.logic.TutorialManager
import com.EGomez.idleecosystem.domain.logic.UpgradeManager
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

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = GameRepository(database.gameDao(), database)
        val preferences = GamePreferences(context)
        val stats = StatisticsManager()
        val sound = SoundManager(context, preferences)

        engineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        scope = CoroutineScope(SupervisorJob())
        engine = GameEngine(
            buildingManager = BuildingManager(BuildingCalculator()),
            upgradeManager = UpgradeManager(),
            prestigeManager = PrestigeManager(),
            achievementManager = AchievementManager(),
            prestigeShopManager = PrestigeShopManager(),
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
        // 1e7 generados / 1e6 = 10 -> raíz entera 3
        assertEquals(0, BigDecimal(3).compareTo(economy.sanity))
        assertEquals(0, engine.buildings.value.first { it.id == NOTES }.amountOwned)

        val state = eventually {
            repository.gameState.first()?.takeIf { BigDecimal(it.prestigeCurrency).compareTo(BigDecimal(3)) == 0 }
        }
        assertEquals(0, BigDecimal(3).compareTo(BigDecimal(state.prestigeCurrency)))
        val notes = eventually { repository.loadBuildings().find { it.buildingId == NOTES } }
        assertEquals(0, notes.amountOwned)
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
        const val TIMEOUT_MS = 10_000L
    }
}
