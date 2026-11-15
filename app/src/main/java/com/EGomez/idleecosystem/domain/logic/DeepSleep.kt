package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import java.math.BigDecimal
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/**
 * Un nodo del Árbol del Sueño: se compra con Puntos de Sueño y su efecto vale en todos
 * los ciclos, soñados o despiertos. [requires] es el nodo que hay que tener antes.
 */
enum class DreamNode(
    val id: String,
    val title: String,
    val description: String,
    val baseCost: Double,
    val costGrowth: Double,
    val maxLevel: Int,
    val requires: String? = null
) {
    STEPS("steps", "Los Setenta Peldaños", "El Sueño pesa menos: su exponente sube +0,02 por nivel.", 1.0, 1.4, 10),
    FLAME("flame", "La Caverna de la Llama", "Producción x2 por nivel, en todos los ciclos.", 1.0, 1.5, 20, "steps"),
    ULTHAR("ulthar", "Los Gatos de Ulthar", "Poder de clic x3 por nivel, en todos los ciclos.", 1.0, 1.5, 10, "steps"),
    CELEPHAIS("celephais", "Celephaïs", "Cordura ganada x1,25 por nivel.", 3.0, 1.6, 10, "flame"),
    ZOOGS("zoogs", "Los Zoogs del Bosque", "Ganancias sin conexión x2 por nivel.", 3.0, 1.6, 10, "ulthar"),
    INQUANOK("inquanok", "Las Canteras de Inquanok", "Éter ganado +15 % por nivel.", 8.0, 1.8, 10, "celephais"),
    NIGHTGAUNTS("nightgaunts", "Los Descarnados de la Noche", "Puntos de Sueño +25 % por nivel.", 10.0, 2.0, 5, "zoogs"),
    KADATH("kadath", "Kadath en el Yermo Helado", "Despierto, la producción se eleva a ^1,01 por nivel.", 25.0, 2.0, 3, "inquanok");

    /** Puntos de Sueño que cuesta el próximo nivel. */
    fun cost(level: Int): Int = ceil(baseCost * costGrowth.pow(level)).toInt()

    /** Lo gastado en los primeros [level] niveles (lo que devuelve reiniciar el árbol). */
    fun spent(level: Int): Int = (0 until level).sumOf { cost(it) }

    fun isMaxed(level: Int): Boolean = level >= maxLevel

    companion object {
        fun fromId(id: String): DreamNode? = entries.firstOrNull { it.id == id }
    }
}

/**
 * El Sueño Profundo (v0.20.0), inspirado en las Eternidades con desafío de Antimatter
 * Dimensions y en las capas de "realidad alterada" de los idle. Desde la 3ª Trascendencia
 * se puede elegir que el ciclo siguiente sea un Sueño: mientras dura, la producción, el
 * clic y la Cordura se elevan a ^[BASE_EXPONENT]. Al Trascender desde el Sueño se ganan
 * Puntos de Sueño según el mejor Conocimiento de una vida del ciclo, y con ellos se
 * compra el Árbol del Sueño, cuyos efectos valen siempre.
 */
object DeepSleep {
    /** Trascendencias que abren el Sueño. */
    const val UNLOCK_TRANSCENDENCES = 3

    /** El exponente del Sueño sin nodos. */
    const val BASE_EXPONENT = 0.75

    /** Lo que suma cada nivel de Los Setenta Peldaños al exponente. */
    const val STEP_EXPONENT = 0.02

    /** Por debajo de 10^[POINTS_FLOOR] de Conocimiento el Sueño no da Puntos. */
    const val POINTS_FLOOR = 10.0

    fun isUnlocked(transcendences: Int): Boolean = transcendences >= UNLOCK_TRANSCENDENCES

    /** El exponente del Sueño con [steps] niveles de Los Setenta Peldaños (nunca más de 1). */
    fun exponent(steps: Int): Double = (BASE_EXPONENT + STEP_EXPONENT * steps).coerceAtMost(1.0)

    /**
     * Los Puntos de Sueño que vale en total un Sueño con [best] de Conocimiento en una vida:
     * ⌊(log10(best) − 10)^1,5 × bono⌋. Solo se cobra lo que supere lo ya ganado.
     */
    fun totalPoints(best: BigDecimal, multiplier: Double = 1.0): Int {
        if (best <= BigDecimal.ONE) return 0
        val over = BigMath.ln(best) / Math.log(10.0) - POINTS_FLOOR
        if (over <= 0.0) return 0
        return floor(over.pow(1.5) * multiplier + 1e-9).toInt()
    }

    /** Lo que se cobra al despertar: lo que supera a lo ya ganado en Sueños anteriores. */
    fun gain(best: BigDecimal, earned: Int, multiplier: Double = 1.0): Int =
        (totalPoints(best, multiplier) - earned).coerceAtLeast(0)

    /** Si se puede subir [node] a su nivel siguiente. */
    fun canBuy(node: DreamNode, levels: Map<String, Int>, points: Int): Boolean {
        val level = levels[node.id] ?: 0
        if (node.isMaxed(level) || points < node.cost(level)) return false
        return node.requires == null || (levels[node.requires] ?: 0) > 0
    }

    /** Puntos que devuelve reiniciar el árbol con [levels]. */
    fun refund(levels: Map<String, Int>): Int =
        DreamNode.entries.sumOf { it.spent(levels[it.id] ?: 0) }

    fun productionMultiplier(flame: Int): Double = 2.0.pow(flame)
    fun clickMultiplier(ulthar: Int): Double = 3.0.pow(ulthar)
    fun sanityMultiplier(celephais: Int): Double = 1.25.pow(celephais)
    fun offlineMultiplier(zoogs: Int): Double = 2.0.pow(zoogs)
    fun etherMultiplier(inquanok: Int): Double = 1.0 + 0.15 * inquanok
    fun pointsMultiplier(nightgaunts: Int): Double = 1.0 + 0.25 * nightgaunts

    /** El exponente de producción de Kadath, solo fuera del Sueño. */
    fun kadathExponent(kadath: Int): Double = 1.0 + 0.01 * kadath
}
