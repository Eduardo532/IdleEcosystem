package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.BuildingType
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.SurfaceGrey
import java.math.BigDecimal

@Composable
fun BuildingItem(
    building: Building,
    currentFunds: BigDecimal,
    globalMultiplier: BigDecimal,
    calculator: BuildingCalculator,
    buyMultiplier: Int,
    onBuyClick: () -> Unit
) {
    val displayCost: BigDecimal
    val amountToBuyLabel: String

    if (buyMultiplier == -1) {
        val maxAffordable = calculator.calculateMaxAffordable(building, currentFunds)
        if (maxAffordable > 0) {
            displayCost = calculator.calculateCostForAmount(building, maxAffordable)
            amountToBuyLabel = "+$maxAffordable"
        } else {
            displayCost = calculator.calculateNextCost(building)
            amountToBuyLabel = "+1"
        }
    } else {
        displayCost = calculator.calculateCostForAmount(building, buyMultiplier)
        amountToBuyLabel = "+$buyMultiplier"
    }

    val canAfford = currentFunds >= displayCost

    val specificMult = if (globalMultiplier.compareTo(BigDecimal.ZERO) != 0) {
        building.currentMultiplier.divide(globalMultiplier, java.math.MathContext.DECIMAL32)
    } else BigDecimal.ONE
    val hasSpecificMultiplier = specificMult > BigDecimal("1.01")

    val prodLabel = if (building.type == BuildingType.CLICK) "Clic" else "Prod"
    val prodSuffix = if (building.type == BuildingType.CLICK) "" else "/s"

    // --- LÓGICA DE COLOR POR GRUPO (TIER) ---
    // 1-6: Básico (Azul) - Mente y Culto temprano
    // 7-12: Sangre (Rojo) - Sacrificios y Horrores físicos
    // 13-16: Cósmico (Morado) - Espacio, Sueños y Vacío
    val tierColor = when (building.tier) {
        in 1..6 -> Color(0xFF1565C0) // Azul
        in 7..12 -> Color(0xFFC62828) // Rojo Sangre
        else -> Color(0xFF9C27B0)     // Morado Arcano
    }

    // Aplicamos el borde con el color correspondiente
    val borderStroke = BorderStroke(1.dp, tierColor.copy(alpha = 0.6f))

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
        shape = RoundedCornerShape(8.dp),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = building.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (hasSpecificMultiplier) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = Color(0xFF2E7D32), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "x${specificMult.toBeautifulString()}",
                                color = Color(0xFFA5D6A7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                val productionToShow = if(building.amountOwned > 0) building.totalProduction else building.baseProduction.multiply(building.currentMultiplier)

                Row {

                    Text(
                        text = "Posees: ${building.amountOwned} | $prodLabel: ${productionToShow.toBeautifulString()}$prodSuffix",
                        color = if (hasSpecificMultiplier) Color(0xFF81C784) else Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = building.description,
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuyClick,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black, disabledContainerColor = Color(0xFF333333), disabledContentColor = Color.White.copy(alpha = 0.6f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Comprar $amountToBuyLabel", maxLines = 1)
                    Text(text = displayCost.toBeautifulString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}