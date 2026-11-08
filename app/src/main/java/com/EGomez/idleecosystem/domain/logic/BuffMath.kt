package com.EGomez.idleecosystem.domain.logic

import java.math.BigDecimal

/**
 * Combinación de potenciadores temporales (runas, rituales y eventos).
 *
 * Los potenciadores SE SUMAN en lugar de multiplicarse: cada uno aporta su bono
 * (multiplicador - 1) sobre una base de x1. Antes se multiplicaban entre sí y dos
 * runas juntas (Cthulhu x777 y Marea x500) daban x388.500; ahora dan x1.276.
 *
 * Los multiplicadores permanentes (mejoras, Cordura, logros, constelaciones) se
 * siguen multiplicando: son la progresión del juego.
 */
object BuffMath {

    fun additive(multipliers: List<Double>): BigDecimal {
        var total = BigDecimal.ONE
        multipliers.forEach { m ->
            if (m > 1.0) total = total.add(BigDecimal.valueOf(m).subtract(BigDecimal.ONE))
        }
        return total
    }
}
