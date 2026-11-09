package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.AchievementType
import java.math.BigDecimal

/**
 * Conjuntos de logros: los logros de "tener N" de un mismo edificio forman su
 * conjunto. Completarlo entero duplica la producción de ese edificio (en la
 * Sinapsis, el clic).
 *
 * Cada logro suelto da poco (x1,01 a x1,10), así que el último de un conjunto vale
 * mucho más que los demás y se convierte en una meta. Los logros no se pierden al
 * Ascender, así que el bono es permanente. Los secretos no forman parte de ningún
 * conjunto: pedirlos obligaría a buscarlos sin pistas.
 */
object AchievementSets {

    val SET_MULTIPLIER: BigDecimal = BigDecimal(2)

    /** Los logros de cada conjunto, por id de edificio. */
    fun byBuilding(achievements: List<Achievement>): Map<String, List<Achievement>> =
        achievements
            .filter { it.conditionType == AchievementType.BUILDING_OWNED && !it.isSecret && it.targetId != null }
            .groupBy { it.targetId!! }

    /** Los edificios con su conjunto completo. */
    fun completed(achievements: List<Achievement>): Set<String> =
        byBuilding(achievements).filterValues { set -> set.all { it.isUnlocked } }.keys

    fun multiplier(buildingId: String, completed: Set<String>): BigDecimal =
        if (buildingId in completed) SET_MULTIPLIER else BigDecimal.ONE
}
