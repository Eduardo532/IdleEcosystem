package com.EGomez.idleecosystem.domain.model

/**
 * Fases de la Marea (v0.17.4): un ciclo que cambia las reglas cada 10 minutos de
 * juego. Corre con el tiempo jugado (no con el reloj del teléfono), así que no obliga
 * a entrar a ninguna hora. Cada fase favorece una forma de jugar.
 */
enum class TidePhase(
    val id: String,
    val title: String,
    val description: String,
    /** Efectos sobre el juego, con los mismos campos que los pactos. */
    val effects: PactEffects,
    /** Cuánto más sube la Locura en esta fase. */
    val madnessGain: Double = 1.0,
    /** Cuánto más rápido baja la Locura en esta fase. */
    val madnessDecay: Double = 1.0
) {
    HIGH(
        "high", "Marea Alta",
        "El mar sube y trae cosas del fondo: más runas y más eventos, pero la Locura sube más rápido.",
        PactEffects(runeFrequency = 1.5, eventFrequency = 1.5),
        madnessGain = 1.5
    ),
    LOW(
        "low", "Marea Baja",
        "Las aguas se retiran y el culto trabaja en calma: más producción y la Locura baja el doble de rápido.",
        PactEffects(production = 1.25),
        madnessDecay = 2.0
    ),
    DEAD(
        "dead", "Marea Muerta",
        "El mar se detiene y el velo se adelgaza: los rituales cuestan y recargan menos, y aparecen más Ecos.",
        PactEffects(ritualCost = 0.7, ritualCooldown = 0.7, echoChance = 1.5)
    );

    companion object {
        /** Cada fase dura 10 minutos de juego. */
        const val PHASE_SECONDS = 600L
        /** La Marea aparece con esta cantidad de Ascensiones. */
        const val REVEAL_AT = 10

        fun at(playSeconds: Long): TidePhase = entries[((playSeconds / PHASE_SECONDS) % entries.size).toInt()]

        fun secondsLeft(playSeconds: Long): Int = (PHASE_SECONDS - playSeconds % PHASE_SECONDS).toInt()

        fun fromId(id: String?): TidePhase? = entries.firstOrNull { it.id == id }
    }
}
