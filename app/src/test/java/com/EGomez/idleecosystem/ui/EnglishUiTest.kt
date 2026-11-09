package com.EGomez.idleecosystem.ui

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.EGomez.idleecosystem.core.locale.AppLanguage
import com.EGomez.idleecosystem.ui.screens.main.dialogs.AscendDialog
import com.EGomez.idleecosystem.ui.screens.main.dialogs.SettingsDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** La misma interfaz con el idioma en inglés. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "en")
class EnglishUiTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `the ascend dialog is in english`() {
        compose.setContent { AscendDialog(sanityGainFormatted = "3", onConfirm = {}, onDismiss = {}) }

        compose.onNodeWithText("ASCEND?").assertExists()
        compose.onNodeWithText("You will gain +3 Residual Sanity").assertExists()
        compose.onNodeWithText("CANCEL").assertExists()
    }

    @Test
    fun `the language section lists both languages in their own names`() {
        var chosen: AppLanguage? = null
        compose.setContent {
            SettingsDialog(
                uiState = GameUiState(language = AppLanguage.ENGLISH),
                onDismiss = {}, onStatsClick = {}, onToggleMusic = {}, onToggleSfx = {},
                onToggleParticles = {}, onToggleFloatingText = {}, onNotationSelected = {},
                onLanguageSelected = { chosen = it },
                onExportClick = {}, onImportClick = {}, onCancelImport = {}, onPasteClick = {},
                onHardResetClick = {}, onToggleTutorial = {}
            )
        }

        compose.onNodeWithText("Settings").assertExists()
        compose.onNodeWithText("Language").performClick()
        compose.onNodeWithText("English").assertExists()
        compose.onNodeWithText("Español").performClick()
        assertEquals(AppLanguage.SPANISH, chosen)
    }

    @Test
    fun `game content is shown in english`() {
        val notes = com.EGomez.idleecosystem.domain.logic.BuildingCatalog.create().first { it.id == "b_notes" }
        compose.setContent {
            com.EGomez.idleecosystem.ui.components.BuildingItem(
                notes, java.math.BigDecimal(100), java.math.BigDecimal.ONE,
                com.EGomez.idleecosystem.domain.logic.BuildingCalculator(), buyMultiplier = 1
            ) {}
        }

        compose.onNodeWithText("Messy Notes").assertExists()
        compose.onNodeWithText("Frantic scribbles.").assertExists()
    }

    @Test
    fun `the next goal is translated`() {
        compose.setContent {
            com.EGomez.idleecosystem.ui.screens.main.dialogs.AchievementsDialog(
                achievements = emptyList(), achievementsProgress = "0 / 0", secretsProgress = null,
                nextGoal = com.EGomez.idleecosystem.domain.logic.NextGoal("b_not_2", "Notas II", "50 Notas", 0.68f),
                onDismiss = {}
            )
        }

        compose.onNodeWithText("Next goal: 50 Notes (68%)").assertExists()
    }
}
