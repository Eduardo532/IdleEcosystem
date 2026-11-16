package com.EGomez.idleecosystem.domain.logic

import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

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
    fun gridPower(grid: List<Int>, lustre: Int = 0): Double = grid.sumOf { power(it) } * (1.0 + 0.1 * lustre)

    /** El multiplicador de producción: x(1 + Poder). */
    fun productionMultiplier(power: Double): Double = 1.0 + power

    /** Segundos entre cristales: 10 / (1 + 0,05 × Lustre). */
    fun spawnSeconds(lustre: Int): Double = BASE_SPAWN_SECONDS / (1.0 + 0.05 * lustre)

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

    /** Deja un cristal de grado 1 en una casilla vacía al azar (si hay). */
    fun spawn(grid: List<Int>, random: Random): List<Int> {
        val empty = grid.indices.filter { grid[it] == 0 }
        if (empty.isEmpty()) return grid
        return grid.toMutableList().also { it[empty[random.nextInt(empty.size)]] = 1 }
    }

    fun canPolish(grid: List<Int>): Boolean = (grid.maxOrNull() ?: 0) >= POLISH_TIER

    /** Lustre al Pulir: ⌊√(Poder sin Lustre / 10)⌋. */
    fun lustreGain(grid: List<Int>): Int = if (!canPolish(grid)) 0 else floor(sqrt(gridPower(grid) / 10.0)).toInt()
}
