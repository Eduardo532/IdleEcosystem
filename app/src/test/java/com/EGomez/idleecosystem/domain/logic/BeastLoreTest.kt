package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.ui.i18n.ContentTranslations
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** v0.19.12: cada criatura tiene una historia larga, distinta de las demás y traducida. */
class BeastLoreTest {

    @Test
    fun everyBeastHasALongHistory() {
        Bestiary.beasts.forEach { beast ->
            val history = BeastLore.history(beast.id)
            assertTrue("${beast.id} no tiene historia", history != null)
            assertTrue("${beast.id}: la historia es corta", history!!.split(' ').size >= 25)
            assertTrue("${beast.id}: la historia no es más larga que la cita", history.length > beast.lore.length * 2)
        }
    }

    @Test
    fun historiesAreAllDifferent() {
        val all = Bestiary.beasts.mapNotNull { BeastLore.history(it.id) }
        assertTrue(all.size == all.toSet().size)
    }

    @Test
    fun everyHistoryIsTranslated() {
        Bestiary.beasts.forEach { beast ->
            val history = BeastLore.history(beast.id)!!
            val english = ContentTranslations.english(history)
            assertTrue("${beast.id} sin traducir", english != null)
            assertNotEquals(history, english)
        }
    }
}
