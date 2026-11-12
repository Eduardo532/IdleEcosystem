package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.Beast
import com.EGomez.idleecosystem.domain.logic.BeastFamily
import com.EGomez.idleecosystem.domain.logic.BeastKind
import com.EGomez.idleecosystem.domain.logic.BeastRarity
import com.EGomez.idleecosystem.domain.logic.Bestiary
import com.EGomez.idleecosystem.ui.i18n.tr
import kotlin.math.roundToInt

/** Color de la Esencia y del Bestiario. */
val EssenceTeal = Color(0xFF80CBC4)

/** Lo que muestra la pestaña Bestiario (v0.19.8). */
data class BestiaryUi(
    val levels: Map<String, Int> = emptyMap(),
    val essence: Int = 0,
    val bonuses: Map<BeastKind, Double> = emptyMap(),
    val encounterId: String? = null,
    val encounterSecondsLeft: Int = 0
)

fun BeastRarity.uiColor(): Color = Color(color)

@Composable
fun BeastRarity.label(): String = tr(title)

@Composable
fun BeastKind.effectText(percent: Int): String = stringResource(
    when (this) {
        BeastKind.PRODUCTION -> R.string.beast_kind_production
        BeastKind.CLICK -> R.string.beast_kind_click
        BeastKind.SANITY -> R.string.beast_kind_sanity
        BeastKind.OFFLINE -> R.string.beast_kind_offline
        BeastKind.ETHER -> R.string.beast_kind_ether
        BeastKind.STARDUST -> R.string.beast_kind_stardust
        BeastKind.ESSENCE -> R.string.beast_kind_essence
        BeastKind.ENCOUNTER -> R.string.beast_kind_encounter
    },
    percent
)

/**
 * La pestaña Bestiario (capa Ascensión): cuántas criaturas hay descubiertas, la Esencia,
 * lo que suman y una tarjeta por familia con una casilla por criatura. Tocar una casilla
 * descubierta abre su ficha.
 */
@Composable
fun BestiaryPanel(state: BestiaryUi, onUpgrade: (String) -> Unit) {
    var selected by remember { mutableStateOf<String?>(null) }
    val seen = state.levels.count { it.value > 0 }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(BESTIARY_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF101A19),
            border = BorderStroke(1.dp, EssenceTeal.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.bestiary_title), color = EssenceTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$seen / ${Bestiary.beasts.size}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.essence_title), color = EssenceTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("❖ ${state.essence}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                val active = state.bonuses.filter { it.value > 1.0001 }.map { (kind, value) -> kind.effectText(((value - 1.0) * 100).roundToInt()) }
                Text(
                    if (active.isEmpty()) stringResource(R.string.bestiary_hint) else active.joinToString(" · "),
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }
        Bestiary.families.forEach { family ->
            FamilyCard(family, state.levels) { selected = it }
        }
    }
    selected?.let { id ->
        Bestiary.beast(id)?.let { beast ->
            BeastDialog(beast, state.levels[id] ?: 0, state.essence, onUpgrade = { onUpgrade(id) }, onDismiss = { selected = null })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FamilyCard(family: BeastFamily, levels: Map<String, Int>, onSelect: (String) -> Unit) {
    val beasts = Bestiary.beastsOf(family.id)
    val seen = beasts.count { (levels[it.id] ?: 0) > 0 }
    val complete = seen == beasts.size
    val accent = Color(family.color)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF161616),
        border = BorderStroke(1.dp, accent.copy(alpha = if (complete) 0.9f else 0.35f)),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(FAMILY_CARD_TAG + family.id)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(family.glyph + " " + tr(family.title), color = accent, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text(
                    if (complete) stringResource(R.string.bestiary_family_complete) else "$seen/${beasts.size}",
                    color = if (complete) accent else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(tr(family.description), color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 6,
                modifier = Modifier.fillMaxWidth()
            ) {
                beasts.forEach { beast ->
                    BeastTile(beast, family, levels[beast.id] ?: 0, Modifier.weight(1f)) { onSelect(beast.id) }
                }
            }
        }
    }
}

@Composable
private fun BeastTile(beast: Beast, family: BeastFamily, level: Int, modifier: Modifier, onClick: () -> Unit) {
    val known = level > 0
    val rarity = beast.rarity.uiColor()
    Surface(
        onClick = onClick,
        enabled = known,
        shape = RoundedCornerShape(8.dp),
        color = if (known) Color(0xFF202020) else Color(0xFF121212),
        border = BorderStroke(1.dp, if (known) rarity else rarity.copy(alpha = 0.25f)),
        modifier = modifier.aspectRatio(1f).testTag(BEAST_TILE_TAG + beast.id)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(if (known) family.glyph else "?", color = if (known) Color.White else Color.DarkGray, fontSize = 16.sp)
            if (known) {
                Text(
                    "$level",
                    color = rarity,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 3.dp, bottom = 1.dp)
                )
            }
        }
    }
}

/** La ficha de una criatura: rareza, familia, historia, lo que da y subir de nivel. */
@Composable
fun BeastDialog(beast: Beast, level: Int, essence: Int, onUpgrade: () -> Unit, onDismiss: () -> Unit) {
    val family = Bestiary.familyOf(beast)
    val maxed = level >= Bestiary.MAX_LEVEL
    val cost = Bestiary.upgradeCost(beast, level)
    val per = (beast.rarity.bonusPerLevel * 100).roundToInt()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        modifier = Modifier.testTag(BEAST_DIALOG_TAG),
        title = {
            Column {
                Text(family.glyph + " " + tr(beast.name), color = Color.White, fontWeight = FontWeight.Bold)
                Text(beast.rarity.label() + " · " + tr(family.title), color = beast.rarity.uiColor(), fontSize = 12.sp)
            }
        },
        text = {
            Column {
                Text("“" + tr(beast.lore) + "”", color = Color.LightGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(R.string.beast_level, level, Bestiary.MAX_LEVEL), color = EssenceTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(family.kind.effectText(per * level), color = Color.White, fontSize = 12.sp)
                Text(stringResource(R.string.beast_per_level, family.kind.effectText(per)), color = Color.Gray, fontSize = 11.sp)
            }
        },
        confirmButton = {
            if (maxed) {
                Text(stringResource(R.string.eternal_maxed), color = EssenceTeal, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
            } else {
                Button(
                    onClick = onUpgrade,
                    enabled = essence >= cost,
                    colors = ButtonDefaults.buttonColors(containerColor = EssenceTeal, contentColor = Color.Black),
                    modifier = Modifier.testTag(BEAST_UPGRADE_TAG)
                ) { Text(stringResource(R.string.beast_upgrade, cost), fontWeight = FontWeight.Bold) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = Color.Gray) } }
    )
}

/**
 * El encuentro, flotando arriba de la pantalla: qué criatura acecha (o ??? si no se
 * conoce), su rareza, cuánto falta para que se vaya y el botón para catalogarla.
 */
@Composable
fun EncounterBanner(beastId: String, secondsLeft: Int, known: Boolean, onCatch: () -> Unit, modifier: Modifier = Modifier) {
    val beast = Bestiary.beast(beastId) ?: return
    val family = Bestiary.familyOf(beast)
    val rarity = beast.rarity.uiColor()
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xEE101418),
        border = BorderStroke(1.5.dp, rarity),
        shadowElevation = 8.dp,
        modifier = modifier.padding(horizontal = 24.dp).testTag(ENCOUNTER_TAG)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(family.glyph, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (known) tr(beast.name) else stringResource(R.string.encounter_unknown),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(beast.rarity.label() + " · " + stringResource(R.string.encounter_left, secondsLeft), color = rarity, fontSize = 11.sp)
            }
            Button(
                onClick = onCatch,
                colors = ButtonDefaults.buttonColors(containerColor = rarity, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag(ENCOUNTER_CATCH_TAG)
            ) {
                Text(stringResource(R.string.encounter_catch), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

const val BESTIARY_PANEL_TAG = "bestiary_panel"
const val FAMILY_CARD_TAG = "beast_family_"
const val BEAST_TILE_TAG = "beast_tile_"
const val BEAST_DIALOG_TAG = "beast_dialog"
const val BEAST_UPGRADE_TAG = "beast_upgrade"
const val ENCOUNTER_TAG = "encounter"
const val ENCOUNTER_CATCH_TAG = "encounter_catch"
