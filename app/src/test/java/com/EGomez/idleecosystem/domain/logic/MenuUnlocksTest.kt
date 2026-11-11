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
        assertEquals(MenuLayer.entries, transcended.layers)
        assertEquals(listOf(MenuTab.ETHER, MenuTab.MUTATIONS), transcended.tabsOf(MenuLayer.TRANSCENDENCE))
        assertTrue(transcended.has(MenuTab.RUNES))
        assertNull(transcended.next)
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
