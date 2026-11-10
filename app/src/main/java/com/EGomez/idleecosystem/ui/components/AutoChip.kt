package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.R
import com.EGomez.idleecosystem.ui.theme.AncientGold

/** Interruptor de una automatización ganada con los hitos de Ascensión. */
@Composable
fun AutoChip(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(R.string.auto_chip), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AncientGold,
            selectedLabelColor = Color.Black,
            containerColor = Color(0xFF333333),
            labelColor = Color.Gray
        ),
        border = null,
        modifier = modifier.height(28.dp)
    )
}
