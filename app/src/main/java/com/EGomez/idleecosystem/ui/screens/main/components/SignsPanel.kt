package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.ElderSign
import com.EGomez.idleecosystem.domain.logic.ElderSigns
import com.EGomez.idleecosystem.domain.logic.Planet
import com.EGomez.idleecosystem.domain.logic.SignRarity
import com.EGomez.idleecosystem.ui.i18n.tr
import java.util.Locale

/** Lo que la pestaña Signos necesita del motor (v0.21.2). */
data class SignsUi(
    val awakenings: Int = 0,
    val dust: Int = 0,
    /** Número -> Signo empaquetado. */
    val signs: Map<Int, Long> = emptyMap(),
    /** Planeta -> número del Signo equipado. */
    val equipped: Map<String, Int> = emptyMap()
)

/** El color de cada rareza. */
fun SignRarity.color(): Color = when (this) {
    SignRarity.COMMON -> Color(0xFFB0BEC5)
    SignRarity.RARE -> Color(0xFF64B5F6)
    SignRarity.EPIC -> Color(0xFFBA68C8)
    SignRarity.LEGENDARY -> Color(0xFFFFB74D)
    SignRarity.MYTHIC -> Color(0xFFEF5350)
}

private fun times(value: Double): String = "x" + "%.2f".format(Locale.US, value)

/**
 * La pestaña Signos (capa Despertar): los cuatro planetas con su Signo equipado y el
 * inventario, donde cada Signo se equipa, se mejora con Polvo Astral o se vende.
 */
@Composable
fun SignsPanel(
    ui: SignsUi,
    onEquip: (Int, String) -> Unit,
    onUnequip: (String) -> Unit,
    onUpgrade: (Int) -> Unit,
    onSell: (Int) -> Unit,
    onSellCommons: () -> Unit
) {
    val signs = ui.signs.mapValues { ElderSign.unpack(it.value) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(SIGNS_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1B10),
            border = BorderStroke(1.dp, VisionColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.signs_title), color = VisionColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.signs_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.signs_dust), color = VisionColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${ui.dust}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag(SIGNS_DUST_TAG))
                }
            }
        }
        // Planetas
        Text(stringResource(R.string.signs_planets), color = VisionColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
        Planet.entries.forEach { planet ->
            val open = planet.isOpen(ui.awakenings)
            val sign = ui.equipped[planet.id]?.let { signs[it] }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF16150F),
                border = BorderStroke(1.dp, if (sign != null) sign.rarity.color().copy(alpha = 0.7f) else Color(0xFF26251A)),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(PLANET_TAG + planet.id)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            tr(planet.title) + " · " + stringResource(R.string.signs_affinity, tr(planet.affinity.title)),
                            color = if (open) Color.White else Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            when {
                                !open -> stringResource(R.string.signs_planet_locked, planet.awakenings)
                                sign == null -> stringResource(R.string.signs_planet_empty)
                                else -> signLine(sign) + " → " + tr(sign.kind.effect) + " " + times(sign.multiplier(planet))
                            },
                            color = if (sign != null) sign.rarity.color() else Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    if (sign != null) {
                        TextButton(onClick = { onUnequip(planet.id) }, modifier = Modifier.testTag(UNEQUIP_TAG + planet.id)) {
                            Text(stringResource(R.string.signs_unequip), color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        // Inventario
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.signs_inventory, signs.size, ElderSigns.INVENTORY),
                color = VisionColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (signs.values.any { it.rarity == SignRarity.COMMON }) {
                TextButton(onClick = onSellCommons, modifier = Modifier.testTag(SELL_COMMONS_TAG)) {
                    Text(stringResource(R.string.signs_sell_commons), color = Color(0xFFEF9A9A), fontSize = 11.sp)
                }
            }
        }
        if (signs.isEmpty()) Text(stringResource(R.string.signs_empty), color = Color.Gray, fontSize = 11.sp)
        val equippedNumbers = ui.equipped.values.toSet()
        signs.entries
            .sortedWith(compareByDescending<Map.Entry<Int, ElderSign>> { it.value.rarity.ordinal }.thenByDescending { it.value.bonus() })
            .forEach { (n, sign) -> SignRow(n, sign, n in equippedNumbers, ui, onEquip, onUpgrade, onSell) }
    }
}

/** "Signo de la Llama · Épico · 87 % · Nv 3". */
@Composable
private fun signLine(sign: ElderSign): String =
    tr(sign.kind.title) + " · " + tr(sign.rarity.title) + " · " + sign.quality + " % · " + stringResource(R.string.beast_level_short, sign.level)

@Composable
private fun SignRow(
    n: Int,
    sign: ElderSign,
    isEquipped: Boolean,
    ui: SignsUi,
    onEquip: (Int, String) -> Unit,
    onUpgrade: (Int) -> Unit,
    onSell: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF16150F),
        border = BorderStroke(1.dp, sign.rarity.color().copy(alpha = if (isEquipped) 1f else 0.4f)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(SIGN_TAG + n)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(signLine(sign), color = sign.rarity.color(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.signs_bonus, tr(sign.kind.effect), "+" + "%.0f".format(Locale.US, sign.bonus() * 100) + " %") +
                    if (isEquipped) " · " + stringResource(R.string.signs_equipped) else "",
                color = Color.LightGray,
                fontSize = 11.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                Planet.entries.filter { it.isOpen(ui.awakenings) && ui.equipped[it.id] != n }.forEach { planet ->
                    OutlinedButton(
                        onClick = { onEquip(n, planet.id) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp).testTag(EQUIP_TAG + n + "_" + planet.id)
                    ) {
                        Text(tr(planet.title), color = if (planet.affinity == sign.kind) VisionColor else Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                if (!sign.isMaxed) {
                    Button(
                        onClick = { onUpgrade(n) },
                        enabled = ui.dust >= sign.upgradeCost(),
                        colors = ButtonDefaults.buttonColors(containerColor = VisionColor, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp).testTag(UPGRADE_SIGN_TAG + n)
                    ) {
                        Text(stringResource(R.string.signs_upgrade, sign.upgradeCost()), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = { onSell(n) }, modifier = Modifier.height(32.dp).testTag(SELL_SIGN_TAG + n)) {
                    Text(stringResource(R.string.signs_sell, sign.dust()), color = Color(0xFFEF9A9A), fontSize = 11.sp)
                }
            }
        }
    }
}

const val SIGNS_PANEL_TAG = "signs_panel"
const val SIGNS_DUST_TAG = "signs_dust"
const val PLANET_TAG = "planet_"
const val UNEQUIP_TAG = "unequip_"
const val SIGN_TAG = "sign_"
const val EQUIP_TAG = "equip_"
const val UPGRADE_SIGN_TAG = "upgrade_sign_"
const val SELL_SIGN_TAG = "sell_sign_"
const val SELL_COMMONS_TAG = "sell_commons"
