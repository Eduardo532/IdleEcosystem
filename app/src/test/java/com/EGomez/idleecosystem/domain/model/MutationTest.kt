package com.EGomez.idleecosystem.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MutationTest {

    @Test
    fun `the offer is deterministic, has three options and no repeats`() {
        (0L..200L).forEach { seed ->
            val offer = Mutation.offer(seed)
            assertEquals(Mutation.OFFER_SIZE, offer.size)
            assertEquals(offer.size, offer.toSet().size)
            assertEquals(offer, Mutation.offer(seed))
        }
    }

    @Test
    fun `different ascensions offer different mutations`() {
        val offers = (0L..50L).map { Mutation.offer(it * 7919L + 17L).toSet() }.toSet()
        assertTrue("solo ${offers.size} ofertas distintas", offers.size > 20)
    }

    @Test
    fun `every mutation has a boon and a price`() {
        assertEquals(20, Mutation.entries.size)
        Mutation.entries.forEach { mutation ->
            assertNotEquals(mutation.id, PactEffects(), mutation.effects)
            assertTrue(mutation.title.isNotBlank() && mutation.description.contains("pero"))
            assertEquals(mutation, Mutation.fromId(mutation.id))
        }
        assertEquals(Mutation.entries.size, Mutation.entries.map { it.id }.toSet().size)
    }
}
