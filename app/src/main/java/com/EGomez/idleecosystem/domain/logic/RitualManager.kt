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

@Singleton
class RitualManager @Inject constructor() {

    private val _rituals = MutableStateFlow<List<Ritual>>(emptyList())
    val rituals: StateFlow<List<Ritual>> = _rituals.asStateFlow()

    init {
        initializeRituals()
    }

    private fun initializeRituals() {
        val initialList = listOf(
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
                id = "rit_knowledge_offering",
                name = "Ofrenda de Conocimiento",
                description = "Los Ecos se disuelven en saber: 2 minutos de producción al instante.",
                baseCost = BigDecimal("25"),
                durationSeconds = 1,
                cooldownSeconds = 600,
                effectType = RitualEffectType.INSTANT_KNOWLEDGE,
                effectValue = 120.0 // segundos de producción base
            ),
            Ritual(
                id = "rit_temporal_rift",
                name = "Grieta Temporal",
                description = "Un instante de eficiencia imposible. x20 a la producción.",
                baseCost = BigDecimal("50"),
                durationSeconds = 15,
                cooldownSeconds = 300,
                effectType = RitualEffectType.PRODUCTION_MULTIPLIER,
                effectValue = 20.0
            ),
            Ritual(
                id = "rit_eternal_vigil",
                name = "Vigilia Eterna",
                description = "Dos minutos sin dormir, sin parpadear. x6 a la producción.",
                baseCost = BigDecimal("100"),
                durationSeconds = 120,
                cooldownSeconds = 1200,
                effectType = RitualEffectType.PRODUCTION_MULTIPLIER,
                effectValue = 6.0
            )
        )
        _rituals.value = initialList
    }

    /**
     * Avanza el tiempo de los rituales. Soporta saltos grandes (por ejemplo, el tiempo
     * que la app estuvo cerrada): un ritual activo puede terminar y completar su
     * enfriamiento en un mismo paso.
     */
    fun tick(deltaSeconds: Long = 1) {
        if (deltaSeconds <= 0) return
        _rituals.update { currentList -> currentList.map { advance(it, deltaSeconds) } }
    }

    private fun advance(ritual: Ritual, deltaSeconds: Long): Ritual {
        var current = ritual
        var remaining = deltaSeconds
        while (remaining > 0 && current.state != RitualState.READY) {
            if (current.timeRemaining > remaining) {
                return current.copy(timeRemaining = current.timeRemaining - remaining)
            }
            remaining -= current.timeRemaining
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

    fun getActiveProductionMultipliers(): List<Double> =
        _rituals.value.filter { it.state == RitualState.ACTIVE && it.effectType == RitualEffectType.PRODUCTION_MULTIPLIER }
            .map { it.effectValue }

    fun getActiveClickMultipliers(): List<Double> =
        _rituals.value.filter { it.state == RitualState.ACTIVE && it.effectType == RitualEffectType.CLICK_MULTIPLIER }
            .map { it.effectValue }

    // --- PERSISTENCIA ---

    fun snapshot(): List<RitualSnapshot> =
        _rituals.value.map { RitualSnapshot(it.id, it.state, it.timeRemaining) }

    /** Restaura el estado guardado y descuenta el tiempo que pasó desde que se guardó. */
    fun restore(snapshots: List<RitualSnapshot>, elapsedSeconds: Long) {
        _rituals.update { currentList ->
            currentList.map { ritual ->
                val saved = snapshots.find { it.id == ritual.id } ?: return@map ritual
                val total = if (saved.state == RitualState.COOLDOWN) ritual.cooldownSeconds else ritual.durationSeconds
                ritual.copy(
                    state = saved.state,
                    timeRemaining = if (saved.state == RitualState.READY) 0 else saved.timeRemaining.coerceIn(0, total),
                    totalTimeForProgress = total
                )
            }
        }
        tick(elapsedSeconds)
    }

    fun reset() {
        initializeRituals()
    }
}

/** Estado mínimo de un ritual para guardarlo entre sesiones. */
data class RitualSnapshot(val id: String, val state: RitualState, val timeRemaining: Long)
