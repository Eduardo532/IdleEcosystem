package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.EGomez.idleecosystem.ui.GameUiState
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    uiState: GameUiState,
    onDismiss: () -> Unit,
    onStatsClick: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleSfx: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onCancelImport: () -> Unit,
    onPasteClick: (String) -> Unit,
    onHardResetClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Configuración", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStatsClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AncientGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VER ESTADÍSTICAS", color = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Música", color = Color.Gray, fontSize = 12.sp)
                        Switch(
                            checked = uiState.isMusicEnabled,
                            onCheckedChange = { onToggleMusic() },
                            colors = SwitchDefaults.colors(checkedThumbColor = ArcanePurple, checkedTrackColor = ArcanePurple.copy(alpha = 0.5f))
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Efectos", color = Color.Gray, fontSize = 12.sp)
                        Switch(
                            checked = uiState.isSfxEnabled,
                            onCheckedChange = { onToggleSfx() },
                            colors = SwitchDefaults.colors(checkedThumbColor = AncientGold, checkedTrackColor = AncientGold.copy(alpha = 0.5f))
                        )
                    }
                }
                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                Text("Exportar Partida", color = AncientGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.saveStringExport.isNotEmpty() && uiState.exportPreview != null) {
                    val preview = uiState.exportPreview!!
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, AncientGold)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("TU ARCHIVO ACTUAL", color = AncientGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Fecha: ${preview.date}", color = Color.Gray, fontSize = 12.sp)
                            Text("Conocimiento: ${preview.money}", color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Logros: ${preview.achievements}", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(uiState.saveStringExport))
                            isCopied = true
                            scope.launch { delay(2000); isCopied = false }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isCopied) Color(0xFF4CAF50) else AncientGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy, null, tint = if (isCopied) Color.White else Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isCopied) "¡COPIADO!" else "COPIAR AL PORTAPAPELES", color = if (isCopied) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onExportClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("GENERAR CÓDIGO") }
                }

                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                Text("Importar Partida", color = AncientGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.importPreview != null) {
                    val preview = uiState.importPreview!!
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, ArcanePurple)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ARCHIVO ENCONTRADO", color = ArcanePurple, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Fecha: ${preview.date}", color = Color.Gray, fontSize = 12.sp)
                            Text("Conocimiento: ${preview.money}", color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Logros: ${preview.achievements}", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onCancelImport, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White), modifier = Modifier.weight(1f)) { Text("CANCELAR", fontSize = 10.sp) }
                        Button(onClick = onImportClick, colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple, contentColor = Color.White), modifier = Modifier.weight(1f)) { Text("CARGAR DATOS", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    }
                } else {
                    Button(
                        onClick = {
                            val text = clipboardManager.getText()?.text
                            if (!text.isNullOrEmpty()) onPasteClick(text)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentPaste, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PEGAR CÓDIGO")
                    }
                    if (uiState.importError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.importError!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                Button(onClick = onHardResetClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679)), modifier = Modifier.fillMaxWidth()) { Text("REINICIO TOTAL", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
        }
    }
}