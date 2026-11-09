package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class SystemRevealTest {

    private fun reveal(
        lifetimeKnowledge: String = "0",
        anyUpgradeBought: Boolean = false,
        lifetimeEchoes: String = "0",
        echoes: String = "0",
        canAscend: Boolean = false,
        ascensions: Int = 0,
        sanity: String = "0"
    ) = SystemReveal.compute(
        BigDecimal(lifetimeKnowledge), anyUpgradeBought, BigDecimal(lifetimeEchoes), BigDecimal(echoes),
        canAscend, ascensions, BigDecimal(sanity)
    )

    @Test
    fun `a new game hides everything but research`() {
        assertEquals(SystemReveal(grimoire = false, rituals = false, ascension = false), reveal())
    }

    @Test
    fun `each system appears with its own requirement`() {
        assertEquals(true, reveal(lifetimeKnowledge = "250").grimoire)
        assertEquals(false, reveal(lifetimeKnowledge = "249").grimoire)
        assertEquals(true, reveal(lifetimeEchoes = "1").rituals)
        assertEquals(true, reveal(canAscend = true).ascension)
        // Encontrar un Eco no revela el Grimorio
        assertEquals(false, reveal(lifetimeEchoes = "1").grimoire)
    }

    @Test
    fun `once ascended everything stays visible`() {
        assertEquals(SystemReveal(), reveal(ascensions = 1))
        // Con Cordura guardada (de una partida importada) la Ascensión se ve
        assertEquals(true, reveal(sanity = "3").ascension)
    }
}
