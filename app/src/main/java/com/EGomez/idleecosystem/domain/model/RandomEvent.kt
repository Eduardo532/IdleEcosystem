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
    val followUpEventId: String? = null,
    /** Memoria (v0.26.1): lo que el juego recuerda para siempre de esta decisión. */
    val memories: List<String> = emptyList()
)

/** Lo que un evento con memoria necesita saber de la partida (v0.26.1). */
data class EventProgress(
    val memories: Set<String> = emptySet(),
    val transcendences: Int = 0,
    val awakenings: Int = 0
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
    val isFollowUp: Boolean = false,
    /** Memoria (v0.26.1): aparece solo si se recuerdan todas estas decisiones... */
    val requiresAll: List<String> = emptyList(),
    /** ...y al menos una de estas... */
    val requiresAny: List<String> = emptyList(),
    /** ...y ninguna de estas (así cada encuentro ocurre una sola vez). */
    val forbids: List<String> = emptyList(),
    val minTranscendences: Int = 0,
    val minAwakenings: Int = 0
) {
    /** Si las memorias y el progreso permiten que aparezca. */
    fun allows(progress: EventProgress): Boolean =
        progress.transcendences >= minTranscendences && progress.awakenings >= minAwakenings &&
            requiresAll.all { it in progress.memories } &&
            (requiresAny.isEmpty() || requiresAny.any { it in progress.memories }) &&
            forbids.none { it in progress.memories }

    /** Un evento que recuerda decisiones anteriores. */
    val remembers: Boolean get() = requiresAll.isNotEmpty() || requiresAny.isNotEmpty()
}