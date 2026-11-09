package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.domain.model.EffectType
import com.EGomez.idleecosystem.domain.model.RandomEvent
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import kotlinx.coroutines.delay

@Composable
fun EventDialog(
    event: RandomEvent,
    resultText: String?,
    onOptionSelected: (Int) -> Unit
) {
    if (resultText != null) {
        // Estado 2: Resultado
        AlertDialog(
            onDismissRequest = {},
            title = { Text(event.title, color = ArcanePurple, fontWeight = FontWeight.Bold) },
            text = { Text(resultText, color = Color.White, fontStyle = FontStyle.Italic) },
            confirmButton = {},
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(BorderStroke(1.dp, ArcanePurple), RoundedCornerShape(16.dp))
        )
    } else {
        // Estado 1: Selección (con delay de seguridad)
        var areButtonsEnabled by remember { mutableStateOf(false) }
        LaunchedEffect(event.id) {
            areButtonsEnabled = false
            delay(1000)
            areButtonsEnabled = true
        }

        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    event.title,
                    color = AncientGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(event.description, color = Color.LightGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    event.options.forEachIndexed { index, option ->
                        Button(
                            onClick = { onOptionSelected(index) },
                            enabled = areButtonsEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (areButtonsEnabled) Color(0xFF2D2D2D) else Color(0xFF444444),
                                disabledContainerColor = Color(0xFF444444),
                                contentColor = Color.White,
                                disabledContentColor = Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            border = BorderStroke(
                                1.dp,
                                if (areButtonsEnabled) Color.Gray.copy(alpha = 0.5f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    option.text,
                                    color = if (areButtonsEnabled) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Renderizado de efectos previos (preview)
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (option.effects.all { it.type == EffectType.NOTHING }) {
                                        Text(stringResource(R.string.event_no_effect), color = Color.Gray, fontSize = 11.sp)
                                    } else {
                                        option.effects.forEachIndexed { i, effect ->
                                            if (i > 0) Spacer(modifier = Modifier.width(12.dp))
                                            val alpha = if (areButtonsEnabled) 1f else 0.5f
                                            when (effect.type) {
                                                EffectType.ADD_KNOWLEDGE_PERCENT -> {
                                                    val percent = (effect.value * 100).toInt()
                                                    val color = if (percent >= 0) Color(0xFF81C784) else Color(0xFFE57373)
                                                    Text(
                                                        stringResource(R.string.event_knowledge, "${if (percent >= 0) "+" else ""}$percent"),
                                                        color = color.copy(alpha = alpha),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                EffectType.ADD_SANITY_PERCENT -> {
                                                    val percent = (effect.value * 100).toInt()
                                                    val color = if (percent >= 0) Color(0xFF64B5F6) else Color(0xFFE57373)
                                                    Text(
                                                        stringResource(R.string.event_sanity, "${if (percent >= 0) "+" else ""}$percent"),
                                                        color = color.copy(alpha = alpha),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                EffectType.GLOBAL_BUFF -> {
                                                    Text(
                                                        stringResource(R.string.event_buff, effect.value.toString()),
                                                        color = AncientGold.copy(alpha = alpha),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                else -> {}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF121212),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(
                BorderStroke(2.dp, AncientGold.copy(alpha = 0.6f)),
                RoundedCornerShape(16.dp)
            )
        )
    }
}