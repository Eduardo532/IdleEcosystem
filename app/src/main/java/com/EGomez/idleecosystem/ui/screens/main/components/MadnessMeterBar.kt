package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.MadnessMeter
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.theme.AncientGold

private val Calm = Color(0xFF7E57C2)
private val Danger = Color(0xFFD50000)

/**
 * El Medidor de Locura (v0.17.3), bajo los potenciadores: cuánta Locura hay y cuánto
 * suma a la producción. Pasa del violeta al rojo; durante el colapso muestra el castigo.
 * Al tocarlo se explica cómo funciona.
 */
@Composable
fun MadnessMeterBar(
    madness: Double,
    collapseSeconds: Int,
    possessedBuilding: String?,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val fraction = (madness / MadnessMeter.MAX).toFloat().coerceIn(0f, 1f)
    val color = if (collapseSeconds > 0) Danger else lerp(Calm, Danger, fraction)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag(MADNESS_METER_TAG)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val text = if (compact) {
                if (collapseSeconds > 0) "☠ ${collapseSeconds}s" else "☠ ${madness.toInt()}%"
            } else if (collapseSeconds > 0) {
                stringResource(R.string.madness_collapse_bar, collapseSeconds)
            } else {
                stringResource(R.string.madness_bar, madness.toInt(), multiplier(1.0 + madness * MadnessMeter.BONUS_PER_POINT))
            }
            Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (!compact) possessedBuilding?.let {
                Text(
                    stringResource(R.string.madness_possessed_short, tr(it)),
                    color = Danger,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(Calm, color)))
            )
        }
    }
}

/** Cómo funciona la Locura, con el valor actual. */
@Composable
fun MadnessInfoDialog(madness: Double, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text(stringResource(R.string.madness_title), color = Danger, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    stringResource(R.string.madness_current, madness.toInt(), multiplier(1.0 + madness * MadnessMeter.BONUS_PER_POINT)),
                    color = AncientGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    R.string.madness_info_rise,
                    R.string.madness_info_fall,
                    R.string.madness_info_omens,
                    R.string.madness_info_collapse
                ).forEach { res ->
                    Text(stringResource(res), color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.madness_close), color = AncientGold) }
        },
        modifier = Modifier.testTag(MADNESS_DIALOG_TAG)
    )
}

const val MADNESS_METER_TAG = "madness_meter"
const val MADNESS_DIALOG_TAG = "madness_dialog"
