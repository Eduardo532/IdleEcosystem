package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import com.EGomez.idleecosystem.domain.logic.Lethargy
import com.EGomez.idleecosystem.ui.components.GameIcons
import com.EGomez.idleecosystem.ui.components.IconLabel
import java.util.Locale

/** Color del Letargo: verde de musgo. */
val LethargyGreen = Color(0xFF9CCC65)

/** Lo que la pestaña Letargo necesita del motor (v0.20.2). */
data class LethargyUi(
    val active: Boolean = false,
    val seconds: Long = 0
)

/** "1 h 05 min" o "12 min". */
fun lethargyTime(seconds: Long): String {
    val minutes = seconds / 60
    return if (minutes >= 60) "%d h %02d min".format(Locale.US, minutes / 60, minutes % 60) else "$minutes min"
}

/** "x2.58". */
private fun times(value: Double): String = "x" + "%.2f".format(Locale.US, value)

/**
 * La pestaña Letargo (capa Trascendencia): caer en Letargo o salir, el Sopor de la vida y
 * lo que dará al Ascender.
 */
@Composable
fun LethargyPanel(ui: LethargyUi, onSet: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag(LETHARGY_PANEL_TAG)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF141A12),
            border = BorderStroke(1.dp, LethargyGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.lethargy_title), color = LethargyGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.lethargy_hint), color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = ui.active,
                    onCheckedChange = onSet,
                    colors = SwitchDefaults.colors(checkedTrackColor = LethargyGreen),
                    modifier = Modifier.testTag(LETHARGY_SWITCH_TAG)
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF151913),
            border = BorderStroke(1.dp, if (ui.active) LethargyGreen else Color(0xFF283024)),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(LETHARGY_STATUS_TAG)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (ui.active) {
                    IconLabel(
                        GameIcons.Hourglass,
                        stringResource(R.string.lethargy_active, lethargyTime(ui.seconds)),
                        color = LethargyGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.lethargy_sanity, times(Lethargy.sanityMultiplier(ui.seconds))),
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.testTag(LETHARGY_SANITY_TAG)
                    )
                    Text(
                        stringResource(R.string.lethargy_extra, Lethargy.extraAscensions(ui.seconds), Lethargy.MAX_EXTRA),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    val next = Lethargy.secondsToNextExtra(ui.seconds)
                    if (next > 0) {
                        Text(stringResource(R.string.lethargy_next, lethargyTime(next)), color = Color.LightGray, fontSize = 11.sp)
                    }
                    Text(stringResource(R.string.lethargy_warning), color = Color(0xFFEF9A9A), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                } else {
                    Text(stringResource(R.string.lethargy_off), color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
        Text(stringResource(R.string.lethargy_rules), color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

/** Aviso en Investigación mientras dura el Letargo. */
@Composable
fun LethargyBanner(seconds: Long) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF141A12),
        border = BorderStroke(1.dp, LethargyGreen.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).testTag(LETHARGY_BANNER_TAG)
    ) {
        IconLabel(
            GameIcons.Hourglass,
            stringResource(R.string.lethargy_banner, lethargyTime(seconds), times(Lethargy.sanityMultiplier(seconds))),
            color = LethargyGreen,
            fontSize = 12.sp,
            modifier = Modifier.padding(10.dp)
        )
    }
}

const val LETHARGY_PANEL_TAG = "lethargy_panel"
const val LETHARGY_SWITCH_TAG = "lethargy_switch"
const val LETHARGY_STATUS_TAG = "lethargy_status"
const val LETHARGY_SANITY_TAG = "lethargy_sanity"
const val LETHARGY_BANNER_TAG = "lethargy_banner"
