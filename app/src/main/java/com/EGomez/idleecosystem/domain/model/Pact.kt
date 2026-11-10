package com.EGomez.idleecosystem.domain.model

/** El rango de un dios en el panteón: decide cuándo aparece y cuánto pesa su pacto. */
enum class PactTier { NONE, GREAT_OLD_ONE, OUTER_GOD }

/**
 * Lo que un pacto cambia durante toda una vida. Todo vale 1,0 (sin cambio) salvo que
 * el pacto diga otra cosa; las ventajas y desventajas salen del mito de cada dios.
 */
data class PactEffects(
    /** Producción de todos los edificios. */
    val production: Double = 1.0,
    /** Poder de cada clic. */
    val click: Double = 1.0,
    /** Progreso sin conexión (0 = no hay progreso sin conexión). */
    val offline: Double = 1.0,
    /** Frecuencia de las runas (2 = el doble de seguido). */
    val runeFrequency: Double = 1.0,
    /** Costo en Ecos de los rituales. */
    val ritualCost: Double = 1.0,
    /** Duración de los rituales. */
    val ritualDuration: Double = 1.0,
    /** Recarga de los rituales (0,5 = la mitad de espera). */
    val ritualCooldown: Double = 1.0,
    /** Probabilidad de encontrar un Eco al investigar. */
    val echoChance: Double = 1.0,
    /** Cordura ganada al terminar esta vida. */
    val sanityGain: Double = 1.0,
    /** Frecuencia de los eventos. */
    val eventFrequency: Double = 1.0,
    /** Fuerza de los potenciadores de eventos (2 = un x5 pasa a x9). */
    val eventBuffPower: Double = 1.0,
    /** Costo de los edificios. */
    val buildingCost: Double = 1.0,
    /** Multiplicadores para edificios concretos, por id. */
    val buildings: Map<String, Double> = emptyMap(),
    /** Azathoth: la producción cambia al azar entre x[CHAOS_MIN] y x[CHAOS_MAX]. */
    val chaos: Boolean = false
) {
    fun buildingMultiplier(buildingId: String): Double = buildings[buildingId] ?: 1.0

    /** Un potenciador de evento xM con este pacto. */
    fun eventBuff(multiplier: Double): Double = 1.0 + (multiplier - 1.0) * eventBuffPower

    companion object {
        const val CHAOS_MIN = 1
        const val CHAOS_MAX = 20
        /** Cada cuántos segundos se vuelve a tirar el caos de Azathoth. */
        const val CHAOS_PERIOD_SECONDS = 30
    }
}

/**
 * El panteón: un pacto por vida, elegido al Ascender. "Sin pacto" siempre está; los
 * Grandes Antiguos aparecen a lo largo de las primeras Ascensiones y los Dioses
 * Exteriores, los más poderosos del mito, en el juego tardío.
 *
 * Los textos están en español; [ContentTranslations] tiene su versión en inglés.
 */
enum class Pact(
    val id: String,
    val tier: PactTier,
    /** Ascensiones totales para poder elegirlo (al Ascender por esa vez). */
    val unlockAscensions: Int,
    val godName: String,
    val epithet: String,
    val lore: String,
    val effects: PactEffects,
    /** Color del sigilo (ARGB). */
    val color: Long
) {
    NONE(
        "none", PactTier.NONE, 0,
        "Sin Pacto", "La Mente Propia",
        "Ningún dios reclama tu alma en esta vida. Sin favores ni deudas, lo que comprendes es solo tuyo, y tu Cordura vuelve más entera de cada Ascensión.",
        PactEffects(sanityGain = 1.25),
        0xFFB0BEC5
    ),

    // --- GRANDES ANTIGUOS ---
    CTHULHU(
        "cthulhu", PactTier.GREAT_OLD_ONE, 1,
        "Cthulhu", "El Durmiente de R'lyeh",
        "Yace muerto pero soñando en la ciudad hundida de R'lyeh. Sus sueños se filtran en las mentes sensibles mientras duermen: con su pacto, el culto avanza cuando tú no miras. Pero su letargo pesa sobre el mundo despierto.",
        PactEffects(offline = 3.0, buildings = mapOf("b_temple" to 2.0), runeFrequency = 0.5),
        0xFF2E8B7A
    ),
    HASTUR(
        "hastur", PactTier.GREAT_OLD_ONE, 1,
        "Hastur", "El Rey de Amarillo",
        "El Innombrable reina en Carcosa, junto al lago de Hali. Quien lee la obra que lleva su nombre gana una lucidez terrible y febril, y paga con la razón: la mente que vuelve de Carcosa nunca vuelve entera.",
        PactEffects(production = 2.0, sanityGain = 0.75),
        0xFFE0C341
    ),
    TULZSCHA(
        "tulzscha", PactTier.GREAT_OLD_ONE, 1,
        "Tulzscha", "La Llama Verde",
        "Danza en la corte de Azathoth y arde en los festivales secretos del solsticio. Su fuego verde prolonga cada rito, pero consume los ecos antes de que lleguen a oírse.",
        PactEffects(ritualDuration = 2.0, echoChance = 0.5),
        0xFF5BE37D
    ),
    UBBO_SATHLA(
        "ubbo_sathla", PactTier.GREAT_OLD_ONE, 3,
        "Ubbo-Sathla", "La Fuente Primigenia",
        "La masa informe de la que brotó toda la vida de la Tierra, guardiana de las tablillas de los Dioses Arquetípicos. Bajo su pacto, lo antiguo y simple florece; lo complejo vuelve, poco a poco, a la fuente.",
        PactEffects(
            buildings = mapOf(
                "b_notes" to 5.0, "b_scrolls" to 5.0, "b_artifact" to 5.0, "b_cultist" to 5.0,
                "b_monolith" to 0.5, "b_temple" to 0.5, "b_gate" to 0.5, "b_shoggoth" to 0.5,
                "b_dream" to 0.5, "b_star" to 0.5, "b_oldone" to 0.5, "b_void" to 0.5
            )
        ),
        0xFF8FA37A
    ),
    ABHOTH(
        "abhoth", PactTier.GREAT_OLD_ONE, 3,
        "Abhoth", "El Padre de la Inmundicia",
        "Una masa gris que engendra sin cesar criaturas deformes en las cavernas de N'kai, y devora a muchas de las que pare. Con su pacto todo se multiplica más barato, aunque parte de lo creado vuelve a sus fauces.",
        PactEffects(buildingCost = 0.6, production = 0.8),
        0xFF9E9E9E
    ),
    NUG(
        "nug", PactTier.GREAT_OLD_ONE, 5,
        "Nug", "El Primogénito",
        "Gemelo de Yeb, hijo de la Cabra Negra y, según los textos, padre de Cthulhu. Los cultos lo veneran en ritos multitudinarios: su favor fortalece a los fieles y a sus templos, no a la mano que investiga.",
        PactEffects(
            buildings = mapOf("b_cultist" to 4.0, "b_priest" to 4.0, "b_sanctuary" to 4.0, "b_altar" to 4.0),
            click = 0.5
        ),
        0xFFB0413E
    ),
    YEB(
        "yeb", PactTier.GREAT_OLD_ONE, 5,
        "Yeb", "El de las Nieblas Susurrantes",
        "El otro gemelo, envuelto en nieblas que susurran. Guía la mano del que busca y le acerca los ecos del Vacío, pero su niebla oculta y entorpece el trabajo del culto.",
        PactEffects(click = 4.0, echoChance = 1.5, production = 0.75),
        0xFF7E57C2
    ),
    DAOLOTH(
        "daoloth", PactTier.GREAT_OLD_ONE, 10,
        "Daoloth", "El que Rasga los Velos",
        "Una geometría imposible que ve todas las dimensiones a la vez; los astrólogos lo invocan para ver el pasado y el futuro. Bajo su mirada aparecen runas y presagios por todas partes, pero mirarlo de frente quiebra la mente del culto.",
        PactEffects(runeFrequency = 2.0, eventFrequency = 1.5, production = 0.75),
        0xFF4FC3F7
    ),
    GHROTH(
        "ghroth", PactTier.GREAT_OLD_ONE, 10,
        "Ghroth", "El Heraldo",
        "Un astro errante que recorre el cosmos cantando la Música de las Esferas, el canto que despierta a lo que duerme. Su paso trae prodigios más seguidos y más fuertes, pero su canción también despierta la locura.",
        PactEffects(eventFrequency = 2.0, eventBuffPower = 2.0, sanityGain = 0.8),
        0xFFFF8A50
    ),

    // --- DIOSES EXTERIORES ---
    SHUB_NIGGURATH(
        "shub_niggurath", PactTier.OUTER_GOD, 20,
        "Shub-Niggurath", "La Cabra Negra de los Mil Retoños",
        "La fertilidad monstruosa del cosmos, madre de mil crías. Todo lo que se construye bajo su pacto se multiplica y abunda; a cambio, sus cultos exigen sacrificios mayores y la mano del investigador queda olvidada entre la multitud.",
        PactEffects(production = 3.0, buildingCost = 0.85, ritualCost = 2.0, click = 0.5),
        0xFF4CAF50
    ),
    YOG_SOTHOTH(
        "yog_sothoth", PactTier.OUTER_GOD, 30,
        "Yog-Sothoth", "La Llave y la Puerta",
        "Coexiste con todo el tiempo y todo el espacio: es la puerta, la llave y el guardián del umbral. Bajo su pacto, el tiempo ausente rinde como nunca y los ritos se renuevan antes, pero las runas, atadas al instante, rara vez cruzan.",
        PactEffects(offline = 5.0, ritualCooldown = 0.5, sanityGain = 1.2, runeFrequency = 0.5),
        0xFFFFD54F
    ),
    NYARLATHOTEP(
        "nyarlathotep", PactTier.OUTER_GOD, 40,
        "Nyarlathotep", "El Caos Reptante",
        "El alma y mensajero de los Dioses Exteriores, el único que camina entre los hombres con mil máscaras. Su pacto premia al que está presente: la mano, las runas y los presagios se multiplican, pero lo que no se ve no avanza.",
        PactEffects(click = 10.0, runeFrequency = 2.0, eventFrequency = 1.5, offline = 0.0),
        0xFFD32F2F
    ),
    AZATHOTH(
        "azathoth", PactTier.OUTER_GOD, 50,
        "Azathoth", "El Sultán Demoníaco",
        "El caos nuclear en el centro de todo, ciego e idiota, arrullado por flautas monótonas. Su pacto es una fuerza sin propósito: la producción salta al azar entre x1 y x20 cada medio minuto. Ningún rito lo conmueve, y todos cuestan el doble.",
        PactEffects(chaos = true, sanityGain = 1.5, ritualCost = 2.0),
        0xFFE1BEE7
    );

    fun isUnlocked(totalAscensions: Int) = totalAscensions >= unlockAscensions

    companion object {
        fun fromId(id: String?): Pact = entries.firstOrNull { it.id == id } ?: NONE

        /** Los pactos que se pueden elegir en la Ascensión número [ascensionNumber]. */
        fun available(ascensionNumber: Int): List<Pact> = entries.filter { it.isUnlocked(ascensionNumber) }
    }
}
