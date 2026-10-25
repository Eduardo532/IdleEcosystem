package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple

@Composable
fun HardResetDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
        title = { Text("¿ESTÁS SEGURO?", color = Color.Red, fontWeight = FontWeight.Black) },
        text = { Text("Esta acción borrará PERMANENTEMENTE todo tu progreso.\n\nNo se puede deshacer.", color = Color.White) },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.Red,
        textContentColor = Color.White,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("SÍ, BORRAR TODO", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}

/** Confirmación antes de ascender: antes un toque accidental reiniciaba la partida. */
@Composable
fun AscendDialog(sanityGainFormatted: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Ascender?", color = Color(0xFFCF6679), fontWeight = FontWeight.Black) },
        text = {
            Column {
                Text(
                    "Perderás tu Conocimiento, edificios y mejoras del Grimorio.",
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Conservas logros, Ecos y la Tienda de Cordura.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Ganarás +$sanityGainFormatted Cordura",
                    color = ArcanePurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        containerColor = Color(0xFF1E1E1E),
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679), contentColor = Color.Black)
            ) {
                Text("ASCENDER", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}

@Composable
fun OfflineGainsDialog(
    timeFormatted: String,
    gainsFormatted: String,
    echoesFormatted: String = "",
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mientras dormías...", color = ArcanePurple, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Tu locura creció en las sombras.", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Tiempo: $timeFormatted", color = Color.White)
                Text(text = "Ganancia: +$gainsFormatted", color = Color(0xFF4CAF50), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                if (echoesFormatted.isNotEmpty()) {
                    Text(text = "Ecos: +$echoesFormatted", color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple)) {
                Text("Aceptar")
            }
        },
        containerColor = Color(0xFF1E1E1E),
        textContentColor = Color.White
    )
}

@Composable
fun RefundDialog(refundAmount: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Actualización del Cosmos", color = AncientGold, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("El sistema de Prestigio ha evolucionado a un Mapa Estelar.", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tus mejoras anteriores han sido reiniciadas y tu inversión ha sido devuelta íntegramente.", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cordura Recuperada: +$refundAmount", color = ArcanePurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black)) {
                Text("EXPLORAR")
            }
        },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(BorderStroke(1.dp, AncientGold), RoundedCornerShape(16.dp))
    )
}