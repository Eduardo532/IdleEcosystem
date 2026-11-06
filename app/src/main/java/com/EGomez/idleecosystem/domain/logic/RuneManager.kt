package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.RuneCatalog
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.RuneVariant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

enum class RuneState {
    HIDDEN, APPEARING, ACTIVE, SUCCESS, FAILED
}

// Eventos de recompensa instantánea que el ViewModel debe manejar
sealed class RuneReward {
    data class TimeWarp(val seconds: Long) : RuneReward()
    data class InstantEchoes(val amount: Int) : RuneReward()
    data class SanityBonus(val percent: Double) : RuneReward()
    object TriggerRandomEvent : RuneReward()
}

data class RuneBuff(
    val id: String,
    val multiplier: Double = 1.0,
    val costReduction: Double = 0.0, // 0.15 = 15% descuento
    val echoChanceBonus: Double = 0.0, // 0.01 = +1%
    val expirationTime: Long,
    val isAutoClick: Boolean = false,
    val freezesBuffs: Boolean = false, // Para Runa de Escarcha
    val description: String
)

@Singleton
class RuneManager @Inject constructor(
    private val statisticsManager: StatisticsManager,
    private val soundManager: SoundManager
) {
    private val _currentRune = MutableStateFlow<RuneVariant?>(null)
    val currentRune: StateFlow<RuneVariant?> = _currentRune.asStateFlow()

    private val _runeState = MutableStateFlow(RuneState.HIDDEN)
    val runeState: StateFlow<RuneState> = _runeState.asStateFlow()

    private val _activeBuffs = MutableStateFlow<List<RuneBuff>>(emptyList())
    val activeBuffs: StateFlow<List<RuneBuff>> = _activeBuffs.asStateFlow()

    // Canal para comunicar eventos instantáneos al ViewModel
    private val _runeRewards = MutableSharedFlow<RuneReward>()
    val runeRewards: SharedFlow<RuneReward> = _runeRewards.asSharedFlow()

    private var timeSinceLastRune = 0L
    private var runeLifeTime = 0f

    private val SPAWN_COOLDOWN_SECONDS = 40
    private val SPAWN_CHANCE_PER_TICK = 0.001f

    fun tick(deltaMillis: Long) {
        val currentTime = System.currentTimeMillis()

        // 1. Gestión de Buffs (Runa de Escarcha pausa la expiración)
        val isFrozen = _activeBuffs.value.any { it.freezesBuffs }

        if (!isFrozen) {
            _activeBuffs.update { list -> list.filter { it.expirationTime > currentTime } }
        } else {
            // Si está congelado, extendemos la vida de los otros buffs
            _activeBuffs.update { list ->
                list.map {
                    if (!it.freezesBuffs) it.copy(expirationTime = it.expirationTime + deltaMillis)
                    else it // La runa de escarcha sí se consume a sí misma
                }.filter { it.expirationTime > currentTime }
            }
        }

        // 2. Ciclo de Vida de la Runa
        if (_runeState.value == RuneState.HIDDEN) {
            timeSinceLastRune += deltaMillis
            if (timeSinceLastRune >= SPAWN_COOLDOWN_SECONDS * 1000) {
                if (Random.nextFloat() < SPAWN_CHANCE_PER_TICK) {
                    spawnRune()
                }
            }
        } else if (_runeState.value == RuneState.ACTIVE) {
            runeLifeTime -= (deltaMillis / 1000f)
            if (runeLifeTime <= 0) {
                failRune()
            }
        }
    }

    private fun spawnRune() {
        val roll = Random.nextFloat()
        // AJUSTE DE TIERS:
        // 0% - 80% : Tier 1 (Común)
        // 80% - 97% : Tier 2 (Raro)
        // 97% - 100% : Tier 3 (Mítico - Solo 3% chance)
        val tier = when {
            roll < 0.80f -> RuneTier.TIER_1_SWIPE
            roll < 0.97f -> RuneTier.TIER_2_HOLD
            else -> RuneTier.TIER_3_TRACE
        }

        val variants = RuneCatalog.getVariantsForTier(tier)
        // Selección ponderada
        val totalWeight = variants.sumOf { it.weight }
        val randomValue = Random.nextInt(totalWeight)
        var cumulativeWeight = 0
        var selectedRune = variants.first()

        for (variant in variants) {
            cumulativeWeight += variant.weight
            if (randomValue < cumulativeWeight) {
                selectedRune = variant
                break
            }
        }

        _currentRune.value = selectedRune
        _runeState.value = RuneState.APPEARING
        runeLifeTime = 15f + (tier.ordinal * 5f)
        soundManager.playSound(SoundType.RUNE_SPAWN)

        // Auto-transición visual
        _runeState.value = RuneState.ACTIVE
    }

    suspend fun completeRuneInteraction() {
        val rune = _currentRune.value ?: return
        if (_runeState.value != RuneState.ACTIVE) return

        soundManager.playSound(SoundType.RUNE_SUCCESS)
        _runeState.value = RuneState.SUCCESS

        // Tracking
        when (rune.tier) {
            RuneTier.TIER_1_SWIPE -> statisticsManager.trackRuneSevered()
            RuneTier.TIER_2_HOLD -> statisticsManager.trackRuneStabilized()
            RuneTier.TIER_3_TRACE -> statisticsManager.trackRuneInscribed()
        }

        applyRuneEffect(rune)
        timeSinceLastRune = 0
    }

    private suspend fun applyRuneEffect(rune: RuneVariant) {
        var durationMs: Long = 0
        var multiplier = 1.0
        var isAutoClick = false
        var costReduction = 0.0
        var echoChance = 0.0
        var freezesBuffs = false

        when (rune.id) {
            // --- TIER 1 ---
            "t1_blood" -> { multiplier = 50.0; durationMs = 15_000 }
            "t1_ash" -> { multiplier = 40.0; durationMs = 20_000 }
            "t1_salt" -> { multiplier = 60.0; durationMs = 10_000 }
            "t1_bone" -> { _runeRewards.emit(RuneReward.TimeWarp(120)) } // 2 min
            "t1_shadow" -> { multiplier = 30.0; durationMs = 30_000 }
            "t1_whisper" -> { multiplier = 10.0; durationMs = 30_000; isAutoClick = true }
            "t1_rust" -> { multiplier = 45.0; durationMs = 18_000 }
            "t1_frost" -> { durationMs = 5_000; freezesBuffs = true } // Pausa otros buffs
            "t1_vermin" -> { multiplier = 55.0; durationMs = 12_000 }
            "t1_rift" -> { _runeRewards.emit(RuneReward.TimeWarp(300)) } // 5 min
            "t1_void_minor" -> { durationMs = 30_000; echoChance = 0.01 } // +1% Ecos

            // --- TIER 2 ---
            "t2_chronos" -> { durationMs = 20_000; isAutoClick = true }
            "t2_gravity" -> { multiplier = 200.0; durationMs = 20_000 }
            "t2_barrier" -> { durationMs = 30_000; costReduction = 0.15 } // -15% Costos
            "t2_tidal" -> { multiplier = 500.0; durationMs = 10_000 }
            "t2_psyche" -> { _runeRewards.emit(RuneReward.SanityBonus(0.05)) } // +5% Cordura
            "t2_vision" -> { _runeRewards.emit(RuneReward.TriggerRandomEvent) }
            "t2_entropy" -> { timeSinceLastRune = SPAWN_COOLDOWN_SECONDS * 1000L } // Reinicio instantáneo (siguiente tick spawnea)

            // --- TIER 3 ---
            "t3_cthulhu" -> { multiplier = 777.0; durationMs = 60_000; _runeRewards.emit(RuneReward.InstantEchoes(1)) }
            "t3_nyarlathotep" -> { multiplier = 666.0; durationMs = 60_000; isAutoClick = true }
            "t3_azathoth" -> { _runeRewards.emit(RuneReward.InstantEchoes(50)) }
        }

        if (durationMs > 0) {
            val buff = RuneBuff(
                id = rune.id,
                multiplier = multiplier,
                costReduction = costReduction,
                echoChanceBonus = echoChance,
                expirationTime = System.currentTimeMillis() + durationMs,
                isAutoClick = isAutoClick,
                freezesBuffs = freezesBuffs,
                description = rune.description
            )
            _activeBuffs.update { it + buff }
        }
    }

    fun hideRune() {
        _runeState.value = RuneState.HIDDEN
        _currentRune.value = null
    }

    private fun failRune() {
        _runeState.value = RuneState.FAILED
        hideRune()
    }

    // --- Helpers de Cálculo ---
    fun getTotalRuneMultiplier(): BigDecimal {
        var total = BigDecimal.ONE
        _activeBuffs.value.forEach {
            if (it.multiplier > 1.0) total = total.multiply(BigDecimal.valueOf(it.multiplier))
        }
        return total
    }

    fun getCostReductionMultiplier(): Double {
        val reduction = _activeBuffs.value.sumOf { it.costReduction }
        return (1.0 - reduction).coerceAtLeast(0.1) // Máximo 90% descuento
    }

    fun getEchoChanceBonus(): Double {
        return _activeBuffs.value.sumOf { it.echoChanceBonus }
    }
}