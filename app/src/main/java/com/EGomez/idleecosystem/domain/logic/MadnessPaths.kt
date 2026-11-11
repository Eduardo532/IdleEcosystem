package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Constellation
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import java.math.BigDecimal

/**
 * Las Sendas del Mapa de Locura: una en el centro de cada constelación (v0.19.2: siete,
 * antes tres al final de las ramas). Se abren al iluminar toda su constelación y son
 * excluyentes: mientras una tenga niveles, las demás quedan selladas. Para cambiar de
 * Senda hay que abandonarla al Ascender, y se recupera toda la Cordura gastada en ella.
 */
object MadnessPaths {
    const val HAND = "pu_path_hand"
    const val GEARS = "pu_path_gears"
    const val DREAM = "pu_path_dream"
    const val MIND = "pu_path_mind"
    const val VOID = "pu_path_void"
    const val CULT = "pu_path_cult"
    const val ARCANE = "pu_path_arcane"

    val IDS = listOf(HAND, GEARS, DREAM, MIND, VOID, CULT, ARCANE)

    /** La Senda en el centro de una constelación. */
    fun of(constellation: Constellation): String = when (constellation) {
        Constellation.HAND -> HAND
        Constellation.GEARS -> GEARS
        Constellation.DREAM -> DREAM
        Constellation.MIND -> MIND
        Constellation.VOID -> VOID
        Constellation.CULT -> CULT
        Constellation.ARCANE -> ARCANE
    }

    /** Toda la constelación (sin su Senda) tiene al menos un nivel. */
    fun isComplete(constellation: Constellation, upgrades: List<PrestigeUpgrade>): Boolean {
        val stars = upgrades.filter { it.constellation == constellation && !isPath(it.id) }
        return stars.isNotEmpty() && stars.all { it.level > 0 }
    }

    /** Una Senda se puede comprar si su constelación está completa o si ya se recorre. */
    fun isAvailable(path: PrestigeUpgrade, upgrades: List<PrestigeUpgrade>): Boolean {
        if (path.level > 0) return true
        val constellation = path.constellation ?: return false
        return isComplete(constellation, upgrades)
    }

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
