package com.EGomez.idleecosystem.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ElderSignsTest {

    @Test
    fun `a sign survives packing with every quality and level`() {
        SignKind.entries.forEach { kind ->
            SignRarity.entries.forEach { rarity ->
                listOf(1, 50, 99, 100).forEach { quality ->
                    listOf(1, 7, ElderSigns.MAX_LEVEL).forEach { level ->
                        val sign = ElderSign(kind, rarity, quality, level)
                        assertEquals(sign, ElderSign.unpack(sign.pack()))
                    }
                }
            }
        }
    }

    @Test
    fun `rolls cover every rarity with the expected weights`() {
        val random = Random(42)
        val rolls = List(20_000) { ElderSigns.roll(random) }
        assertTrue(rolls.all { it.quality in 1..100 && it.level == 1 })
        val share = rolls.groupingBy { it.rarity }.eachCount()
        assertEquals(0.5, share.getValue(SignRarity.COMMON) / 20_000.0, 0.02)
        assertEquals(0.01, (share[SignRarity.MYTHIC] ?: 0) / 20_000.0, 0.005)
        assertEquals(SignKind.entries.toSet(), rolls.map { it.kind }.toSet())
    }

    @Test
    fun `bonus grows with rarity, quality and level`() {
        val base = ElderSign(SignKind.FLAME, SignRarity.COMMON, 50)
        assertEquals(2.0, base.bonus(), 1e-12)
        assertEquals(4.0, base.copy(rarity = SignRarity.RARE).bonus(), 1e-12)
        assertEquals(3.0, base.copy(quality = 100).bonus(), 1e-12)
        assertEquals(4.0, base.copy(level = 3).bonus(), 1e-12)
    }

    @Test
    fun `a planet doubles the sign of its affinity`() {
        val flame = ElderSign(SignKind.FLAME, SignRarity.COMMON, 50)
        assertEquals(5.0, flame.multiplier(Planet.YUGGOTH), 1e-12)
        assertEquals(3.0, flame.multiplier(Planet.SATURN), 1e-12)
        val equipped = mapOf(Planet.YUGGOTH to flame, Planet.SATURN to flame)
        assertEquals(15.0, ElderSigns.multiplier(SignKind.FLAME, equipped), 1e-12)
        assertEquals(1.0, ElderSigns.multiplier(SignKind.EYE, equipped), 1e-12)
    }

    @Test
    fun `planets open with awakenings`() {
        assertEquals(listOf(Planet.YUGGOTH), Planet.entries.filter { it.isOpen(1) })
        assertEquals(Planet.entries, Planet.entries.filter { it.isOpen(5) })
    }

    @Test
    fun `upgrading costs more each level and selling pays for rarity and investment`() {
        val sign = ElderSign(SignKind.HAND, SignRarity.EPIC, 100)
        assertEquals(40, sign.upgradeCost())
        assertEquals(80, sign.copy(level = 2).upgradeCost())
        assertEquals(30, sign.dust())
        assertEquals(90, sign.copy(level = 3).dust())
        assertTrue(ElderSign(SignKind.HAND, SignRarity.COMMON, 1).dust() >= 1)
    }
}
