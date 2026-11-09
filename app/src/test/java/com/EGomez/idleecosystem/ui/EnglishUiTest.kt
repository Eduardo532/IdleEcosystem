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
}
