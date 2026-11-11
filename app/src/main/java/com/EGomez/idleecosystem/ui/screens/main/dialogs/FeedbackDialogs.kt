package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.EGomez.idleecosystem.domain.logic.AscensionMilestone
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.domain.model.Mutation
import com.EGomez.idleecosystem.ui.screens.main.components.MutationPicker
import com.EGomez.idleecosystem.ui.screens.main.components.MutationGreen
import com.EGomez.idleecosystem.ui.screens.main.components.TrialPicker
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.ui.components.PactSigil
import com.EGomez.idleecosystem.ui.components.sigilColor
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.screens.main.components.PactEffectList
import com.EGomez.idleecosystem.ui.screens.main.components.pactLines
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple

@Composable
fun HardResetDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
        title = { Text(stringResource(R.string.reset_confirm_title), color = Color.Red, fontWeight = FontWeight.Black) },
        text = { Text(stringResource(R.string.reset_confirm_text), color = Color.White) },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.Red,
        textContentColor = Color.White,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(R.string.reset_confirm_button), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Color.Gray) }
        }
    )
}

/**
 * Confirmación antes de ascender. Antes el botón ascendía al instante: un toque
 * accidental reiniciaba la partida, y el tutorial "El Ciclo" aparecía después.
 */
@Composable
fun AscendDialog(
    sanityGainFormatted: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    pacing: AscendPacing? = null,
    /** Ascensiones ya hechas (null = no mostrar los hitos). */
    ascensions: Int? = null,
    /** Pactos que se pueden elegir para la próxima vida (vacío = no mostrar). */
    pacts: List<Pact> = emptyList(),
    selectedPact: Pact = Pact.NONE,
    onPactSelected: (Pact) -> Unit = {},
    buildingNames: Map<String, String> = emptyMap(),
    /** La Senda del Mapa de Locura que se recorre (null = ninguna). */
    activePath: PrestigeUpgrade? = null,
    abandonPath: Boolean = false,
    onAbandonPathChanged: (Boolean) -> Unit = {},
    /** Pruebas de Locura que se pueden elegir para la próxima vida (vacío = no mostrar). */
    trials: List<Trial> = emptyList(),
    completedTrials: Set<Trial> = emptySet(),
    selectedTrial: Trial? = null,
    onTrialSelected: (Trial?) -> Unit = {},
    /** Mutaciones que ofrece esta Ascensión (vacío = no mostrar). */
    mutationOffer: List<Mutation> = emptyList(),
    selectedMutation: Mutation? = null,
    onMutationSelected: (Mutation?) -> Unit = {}
) {
    // Cada elección se abre en su propio selector; el resumen queda corto
    var open by remember { mutableStateOf<AscendChoice?>(null) }
    var showDetails by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ascend_question), color = AncientGold, fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.ascend_gain, sanityGainFormatted),
                    color = ArcanePurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                // Una sola pista de ritmo: cuándo se duplicaría (o el siguiente punto)
                pacing?.let {
                    Text(
                        if (it.doubleAt != null) stringResource(R.string.sanity_double_short, it.doubleAt)
                        else stringResource(R.string.ascend_next_point, it.nextGain, it.nextAt),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // La próxima vida: una fila por elección
                if (pacts.isNotEmpty()) {
                    ChoiceRow(stringResource(R.string.ascend_row_pact), tr(selectedPact.godName), selectedPact.sigilColor(), AscendChoice.PACT) { open = it }
                }
                if (trials.isNotEmpty()) {
                    ChoiceRow(
                        stringResource(R.string.ascend_row_trial),
                        selectedTrial?.let { tr(it.title) } ?: stringResource(R.string.trial_none),
                        if (selectedTrial != null) Color(0xFFCF6679) else Color.Gray,
                        AscendChoice.TRIAL
                    ) { open = it }
                }
                if (mutationOffer.isNotEmpty()) {
                    ChoiceRow(
                        stringResource(R.string.ascend_row_mutation),
                        selectedMutation?.let { "🧬 " + tr(it.title) } ?: stringResource(R.string.mutation_none),
                        if (selectedMutation != null) MutationGreen else Color.Gray,
                        AscendChoice.MUTATION
                    ) { open = it }
                }
                if (activePath != null) {
                    ChoiceRow(
                        stringResource(R.string.ascend_row_path),
                        stringResource(if (abandonPath) R.string.ascend_path_abandon else R.string.ascend_path_keep, tr(activePath.name)),
                        if (abandonPath) Color(0xFFE57373) else AncientGold,
                        AscendChoice.PATH
                    ) { open = it }
                }

                // Lo demás, plegado: qué se pierde, el ritmo completo y los hitos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDetails = !showDetails }
                        .padding(vertical = 10.dp)
                        .testTag(ASCEND_DETAILS_TAG)
                ) {
                    Text(if (showDetails) "▾" else "▸", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ascend_details), color = Color.Gray, fontSize = 12.sp)
                }
                if (showDetails) {
                    Text(stringResource(R.string.ascend_lose), color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stringResource(R.string.ascend_keep), color = Color.Gray, fontSize = 12.sp)
                    if (pacing != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.ascend_generated, pacing.generated), color = Color.Gray, fontSize = 12.sp)
                        Text(stringResource(R.string.ascend_next_point, pacing.nextGain, pacing.nextAt), color = Color.Gray, fontSize = 12.sp)
                        if (pacing.doubleGain != null && pacing.doubleAt != null) {
                            Text(stringResource(R.string.ascend_double, pacing.doubleGain, pacing.doubleAt), color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    if (ascensions != null) AscensionMilestoneList(ascensions)
                }
            }
        },
        containerColor = Color(0xFF1E1E1E),
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black)
            ) {
                Text(stringResource(R.string.ascend), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Color.Gray) }
        }
    )

    // El selector de la elección abierta, encima del resumen
    open?.let { choice ->
        AlertDialog(
            onDismissRequest = { open = null },
            containerColor = Color(0xFF1E1E1E),
            modifier = Modifier.testTag(ASCEND_PICKER_TAG),
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    when (choice) {
                        AscendChoice.PACT -> PactPicker(pacts, selectedPact, onPactSelected, buildingNames)
                        AscendChoice.TRIAL -> TrialPicker(trials, completedTrials, selectedTrial, onTrialSelected)
                        AscendChoice.MUTATION -> MutationPicker(mutationOffer, selectedMutation, onMutationSelected)
                        AscendChoice.PATH -> activePath?.let { AbandonPathOption(it, abandonPath, onAbandonPathChanged) }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { open = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black),
                    modifier = Modifier.testTag(ASCEND_PICKER_DONE_TAG)
                ) { Text(stringResource(R.string.ascend_choice_done), fontWeight = FontWeight.Bold) }
            }
        )
    }
}

/** Las elecciones de la próxima vida que se abren desde la confirmación de Ascender. */
enum class AscendChoice { PACT, TRIAL, MUTATION, PATH }

/** Una fila de la confirmación: qué se elige, lo elegido y una flecha para cambiarlo. */
@Composable
private fun ChoiceRow(label: String, value: String, valueColor: Color, choice: AscendChoice, onOpen: (AscendChoice) -> Unit) {
    Surface(
        onClick = { onOpen(choice) },
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF262626),
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).testTag(ASCEND_ROW_TAG + choice.name.lowercase())
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.width(76.dp))
            Text(
                value,
                color = valueColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text("›", color = Color.Gray, fontSize = 18.sp)
        }
    }
}

const val ASCEND_ROW_TAG = "ascend_row_"
const val ASCEND_DETAILS_TAG = "ascend_details"
const val ASCEND_PICKER_TAG = "ascend_picker"
const val ASCEND_PICKER_DONE_TAG = "ascend_picker_done"

/** Cambiar de Senda solo se puede al Ascender: se abandona y se recupera su Cordura. */
@Composable
private fun AbandonPathOption(path: PrestigeUpgrade, checked: Boolean, onChange: (Boolean) -> Unit) {
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .testTag(ABANDON_PATH_TAG)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(checkedColor = AncientGold, uncheckedColor = Color.Gray, checkmarkColor = Color.Black)
        )
        Column {
            Text(
                stringResource(R.string.ascend_abandon_path, tr(path.name), MadnessPaths.spent(path).toBeautifulString()),
                color = if (checked) AncientGold else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.ascend_abandon_hint), color = Color.Gray, fontSize = 11.sp)
        }
    }
}

const val ABANDON_PATH_TAG = "abandon_path"

@Composable
fun OfflineGainsDialog(timeFormatted: String, gainsFormatted: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.offline_title), color = ArcanePurple, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.offline_text), color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.offline_time, timeFormatted), color = Color.White)
                Text(text = stringResource(R.string.offline_gain, gainsFormatted), color = Color(0xFF4CAF50), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple)) {
                Text(stringResource(R.string.accept))
            }
        },
        containerColor = Color(0xFF1E1E1E),
        textContentColor = Color.White
    )
}

@Composable
fun RefundDialog(refundAmount: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.refund_title), color = AncientGold, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.refund_text1), color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.refund_text2), color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.refund_amount, refundAmount), color = ArcanePurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black)) {
                Text(stringResource(R.string.explore))
            }
        },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(BorderStroke(1.dp, AncientGold), RoundedCornerShape(16.dp))
    )
}

/** Los textos del ritmo de la Ascensión, ya formateados. */
data class AscendPacing(
    val generated: String,
    val nextGain: String,
    val nextAt: String,
    val doubleGain: String?,
    val doubleAt: String?
)

/**
 * Los hitos de Ascensión, con lo que trae ESTA Ascensión resaltado. [ascensions] son
 * las ya hechas: al confirmar se suma una.
 */
@Composable
private fun AscensionMilestoneList(ascensions: Int) {
    val afterThis = ascensions + 1
    Spacer(modifier = Modifier.height(12.dp))
    Text(stringResource(R.string.ascension_milestones_title), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Text(stringResource(R.string.ascension_count, afterThis), color = Color.Gray, fontSize = 11.sp)
    AscensionMilestone.entries.forEach { milestone ->
        val (mark, color) = when {
            milestone.ascensions == afterThis -> "★" to AncientGold
            milestone.isReached(ascensions) -> "✓" to Color(0xFF81C784)
            else -> "·" to Color.Gray
        }
        Text(
            "$mark ${milestone.ascensions}: ${stringResource(milestoneText(milestone))}",
            color = color,
            fontSize = 12.sp
        )
    }
}

@StringRes
fun milestoneText(milestone: AscensionMilestone): Int = when (milestone) {
    AscensionMilestone.STARTING_SYNAPSES -> R.string.milestone_starting_synapses
    AscensionMilestone.KEEP_CLICK_UPGRADES -> R.string.milestone_keep_click
    AscensionMilestone.BUILDING_AUTOBUYER -> R.string.milestone_building_autobuyer
    AscensionMilestone.GRIMOIRE_AUTOBUYER -> R.string.milestone_grimoire_autobuyer
    AscensionMilestone.KEEP_SYNERGIES -> R.string.milestone_keep_synergies
    AscensionMilestone.AUTO_RITUAL -> R.string.milestone_auto_ritual
}

/** Elegir el pacto de la próxima vida, con lo que da y lo que quita el elegido. */
@Composable
private fun PactPicker(pacts: List<Pact>, selected: Pact, onSelect: (Pact) -> Unit, buildingNames: Map<String, String>) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(stringResource(R.string.pact_choose_title), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp)
    ) {
        pacts.forEach { pact ->
            val isSelected = pact == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(64.dp)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) pact.sigilColor() else Color(0xFF333333),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(pact) }
                    .padding(4.dp)
                    .testTag("$PACT_CHOICE_TAG${pact.id}")
            ) {
                PactSigil(pact, Modifier.size(44.dp))
                Text(tr(pact.godName), color = Color.White, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
    Text("${tr(selected.godName)} — ${tr(selected.epithet)}", color = selected.sigilColor(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    PactEffectList(pactLines(selected.effects, buildingNames), fontSize = 11)
}

const val PACT_CHOICE_TAG = "pact_choice_"

