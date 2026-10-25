package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.GameStatisticsEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Base64

class SaveManagerTest {

    private val jvmCodec = object : Base64Codec {
        override fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
        override fun decode(text: String): ByteArray = Base64.getDecoder().decode(text.trim())
    }
    private val saveManager = SaveManager(jvmCodec)

    private val stats = GameStatisticsEntity(
        totalPlayTimeSeconds = 10, startTime = 1, runTimeSeconds = 5, totalOfflineSeconds = 2,
        lifetimeKnowledge = "100", runKnowledge = "50", maxKnowledgePerSecond = "3",
        totalClicks = 7, manualKnowledgeGenerated = "7", totalPrestiges = 1,
        lifetimeSanity = "4", spentSanity = "1", totalEventsFound = 2, totalEchoes = "3"
    )

    @Test
    fun `exportar e importar conserva los datos`() {
        val buildings = BuildingManager(BuildingCalculator()).availableBuildings.onEach { it.amountOwned = 3 }
        val upgrades = UpgradeManager().availableUpgrades.onEach { it.isBought = it.id == "u_click_1" }
        val shop = PrestigeShopManager().upgrades.onEach { if (it.id == "pu_core") it.level = 2 }

        val code = saveManager.exportSave(
            BigDecimal("12345.6"), BigDecimal(9), BigDecimal(4),
            buildings, upgrades, emptyList(), shop, stats
        )
        val data = saveManager.importSave(code)

        assertNotNull(data)
        data!!
        assertEquals(0, BigDecimal("12345.6").compareTo(data.knowledge))
        assertEquals(0, BigDecimal(4).compareTo(data.echoes))
        assertEquals(listOf("u_click_1"), data.upgrades)
        assertEquals(2, data.prestigeUpgrades.single { it.id == "pu_core" }.level)
        assertTrue(data.buildings.all { it.amount == 3 })
        assertEquals("3", data.statistics.totalEchoes)
    }

    @Test
    fun `un codigo antiguo sin ecos ni estadisticas no deja nulls`() {
        // Antes: Gson dejaba echoes = null y la app crasheaba al usarlo
        val legacyJson = """{"knowledge": 500, "sanity": 2, "buildings": [{"id": "b_notes", "amount": 4}], "upgrades": ["u_notes_1"], "achievements": [], "prestigeUpgrades": [], "timestamp": 1}"""
        val data = saveManager.parseJson(legacyJson)

        assertNotNull(data)
        data!!
        assertEquals(BigDecimal.ZERO, data.echoes)
        assertEquals("0", data.statistics.totalEchoes)
        assertEquals("0", data.statistics.runKnowledge)
        assertEquals(false, data.buildings.single().isVisible)
    }

    @Test
    fun `valores negativos o invalidos se normalizan`() {
        val json = """{"knowledge": -50, "sanity": -1, "echoes": -3, "buildings": [{"id": "b_notes", "amount": -4}, {"amount": 2}], "prestigeUpgrades": [{"id": "pu_core", "level": -2}], "statistics": {"lifetimeKnowledge": "abc"}}"""
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
    fun `codigos corruptos devuelven null`() {
        assertNull(saveManager.importSave("esto no es base64 !!!"))
        assertNull(saveManager.parseJson("{}"))
        assertNull(saveManager.parseJson("no es json"))
    }
}
