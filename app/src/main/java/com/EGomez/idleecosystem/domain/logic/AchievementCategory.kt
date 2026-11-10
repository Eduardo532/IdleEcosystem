package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Achievement

/**
 * Categorías de la lista de logros (v0.17). Con más de 270 logros la lista larga dejó
 * de servir: el diálogo filtra por categoría. Se deduce del grupo del logro.
 */
enum class AchievementCategory {
    PROGRESS, BUILDINGS, ASCENSION, PANTHEON, ARCANE, SECRET;

    companion object {
        fun of(achievement: Achievement): AchievementCategory {
            if (achievement.isSecret) return SECRET
            val group = achievement.groupId.orEmpty()
            return when {
                group.startsWith("4_build") -> BUILDINGS
                group.startsWith("3_prest") || group.startsWith("6_") -> ASCENSION
                group.startsWith("7_") -> PANTHEON
                group.startsWith("8_") -> ARCANE
                else -> PROGRESS
            }
        }
    }
}
