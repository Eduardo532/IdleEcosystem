package com.EGomez.idleecosystem.data.repository

import com.EGomez.idleecosystem.data.database.*
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

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
        premium: BigDecimal,
        prestige: BigDecimal,
        currentEchoes: BigDecimal
    ) {
        val currentState = GameStateEntity(
            id = 1, // SIEMPRE 1
            money = currentKnowledge.toString(),
            premiumCurrency = premium.toString(),
            prestigeCurrency = prestige.toString(),
            echoes = currentEchoes.toString(),
            lastLoginTime = System.currentTimeMillis(),
            totalPlayTime = 0 // Podrías querer conectar esto con el stats manager más adelante
        )
        gameDao.saveGame(currentState)
    }

    suspend fun saveStatistics(stats: GameStatisticsEntity) {
        gameDao.saveGameStatistics(stats)
    }

    // --- El resto de funciones se mantienen igual ---
    suspend fun loadBuildings() = gameDao.getAllBuildings()
    suspend fun saveBuilding(id: String, amount: Int, isVisible: Boolean) = gameDao.insertBuilding(BuildingEntity(id, amount, isVisible))
    suspend fun wipeBuildingsAndUpgrades() { gameDao.deleteAllBuildings(); gameDao.deleteAllUpgrades() }
    suspend fun loadUpgrades() = gameDao.getAllUpgrades()
    suspend fun saveUpgrade(id: String, isBought: Boolean) = gameDao.insertUpgrade(UpgradeEntity(id, isBought))
    suspend fun loadAchievements() = gameDao.getAllAchievements()
    suspend fun saveAchievement(id: String, isUnlocked: Boolean) = gameDao.insertAchievement(AchievementEntity(id, isUnlocked))
    suspend fun clearAllAchievements() = gameDao.deleteAllAchievements()
    suspend fun loadPrestigeUpgrades() = gameDao.getAllPrestigeUpgrades()
    suspend fun savePrestigeUpgrade(id: String, level: Int) = gameDao.insertPrestigeUpgrade(PrestigeUpgradeEntity(id, level))
    suspend fun clearAllPrestigeUpgrades() = gameDao.deleteAllPrestigeUpgrades()

    suspend fun performPrestigeReset(newPrestigeBalance: BigDecimal, currentEchoes: BigDecimal) {
        val resetState = GameStateEntity(
            id = 1,
            money = "0",
            premiumCurrency = "0",
            prestigeCurrency = newPrestigeBalance.toString(),
            echoes = currentEchoes.toString(),
            lastLoginTime = System.currentTimeMillis(),
            totalPlayTime = 0
        )
        gameDao.saveGame(resetState)
    }
}