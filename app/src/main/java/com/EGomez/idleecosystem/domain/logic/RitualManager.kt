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
                baseCost = BigDecimal("500"),
                durationSeconds = 15,
                cooldownSeconds = 60,
                effectType = RitualEffectType.CLICK_MULTIPLIER,
                effectValue = 5.0
            ),
            Ritual(
                id = "rit_void_chant",
                name = "Cántico del Vacío",
                description = "La producción se acelera mientras la realidad se rompe.",
                baseCost = BigDecimal("2500"),
                durationSeconds = 30,
                cooldownSeconds = 120,
                effectType = RitualEffectType.PRODUCTION_MULTIPLIER,
                effectValue = 3.0
            ),
            Ritual(
                id = "rit_temporal_rift",
                name = "Grieta Temporal",
                description = "Un breve instante de eficiencia infinita.",
                baseCost = BigDecimal("15000"),
                durationSeconds = 5,
                cooldownSeconds = 300,
                effectType = RitualEffectType.PRODUCTION_MULTIPLIER,
                effectValue = 20.0
            )
        )
        _rituals.value = initialList
    }

    fun tick(deltaSeconds: Long = 1) {
        _rituals.update { currentList ->
            currentList.map { ritual ->
                when (ritual.state) {
                    RitualState.ACTIVE -> {
                        val newTime = ritual.timeRemaining - deltaSeconds
                        if (newTime <= 0) {
                            // Transición: ACTIVE -> COOLDOWN
                            ritual.copy(
                                state = RitualState.COOLDOWN,
                                timeRemaining = ritual.cooldownSeconds,
                                totalTimeForProgress = ritual.cooldownSeconds
                            )
                        } else {
                            ritual.copy(timeRemaining = newTime)
                        }
                    }
                    RitualState.COOLDOWN -> {
                        val newTime = ritual.timeRemaining - deltaSeconds
                        if (newTime <= 0) {
                            // Transición: COOLDOWN -> READY
                            ritual.copy(
                                state = RitualState.READY,
                                timeRemaining = 0,
                                totalTimeForProgress = ritual.durationSeconds // Prepara para el próximo uso
                            )
                        } else {
                            ritual.copy(timeRemaining = newTime)
                        }
                    }
                    RitualState.READY -> ritual
                }
            }
        }
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
}