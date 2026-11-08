package com.EGomez.idleecosystem.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

/** Hilo en el que corre TODA la lógica del juego (ver GameEngine). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EngineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @EngineDispatcher
    fun provideEngineDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}
