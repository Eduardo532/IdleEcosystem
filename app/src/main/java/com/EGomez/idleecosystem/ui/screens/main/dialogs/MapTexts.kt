package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.BuildingCatalog
import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import com.EGomez.idleecosystem.domain.logic.PrestigeShopManager
import com.EGomez.idleecosystem.domain.model.Constellation
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.UpgradeType
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.screens.main.components.pactLines
import com.EGomez.idleecosystem.ui.theme.AncientGold
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** El color de cada constelación del Mapa de Locura (v0.19.2). */
fun Constellation?.color(): Color = when (this) {
    Constellation.HAND -> Color(0xFFFFB74D)
    Constellation.GEARS -> Color(0xFF90A4AE)
    Constellation.DREAM -> Color(0xFF7986CB)
    Constellation.MIND -> Color(0xFFCE93D8)
    Constellation.VOID -> Color(0xFF4DD0E1)
    Constellation.CULT -> Color(0xFFE57373)
    Constellation.ARCANE -> Color(0xFF81C784)
    null -> AncientGold
}

private fun times(value: BigDecimal): String =
    DecimalFormat("0.00", DecimalFormatSymbols(Locale.US)).format(value)

private fun times(value: Double): String = times(BigDecimal.valueOf(value))

/**
 * Lo que da un nodo con [level] niveles, en una línea. Los nodos con efectos de pacto
 * pueden dar varias cosas: se unen con " · ".
 */
@Composable
fun mapNodeEffect(node: PrestigeUpgrade, level: Int): String {
    val buildingNames = remember { BuildingCatalog.create().associate { it.id to it.name } }
    val mult = BigDecimal.ONE.add(node.effectMultiplier.multiply(BigDecimal(level)))
    node.startingBuilding?.let { building ->
        return stringResource(R.string.map_fx_start, node.effectMultiplier.toInt() * level, tr(buildingNames[building] ?: building))
    }
    node.effectsPerLevel?.let { perLevel ->
        val lines = pactLines(perLevel.scaled(level.toDouble()), buildingNames)
        return if (lines.isEmpty()) "—" else lines.joinToString(" · ") { it.text }
    }
    return when {
        node.id == PrestigeShopManager.ATTENTION_NODE_ID -> stringResource(R.string.map_fx_attention, level)
        node.id == PrestigeShopManager.MADNESS_NODE_ID ->
            stringResource(R.string.map_fx_madness, times((1.0 - node.effectMultiplier.toDouble() * level).coerceAtLeast(0.1)))
        node.id == MadnessPaths.CULT -> stringResource(R.string.map_fx_cult, PrestigeShopManager.CULT_PATH_UNITS * level)
        node.type == UpgradeType.GLOBAL_MULTIPLIER -> stringResource(R.string.map_fx_production, times(mult))
        node.type == UpgradeType.CLICK_MULTIPLIER -> stringResource(R.string.map_fx_click, times(mult))
        node.id == "pu_idle_1" -> stringResource(R.string.map_fx_first_buildings, times(mult))
        node.id == "pu_idle_2" || node.id == MadnessPaths.GEARS -> stringResource(R.string.map_fx_all_buildings, times(mult))
        node.id.startsWith("pu_offline") || node.id == MadnessPaths.DREAM -> stringResource(R.string.map_fx_offline, times(mult))
        node.id.startsWith("pu_void") || node.id == MadnessPaths.VOID -> stringResource(R.string.map_fx_sanity, times(mult))
        else -> "x" + times(mult)
    }
}

@Composable
fun Constellation.titleText(): String = tr(title)
