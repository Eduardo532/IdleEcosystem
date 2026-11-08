package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class RandomEventManagerTest {

    private val manager = RandomEventManager()

    @Test
    fun `without sanity only starter events appear`() {
        repeat(200) {
            val event = manager.getRandomEvent(BigDecimal(100), BigDecimal.ZERO)
            assertNotNull(event)
            assertTrue(event!!.id.startsWith("evt_start"))
        }
    }

    @Test
    fun `with sanity starter events no longer appear`() {
        repeat(200) {
            val event = manager.getRandomEvent(BigDecimal(100), BigDecimal.ONE)
            assertNotNull(event)
            assertTrue(!event!!.id.startsWith("evt_start"))
        }
    }

    @Test
    fun `zero with decimal scale still matches the conditions`() {
        // Before: equals() compared the scale, so 0.00 or 0E-55 (Runa de Mente on zero
        // sanity) matched no condition and events stopped appearing for good.
        assertNotNull(manager.getRandomEvent(BigDecimal.ONE, BigDecimal("0.00")))
        assertNotNull(manager.getRandomEvent(BigDecimal.ONE, BigDecimal.ZERO.setScale(55)))
    }
}
