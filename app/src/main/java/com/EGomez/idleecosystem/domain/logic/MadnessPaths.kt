package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import java.math.BigDecimal

/**
 * Las Sendas del Mapa de Locura: tres nodos al final de las ramas de clic, edificios
 * y tiempo sin conexión. Son excluyentes: mientras una tenga niveles, las otras dos
 * quedan selladas. Para cambiar de Senda hay que abandonarla al Ascender, y se
 * recupera toda la Cordura gastada en ella.
 */
object MadnessPaths {
    const val HAND = "pu_path_hand"
    const val GEARS = "pu_path_gears"
    const val DREAM = "pu_path_dream"

    val IDS = listOf(HAND, GEARS, DREAM)

    fun isPath(id: String): Boolean = id in IDS

    /** La Senda que se está recorriendo, si hay alguna. */
    fun active(upgrades: List<PrestigeUpgrade>): PrestigeUpgrade? =
        upgrades.firstOrNull { isPath(it.id) && it.level > 0 }

    /** Una Senda está sellada cuando otra ya tiene niveles. */
    fun isSealed(node: PrestigeUpgrade, upgrades: List<PrestigeUpgrade>): Boolean {
        if (!isPath(node.id)) return false
        val current = active(upgrades) ?: return false
        return current.id != node.id
    }

    /** Toda la Cordura gastada en un nodo: Σ costo base × multiplicador^i, i < nivel. */
    fun spent(node: PrestigeUpgrade): BigDecimal =
        (0 until node.level).fold(BigDecimal.ZERO) { acc, i ->
            acc.add(node.baseCost.multiply(node.costMultiplier.pow(i)))
        }
}
