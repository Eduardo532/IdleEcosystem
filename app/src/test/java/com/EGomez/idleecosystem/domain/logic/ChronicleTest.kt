package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.TutorialStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ChronicleTest {

    @Test
    fun `five acts of four chapters, the last one is the ending`() {
        assertEquals(5, ChronicleAct.entries.size)
        assertEquals(20, ChronicleChapter.entries.size)
        assertEquals(20, ChronicleChapter.entries.map { it.id }.toSet().size)
        ChronicleAct.entries.forEach { assertEquals(it.name, 4, Chronicle.chaptersOf(it).size) }
        assertEquals(ChronicleChapter.EYE, ChronicleChapter.entries.last())
        assertEquals(AchievementStat.AZATHOTH_AWAKENINGS, ChronicleChapter.EYE.stat)
        assertNull(ChronicleChapter.fromId("prologue"))
        ChronicleChapter.entries.forEach { assertTrue(it.id, it.text.isNotBlank() && it.target > 0) }
    }

    @Test
    fun `chapters are written once, when their stat reaches the target`() {
        val values = mapOf(AchievementStat.BUILDINGS_TOTAL to BigDecimal(3), AchievementStat.CLICKS to BigDecimal(499))
        val value: (String) -> BigDecimal = { values[it] ?: BigDecimal.ZERO }
        assertEquals(listOf(ChronicleChapter.FIRST_NOTE), Chronicle.newChapters(emptySet(), value))
        assertTrue(Chronicle.newChapters(setOf("first_note"), value).isEmpty())
    }

    @Test
    fun `acts start with a chapter and complete with all four`() {
        val life = Chronicle.chaptersOf(ChronicleAct.LIFE).map { it.id }.toSet()
        assertFalse(Chronicle.isActStarted(ChronicleAct.LIFE, emptySet()))
        assertTrue(Chronicle.isActStarted(ChronicleAct.LIFE, setOf("first_note")))
        assertFalse(Chronicle.isActComplete(ChronicleAct.LIFE, setOf("first_note")))
        assertTrue(Chronicle.isActComplete(ChronicleAct.LIFE, life))
        assertEquals(0.2f, Chronicle.progress(life), 1e-6f)
        assertEquals(1f, Chronicle.progress(ChronicleChapter.entries.map { it.id }.toSet()), 1e-6f)
    }

    @Test
    fun `a seen tutorial can be shown again, an unseen one cannot`() {
        val manager = TutorialManager()
        manager.loadProgress(setOf(TutorialStep.WELCOME.id), isEnabled = false)
        manager.replayStep(TutorialStep.MENU_INTRO)
        assertNull(manager.currentStep.value)
        // Se vuelve a ver aunque los tutoriales estén apagados
        manager.replayStep(TutorialStep.WELCOME)
        assertEquals(TutorialStep.WELCOME, manager.currentStep.value)
        manager.dismissCurrentStep()
        assertNull(manager.currentStep.value)
        assertTrue(TutorialStep.WELCOME.id in manager.completedSteps.value)
    }
}
