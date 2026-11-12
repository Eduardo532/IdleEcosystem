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
import com.EGomez.idleecosystem.domain.logic.Lab
import com.EGomez.idleecosystem.domain.logic.LabExperiment
import com.EGomez.idleecosystem.ui.i18n.tr
import java.util.Locale

/** Color del Laboratorio: verde de probeta. */
val LabGreen = Color(0xFFA5D6A7)

/** "^1,05" con dos decimales. */
private fun exponentLabel(value: Double): String = "^" + "%.2f".format(Locale.US, value)

/**
 * La pestaña Laboratorio (capa Trascendencia): el Éter disponible y los seis
 * experimentos de Miskatonic, con su nivel, lo que dan ahora y su costo.
 */
@Composable
fun LabPanel(ether: Int, levels: Map<String, Int>, onBuy: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(LAB_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF111A12),
            border = BorderStroke(1.dp, LabGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.lab_title), color = LabGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.lab_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.tab_ether).uppercase(), color = EtherColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("$ether", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        LabExperiment.entries.forEach { experiment ->
            LabRow(experiment, levels[experiment.id] ?: 0, ether, onBuy)
        }
    }
}

@Composable
private fun LabRow(experiment: LabExperiment, level: Int, ether: Int, onBuy: (String) -> Unit) {
    val maxed = experiment.isMaxed(level)
    val cost = experiment.cost(level)
    val now = if (experiment == LabExperiment.VEIL) {
        stringResource(R.string.lab_now_veil, Lab.veilThreshold(level).toBeautifulString())
    } else stringResource(R.string.lab_now, exponentLabel(experiment.exponent(level)))
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF151A15),
        border = BorderStroke(1.dp, if (level > 0) LabGreen.copy(alpha = 0.6f) else Color(0xFF263026)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(LAB_ROW_TAG + experiment.id)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "⚗ " + tr(experiment.title) + " · " + stringResource(R.string.lab_level, level, experiment.maxLevel),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(tr(experiment.description), color = Color.LightGray, fontSize = 11.sp)
                Text(now, color = LabGreen, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (maxed) {
                Text(stringResource(R.string.eternal_maxed), color = LabGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = { onBuy(experiment.id) },
                    enabled = ether >= cost,
                    colors = ButtonDefaults.buttonColors(containerColor = EtherColor, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(LAB_BUY_TAG + experiment.id)
                ) {
                    Text(stringResource(R.string.lab_cost, cost), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

const val LAB_PANEL_TAG = "lab_panel"
const val LAB_ROW_TAG = "lab_row_"
const val LAB_BUY_TAG = "lab_buy_"
