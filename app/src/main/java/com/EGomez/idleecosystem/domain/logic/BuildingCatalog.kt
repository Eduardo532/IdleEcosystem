package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.BuildingType
import java.math.BigDecimal

/**
 * Definición de los edificios del juego. Cada llamada a [create] devuelve copias
 * nuevas: BuildingManager guarda las suyas (que muta durante la partida) y
 * UpgradeManager arma la escalera del Grimorio a partir de los mismos datos, sin
 * duplicarlos.
 */
object BuildingCatalog {

    fun create(): MutableList<Building> = mutableListOf(
        // Tier 1
        Building("b_synapse", "Sinapsis Arcana", "Genera conocimiento básico.", BigDecimal("10"), BigDecimal("1"), BigDecimal("1.15"), tier = 1, type = BuildingType.CLICK, isVisible = true, amountOwned = 0),
        // Tier 2
        Building("b_notes", "Notas Desordenadas", "Garabatos frenéticos.", BigDecimal("15"), BigDecimal("0.5"), BigDecimal("1.15"), tier = 2, isVisible = true),
        // Tier 3
        Building("b_scrolls", "Pergaminos Prohibidos", "Textos antiguos.", BigDecimal("100"), BigDecimal("4"), BigDecimal("1.15"), tier = 3),
        // Tier 4
        Building("b_artifact", "Artefacto Susurrante", "Vibra con energía.", BigDecimal("1100"), BigDecimal("12"), BigDecimal("1.15"), tier = 4),
        // Tier 5
        Building("b_cultist", "Sectario Novato", "Mano de obra barata.", BigDecimal("12000"), BigDecimal("45"), BigDecimal("1.15"), tier = 5),
        // Tier 6
        Building("b_priest", "Sacerdote Demente", "Dirige los rituales.", BigDecimal("130000"), BigDecimal("150"), BigDecimal("1.15"), tier = 6),
        // Tier 7
        Building("b_sanctuary", "Santuario Subterráneo", "Un lugar seguro.", BigDecimal("1400000"), BigDecimal("600"), BigDecimal("1.15"), tier = 7),
        // Tier 8
        Building("b_altar", "Altar de Hueso", "Sacrificios necesarios.", BigDecimal("20000000"), BigDecimal("2500"), BigDecimal("1.15"), tier = 8),
        // Tier 9
        Building("b_monolith", "Monolito de Obsidiana", "Canaliza el vacío.", BigDecimal("330000000"), BigDecimal("12000"), BigDecimal("1.15"), tier = 9),
        // Tier 10
        Building("b_temple", "Templo de R'lyeh", "La ciudad sumergida.", BigDecimal("5100000000"), BigDecimal("65000"), BigDecimal("1.15"), tier = 10),
        // Tier 11
        Building("b_gate", "Portal Dimensional", "Puerta a otros mundos.", BigDecimal("75000000000"), BigDecimal("400000"), BigDecimal("1.15"), tier = 11),
        // Tier 12
        Building("b_shoggoth", "Criadero de Shoggoths", "Masa biológica útil.", BigDecimal("1000000000000"), BigDecimal("2500000"), BigDecimal("1.15"), tier = 12),
        // Tier 13
        Building("b_dream", "Fragmento de Sueño", "Materializado.", BigDecimal("14000000000000"), BigDecimal("15000000"), BigDecimal("1.15"), tier = 13),
        // Tier 14
        Building("b_star", "Alineación Estelar", "El cosmos se ordena.", BigDecimal("170000000000000"), BigDecimal("100000000"), BigDecimal("1.15"), tier = 14),
        // Tier 15
        Building("b_oldone", "Avatar Primigenio", "Un dios camina.", BigDecimal("2100000000000000"), BigDecimal("850000000"), BigDecimal("1.15"), tier = 15),
        // Tier 16
        Building("b_void", "Trono del Vacío", "El fin de todo.", BigDecimal("26000000000000000"), BigDecimal("10000000000"), BigDecimal("1.15"), tier = 16)
    )
}
