package com.EGomez.idleecosystem.domain.logic

import java.math.BigDecimal
import kotlin.math.pow

/**
 * Una Prueba de Trascendencia: un ciclo entero con una restricción. Se supera cinco veces,
 * de la I a la V; cada nivel pide una vida mejor y sube la recompensa, que vale siempre.
 */
enum class TranscendenceTrial(
    val id: String,
    val title: String,
    val restriction: String,
    val reward: String
) {
    HUNGER(
        "hunger", "La Prueba del Hambre",
        "La producción se eleva a ^0,85.",
        "Producción x3 por nivel superado."
    ),
    BROKEN(
        "broken", "La Prueba de la Cordura Quebrada",
        "La Cordura ganada se eleva a ^0,6.",
        "Cordura ganada x1,5 por nivel superado."
    ),
    SOLITUDE(
        "solitude", "La Prueba de la Soledad",
        "Ningún dios acepta pactos durante el ciclo.",
        "Éter ganado +20 % por nivel superado."
    ),
    SILENCE(
        "silence", "La Prueba del Silencio",
        "El clic no produce nada.",
        "Trascender pide 2 Ascensiones menos por nivel superado."
    );

    companion object {
        fun fromId(id: String): TranscendenceTrial? = entries.firstOrNull { it.id == id }
        fun fromIndex(index: Int): TranscendenceTrial? = entries.getOrNull(index - 1)
    }
}

/**
 * Las Pruebas de Trascendencia (v0.20.3), inspiradas en los Desafíos de Eternidad de
 * Antimatter Dimensions, que se completan cinco veces. Desde la 6ª Trascendencia se elige
 * una Prueba para el ciclo siguiente; se supera Trascendiendo después de una vida con el
 * Conocimiento que pide su nivel.
 */
object TranscendenceTrials {
    const val UNLOCK_TRANSCENDENCES = 6
    const val MAX_LEVEL = 5

    /** Exponentes y recompensas. */
    const val HUNGER_EXPONENT = 0.85
    const val BROKEN_EXPONENT = 0.6
    const val SILENCE_ASCENSIONS = 2

    /** El mínimo de Ascensiones para Trascender, ni con el Silencio superado. */
    const val MIN_REQUIRED_ASCENSIONS = 10

    fun isUnlocked(transcendences: Int): Boolean = transcendences >= UNLOCK_TRANSCENDENCES

    /** Conocimiento en una vida que pide el nivel [level] (1..5): 1e12, 1e15 … 1e24. */
    fun goal(level: Int): BigDecimal = BigDecimal.ONE.scaleByPowerOfTen(9 + 3 * level.coerceIn(1, MAX_LEVEL))

    /** El nivel que se intenta tras haber superado [completed]; null si ya están los cinco. */
    fun nextLevel(completed: Int): Int? = if (completed >= MAX_LEVEL) null else completed + 1

    /** Si con [best] en una vida se supera el nivel [level]. */
    fun isMet(level: Int, best: BigDecimal): Boolean = best >= goal(level)

    fun roman(level: Int): String = listOf("I", "II", "III", "IV", "V").getOrElse(level - 1) { level.toString() }

    fun productionReward(hunger: Int): Double = 3.0.pow(hunger)
    fun sanityReward(broken: Int): Double = 1.5.pow(broken)
    fun etherReward(solitude: Int): Double = 1.0 + 0.2 * solitude

    /** Ascensiones que pide Trascender con [silence] niveles del Silencio superados. */
    fun requiredAscensions(base: Int, silence: Int): Int =
        (base - SILENCE_ASCENSIONS * silence).coerceAtLeast(minOf(base, MIN_REQUIRED_ASCENSIONS))
}
