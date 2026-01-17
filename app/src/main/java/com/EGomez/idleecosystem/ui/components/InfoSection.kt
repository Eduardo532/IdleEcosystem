package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import java.math.BigDecimal

@Composable
fun InfoSection(
    knowledgeFormatted: String,
    knowledgePerSecond: String,
    globalMultiplier: BigDecimal,
    activeBuffMultiplier: Double,
    buffTimeRemaining: Int,
    clickMultiplier: String,
    onManualClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Contadores Principales
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Conocimiento Arcano",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = knowledgeFormatted,
                color = ArcanePurple,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "+$knowledgePerSecond/seg",
                color = Color.Green,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Multiplicador Global
        if (globalMultiplier > BigDecimal.ONE) {
            Surface(
                color = AncientGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AncientGold),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Global: x${globalMultiplier.toBeautifulString()}",
                    color = AncientGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Buff Activo
        if (activeBuffMultiplier > 1.0) {
            Surface(
                color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BONUS ACTIVO: x$activeBuffMultiplier (${buffTimeRemaining}s)",
                        color = Color(0xFF81C784),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Espaciador para mantener consistencia visual si no hay buff pero tampoco global
            if (globalMultiplier <= BigDecimal.ONE) {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Botón "Investigar" (Clicker)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxHeight(0.85f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(ArcanePurple.copy(alpha = 0.1f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onManualClick
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.85f)
                        .clip(CircleShape)
                        .background(ArcanePurple.copy(alpha = 0.2f))
                )
                BoxWithConstraints(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val size = maxWidth
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "INVESTIGAR",
                            color = ArcanePurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = (size.value * 0.12f).sp
                        )
                        Text(
                            text = "+$clickMultiplier",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = (size.value * 0.10f).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}