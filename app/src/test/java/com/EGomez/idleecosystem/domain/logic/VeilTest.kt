package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Trial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class VeilTest {

    private val t = Veil.THRESHOLD

    @Test
    fun `below the veil nothing changes`() {
        assertEquals(0, BigDecimal.ONE.compareTo(Veil.productionFactor(t, broken = false)))
        assertFalse(Veil.isPressing(t, broken = false))
        assertEquals(0, Veil.depth(t))
    }

    @Test
    fun `past the veil production falls with the square`() {
        val ten = t.multiply(BigDecimal.TEN)
        assertTrue(Veil.isPressing(ten, broken = false))
        assertEquals(0, BigDecimal("0.01").compareTo(Veil.productionFactor(ten, broken = false)))
        assertEquals(1, Veil.depth(ten))
        assertEquals(3, Veil.depth(t.multiply(BigDecimal("1000"))))
    }

    @Test
    fun `a broken veil has no cap and multiplies production`() {
        val far = t.multiply(BigDecimal("1e20"))
        assertFalse(Veil.isPressing(far, broken = true))
        assertEquals(0, BigDecimal.valueOf(Veil.BROKEN_PRODUCTION).compareTo(Veil.productionFactor(far, broken = true)))
    }

    @Test
    fun `breaking needs every trial`() {
        assertFalse(Veil.canBreak(Trial.entries.size - 1, broken = false))
        assertTrue(Veil.canBreak(Trial.entries.size, broken = false))
        assertFalse(Veil.canBreak(Trial.entries.size, broken = true))
    }

    @Test
    fun `offline gains stay linear below the veil`() {
        val gain = Veil.offlineGain(BigDecimal.ZERO, BigDecimal(1000), 3600, broken = false)
        assertEquals(0, BigDecimal(3_600_000).compareTo(gain))
    }

    @Test
    fun `offline gains past the veil follow the soft cap`() {
        // Ritmo sin freno de 1 Velo por segundo durante 7 s desde el Velo: K^3 = T^3 + 3*T^3*7 -> K = T * 22^(1/3)
        val gain = Veil.offlineGain(t, t, 7, broken = false)
        val expected = t.multiply(BigDecimal.valueOf(Math.cbrt(22.0) - 1.0))
        val ratio = gain.divide(expected, java.math.MathContext.DECIMAL64).toDouble()
        assertEquals(1.0, ratio, 1e-6)
        // Con el Velo roto es lineal
        assertEquals(0, t.multiply(BigDecimal(7)).compareTo(Veil.offlineGain(t, t, 7, broken = true)))
    }

    @Test
    fun `the approach bar fills in log scale`() {
        assertEquals(0f, Veil.approach(BigDecimal.ZERO), 0f)
        assertEquals(1f, Veil.approach(t), 0f)
        assertTrue(Veil.approach(BigDecimal("1e12")) in 0.4f..0.5f)
    }
}
