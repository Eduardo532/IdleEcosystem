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

    @Test
    fun `counters survive a save and the run ones reset on ascension`() {
        val stats = StatisticsManager()
        stats.trackClick()
        stats.count(AchievementStat.RITUALS, BigDecimal(3))
        stats.record(AchievementStat.MAX_CONVERGENCE, BigDecimal("360"))
        stats.record(AchievementStat.MAX_CONVERGENCE, BigDecimal("27"))
        stats.flag(AchievementStat.FEAT_ASCETIC)
        stats.flag(AchievementStat.FEAT_ASCETIC)

        val restored = StatisticsManager()
        restored.loadFromEntity(stats.getEntityForSave())
        assertEquals(0, BigDecimal.ONE.compareTo(restored.counter(StatisticsManager.RUN_CLICKS)))
        assertEquals(0, BigDecimal(3).compareTo(restored.counter(AchievementStat.RITUALS)))
        assertEquals(0, BigDecimal(360).compareTo(restored.counter(AchievementStat.MAX_CONVERGENCE)))
        assertEquals(0, BigDecimal.ONE.compareTo(restored.counter(AchievementStat.FEAT_ASCETIC)))

        restored.resetRunCounters()
        assertEquals(0, BigDecimal.ZERO.compareTo(restored.counter(StatisticsManager.RUN_CLICKS)))
        assertEquals(0, BigDecimal(3).compareTo(restored.counter(AchievementStat.RITUALS)))
    }

    @Test
    fun `broken counter text is ignored`() {
        val decoded = StatisticsManager.decodeCounters("rituals=4;bad;=3;run.clicks=abc;Mal=1;life.none=2")
        assertEquals(setOf("rituals", "life.none"), decoded.keys)
    }
}
