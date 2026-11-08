package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.TutorialStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialProgressTest {

    private val saveManager = SaveManager()

    @Test
    fun `explicit tutorial list in the save code is kept`() {
        val data = saveManager.parseJson("""{"knowledge": 1, "completedTutorialSteps": ["welcome", "rituals_tab", null]}""")!!
        assertEquals(listOf("welcome", "rituals_tab"), data.completedTutorialSteps)
        assertTrue(TutorialProgress.stepsSeenIn(data).containsAll(listOf("welcome", "rituals_tab")))
    }

    @Test
    fun `legacy code infers seen tutorials from progress`() {
        // Code exported by v0.10.0: no tutorial list, but an advanced game
        val data = saveManager.parseJson(
            """{"knowledge": 5000000, "sanity": 12, "echoes": 3,
                "buildings": [{"id": "b_notes", "amount": 40}],
                "upgrades": ["u_click_1"],
                "prestigeUpgrades": [{"id": "pu_core", "level": 1}],
                "statistics": {"totalPlayTimeSeconds": 9000, "totalPrestiges": 2, "totalEchoes": "30",
                               "totalEventsFound": 4, "runesSevered": 3, "totalOfflineSeconds": 600}}"""
        )!!
        val seen = TutorialProgress.stepsSeenIn(data)

        val expected = listOf(
            TutorialStep.WELCOME, TutorialStep.MENU_INTRO, TutorialStep.FIRST_BUILDING,
            TutorialStep.BUY_MULTIPLIERS, TutorialStep.UPGRADES_TAB, TutorialStep.TOP_BAR_INFO,
            TutorialStep.ECHO_FOUND, TutorialStep.RITUALS_TAB, TutorialStep.EVENT_APPEAR,
            TutorialStep.RUNE_INTRO_GENERIC, TutorialStep.RUNE_TUTORIAL_SWIPE,
            TutorialStep.PRESTIGE_READY, TutorialStep.PRESTIGE_MECHANICS, TutorialStep.PRESTIGE_SHOP,
            TutorialStep.OFFLINE_GAINS
        )
        expected.forEach { assertTrue("${it.id} should be marked as seen", it.id in seen) }

        // Never stabilized or inscribed a rune: those tutorials are still pending
        assertFalse(TutorialStep.RUNE_TUTORIAL_HOLD.id in seen)
        assertFalse(TutorialStep.RUNE_TUTORIAL_TRACE.id in seen)
    }

    @Test
    fun `an empty save marks nothing as seen`() {
        val data = saveManager.parseJson("""{"knowledge": 0, "buildings": []}""")!!
        assertTrue(TutorialProgress.stepsSeenIn(data).isEmpty())
    }

    @Test
    fun `marking steps as completed removes them from screen and queue`() {
        val manager = TutorialManager()
        manager.triggerStep(TutorialStep.WELCOME)       // shown
        manager.triggerStep(TutorialStep.MENU_INTRO)    // queued
        manager.triggerStep(TutorialStep.ECHO_FOUND)    // queued

        manager.markStepsAsCompleted(setOf(TutorialStep.WELCOME.id, TutorialStep.MENU_INTRO.id))

        assertEquals(TutorialStep.ECHO_FOUND, manager.currentStep.value)
        manager.dismissCurrentStep()
        assertNull(manager.currentStep.value)

        // Completed steps are never shown again
        manager.triggerStep(TutorialStep.WELCOME)
        assertNull(manager.currentStep.value)
    }
}
