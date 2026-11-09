package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Upgrade
import java.math.BigDecimal

/**
 * "Comprar todas" del Grimorio: qué mejoras alcanzan con el Conocimiento actual,
 * comprándolas de la más barata a la más cara. Lo usan el motor (para comprar) y
 * la interfaz (para mostrar cuántas son), así ambos cuentan lo mismo.
 */
object GrimoireShopping {

    fun plan(upgrades: List<Upgrade>, funds: BigDecimal): List<Upgrade> {
        var left = funds
        val result = mutableListOf<Upgrade>()
        for (upgrade in upgrades.filter { !it.isBought }.sortedBy { it.cost }) {
            if (upgrade.cost > left) break
            left = left.subtract(upgrade.cost)
            result += upgrade
        }
        return result
    }

    fun affordableCount(upgrades: List<Upgrade>, funds: BigDecimal): Int = plan(upgrades, funds).size
}
