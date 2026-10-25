package com.EGomez.idleecosystem.ui.screens.main.dialogs

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
import java.math.BigDecimal

@Composable
fun PrestigeShopDialog(
    upgrades: List<PrestigeUpgrade>,
    currencyFormatted: String,
    currency: BigDecimal,
    onBuy: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedNode by remember { mutableStateOf<PrestigeUpgrade?>(null) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
            Box(modifier = Modifier.fillMaxSize().padding(top = 60.dp, bottom = 150.dp, start = 20.dp, end = 20.dp)) {
                ConstellationMap(upgrades = upgrades, onNodeClick = { node -> selectedNode = node })
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
                        Text("MAPA DE LOCURA", color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Cordura: $currencyFormatted", color = ArcanePurple, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Cerrar", tint = Color.White) }
                }
            }
            if (selectedNode != null) {
                val node = selectedNode!!
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
                            if (node.level > 0) Text("Nivel ${node.level}", color = AncientGold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(node.description, color = Color.LightGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Efecto Actual: ", color = Color.Gray, fontSize = 12.sp)
                            Text(node.currentEffectDisplay, color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (parentUnlocked) {
                            val canAfford = currency >= node.currentCost
                            Button(
                                onClick = { onBuy(node.id) },
                                enabled = canAfford,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ArcanePurple,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF333333),
                                    disabledContentColor = Color.Gray
                                )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (canAfford) "MEJORAR" else "CORDURA INSUFICIENTE", fontWeight = FontWeight.Bold)
                                    Text("Coste: ${node.currentCost.toBeautifulString()} Cordura", fontSize = 10.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = {}, enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.DarkGray)
                            ) { Text("BLOQUEADO (Requiere conexión previa)", color = Color.Gray) }
                        }
                    }
                }
            } else {
                Text(
                    "Toca una estrella para ver detalles",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                )
            }
        }
    }
}