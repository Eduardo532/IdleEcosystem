package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import com.EGomez.idleecosystem.domain.logic.StarUpgrade
import com.EGomez.idleecosystem.domain.logic.Stars
import com.EGomez.idleecosystem.ui.i18n.tr
import java.math.BigDecimal
import kotlin.math.floor

/** Color de las Estrellas y del Polvo Estelar. */
val StarGold = Color(0xFFFFE082)

/** Lo que muestra la pestaña Estrellas (v0.19.7). */
data class StarsUi(
    val stars: Double = 0.0,
    val stardust: Double = 0.0,
    val starsPerHour: Double = 0.0,
    val dustPerSecond: Double = 0.0,
    val levels: Map<String, Int> = emptyMap(),
    val aligned: Boolean = false,
    val secondsLeft: Long = 0
)

private fun Double.pretty(): String =
    if (this < 1000) {
        if (this == floor(this)) this.toLong().toString() else "%.1f".format(java.util.Locale.US, this)
    } else BigDecimal.valueOf(this).toBeautifulString()

private fun clock(seconds: Long): String = "%d:%02d".format(seconds / 60, seconds % 60)

/**
 * La pestaña Estrellas (capa Trascendencia): cuántas Estrellas hay y cuánto Polvo dan, si
 * están en posición (y cuánto falta) y las mejoras estelares.
 */
@Composable
fun StarsPanel(state: StarsUi, onBuy: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(STARS_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF16140E),
            border = BorderStroke(1.dp, StarGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.stars_title), color = StarGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("✦ " + floor(state.stars).pretty(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.stars_rate, state.starsPerHour.pretty()), color = Color.Gray, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.stardust_title), color = StarGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(state.stardust.pretty(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.stardust_rate, state.dustPerSecond.pretty()), color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
        AlignmentCard(state)
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.stars_upgrades_title), color = StarGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        StarUpgrade.entries.forEach { upgrade ->
            StarRow(upgrade, state.levels[upgrade.id] ?: 0, state.stardust, onBuy)
        }
    }
}

@Composable
private fun AlignmentCard(state: StarsUi) {
    val window = Stars.alignmentSeconds(state.levels[StarUpgrade.CONJUNCTION.id] ?: 0)
    val total = if (state.aligned) window else Stars.ALIGNMENT_PERIOD_SECONDS - window
    val progress = if (total > 0) (1f - state.secondsLeft.toFloat() / total).coerceIn(0f, 1f) else 0f
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (state.aligned) Color(0xFF2A2410) else Color(0xFF151515),
        border = BorderStroke(1.dp, if (state.aligned) StarGold else Color(0xFF2E2E2E)),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(ALIGNMENT_TAG)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                if (state.aligned) stringResource(R.string.stars_aligned, multiplier(Stars.ALIGNED_DUST), clock(state.secondsLeft))
                else stringResource(R.string.stars_next_alignment, clock(state.secondsLeft)),
                color = if (state.aligned) StarGold else Color.LightGray,
                fontSize = 12.sp,
                fontWeight = if (state.aligned) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = StarGold,
                trackColor = Color(0xFF2A2A2A),
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun StarRow(upgrade: StarUpgrade, level: Int, dust: Double, onBuy: (String) -> Unit) {
    val maxed = upgrade.isMaxed(level)
    val cost = upgrade.cost(level)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1C1A12),
        border = BorderStroke(1.dp, if (level > 0) StarGold.copy(alpha = 0.6f) else Color(0xFF332F22)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(STAR_ROW_TAG + upgrade.id)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tr(upgrade.title) + " · " + stringResource(R.string.eternal_level, level),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(tr(upgrade.description), color = Color.LightGray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (maxed) {
                Text(stringResource(R.string.eternal_maxed), color = StarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = { onBuy(upgrade.id) },
                    enabled = dust >= cost,
                    colors = ButtonDefaults.buttonColors(containerColor = StarGold, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(STAR_BUY_TAG + upgrade.id)
                ) {
                    Text("✦ " + cost.pretty(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

const val STARS_PANEL_TAG = "stars_panel"
const val ALIGNMENT_TAG = "stars_alignment"
const val STAR_ROW_TAG = "star_row_"
const val STAR_BUY_TAG = "star_buy_"
