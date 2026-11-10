package com.EGomez.idleecosystem.ui.screens.main.components

import com.EGomez.idleecosystem.ui.i18n.tr
import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.EGomez.idleecosystem.ui.components.AutoChip
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
    /** Si el ritual se activa solo: null mientras el hito no está alcanzado. */
    autoSelected: Boolean? = null,
    onToggleAuto: () -> Unit = {},
    onActivate: () -> Unit
) {
    val canAfford = currentFunds >= ritual.cost
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
                    text = tr(ritual.name),
                    color = if (ritual.state == RitualState.ACTIVE) AncientGold else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ritual.state == RitualState.ACTIVE) {
                        Text(stringResource(R.string.active_label), color = Color(0xFF4CAF50), fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }
                    if (autoSelected != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AutoChip(selected = autoSelected, onClick = onToggleAuto)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tr(ritual.description),
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
                            Text(stringResource(R.string.ritual_invoke, ritual.cost.toBeautifulString()), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Text(stringResource(R.string.ritual_missing_echoes, ritual.cost.toBeautifulString()), fontSize = 11.sp)
                        }
                    }
                }

                RitualState.ACTIVE, RitualState.COOLDOWN -> {
                    val progress = ritual.getProgress()
                    val animatedProgress by animateFloatAsState(targetValue = progress, label = "Progress")
                    val barColor = if (ritual.state == RitualState.ACTIVE) Color(0xFF4CAF50) else Color.Gray
                    val label = if (ritual.state == RitualState.ACTIVE)
                        stringResource(R.string.ritual_time_left, ritual.timeRemaining)
                    else
                        stringResource(R.string.ritual_cooldown, ritual.timeRemaining)

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