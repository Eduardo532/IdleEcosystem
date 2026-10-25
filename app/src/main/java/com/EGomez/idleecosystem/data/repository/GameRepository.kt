package com.EGomez.idleecosystem.data.repository

import com.EGomez.idleecosystem.data.database.*
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/** Foto completa del progreso, para guardarla de una vez. */
data class GameSnapshot(
    val knowledge: BigDecimal,
    val sanity: BigDecimal,
    val echoes: BigDecimal,
    val buildings: List<BuildingEntity>,
    val upgrades: List<UpgradeEntity>,
    val unlockedAchievements: List<AchievementEntity>,
    val prestigeUpgrades: List<PrestigeUpgradeEntity>,
    val statistics: GameStatisticsEntity
)

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao
) {
    val gameState: Flow<GameStateEntity?> = gameDao.getGameState()
    val gameStatistics: Flow<GameStatisticsEntity?> = gameDao.getGameStatistics()

    suspend fun createNewGame() {
        val initialGame = GameStateEntity(
            id = 1, // IMPORTANTE: ID FIJO
            money = "0",
            premiumCurrency = "0",
            prestigeCurrency = "0",
            echoes = "0",
            lastLoginTime = System.currentTimeMillis(),
            totalPlayTime = 0
        )
        gameDao.saveGame(initialGame)

        val initialStats = GameStatisticsEntity(
            id = 1, // ID Fijo para stats también
            totalPlayTimeSeconds = 0,
            startTime = System.currentTimeMillis(),
            runTimeSeconds = 0,
            totalOfflineSeconds = 0,
            lifetimeKnowledge = "0",
            runKnowledge = "0",
            maxKnowledgePerSecond = "0",
            totalClicks = 0,
            manualKnowledgeGenerated = "0",
            totalPrestiges = 0,
            lifetimeSanity = "0",
            spentSanity = "0",
            totalEventsFound = 0
        )
        gameDao.saveGameStatistics(initialStats)
    }

    suspend fun saveGame(
        currentKnowledge: BigDecimal,
        prestige: BigDecimal,
        currentEchoes: BigDecimal
    ) {
        val currentState = GameStateEntity(
            id = 1, // SIEMPRE 1
            money = currentKnowledge.toString(),
            premiumCurrency = "0",
            prestigeCurrency = prestige.toString(),
            echoes = currentEchoes.toString(),
            lastLoginTime = System.currentTimeMillis(),
            totalPlayTime = 0
        )
        gameDao.saveGame(currentState)
    }

    /**
     * Guarda TODO el progreso. Antes el autoguardado no incluía las mejoras ni la tienda
     * de prestigio, así que al importar una partida esos datos se perdían al reiniciar.
     * Cada lista se inserta en una sola transacción en lugar de una por fila.
     */
    suspend fun saveSnapshot(snapshot: GameSnapshot) {
        saveGame(snapshot.knowledge, snapshot.sanity, snapshot.echoes)
        gameDao.insertBuildings(snapshot.buildings)
        gameDao.insertUpgrades(snapshot.upgrades)
        if (snapshot.unlockedAchievements.isNotEmpty()) gameDao.insertAchievements(snapshot.unlockedAchievements)
        gameDao.insertPrestigeUpgrades(snapshot.prestigeUpgrades)
        gameDao.saveGameStatistics(snapshot.statistics)
    }

    /** Borra todo el progreso guardado (reinicio total o antes de importar). */
    suspend fun wipeProgressTables() {
        gameDao.deleteAllBuildings()
        gameDao.deleteAllUpgrades()
        gameDao.deleteAllAchievements()
        gameDao.deleteAllPrestigeUpgrades()
    }

    suspend fun saveStatistics(stats: GameStatisticsEntity) {
        gameDao.saveGameStatistics(stats)
    }

    suspend fun loadBuildings() = gameDao.getAllBuildings()
    suspend fun wipeBuildingsAndUpgrades() { gameDao.deleteAllBuildings(); gameDao.deleteAllUpgrades() }
    suspend fun loadUpgrades() = gameDao.getAllUpgrades()
    suspend fun loadAchievements() = gameDao.getAllAchievements()
    suspend fun loadPrestigeUpgrades() = gameDao.getAllPrestigeUpgrades()
    suspend fun clearAllPrestigeUpgrades() = gameDao.deleteAllPrestigeUpgrades()
}
