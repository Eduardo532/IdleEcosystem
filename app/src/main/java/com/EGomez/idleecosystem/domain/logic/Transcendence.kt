package com.EGomez.idleecosystem.domain.logic

import java.math.BigDecimal
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Mejoras eternas (v0.18.0): se compran con Éter y sobreviven a Trascender.
 * [maxLevel] null = sin tope.
 */
enum class EternalUpgrade(
    val id: String,
    val title: String,
    val description: String,
    val baseCost: Int,
    val costGrowth: Double,
    val maxLevel: Int?
) {
    MEMORY("memory", "Memoria Ancestral", "Cada ciclo empieza con 5 Ascensiones por nivel (cuentan para los hitos, la Atención, los pactos y las Pruebas).", 1, 2.0, 10),
    ETERNAL_FIRE("fire", "Fuego Eterno", "Producción x2 por nivel.", 1, 1.6, null),
    PRIMORDIAL_SANITY("sanity", "Cordura Primordial", "Cordura ganada al Ascender x1,5 por nivel.", 2, 1.8, null),
    ETHER_ECHO("ether", "Eco del Éter", "Éter ganado al Trascender x1,25 por nivel.", 3, 2.0, 5),
    REMEMBERED_TRIALS("trials", "Pruebas Recordadas", "Las Pruebas superadas no se pierden al Trascender.", 5, 1.0, 1),
    ETERNAL_RUNES("runes", "Runas Eternas", "Las runas grabadas y sus capturas no se pierden al Trascender.", 4, 1.0, 1);

    /** Costo en Éter del próximo nivel. */
    fun cost(level: Int): Int = ceil(baseCost * costGrowth.pow(level)).toInt()

    fun isMaxed(level: Int): Boolean = maxLevel != null && level >= maxLevel

    companion object {
        fun fromId(id: String): EternalUpgrade? = entries.firstOrNull { it.id == id }
    }
}

/**
 * Trascender (v0.18.0): la segunda capa de prestigio. Con 50 Ascensiones en el ciclo
 * se puede reiniciar casi todo (Cordura, Mapa de Locura, Sendas, Ascensiones, Pruebas y
 * runas grabadas; nunca los logros) a cambio de Éter, que compra mejoras eternas.
 */
object Transcendence {
    /** Ascensiones del ciclo que pide Trascender. */
    const val REQUIRED_ASCENSIONS = 50
    /** Ascensiones con las que empieza el ciclo por nivel de Memoria Ancestral. */
    const val ASCENSIONS_PER_MEMORY = 5

    /**
     * Éter = ⌊√(Ascensiones del ciclo × log10(1 + Cordura ganada en el ciclo)) × bono⌋.
     * Crece despacio con ambas: más vidas y vidas mejores.
     */
    fun etherGain(cycleAscensions: Int, cycleSanity: BigDecimal, multiplier: Double = 1.0, required: Int = REQUIRED_ASCENSIONS): Int {
        if (cycleAscensions < required || cycleSanity.signum() <= 0) return 0
        val logSanity = log10(1.0 + cycleSanity.toDouble().coerceAtMost(Double.MAX_VALUE / 2))
        return floor(sqrt(cycleAscensions * logSanity) * multiplier).toInt()
    }

    fun productionMultiplier(fireLevel: Int): Double = 2.0.pow(fireLevel)
    fun sanityMultiplier(level: Int): Double = 1.5.pow(level)
    fun etherMultiplier(level: Int): Double = 1.25.pow(level)
}

/**
 * Hitos de Trascendencia (v0.19.5): como los de Ascensión, pero por Trascendencias.
 * Quitan lo tedioso de volver a empezar un ciclo: la automatización, las primeras
 * estrellas del Mapa y la Senda se conservan, y Trascender se vuelve automático y más
 * corto. (Inspirados en los Eternity Milestones de Antimatter Dimensions y Revolution Idle.)
 */
enum class TranscendenceMilestone(val transcendences: Int, val title: String, val description: String) {
    MACHINE_MEMORY(1, "Memoria de la Máquina", "Las automatizaciones de los hitos de Ascensión funcionan desde el inicio de cada ciclo."),
    REMEMBERED_SKY(2, "Cielo Recordado", "Cada ciclo empieza con El Despertar y la primera estrella de cada constelación a nivel 1."),
    ETERNAL_PATH(3, "Senda Eterna", "La Senda que recorres y sus niveles no se pierden al Trascender."),
    AUTO_TRANSCEND(5, "Trascendencia Automática", "Puedes Trascender sola cuando el Éter que ganarías iguale al que tienes."),
    OPEN_EYES(8, "Mirada Antigua", "La Atención cuenta todas tus Ascensiones desde siempre, no solo las del ciclo."),
    SHORTCUT(12, "El Atajo", "Trascender pide 40 Ascensiones en el ciclo en lugar de 50.");

    fun isReached(transcendences: Int) = transcendences >= this.transcendences

    companion object {
        /** Ascensiones del ciclo que pide Trascender con El Atajo. */
        const val SHORTCUT_ASCENSIONS = 40

        fun reached(transcendences: Int): Int = entries.count { it.isReached(transcendences) }

        fun next(transcendences: Int): TranscendenceMilestone? = entries.firstOrNull { !it.isReached(transcendences) }

        /** Ascensiones del ciclo que pide Trascender. */
        fun requiredAscensions(transcendences: Int): Int =
            if (SHORTCUT.isReached(transcendences)) SHORTCUT_ASCENSIONS else Transcendence.REQUIRED_ASCENSIONS

        /**
         * La regla de la Trascendencia automática: cuando el Éter que se gana es al menos
         * el que se tiene (lo duplica), y nunca por menos de 1.
         */
        fun shouldAutoTranscend(gain: Int, ether: Int): Boolean = gain > 0 && gain >= maxOf(1, ether)
    }
}
