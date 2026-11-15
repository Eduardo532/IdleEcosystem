package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.Expeditions
import com.EGomez.idleecosystem.domain.logic.Relic
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import com.EGomez.idleecosystem.ui.i18n.tr
import java.math.BigDecimal

/** Color de la Expedición: el oro de sus trofeos. */
val ExpeditionGold = Color(0xFFFFCA28)

/** Lo que la pestaña Expedición necesita del motor (v0.21.3). */
data class ExpeditionUi(
    val level: Int = 0,
    val dealt: Double = 0.0,
    val dps: Double = 0.0,
    val best: Int = 0,
    val gold: Int = 0,
    val goldOnAwaken: Int = 0,
    val relics: Map<String, Int> = emptyMap()
)

private fun Double.short(): String = BigDecimal.valueOf(this).toBeautifulString()

/**
 * La pestaña Expedición (capa Despertar): el círculo contra el horror del nivel en curso,
 * su vida, el daño por segundo, el golpe a mano, el Oro y las tres Reliquias.
 */
@Composable
fun ExpeditionPanel(ui: ExpeditionUi, onStrike: () -> Unit, onBuyRelic: (String) -> Unit) {
    val horror = Expeditions.horror(ui.level)
    val health = Expeditions.health(ui.level)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(EXPEDITION_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1810),
            border = BorderStroke(1.dp, ExpeditionGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.expedition_title), color = ExpeditionGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.expedition_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.expedition_gold), color = ExpeditionGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${ui.gold}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag(EXPEDITION_GOLD_TAG))
                }
            }
        }
        // El círculo y el horror
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF15130E),
            border = BorderStroke(1.dp, ExpeditionGold.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.expedition_level, ui.level, ui.best),
                    color = ExpeditionGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag(EXPEDITION_LEVEL_TAG)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2312))
                ) {
                    BeastPortrait(horror, known = true, modifier = Modifier.size(96.dp).clip(CircleShape))
                }
                Text(tr(horror.name), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { (ui.dealt / health).toFloat().coerceIn(0f, 1f) },
                    color = Color(0xFFE57373),
                    trackColor = Color(0xFF332222),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(8.dp).testTag(EXPEDITION_HEALTH_TAG)
                )
                Text(
                    stringResource(R.string.expedition_health, (health - ui.dealt).coerceAtLeast(0.0).short(), health.short()),
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(stringResource(R.string.expedition_dps, ui.dps.short()), color = Color.LightGray, fontSize = 11.sp)
                Button(
                    onClick = onStrike,
                    colors = ButtonDefaults.buttonColors(containerColor = ExpeditionGold, contentColor = Color.Black),
                    modifier = Modifier.padding(top = 6.dp).testTag(EXPEDITION_STRIKE_TAG)
                ) {
                    Text(stringResource(R.string.expedition_strike), fontWeight = FontWeight.Bold)
                }
                Text(stringResource(R.string.expedition_gold_on_awaken, ui.goldOnAwaken), color = ExpeditionGold, fontSize = 11.sp)
            }
        }
        // Reliquias
        Text(stringResource(R.string.relics_title), color = ExpeditionGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        Relic.entries.forEach { relic ->
            val level = ui.relics[relic.id] ?: 0
            val unlocked = relic.isUnlocked(ui.best)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF16140E),
                border = BorderStroke(1.dp, if (level > 0) ExpeditionGold.copy(alpha = 0.6f) else Color(0xFF2A261A)),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(RELIC_TAG + relic.id)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        IconLabel(
                            if (unlocked) GameIcons.Trophy else GameIcons.Lock,
                            tr(relic.title) + " · " + stringResource(R.string.lab_level, level, relic.maxLevel),
                            color = if (unlocked) Color.White else Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(tr(relic.description), color = Color.LightGray, fontSize = 11.sp)
                        if (!unlocked) Text(stringResource(R.string.relic_locked, relic.unlockLevel), color = Color.Gray, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (relic.isMaxed(level)) {
                        Text(stringResource(R.string.eternal_maxed), color = ExpeditionGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Button(
                            onClick = { onBuyRelic(relic.id) },
                            enabled = unlocked && ui.gold >= relic.cost(level),
                            colors = ButtonDefaults.buttonColors(containerColor = ExpeditionGold, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag(RELIC_BUY_TAG + relic.id)
                        ) {
                            Text(stringResource(R.string.relic_cost, relic.cost(level)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

const val EXPEDITION_PANEL_TAG = "expedition_panel"
const val EXPEDITION_GOLD_TAG = "expedition_gold"
const val EXPEDITION_LEVEL_TAG = "expedition_level"
const val EXPEDITION_HEALTH_TAG = "expedition_health"
const val EXPEDITION_STRIKE_TAG = "expedition_strike"
const val RELIC_TAG = "relic_"
const val RELIC_BUY_TAG = "relic_buy_"
