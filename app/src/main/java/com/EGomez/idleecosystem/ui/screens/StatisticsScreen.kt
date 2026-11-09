package com.EGomez.idleecosystem.ui.screens

import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
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
import com.EGomez.idleecosystem.domain.model.GameStatistics
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import com.EGomez.idleecosystem.ui.theme.SurfaceGrey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun StatisticsScreen(
    stats: GameStatistics,
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
                    Text(stringResource(R.string.stats_title), color = AncientGold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = Color.Gray) }
                }

                HorizontalDivider(color = AncientGold.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {

                    StatSection(stringResource(R.string.stats_time)) {
                        StatRow(stringResource(R.string.stats_start), formatDate(stats.startTime))
                        StatRow(stringResource(R.string.stats_active_time), formatTime(stats.activePlayTimeSeconds))
                        StatRow(stringResource(R.string.stats_offline_time), formatTime(stats.totalOfflineSeconds))
                        StatRow(stringResource(R.string.stats_total_time), formatTime(stats.totalPlayTimeSeconds))
                        StatRow(stringResource(R.string.stats_run_time), formatTime(stats.runTimeSeconds))
                    }

                    StatSection(stringResource(R.string.stats_knowledge)) {
                        StatRow(stringResource(R.string.stats_lifetime), stats.lifetimeKnowledge.toBeautifulString())
                        StatRow(stringResource(R.string.stats_this_run), stats.runKnowledge.toBeautifulString())
                        StatRow(stringResource(R.string.stats_peak), stats.maxKnowledgePerSecond.toBeautifulString())
                    }

                    StatSection(stringResource(R.string.stats_interaction)) {
                        StatRow(stringResource(R.string.stats_clicks), stats.totalClicks.toString())
                        StatRow(stringResource(R.string.stats_manual), stats.manualKnowledgeGenerated.toBeautifulString())
                        val total = stats.lifetimeKnowledge
                        val manual = stats.manualKnowledgeGenerated
                        val percent = if (total > java.math.BigDecimal.ZERO) {
                            manual.multiply(java.math.BigDecimal(100)).divide(total, 2, java.math.RoundingMode.HALF_UP)
                        } else java.math.BigDecimal.ZERO
                        StatRow(stringResource(R.string.stats_manual_percent), "$percent%")
                    }

                    StatSection(stringResource(R.string.stats_runes)) {
                        StatRow(stringResource(R.string.stats_runes_t1), stats.runesSevered.toString())
                        StatRow(stringResource(R.string.stats_runes_t2), stats.runesStabilized.toString())
                        StatRow(stringResource(R.string.stats_runes_t3), stats.runesInscribed.toString())
                    }

                    StatSection(stringResource(R.string.stats_ascension)) {
                        StatRow(stringResource(R.string.stats_ascensions), stats.totalPrestiges.toString())
                        StatRow(stringResource(R.string.stats_sanity_gained), stats.lifetimeSanity.toBeautifulString())
                        StatRow(stringResource(R.string.stats_sanity_spent), stats.spentSanity.toBeautifulString())
                    }

                    StatSection(stringResource(R.string.stats_mysteries)) {
                        StatRow(stringResource(R.string.stats_events), stats.totalEventsFound.toString())
                        StatRow(stringResource(R.string.stats_echoes), stats.totalEchoes.toBeautifulString())
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.stats_quote),
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