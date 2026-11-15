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
import com.EGomez.idleecosystem.domain.logic.Invocation
import com.EGomez.idleecosystem.domain.logic.SummoningChain
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import com.EGomez.idleecosystem.ui.i18n.tr
import java.math.BigDecimal
import java.util.Locale

/** Color de la Cadena de Invocación: rojo de cera. */
val ChainRed = Color(0xFFE57373)

/** Lo que la pestaña Cadena necesita del motor (v0.20.1). Listas del eslabón 1 al 8. */
data class ChainUi(
    val knowledge: BigDecimal = BigDecimal.ZERO,
    val power: BigDecimal = BigDecimal.ZERO,
    val multiplier: Double = 1.0,
    val amounts: List<BigDecimal> = emptyList(),
    val bought: List<Int> = emptyList()
)

/**
 * La pestaña Cadena (capa Trascendencia): el Poder de Invocación y lo que multiplica, y
 * los ocho eslabones con sus unidades, su multiplicador y su precio en Conocimiento.
 */
@Composable
fun ChainPanel(ui: ChainUi, onBuy: (Int) -> Unit, onBuyMax: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(CHAIN_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1214),
            border = BorderStroke(1.dp, ChainRed.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.chain_title), color = ChainRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.chain_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.chain_power_label), color = ChainRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        ui.power.toBeautifulString(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag(CHAIN_POWER_TAG)
                    )
                    Text(
                        stringResource(R.string.chain_multiplier, "%.2f".format(Locale.US, ui.multiplier)),
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }
        OutlinedButton(
            onClick = onBuyMax,
            border = BorderStroke(1.dp, ChainRed.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(CHAIN_BUY_MAX_TAG)
        ) {
            Text(stringResource(R.string.chain_buy_max), color = ChainRed, fontSize = 12.sp)
        }
        Invocation.entries.forEachIndexed { i, invocation ->
            InvocationRow(
                invocation,
                amount = ui.amounts.getOrElse(i) { BigDecimal.ZERO },
                bought = ui.bought.getOrElse(i) { 0 },
                knowledge = ui.knowledge,
                onBuy = onBuy
            )
        }
    }
}

@Composable
private fun InvocationRow(invocation: Invocation, amount: BigDecimal, bought: Int, knowledge: BigDecimal, onBuy: (Int) -> Unit) {
    val cost = invocation.cost(bought)
    val multiplier = SummoningChain.tierMultiplier(bought)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF181314),
        border = BorderStroke(1.dp, if (bought > 0) ChainRed.copy(alpha = 0.6f) else Color(0xFF2E2426)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(CHAIN_ROW_TAG + invocation.tier)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                IconLabel(
                    GameIcons.Chain,
                    "${invocation.tier}. " + tr(invocation.title),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.chain_row_info, amount.toBeautifulString(), bought, "%.0f".format(Locale.US, multiplier)),
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(tr(invocation.description), color = Color.Gray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onBuy(invocation.tier) },
                enabled = knowledge >= cost,
                colors = ButtonDefaults.buttonColors(containerColor = ChainRed, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag(CHAIN_BUY_TAG + invocation.tier)
            ) {
                Text(cost.toBeautifulString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

const val CHAIN_PANEL_TAG = "chain_panel"
const val CHAIN_POWER_TAG = "chain_power"
const val CHAIN_BUY_MAX_TAG = "chain_buy_max"
const val CHAIN_ROW_TAG = "chain_row_"
const val CHAIN_BUY_TAG = "chain_buy_"
