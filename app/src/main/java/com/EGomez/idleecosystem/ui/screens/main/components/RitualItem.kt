package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.model.Ritual
import com.EGomez.idleecosystem.domain.model.RitualState
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import java.math.BigDecimal

@Composable
fun RitualItem(
    ritual: Ritual,
    currentFunds: BigDecimal,
    onActivate: () -> Unit
) {
    val canAfford = currentFunds >= ritual.baseCost
    val isReady = ritual.state == RitualState.READY

    // Configuración de colores según estado
    val borderColor = when (ritual.state) {
        RitualState.ACTIVE -> Color(0xFF4CAF50) // Verde brillante
        RitualState.COOLDOWN -> Color.Gray
        RitualState.READY -> if (canAfford) ArcanePurple else Color(0xFF553333)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Cabecera: Nombre y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ritual.name,
                    color = if (ritual.state == RitualState.ACTIVE) AncientGold else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (ritual.state == RitualState.ACTIVE) {
                    Text("ACTIVO", color = Color(0xFF4CAF50), fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ritual.description,
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Zona de Acción o Progreso
            when (ritual.state) {
                RitualState.READY -> {
                    Button(
                        onClick = onActivate,
                        enabled = canAfford,
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArcanePurple,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF333333),
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (canAfford) {
                            Text("INVOCAR (${ritual.baseCost.toBeautifulString()})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Text("FALTA CONOCIMIENTO (${ritual.baseCost.toBeautifulString()})", fontSize = 11.sp)
                        }
                    }
                }

                RitualState.ACTIVE, RitualState.COOLDOWN -> {
                    val progress = ritual.getProgress()
                    val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")
                    val barColor = if (ritual.state == RitualState.ACTIVE) Color(0xFF4CAF50) else Color.Gray
                    val label = if (ritual.state == RitualState.ACTIVE)
                        "Tiempo restante: ${ritual.timeRemaining}s"
                    else
                        "Enfriamiento: ${ritual.timeRemaining}s"

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, color = barColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = barColor,
                            trackColor = Color(0xFF333333),
                        )
                    }
                }
            }
        }
    }
}