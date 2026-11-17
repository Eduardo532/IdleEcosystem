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
import com.EGomez.idleecosystem.domain.logic.AzathothDream
import com.EGomez.idleecosystem.domain.logic.ChaosNode
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
    val levels: Map<String, Int> = emptyMap()
)

/**
 * La pestaña Descenso (capa El Sueño de Azathoth): el Caos, lo que pide Descender, el
 * botón con su confirmación y el árbol del Caos.
 */
@Composable
fun DescentPanel(ui: DescentUi, onDescend: () -> Unit, onBuyNode: (String) -> Unit) {
    var confirming by remember { mutableStateOf(false) }
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

const val DESCENT_PANEL_TAG = "descent_panel"
const val DESCENT_CHAOS_TAG = "descent_chaos"
const val DESCEND_TAG = "descend"
const val DESCEND_CONFIRM_TAG = "descend_confirm"
const val CHAOS_NODE_TAG = "chaos_node_"
const val CHAOS_BUY_TAG = "chaos_buy_"
