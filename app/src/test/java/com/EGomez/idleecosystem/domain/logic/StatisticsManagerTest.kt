package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class StatisticsManagerTest {

    @Test
    fun `snapshot copies the current values`() {
        val stats = StatisticsManager()
        stats.trackPlayTime(30)
        stats.trackOfflineTime(10)
        stats.trackClick()
        stats.trackKnowledgeGenerated(BigDecimal("5"), isManual = true)

        val snapshot = stats.snapshot()

        assertEquals(40, snapshot.totalPlayTimeSeconds)
        assertEquals(30, snapshot.activePlayTimeSeconds)
        assertEquals(1, snapshot.totalClicks)
        assertEquals(0, BigDecimal("5").compareTo(snapshot.manualKnowledgeGenerated))
    }

    @Test
    fun `snapshot does not change when the manager keeps counting`() {
        val stats = StatisticsManager()
        stats.trackClick()
        val snapshot = stats.snapshot()

        stats.trackClick()
        stats.trackPlayTime(5)

        assertEquals(1, snapshot.totalClicks)
        assertEquals(0, snapshot.totalPlayTimeSeconds)
        assertEquals(2, stats.snapshot().totalClicks)
    }
}
