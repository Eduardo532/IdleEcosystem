package com.EGomez.idleecosystem.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.StatisticsManager
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import com.EGomez.idleecosystem.ui.theme.SurfaceGrey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun StatisticsScreen(
    statisticsManager: StatisticsManager,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    fun formatTime(seconds: Long): String {
        val days = TimeUnit.SECONDS.toDays(seconds)
        val hours = TimeUnit.SECONDS.toHours(seconds) % 24
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val secs = seconds % 60

        return if (days > 0) "${days}d ${hours}h ${minutes}m"
        else if (hours > 0) "${hours}h ${minutes}m ${secs}s"
        else "${minutes}m ${secs}s"
    }

    fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF121212),
            border = BorderStroke(1.dp, AncientGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Registro Akáshico", color = AncientGold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray) }
                }

                HorizontalDivider(color = AncientGold.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {

                    StatSection("Tiempo y Existencia") {
                        StatRow("Inicio de la Obsesión", formatDate(statisticsManager.startTime))
                        StatRow("Tiempo Activo (App Abierta)", formatTime(statisticsManager.getActivePlayTime()))
                        StatRow("Tiempo en el Vacío (Offline)", formatTime(statisticsManager.totalOfflineSeconds))
                        StatRow("Existencia Total", formatTime(statisticsManager.totalPlayTimeSeconds))
                        StatRow("Tiempo en esta Vida", formatTime(statisticsManager.runTimeSeconds))
                    }

                    StatSection("Conocimiento Prohibido") {
                        StatRow("Generado (Histórico)", statisticsManager.lifetimeKnowledge.toBeautifulString())
                        StatRow("Generado (Esta Vida)", statisticsManager.runKnowledge.toBeautifulString())
                        StatRow("Mayor Producción/seg", statisticsManager.maxKnowledgePerSecond.toBeautifulString())
                    }

                    StatSection("Interacción") {
                        StatRow("Clics Realizados", statisticsManager.totalClicks.toString())
                        StatRow("Conocimiento Manual", statisticsManager.manualKnowledgeGenerated.toBeautifulString())
                        val total = statisticsManager.lifetimeKnowledge
                        val manual = statisticsManager.manualKnowledgeGenerated
                        val percent = if (total > java.math.BigDecimal.ZERO) {
                            manual.multiply(java.math.BigDecimal(100)).divide(total, 2, java.math.RoundingMode.HALF_UP)
                        } else java.math.BigDecimal.ZERO
                        StatRow("Porcentaje Manual", "$percent%")
                    }

                    StatSection("Maestría Rúnica") {
                        StatRow("Runas Cortadas (Tier 1)", statisticsManager.runesSevered.toString())
                        StatRow("Runas Estabilizadas (Tier 2)", statisticsManager.runesStabilized.toString())
                        StatRow("Runas Inscritas (Tier 3)", statisticsManager.runesInscribed.toString())
                    }

                    StatSection("Locura y Ascensión") {
                        StatRow("Veces Ascendido", statisticsManager.totalPrestiges.toString())
                        StatRow("Cordura Obtenida", statisticsManager.lifetimeSanity.toBeautifulString())
                        StatRow("Cordura Gastada", statisticsManager.spentSanity.toBeautifulString())
                    }

                    StatSection("Misterios") {
                        StatRow("Eventos Encontrados", statisticsManager.totalEventsFound.toString())
                        StatRow("Ecos Hallados", statisticsManager.totalEchoes.toBeautifulString())
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "\"El tiempo es un círculo plano...\"",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun StatSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, color = ArcanePurple, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceGrey.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) { Column(modifier = Modifier.padding(12.dp)) { content() } }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray, fontSize = 14.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}