package com.EGomez.idleecosystem.ui.components

import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import com.EGomez.idleecosystem.domain.logic.PrestigeShopManager
import com.EGomez.idleecosystem.domain.model.Constellation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyLayoutTest {

    private val upgrades = PrestigeShopManager().upgrades

    @Test
    fun `every constellation has a real figure with one star per node`() {
        assertEquals(Constellation.entries.toSet(), SkyLayout.figures.keys)
        SkyLayout.figures.forEach { (c, figure) ->
            val stars = upgrades.filter { it.constellation == c && !MadnessPaths.isPath(it.id) }
            assertEquals("$c", stars.size, figure.nodes.size)
            assertTrue("$c: nodos y Senda deben ser estrellas de la figura", (figure.nodes + figure.path).all { it in figure.points })
            assertTrue("$c: las líneas unen estrellas de la figura", figure.edges.all { (a, b) -> a in figure.points && b in figure.points })
            assertEquals("$c: sin estrellas repetidas", figure.nodes.size, figure.nodes.toSet().size)
        }
    }

    @Test
    fun `the thread from the awakening reaches the nearest star of each figure`() {
        val positions = SkyLayout.positions(upgrades)
        Constellation.entries.forEach { c ->
            val stars = upgrades.filter { it.constellation == c && !MadnessPaths.isPath(it.id) }
            val nearest = stars.minByOrNull { (positions.getValue(it.id) - SkyLayout.CORE).getDistance() }
            assertEquals("$c", stars.first().id, nearest?.id)
        }
    }

    @Test
    fun `stars do not overlap and paths sit in their figure`() {
        val positions = SkyLayout.positions(upgrades)
        val points = positions.filterKeys { it != "pu_core" }.values.toList()
        points.forEachIndexed { i, a -> points.drop(i + 1).forEach { b -> assertTrue((a - b).getDistance() > 25f) } }
        MadnessPaths.IDS.forEach { id ->
            val path = upgrades.first { it.id == id }
            val center = SkyLayout.center(path.constellation!!)
            assertTrue("$id", (positions.getValue(id) - center).getDistance() < 80f)
        }
        assertEquals("Rigel", SkyLayout.starName(upgrades.first { it.id == "pu_click_1" }, upgrades))
        assertEquals("Algol", SkyLayout.starName(upgrades.first { it.id == PrestigeShopManager.ATTENTION_NODE_ID }, upgrades))
    }
}
