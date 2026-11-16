package com.EGomez.idleecosystem.domain.logic

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * El árbol de refinado (v0.22.1): nodos que se compran con Esencia de los Mi-Go, que da
 * Refinar. Valen para siempre.
 */
enum class RefineNode(
    val id: String,
    val title: String,
    val description: String,
    val baseCost: Double,
    val costGrowth: Double,
    val maxLevel: Int,
    val requires: String? = null
) {
    DRILLS("drills", "Taladros de los Mi-Go", "La Mina va x1,5 más rápido por nivel.", 1.0, 2.0, 5),
    FACETS("facets", "Facetas Infinitas", "El Poder de los cristales x2 por nivel.", 1.0, 2.0, 10, "drills"),
    SEED("seed", "Semilla de Cuarzo", "Los cristales de la Mina nacen un grado más alto por nivel.", 3.0, 3.0, 3, "drills"),
    FUNGAL("fungal", "Pulidor Fungoide", "Lustre al Pulir +50 % por nivel.", 2.0, 2.0, 5, "facets"),
    LENS("lens", "Lente de Yuggoth", "El Poder también multiplica el Éter: x(1 + 0,1 × log10(1 + Poder)) por nivel.", 4.0, 2.5, 5, "facets"),
    VEIN("vein", "Veta Profunda", "La Mina deja un cristal más cada vez por nivel.", 5.0, 3.0, 2, "seed"),
    ECHO("echo", "Eco del Cristal", "Pulir conserva el cristal de mayor grado, dos grados más bajo.", 8.0, 1.0, 1, "fungal"),
    CORE("core", "Núcleo del Planeta", "El multiplicador de los cristales se eleva a ^1,05 por nivel.", 15.0, 3.0, 3, "lens");

    /** Esencia que cuesta el próximo nivel. */
    fun cost(level: Int): Int = ceil(baseCost * costGrowth.pow(level)).toInt()

    /** Lo gastado en los primeros [level] niveles (lo que devuelve reiniciar el árbol). */
    fun spent(level: Int): Int = (0 until level).sumOf { cost(it) }

    fun isMaxed(level: Int): Boolean = level >= maxLevel

    companion object {
        fun fromId(id: String): RefineNode? = entries.firstOrNull { it.id == id }
    }
}

/**
 * Los Cristales de Yuggoth (v0.22.0), inspirados en los minerales de Revolution Idle.
 * Desde el segundo Despertar, la Mina deja un cristal en una casilla vacía de una
 * cuadrícula de 4 × 4 cada pocos segundos. Dos cristales del mismo grado se fusionan en
 * uno del grado siguiente. El Poder de los cristales multiplica la producción; Pulir
 * vacía la cuadrícula a cambio de Lustre, que hace más fuertes y más rápidos los cristales.
 */
object YuggothCrystals {
    const val UNLOCK_AWAKENINGS = 2
    const val SIZE = 4
    const val CELLS = SIZE * SIZE

    /** Segundos entre cristales sin Lustre. */
    const val BASE_SPAWN_SECONDS = 10.0

    /** Pulir pide un cristal de este grado en la cuadrícula. */
    const val POLISH_TIER = 5

    fun isUnlocked(awakenings: Int): Boolean = awakenings >= UNLOCK_AWAKENINGS

    /** El Poder de un cristal de grado [tier]: 3^(grado − 1). */
    fun power(tier: Int): Double = if (tier <= 0) 0.0 else 3.0.pow(tier - 1)

    /** El Poder de la cuadrícula, con el Lustre: Σ 3^(grado − 1) × (1 + 0,1 × Lustre). */
    fun gridPower(grid: List<Int>, lustre: Int = 0, facets: Int = 0): Double =
        grid.sumOf { power(it) } * (1.0 + 0.1 * lustre) * 2.0.pow(facets)

    /** El multiplicador de producción: x(1 + Poder), elevado a ^(1 + 0,05 × Núcleo). */
    fun productionMultiplier(power: Double, core: Int = 0): Double = (1.0 + power).pow(1.0 + 0.05 * core)

    /** Segundos entre cristales: 10 / (1 + 0,05 × Lustre) / 1,5^Taladros. */
    fun spawnSeconds(lustre: Int, drills: Int = 0): Double = BASE_SPAWN_SECONDS / (1.0 + 0.05 * lustre) / 1.5.pow(drills)

    /** Si [a] y [b] se pueden fusionar: casillas distintas con el mismo grado. */
    fun canMerge(grid: List<Int>, a: Int, b: Int): Boolean =
        a != b && a in grid.indices && b in grid.indices && grid[a] > 0 && grid[a] == grid[b]

    /** Fusiona [a] en [b]: [b] sube un grado y [a] queda vacía. */
    fun merge(grid: List<Int>, a: Int, b: Int): List<Int> {
        if (!canMerge(grid, a, b)) return grid
        return grid.toMutableList().also { it[b] = grid[b] + 1; it[a] = 0 }
    }

    /**
     * Fusiona todo lo que se pueda, de los grados bajos a los altos, hasta que no queden
     * parejas. Devuelve la cuadrícula y cuántas fusiones hizo.
     */
    fun mergeAll(grid: List<Int>): Pair<List<Int>, Int> {
        var current = grid
        var merges = 0
        while (true) {
            val pair = current.indices
                .filter { current[it] > 0 }
                .groupBy { current[it] }
                .filterValues { it.size >= 2 }
                .minByOrNull { it.key }
                ?.value ?: break
            current = merge(current, pair[1], pair[0])
            merges++
        }
        return current to merges
    }

    /**
     * Deja [count] cristales de grado [tier] en casillas vacías al azar (los que quepan).
     * Con la Semilla de Cuarzo nacen de grado 1 + nivel; con la Veta Profunda caen 1 + nivel.
     */
    fun spawn(grid: List<Int>, random: Random, tier: Int = 1, count: Int = 1): List<Int> {
        val next = grid.toMutableList()
        repeat(count) {
            val empty = next.indices.filter { next[it] == 0 }
            if (empty.isEmpty()) return next
            next[empty[random.nextInt(empty.size)]] = tier
        }
        return next
    }

    fun canPolish(grid: List<Int>): Boolean = (grid.maxOrNull() ?: 0) >= POLISH_TIER

    /** Lustre al Pulir: ⌊√(Poder sin Lustre / 10) × (1 + 0,5 × Pulidor)⌋. */
    fun lustreGain(grid: List<Int>, fungal: Int = 0): Int =
        if (!canPolish(grid)) 0 else floor(sqrt(gridPower(grid) / 10.0) * (1.0 + 0.5 * fungal) + 1e-9).toInt()

    /** Grados que pierde el cristal que guarda el Eco del Cristal. */
    const val ECHO_TIER_LOSS = 2

    /**
     * La cuadrícula tras Pulir: vacía o, con el Eco del Cristal, solo con el cristal de
     * mayor grado, dos grados más bajo (así no se puede volver a Pulir enseguida).
     */
    fun afterPolish(grid: List<Int>, echo: Boolean): List<Int> {
        val empty = List(grid.size) { 0 }
        if (!echo) return empty
        val best = grid.indices.maxByOrNull { grid[it] } ?: return empty
        return empty.toMutableList().also { it[best] = (grid[best] - ECHO_TIER_LOSS).coerceAtLeast(0) }
    }

    // ---------------------------------------------------------------- Refinar (v0.22.1)

    /** Lustre que pide Refinar. */
    const val REFINE_LUSTRE = 25

    fun canRefine(lustre: Int): Boolean = lustre >= REFINE_LUSTRE

    /** Esencia de los Mi-Go al Refinar: ⌊√(Lustre / 5)⌋. */
    fun essenceGain(lustre: Int): Int = if (!canRefine(lustre)) 0 else floor(sqrt(lustre / 5.0) + 1e-9).toInt()

    /** El Éter que da la Lente de Yuggoth: x(1 + 0,1 × nivel × log10(1 + Poder)). */
    fun etherMultiplier(power: Double, lens: Int): Double = 1.0 + 0.1 * lens * log10(1.0 + power)

    /** Si se puede subir [node] a su nivel siguiente. */
    fun canBuy(node: RefineNode, levels: Map<String, Int>, essence: Int): Boolean {
        val level = levels[node.id] ?: 0
        if (node.isMaxed(level) || essence < node.cost(level)) return false
        return node.requires == null || (levels[node.requires] ?: 0) > 0
    }

    /** Esencia que devuelve reiniciar el árbol. */
    fun refund(levels: Map<String, Int>): Int = RefineNode.entries.sumOf { it.spent(levels[it.id] ?: 0) }
}
