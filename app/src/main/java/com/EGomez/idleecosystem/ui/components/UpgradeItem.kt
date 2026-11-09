package com.EGomez.idleecosystem.ui.components

import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.model.Upgrade
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import java.math.BigDecimal

@Composable
fun UpgradeItem(
    upgrade: Upgrade,
    targetInfo: String,
    currentFunds: BigDecimal,
    onBuy: () -> Unit
) {
    val canAfford = currentFunds >= upgrade.cost
    val isBought = upgrade.isBought

    // Definición de colores
    val infoGreen = Color(0xFF81C784)

    // Lógica de Borde: Verde si está comprado, Morado sutil si no
    val borderColor = if (isBought) infoGreen.copy(alpha = 0.5f) else ArcanePurple.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), // Fondo Oscuro
        border = BorderStroke(1.dp, borderColor), // Borde Dinámico
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // COLUMNA IZQUIERDA (Info)
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                if (isBought){
                    Text(
                        text = upgrade.name,
                        color = infoGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                else{
                    Text(
                        text = upgrade.name,
                        color = ArcanePurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))


                Text(
                    text = upgrade.description,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                val multString = if (upgrade.multiplier % 1.0 == 0.0) {
                    upgrade.multiplier.toInt().toString()
                } else {
                    upgrade.multiplier.toString()
                }

                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (targetInfo.isNotEmpty()) {
                        Text(
                            text = "$targetInfo",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                Row (verticalAlignment = Alignment.CenterVertically){
                    Text(
                        text = stringResource(R.string.upgrade_effect, multString),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isBought) {
                Text(
                    text = stringResource(R.string.bought_label),
                    color = infoGreen, // CAMBIO: Texto Gris solicitado
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                // BOTÓN: Morado para comprar
                Button(
                    onClick = onBuy,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) ArcanePurple else Color(0xFF333333),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF222222),
                        disabledContentColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = upgrade.cost.toBeautifulString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}