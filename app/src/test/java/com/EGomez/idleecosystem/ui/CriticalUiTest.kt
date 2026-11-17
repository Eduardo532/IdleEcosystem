package com.EGomez.idleecosystem.ui

import android.app.Application
import androidx.compose.ui.test.assertTextEquals
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
import com.EGomez.idleecosystem.ui.screens.main.components.EtherPanel
import com.EGomez.idleecosystem.ui.screens.main.components.TranscendMilestoneList
import com.EGomez.idleecosystem.ui.screens.main.dialogs.MAP_VIEW_TOGGLE_TAG
import com.EGomez.idleecosystem.ui.screens.main.dialogs.MAP_CHIP_TAG
import com.EGomez.idleecosystem.ui.screens.main.dialogs.MAP_NODE_ROW_TAG
import com.EGomez.idleecosystem.ui.screens.main.dialogs.MAP_LIST_TAG
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import com.EGomez.idleecosystem.ui.screens.main.dialogs.ASCEND_ROW_TAG
import com.EGomez.idleecosystem.ui.screens.main.dialogs.ASCEND_DETAILS_TAG
import com.EGomez.idleecosystem.ui.screens.main.dialogs.ASCEND_PICKER_DONE_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.ETERNAL_BUY_TAG
import com.EGomez.idleecosystem.domain.model.Trial
import androidx.compose.ui.test.performScrollTo
import androidx.compose.foundation.verticalScroll
import com.EGomez.idleecosystem.ui.screens.main.components.TrialList
import com.EGomez.idleecosystem.ui.screens.main.components.RuneAltar
import com.EGomez.idleecosystem.ui.screens.main.components.MadnessMeterBar
import com.EGomez.idleecosystem.ui.screens.main.components.TideIndicator
import com.EGomez.idleecosystem.ui.screens.main.components.TideInfoDialog
import com.EGomez.idleecosystem.ui.screens.main.components.TIDE_INDICATOR_TAG
import com.EGomez.idleecosystem.domain.model.TidePhase
import com.EGomez.idleecosystem.ui.screens.main.components.MADNESS_METER_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.RUNE_ENGRAVE_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.TRIAL_CHOICE_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.MUTATION_CHOICE_TAG
import com.EGomez.idleecosystem.domain.model.Mutation
import com.EGomez.idleecosystem.ui.screens.main.components.TRIAL_ABANDON_TAG
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
import com.EGomez.idleecosystem.ui.screens.main.components.NEXT_UNLOCK_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.MENU_LAYER_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.MENU_TAB_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.NEW_BADGE_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.BUY_MULTIPLIER_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.MUTATION_ROW_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.MutationCatalog
import com.EGomez.idleecosystem.ui.screens.BUFF_CHIP_TAG
import com.EGomez.idleecosystem.ui.components.RunePlate
import com.EGomez.idleecosystem.ui.components.RuneCaughtNotice
import com.EGomez.idleecosystem.domain.logic.MenuTab
import com.EGomez.idleecosystem.domain.logic.MenuUnlocks
import com.EGomez.idleecosystem.domain.model.RuneCatalog
import com.EGomez.idleecosystem.ui.screens.main.components.AUTO_BUILDINGS_TAG
import com.EGomez.idleecosystem.domain.logic.SystemReveal
import com.EGomez.idleecosystem.domain.logic.RuneBuff
import com.EGomez.idleecosystem.ui.screens.ActiveBuffsDisplay
import com.EGomez.idleecosystem.ui.screens.main.components.RESEARCH_BUTTON_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.COMPACT_RESEARCH_BUTTON_TAG
import com.EGomez.idleecosystem.ui.screens.main.components.CompactResearchButton
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

        compose.onNodeWithText("Senda de la Mano · continúa").assertExists()
        compose.onNodeWithTag(ASCEND_ROW_TAG + "path").performClick()
        compose.onNodeWithText("recuperas", substring = true).assertExists()
        compose.onNodeWithTag(ABANDON_PATH_TAG).performClick()
        assertTrue(abandon)
        compose.onNodeWithTag(ASCEND_PICKER_DONE_TAG).performClick()
        compose.onNodeWithText("Senda de la Mano · se abandona").assertExists()
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
    fun `the ascend dialog lets you pick a trial and shows its rule`() {
        var chosen by mutableStateOf<Trial?>(null)
        compose.setContent {
            AscendDialog(
                sanityGainFormatted = "3", onConfirm = {}, onDismiss = {},
                trials = listOf(Trial.BLINDNESS, Trial.HUNGER), completedTrials = setOf(Trial.BLINDNESS),
                selectedTrial = chosen, onTrialSelected = { chosen = it }
            )
        }

        compose.onNodeWithText("Sin Prueba").assertExists()
        compose.onNodeWithTag(ASCEND_ROW_TAG + "trial").performClick()
        compose.onNodeWithTag(TRIAL_CHOICE_TAG + "hunger").performClick()
        assertEquals(Trial.HUNGER, chosen)
        compose.onNodeWithText("Los edificios cuestan x10.").assertExists()
        compose.onNodeWithText("✓ Ceguera").assertExists()
        compose.onNodeWithTag(TRIAL_CHOICE_TAG + "none").performClick()
        assertEquals(null, chosen)
    }

    @Test
    fun `the ascend dialog offers mutations and explains the chosen one`() {
        var chosen by mutableStateOf<Mutation?>(null)
        val offer = listOf(Mutation.FEVERISH_NOTES, Mutation.BURNING_SYNAPSE, Mutation.HUNGRY_VOID)
        compose.setContent {
            AscendDialog(
                sanityGainFormatted = "3", onConfirm = {}, onDismiss = {},
                mutationOffer = offer, selectedMutation = chosen, onMutationSelected = { chosen = it }
            )
        }

        compose.onNodeWithTag(ASCEND_ROW_TAG + "mutation").performClick()
        compose.onNodeWithText("Mutación de la próxima vida").assertExists()
        compose.onNodeWithTag(MUTATION_CHOICE_TAG + "burning_synapse").performClick()
        assertEquals(Mutation.BURNING_SYNAPSE, chosen)
        compose.onNodeWithText("El clic x5, pero toda la producción x0,75.").assertExists()
        compose.onNodeWithTag(MUTATION_CHOICE_TAG + "none").performClick()
        assertEquals(null, chosen)
    }

    @Test
    fun `the active trial shows its progress and can be abandoned`() {
        var abandoned = 0
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                TrialList(ascensions = 5, activeTrial = Trial.SILENCE, progress = 0.4f, completed = setOf(Trial.HUNGER), onAbandon = { abandoned++ })
            }
        }

        compose.onNodeWithText("En curso: 40%").assertExists()
        compose.onNodeWithText("✓ Superada").assertExists()
        // Mortalidad pide 20 Ascensiones: todavía no se ve su regla
        compose.onNodeWithText("La Cordura Residual y el Mapa de Locura no dan bono.").assertDoesNotExist()
        compose.onNodeWithTag(TRIAL_ABANDON_TAG).performScrollTo().performClick()
        assertEquals(1, abandoned)
    }

    @Test
    fun `a rune caught enough times can be engraved`() {
        var engraved: String? = null
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                RuneAltar(
                    slots = 1, engraved = emptyList(),
                    catches = mapOf("t1_blood" to 10, "t1_ash" to 3),
                    onEngrave = { engraved = it }, onRemove = {}
                )
            }
        }

        compose.onNodeWithText("Espacios de Runa: 0 de 1").assertExists()
        compose.onNodeWithText("Atrapadas: 3 de 10").assertExists()
        compose.onNodeWithTag(RUNE_ENGRAVE_TAG + "t1_ash").assertDoesNotExist()
        compose.onNodeWithTag(RUNE_ENGRAVE_TAG + "t1_blood").performScrollTo().performClick()
        assertEquals("t1_blood", engraved)
    }

    @Test
    fun `the madness meter shows its bonus and the collapse`() {
        var opened = 0
        var collapse by mutableStateOf(0)
        compose.setContent { MadnessMeterBar(63.0, collapse, null) { opened++ } }

        compose.onNodeWithText("Locura 63%", substring = true).assertExists()
        compose.onNodeWithTag(MADNESS_METER_TAG).performClick()
        assertEquals(1, opened)
        collapse = 42
        compose.onNodeWithText("COLAPSO", substring = true).assertExists()
    }

    @Test
    fun `the tide indicator shows the phase and opens the three phases`() {
        var showInfo by mutableStateOf(false)
        compose.setContent {
            TideIndicator(TidePhase.HIGH, 272) { showInfo = true }
            if (showInfo) TideInfoDialog(TidePhase.HIGH) { showInfo = false }
        }

        compose.onNodeWithText("Marea Alta · 4:32").assertExists()
        compose.onNodeWithText("Luego: Marea Baja").assertExists()
        compose.onNodeWithTag(TIDE_INDICATOR_TAG).performClick()
        compose.onNodeWithText("Marea Muerta").assertExists()
    }

    @Test
    fun `transcending asks for a second tap`() {
        var transcended = 0
        var bought: String? = null
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                EtherPanel(
                    cycleAscensions = 50, etherGain = 14, ether = 3, eternalLevels = mapOf("trials" to 1),
                    onBuyEternal = { bought = it }, onTranscend = { transcended++ }
                )
            }
        }

        compose.onNodeWithText("Trascender (+14 Éter)").performClick()
        assertEquals(0, transcended)
        compose.onNodeWithText("Toca otra vez para Trascender").performClick()
        assertEquals(1, transcended)
        compose.onNodeWithTag(ETERNAL_BUY_TAG + "fire").performScrollTo().performClick()
        assertEquals("fire", bought)
        compose.onNodeWithTag(ETERNAL_BUY_TAG + "trials").assertDoesNotExist()
    }

    @Test
    fun `the transcendence milestones show what was reached and what comes next`() {
        compose.setContent { TranscendMilestoneList(transcendences = 3) }

        compose.onNodeWithText("Hitos de Trascendencia (3/6)").assertExists()
        compose.onNodeWithText("Senda Eterna").assertExists()
        // El siguiente se lee entero; los demás, solo su nombre y cuánto falta
        compose.onNodeWithText("Puedes Trascender sola cuando el Éter que ganarías iguale al que tienes.").assertExists()
        compose.onNodeWithText("Trascendencias: 3/5").assertExists()
        compose.onNodeWithText("El Atajo").assertExists()
        compose.onNodeWithText("Trascender pide 40 Ascensiones en el ciclo en lugar de 50.").assertDoesNotExist()
    }

    @Test
    fun `automatic transcendence appears in the automation panel with its milestone`() {
        var toggled = 0
        compose.setContent {
            com.EGomez.idleecosystem.ui.screens.main.dialogs.AutomationDialog(
                settings = com.EGomez.idleecosystem.domain.logic.AutomationSettings(), ascensions = 50,
                buildings = emptyList(), rituals = emptyList(),
                onBudgetSelected = {}, onToggleBuildings = {}, onToggleBuildingExcluded = {}, onToggleGrimoire = {},
                onToggleAutoAscend = {}, onRitualSelected = {}, onDismiss = {},
                transcendences = 5, onToggleAutoTranscend = { toggled++ }
            )
        }

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.dialogs.AUTO_TRANSCEND_TAG).performScrollTo().performClick()
        assertEquals(1, toggled)
    }

    @Test
    fun `the veil card shows the trials left and breaks with all of them`() {
        var broke = 0
        var completed by mutableStateOf(4)
        compose.setContent { com.EGomez.idleecosystem.ui.screens.main.components.VeilCard(completed, broken = false, onBreak = { broke++ }) }

        compose.onNodeWithText("Pruebas superadas: 4/10").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.VEIL_BREAK_TAG).assertDoesNotExist()

        completed = 10
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.VEIL_BREAK_TAG).performClick()
        assertEquals(1, broke)
    }

    @Test
    fun `the stars tab shows the alignment and buys an affordable upgrade`() {
        val bought = mutableListOf<String>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.StarsPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.StarsUi(stars = 12.4, stardust = 40.0, aligned = true, secondsLeft = 95),
                    onBuy = { bought += it }
                )
            }
        }

        compose.onNodeWithText("✦ 12").assertExists()
        compose.onNodeWithText("✦ Las estrellas están en posición: Polvo x5 · quedan 1:35").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.STAR_BUY_TAG + "light").performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.STAR_BUY_TAG + "ether").performScrollTo().assertIsNotEnabled()
        assertEquals(listOf("light"), bought)
    }

    @Test
    fun `stellar upgrades can be bought ten at a time or to the max`() {
        val bought = mutableListOf<Pair<String, Int>>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.StarsPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.StarsUi(stardust = 1000.0),
                    onBuy = {},
                    onBuyMany = { id, amount -> bought += id to amount }
                )
            }
        }

        // x10 de Luz Estelar no alcanza con 1.000 de Polvo
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.components.BULK_AMOUNT_TAG + "x10").performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.STAR_BUY_TAG + "light").performScrollTo().assertIsNotEnabled()
        // MAX compra 4 niveles
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.components.BULK_AMOUNT_TAG + "MAX").performScrollTo().performClick()
        compose.onNodeWithText("x4 · ✦ 400", substring = true).assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.STAR_BUY_TAG + "light").performScrollTo().performClick()
        assertEquals(listOf("light" to com.EGomez.idleecosystem.domain.logic.BulkBuy.MAX), bought)
    }

    @Test
    fun `a map node can be bought several levels at once`() {
        val node = PrestigeUpgrade("pu_core", "El Despertar", "Aumenta la Producción Global.", BigDecimal("1"), BigDecimal("1.5"), BigDecimal("0.10"), UpgradeType.GLOBAL_MULTIPLIER)
        val bought = mutableListOf<Pair<String, Int>>()
        compose.setContent {
            PrestigeShopDialog(listOf(node), "200", BigDecimal(200), onBuy = {}, onDismiss = {}, onBuyMany = { id, amount -> bought += id to amount })
        }
        compose.onNodeWithTag(MAP_VIEW_TOGGLE_TAG + "list").performClick()
        compose.onNodeWithTag(MAP_NODE_ROW_TAG + "pu_core").performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.components.BULK_AMOUNT_TAG + "x10").performClick()
        compose.onNodeWithText("MEJORAR x10", substring = true).assertExists().performClick()
        assertEquals(listOf("pu_core" to 10), bought)
    }

    @Test
    fun `the bestiary card shows the lore and bonus of a creature`() {
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.BestiaryPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.BestiaryUi(levels = mapOf("deep_dagon" to 2), seals = 30),
                    onSummon = {}, onBuySeals = {}, onPickWithShards = {}
                )
            }
        }

        compose.onNodeWithText("1 / 312").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_TILE_TAG + "deep_dagon").performScrollTo().performClick()
        val inCard = androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag(com.EGomez.idleecosystem.ui.screens.main.components.CREATURE_CARD_TAG))
        compose.onNode(androidx.compose.ui.test.hasText("Padre Dagón") and inCard).assertExists()
        compose.onNodeWithText("“El que se alza del fondo cuando la marea muere.”").assertExists()
        compose.onNodeWithText("+30 % producción").assertExists()
        // v0.19.12: la historia larga y el visor con zoom
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CREATURE_HISTORY_TAG).performScrollTo()
            .assertTextEquals(com.EGomez.idleecosystem.domain.logic.BeastLore.history("deep_dagon")!!)
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CREATURE_IMAGE_TAG).performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_VIEWER_TAG).assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_VIEWER_CLOSE_TAG).performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_VIEWER_TAG).assertDoesNotExist()
    }

    @Test
    fun `the collection shows names, rarity and level next to small pictures`() {
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.BestiaryPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.BestiaryUi(levels = mapOf("deep_dagon" to 4)),
                    onSummon = {}, onBuySeals = {}, onPickWithShards = {}
                )
            }
        }

        val dagon = androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_TILE_TAG + "deep_dagon"))
        compose.onNode(androidx.compose.ui.test.hasText("Padre Dagón") and dagon, useUnmergedTree = true).assertExists()
        compose.onNode(androidx.compose.ui.test.hasText("Primigenia · Nv 4") and dagon, useUnmergedTree = true).assertExists()
        val unknown = androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_TILE_TAG + "deep_spawnling"))
        compose.onNode(androidx.compose.ui.test.hasText("???") and unknown, useUnmergedTree = true).assertExists()
        compose.onNode(androidx.compose.ui.test.hasText("Cría de Innsmouth") and unknown, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `an unknown creature's picture cannot be opened`() {
        compose.setContent {
            com.EGomez.idleecosystem.ui.screens.main.components.CreatureCardContent(
                com.EGomez.idleecosystem.domain.logic.Bestiary.beast("deep_spawnling")!!, level = 0, shards = 0, onPick = {}, onDismiss = {}
            )
        }

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CREATURE_IMAGE_TAG).performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_VIEWER_TAG).assertDoesNotExist()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CREATURE_HISTORY_TAG).assertDoesNotExist()
    }

    @Test
    fun `summoning buttons spend seals and a missing creature can be chosen with shards`() {
        val summons = mutableListOf<Int>()
        val picked = mutableListOf<String>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.BestiaryPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.BestiaryUi(seals = 50, shards = 40),
                    onSummon = { summons += it }, onBuySeals = {}, onPickWithShards = { picked += it }
                )
            }
        }

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.SUMMON_ONE_TAG).performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.SUMMON_TEN_TAG).performScrollTo().assertIsNotEnabled()
        assertEquals(listOf(1), summons)

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_TILE_TAG + "deep_spawnling").performScrollTo().performClick()
        compose.onNode(androidx.compose.ui.test.hasText("???") and androidx.compose.ui.test.hasAnyAncestor(androidx.compose.ui.test.hasTestTag(com.EGomez.idleecosystem.ui.screens.main.components.CREATURE_CARD_TAG))).assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.PICK_WITH_SHARDS_TAG).performClick()
        assertEquals(listOf("deep_spawnling"), picked)
    }

    @Test
    fun `an encounter shows a silhouette and gives seals`() {
        var caught = 0
        compose.setContent {
            com.EGomez.idleecosystem.ui.screens.main.components.EncounterBanner("deep_dagon", 30, onCatch = { caught++ })
        }

        compose.onNodeWithText("Algo acecha…").assertExists()
        compose.onNodeWithText("Padre Dagón").assertDoesNotExist()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.ENCOUNTER_CATCH_TAG).performClick()
        assertEquals(1, caught)
    }

    @Test
    fun `summon results show what came out`() {
        compose.setContent {
            com.EGomez.idleecosystem.ui.screens.main.components.SummonResultsDialog(
                listOf(com.EGomez.idleecosystem.domain.logic.SummonResult("deep_dagon", isNew = true, level = 1, shards = 0)),
                onDismiss = {}
            )
        }
        compose.mainClock.advanceTimeBy(1000)
        compose.onNodeWithText("Padre Dagón").assertExists()
        compose.onNodeWithText("NUEVA").assertExists()
    }

    @Test
    fun `the laboratory shows each exponent and buys an affordable experiment`() {
        val bought = mutableListOf<String>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.LabPanel(5, mapOf("armitage" to 2), onBuy = { bought += it })
            }
        }

        compose.onNodeWithText("Ahora: ^1.02").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.LAB_BUY_TAG + "ibn_ghazi").performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.LAB_BUY_TAG + "pnakotic").performScrollTo().assertIsNotEnabled()
        assertEquals(listOf("ibn_ghazi"), bought)
    }

    @Test
    fun `the dream panel toggles the next dream and buys from the tree`() {
        val toggles = mutableListOf<Boolean>()
        val bought = mutableListOf<String>()
        var respecs = 0
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.DreamPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.DreamUi(points = 2, levels = mapOf("steps" to 1)),
                    onSetNext = { toggles += it },
                    onWake = {},
                    onBuy = { bought += it },
                    onRespec = { respecs++ }
                )
            }
        }

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.DREAM_NEXT_TAG).performClick()
        assertEquals(listOf(true), toggles)
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.DREAM_BUY_TAG + "flame").performScrollTo().performClick()
        // Kadath pide Inquanok: bloqueado
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.DREAM_BUY_TAG + "kadath").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.DREAM_RESPEC_TAG).performScrollTo().performClick()
        assertEquals(listOf("flame"), bought)
        assertEquals(1, respecs)
    }

    @Test
    fun `the chain panel buys links it can afford and buys everything at once`() {
        val bought = mutableListOf<Int>()
        var max = 0
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.ChainPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.ChainUi(
                        knowledge = java.math.BigDecimal("5e6"),
                        power = java.math.BigDecimal("1e10"),
                        multiplier = 121.0,
                        amounts = List(8) { java.math.BigDecimal.ZERO },
                        bought = List(8) { 0 }
                    ),
                    onBuy = { bought += it },
                    onBuyMax = { max++ }
                )
            }
        }

        compose.onNodeWithText("Producción x121.00").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CHAIN_BUY_TAG + 1).performScrollTo().performClick()
        // El Cirio Negro cuesta 1e9: no alcanza
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CHAIN_BUY_TAG + 2).performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CHAIN_BUY_MAX_TAG).performScrollTo().performClick()
        assertEquals(listOf(1), bought)
        assertEquals(1, max)
    }

    @Test
    fun `the lethargy panel shows the torpor rewards and toggles`() {
        val toggles = mutableListOf<Boolean>()
        compose.setContent {
            androidx.compose.foundation.layout.Column {
                com.EGomez.idleecosystem.ui.screens.main.components.LethargyBanner(1800)
                com.EGomez.idleecosystem.ui.screens.main.components.LethargyPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.LethargyUi(active = true, seconds = 4200),
                    onSet = { toggles += it }
                )
            }
        }

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.LETHARGY_BANNER_TAG).assertExists()
        compose.onNodeWithText("Sopor de esta vida: 1 h 10 min").assertExists()
        compose.onNodeWithText("Al Ascender: Cordura x4.00").assertExists()
        compose.onNodeWithText("Ascensiones extra: 1 de 4").assertExists()
        compose.onNodeWithText("Próxima Ascensión extra en 50 min").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.LETHARGY_SWITCH_TAG).performClick()
        assertEquals(listOf(false), toggles)
    }

    @Test
    fun `the trials panel shows the active trial goal and chooses the next one`() {
        val chosen = mutableListOf<String?>()
        var abandoned = 0
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.TranscendenceTrialsPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.TranscendenceTrialsUi(
                        next = "broken",
                        active = "hunger",
                        cycleBest = java.math.BigDecimal("2e12"),
                        levels = mapOf("silence" to 5)
                    ),
                    onChoose = { chosen += it },
                    onAbandon = { abandoned++ }
                )
            }
        }

        compose.onNodeWithText("En curso: La Prueba del Hambre I").assertExists()
        compose.onNodeWithText("Meta alcanzada: Trasciende para superarla.").assertExists()
        // La activa no se puede elegir; el Silencio ya está superado entero
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.TTRIALS_CHOOSE_TAG + "hunger").assertDoesNotExist()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.TTRIALS_CHOOSE_TAG + "silence").assertDoesNotExist()
        // Tocar la elegida la quita; tocar otra la elige
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.TTRIALS_CHOOSE_TAG + "broken").performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.TTRIALS_CHOOSE_TAG + "solitude").performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.TTRIALS_ABANDON_TAG).performScrollTo().performClick()
        assertEquals(listOf(null, "solitude"), chosen)
        assertEquals(1, abandoned)
    }

    @Test
    fun `the awakening tab shows the requirements and asks before awakening`() {
        var awakened = 0
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.AwakeningPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.AwakeningUi(
                        canAwaken = true, visionGain = 6, transcendences = 12, trialLevels = 5, awakenings = 1
                    ),
                    onAwaken = { awakened++ }
                )
            }
        }

        compose.onNodeWithText("Trascendencias desde el último Despertar: 12 / 10").assertExists()
        compose.onNodeWithText("DESPERTAR (+6 Visiones)").performClick()
        // Primero la confirmación, con lo que se pierde y lo que se conserva
        compose.onNodeWithText("Ganarás 6 Visiones.").assertExists()
        assertEquals(0, awakened)
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.AWAKEN_CONFIRM_TAG).performClick()
        assertEquals(1, awakened)
    }

    @Test
    fun `oaths are sworn for the next awakening and show their multiplier`() {
        val sworn = mutableListOf<Pair<String, Boolean>>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.AwakeningPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.AwakeningUi(
                        awakenings = 1, oathsActive = setOf("t_silence"), oathsNext = setOf("t_silence", "m_blindness")
                    ),
                    onAwaken = {},
                    onSetOath = { id, value -> sworn += id to value }
                )
            }
        }

        compose.onNodeWithText("Rigen este Despertar: 1 Juramentos · Visiones x1.50").assertExists()
        compose.onNodeWithText("Para el próximo Despertar: 2 Juramentos · Visiones x1.70").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.OATH_TAG + "m_hunger").performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.OATH_TAG + "m_blindness").performScrollTo().performClick()
        assertEquals(listOf("m_hunger" to true, "m_blindness" to false), sworn)
    }

    @Test
    fun `elder signs are equipped on open planets, upgraded and sold`() {
        val actions = mutableListOf<String>()
        val flame = com.EGomez.idleecosystem.domain.logic.ElderSign(
            com.EGomez.idleecosystem.domain.logic.SignKind.FLAME, com.EGomez.idleecosystem.domain.logic.SignRarity.EPIC, 80
        )
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.SignsPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.SignsUi(awakenings = 2, dust = 100, signs = mapOf(7 to flame.pack())),
                    onEquip = { n, planet -> actions += "equip $n $planet" },
                    onUnequip = { actions += "unequip $it" },
                    onUpgrade = { actions += "upgrade $it" },
                    onSell = { actions += "sell $it" },
                    onSellCommons = { actions += "commons" }
                )
            }
        }

        compose.onNodeWithText("Signo de la Llama · Épico · 80 % · Nv 1").assertExists()
        compose.onNodeWithText("Se abre con 3 Despertares").assertExists()
        // Solo hay botones para los planetas abiertos
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.EQUIP_TAG + "7_xoth").assertDoesNotExist()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.EQUIP_TAG + "7_yuggoth").performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.UPGRADE_SIGN_TAG + 7).performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.SELL_SIGN_TAG + 7).performScrollTo().performClick()
        assertEquals(listOf("equip 7 yuggoth", "upgrade 7", "sell 7"), actions)
    }

    @Test
    fun `the expedition shows the horror, strikes and buys unlocked relics`() {
        val actions = mutableListOf<String>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.ExpeditionPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.ExpeditionUi(level = 12, best = 30, gold = 30, goldOnAwaken = 25, dps = 961.0),
                    onStrike = { actions += "strike" },
                    onBuyRelic = { actions += it }
                )
            }
        }

        compose.onNodeWithText("Nivel 12 · mejor 30").assertExists()
        compose.onNodeWithText("Al Despertar: +25 de Oro").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.EXPEDITION_STRIKE_TAG).performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.RELIC_BUY_TAG + "trapezohedron").performScrollTo().performClick()
        // La Corona pide 25 de Oro y se abre en el nivel 25: alcanza; el Espejo pide el nivel 50
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.RELIC_BUY_TAG + "mirror").performScrollTo().assertIsNotEnabled()
        assertEquals(listOf("strike", "trapezohedron"), actions)
    }

    @Test
    fun `tapping two crystals of the same tier fuses them`() {
        val merges = mutableListOf<Pair<Int, Int>>()
        var polished = 0
        val grid = List(com.EGomez.idleecosystem.domain.logic.YuggothCrystals.CELLS) { 0 }.toMutableList().also { it[0] = 3; it[5] = 3; it[6] = 2 }
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.CrystalsPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.CrystalsUi(grid = grid, lustreGain = 4),
                    onMerge = { from, to -> merges += from to to },
                    onMergeAll = {},
                    onPolish = { polished++ }
                )
            }
        }

        // Distinto grado: cambia la selección, no fusiona
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CRYSTAL_CELL_TAG + 6).performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CRYSTAL_CELL_TAG + 0).performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CRYSTAL_CELL_TAG + 5).performClick()
        assertEquals(listOf(0 to 5), merges)
        compose.onNodeWithText("PULIR (+4 Lustre)").performScrollTo().performClick()
        assertEquals(1, polished)
    }

    @Test
    fun `refining asks for confirmation and the refining tree buys from the drills`() {
        var refined = 0
        val bought = mutableListOf<String>()
        var ui by androidx.compose.runtime.mutableStateOf(
            com.EGomez.idleecosystem.ui.screens.main.components.CrystalsUi(lustre = 10, essence = 1)
        )
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.CrystalsPanel(
                    ui,
                    onMerge = { _, _ -> },
                    onMergeAll = {},
                    onPolish = {},
                    onRefine = { refined++ },
                    onBuyNode = { bought += it }
                )
            }
        }
        compose.onNodeWithText("Refinar pide Lustre: 10/25").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.REFINE_BUY_TAG + "facets").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.REFINE_BUY_TAG + "drills").performScrollTo().performClick()
        assertEquals(listOf("drills"), bought)

        ui = ui.copy(lustre = 25, essenceGain = 2)
        compose.onNodeWithText("REFINAR (+2 Esencia)").performScrollTo().performClick()
        assertEquals(0, refined)
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.REFINE_CONFIRM_TAG).performClick()
        assertEquals(1, refined)
    }

    @Test
    fun `an elemental is summoned from its card and its tree buys from the root`() {
        val summoned = mutableListOf<String?>()
        val bought = mutableListOf<String>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.ElementalsPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.ElementalsUi(
                        active = "cthugha",
                        favor = mapOf("cthugha" to 5.0, "ithaqua" to 0.0),
                        favorPerSecond = 0.5
                    ),
                    onSummon = { summoned += it },
                    onBuyNode = { bought += it }
                )
            }
        }
        compose.onNodeWithText("Cthugha invocado · +30.00 Favor por minuto").assertExists()
        // El árbol del invocado: la raíz se compra, lo que pide la raíz no
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.ELEMENTAL_BUY_TAG + "cthugha.pyre").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.ELEMENTAL_BUY_TAG + "cthugha.ember").performScrollTo().performClick()
        assertEquals(listOf("cthugha.ember"), bought)

        // Mirar a Ithaqua e invocarlo
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.ELEMENTAL_CARD_TAG + "ithaqua").performScrollTo().performClick()
        compose.onNodeWithText("Dormido: este árbol no hace efecto hasta que lo invoques.").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.ELEMENTAL_SUMMON_TAG + "ithaqua").performScrollTo().performClick()
        assertEquals(listOf<String?>("ithaqua"), summoned)
    }

    @Test
    fun `the yellow deck shows the card in play, chooses the next and reads chapters`() {
        val chosen = mutableListOf<String?>()
        var abandoned = 0
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.CardsPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.CardsUi(
                        active = "tower",
                        completed = mapOf("fool" to 1),
                        fragments = 5,
                        transcendences = 12
                    ),
                    onChoose = { chosen += it },
                    onAbandon = { abandoned++ }
                )
            }
        }
        compose.onNodeWithText("En juego: XVI · La Torre").assertExists()
        compose.onNodeWithText("¡Meta cumplida! Despierta para completar la Carta.").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CARD_ABANDON_TAG).performClick()
        assertEquals(1, abandoned)

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CARD_CHOOSE_TAG + "moon").performScrollTo().performClick()
        assertEquals(listOf<String?>("moon"), chosen)
        // El Rey de Amarillo espera a las otras siete
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CARD_CHOOSE_TAG + "king").assertDoesNotExist()
        compose.onNodeWithText("Pide completar las otras siete Cartas.").performScrollTo().assertExists()

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CARD_READ_TAG + "fool").performScrollTo().performClick()
        compose.onNodeWithText(com.EGomez.idleecosystem.domain.logic.YellowCard.FOOL.chapter).performScrollTo().assertExists()
    }

    @Test
    fun `artifacts are bought with yellow fragments`() {
        val bought = mutableListOf<String>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.CardsPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.CardsUi(fragments = 6, artifacts = mapOf("dagger" to 5)),
                    onChoose = {},
                    onAbandon = {},
                    onBuyArtifact = { bought += it }
                )
            }
        }
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.ARTIFACT_BUY_TAG + "veil").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.ARTIFACT_BUY_TAG + "dagger").assertDoesNotExist()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.ARTIFACT_BUY_TAG + "lamp").performScrollTo().performClick()
        assertEquals(listOf("lamp"), bought)
    }

    @Test
    fun `descending asks for confirmation and the chaos tree buys from the flutes`() {
        var descended = 0
        val bought = mutableListOf<String>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.DescentPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.DescentUi(
                        chaos = 1, chaosGain = 4, canDescend = true, awakeningsSince = 9, kingCompleted = true
                    ),
                    onDescend = { descended++ },
                    onBuyNode = { bought += it }
                )
            }
        }
        compose.onNodeWithText("✓ Despertares desde el último Descenso: 9/8").assertExists()
        compose.onNodeWithText("DESCENDER (+4 Caos)").performClick()
        assertEquals(0, descended)
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.DESCEND_CONFIRM_TAG).performClick()
        assertEquals(1, descended)

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CHAOS_BUY_TAG + "daemon").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.CHAOS_BUY_TAG + "flutes").performScrollTo().performClick()
        assertEquals(listOf("flutes"), bought)
    }

    @Test
    fun `the vision tree buys from the root and refunds`() {
        val bought = mutableListOf<String>()
        var respecs = 0
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.VisionsPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.AwakeningUi(visions = 3, levels = mapOf("first" to 1)),
                    onBuy = { bought += it },
                    onRespec = { respecs++ }
                )
            }
        }

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.VISION_BUY_TAG + "sanity").performScrollTo().performClick()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.VISION_BUY_TAG + "abyss").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.VISIONS_RESPEC_TAG).performScrollTo().performClick()
        assertEquals(listOf("sanity"), bought)
        assertEquals(1, respecs)
    }

    @Test
    fun `while dreaming the panel shows the pending points and lets you wake up`() {
        var woken = 0
        compose.setContent {
            androidx.compose.foundation.layout.Column {
                com.EGomez.idleecosystem.ui.screens.main.components.DreamBanner(0.75)
                com.EGomez.idleecosystem.ui.screens.main.components.DreamPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.DreamUi(active = true, pending = 8, cycleBest = java.math.BigDecimal("1e14")),
                    onSetNext = {},
                    onWake = { woken++ },
                    onBuy = {},
                    onRespec = {}
                )
            }
        }

        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.DREAM_BANNER_TAG).assertExists()
        compose.onNodeWithText("Al Trascender: +8 Puntos de Sueño").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.DREAM_NEXT_TAG).assertDoesNotExist()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.DREAM_WAKE_TAG).performClick()
        assertEquals(1, woken)
    }

    @Test
    fun `the compact research button clicks like the big one`() {
        var clicks = 0
        compose.setContent { CompactResearchButton("12", onManualClick = { _, _, _ -> clicks++ }) }

        compose.onNodeWithText("+12").assertExists()
        compose.onNodeWithTag(COMPACT_RESEARCH_BUTTON_TAG).performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `the rune altar shows mastery stars`() {
        compose.setContent {
            RuneAltar(
                slots = 0, engraved = emptyList(), catches = emptyMap(),
                onEngrave = {}, onRemove = {}, mastery = mapOf("t1_blood" to 120)
            )
        }

        compose.onNodeWithText("Maestría ★★☆☆☆ · 120 de 250").assertExists()
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

        // A la vista, solo cuándo se duplica; el resto está en los detalles
        compose.onNodeWithText("x2 al generar 3.60e+07").assertExists()
        compose.onNodeWithText("Generado en esta vida: 9.50e+06").assertDoesNotExist()
        compose.onNodeWithTag(ASCEND_DETAILS_TAG).performClick()
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
        selectedTab: MenuTab,
        unlocks: MenuUnlocks = MenuUnlocks.ALL,
        buildings: List<Building> = emptyList(),
        upgrades: List<com.EGomez.idleecosystem.domain.model.Upgrade> = emptyList(),
        ascensions: Int = 0
    ) {
        MenuSection(
            isMenuExpanded = true, onToggleMenu = {}, menuWeight = 0.65f,
            selectedTab = selectedTab, onTabSelected = {},
            buyMultiplier = 1, onBuyMultiplierChanged = {}, showPurchased = false, onToggleShowPurchased = {},
            buildings = buildings, upgrades = upgrades, rituals = emptyList(),
            currentKnowledge = BigDecimal.ZERO, currentEchoes = BigDecimal.ZERO, globalMultiplier = BigDecimal.ONE,
            buildingCalculator = BuildingCalculator(), buildingCostMultiplier = 1.0,
            onBuyBuilding = {}, onBuyUpgrade = {}, onActivateRitual = {},
            prestigeCurrencyFormatted = "0", canPrestige = false, potentialPrestigeFormatted = "0",
            unlocks = unlocks, ascensions = ascensions, onAscendClicked = {}, onOpenMadnessMap = {}
        )
    }

    @Test
    fun `a new player sees only research and the next discovery`() {
        // Aunque se pida el Grimorio, sin descubrirlo se muestra Investigación
        compose.setContent { Menu(selectedTab = MenuTab.GRIMOIRE, unlocks = MenuUnlocks()) }

        compose.onNodeWithText("Grimorio").assertDoesNotExist()
        compose.onNodeWithTag(MENU_TAB_TAG + "research").assertDoesNotExist()
        compose.onNodeWithTag(MENU_LAYER_TAG + "life").assertDoesNotExist()
        compose.onNodeWithText("Próximo descubrimiento: reúne 250 de Conocimiento.").assertExists()
        compose.onNodeWithText("ASCENDER").assertDoesNotExist()
        compose.onNodeWithTag(BUY_MULTIPLIER_TAG + "1").assertDoesNotExist()
    }

    @Test
    fun `tabs and layers appear as they are discovered and new ones are marked`() {
        var unlocks by mutableStateOf(MenuUnlocks(setOf(MenuTab.RESEARCH, MenuTab.GRIMOIRE)))
        compose.setContent {
            MenuSection(
                isMenuExpanded = true, onToggleMenu = {}, menuWeight = 0.65f,
                selectedTab = MenuTab.RESEARCH, onTabSelected = {},
                buyMultiplier = 1, onBuyMultiplierChanged = {}, showPurchased = false, onToggleShowPurchased = {},
                buildings = emptyList(), upgrades = emptyList(), rituals = emptyList(),
                currentKnowledge = BigDecimal.ZERO, currentEchoes = BigDecimal.ZERO, globalMultiplier = BigDecimal.ONE,
                buildingCalculator = BuildingCalculator(), buildingCostMultiplier = 1.0,
                onBuyBuilding = {}, onBuyUpgrade = {}, onActivateRitual = {},
                prestigeCurrencyFormatted = "0", canPrestige = false, potentialPrestigeFormatted = "0",
                unlocks = unlocks, newTabs = setOf(MenuTab.GRIMOIRE),
                onAscendClicked = {}, onOpenMadnessMap = {}
            )
        }

        // Dos pestañas de la Vida: fila de pestañas, sin fila de capas
        compose.onNodeWithTag(MENU_TAB_TAG + "grimoire").assertExists()
        compose.onNodeWithTag(NEW_BADGE_TAG, useUnmergedTree = true).assertExists()
        compose.onNodeWithTag(MENU_LAYER_TAG + "life").assertDoesNotExist()
        compose.onNodeWithText("Próximo descubrimiento: encuentra un Eco al investigar.").assertExists()

        // Con la Ascensión aparece la capa nueva y la barra de ASCENDER
        unlocks = MenuUnlocks(setOf(MenuTab.RESEARCH, MenuTab.GRIMOIRE, MenuTab.RITUALS, MenuTab.SANITY))
        compose.onNodeWithTag(MENU_LAYER_TAG + "ascension").assertExists()
        compose.onNodeWithTag(MENU_LAYER_TAG + "transcendence").assertDoesNotExist()
        compose.onNodeWithText("ASCENDER").assertExists()
        compose.onNodeWithText("Próximo descubrimiento: Asciende por primera vez.").assertExists()
    }

    @Test
    fun `the transcendence layer groups its challenges under cycles`() {
        val chosen = mutableListOf<MenuTab>()
        compose.setContent {
            MenuSection(
                isMenuExpanded = true, onToggleMenu = {}, menuWeight = 0.65f,
                selectedTab = MenuTab.CHAIN, onTabSelected = { chosen += it },
                buyMultiplier = 1, onBuyMultiplierChanged = {}, showPurchased = false, onToggleShowPurchased = {},
                buildings = emptyList(), upgrades = emptyList(), rituals = emptyList(),
                currentKnowledge = BigDecimal.ZERO, currentEchoes = BigDecimal.ZERO, globalMultiplier = BigDecimal.ONE,
                buildingCalculator = BuildingCalculator(), buildingCostMultiplier = 1.0,
                onBuyBuilding = {}, onBuyUpgrade = {}, onActivateRitual = {},
                prestigeCurrencyFormatted = "0", canPrestige = false, potentialPrestigeFormatted = "0",
                unlocks = MenuUnlocks.ALL, newTabs = setOf(MenuTab.LETHARGY),
                onAscendClicked = {}, onOpenMadnessMap = {}
            )
        }

        // Arriba solo cuatro pestañas; los desafíos de ciclo van en chips
        compose.onNodeWithTag(MENU_TAB_TAG + "cycles").assertExists()
        compose.onNodeWithTag(MENU_TAB_TAG + "chain").assertDoesNotExist()
        compose.onNodeWithTag(MENU_TAB_TAG + "mutations").assertDoesNotExist()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.MENU_CHIP_TAG + "dream").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.MENU_CHIP_TAG + "lethargy").performClick()
        // Éter abre Éter (con Mutaciones como chip dentro)
        compose.onNodeWithTag(MENU_TAB_TAG + "ether").performClick()
        assertEquals(listOf(MenuTab.LETHARGY, MenuTab.ETHER), chosen)
    }

    @Test
    fun `new creatures carry a badge until their card is opened`() {
        val viewed = mutableListOf<String>()
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                com.EGomez.idleecosystem.ui.screens.main.components.BestiaryPanel(
                    com.EGomez.idleecosystem.ui.screens.main.components.BestiaryUi(
                        levels = mapOf("deep_dagon" to 1, "deep_hydra" to 1),
                        newBeasts = setOf("deep_dagon")
                    ),
                    onSummon = {}, onBuySeals = {}, onPickWithShards = {}, onViewed = { viewed += it }
                )
            }
        }

        compose.onNodeWithText("1 nuevas").assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_NEW_TAG + "deep_dagon", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_NEW_TAG + "deep_hydra", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag(com.EGomez.idleecosystem.ui.screens.main.components.BEAST_TILE_TAG + "deep_dagon").performScrollTo().performClick()
        assertEquals(listOf("deep_dagon"), viewed)
    }

    @Test
    fun `discovered systems look as before`() {
        compose.setContent { Menu(selectedTab = MenuTab.GRIMOIRE) }

        compose.onNodeWithTag(NEXT_UNLOCK_TAG).assertDoesNotExist()
        compose.onNodeWithTag(MENU_LAYER_TAG + "transcendence").assertExists()
        compose.onNodeWithText("ASCENDER").assertExists()
    }

    @Test
    fun `a rune boost chip names its rune and what it gives`() {
        val buff = RuneBuff(id = "t1_blood", multiplier = 10.0, expirationTime = 15_000L, description = "", durationMs = 15_000L)
        compose.setContent { ActiveBuffsDisplay(listOf(buff), clock = { 0L }) }

        compose.onNodeWithTag(BUFF_CHIP_TAG + "t1_blood").assertExists()
        compose.onNodeWithText("Runa de Sangre").assertExists()
        compose.onNodeWithText("x10 · 15s").assertExists()
    }

    @Test
    fun `a rune plate shows its tier, its name and its real effect`() {
        compose.setContent { RunePlate(RuneCatalog.T1_BLOOD, masteryLevel = 0) }

        compose.onNodeWithText("COMÚN · DESLIZA").assertExists()
        compose.onNodeWithText("Runa de Sangre").assertExists()
        compose.onNodeWithText("Producción x10 durante 15 s").assertExists()
    }

    @Test
    fun `rune texts follow mastery and instant rewards`() {
        compose.setContent {
            androidx.compose.foundation.layout.Column {
                // Maestría V: el potenciador dura un 50 % más
                RunePlate(RuneCatalog.T1_ASH, masteryLevel = 5)
                RuneCaughtNotice(RuneCatalog.T1_BONE, masteryLevel = 0)
                RuneCaughtNotice(RuneCatalog.T3_CTHULHU, masteryLevel = 0)
            }
        }

        compose.onNodeWithText("Producción x8 durante 30 s").assertExists()
        compose.onNodeWithText("+2 min de producción al instante").assertExists()
        compose.onNodeWithText("Producción x30 durante 60 s y +1 Eco").assertExists()
    }

    @Test
    fun `the mutation catalog hides the mutations not yet chosen`() {
        compose.setContent {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                MutationCatalog(seen = setOf("feverish_notes"), active = com.EGomez.idleecosystem.domain.model.Mutation.FEVERISH_NOTES)
            }
        }

        compose.onNodeWithText("Mutaciones descubiertas: 1 de 20").assertExists()
        compose.onNodeWithText("Notas Febriles").assertExists()
        compose.onNodeWithText("ACTIVA").assertExists()
        compose.onNodeWithTag(MUTATION_ROW_TAG + "hungry_void").assertExists()
        compose.onNodeWithText("Vacío Hambriento").assertDoesNotExist()
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
        compose.setContent { Menu(selectedTab = MenuTab.GRIMOIRE, buildings = catalog, upgrades = listOf(synergy)) }

        compose.onNodeWithText("Efecto: +1% por unidad de Notas Desordenadas").assertExists()
        compose.onNodeWithText("Efecto: x1").assertDoesNotExist()
    }

    @Test
    fun `the auto chip appears with the building autobuyer milestone`() {
        var ascensions by mutableStateOf(4)
        compose.setContent { Menu(selectedTab = MenuTab.RESEARCH, ascensions = ascensions) }

        compose.onNodeWithTag(AUTO_BUILDINGS_TAG).assertDoesNotExist()
        ascensions = 5
        compose.onNodeWithTag(AUTO_BUILDINGS_TAG).assertExists()
    }

    @Test
    fun `the ascend dialog lists the milestones and marks the next one`() {
        compose.setContent { AscendDialog(sanityGainFormatted = "3", onConfirm = {}, onDismiss = {}, ascensions = 2) }

        compose.onNodeWithText("Esta será tu Ascensión n.º 3.").assertDoesNotExist()
        compose.onNodeWithTag(ASCEND_DETAILS_TAG).performClick()
        compose.onNodeWithText("Esta será tu Ascensión n.º 3.").assertExists()
        compose.onNodeWithText("✓ 1: Empiezas cada vida con 10 Sinapsis.").assertExists()
        compose.onNodeWithText("★ 3: Conservas las mejoras de clic del Grimorio.").assertExists()
        compose.onNodeWithText("· 5: Autocomprador de edificios.").assertExists()
    }

    @Test
    fun `the madness map list shows constellations and locked paths`() {
        val upgrades = com.EGomez.idleecosystem.domain.logic.PrestigeShopManager().upgrades.map { it.copy() }
        compose.setContent { PrestigeShopDialog(upgrades, "50", BigDecimal(50), {}, {}) }

        compose.onNodeWithTag(MAP_VIEW_TOGGLE_TAG + "list").performClick()
        compose.onNodeWithTag(MAP_CHIP_TAG + "mind").performClick()
        compose.onNodeWithText("LA MENTE").assertExists()
        compose.onNodeWithTag(MAP_LIST_TAG).performScrollToNode(hasTestTag(MAP_NODE_ROW_TAG + MadnessPaths.MIND))
        compose.onNodeWithTag(MAP_NODE_ROW_TAG + MadnessPaths.MIND).performClick()
        compose.onNodeWithText("Ilumina toda la constelación para abrir su Senda.").assertExists()
        compose.onNodeWithText("Constelación incompleta").assertIsNotEnabled()

        compose.onNodeWithTag(MAP_CHIP_TAG + "all").performClick()
        compose.onNodeWithTag(MAP_LIST_TAG).performScrollToNode(hasTestTag(MAP_NODE_ROW_TAG + "pu_core"))
        compose.onNodeWithTag(MAP_NODE_ROW_TAG + "pu_core").performClick()
        compose.onNodeWithText("Siguiente nivel: Producción x1.10").assertExists()
        compose.onNodeWithText("MEJORAR", substring = true).assertIsEnabled()
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
        compose.setContent { Menu(selectedTab = MenuTab.PANTHEON, ascensions = 0) }

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

        compose.onNodeWithText("Sin Pacto").assertExists()
        compose.onNodeWithTag(ASCEND_ROW_TAG + "pact").performClick()
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
    fun `the attention card appears after the first ascension and opens the dialog`() {
        var points by mutableStateOf(0)
        var tab by mutableStateOf(MenuTab.SANITY)
        var opened = 0
        compose.setContent {
            MenuSection(
                isMenuExpanded = true, onToggleMenu = {}, menuWeight = 0.65f,
                selectedTab = tab, onTabSelected = {},
                buyMultiplier = 1, onBuyMultiplierChanged = {}, showPurchased = false, onToggleShowPurchased = {},
                buildings = emptyList(), upgrades = emptyList(), rituals = emptyList(),
                currentKnowledge = BigDecimal.ZERO, currentEchoes = BigDecimal.ZERO, globalMultiplier = BigDecimal.ONE,
                buildingCalculator = BuildingCalculator(), buildingCostMultiplier = 1.0,
                onBuyBuilding = {}, onBuyUpgrade = {}, onActivateRitual = {},
                prestigeCurrencyFormatted = "0", canPrestige = false, potentialPrestigeFormatted = "0",
                attentionPoints = points, onOpenAttention = { opened++ },
                attention = mapOf("notes" to 1), onAscendClicked = {}, onOpenMadnessMap = {}
            )
        }

        compose.onNodeWithTag(ATTENTION_BAR_TAG).assertDoesNotExist()
        points = 1
        compose.onNodeWithTag(ATTENTION_BAR_TAG).assertExists().performClick()
        assertEquals(1, opened)
        // Vive en la pestaña Cordura, no en Investigación
        tab = MenuTab.RESEARCH
        compose.onNodeWithTag(ATTENTION_BAR_TAG).assertDoesNotExist()
    }
}

