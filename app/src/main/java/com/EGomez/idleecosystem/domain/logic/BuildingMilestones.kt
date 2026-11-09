package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.AchievementType

/**
 * Los hitos de cada edificio: las cantidades donde pasa algo. Son los escalones del
 * Grimorio (25, 50 y 100, que desbloquean una mejora x2) y los logros de "tener N"
 * de ese edificio (10, 50, 300...), sin contar los secretos.
 *
 * Con ellos el botón "Hito" compra justo lo que falta para el siguiente, y cada
 * tarjeta muestra cuánto queda. Pasado el último hito, cada centena cuenta como uno.
 */
object BuildingMilestones {

    /** Valor de "comprar hasta el siguiente hito" en el selector de cantidad. */
    const val BUY_TO_MILESTONE = -2

    private const val STEP_AFTER_LAST = 100

    fun build(buildingIds: List<String>, achievements: List<Achievement>): Map<String, List<Int>> {
        val ladder = GrimoireLadder.RUNGS.map { it.threshold }
        return buildingIds.associateWith { id ->
            val fromAchievements = achievements
                .filter { it.conditionType == AchievementType.BUILDING_OWNED && it.targetId == id }
                // Los secretos no: la barra los revelaría
                .filter { !it.isSecret }
                .map { it.targetValue.toInt() }
            (ladder + fromAchievements).filter { it > 0 }.distinct().sorted()
        }
    }

    /** El primer hito por encima de lo que ya tienes. */
    fun next(milestones: List<Int>, owned: Int): Int =
        milestones.firstOrNull { it > owned }
            ?: ((owned / STEP_AFTER_LAST) + 1) * STEP_AFTER_LAST

    /** El último hito alcanzado (0 si ninguno): el inicio de la barra de progreso. */
    fun previous(milestones: List<Int>, owned: Int): Int {
        val last = milestones.lastOrNull { it <= owned } ?: return 0
        val pastEnd = milestones.lastOrNull()?.let { owned >= it } ?: true
        return if (pastEnd) maxOf(last, (owned / STEP_AFTER_LAST) * STEP_AFTER_LAST) else last
    }

    /** Cuántas unidades faltan para el siguiente hito (al menos 1). */
    fun amountToNext(milestones: List<Int>, owned: Int): Int = next(milestones, owned) - owned
}
