package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.Veil
import com.EGomez.idleecosystem.domain.model.Trial
import java.math.BigDecimal
import java.math.MathContext
import com.EGomez.idleecosystem.domain.model.Mutation
import com.EGomez.idleecosystem.domain.model.TrialReward
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.theme.AncientGold

private val TrialRed = Color(0xFFCF6679)
private val Done = Color(0xFF81C784)

/** La recompensa de una Prueba, línea por línea, en el idioma del juego. */
@Composable
fun trialRewardLines(reward: TrialReward): List<String> {
    val resources = LocalContext.current.resources
    val lines = mutableListOf<String>()
    fun add(value: Double, res: Int) {
        if (value != 1.0) lines += resources.getString(res, multiplier(value))
    }
    add(reward.production, R.string.pact_effect_production)
    add(reward.click, R.string.pact_effect_click)
    add(reward.offline, R.string.pact_effect_offline)
    add(reward.runeFrequency, R.string.pact_effect_rune_frequency)
    add(reward.buildingCost, R.string.pact_effect_building_cost)
    add(reward.ritualCooldown, R.string.pact_effect_ritual_cooldown)
    add(reward.sanityGain, R.string.pact_effect_sanity)
    if (reward.attentionPoints > 0) lines += resources.getString(R.string.trial_reward_attention, reward.attentionPoints)
    if (reward.runeSlots > 0) lines += resources.getString(R.string.trial_reward_rune_slot, reward.runeSlots)
    reward.pact?.let { lines += resources.getString(R.string.trial_reward_pact, tr(it.godName)) }
    return lines
}

/**
 * Las Pruebas de Locura en la pestaña Panteón: todas, con su regla, su meta, su
 * recompensa y su estado. La de esta vida muestra su avance y se puede abandonar.
 */
@Composable
fun TrialList(
    ascensions: Int,
    activeTrial: Trial?,
    progress: Float,
    completed: Set<Trial>,
    onAbandon: () -> Unit,
    veilBroken: Boolean = false,
    onBreakVeil: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        VeilCard(completed.size, veilBroken, onBreakVeil)
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.trials_intro), color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Trial.entries.forEach { trial ->
            TrialCard(
                trial = trial,
                // Como los pactos: se elige en la Ascensión que viene
                unlocked = trial.isUnlocked(ascensions + 1),
                active = trial == activeTrial,
                progress = progress,
                done = trial in completed,
                onAbandon = onAbandon
            )
        }
    }
}

@Composable
private fun TrialCard(
    trial: Trial,
    unlocked: Boolean,
    active: Boolean,
    progress: Float,
    done: Boolean,
    onAbandon: () -> Unit
) {
    val border = when {
        active -> TrialRed
        done -> Done.copy(alpha = 0.6f)
        unlocked -> Color(0xFF444444)
        else -> Color(0xFF2A2A2A)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF151515),
        border = BorderStroke(if (active) 2.dp else 1.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .testTag(TRIAL_CARD_TAG + trial.id)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (unlocked) tr(trial.title) else "???",
                    color = if (unlocked) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                when {
                    active -> Text(stringResource(R.string.trial_status_active, (progress * 100).toInt()), color = TrialRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    done -> Text(stringResource(R.string.trial_status_completed), color = Done, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    !unlocked -> Text(stringResource(R.string.trial_locked, trial.unlockAscensions), color = Color.Gray, fontSize = 11.sp)
                }
            }
            if (!unlocked) return@Column
            Text(tr(trial.rule), color = TrialRed.copy(alpha = 0.9f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            Text(stringResource(R.string.trial_goal, trial.goal.toBeautifulString()), color = Color.LightGray, fontSize = 12.sp)
            Text(
                stringResource(R.string.trial_reward_title) + ": " + trialRewardLines(trial.reward).joinToString(" · "),
                color = if (done) Done else AncientGold,
                fontSize = 12.sp
            )
            if (active) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = TrialRed,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                )
                TextButton(onClick = onAbandon, modifier = Modifier.testTag(TRIAL_ABANDON_TAG)) {
                    Text(stringResource(R.string.trial_abandon), color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

/** Barra fija en Investigación mientras hay una Prueba: nombre, avance y (Delirio) el multiplicador. */
@Composable
fun TrialBar(trial: Trial, progress: Float, delirium: Double, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2A1218),
        border = BorderStroke(1.dp, TrialRed.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 4.dp)
            .testTag(TRIAL_BAR_TAG)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            val text = stringResource(R.string.trial_bar, tr(trial.title), (progress * 100).toInt()) +
                if (trial.rules.delirium) " · x${multiplier(delirium)}" else ""
            // Una sola línea: la Prueba, su avance y la regla
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text, color = TrialRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(modifier = Modifier.width(8.dp))
                Text(tr(trial.rule), color = Color.LightGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(3.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = TrialRed,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

/** Elegir la Prueba de la próxima vida en la confirmación de Ascender. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrialPicker(trials: List<Trial>, completed: Set<Trial>, selected: Trial?, onSelect: (Trial?) -> Unit) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(stringResource(R.string.trial_choose_title), color = TrialRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    // Todas las opciones a la vista, en varias filas (v0.19.3: antes había que deslizar)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        TrialChip(stringResource(R.string.trial_none), selected == null, TRIAL_CHOICE_TAG + "none") { onSelect(null) }
        trials.forEach { trial ->
            val label = (if (trial in completed) "✓ " else "") + tr(trial.title)
            TrialChip(label, selected == trial, TRIAL_CHOICE_TAG + trial.id) { onSelect(trial) }
        }
    }
    selected?.let { trial ->
        Text(tr(trial.rule), color = TrialRed, fontSize = 12.sp)
        Text(stringResource(R.string.trial_goal, trial.goal.toBeautifulString()), color = Color.LightGray, fontSize = 12.sp)
        if (trial in completed) {
            Text(stringResource(R.string.trial_repeat), color = Color.Gray, fontSize = 11.sp)
        } else {
            Text(
                stringResource(R.string.trial_reward_title) + ": " + trialRewardLines(trial.reward).joinToString(" · "),
                color = AncientGold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TrialChip(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TrialRed,
            selectedLabelColor = Color.Black,
            containerColor = Color(0xFF2A2A2A),
            labelColor = Color.LightGray
        ),
        border = null,
        modifier = Modifier.testTag(tag)
    )
}

/** Elegir la mutación de la próxima vida (v0.18.4): tres al azar o ninguna. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MutationPicker(offer: List<Mutation>, selected: Mutation?, onSelect: (Mutation?) -> Unit) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(stringResource(R.string.mutation_choose_title), color = MutationGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    // Todas las opciones a la vista, en varias filas (v0.19.3: antes había que deslizar)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        TrialChip(stringResource(R.string.mutation_none), selected == null, MUTATION_CHOICE_TAG + "none") { onSelect(null) }
        offer.forEach { mutation ->
            TrialChip("🧬 " + tr(mutation.title), selected == mutation, MUTATION_CHOICE_TAG + mutation.id) { onSelect(mutation) }
        }
    }
    selected?.let { Text(tr(it.description), color = MutationGreen, fontSize = 12.sp) }
}

val MutationGreen = Color(0xFF9CCC65)
const val MUTATION_CHOICE_TAG = "mutation_choice_"
const val VEIL_CARD_TAG = "veil_card"
const val VEIL_BREAK_TAG = "veil_break"
const val VEIL_BANNER_TAG = "veil_banner"

private val VeilViolet = Color(0xFFB39DDB)

/**
 * Romper el Velo (v0.19.6), arriba de las Pruebas: el tope, cuántas Pruebas faltan y el
 * botón para romperlo cuando están las 10. Roto, solo recuerda lo que da.
 */
@Composable
fun VeilCard(completedTrials: Int, broken: Boolean, onBreak: () -> Unit) {
    val total = Trial.entries.size
    val ready = Veil.canBreak(completedTrials, broken)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (broken) Color(0xFF17131F) else Color(0xFF15121A),
        border = BorderStroke(1.dp, VeilViolet.copy(alpha = if (broken || ready) 0.8f else 0.35f)),
        modifier = Modifier.fillMaxWidth().testTag(VEIL_CARD_TAG)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                if (broken) "✧ " + stringResource(R.string.veil_broken_title) else "🜏 " + stringResource(R.string.veil_title),
                color = VeilViolet,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            if (broken) {
                Text(stringResource(R.string.veil_broken_desc, multiplier(Veil.BROKEN_PRODUCTION)), color = Color.LightGray, fontSize = 11.sp)
            } else {
                Text(stringResource(R.string.veil_desc, Veil.THRESHOLD.toBeautifulString()), color = Color.LightGray, fontSize = 11.sp)
                Text(stringResource(R.string.veil_reward, multiplier(Veil.BROKEN_PRODUCTION)), color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (completedTrials.toFloat() / total).coerceIn(0f, 1f) },
                    color = VeilViolet,
                    trackColor = Color(0xFF2A2A2A),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                )
                Text(stringResource(R.string.veil_progress, completedTrials.coerceAtMost(total), total), color = Color.Gray, fontSize = 10.sp)
                if (ready) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onBreak,
                        colors = ButtonDefaults.buttonColors(containerColor = VeilViolet, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().testTag(VEIL_BREAK_TAG)
                    ) { Text(stringResource(R.string.veil_break), fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

/** Aviso arriba de Investigación cuando el Velo frena la producción de esta vida. */
@Composable
fun VeilBanner(factor: BigDecimal) {
    val divisor = if (factor.signum() > 0) BigDecimal.ONE.divide(factor, MathContext.DECIMAL64) else BigDecimal.ZERO
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1A1424),
        border = BorderStroke(1.dp, VeilViolet.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).testTag(VEIL_BANNER_TAG)
    ) {
        Text(
            "🜏 " + stringResource(R.string.veil_pressing, divisor.toBeautifulString()),
            color = VeilViolet,
            fontSize = 12.sp,
            modifier = Modifier.padding(10.dp)
        )
    }
}

const val TRIAL_CARD_TAG = "trial_card_"
const val TRIAL_ABANDON_TAG = "trial_abandon"
const val TRIAL_BAR_TAG = "trial_bar"
const val TRIAL_CHOICE_TAG = "trial_choice_"
