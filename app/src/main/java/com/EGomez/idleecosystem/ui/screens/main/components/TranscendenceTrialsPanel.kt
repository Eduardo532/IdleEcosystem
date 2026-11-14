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
import com.EGomez.idleecosystem.domain.logic.TranscendenceTrial
import com.EGomez.idleecosystem.domain.logic.TranscendenceTrials
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import com.EGomez.idleecosystem.ui.i18n.tr
import java.math.BigDecimal

/** Color de las Pruebas de Trascendencia: oro viejo. */
val TrialGold = Color(0xFFFFD54F)

/** Lo que la pestaña Desafíos necesita del motor (v0.20.3). */
data class TranscendenceTrialsUi(
    val next: String? = null,
    val active: String? = null,
    val cycleBest: BigDecimal = BigDecimal.ZERO,
    val levels: Map<String, Int> = emptyMap()
)

/**
 * La pestaña Desafíos (capa Trascendencia): la Prueba en curso y su meta, y las cuatro
 * Pruebas con sus niveles superados, su restricción, su recompensa y la meta siguiente.
 */
@Composable
fun TranscendenceTrialsPanel(ui: TranscendenceTrialsUi, onChoose: (String?) -> Unit, onAbandon: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(TTRIALS_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1A10),
            border = BorderStroke(1.dp, TrialGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.ttrials_title), color = TrialGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.ttrials_hint), color = Color.LightGray, fontSize = 11.sp)
            }
        }
        val active = ui.active?.let { TranscendenceTrial.fromId(it) }
        if (active != null) {
            val level = TranscendenceTrials.nextLevel(ui.levels[active.id] ?: 0) ?: TranscendenceTrials.MAX_LEVEL
            val goal = TranscendenceTrials.goal(level)
            val met = ui.cycleBest >= goal
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF221E10),
                border = BorderStroke(1.dp, TrialGold),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(TTRIALS_ACTIVE_TAG)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    IconLabel(
                        GameIcons.Trophy,
                        stringResource(R.string.ttrials_active, tr(active.title), TranscendenceTrials.roman(level)),
                        color = TrialGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(tr(active.restriction), color = Color.LightGray, fontSize = 11.sp)
                    Text(
                        stringResource(R.string.ttrials_progress, ui.cycleBest.toBeautifulString(), goal.toBeautifulString()),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        stringResource(if (met) R.string.ttrials_met else R.string.ttrials_not_met),
                        color = if (met) Color(0xFFA5D6A7) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag(TTRIALS_MET_TAG)
                    )
                    TextButton(onClick = onAbandon, modifier = Modifier.testTag(TTRIALS_ABANDON_TAG)) {
                        Text(stringResource(R.string.ttrials_abandon), color = Color(0xFFEF9A9A), fontSize = 12.sp)
                    }
                }
            }
        }
        TranscendenceTrial.entries.forEach { trial -> TrialRow(trial, ui, onChoose) }
    }
}

@Composable
private fun TrialRow(trial: TranscendenceTrial, ui: TranscendenceTrialsUi, onChoose: (String?) -> Unit) {
    val done = ui.levels[trial.id] ?: 0
    val nextLevel = TranscendenceTrials.nextLevel(done)
    val chosen = ui.next == trial.id
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF17160F),
        border = BorderStroke(1.dp, if (chosen) TrialGold else if (done > 0) TrialGold.copy(alpha = 0.4f) else Color(0xFF2C2A1E)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(TTRIALS_ROW_TAG + trial.id)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconLabel(
                    GameIcons.Trophy,
                    tr(trial.title),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stringResource(R.string.ttrials_levels, done, TranscendenceTrials.MAX_LEVEL),
                    color = TrialGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(stringResource(R.string.ttrials_restriction, tr(trial.restriction)), color = Color.LightGray, fontSize = 11.sp)
            Text(stringResource(R.string.ttrials_reward, tr(trial.reward)), color = Color(0xFFA5D6A7), fontSize = 11.sp)
            if (nextLevel != null) {
                Text(
                    stringResource(R.string.ttrials_goal, TranscendenceTrials.roman(nextLevel), TranscendenceTrials.goal(nextLevel).toBeautifulString()),
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                if (ui.active != trial.id) {
                    OutlinedButton(
                        onClick = { onChoose(if (chosen) null else trial.id) },
                        border = BorderStroke(1.dp, TrialGold.copy(alpha = if (chosen) 1f else 0.5f)),
                        modifier = Modifier.padding(top = 4.dp).testTag(TTRIALS_CHOOSE_TAG + trial.id)
                    ) {
                        Text(
                            stringResource(if (chosen) R.string.ttrials_chosen else R.string.ttrials_choose),
                            color = TrialGold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Text(stringResource(R.string.ttrials_mastered), color = TrialGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

const val TTRIALS_PANEL_TAG = "ttrials_panel"
const val TTRIALS_ACTIVE_TAG = "ttrials_active"
const val TTRIALS_MET_TAG = "ttrials_met"
const val TTRIALS_ABANDON_TAG = "ttrials_abandon"
const val TTRIALS_ROW_TAG = "ttrials_row_"
const val TTRIALS_CHOOSE_TAG = "ttrials_choose_"
