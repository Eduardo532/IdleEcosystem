package com.EGomez.idleecosystem.core.di

import android.content.Context
import androidx.room.Room
import com.EGomez.idleecosystem.data.database.AppDatabase
import com.EGomez.idleecosystem.data.database.GameDao
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "idle_lovecraft_db" // Mantenemos tu nombre de DB original
        )
            .addMigrations(*AppDatabase.ALL_MIGRATIONS) // Esto cargará automáticamente la MIGRATION_5_6
            .build()
    }

    @Provides
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    fun provideBuildingCalculator(): BuildingCalculator {
        return BuildingCalculator()
    }
}