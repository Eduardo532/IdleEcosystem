package com.EGomez.idleecosystem.domain.logic

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Un nodo del Árbol de Visiones (v0.21.0): se compra con Visiones y vale para siempre,
 * también después de Despertar. [requires] es el nodo que hay que tener antes.
 */
enum class VisionNode(
    val id: String,
    val title: String,
    val description: String,
    val baseCost: Double,
    val costGrowth: Double,
    val maxLevel: Int,
    val requires: String? = null
) {
    FIRST("first", "La Primera Visión", "Producción x10 por nivel.", 1.0, 2.0, 10),
    SANITY("sanity", "Visión de la Cordura", "Cordura ganada x3 por nivel.", 2.0, 2.0, 10, "first"),
    ETHER("ether", "Visión del Éter", "Éter ganado x2 por nivel.", 2.0, 2.2, 10, "first"),
    MEMORY("memory", "Visión de la Memoria", "Cada ciclo empieza con 10 Ascensiones más por nivel.", 3.0, 2.0, 5, "sanity"),
    HORIZON("horizon", "Visión del Horizonte", "Trascender pide 5 Ascensiones menos por nivel (nunca menos de 10).", 4.0, 2.5, 4, "ether"),
    SLEEPER("sleeper", "Visión del Durmiente", "Ganancias sin conexión x3 por nivel.", 3.0, 2.0, 10, "memory"),
    DREAMER("dreamer", "Visión del Soñador", "Puntos de Sueño x1,5 por nivel.", 5.0, 2.0, 5, "horizon"),
    PROPHET("prophet", "Visión del Profeta", "Visiones ganadas +25 % por nivel.", 8.0, 2.5, 5, "sleeper"),
    ABYSS("abyss", "La Visión del Abismo", "La producción se eleva a ^1,02 por nivel.", 20.0, 3.0, 3, "dreamer");

    /** Visiones que cuesta el próximo nivel. */
    fun cost(level: Int): Int = ceil(baseCost * costGrowth.pow(level)).toInt()

    /** Lo gastado en los primeros [level] niveles (lo que devuelve reiniciar el árbol). */
    fun spent(level: Int): Int = (0 until level).sumOf { cost(it) }

    fun isMaxed(level: Int): Boolean = level >= maxLevel

    companion object {
        fun fromId(id: String): VisionNode? = entries.firstOrNull { it.id == id }
    }
}

/**
 * Hitos del Despertar (v0.21.0): ventajas por número de Despertares que devuelven, de a
 * poco, lo que el Despertar quita.
 */
enum class AwakeningMilestone(val awakenings: Int, val title: String, val description: String) {
    OPEN_EYES(1, "Párpados Abiertos", "Los Hitos de Trascendencia cuentan 5 Trascendencias más."),
    KEPT_ETERNALS(2, "Lo Eterno Recordado", "Pruebas Recordadas y Runas Eternas no se pierden al Despertar."),
    AWAKE_LAB(3, "Laboratorio Despierto", "Los experimentos del Laboratorio no se pierden al Despertar."),
    ETERNAL_SKY(5, "Cielo Eterno", "Las mejoras de las Estrellas y su Polvo no se pierden al Despertar."),
    FULL_SIGHT(10, "Mirada Completa", "Los Hitos de Trascendencia cuentan 12 Trascendencias más.");

    fun isReached(awakenings: Int) = awakenings >= this.awakenings

    companion object {
        fun reached(awakenings: Int): Int = entries.count { it.isReached(awakenings) }
    }
}

/**
 * El Despertar (v0.21.0): la tercera capa de prestigio. Con 10 Trascendencias desde el
 * último Despertar y 5 niveles superados de Pruebas de Trascendencia, reinicia la capa
 * Trascendencia (Éter, mejoras eternas, Laboratorio, Estrellas, consagraciones y la
 * cuenta de Trascendencias) a cambio de Visiones. Se conservan los logros, el Bestiario,
 * el Árbol del Sueño y los niveles de las Pruebas de Trascendencia.
 */
object Awakening {
    /** Trascendencias desde el último Despertar que pide Despertar. */
    const val REQUIRED_TRANSCENDENCES = 10

    /** Niveles superados de Pruebas de Trascendencia (en total) que pide Despertar. */
    const val REQUIRED_TRIAL_LEVELS = 5

    /** Trascendencias (en toda la partida) con las que aparece la capa. */
    const val REVEAL_TRANSCENDENCES = 10

    /** Lo que suman los hitos a las Trascendencias que cuentan para los Hitos de Trascendencia. */
    const val OPEN_EYES_BONUS = 5
    const val FULL_SIGHT_BONUS = 12

    fun isRevealed(lifetimeTranscendences: Int, awakenings: Int): Boolean =
        awakenings > 0 || lifetimeTranscendences >= REVEAL_TRANSCENDENCES

    fun canAwaken(transcendences: Int, trialLevels: Int): Boolean =
        transcendences >= REQUIRED_TRANSCENDENCES && trialLevels >= REQUIRED_TRIAL_LEVELS

    /**
     * Visiones = ⌊√(Trascendencias × log10(1 + Éter ganado desde el último Despertar)) × bono⌋.
     * Como el Éter, crece con las dos: más ciclos y ciclos mejores.
     */
    fun visionGain(transcendences: Int, etherEarned: Double, multiplier: Double = 1.0): Int {
        if (transcendences < REQUIRED_TRANSCENDENCES || etherEarned <= 0.0) return 0
        return floor(sqrt(transcendences * log10(1.0 + etherEarned)) * multiplier + 1e-9).toInt()
    }

    /** Lo que suman los Hitos del Despertar a las Trascendencias de los Hitos de Trascendencia. */
    fun milestoneBonus(awakenings: Int): Int = when {
        AwakeningMilestone.FULL_SIGHT.isReached(awakenings) -> FULL_SIGHT_BONUS
        AwakeningMilestone.OPEN_EYES.isReached(awakenings) -> OPEN_EYES_BONUS
        else -> 0
    }

    /** Si se puede subir [node] a su nivel siguiente. */
    fun canBuy(node: VisionNode, levels: Map<String, Int>, visions: Int): Boolean {
        val level = levels[node.id] ?: 0
        if (node.isMaxed(level) || visions < node.cost(level)) return false
        return node.requires == null || (levels[node.requires] ?: 0) > 0
    }

    /** Visiones que devuelve reiniciar el árbol. */
    fun refund(levels: Map<String, Int>): Int = VisionNode.entries.sumOf { it.spent(levels[it.id] ?: 0) }

    fun productionMultiplier(first: Int): Double = 10.0.pow(first)
    fun sanityMultiplier(sanity: Int): Double = 3.0.pow(sanity)
    fun etherMultiplier(ether: Int): Double = 2.0.pow(ether)
    fun startingAscensions(memory: Int): Int = 10 * memory
    fun offlineMultiplier(sleeper: Int): Double = 3.0.pow(sleeper)
    fun dreamPointsMultiplier(dreamer: Int): Double = 1.5.pow(dreamer)
    fun visionMultiplier(prophet: Int): Double = 1.0 + 0.25 * prophet
    fun productionExponent(abyss: Int): Double = 1.0 + 0.02 * abyss

    /** Ascensiones que pide Trascender con [horizon] niveles de la Visión del Horizonte. */
    fun requiredAscensions(base: Int, horizon: Int): Int = (base - 5 * horizon).coerceAtLeast(minOf(base, 10))
}
