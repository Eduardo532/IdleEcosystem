package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
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
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.MenuLayer
import com.EGomez.idleecosystem.domain.logic.MenuTab
import com.EGomez.idleecosystem.domain.logic.NextUnlock
import com.EGomez.idleecosystem.domain.logic.SystemReveal
import com.EGomez.idleecosystem.domain.logic.Transcendence
import com.EGomez.idleecosystem.domain.model.Mutation
import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import java.math.BigDecimal

/** Color de cada capa: el de su moneda. */
val SanityRose = Color(0xFFCF6679)

fun MenuLayer.color(): Color = when (this) {
    MenuLayer.LIFE -> ArcanePurple
    MenuLayer.ASCENSION -> SanityRose
    MenuLayer.TRANSCENDENCE -> EtherColor
}

@Composable
fun MenuLayer.title(): String = stringResource(
    when (this) {
        MenuLayer.LIFE -> R.string.layer_life
        MenuLayer.ASCENSION -> R.string.layer_ascension
        MenuLayer.TRANSCENDENCE -> R.string.layer_transcendence
    }
)

@Composable
fun MenuTab.title(): String = stringResource(
    when (this) {
        MenuTab.RESEARCH -> R.string.tab_research
        MenuTab.GRIMOIRE -> R.string.tab_grimoire
        MenuTab.RITUALS -> R.string.tab_rituals
        MenuTab.SANITY -> R.string.tab_sanity
        MenuTab.PANTHEON -> R.string.tab_pantheon
        MenuTab.TRIALS -> R.string.tab_trials
        MenuTab.RUNES -> R.string.tab_runes
        MenuTab.ETHER -> R.string.tab_ether
        MenuTab.MUTATIONS -> R.string.tab_mutations
        MenuTab.STARS -> R.string.tab_stars
        MenuTab.BESTIARY -> R.string.tab_bestiary
        MenuTab.LAB -> R.string.tab_lab
        MenuTab.DREAM -> R.string.tab_dream
    }
)

/** Lo próximo por descubrir: una sola línea con un candado, al final de Investigación. */
@Composable
fun NextUnlockHint(next: NextUnlock) {
    val text = when (next) {
        NextUnlock.GRIMOIRE -> stringResource(R.string.next_unlock_grimoire, SystemReveal.GRIMOIRE_AT.toBeautifulString())
        NextUnlock.RITUALS -> stringResource(R.string.next_unlock_rituals)
        NextUnlock.ASCENSION -> stringResource(R.string.next_unlock_ascension, ASCENSION_REQUIREMENT.toBeautifulString())
        NextUnlock.PANTHEON -> stringResource(R.string.next_unlock_pantheon)
        NextUnlock.TRIALS -> stringResource(R.string.next_unlock_trials, Trial.REVEAL_AT)
        NextUnlock.TRANSCENDENCE -> stringResource(R.string.next_unlock_transcendence, Transcendence.REQUIRED_ASCENSIONS)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .testTag(NEXT_UNLOCK_TAG)
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = Color.Gray, fontSize = 12.sp)
    }
}

/** Conocimiento en una vida que pide la primera Ascensión. */
val ASCENSION_REQUIREMENT: BigDecimal = BigDecimal("1000000")

/**
 * La pestaña Cordura (capa Ascensión): la Cordura, lo que daría Ascender y los accesos al
 * Mapa de Locura, la Atención y la automatización, cada uno cuando ya existe.
 */
@Composable
fun SanityHub(
    sanityFormatted: String,
    canPrestige: Boolean,
    potentialPrestigeFormatted: String,
    ascensions: Int,
    attentionPoints: Int,
    attentionUsed: Int,
    automationUnlocked: Boolean,
    onOpenMap: () -> Unit,
    onOpenAttention: () -> Unit,
    onOpenAutomation: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1C1218),
            border = BorderStroke(1.dp, SanityRose.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.residual_sanity_title), color = SanityRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(sanityFormatted, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (canPrestige) stringResource(R.string.hub_ascend_now, potentialPrestigeFormatted)
                    else stringResource(R.string.hub_ascend_locked, ASCENSION_REQUIREMENT.toBeautifulString()),
                    color = if (canPrestige) Color(0xFF81C784) else Color.Gray,
                    fontSize = 12.sp
                )
                if (ascensions > 0) Text(stringResource(R.string.hub_ascensions, ascensions), color = Color.Gray, fontSize = 11.sp)
            }
        }
        HubCard(
            title = stringResource(R.string.madness_map),
            subtitle = stringResource(R.string.hub_map_desc),
            accent = AncientGold,
            tag = HUB_MAP_TAG,
            onClick = onOpenMap
        )
        if (attentionPoints > 0) {
            HubCard(
                title = "◎ " + stringResource(R.string.hub_attention),
                subtitle = stringResource(R.string.hub_attention_desc, attentionUsed, attentionPoints),
                accent = if (attentionUsed < attentionPoints) AncientGold else ArcanePurple,
                tag = ATTENTION_BAR_TAG,
                onClick = onOpenAttention
            )
        }
        if (automationUnlocked) {
            HubCard(
                title = stringResource(R.string.automation_title),
                subtitle = stringResource(R.string.hub_automation_desc),
                accent = Color(0xFF90CAF9),
                tag = HUB_AUTOMATION_TAG,
                onClick = onOpenAutomation
            )
        }
    }
}

@Composable
private fun HubCard(title: String, subtitle: String, accent: Color, tag: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1A1A1A),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(tag)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.LightGray, fontSize = 11.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

/**
 * La pestaña Mutaciones (capa Trascendencia): las 20 mutaciones, con las descubiertas a
 * la vista y las demás ocultas, y la activa marcada.
 */
@Composable
fun MutationCatalog(seen: Set<String>, active: Mutation?) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(MUTATION_CATALOG_TAG)) {
        Text(
            stringResource(R.string.mutation_catalog_title, seen.size, Mutation.entries.size),
            color = MutationGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(stringResource(R.string.mutation_catalog_hint), color = Color.Gray, fontSize = 11.sp)
        Mutation.entries.forEach { mutation ->
            val known = mutation.id in seen || mutation == active
            val isActive = mutation == active
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isActive) Color(0xFF1B2412) else Color(0xFF151515),
                border = BorderStroke(1.dp, if (isActive) MutationGreen else Color(0xFF2E2E2E)),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag(MUTATION_ROW_TAG + mutation.id)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (known) tr(mutation.title) else "???",
                            color = if (known) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            if (known) tr(mutation.description) else stringResource(R.string.mutation_unknown),
                            color = if (known) MutationGreen else Color.DarkGray,
                            fontSize = 11.sp
                        )
                    }
                    if (isActive) {
                        Text(stringResource(R.string.mutation_active), color = MutationGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

const val NEXT_UNLOCK_TAG = "next_unlock"
const val HUB_MAP_TAG = "hub_map"
const val HUB_AUTOMATION_TAG = "hub_automation"
const val MUTATION_CATALOG_TAG = "mutation_catalog"
const val MUTATION_ROW_TAG = "mutation_row_"
