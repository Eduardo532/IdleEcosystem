package com.EGomez.idleecosystem.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // --- JUEGO GENERAL ---
    @Query("SELECT * FROM game_save WHERE id = 0")
    fun getGameState(): Flow<GameStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(gameState: GameStateEntity)

    // --- EDIFICIOS ---
    @Query("SELECT * FROM buildings_save")
    suspend fun getAllBuildings(): List<BuildingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity)

    @Query("DELETE FROM buildings_save")
    suspend fun deleteAllBuildings()

    // --- MEJORAS ---
    @Query("SELECT * FROM upgrades_save")
    suspend fun getAllUpgrades(): List<UpgradeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpgrade(upgrade: UpgradeEntity)

    @Query("DELETE FROM upgrades_save")
    suspend fun deleteAllUpgrades()

    // --- LOGROS ---
    @Query("SELECT * FROM achievements_save")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Query("DELETE FROM achievements_save")
    suspend fun deleteAllAchievements()

    // --- PRESTIGIO SHOP ---
    @Query("SELECT * FROM prestige_upgrades")
    suspend fun getAllPrestigeUpgrades(): List<PrestigeUpgradeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrestigeUpgrade(entity: PrestigeUpgradeEntity)

    @Query("DELETE FROM prestige_upgrades")
    suspend fun deleteAllPrestigeUpgrades()

    // --- ESTADÍSTICAS ---
    // Usamos ID = 1 como definimos en la migración
    @Query("SELECT * FROM game_statistics WHERE id = 1")
    fun getGameStatistics(): Flow<GameStatisticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameStatistics(stats: GameStatisticsEntity)
}