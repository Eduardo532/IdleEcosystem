package com.EGomez.idleecosystem.ui.components

import com.EGomez.idleecosystem.ui.i18n.tr
import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple // Aseguramos el import del color morado

@Composable
fun AchievementItem(
    achievement: Achievement,
    isRevealed: Boolean
) {
    val isUnlocked = achievement.isUnlocked
    val showContent = isUnlocked || isRevealed

    // LÓGICA DE COLOR:
    // Si es secreto, usamos ArcanePurple. Si es normal, AncientGold.
    val accentColor = if (achievement.isSecret) ArcanePurple else AncientGold

    // Colores base
    val containerColor = if (isUnlocked) Color(0xFF2D2D2D) else Color(0xFF1E1E1E)
    // El borde ahora usa el accentColor si está desbloqueado
    val borderColor = if (isUnlocked) accentColor else Color.DarkGray
    // El título también se tiñe si está desbloqueado
    val titleColor = if (isUnlocked) accentColor else if (showContent) Color.LightGray else Color.DarkGray
    val alpha = if (isUnlocked) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor.copy(alpha = alpha)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ICONO
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isUnlocked) accentColor.copy(alpha = 0.2f) else Color.Black,
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.Star else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) accentColor else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // TEXTO
            Column(modifier = Modifier.weight(1f)) {
                // 1. TÍTULO
                Text(
                    text = if (showContent) tr(achievement.name) else "???",
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                if (showContent) {
                    // 2. REQUISITO
                    Text(
                        text = tr(achievement.requirement),
                        color = if (isUnlocked) Color(0xFF66BB6A) else Color(0xFFFFB74D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // 3. DESCRIPCIÓN
                    Text(
                        text = tr(achievement.description),
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 13.sp
                    )

                    // 4. EFECTO
                    if (isUnlocked) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.achievement_effect, achievement.bonusMultiplier.toString()),
                            color = accentColor, // También aplicamos el color al texto del efecto
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // TEXTO OCULTO
                    Text(
                        text = stringResource(R.string.achievement_hidden),
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}