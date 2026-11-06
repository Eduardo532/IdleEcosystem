package com.EGomez.idleecosystem.data.repository

import com.EGomez.idleecosystem.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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
            totalPlayTime = 0,
            tutorialEnabled = true, // Default: Activado
            completedTutorialSteps = "" // Default: Ninguno
        )
        gameDao.saveGame(initialGame)

        val initialStats = GameStatisticsEntity(
            id = 1,
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
            totalEchoes = "0",
            totalEventsFound = 0,
            runesSevered = 0,
            runesStabilized = 0,
            runesInscribed = 0
        )
        gameDao.saveStatistics(initialStats)
    }

    // --- ACTUALIZADO: AHORA RECIBE EL ESTADO DEL TUTORIAL ---
    suspend fun saveGame(
        money: BigDecimal,
        premium: BigDecimal,
        prestige: BigDecimal,
        echoes: BigDecimal,
        tutorialEnabled: Boolean,
        completedSteps: String
    ) {
        // Recuperamos el tiempo de juego para no perderlo
        val current = gameState.firstOrNull()
        val currentPlayTime = current?.totalPlayTime ?: 0L

        val entity = GameStateEntity(
            id = 1,
            money = money.toString(),
            premiumCurrency = premium.toString(),
            prestigeCurrency = prestige.toString(),
            echoes = echoes.toString(),
            lastLoginTime = System.currentTimeMillis(),
            totalPlayTime = currentPlayTime,
            tutorialEnabled = tutorialEnabled,
            completedTutorialSteps = completedSteps
        )
        gameDao.saveGame(entity)
    }

    // NUEVO: Guardado ligero específico para el tutorial
    suspend fun saveTutorialState(enabled: Boolean, steps: String) {
        gameDao.updateTutorialState(enabled, steps)
    }

    // --- MANEJO DE EDIFICIOS ---
    suspend fun loadBuildings() = gameDao.getAllBuildings()
    suspend fun saveBuilding(id: String, amount: Int, isVisible: Boolean) = gameDao.insertBuilding(BuildingEntity(id, amount, isVisible))
    suspend fun wipeBuildingsAndUpgrades() { gameDao.deleteAllBuildings(); gameDao.deleteAllUpgrades() }

    // --- MANEJO DE MEJORAS ---
    suspend fun loadUpgrades() = gameDao.getAllUpgrades()
    suspend fun saveUpgrade(id: String, isBought: Boolean) = gameDao.insertUpgrade(UpgradeEntity(id, isBought))

    // --- MANEJO DE LOGROS ---
    suspend fun loadAchievements() = gameDao.getAllAchievements()
    suspend fun saveAchievement(id: String, isUnlocked: Boolean) = gameDao.insertAchievement(AchievementEntity(id, isUnlocked))
    suspend fun clearAllAchievements() = gameDao.deleteAllAchievements()

    // --- MANEJO DE TIENDA DE PRESTIGIO ---
    suspend fun loadPrestigeUpgrades() = gameDao.getAllPrestigeUpgrades()
    suspend fun savePrestigeUpgrade(id: String, level: Int) = gameDao.insertPrestigeUpgrade(PrestigeUpgradeEntity(id, level))
    suspend fun clearAllPrestigeUpgrades() = gameDao.deleteAllPrestigeUpgrades()

    // --- MANEJO DE ESTADÍSTICAS ---
    suspend fun saveStatistics(entity: GameStatisticsEntity) = gameDao.saveStatistics(entity)

    // --- ACTUALIZADO: PRESTIGIO CON TUTORIAL ---
    suspend fun performPrestigeReset(
        newPrestigeBalance: BigDecimal,
        currentEchoes: BigDecimal,
        tutorialEnabled: Boolean,
        completedSteps: String
    ) {
        val current = gameState.firstOrNull()
        val currentPlayTime = current?.totalPlayTime ?: 0L

        val resetState = GameStateEntity(
            id = 1,
            money = "0",
            premiumCurrency = "0",
            prestigeCurrency = newPrestigeBalance.toString(),
            echoes = currentEchoes.toString(),
            lastLoginTime = System.currentTimeMillis(),
            totalPlayTime = currentPlayTime,
            // Mantenemos configuración del tutorial y pasos completados
            tutorialEnabled = tutorialEnabled,
            completedTutorialSteps = completedSteps
        )
        gameDao.saveGame(resetState)
    }
}