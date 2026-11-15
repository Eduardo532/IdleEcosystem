package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.core.math.BigMath
import java.math.BigDecimal
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/**
 * Una Reliquia (v0.21.3): se compra con Oro de Expedición cuando la mejor Expedición llegó
 * a [unlockLevel]. Sus niveles valen para siempre.
 */
enum class Relic(
    val id: String,
    val title: String,
    val description: String,
    val unlockLevel: Int,
    val baseCost: Double,
    val costGrowth: Double,
    val maxLevel: Int
) {
    TRAPEZOHEDRON("trapezohedron", "El Trapezoedro Resplandeciente", "Producción x5 por nivel.", 10, 10.0, 2.0, 10),
    CROWN("crown", "La Corona de Yig", "Éter ganado +50 % por nivel.", 25, 25.0, 2.0, 10),
    MIRROR("mirror", "El Espejo de Nitocris", "Daño de Expedición x2 y Visiones +10 % por nivel.", 50, 50.0, 2.5, 5);

    fun cost(level: Int): Int = ceil(baseCost * costGrowth.pow(level)).toInt()

    fun isMaxed(level: Int) = level >= maxLevel

    fun isUnlocked(bestLevel: Int) = bestLevel >= unlockLevel

    companion object {
        fun fromId(id: String): Relic? = entries.firstOrNull { it.id == id }
    }
}

/**
 * Las Expediciones (v0.21.3), inspiradas en el minijuego de ataque de Revolution Idle.
 * Desde el primer Despertar, un círculo golpea horrores cada segundo con un daño que
 * crece con la producción. Cada horror vencido sube el nivel, que no tiene tope; al
 * Despertar, el nivel paga Oro y vuelve a cero. El Oro compra Reliquias.
 */
object Expeditions {
    /** Vida del horror de nivel [level]: 100 × 1,25^nivel. */
    fun health(level: Int): Double = 100.0 * 1.25.pow(level)

    /**
     * Daño por segundo: (1 + log10(1 + producción))² × bono. Con 1e30 por segundo son
     * unos 960; la vida de los horrores crece más rápido, así que el nivel se frena solo.
     */
    fun damagePerSecond(production: BigDecimal, multiplier: Double = 1.0): Double {
        val digits = if (production.signum() <= 0) 0.0 else BigMath.ln(production.add(BigDecimal.ONE)) / Math.log(10.0)
        return (1.0 + digits).pow(2) * multiplier
    }

    /** Lo que hace un golpe a mano: una décima del daño por segundo. */
    const val TAP_SHARE = 0.1

    /** Tope de horrores vencidos de una vez (para que el tiempo sin conexión no se cuelgue). */
    const val MAX_KILLS = 10_000

    /**
     * Aplica [damage] al horror de [level] que ya recibió [dealt]. Devuelve el nivel nuevo,
     * el daño que lleva el horror actual y cuántos se vencieron.
     */
    fun strike(level: Int, dealt: Double, damage: Double): Triple<Int, Double, Int> {
        var lvl = level
        var acc = dealt + damage
        var kills = 0
        while (kills < MAX_KILLS && acc >= health(lvl)) {
            acc -= health(lvl)
            lvl++
            kills++
        }
        return Triple(lvl, acc, kills)
    }

    /** Oro al Despertar con la Expedición en [level]: ⌊nivel^1,3⌋. */
    fun goldFor(level: Int): Int = if (level <= 0) 0 else floor(level.toDouble().pow(1.3)).toInt()

    /** El horror de cada nivel: una criatura del Bestiario, siempre la misma para ese nivel. */
    fun horror(level: Int): Beast = Bestiary.beasts[(level * 37) % Bestiary.beasts.size]

    fun productionMultiplier(trapezohedron: Int): Double = 5.0.pow(trapezohedron)
    fun etherMultiplier(crown: Int): Double = 1.0 + 0.5 * crown
    fun damageMultiplier(mirror: Int): Double = 2.0.pow(mirror)
    fun visionMultiplier(mirror: Int): Double = 1.0 + 0.1 * mirror
}
