package com.EGomez.idleecosystem.ui.screens.main.components

import com.EGomez.idleecosystem.ui.components.GameIcons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.model.TidePhase
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.theme.AncientGold

/** Color de cada fase de la Marea. */
fun tideColor(tide: TidePhase): Color = when (tide) {
    TidePhase.HIGH -> Color(0xFF29B6F6)
    TidePhase.LOW -> Color(0xFF26A69A)
    TidePhase.DEAD -> Color(0xFF9E9E9E)
}

private fun mmss(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

/** La fase de la Marea, cuánto le queda y cuál viene. Al tocarla se explican las tres. */
@Composable
fun TideIndicator(
    tide: TidePhase,
    secondsLeft: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val next = TidePhase.entries[(tide.ordinal + 1) % TidePhase.entries.size]
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag(TIDE_INDICATOR_TAG)
    ) {
        Icon(GameIcons.Wave, contentDescription = null, tint = tideColor(tide), modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            stringResource(R.string.tide_bar, tr(tide.title), mmss(secondsLeft)),
            color = tideColor(tide),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (!compact) Text(stringResource(R.string.tide_next, tr(next.title)), color = Color.Gray, fontSize = 10.sp)
    }
}

/** Las tres fases de la Marea con sus efectos; la actual, resaltada. */
@Composable
fun TideInfoDialog(current: TidePhase, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = { Text(stringResource(R.string.tide_title), color = tideColor(current), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.tide_intro), color = Color.Gray, fontSize = 12.sp)
                TidePhase.entries.forEach { tide ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        (if (tide == current) "› " else "") + tr(tide.title),
                        color = tideColor(tide),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(tr(tide.description), color = Color.LightGray, fontSize = 12.sp)
                    PactEffectList(pactLines(tide.effects, emptyMap()), fontSize = 11)
                    if (tide.madnessGain != 1.0) Text(stringResource(R.string.tide_madness_gain, multiplier(tide.madnessGain)), color = Color(0xFFE57373), fontSize = 11.sp)
                    if (tide.madnessDecay != 1.0) Text(stringResource(R.string.tide_madness_decay, multiplier(tide.madnessDecay)), color = Color(0xFF81C784), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.madness_close), color = AncientGold) }
        },
        modifier = Modifier.testTag(TIDE_DIALOG_TAG)
    )
}

const val TIDE_INDICATOR_TAG = "tide_indicator"
const val TIDE_DIALOG_TAG = "tide_dialog"
