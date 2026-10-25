package com.EGomez.idleecosystem.domain.model

import java.math.BigDecimal

enum class EffectType {
    ADD_PRODUCTION_MINUTES, // Ganas (o pierdes) X minutos de tu producción actual
    ADD_SANITY_PERCENT,     // Ganas (o pierdes) % de Cordura actual (redondeado a entero)
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
    val effects: List<EventEffect>
)

data class RandomEvent(
    val id: String,
    val title: String,
    val description: String,
    val options: List<EventOption>,
    val weight: Int = 10,
    /**
     * Condición de aparición: (conocimiento actual, Cordura histórica).
     * La Cordura histórica (total obtenida) indica si el jugador ya ascendió alguna vez;
     * no usamos el saldo porque gastarlo todo en la tienda devolvía los eventos de inicio.
     */
    val condition: (BigDecimal, BigDecimal) -> Boolean = { _, _ -> true }
)