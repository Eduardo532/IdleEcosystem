package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YellowCardsTest {

    @Test
    fun `the deck has eight cards and every oath exists`() {
        assertEquals(8, YellowCard.entries.size)
        assertEquals(8, YellowCard.entries.map { it.id }.toSet().size)
        YellowCard.entries.forEach { card ->
            assertEquals(card.id, card.oathIds.size, card.oaths.size)
            assertTrue(card.id, card.chapter.isNotBlank())
        }
        assertEquals(YellowCard.MOON, YellowCard.fromId("moon"))
        assertNull(YellowCard.fromId("lovers"))
    }

    @Test
    fun `goals ask for more than the awakening itself and grow`() {
        val goals = YellowCard.entries.map { it.goal }
        assertTrue(goals.all { it > Awakening.REQUIRED_TRANSCENDENCES })
        assertEquals(goals.sorted(), goals)
        assertEquals(16, YellowCard.KING.goal)
    }

    @Test
    fun `the cards open at the fifth awakening with all four elementals`() {
        assertFalse(YellowCards.isUnlocked(4, true))
        assertFalse(YellowCards.isUnlocked(5, false))
        assertTrue(YellowCards.isUnlocked(5, true))
    }

    @Test
    fun `the king waits for the other seven`() {
        assertTrue(YellowCards.isAvailable(YellowCard.TOWER, emptySet()))
        assertFalse(YellowCards.isAvailable(YellowCard.KING, emptySet()))
        val seven = YellowCard.entries.filter { it != YellowCard.KING }.toSet()
        assertFalse(YellowCards.isAvailable(YellowCard.KING, seven - YellowCard.DEATH))
        assertTrue(YellowCards.isAvailable(YellowCard.KING, seven))
    }

    @Test
    fun `completing pays fragments, half when repeated`() {
        assertFalse(YellowCards.isComplete(YellowCard.MOON, 12))
        assertTrue(YellowCards.isComplete(YellowCard.MOON, 13))
        assertEquals(20, YellowCards.fragmentsFor(YellowCard.KING, firstTime = true))
        assertEquals(10, YellowCards.fragmentsFor(YellowCard.KING, firstTime = false))
        assertEquals(2, YellowCards.fragmentsFor(YellowCard.FOOL, firstTime = false))
    }

    @Test
    fun `each completed card gives its own bonus`() {
        assertEquals(1.0, YellowCards.factor(CardBonus.PRODUCTION, emptySet()), 1e-12)
        assertEquals(10.0, YellowCards.factor(CardBonus.PRODUCTION, setOf(YellowCard.TOWER)), 1e-12)
        assertEquals(1.0, YellowCards.factor(CardBonus.CLICK, setOf(YellowCard.TOWER)), 1e-12)
        assertEquals(1.05, YellowCards.factor(CardBonus.EXPONENT, setOf(YellowCard.KING)), 1e-12)
        assertEquals(CardBonus.entries.size, YellowCard.entries.map { it.bonus }.toSet().size)
    }

    @Test
    fun `the card in play adds its oaths without repeating the sworn ones`() {
        val sworn = listOfNotNull(Oaths.fromId("m_hunger"), Oaths.fromId("m_amnesia"))
        val rules = YellowCards.ruleOaths(sworn, YellowCard.TOWER)
        assertEquals(listOf("m_hunger", "m_amnesia", "m_burned_grimoire"), rules.map { it.id })
        assertEquals(sworn, YellowCards.ruleOaths(sworn, null))
    }
}
