package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.AscensionMilestone
import com.EGomez.idleecosystem.domain.logic.AutomationSettings
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.Ritual
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple

/**
 * El panel de automatización: qué compran los autocompradores, cuánto pueden gastar,
 * qué ritual se activa solo y si la Ascensión es automática. Cada sección aparece
 * con su hito de Ascensión; las que faltan dicen con cuántas Ascensiones se abren.
 */
@Composable
fun AutomationDialog(
    settings: AutomationSettings,
    ascensions: Int,
    buildings: List<Building>,
    rituals: List<Ritual>,
    onBudgetSelected: (Int) -> Unit,
    onToggleBuildings: () -> Unit,
    onToggleBuildingExcluded: (String) -> Unit,
    onToggleGrimoire: () -> Unit,
    onToggleAutoAscend: () -> Unit,
    onRitualSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            border = BorderStroke(1.dp, AncientGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.automation_title), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, stringResource(R.string.close), tint = Color.Gray) }
                }
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- Presupuesto ---
                    SectionTitle(stringResource(R.string.automation_budget_title))
                    Text(stringResource(R.string.automation_budget, settings.budgetPercent), color = Color.Gray, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AutomationSettings.BUDGET_OPTIONS.forEach { percent ->
                            FilterChip(
                                selected = settings.budgetPercent == percent,
                                onClick = { onBudgetSelected(percent) },
                                label = { Text("$percent%") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AncientGold,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF333333),
                                    labelColor = Color.Gray
                                ),
                                border = null
                            )
                        }
                    }

                    // --- Edificios ---
                    SectionTitle(stringResource(R.string.automation_buildings))
                    if (AscensionMilestone.BUILDING_AUTOBUYER.isReached(ascensions)) {
                        SwitchRow(stringResource(R.string.automation_buildings_hint), settings.buildings, onToggleBuildings)
                        buildings.forEach { building ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = settings.buildings) { onToggleBuildingExcluded(building.id) }
                            ) {
                                Checkbox(
                                    checked = building.id !in settings.excludedBuildings,
                                    onCheckedChange = { onToggleBuildingExcluded(building.id) },
                                    enabled = settings.buildings,
                                    colors = CheckboxDefaults.colors(checkedColor = ArcanePurple)
                                )
                                Text(tr(building.name), color = if (settings.buildings) Color.White else Color.Gray, fontSize = 13.sp)
                            }
                        }
                    } else Locked(AscensionMilestone.BUILDING_AUTOBUYER)

                    // --- Grimorio y Ascensión automática ---
                    SectionTitle(stringResource(R.string.automation_grimoire))
                    if (AscensionMilestone.GRIMOIRE_AUTOBUYER.isReached(ascensions)) {
                        SwitchRow(stringResource(R.string.automation_grimoire_hint), settings.grimoire, onToggleGrimoire)
                    } else Locked(AscensionMilestone.GRIMOIRE_AUTOBUYER)

                    SectionTitle(stringResource(R.string.automation_ascend))
                    if (AscensionMilestone.GRIMOIRE_AUTOBUYER.isReached(ascensions)) {
                        SwitchRow(
                            stringResource(R.string.automation_ascend_hint),
                            settings.autoAscend,
                            onToggleAutoAscend,
                            modifier = Modifier.testTag(AUTO_ASCEND_TAG)
                        )
                    } else Locked(AscensionMilestone.GRIMOIRE_AUTOBUYER)

                    // --- Ritual automático ---
                    SectionTitle(stringResource(R.string.automation_ritual))
                    if (AscensionMilestone.AUTO_RITUAL.isReached(ascensions)) {
                        RitualChoice(stringResource(R.string.automation_ritual_none), settings.ritualId == null) { onRitualSelected(null) }
                        rituals.forEach { ritual ->
                            RitualChoice(tr(ritual.name), settings.ritualId == ritual.id) { onRitualSelected(ritual.id) }
                        }
                    } else Locked(AscensionMilestone.AUTO_RITUAL)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = AncientGold,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
    )
}

@Composable
private fun SwitchRow(text: String, checked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Text(text, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedThumbColor = ArcanePurple, checkedTrackColor = ArcanePurple.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun RitualChoice(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        RadioButton(selected = selected, onClick = onSelect, colors = RadioButtonDefaults.colors(selectedColor = ArcanePurple))
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun Locked(milestone: AscensionMilestone) {
    Text(stringResource(R.string.automation_locked, milestone.ascensions), color = Color.Gray, fontSize = 12.sp)
}

const val AUTO_ASCEND_TAG = "auto_ascend"
