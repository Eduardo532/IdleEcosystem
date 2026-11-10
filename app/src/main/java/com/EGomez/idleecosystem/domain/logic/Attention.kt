package com.EGomez.idleecosystem.domain.logic

/**
 * La Atención: puntos que no se gastan, se reparten. Cada punto mirando algo le da
 * +[BONUS_PER_POINT] de su efecto, y se puede mover gratis en cualquier momento.
 * (Inspirada en la Energía de NGU Idle y los oficios de Kittens Game.)
 *
 * Se puede mirar un edificio (su producción; en la Sinapsis, el clic), el [DREAM]
 * (progreso sin conexión) o la [VIGIL] (frecuencia de las runas). Los puntos crecen
 * con las Ascensiones: 1 con la primera y uno más cada 3, hasta [MAX_POINTS].
 */
object Attention {

    const val DREAM = "dream"
    const val VIGIL = "vigil"
    const val BONUS_PER_POINT = 0.25
    const val MAX_POINTS = 10
    private const val ASCENSIONS_PER_POINT = 3

    /** Puntos de Atención con [ascensions] Ascensiones. */
    fun available(ascensions: Int): Int =
        if (ascensions <= 0) 0 else minOf(MAX_POINTS, 1 + ascensions / ASCENSIONS_PER_POINT)

    /** Ascensiones totales con las que llega el próximo punto, o null si ya están todos. */
    fun nextPointAt(ascensions: Int): Int? {
        val current = available(ascensions)
        if (current >= MAX_POINTS) return null
        return if (current == 0) 1 else current * ASCENSIONS_PER_POINT
    }

    /** El multiplicador de algo que tiene [points] puntos de Atención. */
    fun multiplier(points: Int): Double = 1.0 + BONUS_PER_POINT * points.coerceAtLeast(0)

    /**
     * Deja un reparto válido: sin ceros ni negativos y sin pasar de [available]. Si
     * sobran puntos (por ejemplo tras un reinicio total), se quitan del final.
     */
    fun clamp(allocation: Map<String, Int>, available: Int): Map<String, Int> {
        var left = available.coerceAtLeast(0)
        val result = linkedMapOf<String, Int>()
        allocation.forEach { (target, points) ->
            val kept = points.coerceAtLeast(0).coerceAtMost(left)
            if (kept > 0) {
                result[target] = kept
                left -= kept
            }
        }
        return result
    }

    fun used(allocation: Map<String, Int>): Int = allocation.values.sum()

    /** "b_notes:2,vigil:1" */
    fun encode(allocation: Map<String, Int>): String =
        allocation.filterValues { it > 0 }.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun decode(text: String?): Map<String, Int> {
        if (text.isNullOrBlank()) return emptyMap()
        val result = linkedMapOf<String, Int>()
        text.split(",").forEach { part ->
            val (target, points) = part.split(":").takeIf { it.size == 2 } ?: return@forEach
            points.toIntOrNull()?.takeIf { it > 0 }?.let { result[target.trim()] = it }
        }
        return result
    }
}
