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
import com.EGomez.idleecosystem.domain.logic.EternalUpgrade
import com.EGomez.idleecosystem.domain.logic.Transcendence
import com.EGomez.idleecosystem.domain.logic.TranscendenceMilestone
import com.EGomez.idleecosystem.ui.i18n.tr

/** Color del Éter. */
val EtherColor = Color(0xFFB3E5FC)

/**
 * La pestaña Éter (Trascendencia): cuánto Éter daría Trascender, qué se pierde y qué se
 * conserva, y las mejoras eternas. Desde la v0.19.0 vive en su pestaña en lugar de un
 * diálogo abierto desde el panel de Cordura. Trascender pide tocar dos veces.
 */
@Composable
fun EtherPanel(
    cycleAscensions: Int,
    etherGain: Int,
    ether: Int,
    eternalLevels: Map<String, Int>,
    onBuyEternal: (String) -> Unit,
    onTranscend: () -> Unit,
    transcendences: Int = 0,
    required: Int = Transcendence.REQUIRED_ASCENSIONS
) {
    var armed by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(ETHER_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF12161C),
            border = BorderStroke(1.dp, EtherColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.transcend_ether, ether), color = EtherColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.transcend_cycle, cycleAscensions, required),
                    color = if (cycleAscensions >= required) Color(0xFF81C784) else Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.transcend_loses), color = Color(0xFFE57373), fontSize = 11.sp)
                Text(stringResource(R.string.transcend_keeps), color = Color(0xFF81C784), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { if (armed) { armed = false; onTranscend() } else armed = true },
                    enabled = etherGain > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EtherColor,
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFF2E3640),
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag(TRANSCEND_CONFIRM_TAG)
                ) {
                    Text(
                        when {
                            etherGain <= 0 -> stringResource(R.string.transcend_locked, required)
                            armed -> stringResource(R.string.transcend_confirm_again)
                            else -> stringResource(R.string.transcend_confirm, etherGain)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TranscendMilestoneList(transcendences)
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.transcend_eternal_title), color = EtherColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        EternalUpgrade.entries.forEach { upgrade ->
            EternalRow(upgrade, eternalLevels[upgrade.id] ?: 0, ether, onBuyEternal)
        }
    }
}

/**
 * Los Hitos de Trascendencia (v0.19.5): los alcanzados y el siguiente a la vista, con lo
 * que dan; los demás solo con su nombre y cuántas Trascendencias piden.
 */
@Composable
fun TranscendMilestoneList(transcendences: Int) {
    val next = TranscendenceMilestone.next(transcendences)
    Column(modifier = Modifier.fillMaxWidth().testTag(TRANSCEND_MILESTONES_TAG)) {
        Text(
            stringResource(R.string.transcend_milestones_title, TranscendenceMilestone.reached(transcendences), TranscendenceMilestone.entries.size),
            color = EtherColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        TranscendenceMilestone.entries.forEach { milestone ->
            val reached = milestone.isReached(transcendences)
            val shown = reached || milestone == next
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(TRANSCEND_MILESTONE_ROW_TAG + milestone.name)
            ) {
                Text(
                    if (reached) "✦" else "◇",
                    color = if (reached) EtherColor else Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.width(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tr(milestone.title),
                        color = if (reached) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    if (shown) {
                        Text(tr(milestone.description), color = if (reached) Color.LightGray else Color.Gray, fontSize = 11.sp)
                    }
                    if (!reached) {
                        Text(
                            stringResource(R.string.transcend_milestone_at, transcendences, milestone.transcendences),
                            color = Color.DarkGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EternalRow(upgrade: EternalUpgrade, level: Int, ether: Int, onBuy: (String) -> Unit) {
    val maxed = upgrade.isMaxed(level)
    val cost = upgrade.cost(level)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1A2129),
        border = BorderStroke(1.dp, if (level > 0) EtherColor.copy(alpha = 0.6f) else Color(0xFF2E3640)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(ETERNAL_ROW_TAG + upgrade.id)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tr(upgrade.title) + if (upgrade.maxLevel == 1) "" else " · " + stringResource(R.string.eternal_level, level),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(tr(upgrade.description), color = Color.LightGray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (maxed) {
                Text(stringResource(R.string.eternal_maxed), color = EtherColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = { onBuy(upgrade.id) },
                    enabled = ether >= cost,
                    colors = ButtonDefaults.buttonColors(containerColor = EtherColor, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(ETERNAL_BUY_TAG + upgrade.id)
                ) {
                    Text(stringResource(R.string.eternal_cost, cost), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

const val ETHER_PANEL_TAG = "ether_panel"
const val TRANSCEND_MILESTONES_TAG = "transcend_milestones"
const val TRANSCEND_MILESTONE_ROW_TAG = "transcend_milestone_"
const val TRANSCEND_CONFIRM_TAG = "transcend_confirm"
const val ETERNAL_ROW_TAG = "eternal_row_"
const val ETERNAL_BUY_TAG = "eternal_buy_"
