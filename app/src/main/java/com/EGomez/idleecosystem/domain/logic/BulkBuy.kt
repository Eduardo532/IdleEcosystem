package com.EGomez.idleecosystem.domain.logic

import java.math.BigDecimal

/**
 * Comprar varios niveles a la vez (v0.20.5): en el Mapa de Locura y en las Estrellas.
 * Con una cantidad fija (x10, x100) se compra el lote entero o nada, como con los
 * edificios; si el nodo tiene tope, el lote se recorta a lo que falta. [MAX] compra
 * todos los niveles que alcancen.
 */
object BulkBuy {
    const val MAX = -1

    /** Las cantidades que se ofrecen, en orden. */
    val AMOUNTS = listOf(1, 10, 100, MAX)

    /** Tope de niveles de una sola compra (para que MAX nunca se cuelgue). */
    const val LIMIT = 1000

    /** Cuántos niveles se compran y cuánto cuestan en total. */
    data class Plan(val levels: Int, val cost: BigDecimal) {
        val isEmpty: Boolean get() = levels == 0
    }

    /**
     * El plan de comprar [amount] niveles desde [level] con [budget], si el nodo llega a
     * [maxLevel] (null = sin tope). [costAt] da el costo del nivel siguiente a uno dado.
     * Con cantidad fija, si no alcanza, el plan dice cuánto costaría el lote (levels = 0).
     */
    fun plan(level: Int, maxLevel: Int?, budget: BigDecimal, amount: Int, costAt: (Int) -> BigDecimal): Plan {
        val room = (maxLevel?.minus(level) ?: LIMIT).coerceIn(0, LIMIT)
        if (room == 0) return Plan(0, BigDecimal.ZERO)
        if (amount == MAX) {
            var total = BigDecimal.ZERO
            var bought = 0
            while (bought < room) {
                val next = total.add(costAt(level + bought))
                if (next > budget) break
                total = next
                bought++
            }
            // Si no alcanza ni uno, se muestra el costo del primero
            return if (bought == 0) Plan(0, costAt(level)) else Plan(bought, total)
        }
        val wanted = amount.coerceIn(1, room)
        var total = BigDecimal.ZERO
        for (i in 0 until wanted) total = total.add(costAt(level + i))
        return Plan(if (total <= budget) wanted else 0, total)
    }

    /** "x10" o "MAX". */
    fun label(amount: Int): String = if (amount == MAX) "MAX" else "x$amount"
}
