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
import com.EGomez.idleecosystem.domain.logic.Awakening
import com.EGomez.idleecosystem.domain.logic.AwakeningMilestone
import com.EGomez.idleecosystem.domain.logic.Oaths
import com.EGomez.idleecosystem.domain.logic.VisionNode
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import com.EGomez.idleecosystem.ui.i18n.tr

/** Color del Despertar: la luz de las Visiones. */
val VisionColor = Color(0xFFFFF176)

/** Lo que la capa Despertar necesita del motor (v0.21.0). */
data class AwakeningUi(
    val awakenings: Int = 0,
    val visions: Int = 0,
    val visionsEarned: Int = 0,
    val visionGain: Int = 0,
    val canAwaken: Boolean = false,
    val transcendences: Int = 0,
    val trialLevels: Int = 0,
    val levels: Map<String, Int> = emptyMap(),
    /** Juramentos (v0.21.1). */
    val oathsActive: Set<String> = emptySet(),
    val oathsNext: Set<String> = emptySet()
)

/**
 * La pestaña Despertar: los requisitos, las Visiones que daría, el botón (con su
 * confirmación) y los Hitos del Despertar.
 */
@Composable
fun AwakeningPanel(ui: AwakeningUi, onAwaken: () -> Unit, onSetOath: (String, Boolean) -> Unit = { _, _ -> }) {
    var confirming by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(AWAKEN_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1B10),
            border = BorderStroke(1.dp, VisionColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.awaken_title), color = VisionColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.awaken_hint), color = Color.LightGray, fontSize = 11.sp)
                    Text(stringResource(R.string.awaken_count, ui.awakenings), color = Color.Gray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.visions_label), color = VisionColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${ui.visions}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        // Requisitos
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF17160F),
            border = BorderStroke(1.dp, if (ui.canAwaken) VisionColor else Color(0xFF2E2C1E)),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Requirement(
                    stringResource(R.string.awaken_req_transcend, ui.transcendences, Awakening.REQUIRED_TRANSCENDENCES),
                    ui.transcendences >= Awakening.REQUIRED_TRANSCENDENCES
                )
                Requirement(
                    stringResource(R.string.awaken_req_trials, ui.trialLevels, Awakening.REQUIRED_TRIAL_LEVELS),
                    ui.trialLevels >= Awakening.REQUIRED_TRIAL_LEVELS
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { confirming = true },
                    enabled = ui.canAwaken && ui.visionGain > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = VisionColor, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().testTag(AWAKEN_BUTTON_TAG)
                ) {
                    Text(
                        if (ui.canAwaken) stringResource(R.string.awaken_button, ui.visionGain) else stringResource(R.string.awaken_locked),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        OathsSection(ui, onSetOath)
        // Hitos
        Text(
            stringResource(R.string.awaken_milestones),
            color = VisionColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )
        AwakeningMilestone.entries.forEach { milestone ->
            val reached = milestone.isReached(ui.awakenings)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF16150F),
                border = BorderStroke(1.dp, if (reached) VisionColor.copy(alpha = 0.6f) else Color(0xFF26251A)),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(AWAKEN_MILESTONE_TAG + milestone.awakenings)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    IconLabel(
                        if (reached) GameIcons.Eye else GameIcons.Lock,
                        tr(milestone.title) + " · " + stringResource(R.string.awaken_milestone_at, milestone.awakenings),
                        color = if (reached) VisionColor else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(tr(milestone.description), color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }
    }
    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(R.string.awaken_confirm_title), color = VisionColor) },
            text = {
                Column {
                    Text(stringResource(R.string.awaken_confirm_gain, ui.visionGain), color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stringResource(R.string.awaken_loses), color = Color(0xFFEF9A9A), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stringResource(R.string.awaken_keeps), color = Color(0xFFA5D6A7), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { confirming = false; onAwaken() }, modifier = Modifier.testTag(AWAKEN_CONFIRM_TAG)) {
                    Text(stringResource(R.string.awaken_confirm), color = VisionColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) { Text(stringResource(R.string.cancel), color = Color.Gray) }
            },
            containerColor = Color(0xFF1A1A14)
        )
    }
}

/**
 * Los Juramentos (v0.21.1): los que rigen este Despertar (y cuánto multiplican las
 * Visiones) y la lista para jurar los del próximo.
 */
@Composable
private fun OathsSection(ui: AwakeningUi, onSetOath: (String, Boolean) -> Unit) {
    val active = Oaths.all.filter { it.id in ui.oathsActive }
    val next = Oaths.all.filter { it.id in ui.oathsNext }
    Text(
        stringResource(R.string.oaths_title),
        color = VisionColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp)
    )
    Text(stringResource(R.string.oaths_hint), color = Color.LightGray, fontSize = 11.sp)
    Text(
        if (active.isEmpty()) stringResource(R.string.oaths_active_none)
        else stringResource(R.string.oaths_active, active.size, multiplier(Oaths.visionMultiplier(active))),
        color = if (active.isEmpty()) Color.Gray else VisionColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp).testTag(OATHS_ACTIVE_TAG)
    )
    Text(
        stringResource(R.string.oaths_next, next.size, multiplier(Oaths.visionMultiplier(next))),
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier.testTag(OATHS_NEXT_TAG)
    )
    Oaths.all.forEach { oath ->
        val sworn = oath.id in ui.oathsNext
        Surface(
            onClick = { onSetOath(oath.id, !sworn) },
            shape = RoundedCornerShape(10.dp),
            color = if (sworn) Color(0xFF24220F) else Color(0xFF16150F),
            border = BorderStroke(1.dp, if (sworn) VisionColor else Color(0xFF26251A)),
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(OATH_TAG + oath.id)
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tr(oath.title) + if (oath.id in ui.oathsActive) " · " + stringResource(R.string.oath_in_force) else "",
                        color = if (sworn) VisionColor else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(tr(oath.rule), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "+" + (oath.weight * 100).toInt() + " %",
                    color = if (sworn) VisionColor else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Checkbox(
                    checked = sworn,
                    onCheckedChange = { onSetOath(oath.id, it) },
                    colors = CheckboxDefaults.colors(checkedColor = VisionColor, checkmarkColor = Color.Black)
                )
            }
        }
    }
}

/** "x1.70". */
private fun multiplier(value: Double): String = "x" + "%.2f".format(java.util.Locale.US, value)

@Composable
private fun Requirement(text: String, met: Boolean) {
    IconLabel(
        if (met) GameIcons.Eye else GameIcons.Lock,
        text,
        color = if (met) Color(0xFFA5D6A7) else Color.LightGray,
        fontSize = 12.sp
    )
}

/** La pestaña Visiones: el Árbol de Visiones y su reinicio gratis. */
@Composable
fun VisionsPanel(ui: AwakeningUi, onBuy: (String) -> Unit, onRespec: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(VISIONS_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1B10),
            border = BorderStroke(1.dp, VisionColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.visions_tree_title), color = VisionColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.visions_tree_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.visions_label), color = VisionColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${ui.visions}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        VisionNode.entries.forEach { node -> VisionNodeRow(node, ui.levels, ui.visions, onBuy) }
        val refund = Awakening.refund(ui.levels)
        if (refund > 0) {
            OutlinedButton(
                onClick = onRespec,
                border = BorderStroke(1.dp, VisionColor.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(VISIONS_RESPEC_TAG)
            ) {
                Text(stringResource(R.string.visions_respec, refund), color = VisionColor, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun VisionNodeRow(node: VisionNode, levels: Map<String, Int>, visions: Int, onBuy: (String) -> Unit) {
    val level = levels[node.id] ?: 0
    val required = node.requires?.let { VisionNode.fromId(it) }
    val locked = required != null && (levels[required.id] ?: 0) == 0
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF16150F),
        border = BorderStroke(1.dp, if (level > 0) VisionColor.copy(alpha = 0.6f) else Color(0xFF26251A)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(VISION_NODE_TAG + node.id)
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
                Text(stringResource(R.string.eternal_maxed), color = VisionColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = { onBuy(node.id) },
                    enabled = Awakening.canBuy(node, levels, visions),
                    colors = ButtonDefaults.buttonColors(containerColor = VisionColor, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(VISION_BUY_TAG + node.id)
                ) {
                    Text(stringResource(R.string.visions_cost, node.cost(level)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

const val AWAKEN_PANEL_TAG = "awaken_panel"
const val OATHS_ACTIVE_TAG = "oaths_active"
const val OATHS_NEXT_TAG = "oaths_next"
const val OATH_TAG = "oath_"
const val AWAKEN_BUTTON_TAG = "awaken_button"
const val AWAKEN_CONFIRM_TAG = "awaken_confirm"
const val AWAKEN_MILESTONE_TAG = "awaken_milestone_"
const val VISIONS_PANEL_TAG = "visions_panel"
const val VISION_NODE_TAG = "vision_node_"
const val VISION_BUY_TAG = "vision_buy_"
const val VISIONS_RESPEC_TAG = "visions_respec"
