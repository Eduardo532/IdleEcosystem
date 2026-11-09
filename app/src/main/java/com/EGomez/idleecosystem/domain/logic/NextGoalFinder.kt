package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.AchievementType
import com.EGomez.idleecosystem.domain.model.Building
import java.math.BigDecimal
import java.math.MathContext

/** La meta más cercana que se muestra bajo el Conocimiento. */
data class NextGoal(
    val achievementId: String,
    val name: String,
    val requirement: String,
    /** Entre 0 y 1. */
    val progress: Float
)

/**
 * Elige el logro pendiente más avanzado, para que el jugador siempre tenga una meta
 * a la vista sin abrir la lista de logros. Los secretos no cuentan: mostrarlos los
 * revelaría.
 *
 * El progreso se mide igual que el desbloqueo en [AchievementManager]: el saldo de
 * Conocimiento o la cantidad de edificios. A igual progreso gana el que va antes en
 * la lista.
 */
object NextGoalFinder {

    fun find(achievements: List<Achievement>, knowledge: BigDecimal, buildings: List<Building>): NextGoal? {
        val owned = buildings.associate { it.id to it.amountOwned }
        return achievements
            .asSequence()
            .filter { !it.isUnlocked && !it.isSecret }
            .map { it to progressOf(it, knowledge, owned) }
            .sortedWith(compareByDescending<Pair<Achievement, Float>> { it.second }.thenBy { it.first.order })
            .firstOrNull()
            ?.let { (achievement, progress) ->
                NextGoal(achievement.id, achievement.name, achievement.requirement, progress)
            }
    }

    private fun progressOf(achievement: Achievement, knowledge: BigDecimal, owned: Map<String, Int>): Float {
        val current = when (achievement.conditionType) {
            AchievementType.RESOURCE_REACHED -> knowledge
            AchievementType.BUILDING_OWNED -> BigDecimal(owned[achievement.targetId] ?: 0)
        }
        if (achievement.targetValue.signum() <= 0) return 1f
        val ratio = current.divide(achievement.targetValue, MathContext.DECIMAL64)
        return ratio.toFloat().coerceIn(0f, 1f)
    }
}
