package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LethargyTest {

    @Test
    fun `the lethargy opens at the fifth transcendence`() {
        assertFalse(Lethargy.isUnlocked(4))
        assertTrue(Lethargy.isUnlocked(5))
    }

    @Test
    fun `torpor multiplies sanity logarithmically`() {
        assertEquals(1.0, Lethargy.sanityMultiplier(0), 1e-12)
        // 10 min: x2; 30 min: x3; 70 min: x4
        assertEquals(2.0, Lethargy.sanityMultiplier(600), 1e-9)
        assertEquals(3.0, Lethargy.sanityMultiplier(1800), 1e-9)
        assertEquals(4.0, Lethargy.sanityMultiplier(4200), 1e-9)
        assertTrue(Lethargy.sanityMultiplier(86_400) < 9.0)
    }

    @Test
    fun `every hour of torpor counts one more ascension up to four`() {
        assertEquals(0, Lethargy.extraAscensions(3599))
        assertEquals(1, Lethargy.extraAscensions(3600))
        assertEquals(2, Lethargy.extraAscensions(7200))
        assertEquals(Lethargy.MAX_EXTRA, Lethargy.extraAscensions(100_000))
    }

    @Test
    fun `the time to the next extra ascension counts down`() {
        assertEquals(3600, Lethargy.secondsToNextExtra(0))
        assertEquals(600, Lethargy.secondsToNextExtra(3000))
        assertEquals(3600, Lethargy.secondsToNextExtra(3600))
        assertEquals(0, Lethargy.secondsToNextExtra(4 * 3600))
    }
}
