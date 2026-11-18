package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtifactsTest {

    @Test
    fun `seven artifacts with 34 levels in all`() {
        assertEquals(7, Artifact.entries.size)
        assertEquals(7, Artifact.entries.map { it.id }.toSet().size)
        // El logro "Reliquias del Acto Final" pide todos al máximo
        assertEquals(34, Artifact.entries.sumOf { it.maxLevel })
        assertEquals(Artifact.VEIL, Artifact.fromId("veil"))
        assertNull(Artifact.fromId("necronomicon"))
    }

    @Test
    fun `costs grow and the last level is reachable`() {
        assertEquals(listOf(5, 8, 13, 21, 33), (0 until 5).map { Artifact.LAMP.cost(it) })
        assertEquals(15, Artifact.BROOCH.cost(0))
        assertEquals(120, Artifact.BROOCH.cost(3))
        Artifact.entries.forEach { a -> (1 until a.maxLevel).forEach { assertTrue(a.cost(it) > a.cost(it - 1)) } }
    }

    @Test
    fun `buying needs fragments and a free level`() {
        assertTrue(Artifacts.canBuy(Artifact.LAMP, 0, 5))
        assertFalse(Artifacts.canBuy(Artifact.LAMP, 0, 4))
        assertFalse(Artifacts.canBuy(Artifact.LAMP, 5, 9999))
    }

    @Test
    fun `effects`() {
        assertEquals(2.25, Artifacts.favorMultiplier(2), 1e-12)
        assertEquals(8.0, Artifacts.dreamMultiplier(3), 1e-12)
        assertEquals(1.5, Artifacts.mineSpeed(1), 1e-12)
        assertEquals(4.0, Artifacts.stardustMultiplier(2), 1e-12)
        assertEquals(9.0, Artifacts.damageMultiplier(2), 1e-12)
        assertEquals(1.5, Artifacts.visionMultiplier(5), 1e-12)
    }

    @Test
    fun `the brooch adds a quarter of fragments per level`() {
        assertEquals(20, Artifacts.fragments(20, 0))
        assertEquals(25, Artifacts.fragments(20, 1))
        assertEquals(40, Artifacts.fragments(20, 4))
        // 5 × 1,25 = 6,25
        assertEquals(6, Artifacts.fragments(5, 1))
    }
}
