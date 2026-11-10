package com.EGomez.idleecosystem.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.Ritual
import com.EGomez.idleecosystem.domain.model.RitualEffectType
import com.EGomez.idleecosystem.ui.components.BuildingItem
import com.EGomez.idleecosystem.ui.screens.main.components.RitualItem
import com.EGomez.idleecosystem.ui.screens.main.dialogs.AscendDialog
import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import com.EGomez.idleecosystem.ui.screens.main.dialogs.PATH_NOTE_TAG
import com.EGomez.idleecosystem.ui.screens.main.dialogs.ABANDON_PATH_TAG
import androidx.compose.ui.test.assertTextContains
import com.EGomez.idleecosystem.ui.screens.main.dialogs.AscendPacing
import com.EGomez.idleecosystem.ui.screens.main.dialogs.AchievementsDialog
import com.EGomez.idleecosystem.ui.screens.main.dialogs.ACHIEVEMENT_CATEGORY_TAG
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.AchievementType
import com.EGomez.idleecosystem.ui.screens.main.dialogs.AttentionDialog
import com.EGomez.idleecosystem.ui.screens.main.dialogs.ATTENTION_ADD_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.ATTENTION_BAR_TAG
import com.EGomez.idleecosystem.ui.screens.main.dialogs.PactDetailDialog
import com.EGomez.idleecosystem.ui.screens.main.dialogs.PACT_CHOICE_TAG
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.logic.BuildingCatalog
import com.EGomez.idleecosystem.ui.screens.main.dialogs.AutomationDialog
import com.EGomez.idleecosystem.ui.screens.main.dialogs.AUTO_ASCEND_TAG
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import com.EGomez.idleecosystem.ui.screens.main.components.InfoSection
import com.EGomez.idleecosystem.ui.screens.main.components.MenuSection
import com.EGomez.idleecosystem.ui.screens.main.components.LOCKED_ASCENSION_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.AUTO_BUILDINGS_TAG
import com.EGomez.idleecosystem.domain.logic.SystemReveal
import com.EGomez.idleecosystem.domain.logic.RuneBuff
import com.EGomez.idleecosystem.ui.screens.ActiveBuffsDisplay
import com.EGomez.idleecosystem.ui.screens.main.components.RESEARCH_BUTTON_TAG
import androidx.compose.runtime.Composable
import org.junit.Assert.assertTrue
import com.EGomez.idleecosystem.ui.screens.main.dialogs.NEXT_GOAL_TAG
import com.EGomez.idleecosystem.domain.logic.BuildingMilestones
import com.EGomez.idleecosystem.domain.logic.NextGoal
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import com.EGomez.idleecosystem.ui.screens.main.dialogs.PrestigeShopDialog
import com.EGomez.idleecosystem.ui.screens.main.dialogs.CONSTELLATION_MAP_TAG
import androidx.compose.ui.test.onNodeWithContentDescription
import com.EGomez.idleecosystem.core.math.NumberNotation
import com.EGomez.idleecosystem.ui.screens.main.dialogs.SettingsDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal

/**
 * Los flujos de interfaz donde un error le cuesta progreso al jugador: comprar con
 * MAX, ascender e invocar rituales. Corren sobre Robolectric, sin emulador.
 */
@RunWith(RobolectricTestRunner::class)
// Textos en español (el idioma por defecto del juego); EnglishUiTest cubre el inglés
@Config(sdk = [34], application = Application::class, qualifiers = "es")
class CriticalUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val notes = Building(
        id = "b_notes",
        name = "Notas Desordenadas",
        description = "Garabatos frenéticos.",
        baseCost = BigDecimal("15"),
        baseProduction = BigDecimal("0.5"),
        growthFactor = BigDecimal("1.15"),
        isVisible = true
    )

    @Test
    fun `MAX shows how many you can afford and buys`() {
        var bought = 0
        compose.setContent {
            // 15 + 17,25 + 19,84 + 22,81 = 74,9 -> alcanzan 4 (el quinto cuesta 26,23)
            BuildingItem(notes, BigDecimal(100), BigDecimal.ONE, BuildingCalculator(), buyMultiplier = -1) { bought++ }
        }

        compose.onNodeWithText("Comprar +4", substring = true).assertIsEnabled().performClick()
        assertEquals(1, bought)
    }

    @Test
    fun `a building you cannot afford cannot be bought`() {
        var bought = 0
        compose.setContent {
            BuildingItem(notes, BigDecimal(10), BigDecimal.ONE, BuildingCalculator(), buyMultiplier = 1) { bought++ }
        }

        compose.onNodeWithText("Comprar +1", substring = true).assertIsNotEnabled().performClick()
        assertEquals(0, bought)
    }

    @Test
    fun `the ascend dialog shows the reward and only ascends on confirm`() {
        var ascended = 0
        var dismissed = 0
        compose.setContent {
            AscendDialog(sanityGainFormatted = "3", onConfirm = { ascended++ }, onDismiss = { dismissed++ })
        }

        compose.onNodeWithText("Ganarás +3 Cordura Residual").assertExists()
        compose.onNodeWithText("CANCELAR").performClick()
        assertEquals(0, ascended)
        assertEquals(1, dismissed)

        compose.onNodeWithText("ASCENDER").performClick()
        assertEquals(1, ascended)
    }

    @Test
    fun `rituals ask for echoes, not knowledge`() {
        val offering = Ritual(
            id = "rit_knowledge_offering",
            name = "Ofrenda de Conocimiento",
            description = "Prueba",
            baseCost = BigDecimal("25"),
            durationSeconds = 1,
            cooldownSeconds = 600,
            effectType = RitualEffectType.INSTANT_KNOWLEDGE,
            effectValue = 120.0
        )
        var funds by mutableStateOf(BigDecimal(10))
        var invoked = 0
        compose.setContent { RitualItem(offering, funds) { invoked++ } }

        compose.onNodeWithText("FALTAN ECOS (25)").assertIsNotEnabled()

        funds = BigDecimal(25)
        compose.onNodeWithText("INVOCAR (25 Ecos)").assertIsEnabled().performClick()
        assertEquals(1, invoked)
    }

    @Test
    fun `the shop panel shows the new level right after buying`() {
        // Antes el panel guardaba la copia del nodo tocado y seguía en "nivel 0"
        // hasta cerrar y volver a abrir la tienda
        val node = PrestigeUpgrade(
            id = "pu_test",
            name = "Nodo de Prueba",
            description = "Prueba",
            baseCost = BigDecimal.ONE,
            costMultiplier = BigDecimal("2"),
            effectMultiplier = BigDecimal("0.1"),
            type = UpgradeType.GLOBAL_MULTIPLIER,
            x = 0.5f,
            y = 0.5f
        )
        var upgrades by mutableStateOf(listOf(node))
        compose.setContent {
            PrestigeShopDialog(
                upgrades = upgrades,
                currencyFormatted = "10",
                currency = BigDecimal(10),
                onBuy = { id -> upgrades = upgrades.map { if (it.id == id) it.copy(level = it.level + 1) else it } },
                onDismiss = {}
            )
        }

        compose.onNodeWithTag(CONSTELLATION_MAP_TAG).performTouchInput { click(center) }
        compose.onNodeWithText("MEJORAR").performClick()
        compose.onNodeWithText("Nivel 1").assertExists()
        compose.onNodeWithText("Coste: 2 Cordura").assertExists()

        compose.onNodeWithText("MEJORAR").performClick()
        compose.onNodeWithText("Nivel 2").assertExists()
    }

    @Test
    fun `the shop button is disabled and says how much is missing`() {
        val node = PrestigeUpgrade(
            id = "pu_test", name = "Nodo de Prueba", description = "Prueba",
            baseCost = BigDecimal(5), costMultiplier = BigDecimal(2), effectMultiplier = BigDecimal("0.1"),
            type = UpgradeType.GLOBAL_MULTIPLIER, x = 0.5f, y = 0.5f
        )
        var bought = 0
        compose.setContent {
            PrestigeShopDialog(listOf(node), "2", BigDecimal(2), { bought++ }, {})
        }

        compose.onNodeWithTag(CONSTELLATION_MAP_TAG).performTouchInput { click(center) }
        compose.onNodeWithText("MEJORAR", substring = true).assertIsNotEnabled().performClick()
        compose.onNodeWithText("Faltan 3 Cordura", substring = true).assertExists()
        assertEquals(0, bought)
    }

    @Test
    fun `a sealed path cannot be bought and says why`() {
        val hand = PrestigeUpgrade(
            id = MadnessPaths.HAND, name = "Senda de la Mano", description = "Prueba",
            baseCost = BigDecimal(5), costMultiplier = BigDecimal(2), effectMultiplier = BigDecimal.ONE,
            type = UpgradeType.CLICK_MULTIPLIER, x = 0.02f, y = 0.02f, level = 1
        )
        val gears = hand.copy(id = MadnessPaths.GEARS, name = "Senda del Engranaje", x = 0.5f, y = 0.5f, level = 0)
        var bought = 0
        compose.setContent {
            // La Senda tocada va primero: el mapa elige el primer nodo dentro del radio de toque.
            // (La tarjeta del nodo es clicable y fusiona sus textos: la nota se busca sin fusionar.)
            PrestigeShopDialog(listOf(gears, hand), "100", BigDecimal(100), { bought++ }, {})
        }

        compose.onNodeWithTag(CONSTELLATION_MAP_TAG).performTouchInput { click(center) }
        compose.onNodeWithTag(PATH_NOTE_TAG, useUnmergedTree = true).assertTextContains("Senda de la Mano", substring = true)
        compose.onNodeWithText("Sellada").assertIsNotEnabled()
        assertEquals(0, bought)
    }

    @Test
    fun `the ascend dialog offers to abandon the current path`() {
        val hand = PrestigeUpgrade(
            id = MadnessPaths.HAND, name = "Senda de la Mano", description = "Prueba",
            baseCost = BigDecimal(2_000), costMultiplier = BigDecimal("2.5"), effectMultiplier = BigDecimal(3),
            type = UpgradeType.CLICK_MULTIPLIER, level = 2
        )
        var abandon by mutableStateOf(false)
        compose.setContent {
            AscendDialog(
                sanityGainFormatted = "3", onConfirm = {}, onDismiss = {},
                activePath = hand, abandonPath = abandon, onAbandonPathChanged = { abandon = it }
            )
        }

        compose.onNodeWithText("recuperas", substring = true).assertExists()
        compose.onNodeWithTag(ABANDON_PATH_TAG).performClick()
        assertTrue(abandon)
    }

    @Test
    fun `settings open a section, change the notation and go back`() {
        var chosen: NumberNotation? = null
        compose.setContent {
            SettingsDialog(
                uiState = GameUiState(),
                onDismiss = {}, onStatsClick = {}, onToggleMusic = {}, onToggleSfx = {},
                onToggleParticles = {}, onToggleFloatingText = {},
                onNotationSelected = { chosen = it },
                onLanguageSelected = {},
                onExportClick = {}, onImportClick = {}, onCancelImport = {}, onPasteClick = {},
                onHardResetClick = {}, onToggleTutorial = {}
            )
        }

        // La lista de secciones; el reinicio total ya no está a la vista
        compose.onNodeWithText("Zona peligrosa").assertExists()
        compose.onNodeWithText("REINICIO TOTAL").assertDoesNotExist()

        compose.onNodeWithText("Pantalla").performClick()
        compose.onNodeWithText("1.23 M").assertExists() // ejemplo de la notación con nombres
        compose.onNodeWithText("Con nombres").performClick()
        assertEquals(NumberNotation.SUFFIX, chosen)

        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Zona peligrosa").assertExists()
    }

    @Test
    fun `the milestone option buys exactly what is missing`() {
        var bought = 0
        compose.setContent {
            BuildingItem(
                notes.copy(amountOwned = 19), BigDecimal(100_000), BigDecimal.ONE, BuildingCalculator(),
                buyMultiplier = BuildingMilestones.BUY_TO_MILESTONE,
                milestones = listOf(10, 25, 50)
            ) { bought++ }
        }

        // 19 Notas: el hito siguiente es 25
        compose.onNodeWithText("Hito 25 · faltan 6").assertExists()
        compose.onNodeWithText("Comprar +6", substring = true).assertIsEnabled().performClick()
        assertEquals(1, bought)
    }

    @Test
    fun `achievements can be filtered by category`() {
        fun ach(id: String, name: String, group: String) = Achievement(
            id, name, "d", AchievementType.STAT, null, BigDecimal.ONE, BigDecimal("1.01"),
            isUnlocked = true, groupId = group, order = id.length, requirement = "r", stat = "ascensions"
        )
        compose.setContent {
            AchievementsDialog(
                achievements = listOf(ach("a1", "Primer Umbral", "6_asc"), ach("a22", "El Escéptico", "7_pact_none_1")),
                achievementsProgress = "2 / 2", secretsProgress = null, onDismiss = {}
            )
        }

        compose.onNodeWithText("Primer Umbral").assertExists()
        compose.onNodeWithTag(ACHIEVEMENT_CATEGORY_TAG + "PANTHEON").performClick()
        compose.onNodeWithText("El Escéptico").assertExists()
        compose.onNodeWithText("Primer Umbral").assertDoesNotExist()
        compose.onNodeWithTag(ACHIEVEMENT_CATEGORY_TAG + "all").performClick()
        compose.onNodeWithText("Primer Umbral").assertExists()
    }

    @Test
    fun `the next goal is shown at the top of the achievements`() {
        compose.setContent {
            AchievementsDialog(
                achievements = emptyList(), achievementsProgress = "0 / 0", secretsProgress = null,
                nextGoal = NextGoal("b_not_2", "Notas II", "50 Notas", 0.68f),
                onDismiss = {}
            )
        }

        compose.onNodeWithTag(NEXT_GOAL_TAG).assertExists()
        compose.onNodeWithText("Próxima meta: 50 Notas (68%)").assertExists()
    }

    @Test
    fun `the ascend dialog says when the reward doubles`() {
        compose.setContent {
            AscendDialog(
                sanityGainFormatted = "3", onConfirm = {}, onDismiss = {},
                pacing = AscendPacing("9.50e+06", "4", "1.60e+07", "6", "3.60e+07")
            )
        }

        compose.onNodeWithText("Generado en esta vida: 9.50e+06").assertExists()
        compose.onNodeWithText("Siguiente punto: +4 al llegar a 1.60e+07").assertExists()
        compose.onNodeWithText("Para duplicar: +6 al llegar a 3.60e+07").assertExists()
    }

    @Composable
    private fun ResearchButton(onClick: () -> Unit) {
        InfoSection(
            knowledgeFormatted = "100", knowledgePerSecond = "1", globalMultiplier = BigDecimal.ONE,
            activeBuffMultiplier = 1.0, buffTimeRemaining = 0, clickMultiplier = "1",
            onManualClick = { _, _, _ -> onClick() }
        )
    }

    @Test
    fun `a quick tap on research is a single click`() {
        var clicks = 0
        compose.setContent { ResearchButton { clicks++ } }

        compose.onNodeWithTag(RESEARCH_BUTTON_TAG).performClick()
        compose.waitForIdle()
        assertEquals(1, clicks)
    }

    @Test
    fun `holding research keeps clicking until released`() {
        var clicks = 0
        compose.setContent { ResearchButton { clicks++ } }

        compose.onNodeWithTag(RESEARCH_BUTTON_TAG).performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(1_000)
        compose.onNodeWithTag(RESEARCH_BUTTON_TAG).performTouchInput { up() }
        compose.waitForIdle()
        val afterRelease = clicks

        // 1 al apoyar el dedo y luego uno cada 125 ms desde los 300 ms (unos 6 en total)
        assertTrue("clics: $afterRelease", afterRelease >= 4)
        // Al soltar se detiene
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        assertEquals(afterRelease, clicks)
    }

    @Composable
    private fun Menu(
        selectedTab: Int,
        revealed: SystemReveal,
        buildings: List<Building> = emptyList(),
        upgrades: List<com.EGomez.idleecosystem.domain.model.Upgrade> = emptyList(),
        ascensions: Int = 0
    ) {
        MenuSection(
            isMenuExpanded = true, onToggleMenu = {}, menuWeight = 0.65f,
            selectedTabIndex = selectedTab, onTabSelected = {},
            buyMultiplier = 1, onBuyMultiplierChanged = {}, showPurchased = false, onToggleShowPurchased = {},
            buildings = buildings, upgrades = upgrades, rituals = emptyList(),
            currentKnowledge = BigDecimal.ZERO, currentEchoes = BigDecimal.ZERO, globalMultiplier = BigDecimal.ONE,
            buildingCalculator = BuildingCalculator(), buildingCostMultiplier = 1.0,
            onBuyBuilding = {}, onBuyUpgrade = {}, onActivateRitual = {},
            prestigeCurrencyFormatted = "0", canPrestige = false, potentialPrestigeFormatted = "0",
            revealed = revealed, ascensions = ascensions, onAscendClicked = {}, onShowPrestigeShop = {}
        )
    }

    @Test
    fun `undiscovered systems show their requirement`() {
        compose.setContent { Menu(selectedTab = 1, revealed = SystemReveal(false, false, false)) }

        compose.onNodeWithText("Grimorio").assertDoesNotExist()
        compose.onNodeWithText("Reúne 250 de Conocimiento para descubrir el Grimorio.", substring = true).assertExists()
        compose.onNodeWithTag(LOCKED_ASCENSION_TAG).assertExists()
        compose.onNodeWithText("ASCENDER").assertDoesNotExist()
    }

    @Test
    fun `discovered systems look as before`() {
        compose.setContent { Menu(selectedTab = 1, revealed = SystemReveal()) }

        compose.onNodeWithTag(LOCKED_ASCENSION_TAG).assertDoesNotExist()
        compose.onNodeWithText("ASCENDER").assertExists()
    }

    @Test
    fun `the rune countdown goes down in real time`() {
        var now = 0L
        val buff = RuneBuff(id = "t1_test", multiplier = 2.0, expirationTime = 20_000L, description = "")
        compose.setContent { ActiveBuffsDisplay(listOf(buff), clock = { now }) }

        compose.onNodeWithText("20s", substring = true).assertExists()

        // La lista no cambia: solo pasa el tiempo
        now = 5_000L
        compose.mainClock.advanceTimeBy(500)
        compose.onNodeWithText("15s", substring = true).assertExists()
    }

    @Test
    fun `a synergy shows its per unit effect instead of x1`() {
        val catalog = com.EGomez.idleecosystem.domain.logic.BuildingCatalog.create()
        val synergy = com.EGomez.idleecosystem.domain.logic.BuildingSynergies.create(catalog)
            .first { it.sourceBuildingId == "b_notes" }
        compose.setContent { Menu(selectedTab = 1, revealed = SystemReveal(), buildings = catalog, upgrades = listOf(synergy)) }

        compose.onNodeWithText("Efecto: +1% por unidad de Notas Desordenadas").assertExists()
        compose.onNodeWithText("Efecto: x1").assertDoesNotExist()
    }

    @Test
    fun `the auto chip appears with the building autobuyer milestone`() {
        var ascensions by mutableStateOf(4)
        compose.setContent { Menu(selectedTab = 0, revealed = SystemReveal(), ascensions = ascensions) }

        compose.onNodeWithTag(AUTO_BUILDINGS_TAG).assertDoesNotExist()
        ascensions = 5
        compose.onNodeWithTag(AUTO_BUILDINGS_TAG).assertExists()
    }

    @Test
    fun `the ascend dialog lists the milestones and marks the next one`() {
        compose.setContent { AscendDialog(sanityGainFormatted = "3", onConfirm = {}, onDismiss = {}, ascensions = 2) }

        compose.onNodeWithText("Esta será tu Ascensión n.º 3.").assertExists()
        compose.onNodeWithText("✓ 1: Empiezas cada vida con 10 Sinapsis.").assertExists()
        compose.onNodeWithText("★ 3: Conservas las mejoras de clic del Grimorio.").assertExists()
        compose.onNodeWithText("· 5: Autocomprador de edificios.").assertExists()
    }

    @Test
    fun `the madness map shows the net effect of spending sanity`() {
        val node = PrestigeUpgrade(
            id = "pu_test", name = "Nodo de Prueba", description = "Prueba",
            baseCost = BigDecimal.ONE, costMultiplier = BigDecimal("2"), effectMultiplier = BigDecimal("0.1"),
            type = UpgradeType.GLOBAL_MULTIPLIER, x = 0.5f, y = 0.5f
        )
        compose.setContent {
            PrestigeShopDialog(upgrades = listOf(node), currencyFormatted = "20", currency = BigDecimal(20), onBuy = {}, onDismiss = {})
        }

        compose.onNodeWithTag(CONSTELLATION_MAP_TAG).performTouchInput { click(center) }
        // Nodo x1,00 -> x1,10; Cordura 20 -> 19: bono x3,00 -> x2,90; neto 1,1 x 2,9 / 3 = 1,063
        compose.onNodeWithText("Al mejorar: x1.00 → x1.10").assertExists()
        compose.onNodeWithText("Bono de Cordura: x3.00 → x2.90").assertExists()
        compose.onNodeWithText("Neto en producción: x1.06").assertExists()
    }

    @Composable
    private fun Automation(ascensions: Int, onBudget: (Int) -> Unit = {}) {
        AutomationDialog(
            settings = com.EGomez.idleecosystem.domain.logic.AutomationSettings(),
            ascensions = ascensions,
            buildings = listOf(notes),
            rituals = emptyList(),
            onBudgetSelected = onBudget,
            onToggleBuildings = {}, onToggleBuildingExcluded = {}, onToggleGrimoire = {},
            onToggleAutoAscend = {}, onRitualSelected = {}, onDismiss = {}
        )
    }

    @Test
    fun `the automation panel opens each section with its milestone`() {
        compose.setContent { Automation(ascensions = 5) }

        compose.onNodeWithText("Notas Desordenadas").assertExists()
        // La Ascensión automática llega con 10 Ascensiones
        compose.onNodeWithTag(AUTO_ASCEND_TAG).assertDoesNotExist()
        compose.onAllNodesWithText("Se desbloquea con 10 Ascensiones.").assertCountEquals(2)
    }

    @Test
    fun `the automation budget can be changed`() {
        var chosen = 0
        compose.setContent { Automation(ascensions = 10, onBudget = { chosen = it }) }

        compose.onNodeWithTag(AUTO_ASCEND_TAG).assertExists()
        compose.onNodeWithText("25%").performClick()
        assertEquals(25, chosen)
    }

    @Test
    fun `the pantheon shows unlocked gods and hides the rest`() {
        compose.setContent { Menu(selectedTab = 3, revealed = SystemReveal(), ascensions = 0) }

        // En la primera Ascensión ya se puede elegir a Cthulhu
        compose.onNodeWithText("Cthulhu").assertExists()
        compose.onNodeWithText("Azathoth").assertDoesNotExist()
        compose.onNodeWithText("Se revela con 50 Ascensiones").assertExists()
        compose.onNodeWithText("Dioses Exteriores").assertExists()
    }

    @Test
    fun `a god's page tells its myth and its pact`() {
        compose.setContent {
            PactDetailDialog(
                pact = Pact.HASTUR, active = true,
                buildingNames = BuildingCatalog.create().associate { it.id to it.name },
                onDismiss = {}
            )
        }

        compose.onNodeWithText("El Rey de Amarillo").assertExists()
        compose.onNodeWithText("Pacto de esta vida").assertExists()
        compose.onNodeWithText("+ Producción x2").assertExists()
        compose.onNodeWithText("− Cordura ganada al Ascender x0.75").assertExists()
    }

    @Test
    fun `building pacts name the buildings they change`() {
        compose.setContent {
            PactDetailDialog(
                pact = Pact.NUG, active = false,
                buildingNames = BuildingCatalog.create().associate { it.id to it.name },
                onDismiss = {}
            )
        }

        compose.onNodeWithText("+ Sectario Novato, Sacerdote Demente, Santuario Subterráneo, Altar de Hueso: x4").assertExists()
        compose.onNodeWithText("− Clic x0.5").assertExists()
    }

    @Test
    fun `the ascend dialog lets the player choose the next pact`() {
        var chosen: Pact? = null
        compose.setContent {
            AscendDialog(
                sanityGainFormatted = "3", onConfirm = {}, onDismiss = {},
                pacts = Pact.available(1), selectedPact = Pact.NONE, onPactSelected = { chosen = it }
            )
        }

        compose.onNodeWithText("Pacto para la próxima vida").assertExists()
        compose.onNodeWithText("+ Cordura ganada al Ascender x1.25").assertExists()
        compose.onNodeWithTag("${PACT_CHOICE_TAG}cthulhu").performClick()
        assertEquals(Pact.CTHULHU, chosen)
    }

    @Test
    fun `attention can be moved freely within the available points`() {
        val added = mutableListOf<String>()
        compose.setContent {
            AttentionDialog(
                allocation = mapOf("b_notes" to 1), points = 2, ascensions = 3, buildings = listOf(notes),
                onAdd = { added += it }, onRemove = {}, onClear = {}, onDismiss = {}
            )
        }

        compose.onNodeWithText("Puntos usados: 1 de 2").assertExists()
        compose.onNodeWithText("Producción · +25%").assertExists()
        compose.onNodeWithTag("${ATTENTION_ADD_TAG}vigil").performClick()
        assertEquals(listOf("vigil"), added)
    }

    @Test
    fun `the attention bar appears after the first ascension and opens the dialog`() {
        var points by mutableStateOf(0)
        var tab by mutableStateOf(0)
        var opened = 0
        compose.setContent {
            MenuSection(
                isMenuExpanded = true, onToggleMenu = {}, menuWeight = 0.65f,
                selectedTabIndex = tab, onTabSelected = {},
                buyMultiplier = 1, onBuyMultiplierChanged = {}, showPurchased = false, onToggleShowPurchased = {},
                buildings = emptyList(), upgrades = emptyList(), rituals = emptyList(),
                currentKnowledge = BigDecimal.ZERO, currentEchoes = BigDecimal.ZERO, globalMultiplier = BigDecimal.ONE,
                buildingCalculator = BuildingCalculator(), buildingCostMultiplier = 1.0,
                onBuyBuilding = {}, onBuyUpgrade = {}, onActivateRitual = {},
                prestigeCurrencyFormatted = "0", canPrestige = false, potentialPrestigeFormatted = "0",
                attentionPoints = points, onOpenAttention = { opened++ },
                attention = mapOf("notes" to 1), onAscendClicked = {}, onShowPrestigeShop = {}
            )
        }

        compose.onNodeWithTag(ATTENTION_BAR_TAG).assertDoesNotExist()
        points = 1
        compose.onNodeWithTag(ATTENTION_BAR_TAG).assertExists().performClick()
        assertEquals(1, opened)
        // Solo vive en la pestaña Investigación
        tab = 2
        compose.onNodeWithTag(ATTENTION_BAR_TAG).assertDoesNotExist()
    }
}

