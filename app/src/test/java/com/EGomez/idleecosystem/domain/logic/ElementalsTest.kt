package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ElementalsTest {

    @Test
    fun `the four elementals open at the third awakening`() {
        assertFalse(Elementals.isUnlocked(2))
        assertTrue(Elementals.isUnlocked(3))
        assertEquals(4, Elemental.entries.size)
        Elemental.entries.forEach { assertEquals(4, it.nodes.size) }
        assertEquals(ElementalNode.entries.size, ElementalNode.entries.map { it.id }.toSet().size)
        assertEquals("cthugha.ember", ElementalNode.EMBER.id)
        assertEquals(ElementalNode.STONE, ElementalNode.fromId("nyarlathotep.stone"))
        assertNull(Elemental.fromId("hastur"))
    }

    @Test
    fun `every tree starts from its first node and stays in its own tree`() {
        Elemental.entries.forEach { e ->
            assertNull(e.nodes.first().required)
            e.nodes.drop(1).forEach { node ->
                assertEquals(e, node.required!!.elemental)
            }
        }
    }

    @Test
    fun `favor grows with the digits of production`() {
        assertEquals(1.0 / 300.0, Elementals.favorPerSecond(BigDecimal.ZERO), 1e-12)
        // 1e99 por segundo: (1 + 99) / 300
        assertEquals(100.0 / 300.0, Elementals.favorPerSecond(BigDecimal("1e99")), 1e-6)
        assertEquals(100.0 / 300.0 * 2.25, Elementals.favorPerSecond(BigDecimal("1e99"), north = 2), 1e-6)
        // Más allá del Double también funciona
        assertEquals(1001.0 / 300.0, Elementals.favorPerSecond(BigDecimal("1e1000")), 1e-3)
    }

    @Test
    fun `nodes cost favor and need their parent`() {
        val none = emptyMap<String, Int>()
        assertTrue(Elementals.canBuy(ElementalNode.EMBER, none, 2.0))
        assertFalse(Elementals.canBuy(ElementalNode.EMBER, none, 1.9))
        assertFalse(Elementals.canBuy(ElementalNode.PYRE, none, 999.0))
        assertTrue(Elementals.canBuy(ElementalNode.PYRE, mapOf("cthugha.ember" to 1), 3.0))
        // El nodo de otro árbol no abre este
        assertFalse(Elementals.canBuy(ElementalNode.DRIFT, mapOf("cthugha.ember" to 1), 999.0))
        assertFalse(Elementals.canBuy(ElementalNode.EMBER, mapOf("cthugha.ember" to 10), 1e9))
        assertEquals(1024, ElementalNode.EMBER.cost(9))
        assertEquals(60, ElementalNode.STARFIRE.cost(1))
    }

    @Test
    fun `only the summoned elemental's tree takes effect`() {
        val levels = mapOf("cthugha.ember" to 3, "ithaqua.gale" to 2)
        assertEquals(3, Elementals.effective(ElementalNode.EMBER, levels, Elemental.CTHUGHA))
        assertEquals(0, Elementals.effective(ElementalNode.GALE, levels, Elemental.CTHUGHA))
        assertEquals(0, Elementals.effective(ElementalNode.EMBER, levels, null))
    }

    @Test
    fun `effects`() {
        assertEquals(125.0, Elementals.productionMultiplier(3), 1e-9)
        assertEquals(100.0, Elementals.clickMultiplier(2), 1e-9)
        assertEquals(1.06, Elementals.productionExponent(3), 1e-12)
        assertEquals(9.0, Elementals.offlineMultiplier(2), 1e-9)
        // Piedra Negra 2 y Faraón 1 con 4 Despertares: 4 × 3
        assertEquals(12.0, Elementals.etherMultiplier(2, 1, 4), 1e-9)
        assertEquals(1.5, Elementals.goldMultiplier(2), 1e-12)
        assertEquals(1.6, Elementals.visionMultiplier(3), 1e-12)
    }
}
