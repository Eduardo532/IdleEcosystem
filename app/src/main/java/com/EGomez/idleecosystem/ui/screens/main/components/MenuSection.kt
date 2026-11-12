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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.EGomez.idleecosystem.domain.model.Trial
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
import com.EGomez.idleecosystem.domain.logic.MenuLayer
import com.EGomez.idleecosystem.domain.logic.MenuTab
import com.EGomez.idleecosystem.domain.logic.MenuUnlocks
import com.EGomez.idleecosystem.domain.model.Mutation
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
    selectedTab: MenuTab,
    onTabSelected: (MenuTab) -> Unit,
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
    buildingCostMultiplier: Double,
    buildingMilestones: Map<String, List<Int>> = emptyMap(),
    onBuyBuilding: (String) -> Unit,
    onBuyUpgrade: (String) -> Unit,
    onBuyAllUpgrades: () -> Unit = {},
    onActivateRitual: (String) -> Unit,
    prestigeCurrencyFormatted: String,
    canPrestige: Boolean,
    potentialPrestigeFormatted: String,
    doubleSanityAtFormatted: String? = null,
    /** Interfaz por capas (v0.19.0): qué pestañas se ven y cuáles son nuevas. */
    unlocks: MenuUnlocks = MenuUnlocks.ALL,
    newTabs: Set<MenuTab> = emptySet(),
    ascensions: Int = 0,
    /** Ascensiones que cuentan para los hitos (Memoria de la Máquina las completa). */
    milestoneAscensions: Int = ascensions,
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
    veilBroken: Boolean = false,
    veilPressing: Boolean = false,
    veilFactor: java.math.BigDecimal = java.math.BigDecimal.ONE,
    onBreakVeil: () -> Unit = {},
    starsUi: StarsUi = StarsUi(),
    onBuyStar: (String) -> Unit = {},
    runeSlots: Int = 0,
    engravedRunes: List<String> = emptyList(),
    runeCatches: Map<String, Int> = emptyMap(),
    onEngraveRune: (String) -> Unit = {},
    onRemoveEngraving: (String) -> Unit = {},
    runeMastery: Map<String, Int> = emptyMap(),
    activeMutation: Mutation? = null,
    seenMutations: Set<String> = emptySet(),
    cycleAscensions: Int = 0,
    etherGain: Int = 0,
    ether: Int = 0,
    eternalLevels: Map<String, Int> = emptyMap(),
    transcendences: Int = 0,
    transcendRequired: Int = com.EGomez.idleecosystem.domain.logic.Transcendence.REQUIRED_ASCENSIONS,
    onBuyEternal: (String) -> Unit = {},
    onTranscend: () -> Unit = {},
    consecrationRevealed: Boolean = false,
    consecrations: Map<String, Int> = emptyMap(),
    onConsecrate: (String) -> Unit = {},
    onAscendClicked: () -> Unit,
    onOpenMadnessMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tab = unlocks.resolve(selectedTab)
    // Pruebas: Grimorio Quemado y Silencio cierran su pestaña durante la vida
    val lockedTabText = when {
        tab == MenuTab.GRIMOIRE && activeTrial?.rules?.noGrimoire == true -> activeTrial?.let { "${tr(it.title)}: ${tr(it.rule)}" }
        tab == MenuTab.RITUALS && activeTrial?.rules?.noRituals == true -> activeTrial?.let { "${tr(it.title)}: ${tr(it.rule)}" }
        else -> null
    }
    // Los multiplicadores de compra aparecen cuando ya hay algo que comprar en cantidad
    val showBuyMultipliers = ascensions > 0 || buildings.sumOf { it.amountOwned } >= BUY_MULTIPLIERS_AT

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
            // Capas: Vida, Ascensión y Trascendencia (solo si hay más de una)
            val layers = unlocks.layers
            // El autocomprador (hito de Ascensión) va al final de la fila de capas
            val autobuyer = AscensionMilestone.BUILDING_AUTOBUYER.isReached(milestoneAscensions)
            val autoControls: (@Composable RowScope.() -> Unit)? = if (tab == MenuTab.RESEARCH && autobuyer) {
                {
                    AutoChip(
                        selected = automation.buildings,
                        onClick = onToggleAutoBuildings,
                        modifier = Modifier.testTag(AUTO_BUILDINGS_TAG)
                    )
                    IconButton(
                        onClick = onOpenAutomation,
                        modifier = Modifier.size(32.dp).testTag(AUTOMATION_SETTINGS_TAG)
                    ) {
                        Icon(Icons.Default.Settings, stringResource(R.string.cd_automation_settings), tint = AncientGold)
                    }
                }
            } else null
            if (layers.size > 1 || autoControls != null) {
                LayerBar(layers.takeIf { it.size > 1 }.orEmpty(), tab.layer, newTabs, autoControls) { layer ->
                    onTabSelected(unlocks.tabsOf(layer).first())
                }
            }

            // Pestañas de la capa actual (una sola pestaña no necesita fila)
            val layerTabs = unlocks.tabsOf(tab.layer)
            if (layerTabs.size > 1) {
                SubTabRow(layerTabs, tab, tab.layer.color(), newTabs, onTabSelected)
            }

            if (tab == MenuTab.RESEARCH && showBuyMultipliers) {
                val milestoneLabel = stringResource(R.string.buy_milestone_chip)
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

            // Prueba de Locura en curso: barra fija con su avance; abre la lista de Pruebas
            if (tab == MenuTab.RESEARCH && activeTrial != null) {
                TrialBar(activeTrial, trialProgress, deliriumMultiplier) { onTabSelected(MenuTab.TRIALS) }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).testTag(MENU_CONTENT_TAG + tab.id),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                if (lockedTabText != null) {
                    item { LockedSystem(lockedTabText) }
                } else when (tab) {
                    MenuTab.RESEARCH -> {
                        if (veilPressing) item(key = "veil_banner") { VeilBanner(veilFactor) }
                        items(buildings, key = { it.id }) { building ->
                            BuildingItem(
                                building = building,
                                currentFunds = currentKnowledge,
                                globalMultiplier = globalMultiplier,
                                calculator = buildingCalculator,
                                buyMultiplier = if (showBuyMultipliers) buyMultiplier else 1,
                                costMultiplier = buildingCostMultiplier,
                                milestones = buildingMilestones[building.id].orEmpty(),
                                attentionPoints = attention[building.id] ?: 0,
                                consecration = consecrations[building.id] ?: 0,
                                consecrationRevealed = consecrationRevealed,
                                ether = ether,
                                onConsecrate = { onConsecrate(building.id) },
                                onBuyClick = { onBuyBuilding(building.id) }
                            )
                        }
                        // Lo próximo por descubrir, una sola pista a la vez
                        unlocks.next?.let { next -> item { NextUnlockHint(next) } }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    MenuTab.GRIMOIRE -> {
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
                                if (AscensionMilestone.GRIMOIRE_AUTOBUYER.isReached(milestoneAscensions)) {
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
                    MenuTab.RITUALS -> {
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
                        items(rituals, key = { it.id }) { ritual ->
                            RitualItem(
                                ritual = ritual,
                                currentFunds = currentEchoes,
                                autoSelected = if (AscensionMilestone.AUTO_RITUAL.isReached(milestoneAscensions)) automation.ritualId == ritual.id else null,
                                onToggleAuto = { onToggleAutoRitual(ritual.id) },
                                onActivate = { onActivateRitual(ritual.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    MenuTab.SANITY -> item {
                        SanityHub(
                            sanityFormatted = prestigeCurrencyFormatted,
                            canPrestige = canPrestige,
                            potentialPrestigeFormatted = potentialPrestigeFormatted,
                            ascensions = ascensions,
                            attentionPoints = attentionPoints,
                            attentionUsed = Attention.used(attention),
                            automationUnlocked = autobuyer,
                            onOpenMap = onOpenMadnessMap,
                            onOpenAttention = onOpenAttention,
                            onOpenAutomation = onOpenAutomation
                        )
                    }
                    MenuTab.PANTHEON -> item { PantheonGallery(ascensions, activePact, onPactSelected, earlyPacts) }
                    MenuTab.TRIALS -> item { TrialList(ascensions, activeTrial, trialProgress, completedTrials, onAbandonTrial, veilBroken, onBreakVeil) }
                    MenuTab.RUNES -> item { RuneAltar(runeSlots, engravedRunes, runeCatches, onEngraveRune, onRemoveEngraving, runeMastery) }
                    MenuTab.ETHER -> item { EtherPanel(cycleAscensions, etherGain, ether, eternalLevels, onBuyEternal, onTranscend, transcendences, transcendRequired) }
                    MenuTab.MUTATIONS -> item { MutationCatalog(seenMutations, activeMutation) }
                    MenuTab.STARS -> item { StarsPanel(starsUi, onBuyStar) }
                }
            }

            if (isMenuExpanded && unlocks.has(MenuTab.SANITY)) {
                HorizontalDivider(color = Color(0xFF333333))
                // Barra de Ascensión en una sola fila: la Cordura, el pacto y la mutación de
                // esta vida, y ASCENDER. El Mapa de Locura y el Éter tienen su pestaña.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D0D0D))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                stringResource(R.string.residual_sanity_title),
                                color = SanityRose,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(prestigeCurrencyFormatted, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // El pacto de esta vida (con Azathoth, el caos actual)
                            if (activePact != Pact.NONE) {
                                Text(
                                    if (activePact.effects.chaos) "${tr(activePact.godName)} · x$chaosMultiplier" else tr(activePact.godName),
                                    color = activePact.sigilColor(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(end = 8.dp).testTag(ACTIVE_PACT_TAG)
                                )
                            }
                            // La mutación de esta vida (v0.18.4)
                            activeMutation?.let {
                                Text(
                                    "🧬 " + tr(it.title),
                                    color = MutationGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag(ACTIVE_MUTATION_TAG)
                                )
                            }
                        }
                        // Cuándo conviene ascender: el punto en que la recompensa se duplica
                        if (canPrestige && doubleSanityAtFormatted != null) {
                            Text(
                                stringResource(R.string.sanity_double_short, doubleSanityAtFormatted),
                                color = Color.Gray,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                    Button(
                        onClick = onAscendClicked,
                        enabled = canPrestige,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SanityRose,
                            disabledContainerColor = Color(0xFF333333),
                            contentColor = Color.Black,
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(44.dp).testTag(ASCEND_BUTTON_TAG)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.ascend), fontWeight = FontWeight.Black, fontSize = 12.sp)
                            if (canPrestige) {
                                Text("+$potentialPrestigeFormatted", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            } else {
                                Text(stringResource(R.string.ascend_requirement, ASCENSION_REQUIREMENT.toBeautifulString()), fontSize = 8.sp, maxLines = 1)
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

/** Fila de capas: cada una con el color de su moneda y una marca si trae algo nuevo. */
@Composable
private fun LayerBar(
    layers: List<MenuLayer>,
    selected: MenuLayer,
    newTabs: Set<MenuTab>,
    trailing: (@Composable RowScope.() -> Unit)?,
    onSelect: (MenuLayer) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        if (layers.isEmpty()) Spacer(modifier = Modifier.weight(1f))
        layers.forEach { layer ->
            val isSelected = layer == selected
            val color = layer.color()
            Surface(
                onClick = { onSelect(layer) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) color.copy(alpha = 0.18f) else Color(0xFF202020),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else Color(0xFF333333)),
                modifier = Modifier.weight(1f).height(30.dp).testTag(MENU_LAYER_TAG + layer.name.lowercase())
            ) {
                Box(contentAlignment = Alignment.Center) {
                    TabTitle(
                        layer.title(),
                        isNew = newTabs.any { it.layer == layer },
                        color = if (isSelected) color else Color.Gray,
                        fontSize = 12
                    )
                }
            }
        }
        trailing?.invoke(this)
    }
}

/**
 * Pestañas de una capa: reparten el ancho por igual, sin el relleno de las pestañas de
 * Material (que cortaba "Investigación" con tres pestañas), con una línea del color de la
 * capa bajo la elegida.
 */
@Composable
private fun SubTabRow(
    tabs: List<MenuTab>,
    selected: MenuTab,
    accent: Color,
    newTabs: Set<MenuTab>,
    onSelect: (MenuTab) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            tabs.forEach { item ->
                val isSelected = item == selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(item) }
                        .testTag(MENU_TAB_TAG + item.id)
                ) {
                    Box(modifier = Modifier.height(36.dp).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
                        TabTitle(item.title(), item in newTabs, color = if (isSelected) accent else Color.Gray, fontSize = 13)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(2.dp)
                            .background(if (isSelected) accent else Color.Transparent, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
        HorizontalDivider(color = Color(0xFF2A2A2A))
    }
}

/** Título de pestaña o capa, con un punto dorado si tiene algo sin abrir. */
@Composable
private fun TabTitle(title: String, isNew: Boolean, color: Color = Color.Unspecified, fontSize: Int = 13) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        if (isNew) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(AncientGold, androidx.compose.foundation.shape.CircleShape)
                    .testTag(NEW_BADGE_TAG)
            )
        }
    }
}

/** Unidades compradas a partir de las cuales aparecen los multiplicadores (igual que su tutorial). */
const val BUY_MULTIPLIERS_AT = 5
const val MENU_TAB_TAG = "menu_tab_"
const val MENU_LAYER_TAG = "menu_layer_"
const val MENU_CONTENT_TAG = "menu_content_"
const val NEW_BADGE_TAG = "new_badge"
const val ASCEND_BUTTON_TAG = "ascend_button"

const val AUTO_BUILDINGS_TAG = "auto_buildings"
const val AUTOMATION_SETTINGS_TAG = "automation_settings"
const val ACTIVE_PACT_TAG = "active_pact"
const val ACTIVE_MUTATION_TAG = "active_mutation"
const val ATTENTION_BAR_TAG = "attention_bar"

/** Botón de multiplicador de compra: compacto y del mismo ancho que los demás. */
@Composable
private fun BuyMultiplierChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) ArcanePurple else Color(0xFF333333),
        modifier = modifier.height(28.dp)
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

/** Una pestaña cerrada por la Prueba en curso: un candado y la regla. */
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
        selectedTab = MenuTab.RITUALS,
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
        onOpenMadnessMap = {}
    )
}