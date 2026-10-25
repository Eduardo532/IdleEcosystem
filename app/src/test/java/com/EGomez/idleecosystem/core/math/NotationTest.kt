package com.EGomez.idleecosystem.core.math

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class NotationTest {

    @Test
    fun `numeros pequenos usan punto decimal sin importar el idioma`() {
        assertEquals("0", BigDecimal.ZERO.toBeautifulString(NumberNotation.SCIENTIFIC))
        assertEquals("150.5", BigDecimal("150.5").toBeautifulString(NumberNotation.SCIENTIFIC))
        assertEquals("999.99", BigDecimal("999.99").toBeautifulString(NumberNotation.SUFFIX))
        assertEquals("-12.5", BigDecimal("-12.5").toBeautifulString(NumberNotation.SUFFIX))
    }

    @Test
    fun `notacion cientifica`() {
        assertEquals("1.50e+06", BigDecimal("1500000").toBeautifulString(NumberNotation.SCIENTIFIC))
        assertEquals("1.00e+400", BigDecimal("1e400").toBeautifulString(NumberNotation.SCIENTIFIC))
    }

    @Test
    fun `notacion con sufijos`() {
        assertEquals("1.00K", BigDecimal("1000").toBeautifulString(NumberNotation.SUFFIX))
        assertEquals("1.50M", BigDecimal("1500000").toBeautifulString(NumberNotation.SUFFIX))
        assertEquals("12.35B", BigDecimal("12345678901").toBeautifulString(NumberNotation.SUFFIX))
        assertEquals("-2.00K", BigDecimal("-2000").toBeautifulString(NumberNotation.SUFFIX))
    }

    @Test
    fun `el redondeo pasa al siguiente sufijo`() {
        assertEquals("1.00M", BigDecimal("999999").toBeautifulString(NumberNotation.SUFFIX))
    }

    @Test
    fun `mas alla de los sufijos conocidos se usa notacion cientifica`() {
        val huge = BigDecimal(BigInteger.TEN.pow(200))
        assertEquals("1.00e+200", huge.toBeautifulString(NumberNotation.SUFFIX))
    }

    @Test
    fun `formato de tiempo`() {
        assertEquals("0s", formatSecondsToTime(0))
        assertEquals("45s", formatSecondsToTime(45))
        assertEquals("1h 0m 5s", formatSecondsToTime(3605))
        assertEquals("2d 3h 0m 0s", formatSecondsToTime(2 * 86400 + 3 * 3600))
    }
}
