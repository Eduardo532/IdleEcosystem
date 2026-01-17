package com.EGomez.idleecosystem.core.math

import java.math.BigDecimal

// En el futuro, esto podría reemplazarse por una librería como BreakEternity.kt
// si necesitamos números mayores a 1e308 de forma performante.
typealias BigNumber = BigDecimal

fun BigNumber.toScientificString(): String {
    // Aquí implementaremos la lógica de notación (e.g., 1.52e450) [cite: 160]
    return this.toString()
}