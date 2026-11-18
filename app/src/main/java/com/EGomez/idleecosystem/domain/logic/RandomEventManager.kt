package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.EffectType
import com.EGomez.idleecosystem.domain.model.EventEffect
import com.EGomez.idleecosystem.domain.model.EventOption
import com.EGomez.idleecosystem.domain.model.EventProgress
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
        ),

        // ========================================================================
        // CADENAS: una decisión abre el próximo evento
        // ========================================================================

        RandomEvent(
            id = "evt_chain_book_1",
            title = "El Libro sin Título",
            description = "En tu escritorio hay un libro que no compraste. La tapa está tibia.",
            options = listOf(
                EventOption(
                    text = "Abrirlo",
                    flavorResult = "Las páginas están en blanco... por ahora.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0)),
                    followUpEventId = "evt_chain_book_2"
                ),
                EventOption(
                    text = "Venderlo",
                    flavorResult = "El librero no quiso tocarlo, pero pagó bien.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.05))
                )
            ),
            weight = 6,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),
        RandomEvent(
            id = "evt_chain_book_2",
            title = "Páginas que se Escriben Solas",
            description = "Las páginas del libro se llenan de tinta mientras duermes. Ahora hablan de ti.",
            options = listOf(
                EventOption(
                    text = "Seguir leyendo",
                    flavorResult = "Cada línea revela algo que no deberías saber.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.15)),
                    followUpEventId = "evt_chain_book_3"
                ),
                EventOption(
                    text = "Quemar el libro",
                    flavorResult = "Las cenizas forman una palabra antes de dispersarse.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.03))
                )
            ),
            isFollowUp = true
        ),
        RandomEvent(
            id = "evt_chain_book_3",
            title = "El Autor",
            description = "La última página tiene una firma. Es tu letra. Alguien llama a la puerta.",
            options = listOf(
                EventOption(
                    text = "Abrir la puerta",
                    flavorResult = "Te reconoces en el umbral. Por un minuto, sabes todo.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 4.0, durationSeconds = 60))
                ),
                EventOption(
                    text = "Esconderte",
                    flavorResult = "Los golpes cesan al amanecer. El libro ya no está.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.05))
                )
            ),
            isFollowUp = true
        ),

        RandomEvent(
            id = "evt_chain_guest_1",
            title = "Un Desconocido en la Puerta",
            description = "Un hombre empapado pide refugio. No está lloviendo.",
            options = listOf(
                EventOption(
                    text = "Dejarlo pasar",
                    flavorResult = "Se sienta junto al fuego sin decir palabra.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0)),
                    followUpEventId = "evt_chain_guest_2",
                    memories = listOf(GUEST_WELCOMED)
                ),
                EventOption(
                    text = "Cerrar la puerta",
                    flavorResult = "Escuchas pasos alejándose... hacia el mar.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0)),
                    memories = listOf(GUEST_SHUT)
                )
            ),
            weight = 6,
            condition = { _, sanity -> sanity.signum() > 0 }
        ),
        RandomEvent(
            id = "evt_chain_guest_2",
            title = "El Huésped no Duerme",
            description = "Toda la noche murmura en una lengua que casi entiendes.",
            options = listOf(
                EventOption(
                    text = "Transcribir sus palabras",
                    flavorResult = "Al amanecer, tus notas valen una fortuna.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.12))
                ),
                EventOption(
                    text = "Responderle",
                    flavorResult = "Sonríe. Te enseña una canción para la próxima marea.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 3.0, durationSeconds = 45))
                )
            ),
            isFollowUp = true
        ),

        // ========================================================================
        // MEMORIAS: solo aparecen después de Ascender
        // ========================================================================

        RandomEvent(
            id = "evt_ascended_memories",
            title = "Recuerdos de Otra Vida",
            description = "Reconoces una fórmula que nunca estudiaste. La escribiste antes de Ascender.",
            options = listOf(
                EventOption(
                    text = "Recordar",
                    flavorResult = "Tu yo anterior dejó buenas notas.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.08))
                ),
                EventOption(
                    text = "Dejar el pasado atrás",
                    flavorResult = "Hay paz en olvidar.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.03))
                )
            ),
            weight = 8,
            condition = { _, sanity -> sanity.signum() > 0 },
            minAscensions = 1
        ),
        RandomEvent(
            id = "evt_ascended_echo",
            title = "El Eco de tus Ascensiones",
            description = "Tres versiones de ti susurran a la vez. Coinciden en todo.",
            options = listOf(
                EventOption(
                    text = "Escuchar el coro",
                    flavorResult = "Por un momento trabajan contigo.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 3.0, durationSeconds = 90))
                ),
                EventOption(
                    text = "Preguntarles algo",
                    flavorResult = "Responden con lo que aprendieron.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.20))
                )
            ),
            weight = 5,
            condition = { _, sanity -> sanity.signum() > 0 },
            minAscensions = 3
        ),
        RandomEvent(
            id = "evt_ascended_throne",
            title = "La Mirada del Trono",
            description = "Has subido tantas veces que algo, allá arriba, te devuelve la mirada.",
            options = listOf(
                EventOption(
                    text = "Sostener la mirada",
                    flavorResult = "No parpadeas. Algo te respeta.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 6.0, durationSeconds = 45))
                ),
                EventOption(
                    text = "Inclinar la cabeza",
                    flavorResult = "La humildad tiene su recompensa.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.10))
                )
            ),
            weight = 3,
            condition = { _, sanity -> sanity.signum() > 0 },
            minAscensions = 5
        ),

        // ========================================================================
        // EVENTOS CON MEMORIA (v0.26.1): recuerdan tus decisiones de otras vidas
        // ========================================================================

        RandomEvent(
            id = "evt_mem_cats_1",
            title = "Un Gato en el Umbral",
            description = "Un gato negro te mira desde el umbral, como si ya te conociera. No maúlla. Espera.",
            options = listOf(
                EventOption(
                    text = "Darle de comer",
                    flavorResult = "Come despacio, sin dejar de mirarte. Al irse, deja una pluma que no es de ningún pájaro.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.05)),
                    memories = listOf(CATS_FED)
                ),
                EventOption(
                    text = "Espantarlo",
                    flavorResult = "Se va sin prisa. En el barro, sus huellas siguen caminando un rato después.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.03)),
                    memories = listOf(CATS_SCARED)
                )
            ),
            weight = 8,
            minAscensions = 2,
            forbids = listOf(CATS_FED, CATS_SCARED)
        ),
        RandomEvent(
            id = "evt_mem_cats_fed",
            title = "Los Gatos Vuelven",
            description = "Ahora son siete. El primero, el negro, te reconoce y trae a los demás. Dejan regalos en tu escritorio que no son de este mundo.",
            options = listOf(
                EventOption(
                    text = "Aceptar los regalos",
                    flavorResult = "Un hueso de luna, una llave sin puerta, un sueño atado con un hilo. Todo sirve.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 5.0, durationSeconds = 60)),
                    memories = listOf(CATS_GIFTS)
                ),
                EventOption(
                    text = "Seguirlos",
                    flavorResult = "Te llevan por calles que no estaban en tu ciudad. Vuelves con la cabeza llena.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.20)),
                    memories = listOf(CATS_FOLLOWED)
                )
            ),
            weight = 8,
            requiresAll = listOf(CATS_FED),
            forbids = listOf(CATS_GIFTS, CATS_FOLLOWED),
            minTranscendences = 1
        ),
        RandomEvent(
            id = "evt_mem_cats_scared",
            title = "Ojos en la Oscuridad",
            description = "Cada noche hay más ojos en tu ventana. No parpadean. Recuerdas al gato negro que espantaste.",
            options = listOf(
                EventOption(
                    text = "Pedir perdón con leche",
                    flavorResult = "A la mañana el plato está vacío y los ojos se han ido. Casi todos.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.05)),
                    memories = listOf(CATS_FORGIVEN)
                ),
                EventOption(
                    text = "Cerrar las cortinas",
                    flavorResult = "Los ojos siguen ahí, del otro lado de la tela. Lo sabes.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0)),
                    memories = listOf(CATS_ENEMIES)
                )
            ),
            weight = 8,
            requiresAll = listOf(CATS_SCARED),
            forbids = listOf(CATS_FORGIVEN, CATS_ENEMIES),
            minTranscendences = 1
        ),
        RandomEvent(
            id = "evt_mem_cats_council",
            title = "El Consejo de Ulthar",
            description = "Los gatos de Ulthar te reciben en su consejo, en la cara oculta de la luna. Recuerdan cada gesto tuyo, y hoy votan en tu favor.",
            options = listOf(
                EventOption(
                    text = "Escuchar",
                    flavorResult = "Hablan de los dioses como de ratones. Por un rato, tú también.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 10.0, durationSeconds = 120)),
                    memories = listOf(CATS_END)
                ),
                EventOption(
                    text = "Hablar",
                    flavorResult = "Les cuentas lo que sabes. Te corrigen tres veces, con amabilidad.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.30)),
                    memories = listOf(CATS_END)
                )
            ),
            weight = 8,
            requiresAny = listOf(CATS_GIFTS, CATS_FOLLOWED, CATS_FORGIVEN),
            forbids = listOf(CATS_END),
            minAwakenings = 1
        ),
        RandomEvent(
            id = "evt_mem_cats_vengeance",
            title = "Ulthar no Olvida",
            description = "En Ulthar una ley antigua prohíbe matar gatos. Nadie dijo nada de espantarlos. Pero ellos lo recuerdan, y hoy vienen todos.",
            options = listOf(
                EventOption(
                    text = "Aceptar su juicio",
                    flavorResult = "Te arañan una vez cada uno. Al final, el negro se sienta en tu regazo. Es una tregua.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.03)),
                    memories = listOf(CATS_BITTER)
                ),
                EventOption(
                    text = "Huir",
                    flavorResult = "Corres toda la noche. Al amanecer estás en tu cuarto, y el gato negro duerme en tu silla.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0)),
                    memories = listOf(CATS_BITTER)
                )
            ),
            weight = 8,
            requiresAll = listOf(CATS_ENEMIES),
            forbids = listOf(CATS_BITTER),
            minAwakenings = 1
        ),
        RandomEvent(
            id = "evt_mem_letters_1",
            title = "Una Carta de Arkham",
            description = "El profesor Wilmarth, de la Universidad de Miskatonic, te escribe: leyó tus notas en una revista que no recuerdas haber publicado, y quiere saber más.",
            options = listOf(
                EventOption(
                    text = "Responder con todo",
                    flavorResult = "Le escribes veinte páginas. Su respuesta llega antes de que envíes la tuya.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.08)),
                    memories = listOf(LETTERS_ANSWERED)
                ),
                EventOption(
                    text = "Quemar la carta",
                    flavorResult = "El papel tarda demasiado en arder. Las letras se resisten.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.03)),
                    memories = listOf(LETTERS_BURNED)
                )
            ),
            weight = 8,
            minAscensions = 3,
            forbids = listOf(LETTERS_ANSWERED, LETTERS_BURNED)
        ),
        RandomEvent(
            id = "evt_mem_letters_answered",
            title = "La Segunda Carta",
            description = "Wilmarth te agradece. En las colinas de Vermont hay unos seres que vienen de Yuggoth, y ahora le escriben con su letra. Te pide que no le escribas más.",
            options = listOf(
                EventOption(
                    text = "Escribirle igual",
                    flavorResult = "Tu carta vuelve sin abrir, pero con algo adentro que no es tuyo.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 4.0, durationSeconds = 60)),
                    memories = listOf(LETTERS_INSISTED)
                ),
                EventOption(
                    text = "Respetar su deseo",
                    flavorResult = "Guardas su carta en un cajón. A veces, de noche, la oyes doblarse sola.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.05)),
                    memories = listOf(LETTERS_RESPECTED)
                )
            ),
            weight = 8,
            requiresAll = listOf(LETTERS_ANSWERED),
            forbids = listOf(LETTERS_INSISTED, LETTERS_RESPECTED),
            minTranscendences = 1
        ),
        RandomEvent(
            id = "evt_mem_letters_burned",
            title = "Cenizas que Escriben",
            description = "Las cenizas de aquella carta de Arkham que quemaste forman frases en tu chimenea. La letra es del profesor.",
            options = listOf(
                EventOption(
                    text = "Leerlas",
                    flavorResult = "Dicen lo que la carta decía, y un poco más.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.15)),
                    memories = listOf(LETTERS_ASHES)
                ),
                EventOption(
                    text = "Barrerlas",
                    flavorResult = "La escoba deja surcos con forma de firma.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0)),
                    memories = listOf(LETTERS_SWEPT)
                )
            ),
            weight = 8,
            requiresAll = listOf(LETTERS_BURNED),
            forbids = listOf(LETTERS_ASHES, LETTERS_SWEPT),
            minTranscendences = 1
        ),
        RandomEvent(
            id = "evt_mem_letters_cylinder",
            title = "El Cilindro",
            description = "Llega un paquete sin remitente desde Vermont: un cilindro de metal que zumba. Dentro, una voz que conoces por carta dice tu nombre.",
            options = listOf(
                EventOption(
                    text = "Hablar con la voz",
                    flavorResult = "Wilmarth, o lo que queda de él, te cuenta Yuggoth con todo detalle.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 8.0, durationSeconds = 90)),
                    memories = listOf(LETTERS_END)
                ),
                EventOption(
                    text = "Enterrarlo",
                    flavorResult = "Lo entierras bajo el roble. El zumbido sigue, más bajo, todas las noches.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.08)),
                    memories = listOf(LETTERS_END)
                )
            ),
            weight = 8,
            requiresAny = listOf(LETTERS_INSISTED, LETTERS_RESPECTED, LETTERS_ASHES),
            forbids = listOf(LETTERS_END),
            minAwakenings = 1
        ),
        RandomEvent(
            id = "evt_mem_guest_tide",
            title = "La Marea Recuerda",
            description = "La marea sube hasta tu puerta y deja conchas con forma de ojo. El huésped empapado al que diste refugio dejó una dirección escrita con sal: Innsmouth.",
            options = listOf(
                EventOption(
                    text = "Ir a Innsmouth",
                    flavorResult = "El pueblo huele a pescado y a secreto. Te tratan como a un pariente.",
                    effects = listOf(EventEffect(EffectType.ADD_KNOWLEDGE_PERCENT, 0.20)),
                    memories = listOf(GUEST_INNSMOUTH)
                ),
                EventOption(
                    text = "Guardar las conchas",
                    flavorResult = "Si te acercas una al oído, oyes una canción que ya conoces.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 4.0, durationSeconds = 60)),
                    memories = listOf(GUEST_SHELLS)
                )
            ),
            weight = 8,
            requiresAll = listOf(GUEST_WELCOMED),
            forbids = listOf(GUEST_INNSMOUTH, GUEST_SHELLS),
            minTranscendences = 1
        ),
        RandomEvent(
            id = "evt_mem_guest_knock",
            title = "Golpes desde el Mar",
            description = "Alguien golpea tu puerta de noche. Huele a sal. Es el hombre empapado al que le cerraste la puerta, pero ya no parece un hombre.",
            options = listOf(
                EventOption(
                    text = "Abrir esta vez",
                    flavorResult = "Entra, gotea, se sienta. No dice nada de la otra vez.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.05)),
                    memories = listOf(GUEST_SECOND_CHANCE)
                ),
                EventOption(
                    text = "Atrancar la puerta",
                    flavorResult = "Los golpes siguen hasta el amanecer. La madera queda húmeda para siempre.",
                    effects = listOf(EventEffect(EffectType.NOTHING, 0.0)),
                    memories = listOf(GUEST_BARRED)
                )
            ),
            weight = 8,
            requiresAll = listOf(GUEST_SHUT),
            forbids = listOf(GUEST_SECOND_CHANCE, GUEST_BARRED),
            minTranscendences = 1
        ),
        RandomEvent(
            id = "evt_mem_guest_deep",
            title = "Y'ha-nthlei",
            description = "Te invitan bajo el Arrecife del Diablo, a la ciudad de los Profundos. Recuerdan quién les abrió la puerta.",
            options = listOf(
                EventOption(
                    text = "Bajar",
                    flavorResult = "Respiras agua por primera vez. No es tan distinto.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 10.0, durationSeconds = 120)),
                    memories = listOf(GUEST_END)
                ),
                EventOption(
                    text = "Prometer volver",
                    flavorResult = "Te dejan ir. Saben que vas a cumplir.",
                    effects = listOf(EventEffect(EffectType.ADD_SANITY_PERCENT, 0.10)),
                    memories = listOf(GUEST_END)
                )
            ),
            weight = 8,
            requiresAny = listOf(GUEST_INNSMOUTH, GUEST_SHELLS, GUEST_SECOND_CHANCE),
            forbids = listOf(GUEST_END),
            minAwakenings = 1
        ),
        RandomEvent(
            id = "evt_mem_witnesses",
            title = "Los Tres Testigos",
            description = "Un gato negro, un profesor que ya no tiene cuerpo y un hombre que huele a sal llaman a tu puerta a la vez. Vienen a contarte cómo termina todo.",
            options = listOf(
                EventOption(
                    text = "Escucharlos",
                    flavorResult = "Cada uno cuenta una parte. Juntas, son la historia que la Crónica no escribió.",
                    effects = listOf(EventEffect(EffectType.GLOBAL_BUFF, 20.0, durationSeconds = 300)),
                    memories = listOf(WITNESSES)
                )
            ),
            weight = 8,
            requiresAll = listOf(LETTERS_END, GUEST_END),
            requiresAny = listOf(CATS_END, CATS_BITTER),
            forbids = listOf(WITNESSES),
            minAwakenings = 3
        ),
    )

    companion object {
        // Las memorias de los eventos (v0.26.1), guardadas como "memory.<clave>"
        const val CATS_FED = "cats_fed"
        const val CATS_SCARED = "cats_scared"
        const val CATS_GIFTS = "cats_gifts"
        const val CATS_FOLLOWED = "cats_followed"
        const val CATS_FORGIVEN = "cats_forgiven"
        const val CATS_ENEMIES = "cats_enemies"
        const val CATS_END = "cats_end"
        const val CATS_BITTER = "cats_bitter"
        const val LETTERS_ANSWERED = "letters_answered"
        const val LETTERS_BURNED = "letters_burned"
        const val LETTERS_INSISTED = "letters_insisted"
        const val LETTERS_RESPECTED = "letters_respected"
        const val LETTERS_ASHES = "letters_ashes"
        const val LETTERS_SWEPT = "letters_swept"
        const val LETTERS_END = "letters_end"
        const val GUEST_WELCOMED = "guest_welcomed"
        const val GUEST_SHUT = "guest_shut"
        const val GUEST_INNSMOUTH = "guest_innsmouth"
        const val GUEST_SHELLS = "guest_shells"
        const val GUEST_SECOND_CHANCE = "guest_second_chance"
        const val GUEST_BARRED = "guest_barred"
        const val GUEST_END = "guest_end"
        const val WITNESSES = "witnesses"

        /** Las tres historias con memoria y lo que las cierra. */
        val ARC_ENDINGS: Map<String, List<String>> = mapOf(
            "cats" to listOf(CATS_END, CATS_BITTER),
            "letters" to listOf(LETTERS_END),
            "guest" to listOf(GUEST_END)
        )
    }

    /** Un evento por id (las continuaciones de cadena se piden así). */
    fun getEvent(id: String): RandomEvent? = events.find { it.id == id }

    /** Todo el catálogo, para revisarlo en los tests. */
    fun allEvents(): List<RandomEvent> = events

    // AHORA RECIBE EL ESTADO PARA FILTRAR
    fun getRandomEvent(
        currentKnowledge: BigDecimal,
        currentSanity: BigDecimal,
        ascensions: Int = 0,
        progress: EventProgress = EventProgress()
    ): RandomEvent? {
        // 1. Filtramos por la condición de cada evento. Las continuaciones de cadena
        //    nunca salen al azar, y las Memorias esperan sus Ascensiones.
        val validEvents = events.filter {
            !it.isFollowUp && ascensions >= it.minAscensions && it.allows(progress) &&
                it.condition(currentKnowledge, currentSanity)
        }

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