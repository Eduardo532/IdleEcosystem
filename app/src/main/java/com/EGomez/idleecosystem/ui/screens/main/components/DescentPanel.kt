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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.AzathothDream
import com.EGomez.idleecosystem.domain.logic.ChaosNode
import com.EGomez.idleecosystem.domain.logic.EndingSummary
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import com.EGomez.idleecosystem.ui.i18n.tr

/** El color del Caos: un magenta que no debería existir. */
val ChaosColor = Color(0xFFE040FB)

/** Lo que la pestaña Descenso necesita del motor (v0.25.0). */
data class DescentUi(
    val chaos: Int = 0,
    val chaosGain: Int = 0,
    val canDescend: Boolean = false,
    val descents: Int = 0,
    val awakeningsSince: Int = 0,
    val required: Int = AzathothDream.REQUIRED_AWAKENINGS,
    val kingCompleted: Boolean = false,
    val levels: Map<String, Int> = emptyMap(),
    /** Despertar a Azathoth (v0.25.1). */
    val azathothAwakenings: Int = 0,
    val eyeCost: Int = AzathothDream.EYE_BASE_COST,
    val canAwakenAzathoth: Boolean = false,
    val ending: EndingSummary? = null
)

/**
 * La pestaña Descenso (capa El Sueño de Azathoth): el Caos, lo que pide Descender, el
 * botón con su confirmación y el árbol del Caos.
 */
@Composable
fun DescentPanel(
    ui: DescentUi,
    onDescend: () -> Unit,
    onBuyNode: (String) -> Unit,
    onAwakenAzathoth: () -> Unit = {},
    onReadEnding: () -> Unit = {}
) {
    var confirming by remember { mutableStateOf(false) }
    var confirmingEye by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(DESCENT_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1A0E1C),
            border = BorderStroke(1.dp, ChaosColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.descent_title), color = ChaosColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.descent_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.descent_chaos), color = ChaosColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${ui.chaos}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag(DESCENT_CHAOS_TAG))
                }
            }
        }
        // Lo que pide Descender
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF140B16),
            border = BorderStroke(1.dp, if (ui.canDescend) ChaosColor else Color(0xFF33203A)),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Requirement(stringResource(R.string.descent_req_king), ui.kingCompleted)
                Requirement(
                    stringResource(R.string.descent_req_awakenings, ui.awakeningsSince, ui.required),
                    ui.awakeningsSince >= ui.required
                )
                if (ui.descents > 0) Text(stringResource(R.string.descent_count, ui.descents), color = Color.Gray, fontSize = 11.sp)
                Button(
                    onClick = { confirming = true },
                    enabled = ui.canDescend && ui.chaosGain > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = ChaosColor, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(DESCEND_TAG)
                ) {
                    Text(
                        if (ui.canDescend) stringResource(R.string.descent_button, ui.chaosGain) else stringResource(R.string.descent_locked),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Text(stringResource(R.string.chaos_tree_title), color = ChaosColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        ChaosNode.entries.forEach { node -> ChaosNodeRow(node, ui.levels, ui.chaos, onBuyNode) }
        // El Ojo de Azathoth: el final (v0.25.1)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0E0610),
            border = BorderStroke(2.dp, if (ui.canAwakenAzathoth || ui.azathothAwakenings > 0) ChaosColor else Color(0xFF33203A)),
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp).testTag(EYE_PANEL_TAG)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.eye_title), color = ChaosColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.eye_hint), color = Color.LightGray, fontSize = 11.sp)
                if (ui.azathothAwakenings > 0) {
                    Text(stringResource(R.string.eye_done, ui.azathothAwakenings), color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                }
                Text(
                    stringResource(R.string.eye_requires, ui.eyeCost),
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Button(
                    onClick = { confirmingEye = true },
                    enabled = ui.canAwakenAzathoth,
                    colors = ButtonDefaults.buttonColors(containerColor = ChaosColor, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(EYE_TAG)
                ) {
                    Text(stringResource(R.string.eye_button, ui.eyeCost), fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                if (ui.ending != null) {
                    TextButton(onClick = onReadEnding, modifier = Modifier.testTag(EYE_READ_TAG)) {
                        Text(stringResource(R.string.eye_read), color = ChaosColor, fontSize = 12.sp)
                    }
                }
            }
        }
    }
    if (confirmingEye) {
        AlertDialog(
            onDismissRequest = { confirmingEye = false },
            title = { Text(stringResource(R.string.eye_confirm_title), color = ChaosColor) },
            text = { Text(stringResource(R.string.eye_confirm_text, ui.eyeCost), color = Color.White, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { confirmingEye = false; onAwakenAzathoth() }, modifier = Modifier.testTag(EYE_CONFIRM_TAG)) {
                    Text(stringResource(R.string.eye_confirm), color = ChaosColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingEye = false }) { Text(stringResource(R.string.cancel), color = Color.Gray) }
            },
            containerColor = Color(0xFF1A0E1C)
        )
    }
    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(R.string.descent_confirm_title), color = ChaosColor) },
            text = {
                Column {
                    Text(stringResource(R.string.descent_confirm_gain, ui.chaosGain), color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stringResource(R.string.descent_confirm_loses), color = Color(0xFFEF9A9A), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stringResource(R.string.descent_confirm_keeps), color = Color(0xFF81C784), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { confirming = false; onDescend() }, modifier = Modifier.testTag(DESCEND_CONFIRM_TAG)) {
                    Text(stringResource(R.string.descent_confirm), color = ChaosColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) { Text(stringResource(R.string.cancel), color = Color.Gray) }
            },
            containerColor = Color(0xFF1A0E1C)
        )
    }
}

@Composable
private fun Requirement(text: String, met: Boolean) {
    Text((if (met) "✓ " else "✗ ") + text, color = if (met) Color(0xFF81C784) else Color.LightGray, fontSize = 12.sp)
}

@Composable
private fun ChaosNodeRow(node: ChaosNode, levels: Map<String, Int>, chaos: Int, onBuy: (String) -> Unit) {
    val level = levels[node.id] ?: 0
    val required = node.requires?.let { ChaosNode.fromId(it) }
    val locked = required != null && (levels[required.id] ?: 0) == 0
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF140B16),
        border = BorderStroke(1.dp, if (level > 0) ChaosColor.copy(alpha = 0.6f) else Color(0xFF33203A)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(CHAOS_NODE_TAG + node.id)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                IconLabel(
                    if (locked) GameIcons.Lock else GameIcons.Eye,
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
                Text(stringResource(R.string.eternal_maxed), color = ChaosColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = { onBuy(node.id) },
                    enabled = AzathothDream.canBuy(node, levels, chaos),
                    colors = ButtonDefaults.buttonColors(containerColor = ChaosColor, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(CHAOS_BUY_TAG + node.id)
                ) {
                    Text(stringResource(R.string.chaos_cost, node.cost(level)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * El final (v0.25.1): el epílogo y el resumen de la partida, a pantalla completa.
 * "Seguir soñando" lo cierra y el juego continúa.
 */
@Composable
fun EndingDialog(summary: EndingSummary, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(color = Color(0xFF07030A), modifier = Modifier.fillMaxSize().testTag(ENDING_TAG)) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.ending_title), color = ChaosColor, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(20.dp))
                AzathothDream.EPILOGUE.forEach { paragraph ->
                    Text(tr(paragraph), color = Color(0xFFEDE0F0), fontSize = 15.sp, fontStyle = FontStyle.Italic, modifier = Modifier.padding(bottom = 14.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(R.string.ending_summary), color = ChaosColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val hours = summary.playSeconds / 3600
                val minutes = (summary.playSeconds % 3600) / 60
                listOf(
                    stringResource(R.string.ending_time, hours, minutes),
                    stringResource(R.string.ending_ascensions, summary.ascensions),
                    stringResource(R.string.ending_transcendences, summary.transcendences),
                    stringResource(R.string.ending_awakenings, summary.awakenings),
                    stringResource(R.string.ending_descents, summary.descents),
                    stringResource(R.string.ending_cards, summary.cardsCompleted),
                    stringResource(R.string.ending_achievements, summary.achievementsUnlocked, summary.achievementsTotal)
                ).forEach { Text(it, color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp)) }
                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.ending_thanks), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ChaosColor, contentColor = Color.Black),
                    modifier = Modifier.testTag(ENDING_CONTINUE_TAG)
                ) {
                    Text(stringResource(R.string.ending_continue), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

const val DESCENT_PANEL_TAG = "descent_panel"
const val EYE_PANEL_TAG = "eye_panel"
const val EYE_TAG = "eye_awaken"
const val EYE_CONFIRM_TAG = "eye_confirm"
const val EYE_READ_TAG = "eye_read"
const val ENDING_TAG = "ending"
const val ENDING_CONTINUE_TAG = "ending_continue"
const val DESCENT_CHAOS_TAG = "descent_chaos"
const val DESCEND_TAG = "descend"
const val DESCEND_CONFIRM_TAG = "descend_confirm"
const val CHAOS_NODE_TAG = "chaos_node_"
const val CHAOS_BUY_TAG = "chaos_buy_"
