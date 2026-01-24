package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.EGomez.idleecosystem.ui.GameUiState
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.CrimsonRed
import com.EGomez.idleecosystem.ui.theme.SurfaceGrey
import com.EGomez.idleecosystem.ui.theme.VoidBlack

@Composable
fun SettingsDialog(
    uiState: GameUiState,
    onDismiss: () -> Unit,
    onStatsClick: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleSfx: () -> Unit,
    // NUEVOS CALLBACKS
    onToggleParticles: () -> Unit,
    onToggleFloatingText: () -> Unit,
    // ----------------
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onCancelImport: () -> Unit,
    onPasteClick: (String) -> Unit,
    onHardResetClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
            border = androidx.compose.foundation.BorderStroke(2.dp, AncientGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 700.dp) // Limitar altura en pantallas grandes
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONFIGURACIÓN",
                        color = AncientGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Divider(color = AncientGold.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // SECCIÓN: ESTADÍSTICAS
                    Button(
                        onClick = onStatsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AncientGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver Estadísticas Completas", color = VoidBlack, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECCIÓN: AUDIO
                    SettingsSectionTitle("Audio")
                    SettingsSwitchRow(
                        label = "Música",
                        checked = uiState.isMusicEnabled,
                        onCheckedChange = { onToggleMusic() }
                    )
                    SettingsSwitchRow(
                        label = "Efectos de Sonido",
                        checked = uiState.isSfxEnabled,
                        onCheckedChange = { onToggleSfx() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECCIÓN: VISUAL (NUEVA)
                    SettingsSectionTitle("Gráficos")
                    SettingsSwitchRow(
                        label = "Partículas",
                        checked = uiState.areParticlesEnabled,
                        onCheckedChange = { onToggleParticles() }
                    )
                    SettingsSwitchRow(
                        label = "Texto Flotante",
                        checked = uiState.isFloatingTextEnabled,
                        onCheckedChange = { onToggleFloatingText() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECCIÓN: GESTIÓN DE DATOS
                    SettingsSectionTitle("Gestión de Datos")

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onExportClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AncientGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AncientGold)
                        ) {
                            Text("Exportar")
                        }
                    }

                    if (uiState.saveStringExport.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Código de Guardado (Copia esto):",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VoidBlack)
                                .border(1.dp, Color.Gray)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = uiState.saveStringExport,
                                color = AncientGold,
                                fontSize = 10.sp,
                                lineHeight = 12.sp
                            )
                        }

                        uiState.exportPreview?.let { preview ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Fecha: ${preview.date}", color = Color.Gray, fontSize = 10.sp)
                            Text("Conocimiento: ${preview.money}", color = Color.Gray, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Importar Partida:",
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    var tempImportText by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = tempImportText,
                        onValueChange = { tempImportText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 10.sp),
                        placeholder = { Text("Pega aquí tu código...", color = Color.Gray, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AncientGold,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Button(
                        onClick = {
                            onPasteClick(tempImportText)
                            tempImportText = ""
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        enabled = tempImportText.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("Verificar Código", color = Color.White)
                    }

                    // Previsualización de Importación
                    uiState.importError?.let {
                        Text(it, color = CrimsonRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }

                    uiState.importPreview?.let { preview ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("PREVISUALIZACIÓN:", color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Fecha: ${preview.date}", color = Color.White, fontSize = 12.sp)
                                Text("Conocimiento: ${preview.money}", color = Color.White, fontSize = 12.sp)
                                Text("Logros: ${preview.achievements}", color = Color.White, fontSize = 12.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onCancelImport,
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancelar")
                                    }
                                    Button(
                                        onClick = onImportClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = AncientGold),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("IMPORTAR", color = VoidBlack)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = CrimsonRed.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // SECCIÓN: DANGER ZONE
                    SettingsSectionTitle("Zona de Peligro", color = CrimsonRed)
                    Button(
                        onClick = onHardResetClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("HARD RESET (Borrar Todo)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String, color: Color = AncientGold) {
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AncientGold,
                checkedTrackColor = AncientGold.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}