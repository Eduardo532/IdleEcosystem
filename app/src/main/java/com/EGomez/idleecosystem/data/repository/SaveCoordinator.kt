package com.EGomez.idleecosystem.data.repository

import android.util.Log
import com.EGomez.idleecosystem.data.database.AchievementEntity
import com.EGomez.idleecosystem.data.database.BuildingEntity
import com.EGomez.idleecosystem.data.database.GameStateEntity
import com.EGomez.idleecosystem.data.database.GameStatisticsEntity
import com.EGomez.idleecosystem.data.database.PrestigeUpgradeEntity
import com.EGomez.idleecosystem.data.database.UpgradeEntity
import com.EGomez.idleecosystem.data.preferences.GamePreferences
import com.EGomez.idleecosystem.domain.logic.RitualSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Todo lo guardado, leído de una vez al iniciar. */
data class LoadedGame(
    val state: GameStateEntity?,
    val buildings: List<BuildingEntity>,
    val upgrades: List<UpgradeEntity>,
    val achievements: List<AchievementEntity>,
    val prestigeUpgrades: List<PrestigeUpgradeEntity>,
    val statistics: GameStatisticsEntity?,
    val rituals: List<RitualSnapshot>,
    val ritualsSavedAtMillis: Long,
    val pendingFollowUpEventId: String? = null
)

/**
 * Una petición de guardado. Cada una lleva la foto COMPLETA de la partida, así que
 * varias pendientes se pueden reducir a la última sin perder nada.
 *
 * @param replace borra antes las tablas de progreso (ascensión, reinicio, importación
 * y reembolso de la tienda antigua), para que no queden filas de la partida anterior.
 */
data class SaveRequest(
    val snapshot: GameSnapshot,
    val rituals: List<RitualSnapshot>,
    val takenAtMillis: Long,
    val replace: Boolean,
    // Continuación de una cadena de eventos abierta (preferencias del dispositivo)
    val pendingFollowUpEventId: String? = null
) {
    companion object {
        /**
         * Reduce una ráfaga de peticiones a una sola: la foto más reciente, borrando
         * las tablas si CUALQUIERA de ellas lo pedía (un reemplazo seguido de un
         * guardado normal sigue siendo un reemplazo).
         */
        fun coalesce(requests: List<SaveRequest>): SaveRequest {
            require(requests.isNotEmpty())
            val latest = requests.last()
            return if (requests.any { it.replace }) latest.copy(replace = true) else latest
        }
    }
}

/**
 * Único punto de acceso al guardado.
 *
 * Antes el ViewModel escribía en la base de datos desde cada acción (edificio por
 * edificio, logro por logro) y bajo el mismo Mutex que la lógica del juego. Ahora el
 * motor solo pide "guarda esta foto" y sigue: las peticiones se procesan en orden, en
 * un hilo de E/S, y las que se acumulan mientras se escribe se reducen a la última.
 *
 * Vive en el ámbito de la aplicación, así un guardado pedido al salir (onStop) termina
 * aunque el ViewModel se destruya justo después.
 */
@Singleton
class SaveCoordinator @Inject constructor(
    private val repository: GameRepository,
    private val preferences: GamePreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val requests = Channel<SaveRequest>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (first in requests) {
                val pending = mutableListOf(first)
                while (true) {
                    val next = requests.tryReceive().getOrNull() ?: break
                    pending += next
                }
                write(SaveRequest.coalesce(pending))
            }
        }
    }

    /** Encola un guardado. No suspende: el motor nunca espera a la base de datos. */
    fun save(request: SaveRequest) {
        requests.trySend(request)
    }

    suspend fun load(): LoadedGame = withContext(Dispatchers.IO) {
        val (rituals, ritualsSavedAt) = preferences.loadRituals()
        LoadedGame(
            state = repository.gameState.firstOrNull(),
            buildings = repository.loadBuildings(),
            upgrades = repository.loadUpgrades(),
            achievements = repository.loadAchievements(),
            prestigeUpgrades = repository.loadPrestigeUpgrades(),
            statistics = repository.gameStatistics.firstOrNull(),
            rituals = rituals,
            ritualsSavedAtMillis = ritualsSavedAt,
            pendingFollowUpEventId = preferences.pendingFollowUpEventId
        )
    }

    suspend fun createNewGame() = withContext(Dispatchers.IO) {
        repository.createNewGame()
    }

    private suspend fun write(request: SaveRequest) {
        try {
            preferences.saveRituals(request.rituals, request.takenAtMillis)
            preferences.pendingFollowUpEventId = request.pendingFollowUpEventId
            if (request.replace) {
                repository.replaceWithSnapshot(request.snapshot)
            } else {
                repository.saveSnapshot(request.snapshot)
            }
        } catch (e: Exception) {
            // Un fallo puntual no debe detener la cola: el siguiente guardado lo corrige
            Log.e(TAG, "No se pudo guardar la partida", e)
        }
    }

    private companion object {
        const val TAG = "SaveCoordinator"
    }
}
