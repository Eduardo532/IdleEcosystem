package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.PrestigeUpgradeEntity
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import java.math.BigDecimal
import java.math.MathContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrestigeShopManager @Inject constructor() {

    // --- GRAN ARBOL DE CONSTELACIONES ---
    val upgrades = mutableListOf(
        // 0. NÚCLEO
        PrestigeUpgrade("pu_core", "El Despertar", "Aumenta la Producción Global.", BigDecimal("1"), BigDecimal("1.5"), BigDecimal("0.10"), UpgradeType.GLOBAL_MULTIPLIER, x = 0.5f, y = 0.85f, parentId = null),

        // RAMA IZQUIERDA: PODER ACTIVO (Clics)
        PrestigeUpgrade("pu_click_1", "Dedos Inquietos", "Multiplica el poder de clic.", BigDecimal("5"), BigDecimal("1.5"), BigDecimal("0.50"), UpgradeType.CLICK_MULTIPLIER, x = 0.3f, y = 0.7f, parentId = "pu_core"),
        PrestigeUpgrade("pu_click_2", "Furia Táctil", "Clics más potentes.", BigDecimal("25"), BigDecimal("1.6"), BigDecimal("1.0"), UpgradeType.CLICK_MULTIPLIER, x = 0.15f, y = 0.55f, parentId = "pu_click_1"),
        PrestigeUpgrade("pu_click_3", "Tormenta Psiónica", "Clics devastadores.", BigDecimal("150"), BigDecimal("1.8"), BigDecimal("2.0"), UpgradeType.CLICK_MULTIPLIER, x = 0.1f, y = 0.4f, parentId = "pu_click_2"),
        PrestigeUpgrade("pu_click_4", "La Mano de Dios", "Poder de clic definitivo.", BigDecimal("1000"), BigDecimal("2.5"), BigDecimal("5.0"), UpgradeType.CLICK_MULTIPLIER, x = 0.05f, y = 0.2f, parentId = "pu_click_3"),

        // RAMA DERECHA: TIEMPO (Edificios / Offline)
        PrestigeUpgrade("pu_idle_1", "Engranajes Mentales", "Multiplica edificios básicos.", BigDecimal("10"), BigDecimal("1.5"), BigDecimal("0.20"), UpgradeType.BUILDING_MULTIPLIER, x = 0.7f, y = 0.7f, parentId = "pu_core"),
        PrestigeUpgrade("pu_idle_2", "Sincronía", "Multiplica TODOS los edificios.", BigDecimal("50"), BigDecimal("1.6"), BigDecimal("0.15"), UpgradeType.BUILDING_MULTIPLIER, x = 0.85f, y = 0.55f, parentId = "pu_idle_1"),
        PrestigeUpgrade("pu_offline_1", "Sueño Lúcido", "Multiplica ganancia Offline.", BigDecimal("200"), BigDecimal("1.8"), BigDecimal("0.50"), UpgradeType.BUILDING_MULTIPLIER, x = 0.9f, y = 0.4f, parentId = "pu_idle_2"),
        PrestigeUpgrade("pu_offline_2", "Eternidad en un Segundo", "Ganancia Offline masiva.", BigDecimal("1500"), BigDecimal("2.5"), BigDecimal("2.0"), UpgradeType.BUILDING_MULTIPLIER, x = 0.95f, y = 0.2f, parentId = "pu_offline_1"),

        // RAMA CENTRAL: MENTE (Global)
        PrestigeUpgrade("pu_mind_1", "Concentración", "Multiplicador Global.", BigDecimal("15"), BigDecimal("1.4"), BigDecimal("0.05"), UpgradeType.GLOBAL_MULTIPLIER, x = 0.5f, y = 0.65f, parentId = "pu_core"),
        PrestigeUpgrade("pu_mind_2", "Lógica Alienígena", "Aumento Global considerable.", BigDecimal("80"), BigDecimal("1.7"), BigDecimal("0.25"), UpgradeType.GLOBAL_MULTIPLIER, x = 0.5f, y = 0.5f, parentId = "pu_mind_1"),
        PrestigeUpgrade("pu_mind_3", "Iluminación Oscura", "Potenciador Global avanzado.", BigDecimal("400"), BigDecimal("2.0"), BigDecimal("0.50"), UpgradeType.GLOBAL_MULTIPLIER, x = 0.5f, y = 0.35f, parentId = "pu_mind_2"),
        PrestigeUpgrade("pu_mind_4", "Omnisciencia", "Multiplicador Global divino.", BigDecimal("2500"), BigDecimal("3.0"), BigDecimal("1.5"), UpgradeType.GLOBAL_MULTIPLIER, x = 0.5f, y = 0.2f, parentId = "pu_mind_3"),

        // RAMA SUR: PROFUNDIDAD (Prestigio)
        PrestigeUpgrade("pu_void_1", "Llamada del Vacío", "Multiplica la Cordura obtenida.", BigDecimal("100"), BigDecimal("2.0"), BigDecimal("0.10"), UpgradeType.BUILDING_MULTIPLIER, x = 0.65f, y = 0.35f, parentId = "pu_mind_2"),
        PrestigeUpgrade("pu_void_2", "Abismo", "Gran bono de Cordura.", BigDecimal("500"), BigDecimal("2.5"), BigDecimal("0.20"), UpgradeType.BUILDING_MULTIPLIER, x = 0.75f, y = 0.25f, parentId = "pu_void_1"),
        PrestigeUpgrade("pu_void_3", "Singularidad", "Multiplicador masivo de Cordura.", BigDecimal("5000"), BigDecimal("4.0"), BigDecimal("0.50"), UpgradeType.BUILDING_MULTIPLIER, x = 0.75f, y = 0.1f, parentId = "pu_void_2"),

        // META
        PrestigeUpgrade("pu_star_final", "Ascensión Cósmica", "El Multiplicador Supremo.", BigDecimal("5000"), BigDecimal("5.0"), BigDecimal("1.00"), UpgradeType.GLOBAL_MULTIPLIER, x = 0.5f, y = 0.05f, parentId = "pu_mind_4")
    )

    // --- CÁLCULO LEGACY ---
    data class LegacyUpgradeInfo(val baseCost: BigDecimal, val scaling: BigDecimal)
    private val legacyDefinitions = mapOf(
        "p_start" to LegacyUpgradeInfo(BigDecimal("1"), BigDecimal("2.0")),
        "p_global" to LegacyUpgradeInfo(BigDecimal("5"), BigDecimal("3.0")),
        "p_prestige" to LegacyUpgradeInfo(BigDecimal("10"), BigDecimal("4.0")),
        "p_click" to LegacyUpgradeInfo(BigDecimal("3"), BigDecimal("2.5")),
        "p_offline" to LegacyUpgradeInfo(BigDecimal("8"), BigDecimal("3.0"))
    )

    fun calculateLegacyRefund(savedEntities: List<PrestigeUpgradeEntity>): BigDecimal {
        var totalRefund = BigDecimal.ZERO
        for (saved in savedEntities) {
            val legacyDef = legacyDefinitions[saved.id]

            // SOLO si está en la lista de antiguas, calculamos reembolso.
            if (legacyDef != null) {
                val r = legacyDef.scaling
                val a = legacyDef.baseCost
                val n = saved.level
                if (n > 0) {
                    val numerator = r.pow(n).subtract(BigDecimal.ONE)
                    val denominator = r.subtract(BigDecimal.ONE)
                    val totalCost = a.multiply(numerator).divide(denominator, MathContext.DECIMAL64)
                    totalRefund = totalRefund.add(totalCost)
                }
            }
        }
        return totalRefund
    }

    fun loadFromSave(saved: List<PrestigeUpgradeEntity>) {
        // 1. Limpieza de memoria (Reset)
        upgrades.forEach { it.level = 0 }

        // 2. Cargar estado guardado
        saved.forEach { s -> upgrades.find { it.id == s.id }?.level = s.level }
    }

    fun getCost(id: String): BigDecimal? = upgrades.find { it.id == id }?.currentCost

    fun tryBuy(id: String, currentSanity: BigDecimal): Boolean {
        val upgrade = upgrades.find { it.id == id } ?: return false
        if (upgrade.parentId != null) {
            val parent = upgrades.find { it.id == upgrade.parentId }
            if (parent == null || parent.level == 0) return false
        }
        if (currentSanity >= upgrade.currentCost) {
            upgrade.level++
            return true
        }
        return false
    }

    // =========================================================
    // LÓGICA MULTIPLICATIVA (POTENCIA REAL)
    // =========================================================

    // 1. GLOBAL
    fun getPermanentGlobalMultiplier(): BigDecimal {
        var multiplier = BigDecimal.ONE
        upgrades.filter { it.type == UpgradeType.GLOBAL_MULTIPLIER }.forEach {
            val nodeBonus = it.effectMultiplier.multiply(BigDecimal(it.level))
            val nodeMult = BigDecimal.ONE.add(nodeBonus)
            multiplier = multiplier.multiply(nodeMult)
        }
        return multiplier
    }

    // 2. CLIC
    fun getClickPrestigeMultiplier(): BigDecimal {
        var multiplier = BigDecimal.ONE
        upgrades.filter { it.type == UpgradeType.CLICK_MULTIPLIER }.forEach {
            val nodeBonus = it.effectMultiplier.multiply(BigDecimal(it.level))
            val nodeMult = BigDecimal.ONE.add(nodeBonus)
            multiplier = multiplier.multiply(nodeMult)
        }
        return multiplier
    }

    // 3. EDIFICIOS
    fun getBuildingTierMultiplier(buildingIndex: Int): BigDecimal {
        var multiplier = BigDecimal.ONE

        // A. Engranajes Mentales (Tier 1-6)
        if (buildingIndex <= 5) {
            val u = upgrades.find { it.id == "pu_idle_1" }
            if (u != null && u.level > 0) {
                val nodeBonus = u.effectMultiplier.multiply(BigDecimal(u.level))
                val nodeMult = BigDecimal.ONE.add(nodeBonus)
                multiplier = multiplier.multiply(nodeMult)
            }
        }

        // B. Sincronía (Todos)
        val sync = upgrades.find { it.id == "pu_idle_2" }
        if (sync != null && sync.level > 0) {
            val nodeBonus = sync.effectMultiplier.multiply(BigDecimal(sync.level))
            val nodeMult = BigDecimal.ONE.add(nodeBonus)
            multiplier = multiplier.multiply(nodeMult)
        }

        return multiplier
    }

    // 4. OFFLINE
    fun getOfflinePrestigeMultiplier(): BigDecimal {
        val u1 = upgrades.find { it.id == "pu_offline_1" }
        val u2 = upgrades.find { it.id == "pu_offline_2" }
        var mult = BigDecimal.ONE

        if (u1 != null && u1.level > 0) {
            mult = mult.multiply(BigDecimal.ONE.add(u1.effectMultiplier.multiply(BigDecimal(u1.level))))
        }
        if (u2 != null && u2.level > 0) {
            mult = mult.multiply(BigDecimal.ONE.add(u2.effectMultiplier.multiply(BigDecimal(u2.level))))
        }
        return mult
    }

    // 5. PRESTIGIO
    fun getPrestigeGainMultiplier(): BigDecimal {
        val u1 = upgrades.find { it.id == "pu_void_1" }
        val u2 = upgrades.find { it.id == "pu_void_2" }
        val u3 = upgrades.find { it.id == "pu_void_3" }
        var mult = BigDecimal.ONE

        if (u1 != null && u1.level > 0) mult = mult.multiply(BigDecimal.ONE.add(u1.effectMultiplier.multiply(BigDecimal(u1.level))))
        if (u2 != null && u2.level > 0) mult = mult.multiply(BigDecimal.ONE.add(u2.effectMultiplier.multiply(BigDecimal(u2.level))))
        if (u3 != null && u3.level > 0) mult = mult.multiply(BigDecimal.ONE.add(u3.effectMultiplier.multiply(BigDecimal(u3.level))))

        return mult
    }

    fun getStartingKnowledgeBonus(): BigDecimal = BigDecimal.ZERO
}