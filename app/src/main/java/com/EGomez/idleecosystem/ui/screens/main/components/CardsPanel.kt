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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.Artifact
import com.EGomez.idleecosystem.domain.logic.Artifacts
import com.EGomez.idleecosystem.domain.logic.YellowCard
import com.EGomez.idleecosystem.domain.logic.YellowCards
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import com.EGomez.idleecosystem.ui.i18n.tr

/** El amarillo del Rey. */
val KingYellow = Color(0xFFF2D24B)

/** Lo que la pestaña Cartas necesita del motor (v0.24.0). */
data class CardsUi(
    val active: String? = null,
    val next: String? = null,
    /** Carta -> veces completada. */
    val completed: Map<String, Int> = emptyMap(),
    val fragments: Int = 0,
    /** Trascendencias en este Despertar. */
    val transcendences: Int = 0,
    /** Artefacto -> nivel (v0.24.1). */
    val artifacts: Map<String, Int> = emptyMap()
)

/** Una Carta recién completada, para el aviso con su capítulo. */
data class CardChapterNotice(val cardId: String, val firstTime: Boolean, val fragments: Int)

/**
 * La pestaña Cartas (capa Despertar): la Carta en juego y cuánto falta, la elegida para el
 * próximo Despertar, y el mazo con sus Juramentos, su meta, su premio y su capítulo.
 */
@Composable
fun CardsPanel(ui: CardsUi, onChoose: (String?) -> Unit, onAbandon: () -> Unit, onBuyArtifact: (String) -> Unit = {}) {
    val done = YellowCard.entries.filter { (ui.completed[it.id] ?: 0) > 0 }.toSet()
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(CARDS_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1A0C),
            border = BorderStroke(1.dp, KingYellow.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.cards_title), color = KingYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.cards_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.cards_fragments), color = KingYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${ui.fragments}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag(CARDS_FRAGMENTS_TAG))
                }
            }
        }
        // La Carta en juego
        val active = YellowCard.fromId(ui.active)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF16150B),
            border = BorderStroke(1.dp, if (active != null) KingYellow else Color(0xFF2E2B18)),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(CARD_ACTIVE_TAG)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (active == null) {
                    Text(stringResource(R.string.cards_none_active), color = Color.Gray, fontSize = 12.sp)
                } else {
                    Text(stringResource(R.string.cards_active, active.numeral, tr(active.title)), color = KingYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    val reached = ui.transcendences >= active.goal
                    Text(
                        stringResource(R.string.cards_progress, ui.transcendences, active.goal),
                        color = if (reached) Color(0xFF81C784) else Color.White,
                        fontSize = 12.sp
                    )
                    LinearProgressIndicator(
                        progress = { (ui.transcendences.toFloat() / active.goal).coerceIn(0f, 1f) },
                        color = KingYellow,
                        trackColor = Color(0xFF332F14),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(6.dp)
                    )
                    Text(
                        stringResource(if (reached) R.string.cards_reached else R.string.cards_awaken_to_complete),
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    TextButton(onClick = onAbandon, modifier = Modifier.testTag(CARD_ABANDON_TAG)) {
                        Text(stringResource(R.string.cards_abandon), color = Color(0xFFEF9A9A), fontSize = 11.sp)
                    }
                }
            }
        }
        Text(stringResource(R.string.cards_deck), color = KingYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        YellowCard.entries.forEach { card -> CardRow(card, ui, done, onChoose) }
        // Los Artefactos (v0.24.1)
        Text(stringResource(R.string.artifacts_title), color = KingYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp))
        Text(stringResource(R.string.artifacts_hint), color = Color.Gray, fontSize = 11.sp)
        Artifact.entries.forEach { artifact -> ArtifactRow(artifact, ui.artifacts[artifact.id] ?: 0, ui.fragments, onBuyArtifact) }
    }
}

@Composable
private fun ArtifactRow(artifact: Artifact, level: Int, fragments: Int, onBuy: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF16150B),
        border = BorderStroke(1.dp, if (level > 0) KingYellow.copy(alpha = 0.6f) else Color(0xFF2E2B18)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(ARTIFACT_TAG + artifact.id)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                IconLabel(
                    GameIcons.ElderSeal,
                    tr(artifact.title) + " · " + stringResource(R.string.lab_level, level, artifact.maxLevel),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(tr(artifact.description), color = Color.LightGray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (artifact.isMaxed(level)) {
                Text(stringResource(R.string.eternal_maxed), color = KingYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = { onBuy(artifact.id) },
                    enabled = Artifacts.canBuy(artifact, level, fragments),
                    colors = ButtonDefaults.buttonColors(containerColor = KingYellow, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag(ARTIFACT_BUY_TAG + artifact.id)
                ) {
                    Text(stringResource(R.string.artifacts_cost, artifact.cost(level)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CardRow(card: YellowCard, ui: CardsUi, done: Set<YellowCard>, onChoose: (String?) -> Unit) {
    val times = ui.completed[card.id] ?: 0
    val available = YellowCards.isAvailable(card, done)
    val chosen = ui.next == card.id
    var reading by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF16150B),
        border = BorderStroke(if (chosen) 2.dp else 1.dp, if (chosen || times > 0) KingYellow.copy(alpha = if (chosen) 1f else 0.6f) else Color(0xFF2E2B18)),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(CARD_TAG + card.id)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(card.numeral, color = KingYellow, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(40.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(tr(card.title), color = if (available) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (times > 0) Text(stringResource(R.string.cards_done_times, times), color = Color(0xFF81C784), fontSize = 11.sp)
                }
            }
            if (!available) {
                Text(stringResource(R.string.cards_king_locked), color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            } else {
                card.oaths.forEach { oath ->
                    Text("• " + tr(oath.title) + ": " + tr(oath.rule), color = Color(0xFFE0B0B0), fontSize = 11.sp)
                }
                Text(stringResource(R.string.cards_goal, card.goal), color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                Text(
                    stringResource(R.string.cards_reward, tr(card.bonus.description), card.fragments, card.fragments / 2),
                    color = if (times > 0) Color(0xFF81C784) else KingYellow,
                    fontSize = 11.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    if (chosen) {
                        OutlinedButton(
                            onClick = { onChoose(null) },
                            border = BorderStroke(1.dp, KingYellow.copy(alpha = 0.6f)),
                            modifier = Modifier.testTag(CARD_CHOOSE_TAG + card.id)
                        ) {
                            Text(stringResource(R.string.cards_chosen), color = KingYellow, fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = { onChoose(card.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = KingYellow, contentColor = Color.Black),
                            modifier = Modifier.testTag(CARD_CHOOSE_TAG + card.id)
                        ) {
                            Text(stringResource(R.string.cards_choose), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (times > 0) {
                        TextButton(onClick = { reading = !reading }, modifier = Modifier.testTag(CARD_READ_TAG + card.id)) {
                            Text(stringResource(R.string.cards_read), color = KingYellow, fontSize = 11.sp)
                        }
                    }
                }
                if (reading && times > 0) {
                    Text(tr(card.chapter), color = Color(0xFFF5EBC0), fontSize = 12.sp, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

/** El aviso de una Carta completada: su capítulo y lo que dio. */
@Composable
fun CardChapterDialog(notice: CardChapterNotice, onDismiss: () -> Unit) {
    val card = YellowCard.fromId(notice.cardId) ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(card.numeral + " · " + tr(card.title), color = KingYellow, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (notice.firstTime) {
                    Text(tr(card.chapter), color = Color(0xFFF5EBC0), fontSize = 13.sp, fontStyle = FontStyle.Italic)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.cards_bonus_gained, tr(card.bonus.description)), color = Color(0xFF81C784), fontSize = 12.sp)
                }
                Text(stringResource(R.string.cards_fragments_gained, notice.fragments), color = KingYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag(CARD_CHAPTER_OK_TAG)) {
                Text(stringResource(R.string.cards_chapter_ok), color = KingYellow)
            }
        },
        containerColor = Color(0xFF1C1A0C)
    )
}

const val CARDS_PANEL_TAG = "cards_panel"
const val CARDS_FRAGMENTS_TAG = "cards_fragments"
const val CARD_ACTIVE_TAG = "card_active"
const val CARD_ABANDON_TAG = "card_abandon"
const val CARD_TAG = "card_"
const val CARD_CHOOSE_TAG = "card_choose_"
const val CARD_READ_TAG = "card_read_"
const val CARD_CHAPTER_OK_TAG = "card_chapter_ok"
const val ARTIFACT_TAG = "artifact_"
const val ARTIFACT_BUY_TAG = "artifact_buy_"
