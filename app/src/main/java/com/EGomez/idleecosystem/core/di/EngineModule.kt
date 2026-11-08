package com.EGomez.idleecosystem.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Qualifier
import javax.inject.Singleton

/** Hilo en el que corre TODA la lógica del juego (ver GameEngine). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EngineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    /**
     * Un solo hilo lógico tomado del pool de Dispatchers.Default: el bucle, los
     * cálculos con BigDecimal y el recálculo de multiplicadores ya no compiten con el
     * dibujo de la pantalla, y como nunca corren dos tareas a la vez, el estado mutable
     * de los managers no necesita locks.
     *
     * Es @Singleton a propósito: si un ViewModel nuevo arranca mientras el anterior
     * termina de cancelarse, ambos motores comparten esta misma cola y no pueden tocar
     * los managers al mismo tiempo.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Provides
    @Singleton
    @EngineDispatcher
    fun provideEngineDispatcher(): CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)
}
