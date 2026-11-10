package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.Attention
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.BuildingType
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple

/**
 * Repartir la Atención: el Sueño, la Vigilia y cada edificio visible, con − y +.
 * Mover un punto es gratis, así que el panel invita a probar.
 */
@Composable
fun AttentionDialog(
    allocation: Map<String, Int>,
    points: Int,
    ascensions: Int,
    buildings: List<Building>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val used = Attention.used(allocation)
    val free = points - used
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            border = BorderStroke(1.dp, AncientGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.attention_title), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, stringResource(R.string.close), tint = Color.Gray) }
                }
                Text(stringResource(R.string.attention_intro), color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.attention_used, used, points),
                        color = if (free > 0) Color.White else AncientGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f).testTag(ATTENTION_USED_TAG)
                    )
                    TextButton(onClick = onClear, enabled = used > 0) { Text(stringResource(R.string.attention_clear)) }
                }
                Attention.nextPointAt(ascensions)?.let {
                    Text(stringResource(R.string.attention_next, it), color = Color.Gray, fontSize = 11.sp)
                }
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    AttentionRow(
                        stringResource(R.string.attention_dream), stringResource(R.string.attention_dream_effect),
                        Attention.DREAM, allocation, free, onAdd, onRemove
                    )
                    AttentionRow(
                        stringResource(R.string.attention_vigil), stringResource(R.string.attention_vigil_effect),
                        Attention.VIGIL, allocation, free, onAdd, onRemove
                    )
                    buildings.forEach { building ->
                        AttentionRow(
                            tr(building.name),
                            stringResource(if (building.type == BuildingType.CLICK) R.string.attention_click_effect else R.string.attention_production_effect),
                            building.id, allocation, free, onAdd, onRemove
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttentionRow(
    name: String,
    effect: String,
    target: String,
    allocation: Map<String, Int>,
    free: Int,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    val points = allocation[target] ?: 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = if (points > 0) Color.White else Color.LightGray, fontSize = 14.sp, fontWeight = if (points > 0) FontWeight.Bold else FontWeight.Normal)
            Text(
                if (points > 0) "$effect · " + stringResource(R.string.attention_bonus, (Attention.BONUS_PER_POINT * 100 * points).toInt()) else effect,
                color = if (points > 0) ArcanePurple else Color.Gray,
                fontSize = 11.sp
            )
        }
        IconButton(onClick = { onRemove(target) }, enabled = points > 0, modifier = Modifier.testTag("$ATTENTION_REMOVE_TAG$target")) {
            Text("−", color = if (points > 0) Color.White else Color.DarkGray, fontSize = 20.sp)
        }
        Text("$points", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.width(24.dp))
        IconButton(onClick = { onAdd(target) }, enabled = free > 0, modifier = Modifier.testTag("$ATTENTION_ADD_TAG$target")) {
            Icon(Icons.Default.Add, stringResource(R.string.cd_attention_add), tint = if (free > 0) AncientGold else Color.DarkGray)
        }
    }
}

const val ATTENTION_USED_TAG = "attention_used"
const val ATTENTION_ADD_TAG = "attention_add_"
const val ATTENTION_REMOVE_TAG = "attention_remove_"
