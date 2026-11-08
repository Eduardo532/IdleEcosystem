package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.EffectType
import com.EGomez.idleecosystem.domain.model.EventEffect
import com.EGomez.idleecosystem.domain.model.EventOption
import com.EGomez.idleecosystem.domain.model.RandomEvent
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class RandomEventManager @Inject constructor() {

    private val events = listOf(
        // ========================================================================
        // TIER 0: INICIO SEGURO (Sin Cordura)
        // Aparecen cuando NO tienes Cordura (Prestigio 0)
        // ========================================================================

        RandomEvent(
            id = "evt_start_coin",
            title = "Moneda Antigua",
            description = "Encuentras una moneda con extraños grabados en el suelo. No parece de este país.",
            options = listOf(
                EventOption(
                    text = "Estudiarla",
                    flavorResult = "Los símbolos te dan una idea para mejorar la producción.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.05))
                ),
                EventOption(
                    text = "Guardarla",
                    flavorResult = "Un amuleto de buena suerte.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0))
                )
            ),
            weight = 20,
            condition = { _, sanity -> sanity.signum() == 0 }
        ),

        RandomEvent(
            id = "evt_start_dream",
            title = "Sueño Inquieto",
            description = "Has tenido un sueño vívido sobre geometría, pero despiertas descansado.",
            options = listOf(
                EventOption(
                    text = "Anotar el sueño",
                    flavorResult = "Garabateas algunas formas útiles.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.08))
                ),
                EventOption(
                    text = "Ignorarlo",
                    flavorResult = "Solo fue un sueño.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0))
                )
            ),
            weight = 20,
            condition = { _, sanity -> sanity.signum() == 0 }
        ),

        RandomEvent(
            id = "evt_start_book",
            title = "Libro Polvoriento",
            description = "Un libro cae de la estantería por sí solo. Está abierto en una página interesante.",
            options = listOf(
                EventOption(
                    text = "Leer la página",
                    flavorResult = "Conceptos básicos que habías olvidado.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.10))
                )
            ),
            weight = 15,
            condition = { _, sanity -> sanity.signum() == 0 }
        ),

        // ========================================================================
        // TIER 1: EVENTOS COMUNES (Requieren Cordura desbloqueada)
        // ========================================================================

        RandomEvent(
            id = "evt_rift",
            title = "Grieta en la Realidad",
            description = "Una fisura luminosa palpita en el aire. Escuchas verdades dolorosas.",
            options = listOf(
                EventOption(
                    text = "Mirar dentro",
                    flavorResult = "Tu mente se expande violentamente.",
                    effects = listOf(
                        EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.20),
                        EventEffect(EffectType.ADD_SANITY_PERCENT, -0.05)
                    )
                ),
                EventOption(
                    text = "Cerrarla",
                    flavorResult = "Te sientes seguro ignorando lo desconocido.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.05))
                )
            ),
            weight = 10,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        RandomEvent(
            id = "evt_cat",
            title = "Visitante de Ulthar",
            description = "Un gato negro te observa. Sus ojos contienen galaxias enteras.",
            options = listOf(
                EventOption(
                    text = "Acariciarlo",
                    flavorResult = "Su ronroneo estabiliza tu existencia.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.15))
                ),
                EventOption(
                    text = "Observarlo",
                    flavorResult = "Aprendes sobre el movimiento de los astros.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.10))
                )
            ),
            weight = 10,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        RandomEvent(
            id = "evt_whispers",
            title = "Susurros en la Pared",
            description = "Alguien rasca detrás del papel tapiz. Crees reconocer la voz.",
            options = listOf(
                EventOption(
                    text = "Arrancar el papel",
                    flavorResult = "Encontraste fórmulas antiguas en el yeso.",
                    effects = listOf(
                        EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.15),
                        EventEffect(EffectType.ADD_SANITY_PERCENT, -0.02)
                    )
                ),
                EventOption(
                    text = "Poner música alta",
                    flavorResult = "La música ahoga las voces.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.05))
                )
            ),
            weight = 9,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        // ========================================================================
        // TIER 2: EVENTOS AVANZADOS (Cordura > 0)
        // ========================================================================

        RandomEvent(
            id = "evt_stars",
            title = "Convergencia Astral",
            description = "Las estrellas forman el Signo Amarillo. La magia fluye libremente.",
            options = listOf(
                EventOption(
                    text = "Realizar Ritual",
                    flavorResult = "¡Poder ilimitado!",
                    effects = listOf(
                        EventEffect(EffectType.GLOBAL_BUFF, 3.0, durationSeconds = 60),
                        EventEffect(EffectType.ADD_SANITY_PERCENT, -0.10)
                    )
                ),
                EventOption(
                    text = "Tomar Apuntes",
                    flavorResult = "Documentas el fenómeno con seguridad.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.25))
                )
            ),
            weight = 7,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        RandomEvent(
            id = "evt_geometry",
            title = "Arquitectura Imposible",
            description = "La esquina de tu habitación tiene más ángulos de los que debería.",
            options = listOf(
                EventOption(
                    text = "Estudiar el Vértice",
                    flavorResult = "Comprendes la cuarta dimensión.",
                    effects = listOf(
                        EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.40),
                        EventEffect(EffectType.ADD_SANITY_PERCENT, -0.15)
                    )
                ),
                EventOption(
                    text = "Cerrar los ojos",
                    flavorResult = "Si no lo veo, no es real.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0))
                )
            ),
            weight = 6,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        RandomEvent(
            id = "evt_burn",
            title = "Crisis de Fe",
            description = "Sientes que te observan. Tal vez saber demasiado fue un error.",
            options = listOf(
                EventOption(
                    text = "Quemar apuntes",
                    flavorResult = "El fuego purifica. Recuperas la paz.",
                    effects = listOf(
                        EventEffect(EffectType.ADD_SANITY_PERCENT, 0.30),
                        EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, -0.10)
                    )
                ),
                EventOption(
                    text = "Proteger el saber",
                    flavorResult = "Abrazas la locura.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, -0.05))
                )
            ),
            weight = 5,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        RandomEvent(
            id = "evt_music",
            title = "La Viola Muda",
            description = "Una melodía fantasmal suena desde el piso de arriba.",
            options = listOf(
                EventOption(
                    text = "Escuchar atentamente",
                    flavorResult = "La música acelera tu mente.",
                    effects = listOf(
                        EventEffect(EffectType.GLOBAL_BUFF, 5.0, durationSeconds = 30),
                        EventEffect(EffectType.ADD_SANITY_PERCENT, -0.05)
                    )
                ),
                EventOption(
                    text = "Taparse los oídos",
                    flavorResult = "El silencio es oro.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0))
                )
            ),
            weight = 6,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        // ========================================================================
        // TIER 3: EVENTOS HARDCORE (Cordura > 0)
        // ========================================================================

        RandomEvent(
            id = "evt_madness",
            title = "Epifanía Mortal",
            description = "El velo de la realidad cae. Puedes ver los hilos del universo.",
            options = listOf(
                EventOption(
                    text = "Aceptarlo Todo",
                    flavorResult = "¡LO VEO TODO!",
                    effects = listOf(
                        EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.75),
                        EventEffect(EffectType.ADD_SANITY_PERCENT, -0.50)
                    )
                ),
                EventOption(
                    text = "Gritar y huir",
                    flavorResult = "Te escondes hasta que pasa.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, -0.05))
                )
            ),
            weight = 3,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        RandomEvent(
            id = "evt_cthulhu",
            title = "La Llamada",
            description = "Sueñas con una ciudad sumergida y una voz profunda.",
            options = listOf(
                EventOption(
                    text = "Responder",
                    flavorResult = "Te conviertes en un sacerdote de lo profundo.",
                    effects = listOf(
                        EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.50),
                        EventEffect(EffectType.ADD_SANITY_PERCENT, -0.25)
                    )
                ),
                EventOption(
                    text = "Despertar",
                    flavorResult = "Despiertas sudando agua salada.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, -0.10))
                )
            ),
            weight = 2,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        RandomEvent(
            id = "evt_king",
            title = "La Obra Prohibida",
            description = "Encuentras el manuscrito de 'El Rey de Amarillo'.",
            options = listOf(
                EventOption(
                    text = "Leerlo",
                    flavorResult = "¡NO LLEVAS MÁSCARA!",
                    effects = listOf(
                        EventEffect(EffectType.GLOBAL_BUFF, 10.0, durationSeconds = 20),
                        EventEffect(EffectType.ADD_SANITY_PERCENT, -0.30)
                    )
                ),
                EventOption(
                    text = "Quemarlo",
                    flavorResult = "Las cenizas amarillas vuelan.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.10))
                )
            ),
            weight = 2,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        RandomEvent(
            id = "evt_deepones",
            title = "Oro de los Profundos",
            description = "Un extraño pálido te ofrece joyas raras.",
            options = listOf(
                EventOption(
                    text = "Aceptar el trato",
                    flavorResult = "Las joyas brillan con luz impía.",
                    effects = listOf(
                        EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.35),
                        EventEffect(EffectType.ADD_SANITY_PERCENT, -0.15)
                    )
                ),
                EventOption(
                    text = "Rechazar",
                    flavorResult = "El extraño se marcha.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0))
                )
            ),
            weight = 4,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),

        RandomEvent(
            id = "evt_amnesia",
            title = "Niebla Mental",
            description = "Has visto demasiado. Tu cerebro bloquea recuerdos.",
            options = listOf(
                EventOption(
                    text = "Dejar que pase",
                    flavorResult = "Respiras tranquilo.",
                    effects = listOf(
                        EventEffect(EffectType.ADD_SANITY_PERCENT, 0.50),
                        EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, -0.20)
                    )
                ),
                EventOption(
                    text = "Luchar contra ello",
                    flavorResult = "El dolor es insoportable.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, -0.10))
                )
            ),
            weight = 3,
            condition = { _, sanity -> sanity.signum() > 0 }
        )
    )

    // AHORA RECIBE EL ESTADO PARA FILTRAR
    fun getRandomEvent(currentKnowledge: BigDecimal, currentSanity: BigDecimal): RandomEvent? {
        // 1. Filtramos por la condición definida en cada evento
        val validEvents = events.filter { it.condition(currentKnowledge, currentSanity) }

        if (validEvents.isEmpty()) return null

        // 2. Selección ponderada (Weighted Random)
        val totalWeight = validEvents.sumOf { it.weight }
        if (totalWeight == 0) return validEvents.firstOrNull()

        val randomValue = Random.nextInt(totalWeight)
        var currentWeight = 0
        for (event in validEvents) {
            currentWeight += event.weight
            if (randomValue < currentWeight) return event
        }
        return validEvents.first()
    }
}