package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.EGomez.idleecosystem.domain.logic.BulkBuy

/**
 * Elegir cuántos niveles se compran de una vez (v0.20.5): x1, x10, x100 o MAX. Se usa en el
 * Mapa de Locura y en las Estrellas.
 */
@Composable
fun BulkAmountRow(selected: Int, accent: Color, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.testTag(BULK_ROW_TAG)
    ) {
        Text(stringResource(R.string.bulk_buy_label), color = Color.Gray, fontSize = 11.sp)
        BulkBuy.AMOUNTS.forEach { amount ->
            val chosen = amount == selected
            Surface(
                onClick = { onSelect(amount) },
                shape = RoundedCornerShape(12.dp),
                color = if (chosen) accent.copy(alpha = 0.2f) else Color(0xFF202020),
                border = BorderStroke(1.dp, if (chosen) accent else Color(0xFF333333)),
                modifier = Modifier.height(26.dp).testTag(BULK_AMOUNT_TAG + BulkBuy.label(amount))
            ) {
                Box(modifier = Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                    Text(
                        BulkBuy.label(amount),
                        color = if (chosen) accent else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

const val BULK_ROW_TAG = "bulk_row"
const val BULK_AMOUNT_TAG = "bulk_"
