package com.EGomez.idleecosystem.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        GameStateEntity::class,
        BuildingEntity::class,
        UpgradeEntity::class,
        AchievementEntity::class,
        PrestigeUpgradeEntity::class,
        GameStatisticsEntity::class
    ],
    version = 8 // <--- CAMBIO: SUBIMOS VERSIÓN DE 7 A 8
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        // --- MIGRACIONES ANTERIORES (NO TOCAR) ---
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `achievements_save` (`id` TEXT NOT NULL, `isUnlocked` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `prestige_upgrades` (`id` TEXT NOT NULL, `level` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `game_statistics` (`id` INTEGER NOT NULL, `totalPlayTimeSeconds` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `runTimeSeconds` INTEGER NOT NULL, `lifetimeKnowledge` TEXT NOT NULL, `runKnowledge` TEXT NOT NULL, `maxKnowledgePerSecond` TEXT NOT NULL, `totalClicks` INTEGER NOT NULL, `manualKnowledgeGenerated` TEXT NOT NULL, `totalPrestiges` INTEGER NOT NULL, `lifetimeSanity` TEXT NOT NULL, `spentSanity` TEXT NOT NULL, `totalEventsFound` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                val now = System.currentTimeMillis()
                database.execSQL("INSERT OR IGNORE INTO `game_statistics` VALUES (1, 0, $now, 0, '0', '0', '0', 0, '0', 0, '0', '0', 0)")
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `game_statistics` ADD COLUMN `totalOfflineSeconds` INTEGER NOT NULL DEFAULT 0")
            }
        }

        // --- NUEVA MIGRACIÓN (7 -> 8) ---
        // Esta migración arregla el reinicio de partida y añade los Ecos a las estadísticas.
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Agregar totalEchoes a Estadísticas
                try {
                    database.execSQL("ALTER TABLE `game_statistics` ADD COLUMN `totalEchoes` TEXT NOT NULL DEFAULT '0'")
                } catch (e: Exception) {
                    // Si ya existe por algún motivo, ignoramos el error
                }

                // 2. Arreglar game_save (Quitar Autoincrement y fijar ID=1)
                // Paso A: Crear tabla temporal con la estructura CORRECTA (ID fijo)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `game_save_new` (
                        `id` INTEGER NOT NULL, 
                        `money` TEXT NOT NULL, 
                        `premiumCurrency` TEXT NOT NULL, 
                        `prestigeCurrency` TEXT NOT NULL, 
                        `echoes` TEXT NOT NULL, 
                        `lastLoginTime` INTEGER NOT NULL, 
                        `totalPlayTime` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)

                // Paso B: Copiar los datos de la última partida guardada a la fila ID=1
                database.execSQL("""
                    INSERT INTO game_save_new (id, money, premiumCurrency, prestigeCurrency, echoes, lastLoginTime, totalPlayTime)
                    SELECT 1, money, premiumCurrency, prestigeCurrency, echoes, lastLoginTime, totalPlayTime 
                    FROM game_save 
                    ORDER BY id DESC 
                    LIMIT 1
                """)

                // Paso C: Reemplazar la tabla vieja por la nueva
                database.execSQL("DROP TABLE game_save")
                database.execSQL("ALTER TABLE game_save_new RENAME TO game_save")
            }
        }

        // AGREGAR LA NUEVA MIGRACIÓN A LA LISTA
        val ALL_MIGRATIONS = arrayOf(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
    }
}