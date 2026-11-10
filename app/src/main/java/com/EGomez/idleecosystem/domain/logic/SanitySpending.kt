package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import java.math.BigDecimal
import java.math.MathContext

/**
 * Lo que de verdad cambia al gastar Cordura en el Mapa de Locura. Cada punto de
 * Cordura Residual da +10 % a la producción mientras se guarda, así que gastarla en
 * un nodo también baja ese bono. Mostrar las dos caras convierte la compra en una
 * decisión con números (como los ángeles de AdVenture Capitalist).
 */
object SanitySpending {

    /** El mismo +10 % por punto que aplica [PrestigeManager.getPrestigeMultiplier]. */
    val BONUS_PER_POINT: BigDecimal = BigDecimal("0.10")

    data class Preview(
        /** Multiplicador del nodo antes y después de subirlo un nivel. */
        val nodeBefore: BigDecimal,
        val nodeAfter: BigDecimal,
        /** Bono de la Cordura Residual antes y después de pagar. */
        val sanityBonusBefore: BigDecimal,
        val sanityBonusAfter: BigDecimal,
        /**
         * Cambio neto en la producción global. Solo existe para los nodos globales:
         * los de clic, edificios o Cordura no se pueden comparar en la misma unidad.
         */
        val netProduction: BigDecimal?
    )

    fun preview(node: PrestigeUpgrade, sanity: BigDecimal): Preview {
        val nodeBefore = BigDecimal.ONE.add(node.effectMultiplier.multiply(BigDecimal(node.level)))
        val nodeAfter = BigDecimal.ONE.add(node.effectMultiplier.multiply(BigDecimal(node.level + 1)))
        val sanityAfter = sanity.subtract(node.currentCost).max(BigDecimal.ZERO)
        val bonusBefore = BigDecimal.ONE.add(sanity.multiply(BONUS_PER_POINT))
        val bonusAfter = BigDecimal.ONE.add(sanityAfter.multiply(BONUS_PER_POINT))
        val net = if (node.type == UpgradeType.GLOBAL_MULTIPLIER) {
            nodeAfter.divide(nodeBefore, MathContext.DECIMAL64)
                .multiply(bonusAfter.divide(bonusBefore, MathContext.DECIMAL64), MathContext.DECIMAL64)
        } else null
        return Preview(nodeBefore, nodeAfter, bonusBefore, bonusAfter, net)
    }
}
