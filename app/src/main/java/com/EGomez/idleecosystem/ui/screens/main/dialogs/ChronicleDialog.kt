package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.logic.Chronicle
import com.EGomez.idleecosystem.domain.logic.ChronicleAct
import com.EGomez.idleecosystem.ui.i18n.tr

/** El color del papel viejo de la Crónica. */
val ChroniclePaper = Color(0xFFE8D9B0)

/**
 * La Crónica (v0.26.0): la partida contada como una historia en cinco actos. Cada acto
 * aparece con su primer capítulo; los capítulos sin escribir se ven como renglones en
 * blanco. Arriba, el camino hacia el final.
 */
@Composable
fun ChronicleDialog(written: Set<String>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f).testTag(CHRONICLE_TAG),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1B1710),
            border = BorderStroke(1.dp, ChroniclePaper.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.chronicle_title), color = ChroniclePaper, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                val total = com.EGomez.idleecosystem.domain.logic.ChronicleChapter.entries.size
                val count = com.EGomez.idleecosystem.domain.logic.ChronicleChapter.entries.count { it.id in written }
                Text(stringResource(R.string.chronicle_progress, count, total), color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                LinearProgressIndicator(
                    progress = { Chronicle.progress(written) },
                    color = ChroniclePaper,
                    trackColor = Color(0xFF332C1C),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(6.dp).testTag(CHRONICLE_PROGRESS_TAG)
                )
                ChronicleAct.entries.forEach { act -> ActSection(act, written) }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.close), color = ChroniclePaper)
                }
            }
        }
    }
}

@Composable
private fun ActSection(act: ChronicleAct, written: Set<String>) {
    val started = Chronicle.isActStarted(act, written)
    val complete = Chronicle.isActComplete(act, written)
    var open by remember(act) { mutableStateOf(started && !complete) }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF15120C),
        border = BorderStroke(1.dp, if (started) ChroniclePaper.copy(alpha = if (complete) 0.8f else 0.4f) else Color(0xFF2A2518)),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag(CHRONICLE_ACT_TAG + act.name)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(enabled = started) { open = !open },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.chronicle_act, act.numeral) + " · " +
                        if (started) tr(act.title) else stringResource(R.string.chronicle_unknown),
                    color = if (started) ChroniclePaper else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (complete) Text("✓", color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
            }
            if (open) {
                Chronicle.chaptersOf(act).forEach { chapter ->
                    if (chapter.id in written) {
                        Text(tr(chapter.title), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                        Text(tr(chapter.text), color = ChroniclePaper, fontSize = 13.sp, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 2.dp))
                    } else {
                        Text(stringResource(R.string.chronicle_blank), color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
        }
    }
}

const val CHRONICLE_TAG = "chronicle"
const val CHRONICLE_PROGRESS_TAG = "chronicle_progress"
const val CHRONICLE_ACT_TAG = "chronicle_act_"
const val CHRONICLE_BUTTON_TAG = "chronicle_button"
