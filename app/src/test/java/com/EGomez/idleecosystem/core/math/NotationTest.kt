package com.EGomez.idleecosystem.core.math

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class NotationTest {

    private fun suffix(value: String) = BigDecimal(value).toBeautifulString(NumberNotation.SUFFIX)
    private fun scientific(value: String) = BigDecimal(value).toBeautifulString(NumberNotation.SCIENTIFIC)

    @Test
    fun `small numbers look the same in both notations`() {
        assertEquals("0", suffix("0"))
        assertEquals(scientific("150.5"), suffix("150.5"))
    }

    @Test
    fun `scientific notation is unchanged`() {
        assertEquals("1.50e+06", scientific("1500000"))
        assertEquals("1.00e+03", scientific("1000"))
    }

    @Test
    fun `suffix notation names the scale`() {
        assertEquals("1.00 K", suffix("1000"))
        assertEquals("123.46 K", suffix("123456"))
        assertEquals("1.23 M", suffix("1234567"))
        assertEquals("2.50 B", suffix("2500000000"))
        assertEquals("1.00 T", suffix("1e12"))
        assertEquals("1.00 Vg", suffix("1e63"))
    }

    @Test
    fun `rounding up crosses to the next name`() {
        assertEquals("1.00 M", suffix("999999.9"))
    }

    @Test
    fun `beyond the last name it falls back to scientific`() {
        assertEquals(scientific("1e66"), suffix("1e66"))
    }

    @Test
    fun `negative numbers keep their sign`() {
        assertEquals("-1.50 M", suffix("-1500000"))
    }
}
