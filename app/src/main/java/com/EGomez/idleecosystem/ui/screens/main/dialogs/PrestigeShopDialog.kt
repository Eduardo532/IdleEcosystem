package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import java.math.BigDecimal
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.ui.components.ConstellationMap
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple

@Composable
fun PrestigeShopDialog(
    upgrades: List<PrestigeUpgrade>,
    currencyFormatted: String,
    currency: BigDecimal,
    onBuy: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Se guarda el ID, no el objeto: el nodo se busca en la lista ACTUAL. Antes se
    // guardaba la copia del momento del toque, y tras comprar el panel seguía
    // mostrando el nivel y el costo viejos hasta cerrar y abrir la tienda.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedNode = upgrades.find { it.id == selectedId }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
            Box(modifier = Modifier.fillMaxSize().padding(top = 60.dp, bottom = 150.dp, start = 20.dp, end = 20.dp)) {
                Box(modifier = Modifier.fillMaxSize().testTag(CONSTELLATION_MAP_TAG)) {
                    ConstellationMap(upgrades = upgrades, onNodeClick = { node -> selectedId = node.id })
                }
            }
            Column(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f)).padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.madness_map), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(stringResource(R.string.sanity_amount, currencyFormatted), color = ArcanePurple, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, stringResource(R.string.close), tint = Color.White) }
                }
            }
            if (selectedNode != null) {
                val node = selectedNode
                val parentUnlocked = if (node.parentId == null) true else {
                    val parent = upgrades.find { it.id == node.parentId }
                    parent != null && parent.level > 0
                }
                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    border = BorderStroke(1.dp, if (parentUnlocked) ArcanePurple else Color.Gray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(node.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (node.level > 0) Text(stringResource(R.string.level, node.level), color = AncientGold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(node.description, color = Color.LightGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.current_effect), color = Color.Gray, fontSize = 12.sp)
                            Text(node.currentEffectDisplay, color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (parentUnlocked) {
                            // Antes el botón estaba siempre activo: sin Cordura suficiente
                            // el toque no hacía nada y no se veía por qué
                            val cost = node.currentCost
                            val canAfford = currency >= cost
                            Button(
                                onClick = { onBuy(node.id) },
                                enabled = canAfford,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ArcanePurple,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF333333),
                                    disabledContentColor = Color.LightGray
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.upgrade_button), fontWeight = FontWeight.Bold)
                                    Text(stringResource(R.string.cost_sanity, cost.toBeautifulString()), fontSize = 10.sp)
                                    if (!canAfford) {
                                        Text(
                                            stringResource(R.string.missing_sanity, cost.subtract(currency).toBeautifulString()),
                                            fontSize = 10.sp,
                                            color = Color(0xFFE57373)
                                        )
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = {}, enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.DarkGray)
                            ) { Text(stringResource(R.string.locked_node), color = Color.Gray) }
                        }
                    }
                }
            } else {
                Text(
                    stringResource(R.string.tap_star),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                )
            }
        }
    }
}

const val CONSTELLATION_MAP_TAG = "constellation_map"
