package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.AchievementType
import com.EGomez.idleecosystem.domain.model.Building
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class NextGoalFinderTest {

    private fun knowledge(id: String, target: Long, order: Int, secret: Boolean = false) = Achievement(
        id, id, "", AchievementType.RESOURCE_REACHED, null, BigDecimal(target), BigDecimal("1.01"),
        order = order, requirement = "$target", isSecret = secret
    )

    private fun owned(id: String, building: String, target: Int, order: Int) = Achievement(
        id, id, "", AchievementType.BUILDING_OWNED, building, BigDecimal(target), BigDecimal("1.01"),
        order = order, requirement = "$target $building"
    )

    private fun building(id: String, amount: Int) = Building(
        id = id, name = id, description = "", baseCost = BigDecimal.TEN,
        baseProduction = BigDecimal.ONE, growthFactor = BigDecimal("1.15"), amountOwned = amount
    )

    @Test
    fun `picks the most advanced pending achievement`() {
        val goals = listOf(knowledge("k", 1_000, order = 1), owned("n", "b_notes", 50, order = 2))
        // 100/1000 = 10 % contra 34/50 = 68 %
        val goal = NextGoalFinder.find(goals, BigDecimal(100), listOf(building("b_notes", 34)))!!

        assertEquals("n", goal.achievementId)
        assertEquals(0.68f, goal.progress, 0.001f)
    }

    @Test
    fun `unlocked and secret achievements are skipped`() {
        val done = knowledge("done", 10, order = 1).apply { isUnlocked = true }
        val secret = knowledge("secret", 200, order = 2, secret = true)
        val goal = NextGoalFinder.find(listOf(done, secret, knowledge("k", 1_000, order = 3)), BigDecimal(150), emptyList())

        assertEquals("k", goal!!.achievementId)
    }

    @Test
    fun `ties go to the earliest achievement`() {
        val goals = listOf(owned("b", "b_priest", 10, order = 5), knowledge("a", 100, order = 4))
        assertEquals("a", NextGoalFinder.find(goals, BigDecimal.ZERO, emptyList())!!.achievementId)
    }

    @Test
    fun `no goal when everything is unlocked`() {
        val done = knowledge("done", 10, order = 1).apply { isUnlocked = true }
        assertNull(NextGoalFinder.find(listOf(done), BigDecimal.ZERO, emptyList()))
    }

    @Test
    fun `a new game points to the first achievement`() {
        val goal = NextGoalFinder.find(AchievementManager().availableAchievements, BigDecimal.ZERO, emptyList())
        assertEquals("k_1", goal!!.achievementId)
    }
}
