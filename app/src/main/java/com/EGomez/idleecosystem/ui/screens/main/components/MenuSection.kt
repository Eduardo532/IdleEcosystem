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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import com.EGomez.idleecosystem.domain.logic.AscensionMilestone
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
    onAscendClicked: () -> Unit,
    onShowPrestigeShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Un sistema sin descubrir se ve como "???" hasta cumplir su requisito
    val tabs = listOf(
        stringResource(R.string.tab_research),
        if (revealed.grimoire) stringResource(R.string.tab_grimoire) else LOCKED_TITLE,
        if (revealed.rituals) stringResource(R.string.tab_rituals) else LOCKED_TITLE
    )
    val lockedTabText = when {
        selectedTabIndex == 1 && !revealed.grimoire ->
            stringResource(R.string.locked_grimoire, SystemReveal.GRIMOIRE_AT.toBeautifulString())
        selectedTabIndex == 2 && !revealed.rituals -> stringResource(R.string.locked_rituals)
        else -> null
    }

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
                // Seis opciones no caben en pantallas angostas: la fila se desplaza
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Autocomprador de edificios (hito de Ascensión)
                    if (AscensionMilestone.BUILDING_AUTOBUYER.isReached(ascensions)) {
                        AutoChip(
                            selected = automation.buildings,
                            onClick = onToggleAutoBuildings,
                            modifier = Modifier.testTag(AUTO_BUILDINGS_TAG)
                        )
                        // Panel de automatización: qué edificios, presupuesto, Ascensión automática
                        IconButton(
                            onClick = onOpenAutomation,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag(AUTOMATION_SETTINGS_TAG)
                        ) {
                            Icon(Icons.Default.Settings, stringResource(R.string.cd_automation_settings), tint = AncientGold)
                        }
                    }
                    listOf(1, 5, 10, 100, BuildingMilestones.BUY_TO_MILESTONE, -1).forEach { mult ->
                        val label = when (mult) {
                            -1 -> "MAX"
                            BuildingMilestones.BUY_TO_MILESTONE -> milestoneLabel
                            else -> "x$mult"
                        }
                        FilterChip(
                            selected = buyMultiplier == mult,
                            onClick = { onBuyMultiplierChanged(mult) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ArcanePurple,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF333333),
                                labelColor = Color.Gray
                            ),
                            border = null
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
                        text = { Text(title, fontWeight = FontWeight.Bold) },
                        selectedContentColor = ArcanePurple,
                        unselectedContentColor = Color.Gray
                    )
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

/** Un sistema sin descubrir: un candado y lo que falta para verlo. */
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