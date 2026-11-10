package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.GameStatisticsEntity
import com.EGomez.idleecosystem.domain.model.GameStatistics
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsManager @Inject constructor() {

    // --- MEMORIA RAM ---
    var totalPlayTimeSeconds: Long = 0
    var startTime: Long = System.currentTimeMillis()
    var runTimeSeconds: Long = 0
    var totalOfflineSeconds: Long = 0

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

    // OMNIFIC RUNES
    var runesSevered: Int = 0     // Tier 1 (Swipe)
    var runesStabilized: Int = 0  // Tier 2 (Hold)
    var runesInscribed: Int = 0   // Tier 3 (Trace)

    // Otros
    var totalEventsFound: Int = 0

    var totalEchoes: BigDecimal = BigDecimal.ZERO

    /**
     * Contadores de los logros (v0.17): "clave -> valor". Las claves que empiezan con
     * [RUN_PREFIX] cuentan solo la vida actual y se borran al Ascender.
     */
    val counters: MutableMap<String, BigDecimal> = sortedMapOf()

    // --- CARGA Y GUARDADO ---

    fun loadFromEntity(entity: GameStatisticsEntity?) {
        if (entity == null) return

        totalPlayTimeSeconds = entity.totalPlayTimeSeconds
        startTime = entity.startTime
        runTimeSeconds = entity.runTimeSeconds
        totalOfflineSeconds = entity.totalOfflineSeconds

        lifetimeKnowledge = parseBigDecimal(entity.lifetimeKnowledge)
        runKnowledge = parseBigDecimal(entity.runKnowledge)
        maxKnowledgePerSecond = parseBigDecimal(entity.maxKnowledgePerSecond)

        totalClicks = entity.totalClicks
        manualKnowledgeGenerated = parseBigDecimal(entity.manualKnowledgeGenerated)

        totalPrestiges = entity.totalPrestiges
        lifetimeSanity = parseBigDecimal(entity.lifetimeSanity)
        spentSanity = parseBigDecimal(entity.spentSanity)

        totalEventsFound = entity.totalEventsFound
        totalEchoes = parseBigDecimal(entity.totalEchoes)

        // Cargar estadísticas de Runas
        runesSevered = entity.runesSevered
        runesStabilized = entity.runesStabilized
        runesInscribed = entity.runesInscribed

        counters.clear()
        counters.putAll(decodeCounters(entity.counters))
    }

    fun getEntityForSave(): GameStatisticsEntity {
        return GameStatisticsEntity(
            id = 1,
            totalPlayTimeSeconds = totalPlayTimeSeconds,
            startTime = startTime,
            runTimeSeconds = runTimeSeconds,
            totalOfflineSeconds = totalOfflineSeconds,
            lifetimeKnowledge = lifetimeKnowledge.toString(),
            runKnowledge = runKnowledge.toString(),
            maxKnowledgePerSecond = maxKnowledgePerSecond.toString(),
            totalClicks = totalClicks,
            manualKnowledgeGenerated = manualKnowledgeGenerated.toString(),
            totalPrestiges = totalPrestiges,
            lifetimeSanity = lifetimeSanity.toString(),
            spentSanity = spentSanity.toString(),
            totalEventsFound = totalEventsFound,
            totalEchoes = totalEchoes.toString(),
            runesSevered = runesSevered,
            runesStabilized = runesStabilized,
            runesInscribed = runesInscribed,
            counters = encodeCounters(counters)
        )
    }

    /** Copia inmutable para la UI (el Registro Akáshico). */
    fun snapshot(): GameStatistics = GameStatistics(
        startTime = startTime,
        totalPlayTimeSeconds = totalPlayTimeSeconds,
        totalOfflineSeconds = totalOfflineSeconds,
        runTimeSeconds = runTimeSeconds,
        lifetimeKnowledge = lifetimeKnowledge,
        runKnowledge = runKnowledge,
        maxKnowledgePerSecond = maxKnowledgePerSecond,
        totalClicks = totalClicks,
        manualKnowledgeGenerated = manualKnowledgeGenerated,
        totalPrestiges = totalPrestiges,
        lifetimeSanity = lifetimeSanity,
        spentSanity = spentSanity,
        totalEventsFound = totalEventsFound,
        totalEchoes = totalEchoes,
        runesSevered = runesSevered,
        runesStabilized = runesStabilized,
        runesInscribed = runesInscribed
    )

    // --- LÓGICA DE SEGUIMIENTO ---

    fun trackPlayTime(seconds: Long) {
        totalPlayTimeSeconds += seconds
        runTimeSeconds += seconds
    }

    fun trackOfflineTime(seconds: Long) {
        if (seconds > 0) count(RUN_OFFLINE, BigDecimal(seconds))
        totalOfflineSeconds += seconds
        totalPlayTimeSeconds += seconds
        runTimeSeconds += seconds
    }

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

    fun trackClick() {
        totalClicks++
        count(RUN_CLICKS)
    }

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
    fun trackEventFound() {
        totalEventsFound++
        count(RUN_EVENTS)
    }

    fun trackEchoesFound(amount: BigDecimal) {
        totalEchoes = totalEchoes.add(amount)
        count(RUN_ECHOES, amount)
    }

    fun trackRuneSevered() {
        runesSevered++
        count(RUN_RUNES)
    }

    fun trackRuneStabilized() {
        runesStabilized++
        count(RUN_RUNES)
    }

    fun trackRuneInscribed() {
        runesInscribed++
        count(RUN_RUNES)
    }

    // --- CONTADORES DE LOGROS ---

    fun counter(key: String): BigDecimal = counters[key] ?: BigDecimal.ZERO

    fun count(key: String, amount: BigDecimal = BigDecimal.ONE) {
        if (amount.signum() <= 0) return
        counters[key] = counter(key).add(amount)
    }

    /** Guarda el mayor valor visto (por ejemplo, el mejor multiplicador de la Convergencia). */
    fun record(key: String, value: BigDecimal) {
        if (value > counter(key)) counters[key] = value
    }

    /** Marca algo que pasó al menos una vez. */
    fun flag(key: String) {
        if (counter(key).signum() == 0) counters[key] = BigDecimal.ONE
    }

    /** Al Ascender: se borran los contadores de la vida que termina. */
    fun resetRunCounters() {
        counters.keys.removeAll { it.startsWith(RUN_PREFIX) }
    }

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
        totalEchoes = BigDecimal.ZERO
        runesSevered = 0
        runesStabilized = 0
        runesInscribed = 0
        counters.clear()
    }

    companion object {
        const val RUN_PREFIX = "run."
        const val RUN_CLICKS = "run.clicks"
        const val RUN_EVENTS = "run.events"
        const val RUN_ECHOES = "run.echoes"
        const val RUN_RUNES = "run.runes"
        const val RUN_OFFLINE = "run.offline"
        const val RUN_RITUALS = "run.rituals"

        private val KEY = Regex("[a-z0-9_.]+")

        /** "clave=valor;clave=valor". Las entradas inválidas se ignoran. */
        fun decodeCounters(text: String?): Map<String, BigDecimal> {
            if (text.isNullOrBlank()) return emptyMap()
            val result = sortedMapOf<String, BigDecimal>()
            text.split(';').forEach { entry ->
                val parts = entry.split('=')
                if (parts.size != 2 || !KEY.matches(parts[0])) return@forEach
                val value = parts[1].toBigDecimalOrNull() ?: return@forEach
                if (value.signum() > 0) result[parts[0]] = value
            }
            return result
        }

        fun encodeCounters(counters: Map<String, BigDecimal>): String =
            counters.entries.filter { KEY.matches(it.key) && it.value.signum() > 0 }
                .joinToString(";") { "${it.key}=${it.value.toPlainString()}" }
    }
}