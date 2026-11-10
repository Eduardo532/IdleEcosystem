package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.RuneEngraving
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.RuneVariant
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.theme.AncientGold

/**
 * El Altar de Runas (pestaña Panteón): los espacios ganados con las Pruebas, las runas
 * grabadas y el catálogo de runas con cuántas veces se atrapó cada una.
 */
@Composable
fun RuneAltar(
    slots: Int,
    engraved: List<String>,
    catches: Map<String, Int>,
    onEngrave: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(stringResource(R.string.rune_altar_intro), color = Color.Gray, fontSize = 12.sp)
        Text(
            stringResource(R.string.rune_altar_slots, engraved.size, slots),
            color = AncientGold,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp).testTag(RUNE_SLOTS_TAG)
        )
        if (slots == 0) {
            Text(stringResource(R.string.rune_altar_no_slots), color = Color.LightGray, fontSize = 12.sp)
        }
        RuneTier.entries.forEach { tier ->
            Text(
                stringResource(
                    when (tier) {
                        RuneTier.TIER_1_SWIPE -> R.string.rune_tier_1
                        RuneTier.TIER_2_HOLD -> R.string.rune_tier_2
                        RuneTier.TIER_3_TRACE -> R.string.rune_tier_3
                    },
                    RuneEngraving.required(tier)
                ),
                color = AncientGold,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
            RuneEngraving.ALL.filter { it.tier == tier }.forEach { rune ->
                RuneRow(
                    rune = rune,
                    caught = catches[rune.id] ?: 0,
                    isEngraved = rune.id in engraved,
                    freeSlot = engraved.size < slots,
                    onEngrave = { onEngrave(rune.id) },
                    onRemove = { onRemove(rune.id) }
                )
            }
        }
    }
}

@Composable
private fun RuneRow(
    rune: RuneVariant,
    caught: Int,
    isEngraved: Boolean,
    freeSlot: Boolean,
    onEngrave: () -> Unit,
    onRemove: () -> Unit
) {
    val required = RuneEngraving.required(rune.tier)
    val known = caught > 0
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isEngraved) Color(0xFF221A08) else Color(0xFF151515),
        border = BorderStroke(1.dp, if (isEngraved) AncientGold else Color(0xFF2E2E2E)),
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).testTag(RUNE_ROW_TAG + rune.id)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(if (known) rune.color else Color(0xFF333333), CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (known) tr(rune.name) else "???",
                    color = if (known) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (known) {
                    PactEffectList(pactLines(RuneEngraving.passive(rune.id), emptyMap()), fontSize = 11)
                }
                Text(
                    stringResource(R.string.rune_caught, caught.coerceAtMost(required), required),
                    color = if (caught >= required) Color(0xFF81C784) else Color.Gray,
                    fontSize = 10.sp
                )
            }
            when {
                isEngraved -> TextButton(onClick = onRemove, modifier = Modifier.testTag(RUNE_REMOVE_TAG + rune.id)) {
                    Text(stringResource(R.string.rune_remove), color = Color.Gray, fontSize = 12.sp)
                }
                caught >= required -> Button(
                    onClick = onEngrave,
                    enabled = freeSlot,
                    colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(RUNE_ENGRAVE_TAG + rune.id)
                ) {
                    Text(stringResource(R.string.rune_engrave), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

const val RUNE_SLOTS_TAG = "rune_slots"
const val RUNE_ROW_TAG = "rune_row_"
const val RUNE_ENGRAVE_TAG = "rune_engrave_"
const val RUNE_REMOVE_TAG = "rune_remove_"
