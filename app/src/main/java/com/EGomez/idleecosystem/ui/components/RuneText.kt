package com.EGomez.idleecosystem.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.RuneEffects
import com.EGomez.idleecosystem.domain.logic.RuneEngraving
import com.EGomez.idleecosystem.domain.logic.RuneReward
import com.EGomez.idleecosystem.domain.model.RuneTier
import kotlin.math.roundToInt

/**
 * Lo que da una runa, escrito a partir de sus valores reales (RuneEffects) y de su
 * maestría (v0.19.0). Antes se mostraba la descripción fija del catálogo, que no
 * siempre coincidía con el efecto ni con la duración alargada por la maestría.
 */
@Composable
fun runeEffectText(runeId: String, masteryLevel: Int = 0): String {
    val effect = RuneEffects.forRune(runeId)
    val seconds = (effect.durationMs * RuneEngraving.durationBonus(masteryLevel) / 1000.0).roundToInt()
    val autoSeconds = (effect.effectiveAutoClickMs / 1000.0).roundToInt()
    val buff: String? = when {
        effect.freezesBuffs -> stringResource(R.string.rune_fx_freeze, seconds)
        effect.costReduction > 0 -> stringResource(R.string.rune_fx_costs, (effect.costReduction * 100).roundToInt(), seconds)
        effect.echoChance > 0 -> stringResource(R.string.rune_fx_echo, (effect.echoChance * 100).roundToInt(), seconds)
        effect.multiplier > 1.0 && effect.isAutoClick ->
            stringResource(R.string.rune_fx_prod_auto, multiplierLabel(effect.multiplier), seconds, autoSeconds)
        effect.clickMultiplier > 1.0 && effect.isAutoClick ->
            stringResource(R.string.rune_fx_click_auto, multiplierLabel(effect.clickMultiplier), seconds)
        effect.multiplier > 1.0 -> stringResource(R.string.rune_fx_production, multiplierLabel(effect.multiplier), seconds)
        else -> null
    }
    return when (val reward = effect.reward) {
        is RuneReward.TimeWarp -> stringResource(R.string.rune_fx_time, (reward.seconds / 60).toInt())
        is RuneReward.SanityBonus -> stringResource(R.string.rune_fx_sanity, (reward.percent * 100).roundToInt())
        RuneReward.TriggerRandomEvent -> stringResource(R.string.rune_fx_event)
        is RuneReward.InstantEchoes ->
            if (buff != null) stringResource(R.string.rune_fx_plus_echoes, buff, reward.amount)
            else stringResource(R.string.rune_fx_echoes, reward.amount)
        null -> buff ?: stringResource(R.string.rune_fx_entropy)
    }
}

/** Nivel de la runa y el gesto que pide. */
@Composable
fun runeTierLabel(tier: RuneTier): String = stringResource(
    when (tier) {
        RuneTier.TIER_1_SWIPE -> R.string.rune_tier_common
        RuneTier.TIER_2_HOLD -> R.string.rune_tier_rare
        RuneTier.TIER_3_TRACE -> R.string.rune_tier_mythic
    }
)

/** "10" o "1.5": sin decimales cuando no hacen falta. */
fun multiplierLabel(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else "%.1f".format(java.util.Locale.US, value)

/**
 * El color de la runa, aclarado si es demasiado oscuro para leerse sobre el fondo negro
 * (la Runa de Sombra es casi negra y su nombre desaparecía).
 */
fun readableRuneColor(color: Color): Color =
    if (color.luminance() < 0.25f) lerp(color, Color.White, 0.55f) else color
