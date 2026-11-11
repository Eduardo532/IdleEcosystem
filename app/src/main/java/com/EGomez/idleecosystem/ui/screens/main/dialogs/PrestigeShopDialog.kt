package com.EGomez.idleecosystem.ui.screens.main.dialogs

import com.EGomez.idleecosystem.ui.i18n.tr
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.remember
import com.EGomez.idleecosystem.domain.logic.SanitySpending
import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import java.util.Locale
import java.text.DecimalFormatSymbols
import java.text.DecimalFormat
import com.EGomez.idleecosystem.R
import java.math.BigDecimal
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.ui.components.ConstellationMap
import com.EGomez.idleecosystem.domain.model.Constellation
import com.EGomez.idleecosystem.ui.theme.MadnessRed
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple

@Composable
fun PrestigeShopDialog(
    upgrades: List<PrestigeUpgrade>,
    currencyFormatted: String,
    currency: BigDecimal,
    onBuy: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Se guarda el ID, no el objeto: el nodo se busca en la lista ACTUAL (si no, tras
    // comprar el panel seguía mostrando el nivel y el costo viejos)
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedNode = upgrades.find { it.id == selectedId }
    var listView by rememberSaveable { mutableStateOf(false) }
    var focusId by rememberSaveable { mutableStateOf<String?>(null) }
    var focusKey by remember { mutableIntStateOf(0) }
    val focus = Constellation.fromId(focusId)
    val present = Constellation.entries.filter { c -> upgrades.any { it.constellation == c } }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
            // Cabecera: título, Cordura, Cielo/Lista y cerrar
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.madness_map), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(stringResource(R.string.sanity_amount, currencyFormatted), color = ArcanePurple, fontWeight = FontWeight.Bold)
                }
                MapViewToggle(listView) { listView = it }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, stringResource(R.string.close), tint = Color.White) }
            }
            // Constelaciones: acercan el cielo o filtran la lista
            if (present.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    ConstellationChip(stringResource(R.string.map_all), AncientGold, focus == null, MAP_CHIP_TAG + "all") {
                        focusId = null; focusKey++
                    }
                    present.forEach { c ->
                        val stars = upgrades.filter { it.constellation == c && !MadnessPaths.isPath(it.id) }
                        ConstellationChip(
                            "${c.titleText()} ${stars.count { it.level > 0 }}/${stars.size}",
                            c.color(), focus == c, MAP_CHIP_TAG + c.id
                        ) { focusId = c.id; focusKey++ }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (listView) {
                    MapList(upgrades, focus, currency, selectedId) { selectedId = it.id }
                } else {
                    Box(modifier = Modifier.fillMaxSize().testTag(CONSTELLATION_MAP_TAG)) {
                        ConstellationMap(
                            upgrades = upgrades,
                            onNodeClick = { node -> selectedId = node.id },
                            currency = currency,
                            selectedId = selectedId,
                            focus = focus,
                            focusKey = focusKey
                        )
                    }
                    if (selectedNode == null) {
                        Text(
                            stringResource(R.string.map_zoom_hint),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                        )
                    }
                }
            }
            selectedNode?.let { node -> NodeCard(node, upgrades, currency, onBuy) { selectedId = null } }
        }
    }
}

/** Cielo o lista. */
@Composable
private fun MapViewToggle(listView: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.background(Color(0xFF1A1A1A), RoundedCornerShape(10.dp)).padding(2.dp).testTag(MAP_VIEW_TOGGLE_TAG)
    ) {
        listOf(false to R.string.map_view_sky, true to R.string.map_view_list).forEach { (value, label) ->
            val selected = listView == value
            Text(
                stringResource(label),
                color = if (selected) Color.Black else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(if (selected) AncientGold else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onChange(value) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag(MAP_VIEW_TOGGLE_TAG + if (value) "list" else "sky")
            )
        }
    }
}

@Composable
private fun ConstellationChip(label: String, color: Color, selected: Boolean, tag: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) color.copy(alpha = 0.2f) else Color(0xFF151515),
        border = BorderStroke(1.dp, if (selected) color else Color(0xFF2E2E2E)),
        modifier = Modifier.testTag(tag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = if (selected) color else Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** La vista de lista: cada constelación con sus estrellas y su Senda. */
@Composable
private fun MapList(
    upgrades: List<PrestigeUpgrade>,
    focus: Constellation?,
    currency: BigDecimal,
    selectedId: String?,
    onSelect: (PrestigeUpgrade) -> Unit
) {
    val groups: List<Pair<Constellation?, List<PrestigeUpgrade>>> =
        (listOf<Constellation?>(null) + Constellation.entries)
            .filter { focus == null || it == focus }
            .map { c -> c to upgrades.filter { it.constellation == c }.sortedBy { MadnessPaths.isPath(it.id) } }
            .filter { it.second.isNotEmpty() }
    LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.fillMaxSize()) {
        groups.forEach { (c, nodes) ->
            item(key = "header_" + (c?.id ?: "core")) {
                Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                    Text(
                        c?.titleText()?.uppercase() ?: stringResource(R.string.map_core),
                        color = c.color(),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    c?.let { Text(tr(it.description), color = Color.Gray, fontSize = 11.sp) }
                }
            }
            items(nodes, key = { it.id }) { node -> MapRow(node, upgrades, currency, node.id == selectedId) { onSelect(node) } }
        }
    }
}

@Composable
private fun MapRow(node: PrestigeUpgrade, upgrades: List<PrestigeUpgrade>, currency: BigDecimal, selected: Boolean, onClick: () -> Unit) {
    val state = nodeState(node, upgrades, currency)
    val color = node.constellation.color()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Color(0xFF221A2A) else Color(0xFF131313),
        border = BorderStroke(1.dp, when {
            state.sealed -> MadnessRed.copy(alpha = 0.6f)
            state.affordable -> color
            node.level > 0 -> color.copy(alpha = 0.45f)
            else -> Color(0xFF2A2A2A)
        }),
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).testTag(MAP_NODE_ROW_TAG + node.id)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (MadnessPaths.isPath(node.id)) "✦" else if (node.level > 0) "●" else if (state.reachable) "○" else "·",
                color = if (state.reachable || node.level > 0) color else Color.DarkGray,
                fontSize = 16.sp,
                modifier = Modifier.width(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tr(node.name) + if (node.level > 0) " · " + levelText(node) else "",
                    color = if (state.reachable || node.level > 0) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(mapNodeEffect(node, maxOf(node.level, 1)), color = Color(0xFFB0BEC5), fontSize = 11.sp, maxLines = 2)
            }
            Text(
                when {
                    node.isMaxed -> stringResource(R.string.map_max_short)
                    state.sealed || !state.reachable -> "🔒"
                    else -> node.currentCost.toBeautifulString()
                },
                color = if (state.affordable) AncientGold else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun levelText(node: PrestigeUpgrade): String =
    node.maxLevel?.let { stringResource(R.string.map_level_of, node.level, it) } ?: stringResource(R.string.level, node.level)

/** El estado de un nodo para dibujarlo y para su botón. */
private data class NodeState(val reachable: Boolean, val sealed: Boolean, val affordable: Boolean, val pathLocked: Boolean)

private fun nodeState(node: PrestigeUpgrade, upgrades: List<PrestigeUpgrade>, currency: BigDecimal): NodeState {
    val isPath = MadnessPaths.isPath(node.id)
    val sealed = MadnessPaths.isSealed(node, upgrades)
    val pathLocked = isPath && !MadnessPaths.isAvailable(node, upgrades)
    val parentOk = isPath || node.parentId == null || (upgrades.find { it.id == node.parentId }?.level ?: 0) > 0
    val reachable = !sealed && !pathLocked && parentOk
    return NodeState(reachable, sealed, reachable && !node.isMaxed && currency >= node.currentCost, pathLocked)
}

/** La ficha del nodo elegido: qué da ahora, qué daría el siguiente nivel y el botón. */
@Composable
private fun NodeCard(
    node: PrestigeUpgrade,
    upgrades: List<PrestigeUpgrade>,
    currency: BigDecimal,
    onBuy: (String) -> Unit,
    onClose: () -> Unit
) {
    val state = nodeState(node, upgrades, currency)
    val color = node.constellation.color()
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp).testTag(MAP_NODE_CARD_TAG)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1E)),
        border = BorderStroke(1.dp, if (state.reachable) color else Color.Gray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    node.constellation?.let {
                        Text(it.titleText().uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Text(tr(node.name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                if (node.level > 0 || node.maxLevel != null) Text(levelText(node), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, stringResource(R.string.close), tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
            Text(tr(node.description), color = Color.LightGray, fontSize = 13.sp)
            if (MadnessPaths.isPath(node.id)) {
                val sealedBy = if (state.sealed) MadnessPaths.active(upgrades) else null
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    when {
                        sealedBy != null -> stringResource(R.string.path_sealed, tr(sealedBy.name))
                        state.pathLocked -> stringResource(R.string.map_path_locked)
                        else -> stringResource(R.string.path_exclusive)
                    },
                    color = if (sealedBy != null || state.pathLocked) Color(0xFFE57373) else AncientGold,
                    fontSize = 12.sp,
                    modifier = Modifier.testTag(PATH_NOTE_TAG)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (node.level > 0) Text(stringResource(R.string.map_now, mapNodeEffect(node, node.level)), color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (!node.isMaxed) Text(stringResource(R.string.map_next, mapNodeEffect(node, node.level + 1)), color = Color(0xFFB0BEC5), fontSize = 12.sp)
            if (state.reachable && !node.isMaxed) SpendPreviewLines(node, currency)
            Spacer(modifier = Modifier.height(12.dp))
            val disabledLabel = when {
                state.sealed -> R.string.path_sealed_button
                node.isMaxed -> R.string.map_maxed
                state.pathLocked -> R.string.map_path_locked_button
                !state.reachable -> R.string.locked_node
                else -> null
            }
            if (disabledLabel != null) {
                Button(
                    onClick = {}, enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = if (state.sealed) Color(0xFF3A1A1A) else Color(0xFF2A2A2A))
                ) { Text(stringResource(disabledLabel), color = if (state.sealed) Color(0xFFE57373) else Color.Gray) }
            } else {
                val cost = node.currentCost
                val canAfford = currency >= cost
                Button(
                    onClick = { onBuy(node.id) },
                    enabled = canAfford,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArcanePurple,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF333333),
                        disabledContentColor = Color.LightGray
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.upgrade_button), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.cost_sanity, cost.toBeautifulString()), fontSize = 10.sp)
                        if (!canAfford) {
                            Text(
                                stringResource(R.string.missing_sanity, cost.subtract(currency).toBeautifulString()),
                                fontSize = 10.sp,
                                color = Color(0xFFE57373)
                            )
                        }
                    }
                }
            }
        }
    }
}

const val MAP_VIEW_TOGGLE_TAG = "map_view_"
const val MAP_CHIP_TAG = "map_chip_"
const val MAP_NODE_ROW_TAG = "map_node_"
const val MAP_NODE_CARD_TAG = "map_node_card"
const val CONSTELLATION_MAP_TAG = "constellation_map"
const val PATH_NOTE_TAG = "path_note"

/**
 * Las dos caras de gastar Cordura: lo que sube el nodo y lo que baja el bono de la
 * Cordura Residual. En los nodos globales, además, el efecto neto en la producción.
 */
@Composable
private fun SpendPreviewLines(node: PrestigeUpgrade, sanity: BigDecimal) {
    val preview = remember(node.id, node.level, sanity) { SanitySpending.preview(node, sanity) }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        stringResource(R.string.spend_node, multiplierText(preview.nodeBefore), multiplierText(preview.nodeAfter)),
        color = Color.LightGray,
        fontSize = 12.sp
    )
    Text(
        stringResource(R.string.spend_sanity_bonus, multiplierText(preview.sanityBonusBefore), multiplierText(preview.sanityBonusAfter)),
        color = Color.Gray,
        fontSize = 12.sp
    )
    preview.netProduction?.let { net ->
        Text(
            stringResource(R.string.spend_net, multiplierText(net)),
            color = if (net >= BigDecimal.ONE) Color(0xFF81C784) else Color(0xFFE57373),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.testTag(SPEND_NET_TAG)
        )
    }
}

/** "1.25" o, para multiplicadores enormes, la notación del juego. */
private fun multiplierText(value: BigDecimal): String =
    if (value < BigDecimal(1_000)) DecimalFormat("0.00", DecimalFormatSymbols(Locale.US)).format(value)
    else value.toBeautifulString()

const val SPEND_NET_TAG = "spend_net"

