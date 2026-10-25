package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class RandomEventManagerTest {

    private val manager = RandomEventManager()

    @Test
    fun `sin haber ascendido solo aparecen eventos de inicio`() {
        repeat(200) {
            val event = manager.getRandomEvent(BigDecimal(100), BigDecimal.ZERO)
            assertNotNull(event)
            assertTrue(event!!.id.startsWith("evt_start"))
        }
    }

    @Test
    fun `tras ascender ya no aparecen eventos de inicio`() {
        repeat(200) {
            val event = manager.getRandomEvent(BigDecimal(100), BigDecimal.ONE)
            assertNotNull(event)
            assertTrue(!event!!.id.startsWith("evt_start"))
        }
    }

    @Test
    fun `la escala del BigDecimal no rompe las condiciones`() {
        // Antes se comparaba con equals: "0.00" != 0 y no aparecía ningún evento
        assertNotNull(manager.getRandomEvent(BigDecimal.ONE, BigDecimal("0.00")))
    }
}
