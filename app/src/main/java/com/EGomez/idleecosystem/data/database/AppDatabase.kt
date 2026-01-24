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
    version = 9
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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL("ALTER TABLE `game_statistics` ADD COLUMN `totalEchoes` TEXT NOT NULL DEFAULT '0'")
                } catch (e: Exception) { }

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

                database.execSQL("""
                    INSERT INTO game_save_new (id, money, premiumCurrency, prestigeCurrency, echoes, lastLoginTime, totalPlayTime)
                    SELECT 1, money, premiumCurrency, prestigeCurrency, echoes, lastLoginTime, totalPlayTime 
                    FROM game_save 
                    ORDER BY id DESC 
                    LIMIT 1
                """)

                database.execSQL("DROP TABLE game_save")
                database.execSQL("ALTER TABLE game_save_new RENAME TO game_save")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `game_statistics` ADD COLUMN `runesSevered` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `game_statistics` ADD COLUMN `runesStabilized` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `game_statistics` ADD COLUMN `runesInscribed` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9
        )
    }
}