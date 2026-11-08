package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.AchievementEntity
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.AchievementType
import com.EGomez.idleecosystem.domain.model.Building
import java.math.BigDecimal
import java.math.MathContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementManager @Inject constructor() {

    // ============================================================================================
    // LISTA MAESTRA DE LOGROS (100 Estándar + 13 Secretos = 113 Total)
    // ============================================================================================
    val availableAchievements = mutableListOf<Achievement>(

        // ----------------------------------------------------------------------------------------
        // GRUPO: CONOCIMIENTO (14 Logros) - Se mantiene lineal
        // ----------------------------------------------------------------------------------------
        Achievement("k_1", "El Primer Susurro", "Has escuchado algo.", AchievementType.RESOURCE_REACHED, null, BigDecimal("100"), BigDecimal("1.01"), groupId = "1_know", order = 1, requirement = "100 Conocimiento"),
        Achievement("k_2", "Inquietud", "Las sombras se mueven.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000"), BigDecimal("1.01"), groupId = "1_know", order = 2, requirement = "1,000 Conocimiento"),
        Achievement("k_3", "Insomnio", "No puedes dejar de pensar.", AchievementType.RESOURCE_REACHED, null, BigDecimal("10000"), BigDecimal("1.01"), groupId = "1_know", order = 3, requirement = "10k Conocimiento"),
        Achievement("k_4", "Verdad Incómoda", "La realidad es frágil.", AchievementType.RESOURCE_REACHED, null, BigDecimal("50000"), BigDecimal("1.01"), groupId = "1_know", order = 4, requirement = "50k Conocimiento"),
        Achievement("k_5", "Ojos Abiertos", "Ves colores nuevos.", AchievementType.RESOURCE_REACHED, null, BigDecimal("250000"), BigDecimal("1.02"), groupId = "1_know", order = 5, requirement = "250k Conocimiento"),
        Achievement("k_6", "Mente Fracturada", "El primer millón duele.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000"), BigDecimal("1.02"), groupId = "1_know", order = 6, requirement = "1M Conocimiento"),
        Achievement("k_7", "Visión Remota", "Ves a través de las paredes.", AchievementType.RESOURCE_REACHED, null, BigDecimal("50000000"), BigDecimal("1.02"), groupId = "1_know", order = 7, requirement = "50M Conocimiento"),
        Achievement("k_8", "Vidente del Vacío", "El abismo te saluda.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000"), BigDecimal("1.03"), groupId = "1_know", order = 8, requirement = "1B Conocimiento"),
        Achievement("k_9", "Omnisciencia Menor", "Sabes lo que pasará.", AchievementType.RESOURCE_REACHED, null, BigDecimal("100000000000"), BigDecimal("1.03"), groupId = "1_know", order = 9, requirement = "100B Conocimiento"),
        Achievement("k_10", "Profeta del Fin", "Anuncias la llegada.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000000"), BigDecimal("1.04"), groupId = "1_know", order = 10, requirement = "1T Conocimiento"),
        Achievement("k_11", "Tejedor de Realidad", "La materia obedece.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000000000"), BigDecimal("1.04"), groupId = "1_know", order = 11, requirement = "1Qa Conocimiento"),
        Achievement("k_12", "Ascensión Divina", "Ya no eres humano.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000000000000"), BigDecimal("1.05"), groupId = "1_know", order = 12, requirement = "1Qi Conocimiento"),
        Achievement("k_13", "Uno con el Todo", "Estás en todas partes.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000000000000000"), BigDecimal("1.05"), groupId = "1_know", order = 13, requirement = "1Sx Conocimiento"),
        Achievement("k_14", "Game Over", "Has completado el grimorio.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000000000000000000000000"), BigDecimal("1.10"), groupId = "1_know", order = 14, requirement = "1 Nonillón Conocimiento"),

        // ----------------------------------------------------------------------------------------
        // GRUPO: CLICKS (3 Logros)
        // ----------------------------------------------------------------------------------------
        Achievement("c_1", "Dedo Veloz", "El viaje comienza.", AchievementType.BUILDING_OWNED, "b_synapse", BigDecimal("1"), BigDecimal("1.01"), groupId = "2_click", order = 15, requirement = "Ten 1 Sinapsis"),
        Achievement("c_2", "Martillo Mental", "Golpeas la realidad.", AchievementType.BUILDING_OWNED, "b_synapse", BigDecimal("100"), BigDecimal("1.02"), groupId = "2_click", order = 16, requirement = "Ten 100 Sinapsis"),
        Achievement("c_3", "Rompe Pantallas", "Tus dedos arden.", AchievementType.BUILDING_OWNED, "b_synapse", BigDecimal("500"), BigDecimal("1.03"), groupId = "2_click", order = 17, requirement = "Ten 500 Sinapsis"),

        // ----------------------------------------------------------------------------------------
        // GRUPO: PRESTIGIO (3 Logros)
        // ----------------------------------------------------------------------------------------
        Achievement("p_1", "Renacido", "La muerte es solo el principio.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000"), BigDecimal("1.05"), groupId = "3_prest", order = 18, requirement = "Alcanza 1M (Resetea para ganar más)"),
        Achievement("p_2", "Viajero del Vacío", "El ciclo se repite.", AchievementType.BUILDING_OWNED, "b_cultist", BigDecimal("1"), BigDecimal("1.05"), groupId = "3_prest", order = 19, requirement = "Compra 1 Sectario (Post-inicio)"),
        Achievement("p_3", "Eterno Retorno", "Ya no recuerdas tu origen.", AchievementType.BUILDING_OWNED, "b_void", BigDecimal("1"), BigDecimal("1.10"), groupId = "3_prest", order = 20, requirement = "Alcanza el Trono del Vacío"),

        // ----------------------------------------------------------------------------------------
        // GRUPO: EDIFICIOS (16 Edificios * 5 Tiers = 80 Logros)
        // CAMBIO IMPORTANTE: Ahora cada edificio tiene su propio groupId
        // ----------------------------------------------------------------------------------------

        // 1. SINAPSIS
        Achievement("b_syn_1", "Sinapsis I", "Inicio.", AchievementType.BUILDING_OWNED, "b_synapse", BigDecimal("25"), BigDecimal("1.01"), groupId = "4_build_syn", order = 21, requirement = "25 Sinapsis"),
        Achievement("b_syn_2", "Sinapsis II", "Red neuronal.", AchievementType.BUILDING_OWNED, "b_synapse", BigDecimal("50"), BigDecimal("1.01"), groupId = "4_build_syn", order = 22, requirement = "50 Sinapsis"),
        Achievement("b_syn_3", "Sinapsis III", "Cerebro galáctico.", AchievementType.BUILDING_OWNED, "b_synapse", BigDecimal("150"), BigDecimal("1.02"), groupId = "4_build_syn", order = 23, requirement = "150 Sinapsis"),
        Achievement("b_syn_4", "Sinapsis IV", "Omnipresencia.", AchievementType.BUILDING_OWNED, "b_synapse", BigDecimal("300"), BigDecimal("1.02"), groupId = "4_build_syn", order = 24, requirement = "300 Sinapsis"),
        Achievement("b_syn_5", "Sinapsis V", "Uno con el todo.", AchievementType.BUILDING_OWNED, "b_synapse", BigDecimal("600"), BigDecimal("1.03"), groupId = "4_build_syn", order = 25, requirement = "600 Sinapsis"),

        // 2. NOTAS
        Achievement("b_not_1", "Notas I", "Borradores.", AchievementType.BUILDING_OWNED, "b_notes", BigDecimal("10"), BigDecimal("1.01"), groupId = "4_build_not", order = 26, requirement = "10 Notas"),
        Achievement("b_not_2", "Notas II", "Manuscritos.", AchievementType.BUILDING_OWNED, "b_notes", BigDecimal("50"), BigDecimal("1.01"), groupId = "4_build_not", order = 27, requirement = "50 Notas"),
        Achievement("b_not_3", "Notas III", "Bibliotecas.", AchievementType.BUILDING_OWNED, "b_notes", BigDecimal("100"), BigDecimal("1.02"), groupId = "4_build_not", order = 28, requirement = "100 Notas"),
        Achievement("b_not_4", "Notas IV", "Archivos Akáshicos.", AchievementType.BUILDING_OWNED, "b_notes", BigDecimal("300"), BigDecimal("1.02"), groupId = "4_build_not", order = 29, requirement = "300 Notas"),
        Achievement("b_not_5", "Notas V", "La Verdad Escrita.", AchievementType.BUILDING_OWNED, "b_notes", BigDecimal("600"), BigDecimal("1.03"), groupId = "4_build_not", order = 30, requirement = "600 Notas"),

        // 3. PERGAMINOS
        Achievement("b_scr_1", "Pergaminos I", "Rollos viejos.", AchievementType.BUILDING_OWNED, "b_scrolls", BigDecimal("10"), BigDecimal("1.01"), groupId = "4_build_scr", order = 31, requirement = "10 Pergaminos"),
        Achievement("b_scr_2", "Pergaminos II", "Piel humana.", AchievementType.BUILDING_OWNED, "b_scrolls", BigDecimal("50"), BigDecimal("1.02"), groupId = "4_build_scr", order = 32, requirement = "50 Pergaminos"),
        Achievement("b_scr_3", "Pergaminos III", "Lenguas muertas.", AchievementType.BUILDING_OWNED, "b_scrolls", BigDecimal("100"), BigDecimal("1.02"), groupId = "4_build_scr", order = 33, requirement = "100 Pergaminos"),
        Achievement("b_scr_4", "Pergaminos IV", "Palabras de poder.", AchievementType.BUILDING_OWNED, "b_scrolls", BigDecimal("250"), BigDecimal("1.03"), groupId = "4_build_scr", order = 34, requirement = "250 Pergaminos"),
        Achievement("b_scr_5", "Pergaminos V", "El Necronomicón.", AchievementType.BUILDING_OWNED, "b_scrolls", BigDecimal("500"), BigDecimal("1.04"), groupId = "4_build_scr", order = 35, requirement = "500 Pergaminos"),

        // 4. ARTEFACTO
        Achievement("b_art_1", "Artefactos I", "Curiosidades.", AchievementType.BUILDING_OWNED, "b_artifact", BigDecimal("10"), BigDecimal("1.02"), groupId = "4_build_art", order = 36, requirement = "10 Artefactos"),
        Achievement("b_art_2", "Artefactos II", "Reliquias.", AchievementType.BUILDING_OWNED, "b_artifact", BigDecimal("50"), BigDecimal("1.02"), groupId = "4_build_art", order = 37, requirement = "50 Artefactos"),
        Achievement("b_art_3", "Artefactos III", "Objetos de Poder.", AchievementType.BUILDING_OWNED, "b_artifact", BigDecimal("100"), BigDecimal("1.03"), groupId = "4_build_art", order = 38, requirement = "100 Artefactos"),
        Achievement("b_art_4", "Artefactos IV", "Tecnología Antigua.", AchievementType.BUILDING_OWNED, "b_artifact", BigDecimal("200"), BigDecimal("1.03"), groupId = "4_build_art", order = 39, requirement = "200 Artefactos"),
        Achievement("b_art_5", "Artefactos V", "Singularidades.", AchievementType.BUILDING_OWNED, "b_artifact", BigDecimal("400"), BigDecimal("1.04"), groupId = "4_build_art", order = 40, requirement = "400 Artefactos"),

        // 5. SECTARIO
        Achievement("b_cul_1", "Sectarios I", "Iniciados.", AchievementType.BUILDING_OWNED, "b_cultist", BigDecimal("10"), BigDecimal("1.02"), groupId = "4_build_cul", order = 41, requirement = "10 Sectarios"),
        Achievement("b_cul_2", "Sectarios II", "Acólitos.", AchievementType.BUILDING_OWNED, "b_cultist", BigDecimal("50"), BigDecimal("1.02"), groupId = "4_build_cul", order = 42, requirement = "50 Sectarios"),
        Achievement("b_cul_3", "Sectarios III", "Hermanos.", AchievementType.BUILDING_OWNED, "b_cultist", BigDecimal("100"), BigDecimal("1.03"), groupId = "4_build_cul", order = 43, requirement = "100 Sectarios"),
        Achievement("b_cul_4", "Sectarios IV", "Maestros.", AchievementType.BUILDING_OWNED, "b_cultist", BigDecimal("300"), BigDecimal("1.03"), groupId = "4_build_cul", order = 44, requirement = "300 Sectarios"),
        Achievement("b_cul_5", "Sectarios V", "La Orden.", AchievementType.BUILDING_OWNED, "b_cultist", BigDecimal("600"), BigDecimal("1.05"), groupId = "4_build_cul", order = 45, requirement = "600 Sectarios"),

        // 6. SACERDOTE
        Achievement("b_pri_1", "Sacerdotes I", "Predicadores.", AchievementType.BUILDING_OWNED, "b_priest", BigDecimal("10"), BigDecimal("1.02"), groupId = "4_build_pri", order = 46, requirement = "10 Sacerdotes"),
        Achievement("b_pri_2", "Sacerdotes II", "Obispos Negros.", AchievementType.BUILDING_OWNED, "b_priest", BigDecimal("50"), BigDecimal("1.03"), groupId = "4_build_pri", order = 47, requirement = "50 Sacerdotes"),
        Achievement("b_pri_3", "Sacerdotes III", "Cardenales.", AchievementType.BUILDING_OWNED, "b_priest", BigDecimal("100"), BigDecimal("1.03"), groupId = "4_build_pri", order = 48, requirement = "100 Sacerdotes"),
        Achievement("b_pri_4", "Sacerdotes IV", "Pontífices.", AchievementType.BUILDING_OWNED, "b_priest", BigDecimal("200"), BigDecimal("1.04"), groupId = "4_build_pri", order = 49, requirement = "200 Sacerdotes"),
        Achievement("b_pri_5", "Sacerdotes V", "Mesías Oscuros.", AchievementType.BUILDING_OWNED, "b_priest", BigDecimal("400"), BigDecimal("1.05"), groupId = "4_build_pri", order = 50, requirement = "400 Sacerdotes"),

        // 7. SANTUARIO
        Achievement("b_san_1", "Santuarios I", "Cuevas.", AchievementType.BUILDING_OWNED, "b_sanctuary", BigDecimal("5"), BigDecimal("1.02"), groupId = "4_build_san", order = 51, requirement = "5 Santuarios"),
        Achievement("b_san_2", "Santuarios II", "Catacumbas.", AchievementType.BUILDING_OWNED, "b_sanctuary", BigDecimal("25"), BigDecimal("1.03"), groupId = "4_build_san", order = 52, requirement = "25 Santuarios"),
        Achievement("b_san_3", "Santuarios III", "Templos Ocultos.", AchievementType.BUILDING_OWNED, "b_sanctuary", BigDecimal("50"), BigDecimal("1.03"), groupId = "4_build_san", order = 53, requirement = "50 Santuarios"),
        Achievement("b_san_4", "Santuarios IV", "Ciudad Subterránea.", AchievementType.BUILDING_OWNED, "b_sanctuary", BigDecimal("100"), BigDecimal("1.04"), groupId = "4_build_san", order = 54, requirement = "100 Santuarios"),
        Achievement("b_san_5", "Santuarios V", "Agartha.", AchievementType.BUILDING_OWNED, "b_sanctuary", BigDecimal("200"), BigDecimal("1.05"), groupId = "4_build_san", order = 55, requirement = "200 Santuarios"),

        // 8. ALTAR
        Achievement("b_alt_1", "Altares I", "Sangre.", AchievementType.BUILDING_OWNED, "b_altar", BigDecimal("5"), BigDecimal("1.02"), groupId = "4_build_alt", order = 56, requirement = "5 Altares"),
        Achievement("b_alt_2", "Altares II", "Huesos.", AchievementType.BUILDING_OWNED, "b_altar", BigDecimal("25"), BigDecimal("1.03"), groupId = "4_build_alt", order = 57, requirement = "25 Altares"),
        Achievement("b_alt_3", "Altares III", "Carne.", AchievementType.BUILDING_OWNED, "b_altar", BigDecimal("50"), BigDecimal("1.04"), groupId = "4_build_alt", order = 58, requirement = "50 Altares"),
        Achievement("b_alt_4", "Altares IV", "Almas.", AchievementType.BUILDING_OWNED, "b_altar", BigDecimal("100"), BigDecimal("1.05"), groupId = "4_build_alt", order = 59, requirement = "100 Altares"),
        Achievement("b_alt_5", "Altares V", "Divinidad.", AchievementType.BUILDING_OWNED, "b_altar", BigDecimal("200"), BigDecimal("1.06"), groupId = "4_build_alt", order = 60, requirement = "200 Altares"),

        // 9. MONOLITO
        Achievement("b_mon_1", "Monolitos I", "Piedras.", AchievementType.BUILDING_OWNED, "b_monolith", BigDecimal("5"), BigDecimal("1.02"), groupId = "4_build_mon", order = 61, requirement = "5 Monolitos"),
        Achievement("b_mon_2", "Monolitos II", "Runas.", AchievementType.BUILDING_OWNED, "b_monolith", BigDecimal("20"), BigDecimal("1.03"), groupId = "4_build_mon", order = 62, requirement = "20 Monolitos"),
        Achievement("b_mon_3", "Monolitos III", "Vibración.", AchievementType.BUILDING_OWNED, "b_monolith", BigDecimal("50"), BigDecimal("1.04"), groupId = "4_build_mon", order = 63, requirement = "50 Monolitos"),
        Achievement("b_mon_4", "Monolitos IV", "Transmisión.", AchievementType.BUILDING_OWNED, "b_monolith", BigDecimal("100"), BigDecimal("1.05"), groupId = "4_build_mon", order = 64, requirement = "100 Monolitos"),
        Achievement("b_mon_5", "Monolitos V", "Red Planetaria.", AchievementType.BUILDING_OWNED, "b_monolith", BigDecimal("150"), BigDecimal("1.06"), groupId = "4_build_mon", order = 65, requirement = "150 Monolitos"),

        // 10. TEMPLO
        Achievement("b_tem_1", "Templos I", "Ruinas.", AchievementType.BUILDING_OWNED, "b_temple", BigDecimal("5"), BigDecimal("1.03"), groupId = "4_build_tem", order = 66, requirement = "5 Templos"),
        Achievement("b_tem_2", "Templos II", "Restauración.", AchievementType.BUILDING_OWNED, "b_temple", BigDecimal("15"), BigDecimal("1.04"), groupId = "4_build_tem", order = 67, requirement = "15 Templos"),
        Achievement("b_tem_3", "Templos III", "Culto.", AchievementType.BUILDING_OWNED, "b_temple", BigDecimal("30"), BigDecimal("1.05"), groupId = "4_build_tem", order = 68, requirement = "30 Templos"),
        Achievement("b_tem_4", "Templos IIV", "Peregrinación.", AchievementType.BUILDING_OWNED, "b_temple", BigDecimal("60"), BigDecimal("1.06"), groupId = "4_build_tem", order = 69, requirement = "60 Templos"),
        Achievement("b_tem_5", "Templos V", "R'lyeh.", AchievementType.BUILDING_OWNED, "b_temple", BigDecimal("100"), BigDecimal("1.07"), groupId = "4_build_tem", order = 70, requirement = "100 Templos"),

        // 11. PORTAL
        Achievement("b_gat_1", "Portales I", "Grietas.", AchievementType.BUILDING_OWNED, "b_gate", BigDecimal("5"), BigDecimal("1.03"), groupId = "4_build_gat", order = 71, requirement = "5 Portales"),
        Achievement("b_gat_2", "Portales II", "Ventanas.", AchievementType.BUILDING_OWNED, "b_gate", BigDecimal("15"), BigDecimal("1.04"), groupId = "4_build_gat", order = 72, requirement = "15 Portales"),
        Achievement("b_gat_3", "Portales III", "Puertas.", AchievementType.BUILDING_OWNED, "b_gate", BigDecimal("30"), BigDecimal("1.05"), groupId = "4_build_gat", order = 73, requirement = "30 Portales"),
        Achievement("b_gat_4", "Portales IV", "Puentes.", AchievementType.BUILDING_OWNED, "b_gate", BigDecimal("50"), BigDecimal("1.06"), groupId = "4_build_gat", order = 74, requirement = "50 Portales"),
        Achievement("b_gat_5", "Portales V", "Convergencia.", AchievementType.BUILDING_OWNED, "b_gate", BigDecimal("100"), BigDecimal("1.08"), groupId = "4_build_gat", order = 75, requirement = "100 Portales"),

        // 12. SHOGGOTH
        Achievement("b_sho_1", "Shoggoths I", "Masa.", AchievementType.BUILDING_OWNED, "b_shoggoth", BigDecimal("5"), BigDecimal("1.04"), groupId = "4_build_sho", order = 76, requirement = "5 Shoggoths"),
        Achievement("b_sho_2", "Shoggoths II", "Ojos.", AchievementType.BUILDING_OWNED, "b_shoggoth", BigDecimal("15"), BigDecimal("1.05"), groupId = "4_build_sho", order = 77, requirement = "15 Shoggoths"),
        Achievement("b_sho_3", "Shoggoths III", "Bocas.", AchievementType.BUILDING_OWNED, "b_shoggoth", BigDecimal("30"), BigDecimal("1.06"), groupId = "4_build_sho", order = 78, requirement = "30 Shoggoths"),
        Achievement("b_sho_4", "Shoggoths IV", "Esclavos.", AchievementType.BUILDING_OWNED, "b_shoggoth", BigDecimal("50"), BigDecimal("1.07"), groupId = "4_build_sho", order = 79, requirement = "50 Shoggoths"),
        Achievement("b_sho_5", "Shoggoths V", "Rebelión.", AchievementType.BUILDING_OWNED, "b_shoggoth", BigDecimal("80"), BigDecimal("1.09"), groupId = "4_build_sho", order = 80, requirement = "80 Shoggoths"),

        // 13. SUEÑO
        Achievement("b_dre_1", "Sueños I", "Siesta.", AchievementType.BUILDING_OWNED, "b_dream", BigDecimal("5"), BigDecimal("1.04"), groupId = "4_build_dre", order = 81, requirement = "5 Sueños"),
        Achievement("b_dre_2", "Sueños II", "REM.", AchievementType.BUILDING_OWNED, "b_dream", BigDecimal("15"), BigDecimal("1.05"), groupId = "4_build_dre", order = 82, requirement = "15 Sueños"),
        Achievement("b_dre_3", "Sueños III", "Lucidez.", AchievementType.BUILDING_OWNED, "b_dream", BigDecimal("30"), BigDecimal("1.06"), groupId = "4_build_dre", order = 83, requirement = "30 Sueños"),
        Achievement("b_dre_4", "Sueños IV", "Viaje Astral.", AchievementType.BUILDING_OWNED, "b_dream", BigDecimal("50"), BigDecimal("1.07"), groupId = "4_build_dre", order = 84, requirement = "50 Sueños"),
        Achievement("b_dre_5", "Sueños V", "Despertar.", AchievementType.BUILDING_OWNED, "b_dream", BigDecimal("75"), BigDecimal("1.09"), groupId = "4_build_dre", order = 85, requirement = "75 Sueños"),

        // 14. ESTRELLAS
        Achievement("b_sta_1", "Estrellas I", "Observador.", AchievementType.BUILDING_OWNED, "b_star", BigDecimal("1"), BigDecimal("1.05"), groupId = "4_build_sta", order = 86, requirement = "1 Estrella"),
        Achievement("b_sta_2", "Estrellas II", "Sincronía.", AchievementType.BUILDING_OWNED, "b_star", BigDecimal("5"), BigDecimal("1.06"), groupId = "4_build_sta", order = 87, requirement = "5 Estrellas"),
        Achievement("b_sta_3", "Estrellas III", "Constelación.", AchievementType.BUILDING_OWNED, "b_star", BigDecimal("10"), BigDecimal("1.07"), groupId = "4_build_sta", order = 88, requirement = "10 Estrellas"),
        Achievement("b_sta_4", "Estrellas IV", "Zodiaco.", AchievementType.BUILDING_OWNED, "b_star", BigDecimal("25"), BigDecimal("1.08"), groupId = "4_build_sta", order = 89, requirement = "25 Estrellas"),
        Achievement("b_sta_5", "Estrellas V", "Cielo Caído.", AchievementType.BUILDING_OWNED, "b_star", BigDecimal("50"), BigDecimal("1.10"), groupId = "4_build_sta", order = 90, requirement = "50 Estrellas"),

        // 15. AVATAR
        Achievement("b_old_1", "Avatares I", "Invocador.", AchievementType.BUILDING_OWNED, "b_oldone", BigDecimal("1"), BigDecimal("1.05"), groupId = "4_build_old", order = 91, requirement = "1 Avatar"),
        Achievement("b_old_2", "Avatares II", "Heraldo.", AchievementType.BUILDING_OWNED, "b_oldone", BigDecimal("3"), BigDecimal("1.06"), groupId = "4_build_old", order = 92, requirement = "3 Avatares"),
        Achievement("b_old_3", "Avatares III", "Panteón.", AchievementType.BUILDING_OWNED, "b_oldone", BigDecimal("5"), BigDecimal("1.08"), groupId = "4_build_old", order = 93, requirement = "5 Avatares"),
        Achievement("b_old_4", "Avatares IV", "Guerra.", AchievementType.BUILDING_OWNED, "b_oldone", BigDecimal("10"), BigDecimal("1.10"), groupId = "4_build_old", order = 94, requirement = "10 Avatares"),
        Achievement("b_old_5", "Avatares V", "Era Oscura.", AchievementType.BUILDING_OWNED, "b_oldone", BigDecimal("20"), BigDecimal("1.12"), groupId = "4_build_old", order = 95, requirement = "20 Avatares"),

        // 16. VACÍO
        Achievement("b_voi_1", "Vacío I", "Azathoth.", AchievementType.BUILDING_OWNED, "b_void", BigDecimal("1"), BigDecimal("1.10"), groupId = "4_build_voi", order = 96, requirement = "1 Trono"),
        Achievement("b_voi_2", "Vacío II", "Caos.", AchievementType.BUILDING_OWNED, "b_void", BigDecimal("2"), BigDecimal("1.12"), groupId = "4_build_voi", order = 97, requirement = "2 Tronos"),
        Achievement("b_voi_3", "Vacío III", "Llave.", AchievementType.BUILDING_OWNED, "b_void", BigDecimal("3"), BigDecimal("1.14"), groupId = "4_build_voi", order = 98, requirement = "3 Tronos"),
        Achievement("b_voi_4", "Vacío IV", "Cabra.", AchievementType.BUILDING_OWNED, "b_void", BigDecimal("5"), BigDecimal("1.16"), groupId = "4_build_voi", order = 99, requirement = "5 Tronos"),
        Achievement("b_voi_5", "Vacío V", "Fin.", AchievementType.BUILDING_OWNED, "b_void", BigDecimal("10"), BigDecimal("1.20"), groupId = "4_build_voi", order = 100, requirement = "10 Tronos"),

        // ============================================================================================
        // LOGROS SECRETOS (13) - isSecret = true
        // ============================================================================================

        // 1. LA BESTIA
        Achievement("sec_1", "La Marca de la Bestia", "Su número es humano.", AchievementType.BUILDING_OWNED, "b_cultist", BigDecimal("666"), BigDecimal("1.50"), groupId = "5_secret", order = 101, requirement = "Tener 666 Sectarios", isSecret = true),

        // 2. MALA SUERTE
        Achievement("sec_2", "Mala Suerte", "No mires debajo de la cama.", AchievementType.BUILDING_OWNED, "b_shoggoth", BigDecimal("13"), BigDecimal("1.20"), groupId = "5_secret", order = 102, requirement = "Tener 13 Shoggoths", isSecret = true),

        // 3. SENTIDO DE LA VIDA
        Achievement("sec_3", "El Sentido de la Vida", "La respuesta definitiva.", AchievementType.BUILDING_OWNED, "b_star", BigDecimal("42"), BigDecimal("1.20"), groupId = "5_secret", order = 103, requirement = "Tener 42 Estrellas", isSecret = true),

        // 4. ILUMINATI
        Achievement("sec_4", "Logia Masónica", "El grado supremo.", AchievementType.BUILDING_OWNED, "b_temple", BigDecimal("33"), BigDecimal("1.20"), groupId = "5_secret", order = 104, requirement = "Tener 33 Templos", isSecret = true),

        // 5. OBSESIVO
        Achievement("sec_5", "Grafomanía", "Más papel que vida.", AchievementType.BUILDING_OWNED, "b_notes", BigDecimal("1000"), BigDecimal("1.15"), groupId = "5_secret", order = 105, requirement = "Tener 1000 Notas", isSecret = true),

        // 6. COLECCIONISTA
        Achievement("sec_6", "Viajero Dimensional", "Has cruzado el umbral.", AchievementType.BUILDING_OWNED, "b_gate", BigDecimal("1"), BigDecimal("1.10"), groupId = "5_secret", order = 106, requirement = "Tener 1 Portal", isSecret = true),

        // 7. EJÉRCITO
        Achievement("sec_7", "Ejército de las Sombras", "Están listos para atacar.", AchievementType.BUILDING_OWNED, "b_shoggoth", BigDecimal("500"), BigDecimal("1.30"), groupId = "5_secret", order = 107, requirement = "Tener 500 Shoggoths", isSecret = true),

        // 8. INVASIÓN
        Achievement("sec_8", "Invasión Total", "La Tierra es nuestra.", AchievementType.BUILDING_OWNED, "b_gate", BigDecimal("200"), BigDecimal("1.40"), groupId = "5_secret", order = 108, requirement = "Tener 200 Portales", isSecret = true),

        // 9. VACÍO ABSOLUTO
        Achievement("sec_9", "Consumido", "No queda nada.", AchievementType.BUILDING_OWNED, "b_void", BigDecimal("50"), BigDecimal("1.50"), groupId = "5_secret", order = 109, requirement = "Tener 50 Tronos", isSecret = true),

        // 10-13. HITOS DE CONOCIMIENTO POST-GAME OVER
        Achievement("sec_10", "Conocimiento Prohibido", "Más allá del grimorio.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000000000000000000000000000"), BigDecimal("1.20"), groupId = "5_secret", order = 110, requirement = "1 Decillón Conocimiento", isSecret = true),
        Achievement("sec_11", "Locura Infinita", "La mente se disuelve.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000000000000000000000000000000"), BigDecimal("1.30"), groupId = "5_secret", order = 111, requirement = "1 Undecillón Conocimiento", isSecret = true),
        Achievement("sec_12", "El Fin", "El universo parpadea.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000000000000000000000000000000000"), BigDecimal("1.40"), groupId = "5_secret", order = 112, requirement = "1 Duodecillón Conocimiento", isSecret = true),
        Achievement("sec_13", "Nuevo Comienzo", "Crearás algo nuevo.", AchievementType.RESOURCE_REACHED, null, BigDecimal("1000000000000000000000000000000000000000000"), BigDecimal("2.00"), groupId = "5_secret", order = 113, requirement = "1 Tredecillón Conocimiento", isSecret = true)
    )

    fun loadFromSave(saved: List<AchievementEntity>) {
        availableAchievements.forEach { it.isUnlocked = false }
        saved.forEach { s ->
            availableAchievements.find { it.id == s.id }?.isUnlocked = s.isUnlocked
        }
    }

    fun checkAchievements(currentKnowledge: BigDecimal, buildings: List<Building>): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()
        availableAchievements.filter { !it.isUnlocked }.forEach { achievement ->
            val unlocked = when (achievement.conditionType) {
                AchievementType.RESOURCE_REACHED -> currentKnowledge >= achievement.targetValue
                AchievementType.BUILDING_OWNED -> {
                    val building = buildings.find { it.id == achievement.targetId }
                    building != null && BigDecimal(building.amountOwned) >= achievement.targetValue
                }
            }
            if (unlocked) {
                achievement.isUnlocked = true
                newlyUnlocked.add(achievement)
            }
        }
        return newlyUnlocked
    }

    fun getGlobalAchievementMultiplier(): BigDecimal {
        var multiplier = BigDecimal.ONE
        availableAchievements.filter { it.isUnlocked }.forEach {
            // 16 dígitos significativos: el producto exacto de ~100 bonos arrastraba
            // cientos de decimales a todos los cálculos de producción.
            multiplier = multiplier.multiply(it.bonusMultiplier, MathContext.DECIMAL64)
        }
        return multiplier
    }

    fun resetAchievements() {
        availableAchievements.forEach { it.isUnlocked = false }
    }
}