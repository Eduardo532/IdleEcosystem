package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuUnlocksTest {

    private fun compute(
        reveal: SystemReveal = SystemReveal(false, false, false),
        ascensions: Int = 0,
        runeSlots: Int = 0,
        mastery: Boolean = false,
        transcend: Boolean = false,
        mutations: Boolean = false
    ) = MenuUnlocks.compute(reveal, ascensions, runeSlots, mastery, transcend, mutations)

    @Test
    fun `a new game shows only research`() {
        val unlocks = compute()
        assertEquals(setOf(MenuTab.RESEARCH), unlocks.tabs)
        assertEquals(listOf(MenuLayer.LIFE), unlocks.layers)
        assertEquals(NextUnlock.GRIMOIRE, unlocks.next)
    }

    @Test
    fun `systems appear in order and each layer shows up with its first tab`() {
        val life = compute(SystemReveal(grimoire = true, rituals = true, ascension = false))
        assertEquals(listOf(MenuTab.RESEARCH, MenuTab.GRIMOIRE, MenuTab.RITUALS), life.tabsOf(MenuLayer.LIFE))
        assertEquals(NextUnlock.ASCENSION, life.next)

        val firstAscension = compute(SystemReveal(), ascensions = 0)
        assertEquals(listOf(MenuLayer.LIFE, MenuLayer.ASCENSION), firstAscension.layers)
        assertEquals(listOf(MenuTab.SANITY), firstAscension.tabsOf(MenuLayer.ASCENSION))
        assertEquals(NextUnlock.PANTHEON, firstAscension.next)

        val trials = compute(SystemReveal(), ascensions = 2)
        assertTrue(trials.has(MenuTab.PANTHEON) && trials.has(MenuTab.TRIALS))
        assertFalse(trials.has(MenuTab.RUNES))
        assertEquals(NextUnlock.TRANSCENDENCE, trials.next)
    }

    @Test
    fun `runes and the transcendence layer need their own discoveries`() {
        assertTrue(compute(SystemReveal(), ascensions = 5, runeSlots = 1).has(MenuTab.RUNES))
        val transcended = compute(SystemReveal(), ascensions = 60, transcend = true, mutations = true, mastery = true)
        assertEquals(listOf(MenuLayer.LIFE, MenuLayer.ASCENSION, MenuLayer.TRANSCENDENCE), transcended.layers)
        assertEquals(listOf(MenuTab.ETHER, MenuTab.MUTATIONS), transcended.tabsOf(MenuLayer.TRANSCENDENCE))
        assertTrue(transcended.has(MenuTab.RUNES))
        assertNull(transcended.next)
    }

    @Test
    fun `the transcendence layer shows four tabs and groups the rest`() {
        val all = MenuUnlocks.ALL
        assertEquals(listOf(MenuTab.ETHER, MenuTab.STARS, MenuTab.LAB, MenuTab.CYCLES), all.topTabsOf(MenuLayer.TRANSCENDENCE))
        assertEquals(listOf(MenuTab.ETHER, MenuTab.MUTATIONS), all.choicesOf(MenuTab.ETHER))
        assertEquals(listOf(MenuTab.DREAM, MenuTab.CHAIN, MenuTab.LETHARGY, MenuTab.TTRIALS), all.choicesOf(MenuTab.CYCLES))
        assertTrue(all.choicesOf(MenuTab.LAB).isEmpty())
        // Ciclos no tiene contenido: abre su primera pestaña, o la nueva
        assertEquals(MenuTab.DREAM, all.resolve(MenuTab.CYCLES))
        assertEquals(MenuTab.CHAIN, all.entryOf(MenuTab.CYCLES, setOf(MenuTab.CHAIN)))
        // Un grupo con contenido abre lo nuevo de adentro, si lo hay
        assertEquals(MenuTab.MUTATIONS, all.entryOf(MenuTab.ETHER, setOf(MenuTab.MUTATIONS)))
        assertEquals(MenuTab.ETHER, all.entryOf(MenuTab.ETHER))
        assertEquals(MenuTab.LAB, all.entryOf(MenuTab.LAB))
        assertTrue(MenuTab.CYCLES in MenuUnlocks.NEVER_NEW)
    }

    @Test
    fun `the awakening layer brings its two tabs`() {
        val awake = MenuUnlocks.compute(SystemReveal(), 60, 0, false, true, true, awakeningRevealed = true)
        // Visiones va dentro de Despertar (v0.25.2)
        assertEquals(listOf(MenuTab.AWAKEN), awake.topTabsOf(MenuLayer.AWAKENING))
        assertEquals(listOf(MenuTab.AWAKEN, MenuTab.VISIONS), awake.choicesOf(MenuTab.AWAKEN))
        assertTrue(MenuLayer.AWAKENING in awake.layers)
        assertFalse(MenuLayer.AWAKENING in compute(transcend = true).layers)
    }

    @Test
    fun `the awakening layer shows three tabs and groups the minigames`() {
        val all = MenuUnlocks.ALL
        assertEquals(listOf(MenuTab.AWAKEN, MenuTab.MYSTERIES, MenuTab.CARDS), all.topTabsOf(MenuLayer.AWAKENING))
        assertEquals(
            listOf(MenuTab.SIGNS, MenuTab.EXPEDITION, MenuTab.CRYSTALS, MenuTab.ELEMENTALS),
            all.choicesOf(MenuTab.MYSTERIES)
        )
        // Misterios no tiene contenido: abre su primera pestaña, o la nueva
        assertEquals(MenuTab.SIGNS, all.resolve(MenuTab.MYSTERIES))
        assertEquals(MenuTab.CRYSTALS, all.entryOf(MenuTab.MYSTERIES, setOf(MenuTab.CRYSTALS)))
        assertTrue(MenuTab.MYSTERIES in MenuUnlocks.NEVER_NEW)

        // Con el primer Despertar aparecen Signos y Expedición dentro de Misterios
        val first = MenuUnlocks.compute(
            SystemReveal(), 60, 0, false, true, true,
            awakeningRevealed = true, signsRevealed = true, expeditionRevealed = true
        )
        assertEquals(listOf(MenuTab.AWAKEN, MenuTab.MYSTERIES), first.topTabsOf(MenuLayer.AWAKENING))
        assertEquals(listOf(MenuTab.SIGNS, MenuTab.EXPEDITION), first.choicesOf(MenuTab.MYSTERIES))
    }

    @Test
    fun `the cycles group appears with its first challenge`() {
        val early = compute(SystemReveal(), ascensions = 60, transcend = true, mutations = true)
        assertEquals(listOf(MenuTab.ETHER), early.topTabsOf(MenuLayer.TRANSCENDENCE))
        assertFalse(early.has(MenuTab.CYCLES))
        val dream = MenuUnlocks.compute(SystemReveal(), 60, 0, false, true, true, dreamRevealed = true)
        assertEquals(listOf(MenuTab.ETHER, MenuTab.CYCLES), dream.topTabsOf(MenuLayer.TRANSCENDENCE))
        assertEquals(listOf(MenuTab.DREAM), dream.choicesOf(MenuTab.CYCLES))
        assertEquals(MenuTab.DREAM, dream.resolve(MenuTab.CYCLES))
    }

    @Test
    fun `a tab that is not visible resolves to research`() {
        val unlocks = compute()
        assertEquals(MenuTab.RESEARCH, unlocks.resolve(MenuTab.ETHER))
        assertEquals(MenuTab.RESEARCH, unlocks.resolve(null))
        assertEquals(MenuTab.ETHER, MenuUnlocks.ALL.resolve(MenuTab.ETHER))
        assertEquals(MenuTab.MUTATIONS, MenuTab.fromId("mutations"))
    }
}
