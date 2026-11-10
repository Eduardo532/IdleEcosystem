package com.EGomez.idleecosystem.ui.screens.main.components

import com.EGomez.idleecosystem.ui.i18n.tr
import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.ui.screens.main.dialogs.EtherColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import com.EGomez.idleecosystem.domain.logic.BuildingMilestones
import com.EGomez.idleecosystem.domain.logic.GrimoireShopping
import com.EGomez.idleecosystem.domain.logic.SystemReveal
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.ui.components.sigilColor
import com.EGomez.idleecosystem.domain.logic.AscensionMilestone
import com.EGomez.idleecosystem.domain.logic.Attention
import com.EGomez.idleecosystem.domain.logic.AutomationSettings
import com.EGomez.idleecosystem.ui.components.AutoChip
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.BuildingType
import com.EGomez.idleecosystem.domain.model.Ritual
import com.EGomez.idleecosystem.domain.model.RitualEffectType
import com.EGomez.idleecosystem.domain.model.RitualState
import com.EGomez.idleecosystem.domain.model.Upgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import com.EGomez.idleecosystem.ui.components.BuildingItem
import com.EGomez.idleecosystem.ui.components.UpgradeItem
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import java.math.BigDecimal

@Composable
fun MenuSection(
    isMenuExpanded: Boolean,
    onToggleMenu: () -> Unit,
    menuWeight: Float,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    buyMultiplier: Int,
    onBuyMultiplierChanged: (Int) -> Unit,
    showPurchased: Boolean,
    onToggleShowPurchased: () -> Unit,
    buildings: List<Building>,
    upgrades: List<Upgrade>,
    rituals: List<Ritual>,
    currentKnowledge: BigDecimal,
    currentEchoes: BigDecimal,
    globalMultiplier: BigDecimal,
    buildingCalculator: BuildingCalculator,
    buildingCostMultiplier: Double, // NUEVO PARÁMETRO
    buildingMilestones: Map<String, List<Int>> = emptyMap(),
    onBuyBuilding: (String) -> Unit,
    onBuyUpgrade: (String) -> Unit,
    onBuyAllUpgrades: () -> Unit = {},
    onActivateRitual: (String) -> Unit,
    prestigeCurrencyFormatted: String,
    canPrestige: Boolean,
    potentialPrestigeFormatted: String,
    doubleSanityAtFormatted: String? = null,
    revealed: SystemReveal = SystemReveal(),
    ascensions: Int = 0,
    automation: AutomationSettings = AutomationSettings(),
    onToggleAutoBuildings: () -> Unit = {},
    onToggleAutoGrimoire: () -> Unit = {},
    onToggleAutoRitual: (String) -> Unit = {},
    onOpenAutomation: () -> Unit = {},
    activePact: Pact = Pact.NONE,
    chaosMultiplier: Int = 1,
    onPactSelected: (Pact) -> Unit = {},
    attention: Map<String, Int> = emptyMap(),
    attentionPoints: Int = 0,
    onOpenAttention: () -> Unit = {},
    activeTrial: Trial? = null,
    trialProgress: Float = 0f,
    completedTrials: Set<Trial> = emptySet(),
    deliriumMultiplier: Double = 1.0,
    earlyPacts: Set<Pact> = emptySet(),
    onAbandonTrial: () -> Unit = {},
    runeSlots: Int = 0,
    engravedRunes: List<String> = emptyList(),
    runeCatches: Map<String, Int> = emptyMap(),
    onEngraveRune: (String) -> Unit = {},
    onRemoveEngraving: (String) -> Unit = {},
    transcendRevealed: Boolean = false,
    ether: Int = 0,
    onOpenTranscend: () -> Unit = {},
    onAscendClicked: () -> Unit,
    onShowPrestigeShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Un sistema sin descubrir se ve como "???" hasta cumplir su requisito
    val tabs = listOf(
        stringResource(R.string.tab_research),
        if (revealed.grimoire) stringResource(R.string.tab_grimoire) else LOCKED_TITLE,
        if (revealed.rituals) stringResource(R.string.tab_rituals) else LOCKED_TITLE,
        // El Panteón se descubre junto con la Ascensión, donde se eligen los pactos
        if (revealed.ascension) stringResource(R.string.tab_pantheon) else LOCKED_TITLE
    )
    val lockedTabText = when {
        selectedTabIndex == 1 && !revealed.grimoire ->
            stringResource(R.string.locked_grimoire, SystemReveal.GRIMOIRE_AT.toBeautifulString())
        selectedTabIndex == 2 && !revealed.rituals -> stringResource(R.string.locked_rituals)
        selectedTabIndex == 3 && !revealed.ascension ->
            stringResource(R.string.locked_pantheon, BigDecimal("1000000").toBeautifulString())
        // Pruebas: Grimorio Quemado y Silencio cierran su pestaña durante la vida
        selectedTabIndex == 1 && activeTrial?.rules?.noGrimoire == true -> activeTrial?.let { "${tr(it.title)}: ${tr(it.rule)}" }
        selectedTabIndex == 2 && activeTrial?.rules?.noRituals == true -> activeTrial?.let { "${tr(it.title)}: ${tr(it.rule)}" }
        else -> null
    }
    // Pestaña Panteón: los dioses o las Pruebas de Locura
    var pantheonView by rememberSaveable { mutableStateOf(PantheonView.GODS) }
    val trialsRevealed = ascensions + 1 >= Trial.REVEAL_AT

    Column(
        modifier = modifier
            .background(Color(0xFF161616), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleMenu() }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMenuExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }

        if (menuWeight > 0.2f) {
            if (selectedTabIndex == 0) {
                val milestoneLabel = stringResource(R.string.buy_milestone_chip)
                // Solo los multiplicadores de compra: seis botones del mismo ancho que
                // siempre caben (lo automático vive en la barra fija bajo las pestañas)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(1, 5, 10, 100, BuildingMilestones.BUY_TO_MILESTONE, -1).forEach { mult ->
                        val label = when (mult) {
                            -1 -> "MAX"
                            BuildingMilestones.BUY_TO_MILESTONE -> milestoneLabel
                            else -> "x$mult"
                        }
                        BuyMultiplierChip(
                            label = label,
                            selected = buyMultiplier == mult,
                            onClick = { onBuyMultiplierChanged(mult) },
                            modifier = Modifier.weight(1f).testTag(BUY_MULTIPLIER_TAG + mult)
                        )
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = ArcanePurple,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = ArcanePurple
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                        selectedContentColor = ArcanePurple,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            // Barra fija sobre la lista de edificios (no se desplaza con ella): la Atención
            // y, con el hito de Ascensión, el autocomprador y su panel
            val autobuyer = AscensionMilestone.BUILDING_AUTOBUYER.isReached(ascensions)
            if (selectedTabIndex == 0 && lockedTabText == null && (attentionPoints > 0 || autobuyer)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 8.dp)
                ) {
                    if (attentionPoints > 0) {
                        AttentionBar(
                            attention = attention,
                            points = attentionPoints,
                            buildings = buildings,
                            onClick = onOpenAttention,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (autobuyer) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AutoChip(
                            selected = automation.buildings,
                            onClick = onToggleAutoBuildings,
                            modifier = Modifier.testTag(AUTO_BUILDINGS_TAG)
                        )
                        // Panel de automatización: qué edificios, presupuesto, Ascensión automática
                        IconButton(
                            onClick = onOpenAutomation,
                            modifier = Modifier.size(36.dp).testTag(AUTOMATION_SETTINGS_TAG)
                        ) {
                            Icon(Icons.Default.Settings, stringResource(R.string.cd_automation_settings), tint = AncientGold)
                        }
                    }
                }
            }

            // Prueba de Locura en curso: barra fija con su avance; abre la lista de Pruebas
            if (selectedTabIndex == 0 && activeTrial != null) {
                TrialBar(activeTrial, trialProgress, deliriumMultiplier) {
                    pantheonView = PantheonView.TRIALS
                    onTabSelected(3)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                if (lockedTabText != null) {
                    item { LockedSystem(lockedTabText) }
                } else when (selectedTabIndex) {
                    0 -> {
                        items(buildings, key = { it.id }) { building ->
                            BuildingItem(
                                building = building,
                                currentFunds = currentKnowledge,
                                globalMultiplier = globalMultiplier,
                                calculator = buildingCalculator,
                                buyMultiplier = buyMultiplier,
                                costMultiplier = buildingCostMultiplier, // PASAMOS EL DESCUENTO
                                milestones = buildingMilestones[building.id].orEmpty(),
                                attentionPoints = attention[building.id] ?: 0,
                                onBuyClick = { onBuyBuilding(building.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    1 -> {
                        item {
                            // Cuántas mejoras alcanzan, compradas de la más barata a la más cara
                            val affordableCount = remember(upgrades, currentKnowledge) {
                                GrimoireShopping.affordableCount(upgrades, currentKnowledge)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = onBuyAllUpgrades,
                                    enabled = affordableCount > 0,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AncientGold,
                                        contentColor = Color.Black,
                                        disabledContainerColor = Color(0xFF333333),
                                        disabledContentColor = Color.Gray
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        if (affordableCount > 0) stringResource(R.string.buy_all_count, affordableCount) else stringResource(R.string.buy_all),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                // Autocomprador del Grimorio (hito de Ascensión)
                                if (AscensionMilestone.GRIMOIRE_AUTOBUYER.isReached(ascensions)) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AutoChip(selected = automation.grimoire, onClick = onToggleAutoGrimoire)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    stringResource(R.string.show_purchased),
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Switch(
                                    checked = showPurchased,
                                    onCheckedChange = { onToggleShowPurchased() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = ArcanePurple,
                                        checkedTrackColor = ArcanePurple.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        if (upgrades.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stringResource(R.string.no_secrets), color = Color.Gray)
                                }
                            }
                        } else {
                            items(upgrades, key = { it.id }) { upgrade ->
                                val targetName = if (upgrade.type == UpgradeType.CLICK_MULTIPLIER) stringResource(R.string.target_mind)
                                else if (upgrade.type == UpgradeType.GLOBAL_MULTIPLIER) stringResource(R.string.target_all)
                                else buildings.find { it.id == upgrade.targetBuildingId }?.name?.let { tr(it) } ?: "?"

                                // Una sinergia no multiplica por un valor fijo: suma por unidad
                                val effectText = if (upgrade.type == UpgradeType.SYNERGY) {
                                    val sourceName = buildings.find { it.id == upgrade.sourceBuildingId }?.name?.let { tr(it) } ?: "?"
                                    stringResource(R.string.upgrade_effect_synergy, sourceName)
                                } else null

                                UpgradeItem(
                                    upgrade = upgrade,
                                    targetInfo = stringResource(R.string.affects, targetName),
                                    effectText = effectText,
                                    currentFunds = currentKnowledge,
                                    onBuy = { onBuyUpgrade(upgrade.id) }
                                )
                            }
                        }
                    }
                    2 -> {
                        // --- CONTADOR DE ECOS ---
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.echoes_available, currentEchoes.toBeautifulString()),
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        // ------------------------

                        items(rituals, key = { it.id }) { ritual ->
                            RitualItem(
                                ritual = ritual,
                                currentFunds = currentEchoes,
                                autoSelected = if (AscensionMilestone.AUTO_RITUAL.isReached(ascensions)) automation.ritualId == ritual.id else null,
                                onToggleAuto = { onToggleAutoRitual(ritual.id) },
                                onActivate = { onActivateRitual(ritual.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    3 -> {
                        if (trialsRevealed) {
                            item {
                                PantheonViewChips(
                                    pantheonView,
                                    stringResource(R.string.pantheon_view_trials, completedTrials.size, Trial.entries.size),
                                    stringResource(R.string.pantheon_view_runes, engravedRunes.size, runeSlots)
                                ) { pantheonView = it }
                            }
                        }
                        val view = if (trialsRevealed) pantheonView else PantheonView.GODS
                        when (view) {
                            PantheonView.TRIALS -> item { TrialList(ascensions, activeTrial, trialProgress, completedTrials, onAbandonTrial) }
                            PantheonView.RUNES -> item { RuneAltar(runeSlots, engravedRunes, runeCatches, onEngraveRune, onRemoveEngraving) }
                            PantheonView.GODS -> item { PantheonGallery(ascensions, activePact, onPactSelected, earlyPacts) }
                        }
                    }
                }
            }

            if (isMenuExpanded && !revealed.ascension) {
                HorizontalDivider(color = Color(0xFF333333))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D0D0D))
                        .padding(12.dp)
                        .testTag(LOCKED_ASCENSION_TAG)
                ) {
                    LockedSystem(stringResource(R.string.locked_ascension, BigDecimal("1000000").toBeautifulString()))
                }
            } else if (isMenuExpanded) {
                HorizontalDivider(color = Color(0xFF333333))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D0D0D))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            stringResource(R.string.residual_sanity_title),
                            color = Color(0xFFCF6679),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            prestigeCurrencyFormatted,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // El pacto de esta vida (con Azathoth, el caos actual)
                        if (activePact != Pact.NONE) {
                            Text(
                                if (activePact.effects.chaos) "${tr(activePact.godName)} · x$chaosMultiplier" else tr(activePact.godName),
                                color = activePact.sigilColor(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag(ACTIVE_PACT_TAG)
                            )
                        }
                        // Trascender (v0.18.0): el Éter y el acceso a la segunda capa
                        if (transcendRevealed) {
                            Text(
                                stringResource(R.string.transcend_link, ether),
                                color = EtherColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable(onClick = onOpenTranscend)
                                    .padding(vertical = 2.dp)
                                    .testTag(TRANSCEND_LINK_TAG)
                            )
                        }
                        // Cuándo conviene ascender: el punto en que la recompensa se duplica
                        if (canPrestige && doubleSanityAtFormatted != null) {
                            Text(
                                stringResource(R.string.sanity_double_short, doubleSanityAtFormatted),
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Row {
                        Button(
                            onClick = onShowPrestigeShop,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4A148C),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(stringResource(R.string.shop_button), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onAscendClicked,
                            enabled = canPrestige,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFCF6679),
                                disabledContainerColor = Color(0xFF333333),
                                contentColor = Color.Black,
                                disabledContentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.ascend), fontWeight = FontWeight.Black)
                                if (canPrestige) {
                                    Text(
                                        "+$potentialPrestigeFormatted",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(stringResource(R.string.ascend_requirement, BigDecimal("1000000").toBeautifulString()), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                stringResource(R.string.swipe_hint),
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

private const val LOCKED_TITLE = "???"
const val LOCKED_ASCENSION_TAG = "locked_ascension"
const val AUTO_BUILDINGS_TAG = "auto_buildings"
const val AUTOMATION_SETTINGS_TAG = "automation_settings"
const val ACTIVE_PACT_TAG = "active_pact"
const val TRANSCEND_LINK_TAG = "transcend_link"
const val ATTENTION_BAR_TAG = "attention_bar"

/** Un sistema sin descubrir: un candado y lo que falta para verlo. */
/** Botón de multiplicador de compra: compacto y del mismo ancho que los demás. */
@Composable
private fun BuyMultiplierChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) ArcanePurple else Color(0xFF333333),
        modifier = modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                color = if (selected) Color.Black else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}

const val BUY_MULTIPLIER_TAG = "buy_multiplier_"

/** Barra fija de la Atención: puntos usados y a dónde apuntan; al tocarla abre el reparto. */
@Composable
private fun AttentionBar(
    attention: Map<String, Int>,
    points: Int,
    buildings: List<Building>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val used = Attention.used(attention)
    val dream = stringResource(R.string.attention_dream)
    val vigil = stringResource(R.string.attention_vigil)
    val names = buildings.associate { it.id to tr(it.name) }
    val targets = attention.filterValues { it > 0 }.entries
        .sortedByDescending { it.value }
        .joinToString(" · ") { (id, n) ->
            val name = when (id) {
                Attention.DREAM -> dream
                Attention.VIGIL -> vigil
                else -> names[id] ?: id
            }
            "$name +${(n * Attention.BONUS_PER_POINT * 100).toInt()}%"
        }
    val free = points - used

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF221530),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (free > 0) AncientGold.copy(alpha = 0.7f) else ArcanePurple.copy(alpha = 0.35f)
        ),
        modifier = modifier.testTag(ATTENTION_BAR_TAG)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("◎", color = AncientGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.attention_bar_title, used, points),
                    color = AncientGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when {
                        targets.isEmpty() -> stringResource(R.string.attention_bar_empty)
                        free > 0 -> stringResource(R.string.attention_bar_free, targets, free)
                        else -> targets
                    },
                    color = if (targets.isEmpty()) Color.LightGray else Color(0xFFBFA9D9),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LockedSystem(requirement: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(requirement, color = Color.Gray, fontSize = 13.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun MenuSectionPreview() {
    val dummyBuildings = listOf(
        Building(
            id = "1",
            name = "Nexo",
            description = "Genera energía",
            baseCost = BigDecimal("10"),
            baseProduction = BigDecimal("1"),
            growthFactor = BigDecimal("1.15"),
            amountOwned = 0,
            isVisible = true,
            type = BuildingType.PASSIVE,
            currentMultiplier = BigDecimal.ONE
        )
    )

    val dummyRituals = listOf(
        Ritual(
            id = "r1",
            name = "Ritual de Sangre",
            description = "Poderoso",
            baseCost = BigDecimal("5"),
            durationSeconds = 10,
            cooldownSeconds = 60,
            effectType = RitualEffectType.CLICK_MULTIPLIER,
            effectValue = 2.0,
            state = RitualState.READY,
            timeRemaining = 0,
            totalTimeForProgress = 10
        )
    )

    MenuSection(
        isMenuExpanded = true,
        onToggleMenu = {},
        menuWeight = 0.6f,
        selectedTabIndex = 2,
        onTabSelected = {},
        buyMultiplier = 1,
        onBuyMultiplierChanged = {},
        showPurchased = false,
        onToggleShowPurchased = {},
        buildings = dummyBuildings,
        upgrades = emptyList(),
        rituals = dummyRituals,
        currentKnowledge = BigDecimal("1000"),
        currentEchoes = BigDecimal("10"),
        globalMultiplier = BigDecimal.ONE,
        buildingCalculator = BuildingCalculator(),
        buildingCostMultiplier = 1.0,
        onBuyBuilding = {},
        onBuyUpgrade = {},
        onActivateRitual = {},
        prestigeCurrencyFormatted = "0",
        canPrestige = false,
        potentialPrestigeFormatted = "0",
        onAscendClicked = {},
        onShowPrestigeShop = {}
    )
}