package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.RuneCatalog
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.RuneVariant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

// Estados posibles de una Runa en pantalla
enum class RuneState {
    HIDDEN,
    APPEARING, // Animación de entrada
    ACTIVE,    // Esperando interacción del jugador
    SUCCESS,   // Jugador completó el gesto
    FAILED     // Tiempo agotado o error
}

data class RuneBuff(
    val id: String,
    val multiplier: Double,
    val expirationTime: Long,
    val isAutoClick: Boolean = false,
    val description: String
)

@Singleton
class RuneManager @Inject constructor(
    private val statisticsManager: StatisticsManager,
    private val soundManager: SoundManager // Para feedback auditivo
) {

    // --- ESTADO OBSERVABLE PARA LA UI ---
    private val _currentRune = MutableStateFlow<RuneVariant?>(null)
    val currentRune: StateFlow<RuneVariant?> = _currentRune.asStateFlow()

    private val _runeState = MutableStateFlow(RuneState.HIDDEN)
    val runeState: StateFlow<RuneState> = _runeState.asStateFlow()

    private val _activeBuffs = MutableStateFlow<List<RuneBuff>>(emptyList())
    val activeBuffs: StateFlow<List<RuneBuff>> = _activeBuffs.asStateFlow()

    // --- CONFIGURACIÓN ---
    private var timeSinceLastRune = 0L
    private var runeLifeTime = 0f

    // Intervalo mínimo entre runas
    private val SPAWN_COOLDOWN_SECONDS = 60
    private val SPAWN_CHANCE_PER_TICK = 0.005f

    fun tick(deltaMillis: Long) {
        val currentTime = System.currentTimeMillis()

        // 1. Limpieza de Buffs expirados
        _activeBuffs.update { list ->
            list.filter { it.expirationTime > currentTime }
        }

        // 2. Lógica de Aparición y Vida de la Runa
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
        // Pesos arbitrarios para balance
        // 60% Tier 1 | 30% Tier 2 | 10% Tier 3
        val roll = Random.nextFloat()
        val tier = when {
            roll < 0.60f -> RuneTier.TIER_1_SWIPE
            roll < 0.90f -> RuneTier.TIER_2_HOLD
            else -> RuneTier.TIER_3_TRACE
        }

        val variants = RuneCatalog.getVariantsForTier(tier)
        val totalWeight = variants.sumOf { it.weight }
        val randomWeight = Random.nextInt(totalWeight)
        var currentWeight = 0
        var selectedRune = variants.first()

        for (variant in variants) {
            currentWeight += variant.weight
            if (randomWeight < currentWeight) {
                selectedRune = variant
                break
            }
        }

        // Configurar estado
        _currentRune.value = selectedRune
        _runeState.value = RuneState.ACTIVE
        runeLifeTime = 10f + (tier.ordinal * 5f)

        soundManager.playSound(SoundType.EVENT_APPEAR)
    }

    fun completeRuneInteraction() {
        val rune = _currentRune.value ?: return
        if (_runeState.value != RuneState.ACTIVE) return

        // 1. Efectos visuales/sonoros
        soundManager.playSound(SoundType.ACHIEVEMENT) // Sonido de éxito
        _runeState.value = RuneState.SUCCESS

        // 2. Registrar estadística
        when (rune.tier) {
            RuneTier.TIER_1_SWIPE -> statisticsManager.trackRuneSevered()
            RuneTier.TIER_2_HOLD -> statisticsManager.trackRuneStabilized()
            RuneTier.TIER_3_TRACE -> statisticsManager.trackRuneInscribed()
        }

        // 3. Aplicar el Buff específico
        applyRuneEffect(rune)

        // 4. Resetear temporizador para la siguiente runa
        timeSinceLastRune = 0

    }

    fun hideRune() {
        _runeState.value = RuneState.HIDDEN
        _currentRune.value = null
    }

    private fun failRune() {
        _runeState.value = RuneState.FAILED
        hideRune()
    }

    // --- LÓGICA DE EFECTOS ---
    private fun applyRuneEffect(rune: RuneVariant) {
        val durationMs: Long
        val multiplier: Double
        var isAutoClick = false
        var extraDescription = ""

        when (rune.id) {
            // TIER 1
            "t1_blood" -> { multiplier = 50.0; durationMs = 15_000 }
            "t1_ash" -> { multiplier = 40.0; durationMs = 20_000 }
            "t1_salt" -> { multiplier = 60.0; durationMs = 10_000 }
            "t1_bone" -> { multiplier = 1.0; durationMs = 0; statisticsManager.trackPlayTime(120) }
            "t1_shadow" -> { multiplier = 30.0; durationMs = 30_000 }
            "t1_whisper" -> { multiplier = 10.0; durationMs = 30_000; isAutoClick = true }

            // TIER 2
            "t2_chronos" -> { multiplier = 1.0; durationMs = 20_000; isAutoClick = true }
            "t2_gravity" -> { multiplier = 200.0; durationMs = 20_000 }
            "t2_barrier" -> { multiplier = 1.0; durationMs = 30_000;}
            "t2_tidal" -> { multiplier = 500.0; durationMs = 10_000 }

            // TIER 3
            "t3_cthulhu" -> {
                multiplier = 777.0
                durationMs = 60_000
                statisticsManager.trackEchoesFound(BigDecimal.ONE)
            }
            "t3_nyarlathotep" -> {
                multiplier = 666.0
                durationMs = 60_000
                isAutoClick = true
            }
            "t3_azathoth" -> {
                multiplier = 1.0
                durationMs = 0
                statisticsManager.trackEchoesFound(BigDecimal("50"))
                extraDescription = "+50 Ecos obtenidos"
            }

            else -> {
                when(rune.tier) {
                    RuneTier.TIER_1_SWIPE -> { multiplier = 20.0; durationMs = 15_000 }
                    RuneTier.TIER_2_HOLD -> { multiplier = 100.0; durationMs = 20_000 }
                    RuneTier.TIER_3_TRACE -> { multiplier = 500.0; durationMs = 30_000 }
                }
            }
        }

        if (durationMs > 0) {
            val buff = RuneBuff(
                id = rune.id,
                multiplier = multiplier,
                expirationTime = System.currentTimeMillis() + durationMs,
                isAutoClick = isAutoClick,
                description = if(extraDescription.isNotEmpty()) extraDescription else rune.description
            )
            _activeBuffs.update { it + buff }
        }
    }

    fun getTotalRuneMultiplier(): BigDecimal {
        var total = BigDecimal.ONE
        _activeBuffs.value.forEach {
            if (it.multiplier > 1.0) {
                total = total.multiply(BigDecimal.valueOf(it.multiplier))
            }
        }
        return total
    }
}