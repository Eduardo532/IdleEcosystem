package com.EGomez.idleecosystem.domain.logic

import kotlin.math.ceil
import kotlin.math.pow

/** El árbol del Caos (v0.25.0): nodos permanentes que se compran con Caos. */
enum class ChaosNode(
    val id: String,
    val title: String,
    val description: String,
    val baseCost: Double,
    val costGrowth: Double,
    val maxLevel: Int,
    val requires: String? = null
) {
    FLUTES("flutes", "Las Flautas Idiotas", "Producción x100 por nivel.", 1.0, 2.0, 5),
    DAEMON("daemon", "El Sultán Demonio", "Visiones ganadas x2 por nivel.", 2.0, 2.0, 4, "flutes"),
    NUCLEAR("nuclear", "El Caos Nuclear", "Éter ganado x10 por nivel.", 2.0, 2.0, 4, "flutes"),
    GATES("gates", "Las Puertas Exteriores", "Descender pide 1 Despertar menos por nivel.", 5.0, 2.0, 3, "daemon"),
    ECHOES("echoes", "Ecos del Caos", "Caos al Descender +2 por nivel.", 4.0, 2.0, 3, "nuclear"),
    DANCERS("dancers", "Los Planetas Danzantes", "Favor, velocidad de la Mina y daño de Expedición x2 por nivel.", 6.0, 2.0, 3, "gates"),
    THRONE("throne", "El Trono del Caos", "La producción se eleva a ^1,05 por nivel.", 10.0, 2.0, 3, "echoes");

    /** Caos que cuesta el próximo nivel. */
    fun cost(level: Int): Int = ceil(baseCost * costGrowth.pow(level) - 1e-9).toInt()

    fun isMaxed(level: Int): Boolean = level >= maxLevel

    companion object {
        fun fromId(id: String): ChaosNode? = entries.firstOrNull { it.id == id }
    }
}

/**
 * El Sueño de Azathoth (v0.25.0): la última capa. Con El Rey de Amarillo completado y
 * suficientes Despertares en el Descenso, Descender reinicia la capa Despertar (Visiones,
 * Árbol de Visiones, Juramentos, la Carta, la Expedición, los Cristales y el Favor) a cambio
 * de Caos, que compra el árbol del Caos. Lo comprado en los minijuegos se conserva.
 */
object AzathothDream {
    /** Despertares por Descenso, sin Puertas Exteriores. */
    const val REQUIRED_AWAKENINGS = 8

    fun isRevealed(cardsCompleted: Int, descents: Int): Boolean = cardsCompleted > 0 || descents > 0

    fun requiredAwakenings(gates: Int): Int = (REQUIRED_AWAKENINGS - gates).coerceAtLeast(1)

    fun canDescend(kingCompleted: Boolean, awakeningsSince: Int, gates: Int): Boolean =
        kingCompleted && awakeningsSince >= requiredAwakenings(gates)

    /** Caos al Descender: 2 por cada Despertar desde el mínimo (incluido) y +2 por nivel de Ecos. */
    fun chaosGain(kingCompleted: Boolean, awakeningsSince: Int, gates: Int, echoes: Int): Int {
        if (!canDescend(kingCompleted, awakeningsSince, gates)) return 0
        return 2 * (awakeningsSince - requiredAwakenings(gates) + 1) + 2 * echoes
    }

    fun canBuy(node: ChaosNode, levels: Map<String, Int>, chaos: Int): Boolean {
        val level = levels[node.id] ?: 0
        if (node.isMaxed(level) || chaos < node.cost(level)) return false
        return node.requires == null || (levels[node.requires] ?: 0) > 0
    }

    // Efectos
    fun productionMultiplier(flutes: Int): Double = 100.0.pow(flutes)
    fun visionMultiplier(daemon: Int): Double = 2.0.pow(daemon)
    fun etherMultiplier(nuclear: Int): Double = 10.0.pow(nuclear)
    fun dancersMultiplier(dancers: Int): Double = 2.0.pow(dancers)
    fun productionExponent(throne: Int): Double = 1.05.pow(throne)
}
