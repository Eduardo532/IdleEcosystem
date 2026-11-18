package com.EGomez.idleecosystem.domain.logic

/** Lo que da para siempre una Carta del Rey de Amarillo completada. */
enum class CardBonus(val description: String) {
    CLICK("Poder de clic x100."),
    PRODUCTION("Producción x10."),
    DREAM("Puntos de Sueño x3."),
    SANITY("Cordura ganada x10."),
    STARDUST("Polvo Estelar x5."),
    ETHER("Éter ganado x5."),
    VISIONS("Visiones ganadas +50 %."),
    EXPONENT("La producción se eleva a ^1,05.")
}

/**
 * Las Cartas del Rey de Amarillo (v0.24.0): desafíos de la tercera capa. Se elige una
 * Carta para el próximo Despertar; durante todo ese Despertar rigen sus reglas, que son
 * Juramentos combinados. Si al Despertar otra vez se hicieron las Trascendencias que pide,
 * se completa: da Fragmentos Amarillos y, la primera vez, su bono para siempre y un
 * capítulo de la historia del acto final.
 */
enum class YellowCard(
    val id: String,
    val numeral: String,
    val title: String,
    /** Los Juramentos que impone, por su id ("m_hunger", "t_silence"). */
    val oathIds: List<String>,
    /** Trascendencias que hay que hacer en el Despertar de la Carta. */
    val goal: Int,
    val bonus: CardBonus,
    /** Fragmentos Amarillos de la primera vez; las siguientes, la mitad. */
    val fragments: Int,
    val chapter: String
) {
    FOOL(
        "fool", "0", "El Loco", listOf("m_bound_hands", "t_silence"), 12, CardBonus.CLICK, 5,
        "Encontraste el libro en una librería que no estaba ayer. La tapa es amarilla y no tiene título. En la primera página alguien escribió tu nombre con tu letra."
    ),
    TOWER(
        "tower", "XVI", "La Torre", listOf("m_hunger", "m_burned_grimoire"), 12, CardBonus.PRODUCTION, 5,
        "El primer acto de «El Rey de Amarillo» es inofensivo, dicen todos. Lo leíste de pie, en la tienda. Cuando levantaste la vista, la ciudad tenía una torre de más."
    ),
    MOON(
        "moon", "XVIII", "La Luna", listOf("m_insomnia", "t_broken"), 13, CardBonus.DREAM, 6,
        "Ya no duermes. Cuando cierras los ojos ves el lago de Hali, y sobre él dos soles y una luna negra que te mira con atención."
    ),
    SUN(
        "sun", "XIX", "El Sol", listOf("m_blindness", "m_silence"), 13, CardBonus.SANITY, 6,
        "Las sombras se alargan en el mediodía de Carcosa. Aldebarán brilla a pleno día. Los pocos que pasan por la calle llevan máscaras pálidas, y no te saludan."
    ),
    STAR(
        "star", "XVII", "La Estrella", listOf("t_hunger", "m_amnesia"), 14, CardBonus.STARDUST, 8,
        "Olvidaste qué logros te trajeron aquí. Solo recuerdas las Híades, extrañas estrellas, y una canción que Cassilda cantaba antes de que existiera el canto."
    ),
    HANGED(
        "hanged", "XII", "El Colgado", listOf("m_monomania", "t_solitude"), 14, CardBonus.ETHER, 8,
        "Ningún dios contesta. Te quedan tres oficios y una idea fija. Visto al revés, el Signo Amarillo dibuja tu silueta colgada de un pie."
    ),
    DEATH(
        "death", "XIII", "La Muerte", listOf("m_mortality", "m_delirium"), 15, CardBonus.VISIONS, 10,
        "En el segundo acto la gente del teatro se quita las máscaras. Todos menos el Extraño. «No llevo máscara», dice. Entonces entiendes por qué nadie vuelve del segundo acto."
    ),
    KING(
        "king", "XXII", "El Rey de Amarillo", listOf("m_hunger", "t_hunger", "t_silence", "m_delirium"), 16, CardBonus.EXPONENT, 20,
        "Sus harapos no son harapos. Su corona no es corona. Te tiende la mano y en la palma está el Signo, y detrás del Signo, muy lejos, una flauta suena en el centro de todo. Azathoth sueña. Y en su sueño, por primera vez, alguien lo está mirando."
    );

    /** Los Juramentos que impone. */
    val oaths: List<Oath> get() = oathIds.mapNotNull { Oaths.fromId(it) }

    companion object {
        fun fromId(id: String?): YellowCard? = entries.firstOrNull { it.id == id }
    }
}

object YellowCards {
    const val UNLOCK_AWAKENINGS = 5

    /** Desde el quinto Despertar, si ya se invocó alguna vez a los cuatro Elementales. */
    fun isUnlocked(awakenings: Int, allElementalsSummoned: Boolean): Boolean =
        awakenings >= UNLOCK_AWAKENINGS && allElementalsSummoned

    /** El Rey de Amarillo pide las otras siete; las demás están siempre disponibles. */
    fun isAvailable(card: YellowCard, completed: Set<YellowCard>): Boolean =
        card != YellowCard.KING || YellowCard.entries.filter { it != YellowCard.KING }.all { it in completed }

    fun isComplete(card: YellowCard, transcendences: Int): Boolean = transcendences >= card.goal

    /** Fragmentos de una Carta: todos la primera vez, la mitad las siguientes. */
    fun fragmentsFor(card: YellowCard, firstTime: Boolean): Int = if (firstTime) card.fragments else card.fragments / 2

    /** El factor de un bono si su Carta está completada; si no, 1. */
    fun factor(bonus: CardBonus, completed: Set<YellowCard>): Double {
        if (completed.none { it.bonus == bonus }) return 1.0
        return when (bonus) {
            CardBonus.CLICK -> 100.0
            CardBonus.PRODUCTION -> 10.0
            CardBonus.DREAM -> 3.0
            CardBonus.SANITY -> 10.0
            CardBonus.STARDUST -> 5.0
            CardBonus.ETHER -> 5.0
            CardBonus.VISIONS -> 1.5
            CardBonus.EXPONENT -> 1.05
        }
    }

    /** Los Juramentos que rigen: los jurados más los de la Carta en juego, sin repetir. */
    fun ruleOaths(sworn: List<Oath>, card: YellowCard?): List<Oath> =
        (sworn + (card?.oaths ?: emptyList())).distinctBy { it.id }
}
