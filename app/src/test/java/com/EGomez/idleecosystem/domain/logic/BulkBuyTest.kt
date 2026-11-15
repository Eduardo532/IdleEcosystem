package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BulkBuyTest {

    /** 1, 2, 4, 8 … */
    private val doubling: (Int) -> BigDecimal = { BigDecimal(2).pow(it) }

    @Test
    fun `a fixed lot is bought whole or not at all`() {
        val ten = BulkBuy.plan(0, null, BigDecimal(2000), 10, doubling)
        assertEquals(10, ten.levels)
        assertEquals(0, BigDecimal(1023).compareTo(ten.cost))
        // Sin presupuesto para el lote entero, no compra y dice cuánto costaría
        val short = BulkBuy.plan(0, null, BigDecimal(1000), 10, doubling)
        assertTrue(short.isEmpty)
        assertEquals(0, BigDecimal(1023).compareTo(short.cost))
    }

    @Test
    fun `max buys every level the budget allows`() {
        val max = BulkBuy.plan(3, null, BigDecimal(100), BulkBuy.MAX, doubling)
        // 8 + 16 + 32 = 56; con 64 más ya no alcanza
        assertEquals(3, max.levels)
        assertEquals(0, BigDecimal(56).compareTo(max.cost))
        val none = BulkBuy.plan(3, null, BigDecimal(7), BulkBuy.MAX, doubling)
        assertTrue(none.isEmpty)
        assertEquals(0, BigDecimal(8).compareTo(none.cost))
    }

    @Test
    fun `the level cap trims the lot`() {
        val capped = BulkBuy.plan(8, 10, BigDecimal(1_000_000), 100, doubling)
        assertEquals(2, capped.levels)
        assertEquals(0, BigDecimal(256 + 512).compareTo(capped.cost))
        assertTrue(BulkBuy.plan(10, 10, BigDecimal(1_000_000), BulkBuy.MAX, doubling).isEmpty)
    }

    @Test
    fun `max never runs forever on free levels`() {
        val free = BulkBuy.plan(0, null, BigDecimal.ONE, BulkBuy.MAX) { BigDecimal.ZERO }
        assertEquals(BulkBuy.LIMIT, free.levels)
    }

    @Test
    fun `labels`() {
        assertEquals("x10", BulkBuy.label(10))
        assertEquals("MAX", BulkBuy.label(BulkBuy.MAX))
        assertEquals(listOf(1, 10, 100, BulkBuy.MAX), BulkBuy.AMOUNTS)
    }
}
