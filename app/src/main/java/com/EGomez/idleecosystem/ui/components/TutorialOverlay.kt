package com.EGomez.idleecosystem.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.EGomez.idleecosystem.domain.model.TutorialStep
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import kotlinx.coroutines.delay

@Composable
fun TutorialOverlay(
    currentStep: TutorialStep?,
    onDismiss: () -> Unit
) {
    // Estado para controlar el bloqueo temporal del botón
    var isButtonEnabled by remember { mutableStateOf(false) }

    // Cada vez que cambia el paso del tutorial, reiniciamos el bloqueo
    LaunchedEffect(currentStep) {
        if (currentStep != null) {
            isButtonEnabled = false
            delay(1500) // Bloqueo de 1.5 segundos para evitar clicks accidentales
            isButtonEnabled = true
        }
    }

    if (currentStep != null) {
        Dialog(
            onDismissRequest = {}, // Bloqueamos cierre con botón atrás
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            // Fondo semitransparente oscuro
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)), // Un poco más oscuro para resaltar
                contentAlignment = Alignment.Center
            ) {
                // Tarjeta del Tutorial (Animada)
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        ),
                        border = BorderStroke(2.dp, AncientGold),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF2C2C2C), Color(0xFF121212))
                                    )
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentStep.title,
                                color = AncientGold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 1.dp,
                                color = AncientGold.copy(alpha = 0.5f)
                            )

                            Text(
                                text = currentStep.message,
                                color = Color.White,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // BOTÓN CON COOLDOWN
                            Button(
                                onClick = onDismiss,
                                enabled = isButtonEnabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ArcanePurple,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                                    disabledContentColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isButtonEnabled) "ENTENDIDO" else "LEYENDO...",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}