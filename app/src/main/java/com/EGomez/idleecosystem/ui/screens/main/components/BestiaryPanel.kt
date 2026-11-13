package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.Beast
import com.EGomez.idleecosystem.domain.logic.BeastKind
import com.EGomez.idleecosystem.domain.logic.BeastRarity
import com.EGomez.idleecosystem.domain.logic.Bestiary
import com.EGomez.idleecosystem.domain.logic.SummonResult
import com.EGomez.idleecosystem.ui.components.CreatureArt
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import com.EGomez.idleecosystem.ui.i18n.tr
import java.math.BigDecimal
import kotlin.math.roundToInt

/** Color de los Sellos Antiguos y del Bestiario. */
val SealTeal = Color(0xFF80CBC4)

/** Lo que muestra la pestaña Bestiario (v0.19.10). */
data class BestiaryUi(
    val levels: Map<String, Int> = emptyMap(),
    val seals: Int = 0,
    val shards: Int = 0,
    val sealPrice: BigDecimal = BigDecimal.ZERO,
    val canBuySeals: Boolean = false,
    val sinceMythic: Int = 0,
    val sincePrimordial: Int = 0,
    val dailyStreak: Int = 0,
    val bonuses: Map<BeastKind, Double> = emptyMap(),
    val encounterId: String? = null,
    val encounterSecondsLeft: Int = 0
)

fun BeastRarity.uiColor(): Color = Color(color)

@Composable
fun BeastRarity.label(): String = tr(title)

@Composable
fun BeastKind.effectText(percent: Int): String = stringResource(
    when (this) {
        BeastKind.PRODUCTION -> R.string.beast_kind_production
        BeastKind.CLICK -> R.string.beast_kind_click
        BeastKind.SANITY -> R.string.beast_kind_sanity
        BeastKind.OFFLINE -> R.string.beast_kind_offline
        BeastKind.ETHER -> R.string.beast_kind_ether
        BeastKind.STARDUST -> R.string.beast_kind_stardust
        BeastKind.SEALS -> R.string.beast_kind_seals
        BeastKind.ENCOUNTER -> R.string.beast_kind_encounter
    },
    percent
)

@Composable
fun BeastKind.nameText(): String = stringResource(
    when (this) {
        BeastKind.PRODUCTION -> R.string.beast_kind_name_production
        BeastKind.CLICK -> R.string.beast_kind_name_click
        BeastKind.SANITY -> R.string.beast_kind_name_sanity
        BeastKind.OFFLINE -> R.string.beast_kind_name_offline
        BeastKind.ETHER -> R.string.beast_kind_name_ether
        BeastKind.STARDUST -> R.string.beast_kind_name_stardust
        BeastKind.SEALS -> R.string.beast_kind_name_seals
        BeastKind.ENCOUNTER -> R.string.beast_kind_name_encounter
    }
)

/** El dibujo de una criatura: a color si se conoce, como silueta si no. */
@Composable
fun BeastPortrait(beast: Beast, known: Boolean, modifier: Modifier = Modifier) {
    val domain = Bestiary.domainOf(beast)
    CreatureArt(
        recipe = beast.art,
        seedKey = beast.id,
        color = if (known) Color(domain.color) else Color(0xFF2A2A30),
        eyeColor = if (known) lerpRarity(beast.rarity) else Color(0xFF3A3A40),
        modifier = modifier
    )
}

/** Los ojos brillan con el color de la rareza, un poco aclarado. */
private fun lerpRarity(rarity: BeastRarity): Color = androidx.compose.ui.graphics.lerp(rarity.uiColor(), Color(0xFFFFF59D), 0.35f)

/**
 * La pestaña Bestiario (capa Ascensión): los Sellos Antiguos y los Fragmentos, el altar de
 * invocación, la compra de Sellos con Conocimiento, los bonos y la colección por Dominio.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BestiaryPanel(
    state: BestiaryUi,
    onSummon: (Int) -> Unit,
    onBuySeals: () -> Unit,
    onPickWithShards: (String) -> Unit
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var domainId by remember { mutableStateOf(Bestiary.domains.first().id) }
    val seen = state.levels.count { it.value > 0 }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(BESTIARY_PANEL_TAG)) {
        // --- Monedas y colección
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF101A19),
            border = BorderStroke(1.dp, SealTeal.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.seals_title), color = SealTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    IconLabel(GameIcons.ElderSeal, "${state.seals}", Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconLabel(GameIcons.Shard, stringResource(R.string.shards_count, state.shards), Color.LightGray, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.bestiary_title), color = SealTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("$seen / ${Bestiary.beasts.size}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (state.dailyStreak > 0) Text(stringResource(R.string.daily_streak, state.dailyStreak), color = Color.Gray, fontSize = 10.sp)
                }
            }
        }

        // --- Altar de invocación
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF14121C),
            border = BorderStroke(1.dp, BeastRarity.MYTHIC.uiColor().copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.summon_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(stringResource(R.string.summon_hint), color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SummonButton(stringResource(R.string.summon_one), Bestiary.SUMMON_COST, state.seals >= Bestiary.SUMMON_COST, SUMMON_ONE_TAG, Modifier.weight(1f)) { onSummon(1) }
                    SummonButton(stringResource(R.string.summon_ten), Bestiary.SUMMON_TEN_COST, state.seals >= Bestiary.SUMMON_TEN_COST, SUMMON_TEN_TAG, Modifier.weight(1f)) { onSummon(10) }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.summon_pity, Bestiary.PITY_MYTHIC - state.sinceMythic, Bestiary.PITY_PRIMORDIAL - state.sincePrimordial),
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BeastRarity.entries.forEach {
                        Text("%.1f%%".format(java.util.Locale.US, it.weight / 100.0), color = it.uiColor(), fontSize = 10.sp)
                    }
                }
            }
        }

        // --- Sellos con Conocimiento
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF151515),
            border = BorderStroke(1.dp, Color(0xFF2E2E2E)),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.seals_buy_title, Bestiary.SEALS_PER_PURCHASE), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(stringResource(R.string.seals_buy_hint), color = Color.Gray, fontSize = 10.sp)
                }
                Button(
                    onClick = onBuySeals,
                    enabled = state.canBuySeals,
                    colors = ButtonDefaults.buttonColors(containerColor = SealTeal, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(BUY_SEALS_TAG)
                ) { Text(state.sealPrice.toBeautifulString(), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }

        // --- Bonos
        val active = state.bonuses.filter { it.value > 1.0001 }.map { (kind, value) -> kind.effectText(((value - 1.0) * 100).roundToInt()) }
        if (active.isNotEmpty()) {
            Text(active.joinToString(" · "), color = SealTeal, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }

        // --- Dominios
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Bestiary.domains.forEach { domain ->
                val count = Bestiary.beastsOf(domain.id).count { (state.levels[it.id] ?: 0) > 0 }
                val chosen = domain.id == domainId
                // Sin el alto mínimo táctil de 48 dp: los ocho Dominios caben en pocas filas
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (chosen) Color(domain.color).copy(alpha = 0.22f) else Color(0xFF151515),
                    border = BorderStroke(1.dp, Color(domain.color).copy(alpha = if (chosen) 0.9f else 0.3f)),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { domainId = domain.id }.testTag(DOMAIN_CHIP_TAG + domain.id)
                ) {
                    Text(
                        tr(domain.title) + " $count/${Bestiary.beastsOf(domain.id).size}",
                        color = if (chosen) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }
        DomainGrid(domainId, state.levels) { selected = it }
    }
    selected?.let { id ->
        Bestiary.beast(id)?.let { beast ->
            CreatureCard(
                beast = beast,
                level = state.levels[id] ?: 0,
                shards = state.shards,
                onPick = { onPickWithShards(id) },
                onDismiss = { selected = null }
            )
        }
    }
}

@Composable
private fun SummonButton(label: String, cost: Int, enabled: Boolean, tag: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = BeastRarity.MYTHIC.uiColor(), contentColor = Color.Black),
        modifier = modifier.testTag(tag)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            IconLabel(GameIcons.ElderSeal, "$cost", Color.Black, fontSize = 11.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DomainGrid(domainId: String, levels: Map<String, Int>, onSelect: (String) -> Unit) {
    val domain = Bestiary.domain(domainId) ?: return
    val beasts = Bestiary.beastsOf(domainId)
    val complete = beasts.all { (levels[it.id] ?: 0) > 0 }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF141414),
        border = BorderStroke(1.dp, Color(domain.color).copy(alpha = if (complete) 0.9f else 0.35f)),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(DOMAIN_GRID_TAG + domainId)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr(domain.title), color = Color(domain.color), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text(
                    if (complete) stringResource(R.string.bestiary_domain_complete) else stringResource(R.string.bestiary_domain_bonus, domain.kind.nameText()),
                    color = if (complete) Color(domain.color) else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(tr(domain.description), color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                maxItemsInEachRow = 5,
                modifier = Modifier.fillMaxWidth()
            ) {
                beasts.forEach { beast ->
                    BeastTile(beast, levels[beast.id] ?: 0, Modifier.weight(1f)) { onSelect(beast.id) }
                }
                // Huecos para que la última fila mantenga el tamaño de las casillas
                repeat((5 - beasts.size % 5) % 5) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BeastTile(beast: Beast, level: Int, modifier: Modifier, onClick: () -> Unit) {
    val known = level > 0
    val rarity = beast.rarity.uiColor()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (known) Color(0xFF1C1C22) else Color(0xFF101012),
        border = BorderStroke(1.dp, if (known) rarity else rarity.copy(alpha = 0.25f)),
        modifier = modifier.aspectRatio(1f).testTag(BEAST_TILE_TAG + beast.id)
    ) {
        Box {
            BeastPortrait(beast, known, Modifier.fillMaxSize().padding(3.dp))
            if (known) {
                Text(
                    "$level",
                    color = rarity,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 1.dp)
                )
            }
        }
    }
}

/**
 * La tarjeta de una criatura: su dibujo grande, nombre, rareza y Dominio, su historia, lo
 * que da ahora y por nivel, y su nivel. Si todavía no se tiene, se ve su silueta y se puede
 * elegir con Fragmentos.
 */
@Composable
fun CreatureCard(beast: Beast, level: Int, shards: Int, onPick: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) { CreatureCardContent(beast, level, shards, onPick, onDismiss) }
}

@Composable
fun CreatureCardContent(beast: Beast, level: Int, shards: Int, onPick: () -> Unit, onDismiss: () -> Unit) {
    val known = level > 0
    val domain = Bestiary.domainOf(beast)
    val rarity = beast.rarity.uiColor()
    val per = (beast.rarity.bonusPerLevel * 100).roundToInt()
    run {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF15151A),
            border = BorderStroke(2.dp, rarity),
            modifier = Modifier.fillMaxWidth().testTag(CREATURE_CARD_TAG)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(beast.rarity.label().uppercase(), color = rarity, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text(
                    if (known) tr(beast.name) else "???",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(tr(domain.title), color = Color(domain.color), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0C0C10))
                ) { BeastPortrait(beast, known, Modifier.fillMaxSize().padding(8.dp)) }
                Spacer(modifier = Modifier.height(10.dp))
                if (known) {
                    Text(
                        "“" + tr(beast.lore) + "”",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(stringResource(R.string.beast_level, level, Bestiary.MAX_LEVEL), color = rarity, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LinearProgressIndicator(
                        progress = { level.toFloat() / Bestiary.MAX_LEVEL },
                        color = rarity,
                        trackColor = Color(0xFF2A2A2A),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                    )
                    Text(domain.kind.effectText(per * level), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.beast_per_level, domain.kind.effectText(per)), color = Color.Gray, fontSize = 11.sp)
                    Text(
                        if (level >= Bestiary.MAX_LEVEL) stringResource(R.string.beast_maxed_shards, beast.rarity.shards)
                        else stringResource(R.string.beast_duplicates),
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(stringResource(R.string.beast_unknown), color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.beast_per_level, domain.kind.effectText(per)), color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    val cost = Bestiary.shardCost(beast)
                    Button(
                        onClick = onPick,
                        enabled = shards >= cost,
                        colors = ButtonDefaults.buttonColors(containerColor = rarity, contentColor = Color.Black),
                        modifier = Modifier.testTag(PICK_WITH_SHARDS_TAG)
                    ) {
                        IconLabel(GameIcons.Shard, stringResource(R.string.beast_pick, cost), Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = Color.Gray) }
            }
        }
    }
}

/**
 * Lo que salió al invocar: las criaturas aparecen una tras otra con su rareza, y una marca
 * de "Nueva", "+1 nivel" o los Fragmentos que dieron.
 */
@Composable
fun SummonResultsDialog(results: List<SummonResult>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) { SummonResultsContent(results, onDismiss) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummonResultsContent(results: List<SummonResult>, onDismiss: () -> Unit) {
    run {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF121218),
            border = BorderStroke(2.dp, results.maxOf { Bestiary.beast(it.beastId)?.rarity ?: BeastRarity.COMMON }.uiColor()),
            modifier = Modifier.fillMaxWidth().testTag(SUMMON_RESULTS_TAG)
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.summon_results_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                if (results.size == 1) {
                    RevealedBeast(results.first(), 0, 180.dp)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 5,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        results.forEachIndexed { i, result -> RevealedBeast(result, i, 52.dp, Modifier.weight(1f)) }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.testTag(SUMMON_RESULTS_CLOSE_TAG)) {
                    Text(stringResource(R.string.summon_results_ok), color = SealTeal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RevealedBeast(result: SummonResult, index: Int, size: Dp, modifier: Modifier = Modifier) {
    val beast = Bestiary.beast(result.beastId) ?: return
    val rarity = beast.rarity.uiColor()
    val appear = remember(result, index) { Animatable(0f) }
    LaunchedEffect(result, index) {
        kotlinx.coroutines.delay(index * 120L)
        appear.animateTo(1f, tween(320))
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.alpha(appear.value).scale(0.7f + 0.3f * appear.value)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0C0C10))
        ) {
            BeastPortrait(beast, true, Modifier.fillMaxSize().padding(3.dp))
        }
        if (size > 100.dp) {
            Text(tr(beast.name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
            Text(beast.rarity.label(), color = rarity, fontSize = 12.sp)
        }
        Text(
            when {
                result.isNew -> stringResource(R.string.summon_new)
                result.shards > 0 -> stringResource(R.string.summon_shards, result.shards)
                else -> stringResource(R.string.summon_level_up, result.level)
            },
            color = if (result.isNew) rarity else Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * El encuentro, flotando arriba de la pantalla: la silueta de algo que acecha, su rareza y
 * cuánto falta para que se vaya. Tocarlo deja Sellos Antiguos.
 */
@Composable
fun EncounterBanner(beastId: String, secondsLeft: Int, onCatch: () -> Unit, modifier: Modifier = Modifier) {
    val beast = Bestiary.beast(beastId) ?: return
    val rarity = beast.rarity.uiColor()
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xEE101418),
        border = BorderStroke(1.5.dp, rarity),
        shadowElevation = 8.dp,
        modifier = modifier.padding(horizontal = 24.dp).testTag(ENCOUNTER_TAG)
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            BeastPortrait(beast, known = false, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.encounter_unknown), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(beast.rarity.label() + " · " + stringResource(R.string.encounter_left, secondsLeft), color = rarity, fontSize = 11.sp)
            }
            Button(
                onClick = onCatch,
                colors = ButtonDefaults.buttonColors(containerColor = rarity, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag(ENCOUNTER_CATCH_TAG)
            ) {
                IconLabel(GameIcons.ElderSeal, "+${beast.rarity.encounterSeals}", Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

const val BESTIARY_PANEL_TAG = "bestiary_panel"
const val DOMAIN_CHIP_TAG = "beast_domain_chip_"
const val DOMAIN_GRID_TAG = "beast_domain_"
const val BEAST_TILE_TAG = "beast_tile_"
const val CREATURE_CARD_TAG = "creature_card"
const val PICK_WITH_SHARDS_TAG = "pick_with_shards"
const val SUMMON_ONE_TAG = "summon_one"
const val SUMMON_TEN_TAG = "summon_ten"
const val BUY_SEALS_TAG = "buy_seals"
const val SUMMON_RESULTS_TAG = "summon_results"
const val SUMMON_RESULTS_CLOSE_TAG = "summon_results_close"
const val ENCOUNTER_TAG = "encounter"
const val ENCOUNTER_CATCH_TAG = "encounter_catch"
