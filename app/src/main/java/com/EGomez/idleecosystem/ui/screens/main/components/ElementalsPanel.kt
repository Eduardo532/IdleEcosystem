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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.Elemental
import com.EGomez.idleecosystem.domain.logic.ElementalNode
import com.EGomez.idleecosystem.domain.logic.Elementals
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import com.EGomez.idleecosystem.ui.i18n.tr
import java.util.Locale
import kotlin.math.floor

/** Lo que la pestaña Elementales necesita del motor (v0.23.0). */
data class ElementalsUi(
    val active: String? = null,
    val favor: Map<String, Double> = emptyMap(),
    val levels: Map<String, Int> = emptyMap(),
    val favorPerSecond: Double = 0.0
)

/** El color de cada Elemental. */
fun Elemental.color(): Color = when (this) {
    Elemental.CTHUGHA -> Color(0xFFFF7043)
    Elemental.ITHAQUA -> Color(0xFF80DEEA)
    Elemental.CTHULHU -> Color(0xFF26A69A)
    Elemental.NYARLATHOTEP -> Color(0xFFD4A15A)
}

private fun favorText(value: Double): String = floor(value + 1e-9).toLong().toString()

/**
 * La pestaña Elementales (capa Despertar): los cuatro Elementales, el invocado y su Favor,
 * y el árbol del que se está mirando. Tocar una tarjeta muestra su árbol; INVOCAR lo activa.
 */
@Composable
fun ElementalsPanel(ui: ElementalsUi, onSummon: (String?) -> Unit, onBuyNode: (String) -> Unit) {
    var viewing by remember { mutableStateOf(Elemental.fromId(ui.active) ?: Elemental.CTHUGHA) }
    val gold = Color(0xFFE0C080)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(ELEMENTALS_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1A1714),
            border = BorderStroke(1.dp, gold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.elementals_title), color = gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.elementals_hint), color = Color.LightGray, fontSize = 11.sp)
                Text(
                    Elemental.fromId(ui.active)?.let {
                        stringResource(R.string.elementals_rate, tr(it.title), "%.2f".format(Locale.US, ui.favorPerSecond * 60))
                    } ?: stringResource(R.string.elementals_none),
                    color = Elemental.fromId(ui.active)?.color() ?: Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp).testTag(ELEMENTALS_RATE_TAG)
                )
            }
        }
        // Las cuatro tarjetas, de a dos
        Elemental.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                row.forEach { e -> ElementalCard(e, ui, e == viewing, Modifier.weight(1f)) { viewing = e } }
            }
        }
        // El árbol del que se mira
        val isActive = ui.active == viewing.id
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.elementals_tree, tr(viewing.title)),
                    color = viewing.color(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.elementals_favor, favorText(ui.favor[viewing.id] ?: 0.0)),
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.testTag(ELEMENTAL_FAVOR_TAG + viewing.id)
                )
            }
            if (isActive) {
                OutlinedButton(
                    onClick = { onSummon(null) },
                    border = BorderStroke(1.dp, viewing.color().copy(alpha = 0.6f)),
                    modifier = Modifier.testTag(ELEMENTAL_DISMISS_TAG)
                ) {
                    Text(stringResource(R.string.elementals_dismiss), color = viewing.color(), fontSize = 11.sp)
                }
            } else {
                Button(
                    onClick = { onSummon(viewing.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = viewing.color(), contentColor = Color.Black),
                    modifier = Modifier.testTag(ELEMENTAL_SUMMON_TAG + viewing.id)
                ) {
                    Text(stringResource(R.string.elementals_summon), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (!isActive) {
            Text(stringResource(R.string.elementals_dormant), color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        }
        viewing.nodes.forEach { node -> ElementalNodeRow(node, ui.levels, ui.favor[viewing.id] ?: 0.0, onBuyNode) }
    }
}

@Composable
private fun ElementalCard(e: Elemental, ui: ElementalsUi, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val active = ui.active == e.id
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (active) e.color().copy(alpha = 0.18f) else Color(0xFF15130F),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected || active) e.color() else Color(0xFF2E2A22)),
        modifier = modifier.testTag(ELEMENTAL_CARD_TAG + e.id)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(tr(e.title), color = e.color(), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(tr(e.element), color = Color.LightGray, fontSize = 11.sp)
            Text(
                stringResource(R.string.elementals_favor, favorText(ui.favor[e.id] ?: 0.0)),
                color = Color.White,
                fontSize = 11.sp
            )
            if (active) Text(stringResource(R.string.elementals_active), color = e.color(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ElementalNodeRow(node: ElementalNode, levels: Map<String, Int>, favor: Double, onBuy: (String) -> Unit) {
    val level = levels[node.id] ?: 0
    val required = node.required
    val locked = required != null && (levels[required.id] ?: 0) == 0
    val color = node.elemental.color()
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF15130F),
        border = BorderStroke(1.dp, if (level > 0) color.copy(alpha = 0.6f) else Color(0xFF2E2A22)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(ELEMENTAL_NODE_TAG + node.id)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                IconLabel(
                    if (locked) GameIcons.Lock else GameIcons.ElderSeal,
                    tr(node.title) + " · " + stringResource(R.string.lab_level, level, node.maxLevel),
                    color = if (locked) Color.Gray else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(tr(node.description), color = Color.LightGray, fontSize = 11.sp)
                if (locked) Text(stringResource(R.string.dream_requires, tr(required!!.title)), color = Color.Gray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (node.isMaxed(level)) {
                Text(stringResource(R.string.eternal_maxed), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = { onBuy(node.id) },
                    enabled = Elementals.canBuy(node, levels, favor),
                    colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(ELEMENTAL_BUY_TAG + node.id)
                ) {
                    Text(stringResource(R.string.elementals_cost, node.cost(level)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

const val ELEMENTALS_PANEL_TAG = "elementals_panel"
const val ELEMENTALS_RATE_TAG = "elementals_rate"
const val ELEMENTAL_CARD_TAG = "elemental_card_"
const val ELEMENTAL_FAVOR_TAG = "elemental_favor_"
const val ELEMENTAL_SUMMON_TAG = "elemental_summon_"
const val ELEMENTAL_DISMISS_TAG = "elemental_dismiss"
const val ELEMENTAL_NODE_TAG = "elemental_node_"
const val ELEMENTAL_BUY_TAG = "elemental_buy_"
