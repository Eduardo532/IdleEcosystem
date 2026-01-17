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

    // NUEVO FLOW DE ESTADÍSTICAS
    val gameStatistics: Flow<GameStatisticsEntity?> = gameDao.getGameStatistics()

    suspend fun createNewGame() {
        val initialGame = GameStateEntity(
            money = "0", premiumCurrency = "0", prestigeCurrency = "0",
            lastLoginTime = System.currentTimeMillis(), totalPlayTime = 0
        )
        gameDao.saveGame(initialGame)

        // Inicializar estadísticas vacías
        val initialStats = GameStatisticsEntity(
            id = 1,
            totalPlayTimeSeconds = 0,
            startTime = System.currentTimeMillis(),
            runTimeSeconds = 0,
            totalOfflineSeconds = 0, // <--- ¡AQUÍ ESTABA EL ERROR! (Faltaba este campo)
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

    suspend fun saveGame(currentKnowledge: BigDecimal, premium: BigDecimal, prestige: BigDecimal) {
        val currentState = GameStateEntity(
            money = currentKnowledge.toString(),
            premiumCurrency = premium.toString(),
            prestigeCurrency = prestige.toString(),
            lastLoginTime = System.currentTimeMillis(),
            totalPlayTime = 0
        )
        gameDao.saveGame(currentState)
    }

    // Guardar Estadísticas
    suspend fun saveStatistics(stats: GameStatisticsEntity) {
        gameDao.saveGameStatistics(stats)
    }

    // --- EDIFICIOS ---
    suspend fun loadBuildings(): List<BuildingEntity> = gameDao.getAllBuildings()

    suspend fun saveBuilding(id: String, amount: Int, isVisible: Boolean) {
        gameDao.insertBuilding(BuildingEntity(buildingId = id, amountOwned = amount, isUnlocked = isVisible))
    }

    suspend fun wipeBuildingsAndUpgrades() {
        gameDao.deleteAllBuildings()
        gameDao.deleteAllUpgrades()
    }

    // --- MEJORAS ---
    suspend fun loadUpgrades(): List<UpgradeEntity> = gameDao.getAllUpgrades()

    suspend fun saveUpgrade(id: String, isBought: Boolean) = gameDao.insertUpgrade(UpgradeEntity(id, isBought))

    // --- LOGROS ---
    suspend fun loadAchievements(): List<AchievementEntity> = gameDao.getAllAchievements()

    suspend fun saveAchievement(id: String, isUnlocked: Boolean) {
        gameDao.insertAchievement(AchievementEntity(id, isUnlocked))
    }

    suspend fun clearAllAchievements() {
        gameDao.deleteAllAchievements()
    }

    // --- PRESTIGIO ---
    suspend fun loadPrestigeUpgrades(): List<PrestigeUpgradeEntity> = gameDao.getAllPrestigeUpgrades()

    suspend fun savePrestigeUpgrade(id: String, level: Int) {
        gameDao.insertPrestigeUpgrade(PrestigeUpgradeEntity(id, level))
    }

    suspend fun clearAllPrestigeUpgrades() {
        gameDao.deleteAllPrestigeUpgrades()
    }

    suspend fun performPrestigeReset(newPrestigeBalance: BigDecimal) {
        val resetState = GameStateEntity(
            money = "0", premiumCurrency = "0", prestigeCurrency = newPrestigeBalance.toString(),
            lastLoginTime = System.currentTimeMillis(), totalPlayTime = 0
        )
        gameDao.saveGame(resetState)
    }
}