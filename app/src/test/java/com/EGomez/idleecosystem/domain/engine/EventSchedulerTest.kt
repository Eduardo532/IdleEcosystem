package com.EGomez.idleecosystem.domain.engine

import com.EGomez.idleecosystem.domain.logic.RandomEventManager
import com.EGomez.idleecosystem.domain.model.EffectType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.math.BigDecimal
import kotlin.random.Random

class EventSchedulerTest {

    private val knowledge = BigDecimal(1000)
    private val scheduler = EventScheduler(RandomEventManager())

    @Test
    fun `no event appears before its scheduled time`() {
        scheduler.scheduleNext(nowMillis = 0, random = Random(42))

        assertNull(scheduler.tick(EventScheduler.MIN_DELAY_MS - 1, knowledge, BigDecimal.ZERO))
        assertNotNull(scheduler.tick(EventScheduler.MAX_DELAY_MS, knowledge, BigDecimal.ZERO))
        assertNotNull(scheduler.state.value.currentEvent)
    }

    @Test
    fun `an event on screen is never replaced`() {
        val first = scheduler.trigger(0, knowledge, BigDecimal.ZERO)
        assertNotNull(first)

        // Runa de Visión con un evento abierto
        assertNull(scheduler.trigger(0, knowledge, BigDecimal.ZERO))
        assertSame(first, scheduler.state.value.currentEvent)
    }

    @Test
    fun `a second choice on the same event is ignored`() {
        scheduler.trigger(0, knowledge, BigDecimal.ZERO)

        assertNotNull(scheduler.choose(0, nowMillis = 0))
        assertNull(scheduler.choose(0, nowMillis = 0))
    }

    @Test
    fun `closing an event schedules the next one`() {
        scheduler.trigger(0, knowledge, BigDecimal.ZERO)
        scheduler.choose(0, nowMillis = 0)

        scheduler.close(nowMillis = 1_000)

        assertNull(scheduler.state.value.currentEvent)
        assertNull(scheduler.state.value.resultText)
        assertNull(scheduler.tick(1_000 + EventScheduler.MIN_DELAY_MS - 1, knowledge, BigDecimal.ZERO))
    }

    @Test
    fun `a global buff lasts its duration and then expires`() {
        val (optionIndex, effect) = showEventWithGlobalBuff()

        scheduler.choose(optionIndex, nowMillis = 0)
        assertEquals(effect.value, scheduler.state.value.buffMultiplier, 0.0)
        assertEquals(effect.durationSeconds, scheduler.state.value.buffSecondsRemaining)
        assertEquals(effect.value, scheduler.buffMultiplier, 0.0)

        scheduler.close(nowMillis = 0)
        scheduler.tick(effect.durationSeconds * 1000L, knowledge, BigDecimal.ZERO)
        assertEquals(1.0, scheduler.state.value.buffMultiplier, 0.0)
        assertEquals(0, scheduler.state.value.buffSecondsRemaining)
    }

    @Test
    fun `clearing the buff resets it at once`() {
        val (optionIndex, _) = showEventWithGlobalBuff()
        scheduler.choose(optionIndex, nowMillis = 0)

        scheduler.clearBuff()

        assertEquals(1.0, scheduler.buffMultiplier, 0.0)
        assertEquals(0, scheduler.state.value.buffSecondsRemaining)
    }

    @Test
    fun `choosing a chain option makes its follow-up the next event, soon`() {
        val sanity = BigDecimal.ONE
        var start = scheduler.trigger(0, knowledge, sanity)!!
        var attempts = 0
        while (start.id != "evt_chain_book_1") {
            scheduler.close(nowMillis = 0)
            start = scheduler.trigger(0, knowledge, sanity)!!
            check(++attempts < 5_000) { "El libro sin título nunca apareció" }
        }
        val chainOption = start.options.indexOfFirst { it.followUpEventId != null }

        scheduler.choose(chainOption, nowMillis = 0)
        scheduler.close(nowMillis = 0, random = Random(1))

        assertNull(scheduler.tick(EventScheduler.FOLLOW_UP_MIN_DELAY_MS - 1, knowledge, sanity))
        val next = scheduler.tick(EventScheduler.FOLLOW_UP_MAX_DELAY_MS, knowledge, sanity)
        assertEquals("evt_chain_book_2", next?.id)
    }

    @Test
    fun `an option without a link does not continue the chain`() {
        val sanity = BigDecimal.ONE
        var start = scheduler.trigger(0, knowledge, sanity)!!
        var attempts = 0
        while (start.id != "evt_chain_book_1") {
            scheduler.close(nowMillis = 0)
            start = scheduler.trigger(0, knowledge, sanity)!!
            check(++attempts < 5_000)
        }
        val endOption = start.options.indexOfFirst { it.followUpEventId == null }

        scheduler.choose(endOption, nowMillis = 0)
        scheduler.close(nowMillis = 0)

        // Vuelve a la espera normal de 90 s o más
        assertNull(scheduler.tick(EventScheduler.MIN_DELAY_MS - 1, knowledge, sanity))
    }

    /** Los eventos son aleatorios: se muestran hasta dar con uno que tenga potenciador. */
    private fun showEventWithGlobalBuff(): Pair<Int, com.EGomez.idleecosystem.domain.model.EventEffect> {
        repeat(2_000) { attempt ->
            val sanity = if (attempt % 2 == 0) BigDecimal.ZERO else BigDecimal(100)
            val event = scheduler.trigger(0, knowledge, sanity)!!
            event.options.forEachIndexed { index, option ->
                option.effects.firstOrNull { it.type == EffectType.GLOBAL_BUFF }?.let { return index to it }
            }
            scheduler.close(nowMillis = 0)
        }
        throw AssertionError("Ningún evento del catálogo tiene un potenciador global")
    }
}
