package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.PactEffects
import com.EGomez.idleecosystem.domain.model.Trial
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.random.Random

/**
 * El Bestiario (v0.19.10): 312 criaturas de los Mitos en 8 Dominios, que se consiguen
 * **invocándolas** (gachapón) con **Sellos Antiguos**, una moneda que nunca se reinicia ni
 * se cambia por nada. Una criatura nueva llega a nivel 1; repetida sube de nivel; ya al
 * máximo da Fragmentos, con los que se puede elegir una criatura que falte. Cada criatura
 * suma un bono del tipo de su Dominio, y completar un Dominio lo duplica.
 */
enum class BeastRarity(
    val title: String,
    /** Peso al invocar (sobre 10.000). */
    val weight: Int,
    val bonusPerLevel: Double,
    /** Fragmentos que da una repetida cuando ya está al nivel máximo. */
    val shards: Int,
    /** Fragmentos que cuesta elegir una criatura de esta rareza que falte. */
    val shardCost: Int,
    /** Sellos que da un encuentro de esta rareza. */
    val encounterSeals: Int,
    val color: Long
) {
    COMMON("Común", 5500, 0.01, 1, 30, 1, 0xFFB0BEC5),
    UNCOMMON("Infrecuente", 2700, 0.02, 2, 60, 1, 0xFF81C784),
    RARE("Rara", 1200, 0.04, 4, 120, 2, 0xFF64B5F6),
    MYTHIC("Mítica", 450, 0.08, 10, 300, 3, 0xFFBA68C8),
    PRIMORDIAL("Primigenia", 150, 0.15, 25, 750, 5, 0xFFFFB74D)
}

/** Lo que suman las criaturas de un Dominio. */
enum class BeastKind {
    PRODUCTION, CLICK, SANITY, OFFLINE, ETHER, STARDUST, SEALS, ENCOUNTER
}

data class BeastDomain(
    val id: String,
    val title: String,
    val description: String,
    val kind: BeastKind,
    val color: Long
)

data class Beast(
    val id: String,
    val name: String,
    val lore: String,
    val domainId: String,
    val rarity: BeastRarity,
    /** Receta del dibujo: arquetipo y rasgos (ver CreatureArt). */
    val art: String
)

/** Lo que salió en una invocación. */
data class SummonResult(
    val beastId: String,
    val isNew: Boolean,
    val level: Int,
    /** Fragmentos ganados (solo si ya estaba al máximo). */
    val shards: Int
)

object Bestiary {
    const val MAX_LEVEL = 10

    /** Sellos por una invocación y por diez (la décima sale gratis). */
    const val SUMMON_COST = 10
    const val SUMMON_TEN_COST = 90

    /** Garantías: una Mítica o mejor cada 40 invocaciones, una Primigenia cada 120. */
    const val PITY_MYTHIC = 40
    const val PITY_PRIMORDIAL = 120

    /** Completar un Dominio duplica lo que dan sus criaturas. */
    const val DOMAIN_COMPLETE_FACTOR = 2.0

    /** Topes: los Sellos se multiplican como mucho x3 y los encuentros llegan como mucho al doble de seguido. */
    const val MAX_SEALS_BONUS = 3.0
    const val MAX_ENCOUNTER_BONUS = 2.0

    // ---------------------------------------------------------- fuentes de Sellos
    /** Probabilidad de encontrar un Sello en cada clic (menor que la de un Eco). */
    const val CLICK_SEAL_CHANCE = 0.0005

    /** Comprar Sellos con Conocimiento: 5 Sellos por compra, a un precio que nunca baja. */
    const val SEALS_PER_PURCHASE = 5
    private const val PURCHASE_BASE = 1e5
    private const val PURCHASE_GROWTH = 2.2

    /** Sellos por logro (los secretos dan más). */
    const val SEALS_PER_ACHIEVEMENT = 2
    const val SEALS_PER_SECRET = 10

    /** Ofrenda diaria: 5 Sellos, uno más por día seguido (hasta 15). */
    const val DAILY_BASE = 5
    const val DAILY_MAX = 15

    /** Un encuentro aparece cada 90 a 240 segundos de juego y dura 45. */
    const val MIN_INTERVAL = 90
    const val MAX_INTERVAL = 240
    const val ENCOUNTER_SECONDS = 45

    val domains: List<BeastDomain> get() = BeastCatalog.domains
    val beasts: List<Beast> get() = BeastCatalog.beasts

    private val byId: Map<String, Beast> by lazy { beasts.associateBy { it.id } }
    private val domainById: Map<String, BeastDomain> by lazy { domains.associateBy { it.id } }
    private val byRarity: Map<BeastRarity, List<Beast>> by lazy { beasts.groupBy { it.rarity } }

    fun beast(id: String): Beast? = byId[id]
    fun domain(id: String): BeastDomain? = domainById[id]
    fun domainOf(beast: Beast): BeastDomain = domainById.getValue(beast.domainId)
    fun beastsOf(domainId: String): List<Beast> = beasts.filter { it.domainId == domainId }

    // ---------------------------------------------------------- Sellos
    /** Conocimiento que cuesta la compra número [purchases] + 1 de Sellos. */
    fun sealPrice(purchases: Int): Double = ceil(PURCHASE_BASE * PURCHASE_GROWTH.pow(purchases))

    /** Sellos por superar una Prueba: más las difíciles, x3 la primera vez. */
    fun trialSeals(trial: Trial, firstTime: Boolean): Int = (5 + 2 * trial.ordinal) * if (firstTime) 3 else 1

    /** Sellos por Ascender: 1, y 5 más cada 10 Ascensiones. */
    fun ascensionSeals(totalAscensions: Int): Int = 1 + if (totalAscensions > 0 && totalAscensions % 10 == 0) 5 else 0

    /** Sellos de la ofrenda diaria con [streak] días seguidos (1 el primer día). */
    fun dailySeals(streak: Int): Int = (DAILY_BASE + (streak - 1).coerceAtLeast(0)).coerceAtMost(DAILY_MAX)

    // ---------------------------------------------------------- invocar
    /**
     * La rareza de una invocación. [sinceMythic] y [sincePrimordial] son las invocaciones
     * seguidas sin una Mítica (o mejor) y sin una Primigenia: al llegar a la garantía, sale.
     */
    fun rollRarity(sinceMythic: Int, sincePrimordial: Int, random: Random = Random): BeastRarity {
        if (sincePrimordial + 1 >= PITY_PRIMORDIAL) return BeastRarity.PRIMORDIAL
        val pool = if (sinceMythic + 1 >= PITY_MYTHIC) listOf(BeastRarity.MYTHIC, BeastRarity.PRIMORDIAL) else BeastRarity.entries
        var roll = random.nextInt(pool.sumOf { it.weight })
        pool.forEach { rarity ->
            roll -= rarity.weight
            if (roll < 0) return rarity
        }
        return pool.last()
    }

    /** Una criatura al azar de esa rareza. */
    fun pick(rarity: BeastRarity, random: Random = Random): Beast {
        val pool = byRarity.getValue(rarity)
        return pool[random.nextInt(pool.size)]
    }

    /** Lo que pasa al recibir una criatura con [level] actual: nueva, sube o da Fragmentos. */
    fun receive(beast: Beast, level: Int): SummonResult = when {
        level <= 0 -> SummonResult(beast.id, isNew = true, level = 1, shards = 0)
        level < MAX_LEVEL -> SummonResult(beast.id, isNew = false, level = level + 1, shards = 0)
        else -> SummonResult(beast.id, isNew = false, level = MAX_LEVEL, shards = beast.rarity.shards)
    }

    /** Lo que cuesta elegir con Fragmentos una criatura que todavía no se tiene. */
    fun shardCost(beast: Beast): Int = beast.rarity.shardCost

    // ---------------------------------------------------------- bonos
    fun isDomainComplete(domainId: String, levels: Map<String, Int>): Boolean =
        beastsOf(domainId).all { (levels[it.id] ?: 0) > 0 }

    /** Lo que suma cada tipo: 1 + Σ bono·nivel, con los Dominios completos al doble. */
    fun bonuses(levels: Map<String, Int>): Map<BeastKind, Double> {
        val sums = BeastKind.entries.associateWith { 0.0 }.toMutableMap()
        domains.forEach { domain ->
            val factor = if (isDomainComplete(domain.id, levels)) DOMAIN_COMPLETE_FACTOR else 1.0
            val sum = beastsOf(domain.id).sumOf { (levels[it.id] ?: 0) * it.rarity.bonusPerLevel }
            sums[domain.kind] = sums.getValue(domain.kind) + sum * factor
        }
        return sums.mapValues { (kind, value) ->
            val total = 1.0 + value
            when (kind) {
                BeastKind.SEALS -> total.coerceAtMost(MAX_SEALS_BONUS)
                BeastKind.ENCOUNTER -> total.coerceAtMost(MAX_ENCOUNTER_BONUS)
                else -> total
            }
        }
    }

    fun effects(levels: Map<String, Int>): PactEffects = effectsOf(bonuses(levels))

    /** Los bonos que entran por los efectos de pacto: producción, clic, sin conexión y Cordura. */
    fun effectsOf(b: Map<BeastKind, Double>): PactEffects = PactEffects(
        production = b.getValue(BeastKind.PRODUCTION),
        click = b.getValue(BeastKind.CLICK),
        offline = b.getValue(BeastKind.OFFLINE),
        sanityGain = b.getValue(BeastKind.SANITY)
    )

    // ---------------------------------------------------------- encuentros
    /** Segundos hasta el próximo encuentro: los Cielos lo acercan (hasta la mitad). */
    fun nextInterval(encounterBonus: Double, random: Random = Random): Int {
        val base = random.nextInt(MIN_INTERVAL, MAX_INTERVAL + 1)
        return (base / encounterBonus.coerceIn(1.0, MAX_ENCOUNTER_BONUS)).toInt().coerceAtLeast(30)
    }

    /** La criatura que asoma en un encuentro (solo se ve su silueta): pesa su rareza. */
    fun rollEncounter(random: Random = Random): Beast = pick(rollRarity(0, 0, random), random)
}
