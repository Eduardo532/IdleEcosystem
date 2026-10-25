package com.EGomez.idleecosystem.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.ui.theme.AncientGold

/**
 * Presagio: aviso de que hay un evento disponible. Reemplaza al diálogo que antes
 * aparecía de golpe e interrumpía la partida; el jugador lo abre cuando quiere.
 */
@Composable
fun OmenButton(
    secondsLeft: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "omenPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 700), repeatMode = RepeatMode.Reverse),
        label = "omenScale"
    )

    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier
            .scale(pulse)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = Color(0xFF2A1F05),
        border = BorderStroke(1.dp, AncientGold)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✦ PRESAGIO", color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Toca para ver (${secondsLeft}s)", color = Color.LightGray, fontSize = 10.sp)
        }
    }
}
