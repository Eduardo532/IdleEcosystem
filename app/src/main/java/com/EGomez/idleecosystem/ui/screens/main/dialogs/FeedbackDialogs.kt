package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
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
        title = { Text(stringResource(R.string.reset_confirm_title), color = Color.Red, fontWeight = FontWeight.Black) },
        text = { Text(stringResource(R.string.reset_confirm_text), color = Color.White) },
        containerColor = Color(0xFF1E1E1E),
        titleContentColor = Color.Red,
        textContentColor = Color.White,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(R.string.reset_confirm_button), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Color.Gray) }
        }
    )
}

/**
 * Confirmación antes de ascender. Antes el botón ascendía al instante: un toque
 * accidental reiniciaba la partida, y el tutorial "El Ciclo" aparecía después.
 */
@Composable
fun AscendDialog(sanityGainFormatted: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ascend_question), color = AncientGold, fontWeight = FontWeight.Black) },
        text = {
            Column {
                Text(stringResource(R.string.ascend_lose), color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.ascend_keep), color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.ascend_gain, sanityGainFormatted),
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
                colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black)
            ) {
                Text(stringResource(R.string.ascend), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Color.Gray) }
        }
    )
}

@Composable
fun OfflineGainsDialog(timeFormatted: String, gainsFormatted: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.offline_title), color = ArcanePurple, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.offline_text), color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.offline_time, timeFormatted), color = Color.White)
                Text(text = stringResource(R.string.offline_gain, gainsFormatted), color = Color(0xFF4CAF50), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple)) {
                Text(stringResource(R.string.accept))
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
        title = { Text(stringResource(R.string.refund_title), color = AncientGold, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.refund_text1), color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.refund_text2), color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.refund_amount, refundAmount), color = ArcanePurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black)) {
                Text(stringResource(R.string.explore))
            }
        },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(BorderStroke(1.dp, AncientGold), RoundedCornerShape(16.dp))
    )
}