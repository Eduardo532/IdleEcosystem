package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.GameStatisticsEntity
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsManager @Inject constructor() {

    // --- MEMORIA RAM ---
    var totalPlayTimeSeconds: Long = 0
    var startTime: Long = System.currentTimeMillis()
    var runTimeSeconds: Long = 0
    var totalOfflineSeconds: Long = 0 // <--- NUEVO

    // Economía
    var lifetimeKnowledge: BigDecimal = BigDecimal.ZERO
    var runKnowledge: BigDecimal = BigDecimal.ZERO
    var maxKnowledgePerSecond: BigDecimal = BigDecimal.ZERO

    // Interacción
    var totalClicks: Long = 0
    var manualKnowledgeGenerated: BigDecimal = BigDecimal.ZERO

    // Prestigio
    var totalPrestiges: Int = 0
    var lifetimeSanity: BigDecimal = BigDecimal.ZERO
    var spentSanity: BigDecimal = BigDecimal.ZERO

    // Otros
    var totalEventsFound: Int = 0

    // --- CARGA Y GUARDADO ---

    fun loadFromEntity(entity: GameStatisticsEntity?) {
        if (entity == null) return

        totalPlayTimeSeconds = entity.totalPlayTimeSeconds
        startTime = entity.startTime
        runTimeSeconds = entity.runTimeSeconds
        totalOfflineSeconds = entity.totalOfflineSeconds // <--- CARGAR

        lifetimeKnowledge = parseBigDecimal(entity.lifetimeKnowledge)
        runKnowledge = parseBigDecimal(entity.runKnowledge)
        maxKnowledgePerSecond = parseBigDecimal(entity.maxKnowledgePerSecond)

        totalClicks = entity.totalClicks
        manualKnowledgeGenerated = parseBigDecimal(entity.manualKnowledgeGenerated)

        totalPrestiges = entity.totalPrestiges
        lifetimeSanity = parseBigDecimal(entity.lifetimeSanity)
        spentSanity = parseBigDecimal(entity.spentSanity)

        totalEventsFound = entity.totalEventsFound
    }

    fun getEntityForSave(): GameStatisticsEntity {
        return GameStatisticsEntity(
            id = 1,
            totalPlayTimeSeconds = totalPlayTimeSeconds,
            startTime = startTime,
            runTimeSeconds = runTimeSeconds,
            totalOfflineSeconds = totalOfflineSeconds, // <--- GUARDAR
            lifetimeKnowledge = lifetimeKnowledge.toString(),
            runKnowledge = runKnowledge.toString(),
            maxKnowledgePerSecond = maxKnowledgePerSecond.toString(),
            totalClicks = totalClicks,
            manualKnowledgeGenerated = manualKnowledgeGenerated.toString(),
            totalPrestiges = totalPrestiges,
            lifetimeSanity = lifetimeSanity.toString(),
            spentSanity = spentSanity.toString(),
            totalEventsFound = totalEventsFound
        )
    }

    // --- LÓGICA DE SEGUIMIENTO ---

    fun trackPlayTime(seconds: Long) {
        // Esto cuenta como tiempo ONLINE (activo) o TOTAL,
        // pero para evitar líos, usaremos esto solo para el loop del juego (online).
        // El tiempo total = Online + Offline.
        // Así que sumamos al total y al run.
        totalPlayTimeSeconds += seconds
        runTimeSeconds += seconds
    }

    // NUEVA FUNCIÓN ESPECÍFICA PARA OFFLINE
    fun trackOfflineTime(seconds: Long) {
        totalOfflineSeconds += seconds

        // El tiempo offline TAMBIÉN es tiempo de existencia, así que sumamos al total
        totalPlayTimeSeconds += seconds
        // Y al run actual
        runTimeSeconds += seconds
    }

    // --- GETTERS CALCULADOS ---
    fun getActivePlayTime(): Long {
        return totalPlayTimeSeconds - totalOfflineSeconds
    }

    fun trackKnowledgeGenerated(amount: BigDecimal, isManual: Boolean) {
        if (amount <= BigDecimal.ZERO) return
        lifetimeKnowledge = lifetimeKnowledge.add(amount)
        runKnowledge = runKnowledge.add(amount)
        if (isManual) {
            manualKnowledgeGenerated = manualKnowledgeGenerated.add(amount)
        }
    }

    fun trackClick() { totalClicks++ }

    fun trackProductionPeak(currentProduction: BigDecimal) {
        if (currentProduction > maxKnowledgePerSecond) {
            maxKnowledgePerSecond = currentProduction
        }
    }

    fun trackPrestige(sanityGained: BigDecimal) {
        totalPrestiges++
        lifetimeSanity = lifetimeSanity.add(sanityGained)
        runTimeSeconds = 0
        runKnowledge = BigDecimal.ZERO
    }

    fun trackSanitySpent(amount: BigDecimal) { spentSanity = spentSanity.add(amount) }
    fun trackEventFound() { totalEventsFound++ }

    private fun parseBigDecimal(value: String): BigDecimal {
        return try { BigDecimal(value) } catch (e: Exception) { BigDecimal.ZERO }
    }

    fun hardReset() {
        totalPlayTimeSeconds = 0
        startTime = System.currentTimeMillis()
        runTimeSeconds = 0
        totalOfflineSeconds = 0
        lifetimeKnowledge = BigDecimal.ZERO
        runKnowledge = BigDecimal.ZERO
        maxKnowledgePerSecond = BigDecimal.ZERO
        totalClicks = 0
        manualKnowledgeGenerated = BigDecimal.ZERO
        totalPrestiges = 0
        lifetimeSanity = BigDecimal.ZERO
        spentSanity = BigDecimal.ZERO
        totalEventsFound = 0
    }
}