package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class SaveManagerTest {

    private val saveManager = SaveManager()

    @Test
    fun `legacy code without echoes or statistics has no nulls`() {
        // Before: Gson left echoes = null inside a non-null type and the app crashed later
        val legacyJson = """{"knowledge": 500, "sanity": 2, "buildings": [{"id": "b_notes", "amount": 4}],
            "upgrades": ["u_notes_1"], "achievements": [], "prestigeUpgrades": [], "timestamp": 1}"""
        val data = saveManager.parseJson(legacyJson)

        assertNotNull(data)
        data!!
        assertEquals(BigDecimal.ZERO, data.echoes)
        assertEquals("0", data.statistics.totalEchoes)
        assertEquals("0", data.statistics.runKnowledge)
        assertEquals(0, data.statistics.runesSevered)
        assertEquals(false, data.buildings.single().isVisible)
        assertEquals(listOf("u_notes_1"), data.upgrades)
    }

    @Test
    fun `negative or invalid values are normalized`() {
        val json = """{"knowledge": -50, "sanity": -1, "echoes": -3,
            "buildings": [{"id": "b_notes", "amount": -4}, {"amount": 2}],
            "prestigeUpgrades": [{"id": "pu_core", "level": -2}],
            "statistics": {"lifetimeKnowledge": "abc"}}"""
        val data = saveManager.parseJson(json)!!

        assertEquals(BigDecimal.ZERO, data.knowledge)
        assertEquals(BigDecimal.ZERO, data.sanity)
        assertEquals(BigDecimal.ZERO, data.echoes)
        assertEquals(1, data.buildings.size)
        assertEquals(0, data.buildings.single().amount)
        assertEquals(0, data.prestigeUpgrades.single().level)
        assertEquals("0", data.statistics.lifetimeKnowledge)
    }

    @Test
    fun `duplicated ids are removed`() {
        val json = """{"knowledge": 1, "upgrades": ["a", "a", null], "achievements": ["x", "x"]}"""
        val data = saveManager.parseJson(json)!!
        assertEquals(listOf("a"), data.upgrades)
        assertEquals(listOf("x"), data.achievements)
    }

    @Test
    fun `corrupt content returns null`() {
        assertNull(saveManager.parseJson("{}"))
        assertNull(saveManager.parseJson("not json"))
    }
}
