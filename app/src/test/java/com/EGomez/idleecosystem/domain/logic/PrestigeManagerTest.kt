package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class PrestigeManagerTest {

    private val manager = PrestigeManager()

    @Test
    fun `no se gana cordura bajo el umbral`() {
        assertEquals(BigDecimal.ZERO, manager.calculatePotentialPrestige(BigDecimal("999999")))
    }

    @Test
    fun `la cordura es la raiz del conocimiento generado sobre el umbral`() {
        assertEquals(BigDecimal.ONE, manager.calculatePotentialPrestige(BigDecimal("1000000")))
        assertEquals(BigDecimal(2), manager.calculatePotentialPrestige(BigDecimal("4000000")))
        assertEquals(BigDecimal(10), manager.calculatePotentialPrestige(BigDecimal("100000000")))
    }

    @Test
    fun `no crashea con numeros enormes`() {
        // Antes: Math.sqrt(Infinity) -> BigDecimal(Infinity) lanzaba NumberFormatException
        val result = manager.calculatePotentialPrestige(BigDecimal("1e500"))
        assertEquals(248, result.precision() - result.scale())
    }

    @Test
    fun `multiplicador global de cordura`() {
        manager.residualSanity = BigDecimal(25)
        assertEquals(0, BigDecimal("3.5").compareTo(manager.getPrestigeMultiplier()))
    }
}
