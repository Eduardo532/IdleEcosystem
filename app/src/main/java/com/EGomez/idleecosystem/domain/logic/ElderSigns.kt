package com.EGomez.idleecosystem.domain.logic

import kotlin.math.pow
import kotlin.random.Random

/** Lo que potencia un Signo Antiguo. [base] es su bono con rareza común, calidad 100 % y nivel 1. */
enum class SignKind(val id: String, val title: String, val effect: String, val base: Double) {
    FLAME("flame", "Signo de la Llama", "Producción", 2.0),
    HAND("hand", "Signo de la Mano", "Poder de clic", 3.0),
    MIND("mind", "Signo de la Mente", "Cordura ganada", 0.5),
    ETHER("ether", "Signo del Éter", "Éter ganado", 0.25),
    EYE("eye", "Signo del Ojo", "Visiones ganadas", 0.1);

    companion object {
        fun fromIndex(index: Int): SignKind = entries[index.coerceIn(0, entries.lastIndex)]
    }
}

/** La rareza de un Signo: multiplica su bono, su Polvo al venderlo y el costo de subirlo. */
enum class SignRarity(val title: String, val multiplier: Double, val weight: Int) {
    COMMON("Común", 1.0, 50),
    RARE("Raro", 2.0, 30),
    EPIC("Épico", 4.0, 14),
    LEGENDARY("Legendario", 8.0, 5),
    MYTHIC("Mítico", 16.0, 1);

    companion object {
        fun fromIndex(index: Int): SignRarity = entries[index.coerceIn(0, entries.lastIndex)]
    }
}

/**
 * Un planeta donde se equipa un Signo. Se abre con [awakenings] Despertares y duplica el
 * Signo de su [affinity].
 */
enum class Planet(val id: String, val title: String, val awakenings: Int, val affinity: SignKind) {
    YUGGOTH("yuggoth", "Yuggoth", 1, SignKind.FLAME),
    SATURN("saturn", "Saturno", 2, SignKind.MIND),
    XOTH("xoth", "Xoth", 3, SignKind.ETHER),
    KYTHANIL("kythanil", "Kythanil", 5, SignKind.EYE);

    fun isOpen(awakenings: Int) = awakenings >= this.awakenings

    companion object {
        fun fromId(id: String): Planet? = entries.firstOrNull { it.id == id }
    }
}

/** Un Signo Antiguo: tipo, rareza, calidad (1 a 100) y nivel (1 a [ElderSigns.MAX_LEVEL]). */
data class ElderSign(val kind: SignKind, val rarity: SignRarity, val quality: Int, val level: Int = 1) {
    /** Se guarda como un solo número en los contadores: tipo, rareza, calidad − 1 y nivel. */
    fun pack(): Long = kind.ordinal * 100_000L + rarity.ordinal * 10_000L + (quality - 1) * 100L + level

    /** El bono, sin planeta: base × rareza × (0,5 + calidad) × (1 + 0,5 por nivel de más). */
    fun bonus(): Double = kind.base * rarity.multiplier * (0.5 + quality / 100.0) * (1.0 + 0.5 * (level - 1))

    /** El multiplicador que da equipado en [planet]: x2 si es su afinidad. */
    fun multiplier(planet: Planet): Double = 1.0 + bonus() * if (planet.affinity == kind) 2.0 else 1.0

    /** Polvo Astral que cuesta subirlo un nivel. */
    fun upgradeCost(): Int = (10 * rarity.multiplier * 2.0.pow(level - 1)).toInt()

    /** Polvo Astral que da al venderlo: crece con la rareza, la calidad y lo invertido. */
    fun dust(): Int = (5 * rarity.multiplier * (0.5 + quality / 100.0) * level).toInt().coerceAtLeast(1)

    val isMaxed: Boolean get() = level >= ElderSigns.MAX_LEVEL

    companion object {
        fun unpack(value: Long): ElderSign = ElderSign(
            kind = SignKind.fromIndex((value / 100_000L).toInt()),
            rarity = SignRarity.fromIndex(((value / 10_000L) % 10).toInt()),
            quality = ((value / 100L) % 100).toInt() + 1,
            level = (value % 100L).toInt().coerceAtLeast(1)
        )
    }
}

/**
 * Los Signos Antiguos (v0.21.2), inspirados en la astrología de Revolution Idle. Desde el
 * primer Despertar, cada Trascendencia da un Signo (y cada Despertar, tres). Los Signos se
 * equipan en los cuatro planetas, suben de nivel con Polvo Astral y se venden por él.
 */
object ElderSigns {
    const val MAX_LEVEL = 10
    const val INVENTORY = 30
    const val SIGNS_PER_TRANSCEND = 1
    const val SIGNS_PER_AWAKEN = 3

    /** Un Signo al azar: tipo uniforme, rareza por peso y calidad de 1 a 100. */
    fun roll(random: Random): ElderSign {
        val kind = SignKind.entries[random.nextInt(SignKind.entries.size)]
        var pick = random.nextInt(SignRarity.entries.sumOf { it.weight })
        val rarity = SignRarity.entries.first { pick -= it.weight; pick < 0 }
        val quality = random.nextInt(1, 101)
        return ElderSign(kind, rarity, quality)
    }

    /** El multiplicador total de un tipo con los Signos equipados (planeta -> Signo). */
    fun multiplier(kind: SignKind, equipped: Map<Planet, ElderSign>): Double =
        equipped.filter { it.value.kind == kind }.entries.fold(1.0) { acc, (planet, sign) -> acc * sign.multiplier(planet) }
}
