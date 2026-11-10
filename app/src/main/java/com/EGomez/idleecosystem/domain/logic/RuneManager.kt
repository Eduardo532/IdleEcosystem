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
    val clickMultiplier: Double = 1.0, // Solo afecta al poder de clic
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

    // Runa de Entropía: la siguiente runa aparece apenas se oculte la actual
    private var forceNextSpawn = false

    private val SPAWN_COOLDOWN_SECONDS = 40
    private val SPAWN_CHANCE_PER_TICK = 0.001f

    /** Frecuencia de las runas según el pacto: escala la espera y la probabilidad. */
    var spawnFrequency: Double = 1.0

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
            if (forceNextSpawn) {
                spawnRune()
            } else if (timeSinceLastRune >= SPAWN_COOLDOWN_SECONDS * 1000 / spawnFrequency.coerceAtLeast(0.1)) {
                if (Random.nextFloat() < SPAWN_CHANCE_PER_TICK * spawnFrequency.toFloat()) {
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
        forceNextSpawn = false
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

        // El ciclo se reinicia ANTES de aplicar el efecto: antes se reiniciaba después
        // y anulaba a la Runa de Entropía.
        timeSinceLastRune = 0
        applyRuneEffect(rune)
    }

    private suspend fun applyRuneEffect(rune: RuneVariant) {
        // Los valores de cada runa viven en RuneEffects (con su rango de producción por nivel)
        val effect = RuneEffects.forRune(rune.id)

        if (rune.id == "t2_entropy") forceNextSpawn = true // La siguiente runa aparece al ocultarse esta
        effect.reward?.let { _runeRewards.emit(it) }

        if (effect.durationMs > 0) {
            val now = System.currentTimeMillis()
            // Si el autoclic dura menos que el potenciador, va en un buff aparte
            val splitAutoClick = effect.isAutoClick && effect.effectiveAutoClickMs < effect.durationMs
            val buffs = mutableListOf(
                RuneBuff(
                    id = rune.id,
                    multiplier = effect.multiplier,
                    clickMultiplier = effect.clickMultiplier,
                    costReduction = effect.costReduction,
                    echoChanceBonus = effect.echoChance,
                    expirationTime = now + effect.durationMs,
                    isAutoClick = effect.isAutoClick && !splitAutoClick,
                    freezesBuffs = effect.freezesBuffs,
                    description = rune.description
                )
            )
            if (splitAutoClick) {
                buffs += RuneBuff(
                    id = "${rune.id}_auto",
                    expirationTime = now + effect.effectiveAutoClickMs,
                    isAutoClick = true,
                    description = rune.description
                )
            }
            _activeBuffs.update { it + buffs }
        }
    }

    fun hideRune() {
        _runeState.value = RuneState.HIDDEN
        _currentRune.value = null
    }

    private fun failRune() {
        // Una runa ignorada reinicia la espera: antes la siguiente podía aparecer de inmediato
        timeSinceLastRune = 0
        _runeState.value = RuneState.FAILED
        hideRune()
    }

    // --- Helpers de Cálculo ---
    /** Multiplicadores de producción de cada runa activa (se suman en BuffMath). */
    fun getActiveProductionMultipliers(): List<Double> =
        _activeBuffs.value.map { it.multiplier }.filter { it > 1.0 }

    /** Multiplicadores de clic de cada runa activa (se suman en BuffMath). */
    fun getActiveClickMultipliers(): List<Double> =
        _activeBuffs.value.map { it.clickMultiplier }.filter { it > 1.0 }

    fun getCostReductionMultiplier(): Double {
        val reduction = _activeBuffs.value.sumOf { it.costReduction }
        return (1.0 - reduction).coerceAtLeast(0.1) // Máximo 90% descuento
    }

    fun getEchoChanceBonus(): Double {
        return _activeBuffs.value.sumOf { it.echoChanceBonus }
    }
}