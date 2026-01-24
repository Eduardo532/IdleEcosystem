package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import com.EGomez.idleecosystem.ui.theme.CrimsonRed
import java.math.BigDecimal

@Composable
fun InfoSection(
    knowledgeFormatted: String,
    knowledgePerSecond: String,
    globalMultiplier: BigDecimal,
    activeBuffMultiplier: Double,
    buffTimeRemaining: Int,
    clickMultiplier: String,
    onManualClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    // Variable para rastrear el centro del botón
    var buttonCenter by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- DISPLAY DE CONOCIMIENTO ---
        Text(
            text = "Conocimiento Arcano",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = knowledgeFormatted,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "$knowledgePerSecond / seg",
            style = MaterialTheme.typography.titleMedium,
            color = AncientGold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- BOTÓN PRINCIPAL DE INVESTIGACIÓN ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(ArcanePurple.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    val size = coordinates.size
                    buttonCenter = Offset(
                        x = position.x + size.width / 2f,
                        y = position.y + size.height / 2f
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onManualClick(buttonCenter)
                }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "INVESTIGAR",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Click: x$clickMultiplier",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ESTADÍSTICAS ACTIVAS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MultiplierBadge("Global", "x${globalMultiplier.toDouble()}")

            if (activeBuffMultiplier > 1.0) {
                MultiplierBadge(
                    "Buff (${buffTimeRemaining}s)",
                    "x$activeBuffMultiplier",
                    color = CrimsonRed
                )
            }
        }
    }
}

@Composable
private fun MultiplierBadge(label: String, value: String, color: Color = AncientGold) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
    }
}