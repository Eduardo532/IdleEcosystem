package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal

enum class EffectType {
    ADD_KNOWLEDGE_PERCENT, // Ganas % de Conocimiento actual
    ADD_SANITY_PERCENT,    // Ganas (o pierdes) % de Cordura actual
    GLOBAL_BUFF,           // Multiplicador temporal
    NOTHING                // Texto de sabor sin efecto
}

data class EventEffect(
    val type: EffectType,
    val value: Double,
    val durationSeconds: Int = 0
)

data class EventOption(
    val text: String,
    val flavorResult: String,
    val effects: List<EventEffect>,
    // Cadena: elegir esta opción hace que el PRÓXIMO evento sea este (por id)
    val followUpEventId: String? = null
)

data class RandomEvent(
    val id: String,
    val title: String,
    val description: String,
    val options: List<EventOption>,
    val weight: Int = 10,
    val condition: (BigDecimal, BigDecimal) -> Boolean = { _, _ -> true },
    // Solo aparece después de esta cantidad de Ascensiones
    val minAscensions: Int = 0,
    // Continuación de una cadena: nunca aparece al azar, solo tras su opción previa
    val isFollowUp: Boolean = false
)