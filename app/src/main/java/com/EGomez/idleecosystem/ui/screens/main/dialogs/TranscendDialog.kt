package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.EGomez.idleecosystem.ui.i18n.tr

/** Color del Éter. */
val EtherColor = Color(0xFFB3E5FC)

/**
 * Trascender (v0.18.0): cuánto Éter daría, qué se pierde y qué se conserva, y las
 * mejoras eternas que se compran con Éter. Trascender pide tocar dos veces.
 */
@Composable
fun TranscendDialog(
    cycleAscensions: Int,
    etherGain: Int,
    ether: Int,
    eternalLevels: Map<String, Int>,
    onBuyEternal: (String) -> Unit,
    onTranscend: () -> Unit,
    onDismiss: () -> Unit
) {
    var armed by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF12161C),
        modifier = Modifier.testTag(TRANSCEND_DIALOG_TAG),
        title = { Text(stringResource(R.string.transcend_title), color = EtherColor, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.transcend_ether, ether), color = EtherColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.transcend_cycle, cycleAscensions, Transcendence.REQUIRED_ASCENSIONS),
                    color = if (cycleAscensions >= Transcendence.REQUIRED_ASCENSIONS) Color(0xFF81C784) else Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.transcend_loses), color = Color(0xFFE57373), fontSize = 12.sp)
                Text(stringResource(R.string.transcend_keeps), color = Color(0xFF81C784), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.transcend_eternal_title), color = EtherColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                EternalUpgrade.entries.forEach { upgrade ->
                    EternalRow(upgrade, eternalLevels[upgrade.id] ?: 0, ether, onBuyEternal)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (armed) onTranscend() else armed = true },
                enabled = etherGain > 0,
                colors = ButtonDefaults.buttonColors(containerColor = EtherColor, contentColor = Color.Black),
                modifier = Modifier.testTag(TRANSCEND_CONFIRM_TAG)
            ) {
                Text(
                    if (armed) stringResource(R.string.transcend_confirm_again) else stringResource(R.string.transcend_confirm, etherGain),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Color.Gray) }
        }
    )
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

const val TRANSCEND_DIALOG_TAG = "transcend_dialog"
const val TRANSCEND_CONFIRM_TAG = "transcend_confirm"
const val ETERNAL_ROW_TAG = "eternal_row_"
const val ETERNAL_BUY_TAG = "eternal_buy_"
