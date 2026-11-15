package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.DeepSleep
import com.EGomez.idleecosystem.domain.logic.DreamNode
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import com.EGomez.idleecosystem.ui.i18n.tr
import java.math.BigDecimal
import java.util.Locale

/** Color del Sueño Profundo: azul de luna. */
val DreamBlue = Color(0xFF9FA8DA)

/** Lo que la pestaña Sueño necesita del motor (v0.20.0). */
data class DreamUi(
    val next: Boolean = false,
    val active: Boolean = false,
    val exponent: Double = DeepSleep.BASE_EXPONENT,
    val points: Int = 0,
    val earned: Int = 0,
    val cycleBest: BigDecimal = BigDecimal.ZERO,
    val pending: Int = 0,
    val levels: Map<String, Int> = emptyMap()
)

/** "^0,75" con dos decimales. */
fun dreamExponentLabel(value: Double): String = "^" + "%.2f".format(Locale.US, value)

/**
 * La pestaña Sueño (capa Trascendencia): si el ciclo es un Sueño o si lo será el
 * próximo, los Puntos de Sueño y el Árbol del Sueño.
 */
@Composable
fun DreamPanel(
    ui: DreamUi,
    onSetNext: (Boolean) -> Unit,
    onWake: () -> Unit,
    onBuy: (String) -> Unit,
    onRespec: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(DREAM_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF12131F),
            border = BorderStroke(1.dp, DreamBlue.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.dream_title), color = DreamBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.dream_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.dream_points_label), color = DreamBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${ui.points}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        DreamStatus(ui, onSetNext, onWake)
        Text(
            stringResource(R.string.dream_tree_title),
            color = DreamBlue,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )
        DreamNode.entries.forEach { node -> DreamNodeRow(node, ui.levels, ui.points, onBuy) }
        if (DeepSleep.refund(ui.levels) > 0) {
            OutlinedButton(
                onClick = onRespec,
                border = BorderStroke(1.dp, DreamBlue.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(DREAM_RESPEC_TAG)
            ) {
                Text(stringResource(R.string.dream_respec, DeepSleep.refund(ui.levels)), color = DreamBlue, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DreamStatus(ui: DreamUi, onSetNext: (Boolean) -> Unit, onWake: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (ui.active) Color(0xFF1A1B2E) else Color(0xFF15161F),
        border = BorderStroke(1.dp, if (ui.active) DreamBlue else Color(0xFF2A2C40)),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(DREAM_STATUS_TAG)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (ui.active) {
                IconLabel(
                    GameIcons.Moon,
                    stringResource(R.string.dream_active, dreamExponentLabel(ui.exponent)),
                    color = DreamBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(stringResource(R.string.dream_best, ui.cycleBest.toBeautifulString()), color = Color.LightGray, fontSize = 11.sp)
                Text(
                    stringResource(R.string.dream_pending, ui.pending),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(DREAM_PENDING_TAG)
                )
                if (ui.pending == 0) {
                    Text(stringResource(R.string.dream_pending_hint), color = Color.Gray, fontSize = 11.sp)
                }
                TextButton(onClick = onWake, modifier = Modifier.testTag(DREAM_WAKE_TAG)) {
                    Text(stringResource(R.string.dream_wake), color = Color(0xFFEF9A9A), fontSize = 12.sp)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.dream_next), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.dream_next_hint, dreamExponentLabel(ui.exponent)),
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = ui.next,
                        onCheckedChange = onSetNext,
                        colors = SwitchDefaults.colors(checkedTrackColor = DreamBlue),
                        modifier = Modifier.testTag(DREAM_NEXT_TAG)
                    )
                }
            }
            Text(stringResource(R.string.dream_earned, ui.earned), color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun DreamNodeRow(node: DreamNode, levels: Map<String, Int>, points: Int, onBuy: (String) -> Unit) {
    val level = levels[node.id] ?: 0
    val maxed = node.isMaxed(level)
    val required = node.requires?.let { DreamNode.fromId(it) }
    val locked = required != null && (levels[required.id] ?: 0) == 0
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF14151F),
        border = BorderStroke(1.dp, if (level > 0) DreamBlue.copy(alpha = 0.6f) else Color(0xFF26283A)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(DREAM_NODE_TAG + node.id)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                IconLabel(
                    if (locked) GameIcons.Lock else GameIcons.Moon,
                    tr(node.title) + " · " + stringResource(R.string.lab_level, level, node.maxLevel),
                    color = if (locked) Color.Gray else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(tr(node.description), color = Color.LightGray, fontSize = 11.sp)
                if (locked) Text(stringResource(R.string.dream_requires, tr(required!!.title)), color = Color.Gray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (maxed) {
                Text(stringResource(R.string.eternal_maxed), color = DreamBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = { onBuy(node.id) },
                    enabled = DeepSleep.canBuy(node, levels, points),
                    colors = ButtonDefaults.buttonColors(containerColor = DreamBlue, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(DREAM_BUY_TAG + node.id)
                ) {
                    Text(stringResource(R.string.dream_cost, node.cost(level)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Aviso en Investigación mientras el ciclo es un Sueño. */
@Composable
fun DreamBanner(exponent: Double) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF14152A),
        border = BorderStroke(1.dp, DreamBlue.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).testTag(DREAM_BANNER_TAG)
    ) {
        IconLabel(
            GameIcons.Moon,
            stringResource(R.string.dream_banner, dreamExponentLabel(exponent)),
            color = DreamBlue,
            fontSize = 12.sp,
            modifier = Modifier.padding(10.dp)
        )
    }
}

const val DREAM_PANEL_TAG = "dream_panel"
const val DREAM_STATUS_TAG = "dream_status"
const val DREAM_NEXT_TAG = "dream_next"
const val DREAM_WAKE_TAG = "dream_wake"
const val DREAM_PENDING_TAG = "dream_pending"
const val DREAM_NODE_TAG = "dream_node_"
const val DREAM_BUY_TAG = "dream_buy_"
const val DREAM_RESPEC_TAG = "dream_respec"
const val DREAM_BANNER_TAG = "dream_banner"
