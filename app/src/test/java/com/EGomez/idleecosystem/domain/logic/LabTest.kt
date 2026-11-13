package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext

class LabTest {

    @Test
    fun `exponents grow by level and costs too`() {
        assertEquals(1.0, LabExperiment.ARMITAGE.exponent(0), 1e-12)
        assertEquals(1.1, LabExperiment.ARMITAGE.exponent(10), 1e-12)
        assertEquals(1.25, LabExperiment.PNAKOTIC.exponent(5), 1e-12)
        assertEquals(3, LabExperiment.ARMITAGE.cost(0))
        assertTrue(LabExperiment.ARMITAGE.cost(5) > LabExperiment.ARMITAGE.cost(4))
        assertTrue(LabExperiment.DYER.isMaxed(5))
    }

    @Test
    fun `raising to a power works for small and huge numbers`() {
        val small = Lab.pow(BigDecimal(100), 1.5)
        assertEquals(1000.0, small.toDouble(), 1e-6)
        // 1e300 ^ 1,1 = 1e330: más allá de Double
        val huge = Lab.pow(BigDecimal("1e300"), 1.1)
        assertEquals(330, BigMath.exponent10(huge))
        // Lo que no supera 1 no se toca
        assertEquals(0, BigDecimal("0.5").compareTo(Lab.pow(BigDecimal("0.5"), 2.0)))
        assertEquals(4.0, Lab.pow(2.0, 2.0), 1e-12)
        assertEquals(0.5, Lab.pow(0.5, 2.0), 1e-12)
    }

    @Test
    fun `the factor turns a total into its power`() {
        val total = BigDecimal("1e20")
        val factor = Lab.factorFor(total, 1.05)
        val raised = total.multiply(factor, MathContext.DECIMAL64)
        assertEquals(21, BigMath.exponent10(raised))
        assertEquals(0, BigDecimal.ONE.compareTo(Lab.factorFor(total, 1.0)))
    }

    @Test
    fun `the veil equation moves the veil ten times per level`() {
        assertEquals(0, Veil.THRESHOLD.compareTo(Lab.veilThreshold(0)))
        assertEquals(0, Veil.THRESHOLD.multiply(BigDecimal(1000)).compareTo(Lab.veilThreshold(3)))
        val moved = Lab.veilThreshold(2)
        assertTrue(!Veil.isPressing(Veil.THRESHOLD.multiply(BigDecimal.TEN), broken = false, threshold = moved))
    }
}
