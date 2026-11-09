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

    @Test
    fun `chain follow-ups never appear at random`() {
        repeat(2_000) {
            val event = manager.getRandomEvent(BigDecimal(100), BigDecimal.ONE, ascensions = 10)!!
            assertTrue("${event.id} es una continuación", !event.isFollowUp)
        }
    }

    @Test
    fun `memories wait for their ascensions`() {
        repeat(2_000) {
            val event = manager.getRandomEvent(BigDecimal(100), BigDecimal.ONE, ascensions = 0)!!
            assertTrue(event.minAscensions == 0)
        }
        val seen = (1..5_000).mapNotNull { manager.getRandomEvent(BigDecimal(100), BigDecimal.ONE, ascensions = 5)?.id }.toSet()
        listOf("evt_ascended_memories", "evt_ascended_echo", "evt_ascended_throne")
            .forEach { assertTrue("$it nunca apareció con 5 Ascensiones", it in seen) }
    }

    @Test
    fun `every chain link points to an existing follow-up`() {
        val all = manager.allEvents()
        val linked = all.flatMap { it.options }.mapNotNull { it.followUpEventId }
        linked.forEach { id ->
            val target = manager.getEvent(id)
            assertNotNull("La cadena apunta a $id, que no existe", target)
            assertTrue("$id debería ser una continuación", target!!.isFollowUp)
        }
        // Y ninguna continuación queda huérfana
        all.filter { it.isFollowUp }.forEach { assertTrue("${it.id} es inalcanzable", it.id in linked) }
    }
}
