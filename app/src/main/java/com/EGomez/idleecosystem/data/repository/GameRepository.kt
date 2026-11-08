package com.EGomez.idleecosystem.data.repository

import com.EGomez.idleecosystem.data.database.*
import kotlinx.coroutines.flow.Flow
import androidx.room.withTransaction
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/** Foto completa del progreso para guardarla en una sola transacción. */
data class GameSnapshot(
    val knowledge: BigDecimal,
    val sanity: BigDecimal,
    val echoes: BigDecimal,
    val tutorialEnabled: Boolean,
    val completedTutorialSteps: String,
    val buildings: List<BuildingEntity>,
    val upgrades: List<UpgradeEntity>,
    val unlockedAchievements: List<AchievementEntity>,
    val prestigeUpgrades: List<PrestigeUpgradeEntity>,
    val statistics: GameStatisticsEntity
)

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao,
    private val database: AppDatabase
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
        val current = gameDao.getGameStateOnce()
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

    /**
     * Guarda TODO el progreso de forma atómica. Antes cada tabla (y cada fila) se
     * escribía por separado: si el proceso moría a mitad de un guardado, la partida
     * quedaba inconsistente, y el autoguardado no incluía las mejoras ni la tienda.
     */
    suspend fun saveSnapshot(snapshot: GameSnapshot) {
        database.withTransaction {
            saveGame(
                snapshot.knowledge,
                BigDecimal.ZERO,
                snapshot.sanity,
                snapshot.echoes,
                snapshot.tutorialEnabled,
                snapshot.completedTutorialSteps
            )
            gameDao.insertBuildings(snapshot.buildings)
            gameDao.insertUpgrades(snapshot.upgrades)
            gameDao.insertAchievements(snapshot.unlockedAchievements)
            gameDao.insertPrestigeUpgrades(snapshot.prestigeUpgrades)
            gameDao.saveStatistics(snapshot.statistics)
        }
    }

    /** Reemplaza todo lo guardado por la partida importada, en una sola transacción. */
    suspend fun replaceWithSnapshot(snapshot: GameSnapshot) {
        database.withTransaction {
            gameDao.deleteAllBuildings()
            gameDao.deleteAllUpgrades()
            gameDao.deleteAllAchievements()
            gameDao.deleteAllPrestigeUpgrades()
            saveSnapshot(snapshot)
        }
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
        val current = gameDao.getGameStateOnce()
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