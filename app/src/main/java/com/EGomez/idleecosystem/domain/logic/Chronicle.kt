package com.EGomez.idleecosystem.domain.logic

import java.math.BigDecimal

/** Los cinco actos de la historia, uno por capa (v0.26.0). */
enum class ChronicleAct(val numeral: String, val title: String) {
    LIFE("I", "Las Notas Desordenadas"),
    ASCENSION("II", "La Cordura como Moneda"),
    TRANSCENDENCE("III", "El Éter y las Estrellas"),
    AWAKENING("IV", "Los Dioses te Nombran"),
    CHAOS("V", "El Sueño del Sultán Ciego")
}

/**
 * Un capítulo de la Crónica (v0.26.0): se escribe solo, una vez y para siempre, cuando
 * la estadística [stat] llega a [target]. Los capítulos cuentan la partida como una
 * historia en cinco actos, hasta el final.
 */
enum class ChronicleChapter(
    val id: String,
    val act: ChronicleAct,
    val title: String,
    val text: String,
    val stat: String,
    val target: Long
) {
    // Acto I: la Vida
    FIRST_NOTE(
        "first_note", ChronicleAct.LIFE, "La Primera Nota",
        "Escribiste la primera línea sin saber por qué. A la mañana siguiente había otra debajo, con tu letra, y no recordabas haberla escrito.",
        AchievementStat.BUILDINGS_TOTAL, 1
    ),
    KNOCKING(
        "knocking", ChronicleAct.LIFE, "Golpes en la Pared",
        "Golpeaste la mesa quinientas veces buscando una idea. Algo, del otro lado de la pared, empezó a contestar.",
        AchievementStat.CLICKS, 500
    ),
    WINDOW(
        "window", ChronicleAct.LIFE, "Algo Pasó por la Ventana",
        "Fue rápido y no tenía forma. Dejó una marca en el cristal, por dentro.",
        AchievementStat.EVENTS, 1
    ),
    LIBRARY(
        "library", ChronicleAct.LIFE, "Una Biblioteca Imposible",
        "Ocho clases de conocimiento llenan ya tu cuarto. Los estantes son más altos que el techo, y nadie parece notarlo.",
        AchievementStat.BUILDING_TYPES, 8
    ),

    // Acto II: la Ascensión
    FIRST_DEATH(
        "first_death", ChronicleAct.ASCENSION, "Morir un Poco",
        "Ascender se siente como olvidar un nombre. Lo que pierdes vuelve más rápido; lo que ganas, la Cordura, ya no se puede gastar en ser normal.",
        AchievementStat.ASCENSIONS, 1
    ),
    PACTS(
        "pacts", ChronicleAct.ASCENSION, "Los Primeros Pactos",
        "Dos dioses distintos te ofrecieron algo, y dijiste que sí a los dos. Ninguno te preguntó por el otro. Eso te preocupa más que el precio.",
        AchievementStat.PACTS_LIVED, 2
    ),
    RUNES(
        "runes", ChronicleAct.ASCENSION, "Lo que Dicen las Runas",
        "Cortaste la primera runa del aire. Ardía en un idioma que nunca estudiaste y que, sin embargo, sabías leer.",
        AchievementStat.RUNES, 1
    ),
    TRIALS(
        "trials", ChronicleAct.ASCENSION, "Las Pruebas de Locura",
        "Te ataste las manos, te quemaste el grimorio, te prohibiste dormir. Y aun así seguiste. La locura, descubres, también se entrena.",
        AchievementStat.TRIALS_COMPLETED, 1
    ),

    // Acto III: la Trascendencia
    ETHER(
        "ether", ChronicleAct.TRANSCENDENCE, "Más Allá de la Cordura",
        "Trascender fue dejar caer la Cordura como quien suelta un abrigo mojado. Debajo había Éter, frío y luminoso, y debajo del Éter, estrellas.",
        AchievementStat.TRANSCENDENCES, 1
    ),
    SKY_MAP(
        "sky_map", ChronicleAct.TRANSCENDENCE, "El Mapa del Cielo",
        "Trazaste líneas entre estrellas que no deberían estar ahí. Las líneas, cuando las miras de reojo, siguen trazándose solas.",
        AchievementStat.STAR_LEVELS, 1
    ),
    DEEP_SLEEP(
        "deep_sleep", ChronicleAct.TRANSCENDENCE, "Dormir para Ver",
        "Bajaste los setecientos escalones del Sueño Profundo. En el fondo había una puerta, y en la puerta, tu nombre tallado hace siglos.",
        AchievementStat.DREAMS_COMPLETED, 1
    ),
    CYCLE_TRIALS(
        "cycle_trials", ChronicleAct.TRANSCENDENCE, "Desafíos del Ciclo",
        "Superaste un ciclo entero con una regla imposible. Los dioses ya no te miran con lástima: te miran con cálculo.",
        AchievementStat.TTRIAL_COMPLETIONS, 1
    ),

    // Acto IV: el Despertar
    EYES(
        "eyes", ChronicleAct.AWAKENING, "Abrir los Ojos",
        "Despertaste, y entendiste que todo lo anterior había sido un sueño dentro de otro. Las Visiones no muestran el futuro: muestran lo que estaba ahí desde siempre.",
        AchievementStat.AWAKENINGS, 1
    ),
    PLANETS(
        "planets", ChronicleAct.AWAKENING, "Signos en los Planetas",
        "Grabaste el primer Signo Antiguo y un planeta lejano cambió de órbita para mirarte mejor.",
        AchievementStat.SIGNS_FOUND, 1
    ),
    ELEMENTS(
        "elements", ChronicleAct.AWAKENING, "Fuego, Viento, Agua, Tierra",
        "Invocaste al primero de los cuatro. Los otros tres escucharon, y desde entonces esperan su turno sin paciencia.",
        AchievementStat.ELEMENTAL_SWITCHES, 1
    ),
    YELLOW_BOOK(
        "yellow_book", ChronicleAct.AWAKENING, "El Libro Amarillo",
        "Completaste la primera Carta. El libro amarillo, que no tiene título, ahora tiene un marcador puesto en una página que no leíste.",
        AchievementStat.CARDS_DONE, 1
    ),

    // Acto V: el Caos
    UNMASKED(
        "unmasked", ChronicleAct.CHAOS, "El Rey sin Máscara",
        "Las ocho Cartas sobre la mesa forman el Signo Amarillo. El Rey se quita la máscara, y detrás no hay rostro: hay una puerta hacia abajo.",
        AchievementStat.CARDS_DONE, 8
    ),
    FALL(
        "fall", ChronicleAct.CHAOS, "La Caída",
        "Descendiste al sueño de Azathoth. Lo que construiste en el Despertar se deshizo, salvo lo que ya era tuyo de verdad.",
        AchievementStat.DESCENTS, 1
    ),
    THRONE(
        "throne", ChronicleAct.CHAOS, "Junto al Trono",
        "El árbol del Caos echó cuatro ramas. Desde allí ya se oyen las flautas sin filtro, y casi se distingue la melodía.",
        AchievementStat.CHAOS_NODES, 4
    ),
    EYE(
        "eye", ChronicleAct.CHAOS, "El Ojo",
        "Abriste el Ojo. Esta es la última página de la Crónica, y está escrita con tu letra. Pero la tinta todavía está fresca.",
        AchievementStat.AZATHOTH_AWAKENINGS, 1
    );

    companion object {
        fun fromId(id: String): ChronicleChapter? = entries.firstOrNull { it.id == id }
    }
}

/**
 * El Apéndice de la Crónica (v0.26.1): contenido oculto. Páginas que no cuentan para el
 * final ni se anuncian; aparecen al cerrar las historias de los eventos con memoria.
 */
enum class ChronicleAppendix(val id: String, val title: String, val text: String, val memories: List<String>) {
    ULTHAR(
        "ulthar", "Los Gatos de Ulthar",
        "Los gatos de Ulthar no olvidan a quien les dio de comer. Hay un asiento con tu nombre en su consejo, en la cara oculta de la luna, y una ley nueva: a ti tampoco se te puede hacer daño.",
        listOf(RandomEventManager.CATS_END)
    ),
    ULTHAR_LAW(
        "ulthar_law", "La Ley de Ulthar",
        "En Ulthar no se puede matar a un gato. Tú no mataste a ninguno; solo espantaste a uno. Desde entonces, cada gato del mundo te mira un segundo de más, por si acaso.",
        listOf(RandomEventManager.CATS_BITTER)
    ),
    WHISPERER(
        "whisperer", "El que Susurra en la Oscuridad",
        "Las cartas del profesor Wilmarth terminaron en un cilindro que zumba. Los Mi-Go no se llevaron su cuerpo: se llevaron su mente, y la guardaron para que siguiera escribiendo. Algún día, quizás, te escriba a ti.",
        listOf(RandomEventManager.LETTERS_END)
    ),
    DEEP(
        "deep", "Bajo el Arrecife",
        "El huésped empapado era un Profundo, y su gente no olvida una puerta abierta ni una cerrada. En Y'ha-nthlei guardan para ti un lugar junto a los que vuelven al mar.",
        listOf(RandomEventManager.GUEST_END)
    ),
    UNTOLD(
        "untold", "Lo que No Se Contó",
        "Un gato, una mente en un cilindro y un hombre del mar vieron toda tu historia desde afuera. Esta es su versión: no eras tú quien soñaba, ni Azathoth. Eran ellos, los testigos, quienes te soñaban a ti, para que alguien llegara al final.",
        listOf(RandomEventManager.WITNESSES)
    );

    /** Si las memorias ya escribieron esta página. */
    fun isWritten(memories: Set<String>): Boolean = memories.any { it in this.memories }
}

object Chronicle {
    /** Los capítulos que [value] ya permite escribir y que aún no están en [written]. */
    fun newChapters(written: Set<String>, value: (String) -> BigDecimal): List<ChronicleChapter> =
        ChronicleChapter.entries.filter { it.id !in written && value(it.stat) >= BigDecimal(it.target) }

    fun chaptersOf(act: ChronicleAct): List<ChronicleChapter> = ChronicleChapter.entries.filter { it.act == act }

    /** Un acto se completa con todos sus capítulos escritos. */
    fun isActComplete(act: ChronicleAct, written: Set<String>): Boolean = chaptersOf(act).all { it.id in written }

    /** Un acto se ve desde que tiene un capítulo escrito. */
    fun isActStarted(act: ChronicleAct, written: Set<String>): Boolean = chaptersOf(act).any { it.id in written }

    /** Las páginas del Apéndice que las memorias ya escribieron. */
    fun appendix(memories: Set<String>): List<ChronicleAppendix> = ChronicleAppendix.entries.filter { it.isWritten(memories) }

    /** Fracción de la Crónica escrita: el camino hacia el final. */
    fun progress(written: Set<String>): Float =
        ChronicleChapter.entries.count { it.id in written }.toFloat() / ChronicleChapter.entries.size
}
