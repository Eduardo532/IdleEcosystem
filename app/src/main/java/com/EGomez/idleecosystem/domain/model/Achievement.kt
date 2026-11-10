package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal

enum class AchievementType {
    RESOURCE_REACHED, // Alcanzar X dinero
    BUILDING_OWNED,   // Tener X edificios de tipo Y
    STAT              // Una estadística o contador (ver AchievementStat) llega a X (v0.17)
}

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val conditionType: AchievementType,
    val targetId: String? = null, // ID del edificio si es BUILDING_OWNED
    val targetValue: BigDecimal,
    val bonusMultiplier: BigDecimal, // Ej: 1.05 para +5%
    var isUnlocked: Boolean = false,

    // CAMPOS DE ORDENAMIENTO VISUAL
    val groupId: String? = null, // Para agruparlos visualmente si quieres
    val order: Int = 0,          // Para forzar un orden fijo en la lista
    val requirement: String = "", // Texto descriptivo del requisito (ej: "1000 Oro")

    // NUEVO CAMPO: LOGRO OCULTO
    val isSecret: Boolean = false,

    // v0.17: logros de tipo STAT
    /** La clave que se mide (AchievementStat). */
    val stat: String? = null,
    /** Solo cuenta con este pacto activo (id del pacto). */
    val pact: String? = null,
    /** Solo cuenta recorriendo esta Senda (id del nodo). */
    val path: String? = null
)