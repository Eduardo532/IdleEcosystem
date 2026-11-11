package com.EGomez.idleecosystem.ui.components

import com.EGomez.idleecosystem.ui.i18n.tr
import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.clickable
import com.EGomez.idleecosystem.domain.logic.Consecration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.BuildingType
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import com.EGomez.idleecosystem.domain.logic.BuildingMilestones
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.SurfaceGrey
import java.math.BigDecimal
import java.math.MathContext

@Composable
fun BuildingItem(
    building: Building,
    currentFunds: BigDecimal,
    globalMultiplier: BigDecimal,
    calculator: BuildingCalculator,
    buyMultiplier: Int,
    costMultiplier: Double = 1.0, // NUEVO PARÁMETRO
    milestones: List<Int> = emptyList(),
    /** Puntos de Atención puestos en este edificio. */
    attentionPoints: Int = 0,
    /** Consagración (v0.18.1): nivel actual, si ya se ve el sistema y el Éter disponible. */
    consecration: Int = 0,
    consecrationRevealed: Boolean = false,
    ether: Int = 0,
    onConsecrate: () -> Unit = {},
    onBuyClick: () -> Unit
) {
    // 1. Calcular Costo Base (Sin descuento)
    // Se memoriza: el conocimiento cambia 10 veces por segundo, pero el costo de x1/x10/x100
    // solo depende de cuántos edificios tienes. Solo MAX depende de los fondos actuales.
    val owned = building.amountOwned
    val fundsKey: BigDecimal? = if (buyMultiplier == -1) currentFunds else null
    val (rawCost, amountToBuyLabel) = remember(building.id, owned, buyMultiplier, costMultiplier, fundsKey) {
        if (buyMultiplier == -1) {
            // Para el cálculo de MAX, ajustamos los fondos efectivos según el descuento
            val effectiveFunds = if (costMultiplier < 1.0)
                currentFunds.divide(BigDecimal.valueOf(costMultiplier), MathContext.DECIMAL64)
            else currentFunds

            val maxAffordable = calculator.calculateMaxAffordable(building, effectiveFunds)

            if (maxAffordable > 0) {
                calculator.calculateCostForAmount(building, maxAffordable) to "+$maxAffordable"
            } else {
                calculator.calculateNextCost(building) to "+1"
            }
        } else if (buyMultiplier == BuildingMilestones.BUY_TO_MILESTONE) {
            // Justo lo que falta para el siguiente hito
            val amount = BuildingMilestones.amountToNext(milestones, owned)
            calculator.calculateCostForAmount(building, amount) to "+$amount"
        } else {
            calculator.calculateCostForAmount(building, buyMultiplier) to "+$buyMultiplier"
        }
    }
    val nextMilestone = remember(owned, milestones) { BuildingMilestones.next(milestones, owned) }
    val milestoneProgress = remember(owned, milestones) {
        val start = BuildingMilestones.previous(milestones, owned)
        ((owned - start).toFloat() / (nextMilestone - start)).coerceIn(0f, 1f)
    }

    // 2. Aplicar Descuento
    val finalCost = rawCost.multiply(BigDecimal.valueOf(costMultiplier))
    val hasDiscount = costMultiplier < 1.0

    val canAfford = currentFunds >= finalCost

    val specificMult = if (globalMultiplier.compareTo(BigDecimal.ZERO) != 0) {
        building.currentMultiplier.divide(globalMultiplier, MathContext.DECIMAL32)
    } else BigDecimal.ONE
    val hasSpecificMultiplier = specificMult > BigDecimal("1.01")

    val prodLabel = if (building.type == BuildingType.CLICK) stringResource(R.string.prod_click) else stringResource(R.string.prod_production)
    val prodSuffix = if (building.type == BuildingType.CLICK) "" else "/s"

    val tierColor = when (building.tier) {
        in 1..6 -> Color(0xFF1565C0)
        in 7..12 -> Color(0xFFC62828)
        else -> Color(0xFF9C27B0)
    }

    val borderStroke = BorderStroke(1.dp, tierColor.copy(alpha = 0.6f))

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
        shape = RoundedCornerShape(8.dp),
        border = borderStroke
    ) {
        // Tarjeta compacta (v0.18.2): la descripción se despliega al tocar el nombre
        var showDescription by rememberSaveable(building.id) { mutableStateOf(false) }
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showDescription = !showDescription }
                        .testTag(BUILDING_NAME_TAG + building.id)
                ) {
                    Text(
                        text = tr(building.name),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
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
                    // Consagración
                    if (consecration > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = Color(0xFF12202A), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "✦ ${Consecration.roman(consecration)}",
                                color = Color(0xFFB3E5FC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp).testTag(CONSECRATION_BADGE_TAG + building.id)
                            )
                        }
                    }
                    // La Atención puesta en este edificio
                    if (attentionPoints > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = Color(0xFF2A1A3A), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "◎ $attentionPoints",
                                color = Color(0xFFD4AF37),
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
                        text = stringResource(R.string.owned_line, building.amountOwned, prodLabel, productionToShow.toBeautifulString(), prodSuffix),
                        color = if (hasSpecificMultiplier) Color(0xFF81C784) else Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (showDescription) {
                    Text(
                        text = tr(building.description),
                        color = Color.Gray,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        modifier = Modifier.testTag(BUILDING_DESCRIPTION_TAG + building.id)
                    )
                }

                // Consagración: el próximo nivel, cuántas unidades pide y su costo en Éter
                if (consecrationRevealed && consecration < Consecration.MAX_LEVEL) {
                    val needed = Consecration.requiredOwned(consecration)
                    val cost = Consecration.etherCost(consecration)
                    val ready = Consecration.canConsecrate(consecration, owned, ether)
                    Text(
                        text = if (owned >= needed) stringResource(R.string.consecrate_ready, Consecration.roman(consecration + 1), cost)
                        else stringResource(R.string.consecrate_hint, Consecration.roman(consecration + 1), needed, cost),
                        color = if (ready) Color(0xFFB3E5FC) else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = if (ready) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable(enabled = ready, onClick = onConsecrate)
                            .testTag(CONSECRATE_TAG + building.id)
                    )
                }

                // Cuánto falta para el siguiente hito (escalón del Grimorio o logro)
                if (milestones.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { milestoneProgress },
                            color = tierColor,
                            trackColor = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.milestone_progress, nextMilestone, nextMilestone - owned),
                            color = Color.Gray,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuyClick,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black, disabledContainerColor = Color(0xFF333333), disabledContentColor = Color.White.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.heightIn(min = 40.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.buy_amount, amountToBuyLabel), maxLines = 1, fontSize = 13.sp)

                    if (hasDiscount) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rawCost.toBeautifulString(),
                                fontSize = 9.sp,
                                color = Color.Black.copy(alpha = 0.6f),
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = finalCost.toBeautifulString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD50000)
                            )
                        }
                    } else {
                        Text(text = finalCost.toBeautifulString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}

const val CONSECRATE_TAG = "consecrate_"
const val CONSECRATION_BADGE_TAG = "consecration_badge_"
const val BUILDING_NAME_TAG = "building_name_"
const val BUILDING_DESCRIPTION_TAG = "building_description_"
