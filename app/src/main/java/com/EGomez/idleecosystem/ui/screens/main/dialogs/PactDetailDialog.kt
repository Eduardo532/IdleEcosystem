package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.ui.components.PactSigil
import com.EGomez.idleecosystem.ui.components.sigilColor
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.screens.main.components.PactEffectList
import com.EGomez.idleecosystem.ui.screens.main.components.pactLines
import com.EGomez.idleecosystem.ui.screens.main.components.tierTitle

/** La ficha de un dios: sigilo, rango, mito y lo que da y quita su pacto. */
@Composable
fun PactDetailDialog(pact: Pact, active: Boolean, buildingNames: Map<String, String>, onDismiss: () -> Unit) {
    val color = pact.sigilColor()
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF141414),
            border = BorderStroke(1.dp, color.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth().testTag(PACT_DETAIL_TAG)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(Icons.Default.Close, stringResource(R.string.close), tint = Color.Gray)
                    }
                }
                PactSigil(pact, Modifier.size(140.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(tr(pact.godName), color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(tr(pact.epithet), color = Color.LightGray, fontSize = 14.sp, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
                Text(stringResource(tierTitle(pact.tier)), color = Color.Gray, fontSize = 11.sp)
                if (active) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stringResource(R.string.pact_active_life), color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                Text(tr(pact.lore), color = Color(0xFFCCCCCC), fontSize = 13.sp, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.pact_effects_title),
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) { PactEffectList(pactLines(pact.effects, buildingNames), fontSize = 13) }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (pact.unlockAscensions == 0) stringResource(R.string.pact_always_available)
                    else stringResource(R.string.pact_unlocks_at, pact.unlockAscensions),
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

const val PACT_DETAIL_TAG = "pact_detail"
