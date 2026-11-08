package com.EGomez.idleecosystem.data.preferences

import com.EGomez.idleecosystem.domain.logic.RitualSnapshot
import com.EGomez.idleecosystem.domain.model.RitualState
import org.junit.Assert.assertEquals
import org.junit.Test

class GamePreferencesTest {

    @Test
    fun `rituals are encoded and decoded without loss`() {
        val snapshots = listOf(
            RitualSnapshot("rit_a", RitualState.ACTIVE, 12),
            RitualSnapshot("rit_b", RitualState.COOLDOWN, 300),
            RitualSnapshot("rit_c", RitualState.READY, 0)
        )
        val decoded = GamePreferences.decodeRituals(GamePreferences.encodeRituals(snapshots))
        assertEquals(snapshots, decoded)
    }

    @Test
    fun `corrupt entries are ignored`() {
        val decoded = GamePreferences.decodeRituals("rit_a:ACTIVE:10;garbage;rit_b:NOPE:5;rit_c:READY:x")
        assertEquals(listOf(RitualSnapshot("rit_a", RitualState.ACTIVE, 10)), decoded)
    }
}
