package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.PactEffects
import com.EGomez.idleecosystem.domain.model.PactTier
import com.EGomez.idleecosystem.ui.components.PactSigil
import com.EGomez.idleecosystem.ui.components.sigilColor
import com.EGomez.idleecosystem.ui.i18n.tr
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * La galería del Panteón: todos los dioses por rango. Los que ya se pueden elegir
 * muestran su sigilo y su nombre; los demás, una silueta con cuántas Ascensiones
 * faltan. Tocar uno desbloqueado abre su ficha.
 */
@Composable
fun PantheonGallery(ascensions: Int, activePact: Pact, onPactSelected: (Pact) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(stringResource(R.string.pantheon_intro), color = Color.Gray, fontSize = 12.sp)
        listOf(PactTier.NONE, PactTier.GREAT_OLD_ONE, PactTier.OUTER_GOD).forEach { tier ->
            Text(
                stringResource(tierTitle(tier)),
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            Pact.entries.filter { it.tier == tier }.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    row.forEach { pact ->
                        PactCard(
                            pact = pact,
                            unlocked = pact.isUnlocked(ascensions + 1),
                            active = pact == activePact,
                            onClick = { onPactSelected(pact) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun PactCard(pact: Pact, unlocked: Boolean, active: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val color = pact.sigilColor()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF151515),
        border = BorderStroke(if (active) 2.dp else 1.dp, if (active) color else if (unlocked) color.copy(alpha = 0.35f) else Color(0xFF333333)),
        modifier = modifier
            .clickable(enabled = unlocked, onClick = onClick)
            .testTag("$PACT_CARD_TAG${pact.id}")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
            PactSigil(pact, Modifier.size(56.dp), locked = !unlocked)
            Spacer(modifier = Modifier.height(4.dp))
            if (unlocked) {
                Text(
                    tr(pact.godName),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                if (active) Text(stringResource(R.string.pact_active_short), color = color, fontSize = 10.sp)
            } else {
                Text("???", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.pact_unlocks_at, pact.unlockAscensions),
                    color = Color.Gray,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun tierTitle(tier: PactTier): Int = when (tier) {
    PactTier.NONE -> R.string.pact_tier_none
    PactTier.GREAT_OLD_ONE -> R.string.pact_tier_great_old_ones
    PactTier.OUTER_GOD -> R.string.pact_tier_outer_gods
}

/** Una línea de efecto de un pacto: el texto y si favorece al jugador. */
data class PactLine(val text: String, val good: Boolean)

/** Las ventajas y desventajas de un pacto, en el idioma del juego. */
@Composable
fun pactLines(effects: PactEffects, buildingNames: Map<String, String>): List<PactLine> {
    val resources = LocalContext.current.resources
    val translatedNames = buildingNames.mapValues { (_, name) -> tr(name) }
    return remember(effects, translatedNames, resources) {
        buildPactLines(effects, translatedNames) { id, args -> resources.getString(id, *args) }
    }
}

/** Arma las líneas con [text] (un getString) para poder probarlas sin interfaz. */
fun buildPactLines(
    effects: PactEffects,
    buildingNames: Map<String, String>,
    text: (Int, Array<out Any>) -> String
): List<PactLine> {
    val lines = mutableListOf<PactLine>()
    fun add(value: Double, res: Int, higherIsBetter: Boolean = true) {
        if (value != 1.0) lines += PactLine(text(res, arrayOf(multiplier(value))), (value > 1.0) == higherIsBetter)
    }
    if (effects.chaos) {
        lines += PactLine(
            text(R.string.pact_effect_chaos, arrayOf(PactEffects.CHAOS_MIN, PactEffects.CHAOS_MAX, PactEffects.CHAOS_PERIOD_SECONDS)),
            true
        )
    }
    add(effects.production, R.string.pact_effect_production)
    add(effects.click, R.string.pact_effect_click)
    if (effects.offline == 0.0) lines += PactLine(text(R.string.pact_effect_no_offline, emptyArray()), false)
    else add(effects.offline, R.string.pact_effect_offline)
    effects.buildings.entries.groupBy({ it.value }, { it.key }).forEach { (value, ids) ->
        val names = ids.joinToString(", ") { buildingNames[it] ?: it }
        lines += PactLine(text(R.string.pact_effect_buildings, arrayOf(names, multiplier(value))), value > 1.0)
    }
    add(effects.buildingCost, R.string.pact_effect_building_cost, higherIsBetter = false)
    add(effects.runeFrequency, R.string.pact_effect_rune_frequency)
    add(effects.eventFrequency, R.string.pact_effect_event_frequency)
    add(effects.eventBuffPower, R.string.pact_effect_event_power)
    add(effects.ritualDuration, R.string.pact_effect_ritual_duration)
    add(effects.ritualCooldown, R.string.pact_effect_ritual_cooldown, higherIsBetter = false)
    add(effects.ritualCost, R.string.pact_effect_ritual_cost, higherIsBetter = false)
    add(effects.echoChance, R.string.pact_effect_echo_chance)
    add(effects.sanityGain, R.string.pact_effect_sanity)
    return lines.sortedByDescending { it.good }
}

/** "3", "0.5", "1.25" */
fun multiplier(value: Double): String = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US)).format(value)

/** Las ventajas en verde y las desventajas en rojo. */
@Composable
fun PactEffectList(lines: List<PactLine>, fontSize: Int = 12) {
    Column {
        lines.forEach { line ->
            Text(
                (if (line.good) "+ " else "− ") + line.text,
                color = if (line.good) Color(0xFF81C784) else Color(0xFFE57373),
                fontSize = fontSize.sp
            )
        }
    }
}

const val PACT_CARD_TAG = "pact_card_"
