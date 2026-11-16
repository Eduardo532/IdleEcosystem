package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import java.math.BigDecimal
import kotlin.math.ceil
import kotlin.math.pow

/**
 * Los Cuatro Elementales (v0.23.0): Cthugha (fuego), Ithaqua (viento), Cthulhu (agua) y
 * Nyarlathotep (tierra). Desde el tercer Despertar se invoca a uno; solo uno está activo
 * a la vez. El activo junta Favor con el tiempo, el Favor compra nodos de su árbol y
 * solo el árbol del activo hace efecto.
 */
enum class Elemental(val id: String, val title: String, val element: String) {
    CTHUGHA("cthugha", "Cthugha", "Fuego"),
    ITHAQUA("ithaqua", "Ithaqua", "Viento"),
    CTHULHU("cthulhu", "Cthulhu", "Agua"),
    NYARLATHOTEP("nyarlathotep", "Nyarlathotep", "Tierra");

    /** Los cuatro nodos de su árbol, en orden. */
    val nodes: List<ElementalNode> get() = ElementalNode.entries.filter { it.elemental == this }

    companion object {
        fun fromId(id: String?): Elemental? = entries.firstOrNull { it.id == id }
    }
}

enum class ElementalNode(
    val elemental: Elemental,
    val key: String,
    val title: String,
    val description: String,
    val baseCost: Double,
    val costGrowth: Double,
    val maxLevel: Int,
    val requires: String? = null
) {
    // Cthugha: el fuego de Fomalhaut, la producción
    EMBER(Elemental.CTHUGHA, "ember", "Brasa de Fomalhaut", "Producción x5 por nivel.", 2.0, 2.0, 10),
    PYRE(Elemental.CTHUGHA, "pyre", "Pira Viviente", "Poder de clic x10 por nivel.", 3.0, 2.0, 5, "ember"),
    BLAZE(Elemental.CTHUGHA, "blaze", "Llamarada", "Daño de Expedición x3 por nivel.", 4.0, 2.5, 5, "ember"),
    STARFIRE(Elemental.CTHUGHA, "starfire", "Fuego Estelar", "La producción se eleva a ^1,02 por nivel.", 20.0, 3.0, 3, "blaze"),

    // Ithaqua: el viento que camina, el tiempo
    GALE(Elemental.ITHAQUA, "gale", "Viento Helado", "Ganancias sin conexión x3 por nivel.", 2.0, 2.0, 10),
    DRIFT(Elemental.ITHAQUA, "drift", "Caminante del Viento", "La Mina de Cristales va x1,5 más rápido por nivel.", 3.0, 2.0, 5, "gale"),
    WHIRL(Elemental.ITHAQUA, "whirl", "Torbellino Estelar", "Polvo Estelar x2 por nivel.", 4.0, 2.5, 5, "gale"),
    NORTH(Elemental.ITHAQUA, "north", "Norte Blanco", "Favor de todos los Elementales x1,5 por nivel.", 20.0, 3.0, 3, "whirl"),

    // Cthulhu: el agua de R'lyeh, el sueño y la Cordura
    TIDE(Elemental.CTHULHU, "tide", "Marea de R'lyeh", "Cordura ganada x5 por nivel.", 2.0, 2.0, 10),
    SUNKEN(Elemental.CTHULHU, "sunken", "Sueño Hundido", "Puntos de Sueño x2 por nivel.", 3.0, 2.0, 5, "tide"),
    ABYSSAL(Elemental.CTHULHU, "abyssal", "Fosa Abisal", "Lustre al Pulir +50 % por nivel.", 4.0, 2.5, 5, "tide"),
    RISING(Elemental.CTHULHU, "rising", "Las Torres de R'lyeh", "Visiones ganadas +20 % por nivel.", 20.0, 3.0, 3, "abyssal"),

    // Nyarlathotep: la tierra negra de Khem, el Éter y el Oro
    STONE(Elemental.NYARLATHOTEP, "stone", "Piedra Negra", "Éter ganado x2 por nivel.", 2.0, 2.0, 10),
    KHEM(Elemental.NYARLATHOTEP, "khem", "Oro de Khem", "Oro de Expedición +25 % por nivel.", 3.0, 2.0, 5, "stone"),
    MASKS(Elemental.NYARLATHOTEP, "masks", "El Hombre Negro", "Esencia de los Mi-Go al Refinar +25 % por nivel.", 4.0, 2.5, 5, "stone"),
    PHARAOH(Elemental.NYARLATHOTEP, "pharaoh", "El Faraón Negro", "Éter x(1 + 0,5 × Despertares) por nivel.", 20.0, 3.0, 3, "masks");

    /** "cthugha.ember": la clave con la que se guarda. */
    val id: String get() = elemental.id + "." + key

    /** Favor que cuesta el próximo nivel. */
    fun cost(level: Int): Int = ceil(baseCost * costGrowth.pow(level)).toInt()

    fun isMaxed(level: Int): Boolean = level >= maxLevel

    /** El nodo que pide, del mismo árbol. */
    val required: ElementalNode? get() = requires?.let { r -> entries.firstOrNull { it.elemental == elemental && it.key == r } }

    companion object {
        fun fromId(id: String): ElementalNode? = entries.firstOrNull { it.id == id }
    }
}

object Elementals {
    const val UNLOCK_AWAKENINGS = 3

    fun isUnlocked(awakenings: Int): Boolean = awakenings >= UNLOCK_AWAKENINGS

    /**
     * Favor por segundo del Elemental activo: (1 + log10(1 + producción)) / 300 × Norte Blanco.
     * Con 1e100 por segundo son unos 20 de Favor por minuto.
     */
    fun favorPerSecond(production: BigDecimal, north: Int = 0): Double {
        val digits = if (production.signum() <= 0) 0.0 else BigMath.ln(production.add(BigDecimal.ONE)) / Math.log(10.0)
        return (1.0 + digits) / 300.0 * 1.5.pow(north)
    }

    /** Si se puede subir [node]: hay Favor, no está al máximo y su requisito tiene un nivel. */
    fun canBuy(node: ElementalNode, levels: Map<String, Int>, favor: Double): Boolean {
        val level = levels[node.id] ?: 0
        if (node.isMaxed(level) || favor < node.cost(level)) return false
        return node.required?.let { (levels[it.id] ?: 0) > 0 } ?: true
    }

    /** El nivel de [node] si su Elemental es el activo; si no, 0 (los árboles dormidos no hacen nada). */
    fun effective(node: ElementalNode, levels: Map<String, Int>, active: Elemental?): Int =
        if (node.elemental == active) levels[node.id] ?: 0 else 0

    // Efectos
    fun productionMultiplier(ember: Int): Double = 5.0.pow(ember)
    fun clickMultiplier(pyre: Int): Double = 10.0.pow(pyre)
    fun damageMultiplier(blaze: Int): Double = 3.0.pow(blaze)
    fun productionExponent(starfire: Int): Double = 1.0 + 0.02 * starfire
    fun offlineMultiplier(gale: Int): Double = 3.0.pow(gale)
    fun mineSpeed(drift: Int): Double = 1.5.pow(drift)
    fun stardustMultiplier(whirl: Int): Double = 2.0.pow(whirl)
    fun sanityMultiplier(tide: Int): Double = 5.0.pow(tide)
    fun dreamMultiplier(sunken: Int): Double = 2.0.pow(sunken)
    fun visionMultiplier(rising: Int): Double = 1.0 + 0.2 * rising
    fun etherMultiplier(stone: Int, pharaoh: Int, awakenings: Int): Double =
        2.0.pow(stone) * (1.0 + 0.5 * awakenings).pow(pharaoh)
    fun goldMultiplier(khem: Int): Double = 1.0 + 0.25 * khem
    fun essenceMultiplier(masks: Int): Double = 1.0 + 0.25 * masks
}
