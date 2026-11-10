package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AttentionTest {

    @Test
    fun `points grow with ascensions up to ten`() {
        assertEquals(0, Attention.available(0))
        assertEquals(1, Attention.available(1))
        assertEquals(1, Attention.available(2))
        assertEquals(2, Attention.available(3))
        assertEquals(10, Attention.available(27))
        assertEquals(10, Attention.available(500))
    }

    @Test
    fun `the next point is announced until the last one`() {
        assertEquals(1, Attention.nextPointAt(0))
        assertEquals(3, Attention.nextPointAt(1))
        assertEquals(6, Attention.nextPointAt(4))
        assertNull(Attention.nextPointAt(27))
    }

    @Test
    fun `each point adds a quarter`() {
        assertEquals(1.0, Attention.multiplier(0), 0.0)
        assertEquals(1.75, Attention.multiplier(3), 0.0)
    }

    @Test
    fun `an allocation never uses more points than available`() {
        val wanted = linkedMapOf("b_notes" to 3, Attention.VIGIL to 2, "b_scrolls" to -1)
        assertEquals(mapOf("b_notes" to 3, Attention.VIGIL to 1), Attention.clamp(wanted, 4))
        assertEquals(emptyMap<String, Int>(), Attention.clamp(wanted, 0))
    }

    @Test
    fun `allocations survive being stored as text`() {
        val allocation = linkedMapOf("b_notes" to 2, Attention.DREAM to 1)
        assertEquals(allocation, Attention.decode(Attention.encode(allocation)))
        assertEquals(emptyMap<String, Int>(), Attention.decode("basura,:,x:y"))
        assertEquals(emptyMap<String, Int>(), Attention.decode(null))
    }
}
