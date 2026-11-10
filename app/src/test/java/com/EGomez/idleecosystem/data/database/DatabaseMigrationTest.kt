package com.EGomez.idleecosystem.data.database

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Simula a un jugador de la v0.8.0 (base de datos v8) que actualiza a la versión actual.
 *
 * Crea una base v8 con el mismo esquema que generaba Room en la v0.8.0, la abre con
 * todas las migraciones y comprueba que Room valida el esquema (si una migración
 * olvida una columna, Room lanza IllegalStateException al abrir) y que la partida
 * se conserva intacta.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DatabaseMigrationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(DB_NAME)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DB_NAME)
    }

    @Test
    fun `v0_8_0 save migrates to the current schema without losing progress`() {
        createVersion8Database()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .addMigrations(*AppDatabase.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()

        try {
            val dao = db.gameDao()
            runBlocking {
                val state = dao.getGameStateOnce()
                assertNotNull(state)
                assertEquals("123456", state!!.money)
                assertEquals("7", state.prestigeCurrency)
                assertEquals("3", state.echoes)
                assertTrue(state.tutorialEnabled)
                assertEquals("", state.completedTutorialSteps)
                // v11: el pacto de la vida actual, vacío en partidas anteriores
                assertEquals("", state.activePact)

                assertEquals(12, dao.getAllBuildings().single { it.buildingId == "b_notes" }.amountOwned)
                assertTrue(dao.getAllUpgrades().single { it.upgradeId == "u_click_1" }.isBought)
                assertEquals(2, dao.getAllPrestigeUpgrades().single { it.id == "pu_core" }.level)
                assertTrue(dao.getAllAchievements().single { it.id == "ach_first" }.isUnlocked)
            }
        } finally {
            db.close()
        }
    }

    /** Esquema exacto que Room creaba en la v0.8.0 (base de datos versión 8). */
    private fun createVersion8Database() {
        val file = context.getDatabasePath(DB_NAME)
        file.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(file, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS `game_save` (`id` INTEGER NOT NULL, `money` TEXT NOT NULL, `premiumCurrency` TEXT NOT NULL, `prestigeCurrency` TEXT NOT NULL, `echoes` TEXT NOT NULL, `lastLoginTime` INTEGER NOT NULL, `totalPlayTime` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `buildings_save` (`buildingId` TEXT NOT NULL, `amountOwned` INTEGER NOT NULL, `isUnlocked` INTEGER NOT NULL, PRIMARY KEY(`buildingId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `upgrades_save` (`upgradeId` TEXT NOT NULL, `isBought` INTEGER NOT NULL, PRIMARY KEY(`upgradeId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `achievements_save` (`id` TEXT NOT NULL, `isUnlocked` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `prestige_upgrades` (`id` TEXT NOT NULL, `level` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `game_statistics` (`id` INTEGER NOT NULL, `totalPlayTimeSeconds` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `runTimeSeconds` INTEGER NOT NULL, `totalOfflineSeconds` INTEGER NOT NULL, `lifetimeKnowledge` TEXT NOT NULL, `runKnowledge` TEXT NOT NULL, `maxKnowledgePerSecond` TEXT NOT NULL, `totalClicks` INTEGER NOT NULL, `manualKnowledgeGenerated` TEXT NOT NULL, `totalPrestiges` INTEGER NOT NULL, `lifetimeSanity` TEXT NOT NULL, `spentSanity` TEXT NOT NULL, `totalEventsFound` INTEGER NOT NULL, `totalEchoes` TEXT NOT NULL, PRIMARY KEY(`id`))")

        db.execSQL("INSERT INTO game_save VALUES (1, '123456', '0', '7', '3', 1700000000000, 0)")
        db.execSQL("INSERT INTO buildings_save VALUES ('b_notes', 12, 1)")
        db.execSQL("INSERT INTO upgrades_save VALUES ('u_click_1', 1)")
        db.execSQL("INSERT INTO achievements_save VALUES ('ach_first', 1)")
        db.execSQL("INSERT INTO prestige_upgrades VALUES ('pu_core', 2)")
        db.execSQL("INSERT INTO game_statistics VALUES (1, 100, 1700000000000, 50, 10, '500', '400', '5', 30, '30', 1, '7', '0', 2, '3')")

        db.version = 8
        db.close()
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
