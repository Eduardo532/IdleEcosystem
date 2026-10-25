package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Ritual
import com.EGomez.idleecosystem.domain.model.RitualEffectType
import com.EGomez.idleecosystem.domain.model.RitualState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/** Estado mínimo de un ritual para guardarlo entre sesiones. */
data class RitualSnapshot(val id: String, val state: RitualState, val timeRemaining: Long)

@Singleton
class RitualManager @Inject constructor() {

    private val _rituals = MutableStateFlow<List<Ritual>>(emptyList())
    val rituals: StateFlow<List<Ritual>> = _rituals.asStateFlow()

    init {
        initializeRituals()
    }

    private fun initializeRituals() {
        _rituals.value = defaultRituals()
    }

    private fun defaultRituals() = listOf(
        Ritual(
            id = "rit_blood_frenzy",
            name = "Frenesí de Sangre",
            description = "Tus clicks vibran con violencia. Potencia manual extrema.",
            baseCost = BigDecimal("5"),
            durationSeconds = 15,
            cooldownSeconds = 60,
            effectType = RitualEffectType.CLICK_MULTIPLIER,
            effectValue = 5.0
        ),
        Ritual(
            id = "rit_void_chant",
            name = "Cántico del Vacío",
            description = "La producción se acelera mientras la realidad se rompe.",
            baseCost = BigDecimal("15"),
            durationSeconds = 30,
            cooldownSeconds = 120,
            effectType = RitualEffectType.PRODUCTION_MULTIPLIER,
            effectValue = 3.0
        ),
        Ritual(
            id = "rit_temporal_rift",
            name = "Grieta Temporal",
            description = "Un breve instante de eficiencia infinita.",
            baseCost = BigDecimal("50"),
            durationSeconds = 5,
            cooldownSeconds = 300,
            effectType = RitualEffectType.PRODUCTION_MULTIPLIER,
            effectValue = 20.0
        )
    )

    /**
     * Avanza el tiempo. Soporta saltos grandes (por ejemplo, al volver tras estar cerrado):
     * un ritual activo puede terminar y completar su enfriamiento en un mismo paso.
     */
    fun tick(deltaSeconds: Long = 1) {
        if (deltaSeconds <= 0) return
        _rituals.update { currentList -> currentList.map { advance(it, deltaSeconds) } }
    }

    private fun advance(ritual: Ritual, deltaSeconds: Long): Ritual {
        var current = ritual
        var remainingDelta = deltaSeconds
        while (remainingDelta > 0 && current.state != RitualState.READY) {
            if (current.timeRemaining > remainingDelta) {
                return current.copy(timeRemaining = current.timeRemaining - remainingDelta)
            }
            remainingDelta -= current.timeRemaining
            current = when (current.state) {
                RitualState.ACTIVE -> current.copy(
                    state = RitualState.COOLDOWN,
                    timeRemaining = current.cooldownSeconds,
                    totalTimeForProgress = current.cooldownSeconds
                )
                else -> current.copy(
                    state = RitualState.READY,
                    timeRemaining = 0,
                    totalTimeForProgress = current.durationSeconds
                )
            }
        }
        return current
    }

    fun tryActivateRitual(ritualId: String, currentFunds: BigDecimal): Boolean {
        val ritual = _rituals.value.find { it.id == ritualId } ?: return false

        if (ritual.state == RitualState.READY && currentFunds >= ritual.baseCost) {
            _rituals.update { currentList ->
                currentList.map {
                    if (it.id == ritualId) {
                        it.copy(
                            state = RitualState.ACTIVE,
                            timeRemaining = it.durationSeconds,
                            totalTimeForProgress = it.durationSeconds
                        )
                    } else it
                }
            }
            return true
        }
        return false
    }

    fun getActiveProductionMultiplier(): Double {
        var multiplier = 1.0
        _rituals.value.filter { it.state == RitualState.ACTIVE && it.effectType == RitualEffectType.PRODUCTION_MULTIPLIER }
            .forEach { multiplier *= it.effectValue }
        return multiplier
    }

    fun getActiveClickMultiplier(): Double {
        var multiplier = 1.0
        _rituals.value.filter { it.state == RitualState.ACTIVE && it.effectType == RitualEffectType.CLICK_MULTIPLIER }
            .forEach { multiplier *= it.effectValue }
        return multiplier
    }

    // --- PERSISTENCIA ---
    // Antes los rituales vivían solo en memoria: cerrar la app reiniciaba el enfriamiento.

    fun snapshot(): List<RitualSnapshot> =
        _rituals.value.map { RitualSnapshot(it.id, it.state, it.timeRemaining) }

    /** Restaura el estado guardado y descuenta el tiempo que pasó con la app cerrada. */
    fun restore(snapshots: List<RitualSnapshot>, elapsedSeconds: Long) {
        _rituals.update { currentList ->
            currentList.map { ritual ->
                val saved = snapshots.find { it.id == ritual.id } ?: return@map ritual
                val total = when (saved.state) {
                    RitualState.ACTIVE -> ritual.durationSeconds
                    RitualState.COOLDOWN -> ritual.cooldownSeconds
                    RitualState.READY -> ritual.durationSeconds
                }
                ritual.copy(
                    state = saved.state,
                    timeRemaining = saved.timeRemaining.coerceIn(0, total),
                    totalTimeForProgress = total
                )
            }
        }
        tick(elapsedSeconds.coerceAtLeast(0))
    }

    fun reset() {
        initializeRituals()
    }
}
