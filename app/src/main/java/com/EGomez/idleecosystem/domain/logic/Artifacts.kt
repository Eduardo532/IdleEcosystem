package com.EGomez.idleecosystem.domain.logic

import kotlin.math.ceil
import kotlin.math.pow

/**
 * Los Artefactos (v0.24.1): objetos del acto final que se compran con Fragmentos
 * Amarillos. Valen para siempre y aceleran sistemas concretos.
 */
enum class Artifact(
    val id: String,
    val title: String,
    val description: String,
    val baseCost: Double,
    val costGrowth: Double,
    val maxLevel: Int
) {
    LAMP("lamp", "La Lámpara de Alhazred", "Favor de los Elementales x1,5 por nivel.", 5.0, 1.6, 5),
    SILVER_KEY("silver_key", "La Llave de Plata", "Puntos de Sueño x2 por nivel.", 5.0, 1.6, 5),
    HAMMER("hammer", "El Martillo de los Mi-Go", "La Mina de Cristales va x1,5 más rápido por nivel.", 6.0, 1.7, 5),
    ASTROLABE("astrolabe", "El Astrolabio de Celaeno", "Polvo Estelar x2 por nivel.", 6.0, 1.7, 5),
    DAGGER("dagger", "La Daga de Hali", "Daño de Expedición x3 por nivel.", 8.0, 1.8, 5),
    VEIL("veil", "El Velo de Cassilda", "Visiones ganadas +10 % por nivel.", 10.0, 2.0, 5),
    BROOCH("brooch", "El Broche del Signo", "Fragmentos Amarillos de las Cartas +25 % por nivel.", 15.0, 2.0, 4);

    /** Fragmentos que cuesta el próximo nivel. */
    fun cost(level: Int): Int = ceil(baseCost * costGrowth.pow(level) - 1e-9).toInt()

    fun isMaxed(level: Int): Boolean = level >= maxLevel

    companion object {
        fun fromId(id: String): Artifact? = entries.firstOrNull { it.id == id }
    }
}

object Artifacts {
    fun canBuy(artifact: Artifact, level: Int, fragments: Int): Boolean =
        !artifact.isMaxed(level) && fragments >= artifact.cost(level)

    fun favorMultiplier(lamp: Int): Double = 1.5.pow(lamp)
    fun dreamMultiplier(key: Int): Double = 2.0.pow(key)
    fun mineSpeed(hammer: Int): Double = 1.5.pow(hammer)
    fun stardustMultiplier(astrolabe: Int): Double = 2.0.pow(astrolabe)
    fun damageMultiplier(dagger: Int): Double = 3.0.pow(dagger)
    fun visionMultiplier(veil: Int): Double = 1.0 + 0.1 * veil

    /** Fragmentos de una Carta con el Broche del Signo: ⌊base × (1 + 0,25 × nivel)⌋. */
    fun fragments(base: Int, brooch: Int): Int = kotlin.math.floor(base * (1.0 + 0.25 * brooch) + 1e-9).toInt()
}
