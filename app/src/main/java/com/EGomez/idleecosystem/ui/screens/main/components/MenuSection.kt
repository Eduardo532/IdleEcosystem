package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.Ritual
import com.EGomez.idleecosystem.domain.model.Upgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import com.EGomez.idleecosystem.ui.components.BuildingItem
import com.EGomez.idleecosystem.ui.components.UpgradeItem
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
    rituals: List<Ritual>, // NUEVO PARÁMETRO
    currentKnowledge: BigDecimal,
    globalMultiplier: BigDecimal,
    buildingCalculator: BuildingCalculator,
    onBuyBuilding: (String) -> Unit,
    onBuyUpgrade: (String) -> Unit,
    onActivateRitual: (String) -> Unit, // NUEVO PARÁMETRO
    prestigeCurrencyFormatted: String,
    canPrestige: Boolean,
    potentialPrestigeFormatted: String,
    onAscendClicked: () -> Unit,
    onShowPrestigeShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    // AÑADIMOS LA TERCERA PESTAÑA
    val tabs = listOf("Investigación", "Grimorio", "Rituales")

    Column(
        modifier = modifier
            .background(Color(0xFF161616), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        // Toggle Handle
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
            // Multiplier Chips (Solo visible en la pestaña de Edificios - Index 0)
            if (selectedTabIndex == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(1, 5, 10, 100, -1).forEach { mult ->
                        val label = if (mult == -1) "MAX" else "x$mult"
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

            // Tab Row
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

            // List Content
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> { // Buildings
                        items(buildings) { building ->
                            BuildingItem(
                                building = building,
                                currentFunds = currentKnowledge,
                                globalMultiplier = globalMultiplier,
                                calculator = buildingCalculator,
                                buyMultiplier = buyMultiplier,
                                onBuyClick = { onBuyBuilding(building.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    1 -> { // Upgrades
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    "Ver Comprados",
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
                                    Text("No hay secretos...", color = Color.Gray)
                                }
                            }
                        } else {
                            items(upgrades) { upgrade ->
                                val targetName = if (upgrade.type == UpgradeType.CLICK_MULTIPLIER) "Tu Mente"
                                else if (upgrade.type == UpgradeType.GLOBAL_MULTIPLIER) "Todo"
                                else buildings.find { it.id == upgrade.targetBuildingId }?.name ?: "?"

                                UpgradeItem(
                                    upgrade = upgrade,
                                    targetInfo = "Afecta: $targetName",
                                    currentFunds = currentKnowledge,
                                    onBuy = { onBuyUpgrade(upgrade.id) }
                                )
                            }
                        }
                    }
                    2 -> { // RITUALES (NUEVA PESTAÑA)
                        items(rituals) { ritual ->
                            RitualItem(
                                ritual = ritual,
                                currentFunds = currentKnowledge,
                                onActivate = { onActivateRitual(ritual.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            // Prestige / Bottom Bar
            if (isMenuExpanded) {
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
                            "CORDURA RESIDUAL",
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
                            Text("TIENDA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                Text("ASCENDER", fontWeight = FontWeight.Black)
                                if (canPrestige) {
                                    Text(
                                        "+$potentialPrestigeFormatted",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text("Req: 1.00e+06", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                "Desliza para ver contenido",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
        }
    }
}