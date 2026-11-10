package com.EGomez.idleecosystem.domain.logic

import kotlin.math.pow

/**
 * Consagrar edificios (v0.18.1): tras la primera Trascendencia, cada edificio se puede
 * consagrar hasta 5 veces con Éter. Cada nivel multiplica su producción por 3 y es
 * eterno: no se pierde al Ascender ni al Trascender.
 *
 * Para consagrar hace falta tener, en la vida actual, 100 unidades por nivel nuevo
 * (100 para el I, 200 para el II…), así que también es una meta dentro de cada vida.
 */
object Consecration {
    const val MAX_LEVEL = 5
    const val UNITS_PER_LEVEL = 100
    const val MULTIPLIER_PER_LEVEL = 3.0

    /** Unidades que hay que tener para subir al nivel [level] + 1. */
    fun requiredOwned(level: Int): Int = UNITS_PER_LEVEL * (level + 1)

    /** Éter que cuesta subir al nivel [level] + 1. */
    fun etherCost(level: Int): Int = level + 1

    fun multiplier(level: Int): Double = MULTIPLIER_PER_LEVEL.pow(level.coerceIn(0, MAX_LEVEL))

    fun canConsecrate(level: Int, owned: Int, ether: Int): Boolean =
        level < MAX_LEVEL && owned >= requiredOwned(level) && ether >= etherCost(level)

    /** Número romano del nivel (I a V). */
    fun roman(level: Int): String = listOf("", "I", "II", "III", "IV", "V").getOrElse(level) { level.toString() }
}
