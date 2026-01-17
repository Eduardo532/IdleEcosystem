package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.UpgradeEntity
import com.EGomez.idleecosystem.domain.model.Upgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpgradeManager @Inject constructor() {

    val availableUpgrades = mutableListOf(
        // --- TIER 1 & 2 ---
        Upgrade("u_click_1", "Pluma de Cuervo", "Escribir más rápido invoca la locura antes.", BigDecimal("500"), UpgradeType.CLICK_MULTIPLIER, 3.0),
        Upgrade("u_notes_1", "Tinta de Calamar", "Las notas brillan en la oscuridad.", BigDecimal("250"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_notes"),
        Upgrade("u_notes_2", "Papel de Piel", "Conserva mejor las ideas blasfemas.", BigDecimal("5000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_notes"),
        Upgrade("u_scrolls_1", "Traducción Errónea", "Los errores revelan verdades ocultas.", BigDecimal("2500"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_scrolls"),
        Upgrade("u_cultist_1", "Dagas Ceremoniales", "Sacrificios más eficientes.", BigDecimal("50000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_cultist"),
        Upgrade("u_cultist_2", "Túnicas de Seda", "La comodidad mejora la devoción.", BigDecimal("500000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_cultist"),
        Upgrade("u_artifact_1", "Pedestal de Obsidiana", "Estabiliza las vibraciones.", BigDecimal("15000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_artifact"),
        Upgrade("u_priest_1", "Máscara de Oro", "Oculta la humanidad del sacerdote.", BigDecimal("1500000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_priest"),
        Upgrade("u_sanctuary_1", "Ecos Amplificados", "Los cánticos resuenan eternamente.", BigDecimal("25000000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_sanctuary"),

        // --- TIER 3 ---
        Upgrade("u_global_1", "Alineación Menor", "Los astros favorecen tu investigación.", BigDecimal("10000000"), UpgradeType.GLOBAL_MULTIPLIER, 1.5),
        Upgrade("u_altar_1", "Sangre Fresca", "El altar siempre tiene sed.", BigDecimal("100000000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_altar"),
        Upgrade("u_monolith_1", "Runas Brillantes", "Atrae energía de las líneas ley.", BigDecimal("5000000000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_monolith"),
        Upgrade("u_temple_1", "Geometría Imposible", "Más espacio adentro que afuera.", BigDecimal("75000000000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_temple"),

        // --- TIER 4 ---
        Upgrade("u_click_2", "Escritura Automática", "Tu mano se mueve sola.", BigDecimal("1000000000"), UpgradeType.CLICK_MULTIPLIER, 5.0),
        Upgrade("u_gate_1", "Estabilizador Dimensional", "El portal permanece abierto más tiempo.", BigDecimal("1000000000000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_gate"),
        Upgrade("u_shoggoth_1", "Control Mental", "Obedecen sin cuestionar... por ahora.", BigDecimal("20000000000000"), UpgradeType.BUILDING_MULTIPLIER, 2.0, "b_shoggoth"),

        // --- TIER 5 ---
        Upgrade("u_dream_1", "Onironáutica", "Navegas el mundo de los sueños a voluntad.", BigDecimal("300000000000000"), UpgradeType.BUILDING_MULTIPLIER, 3.0, "b_dream"),
        Upgrade("u_star_1", "Sizigia", "Los planetas entran en conjunción perfecta.", BigDecimal("5000000000000000"), UpgradeType.BUILDING_MULTIPLIER, 3.0, "b_star"),
        Upgrade("u_oldone_1", "Liturgia del Fin", "Los antiguos despiertan.", BigDecimal("100000000000000000"), UpgradeType.BUILDING_MULTIPLIER, 3.0, "b_oldone"),

        // --- FINAL ---
        Upgrade("u_global_2", "Sincronicidad Cósmica", "Todo está conectado.", BigDecimal("500000000000000"), UpgradeType.GLOBAL_MULTIPLIER, 2.0),
        Upgrade("u_void_1", "La Nada Eterna", "Azathoth se agita en sueños.", BigDecimal("1000000000000000000"), UpgradeType.BUILDING_MULTIPLIER, 3.0, "b_void")
    )

    fun loadFromSave(savedUpgrades: List<UpgradeEntity>) {
        // 1. Limpieza de memoria (Reset)
        availableUpgrades.forEach { it.isBought = false }

        // 2. Cargar estado guardado
        savedUpgrades.forEach { saved ->
            val upgrade = availableUpgrades.find { it.id == saved.upgradeId }
            if (upgrade != null) {
                upgrade.isBought = saved.isBought
            }
        }
    }

    fun tryBuyUpgrade(upgradeId: String, currentFunds: BigDecimal): Boolean {
        val upgrade = availableUpgrades.find { it.id == upgradeId } ?: return false
        if (!upgrade.isBought && currentFunds >= upgrade.cost) {
            upgrade.isBought = true
            return true
        }
        return false
    }

    fun getRawClickMultiplier(): BigDecimal {
        var multiplier = 1.0
        availableUpgrades.filter { it.isBought && it.type == UpgradeType.CLICK_MULTIPLIER }
            .forEach { multiplier *= it.multiplier }
        return BigDecimal(multiplier)
    }

    fun getGlobalMultiplier(): BigDecimal {
        var multiplier = 1.0
        availableUpgrades.filter { it.isBought && it.type == UpgradeType.GLOBAL_MULTIPLIER }
            .forEach { multiplier *= it.multiplier }
        return BigDecimal(multiplier)
    }

    fun getSpecificBuildingMultiplier(buildingId: String): BigDecimal {
        var multiplier = 1.0
        availableUpgrades.filter { it.isBought && it.type == UpgradeType.BUILDING_MULTIPLIER && it.targetBuildingId == buildingId }
            .forEach { multiplier *= it.multiplier }
        return BigDecimal(multiplier)
    }

    fun resetForPrestige() {
        availableUpgrades.forEach { it.isBought = false }
    }
}