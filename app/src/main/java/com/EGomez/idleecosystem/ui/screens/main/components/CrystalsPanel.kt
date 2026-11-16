package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.YuggothCrystals
import java.math.BigDecimal
import java.util.Locale

/** Color de los Cristales de Yuggoth: violeta mineral. */
val CrystalViolet = Color(0xFFB39DDB)

/** Lo que la pestaña Cristales necesita del motor (v0.22.0). */
data class CrystalsUi(
    val grid: List<Int> = List(YuggothCrystals.CELLS) { 0 },
    val power: Double = 0.0,
    val lustre: Int = 0,
    val lustreGain: Int = 0,
    val nextSpawn: Double = 0.0
)

/** Del violeta pálido (grado 1) al rosa encendido (grado 12 o más). */
private fun tierColor(tier: Int): Color =
    lerp(Color(0xFF5E4B8B), Color(0xFFFF4081), ((tier - 1) / 11f).coerceIn(0f, 1f))

/**
 * La pestaña Cristales (capa Despertar): la cuadrícula de 4 × 4, el Poder y lo que
 * multiplica, cuándo llega el próximo cristal, fusionar todo y Pulir.
 * Fusionar a mano: tocar un cristal y después otro del mismo grado.
 */
@Composable
fun CrystalsPanel(ui: CrystalsUi, onMerge: (Int, Int) -> Unit, onMergeAll: () -> Unit, onPolish: () -> Unit) {
    var selected by remember { mutableStateOf<Int?>(null) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(CRYSTALS_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF17141F),
            border = BorderStroke(1.dp, CrystalViolet.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.crystals_title), color = CrystalViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.crystals_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.crystals_lustre), color = CrystalViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${ui.lustre}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            stringResource(
                R.string.crystals_power,
                BigDecimal.valueOf(ui.power).toBeautifulString(),
                BigDecimal.valueOf(YuggothCrystals.productionMultiplier(ui.power)).toBeautifulString()
            ),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp).testTag(CRYSTALS_POWER_TAG)
        )
        Text(
            if (ui.grid.none { it == 0 }) stringResource(R.string.crystals_full)
            else stringResource(R.string.crystals_next, "%.1f".format(Locale.US, ui.nextSpawn)),
            color = Color.Gray,
            fontSize = 11.sp
        )
        // La cuadrícula
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            (0 until YuggothCrystals.SIZE).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0 until YuggothCrystals.SIZE).forEach { column ->
                        val cell = row * YuggothCrystals.SIZE + column
                        val tier = ui.grid.getOrElse(cell) { 0 }
                        val isSelected = selected == cell
                        val pairs = selected != null && selected != cell && tier > 0 && ui.grid.getOrNull(selected!!) == tier
                        Surface(
                            onClick = {
                                val from = selected
                                selected = when {
                                    tier == 0 -> null
                                    from == null -> cell
                                    from == cell -> null
                                    YuggothCrystals.canMerge(ui.grid, from, cell) -> { onMerge(from, cell); null }
                                    else -> cell
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (tier > 0) tierColor(tier).copy(alpha = 0.35f) else Color(0xFF141218),
                            border = BorderStroke(
                                if (isSelected || pairs) 2.dp else 1.dp,
                                when {
                                    isSelected -> Color.White
                                    pairs -> CrystalViolet
                                    tier > 0 -> tierColor(tier)
                                    else -> Color(0xFF2A2633)
                                }
                            ),
                            modifier = Modifier.weight(1f).aspectRatio(1f).testTag(CRYSTAL_CELL_TAG + cell)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (tier > 0) Text("$tier", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
            OutlinedButton(
                onClick = { selected = null; onMergeAll() },
                border = BorderStroke(1.dp, CrystalViolet.copy(alpha = 0.6f)),
                modifier = Modifier.weight(1f).testTag(CRYSTALS_MERGE_ALL_TAG)
            ) {
                Text(stringResource(R.string.crystals_merge_all), color = CrystalViolet, fontSize = 12.sp)
            }
            Button(
                onClick = { selected = null; onPolish() },
                enabled = ui.lustreGain > 0,
                colors = ButtonDefaults.buttonColors(containerColor = CrystalViolet, contentColor = Color.Black),
                modifier = Modifier.weight(1f).testTag(CRYSTALS_POLISH_TAG)
            ) {
                Text(
                    if (ui.lustreGain > 0) stringResource(R.string.crystals_polish, ui.lustreGain)
                    else stringResource(R.string.crystals_polish_locked, YuggothCrystals.POLISH_TIER),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(stringResource(R.string.crystals_rules), color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

const val CRYSTALS_PANEL_TAG = "crystals_panel"
const val CRYSTALS_POWER_TAG = "crystals_power"
const val CRYSTAL_CELL_TAG = "crystal_cell_"
const val CRYSTALS_MERGE_ALL_TAG = "crystals_merge_all"
const val CRYSTALS_POLISH_TAG = "crystals_polish"
