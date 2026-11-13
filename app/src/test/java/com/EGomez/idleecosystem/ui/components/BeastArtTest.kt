package com.EGomez.idleecosystem.ui.components

import com.EGomez.idleecosystem.domain.logic.Bestiary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** v0.19.11: cada criatura del Bestiario tiene su ilustración propia, y ninguna sobra. */
class BeastArtTest {

    @Test
    fun everyBeastHasItsOwnIllustration() {
        val missing = Bestiary.beasts.map { it.id }.filterNot { it in BeastArt.drawables }
        assertTrue("Criaturas sin ilustración: $missing", missing.isEmpty())
    }

    @Test
    fun noIllustrationIsLeftOver() {
        val ids = Bestiary.beasts.map { it.id }.toSet()
        val extra = BeastArt.drawables.keys.filterNot { it in ids }
        assertTrue("Ilustraciones sin criatura: $extra", extra.isEmpty())
    }

    @Test
    fun illustrationsAreNotShared() {
        assertEquals(BeastArt.drawables.size, BeastArt.drawables.values.toSet().size)
    }
}
