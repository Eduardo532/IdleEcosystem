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

    // ---------------------------------------------------------------- v0.26.1

    private fun seen(progress: com.EGomez.idleecosystem.domain.model.EventProgress, ascensions: Int = 10): Set<String> =
        (1..6_000).mapNotNull { manager.getRandomEvent(BigDecimal(100), BigDecimal.ONE, ascensions, progress)?.id }.toSet()

    @Test
    fun `events with memory wait for the choices they remember`() {
        val none = seen(com.EGomez.idleecosystem.domain.model.EventProgress(transcendences = 5, awakenings = 5))
        assertTrue("evt_mem_cats_1" in none)
        assertTrue("evt_mem_letters_1" in none)
        manager.allEvents().filter { it.remembers }.forEach { assertTrue("${it.id} sin memoria", it.id !in none) }

        val fed = seen(
            com.EGomez.idleecosystem.domain.model.EventProgress(setOf(RandomEventManager.CATS_FED), transcendences = 1)
        )
        assertTrue("evt_mem_cats_fed" in fed)
        // El primer encuentro ya ocurrió: no vuelve
        assertTrue("evt_mem_cats_1" !in fed)
        // El consejo pide un Despertar
        assertTrue("evt_mem_cats_council" !in fed)
    }

    @Test
    fun `an encounter happens once and the story moves on`() {
        val gifts = com.EGomez.idleecosystem.domain.model.EventProgress(
            setOf(RandomEventManager.CATS_FED, RandomEventManager.CATS_GIFTS), transcendences = 1, awakenings = 1
        )
        val ids = seen(gifts)
        assertTrue("evt_mem_cats_fed" !in ids)
        assertTrue("evt_mem_cats_council" in ids)
    }

    @Test
    fun `every remembered choice can be made and the three witnesses close it all`() {
        val all = manager.allEvents()
        val set = all.flatMap { e -> e.options.flatMap { it.memories } }.toSet()
        all.flatMap { it.requiresAll + it.requiresAny }.forEach { assertTrue("$it no se puede recordar", it in set) }
        val witnesses = manager.getEvent("evt_mem_witnesses")!!
        assertTrue(witnesses.requiresAll.containsAll(listOf(RandomEventManager.LETTERS_END, RandomEventManager.GUEST_END)))
        assertTrue(RandomEventManager.WITNESSES in set)
        val ready = com.EGomez.idleecosystem.domain.model.EventProgress(
            setOf(RandomEventManager.CATS_BITTER, RandomEventManager.LETTERS_END, RandomEventManager.GUEST_END), 9, 3
        )
        assertTrue(witnesses.allows(ready))
        assertTrue(!witnesses.allows(ready.copy(awakenings = 2)))
        assertTrue(!witnesses.allows(ready.copy(memories = ready.memories + RandomEventManager.WITNESSES)))
    }
}
