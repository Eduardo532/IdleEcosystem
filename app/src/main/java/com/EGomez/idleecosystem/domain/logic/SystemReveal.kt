package com.EGomez.idleecosystem.domain.logic

import java.math.BigDecimal

/**
 * Qué sistemas ya descubrió el jugador. Hasta entonces, su pestaña o panel aparece
 * como "???" con el requisito para descubrirlo: el jugador no ve de golpe todo lo que
 * queda, y cada descubrimiento es un pequeño evento.
 *
 * Las condiciones usan estadísticas de toda la partida (Conocimiento y Ecos totales,
 * Ascensiones), así que un sistema descubierto no vuelve a esconderse al gastar,
 * Ascender ni cerrar el juego. Solo el reinicio total los esconde de nuevo.
 */
data class SystemReveal(
    val grimoire: Boolean = true,
    val rituals: Boolean = true,
    val ascension: Boolean = true
) {
    companion object {
        /** Conocimiento que revela el Grimorio: lo que cuesta la mejora más barata. */
        val GRIMOIRE_AT: BigDecimal = BigDecimal("250")

        fun compute(
            lifetimeKnowledge: BigDecimal,
            anyUpgradeBought: Boolean,
            lifetimeEchoes: BigDecimal,
            echoes: BigDecimal,
            canAscend: Boolean,
            ascensions: Int,
            sanity: BigDecimal
        ): SystemReveal {
            val ascended = ascensions > 0
            return SystemReveal(
                grimoire = ascended || anyUpgradeBought || lifetimeKnowledge >= GRIMOIRE_AT,
                rituals = ascended || lifetimeEchoes.signum() > 0 || echoes.signum() > 0,
                ascension = ascended || canAscend || sanity.signum() > 0
            )
        }
    }
}
