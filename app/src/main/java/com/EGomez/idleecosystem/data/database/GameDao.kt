package com.EGomez.idleecosystem.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // CAMBIO: Buscamos explícitamente el ID 1 en game_save
    @Query("SELECT * FROM game_save WHERE id = 1")
    fun getGameState(): Flow<GameStateEntity?>

    // OnConflictStrategy.REPLACE asegura que si ya existe el ID 1, lo actualiza (no crea uno nuevo)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(gameState: GameStateEntity)

    // --- Tablas secundarias (Mantenlas igual) ---
    @Query("SELECT * FROM buildings_save")
    suspend fun getAllBuildings(): List<BuildingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity)

    @Query("DELETE FROM buildings_save")
    suspend fun deleteAllBuildings()

    @Query("SELECT * FROM upgrades_save")
    suspend fun getAllUpgrades(): List<UpgradeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpgrade(upgrade: UpgradeEntity)

    @Query("DELETE FROM upgrades_save")
    suspend fun deleteAllUpgrades()

    @Query("SELECT * FROM achievements_save")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Query("DELETE FROM achievements_save")
    suspend fun deleteAllAchievements()

    @Query("SELECT * FROM prestige_upgrades")
    suspend fun getAllPrestigeUpgrades(): List<PrestigeUpgradeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrestigeUpgrade(entity: PrestigeUpgradeEntity)

    @Query("DELETE FROM prestige_upgrades")
    suspend fun deleteAllPrestigeUpgrades()

    @Query("SELECT * FROM game_statistics WHERE id = 1")
    fun getGameStatistics(): Flow<GameStatisticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameStatistics(stats: GameStatisticsEntity)
}