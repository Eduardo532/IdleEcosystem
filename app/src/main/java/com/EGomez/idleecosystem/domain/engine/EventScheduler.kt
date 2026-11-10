package com.EGomez.idleecosystem.domain.engine

import com.EGomez.idleecosystem.domain.logic.RandomEventManager
import com.EGomez.idleecosystem.domain.model.EffectType
import com.EGomez.idleecosystem.domain.model.EventOption
import com.EGomez.idleecosystem.domain.model.RandomEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.random.Random

/** Lo que la UI necesita de los eventos aleatorios. */
data class EventState(
    val currentEvent: RandomEvent? = null,
    val resultText: String? = null,
    val buffMultiplier: Double = 1.0,
    val buffSecondsRemaining: Int = 0
)

/**
 * Cuándo aparece un evento, cuál está en pantalla y el potenciador temporal que dejó.
 *
 * Antes esto vivía repartido en el ViewModel: un segundo bucle con su propio delay,
 * timestamps sueltos y el multiplicador guardado dentro del estado de la UI. Aquí no
 * hay corrutinas ni reloj propio: el motor llama a [tick] una vez por segundo con la
 * hora actual, lo que permite probarlo sin esperar minutos reales.
 */
class EventScheduler @Inject constructor(
    private val randomEventManager: RandomEventManager
) {
    private val _state = MutableStateFlow(EventState())
    val state: StateFlow<EventState> = _state.asStateFlow()

    private var nextEventAtMillis = 0L
    private var buffExpiresAtMillis = 0L

    // Continuación de una cadena elegida en el último evento: será el próximo
    private var pendingFollowUp: RandomEvent? = null

    val buffMultiplier: Double get() = _state.value.buffMultiplier

    /** Frecuencia de los eventos según el pacto (2 = el doble de seguido). */
    var frequencyMultiplier: Double = 1.0

    /** Fuerza de los potenciadores de eventos según el pacto. */
    var buffPower: Double = 1.0

    fun scheduleNext(nowMillis: Long, random: Random = Random) {
        val delay = random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS) / frequencyMultiplier.coerceAtLeast(0.1)
        nextEventAtMillis = nowMillis + delay.toLong()
    }

    /**
     * Avanza un segundo de juego.
     * @return el evento que acaba de aparecer, o null.
     */
    fun tick(nowMillis: Long, knowledge: BigDecimal, sanity: BigDecimal, ascensions: Int = 0): RandomEvent? {
        updateBuff(nowMillis)
        if (_state.value.currentEvent == null && nowMillis >= nextEventAtMillis) {
            return trigger(nowMillis, knowledge, sanity, ascensions)
        }
        return null
    }

    /**
     * Muestra un evento ahora (también lo usa la Runa de Visión).
     * @return el evento mostrado, o null si ya había uno o ninguno cumple su condición.
     */
    fun trigger(nowMillis: Long, knowledge: BigDecimal, sanity: BigDecimal, ascensions: Int = 0): RandomEvent? {
        // La Runa de Visión podía reemplazar un evento que el jugador estaba leyendo
        if (_state.value.currentEvent != null) return null
        // Una cadena abierta tiene prioridad sobre el azar
        val event = pendingFollowUp?.also { pendingFollowUp = null }
            ?: randomEventManager.getRandomEvent(knowledge, sanity, ascensions)
        if (event == null) {
            scheduleNext(nowMillis)
            return null
        }
        _state.update { it.copy(currentEvent = event, resultText = null) }
        return event
    }

    /**
     * Elige una opción del evento en pantalla. El potenciador global se activa aquí;
     * los efectos sobre Conocimiento y Cordura los aplica el motor.
     * @return la opción elegida, o null si no hay evento o ya se eligió una.
     */
    fun choose(optionIndex: Int, nowMillis: Long): EventOption? {
        val current = _state.value
        val event = current.currentEvent ?: return null
        // Un doble toque rápido aplicaba el efecto dos veces antes de mostrar el resultado
        if (current.resultText != null) return null
        val option = event.options.getOrNull(optionIndex) ?: return null

        option.followUpEventId?.let { pendingFollowUp = randomEventManager.getEvent(it) }

        var buff = current.buffMultiplier
        var remaining = current.buffSecondsRemaining
        option.effects.filter { it.type == EffectType.GLOBAL_BUFF }.forEach { effect ->
            buff = 1.0 + (effect.value - 1.0) * buffPower
            remaining = effect.durationSeconds
            buffExpiresAtMillis = nowMillis + effect.durationSeconds * 1000L
        }
        _state.update {
            it.copy(resultText = option.flavorResult, buffMultiplier = buff, buffSecondsRemaining = remaining)
        }
        return option
    }

    /**
     * La continuación que hay que guardar para no perder la cadena al cerrar la app:
     * la que espera turno o, si ya está en pantalla sin responder, esa misma.
     */
    fun followUpToPersist(): String? =
        pendingFollowUp?.id ?: _state.value.currentEvent?.takeIf { it.isFollowUp && _state.value.resultText == null }?.id

    /** Retoma una cadena guardada: la continuación aparece pronto al volver. */
    fun restoreFollowUp(eventId: String, nowMillis: Long, random: Random = Random) {
        val event = randomEventManager.getEvent(eventId)?.takeIf { it.isFollowUp } ?: return
        pendingFollowUp = event
        nextEventAtMillis = nowMillis + random.nextLong(FOLLOW_UP_MIN_DELAY_MS, FOLLOW_UP_MAX_DELAY_MS)
    }

    /** Cierra el evento tras mostrar su resultado y programa el siguiente. */
    fun close(nowMillis: Long, random: Random = Random) {
        _state.update { it.copy(currentEvent = null, resultText = null) }
        if (pendingFollowUp != null) {
            // La historia sigue pronto, no en 2 a 4 minutos
            nextEventAtMillis = nowMillis + random.nextLong(FOLLOW_UP_MIN_DELAY_MS, FOLLOW_UP_MAX_DELAY_MS)
        } else {
            scheduleNext(nowMillis, random)
        }
    }

    /** Ascensión y reinicio: el potenciador temporal no sobrevive. */
    fun clearBuff() {
        buffExpiresAtMillis = 0L
        _state.update { it.copy(buffMultiplier = 1.0, buffSecondsRemaining = 0) }
    }

    private fun updateBuff(nowMillis: Long) {
        val current = _state.value
        if (nowMillis < buffExpiresAtMillis) {
            val remaining = ((buffExpiresAtMillis - nowMillis) / 1000).toInt()
            if (remaining != current.buffSecondsRemaining) {
                _state.update { it.copy(buffSecondsRemaining = remaining) }
            }
        } else if (current.buffMultiplier > 1.0 || current.buffSecondsRemaining != 0) {
            _state.update { it.copy(buffMultiplier = 1.0, buffSecondsRemaining = 0) }
        }
    }

    companion object {
        const val MIN_DELAY_MS = 90_000L
        const val MAX_DELAY_MS = 240_000L
        const val FOLLOW_UP_MIN_DELAY_MS = 20_000L
        const val FOLLOW_UP_MAX_DELAY_MS = 45_000L
    }
}
