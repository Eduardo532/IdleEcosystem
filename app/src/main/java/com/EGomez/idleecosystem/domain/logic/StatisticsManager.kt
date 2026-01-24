package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.GameStatisticsEntity
import com.EGomez.idleecosystem.domain.model.GameStatistics
import com.EGomez.idleecosystem.domain.model.RuneTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsManager @Inject constructor() {

    private val _stats = MutableStateFlow(GameStatistics())
    val stats: StateFlow<GameStatistics> = _stats.asStateFlow()

    /**
     * Carga las estadísticas desde la entidad de base de datos.
     */
    fun loadFromEntity(entity: GameStatisticsEntity?) {
        if (entity == null) {
            _stats.value = GameStatistics()
            return
        }

        _stats.value = GameStatistics(
            playTimeSeconds = entity.playTimeSeconds,
            clicksManual = entity.clicksManual,
            totalKnowledgeGenerated = try {
                if (entity.totalKnowledgeGenerated.isNotEmpty()) BigDecimal(entity.totalKnowledgeGenerated) else BigDecimal.ZERO
            } catch (e: Exception) { BigDecimal.ZERO },

            maxProductionPerSecond = try {
                if (entity.maxProductionPerSecond.isNotEmpty()) BigDecimal(entity.maxProductionPerSecond) else BigDecimal.ZERO
            } catch (e: Exception) { BigDecimal.ZERO },

            totalPrestigeResets = entity.totalPrestigeResets,

            totalSanitySpent = try {
                if (entity.totalSanitySpent.isNotEmpty()) BigDecimal(entity.totalSanitySpent) else BigDecimal.ZERO
            } catch (e: Exception) { BigDecimal.ZERO },

            totalEchoesFound = try {
                if (entity.totalEchoesFound.isNotEmpty()) BigDecimal(entity.totalEchoesFound) else BigDecimal.ZERO
            } catch (e: Exception) { BigDecimal.ZERO },

            eventsFound = entity.eventsFound,
            offlineTimeSeconds = entity.offlineTimeSeconds,

            // Nuevas estadísticas para el sistema de Runas
            runesSevered = entity.runesSevered,
            runesStabilized = entity.runesStabilized,
            runesInscribed = entity.runesInscribed
        )
    }

    /**
     * Convierte el estado actual en una entidad lista para guardar en Room.
     */
    fun getEntityForSave(): GameStatisticsEntity {
        val s = _stats.value
        return GameStatisticsEntity(
            id = 1,
            playTimeSeconds = s.playTimeSeconds,
            clicksManual = s.clicksManual,
            totalKnowledgeGenerated = s.totalKnowledgeGenerated.toString(),
            maxProductionPerSecond = s.maxProductionPerSecond.toString(),
            totalPrestigeResets = s.totalPrestigeResets,
            totalSanitySpent = s.totalSanitySpent.toString(),
            totalEchoesFound = s.totalEchoesFound.toString(),
            eventsFound = s.eventsFound,
            offlineTimeSeconds = s.offlineTimeSeconds,
            runesSevered = s.runesSevered,
            runesStabilized = s.runesStabilized,
            runesInscribed = s.runesInscribed
        )
    }

    // --- MÉTODOS DE TRACKING ---

    fun trackPlayTime(seconds: Long) {
        _stats.value = _stats.value.copy(playTimeSeconds = _stats.value.playTimeSeconds + seconds)
    }

    fun trackClick() {
        _stats.value = _stats.value.copy(clicksManual = _stats.value.clicksManual + 1)
    }

    fun trackKnowledgeGenerated(amount: BigDecimal, isManual: Boolean) {
        _stats.value = _stats.value.copy(totalKnowledgeGenerated = _stats.value.totalKnowledgeGenerated.add(amount))
    }

    fun trackProductionPeak(amount: BigDecimal) {
        if (amount > _stats.value.maxProductionPerSecond) {
            _stats.value = _stats.value.copy(maxProductionPerSecond = amount)
        }
    }

    fun trackPrestige(sanityGained: BigDecimal) {
        _stats.value = _stats.value.copy(totalPrestigeResets = _stats.value.totalPrestigeResets + 1)
    }

    fun trackSanitySpent(amount: BigDecimal) {
        _stats.value = _stats.value.copy(totalSanitySpent = _stats.value.totalSanitySpent.add(amount))
    }

    fun trackEchoesFound(amount: BigDecimal) {
        _stats.value = _stats.value.copy(totalEchoesFound = _stats.value.totalEchoesFound.add(amount))
    }

    fun trackEventFound() {
        _stats.value = _stats.value.copy(eventsFound = _stats.value.eventsFound + 1)
    }

    fun trackOfflineTime(seconds: Long) {
        _stats.value = _stats.value.copy(offlineTimeSeconds = _stats.value.offlineTimeSeconds + seconds)
    }

    fun hardReset() {
        _stats.value = GameStatistics()
    }

    // --- NUEVO: TRACKING DE RUNAS ---
    fun trackRuneCompleted(tier: RuneTier) {
        _stats.value = when (tier) {
            RuneTier.TIER_1_SWIPE -> _stats.value.copy(runesSevered = _stats.value.runesSevered + 1)
            RuneTier.TIER_2_HOLD -> _stats.value.copy(runesStabilized = _stats.value.runesStabilized + 1)
            RuneTier.TIER_3_TRACE -> _stats.value.copy(runesInscribed = _stats.value.runesInscribed + 1)
        }
    }
}