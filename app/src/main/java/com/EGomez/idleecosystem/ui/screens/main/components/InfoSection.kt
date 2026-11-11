package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import java.math.BigDecimal

@Composable
fun InfoSection(
    knowledgeFormatted: String,
    knowledgePerSecond: String,
    globalMultiplier: BigDecimal,
    activeBuffMultiplier: Double,
    buffTimeRemaining: Int,
    clickMultiplier: String,
    // CAMBIO: Añadido parámetro `buttonRadius`
    onManualClick: (clickOffset: Offset, buttonCenter: Offset, buttonRadius: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. CABECERA ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.arcane_knowledge),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = knowledgeFormatted,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = ArcanePurple
            )
            Text(
                text = stringResource(R.string.per_second, knowledgePerSecond),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. MULTIPLICADORES ---
        if (globalMultiplier > BigDecimal.ONE) {
            Surface(
                color = AncientGold.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AncientGold),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.global_multiplier, globalMultiplier.toBeautifulString()),
                    color = AncientGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (activeBuffMultiplier > 1.0) {
            Surface(
                color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.active_bonus, activeBuffMultiplier.toString(), buffTimeRemaining),
                        color = Color(0xFF81C784),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            if (globalMultiplier <= BigDecimal.ONE) Spacer(modifier = Modifier.height(32.dp))
        }

        // --- 3. BOTÓN INVESTIGAR ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .weight(1f)
        ) {
            var buttonPositionInRoot by remember { mutableStateOf(Offset.Zero) }
            // El gesto vive mientras dure la pantalla: siempre llama a la versión actual
            val currentOnManualClick by rememberUpdatedState(onManualClick)
            var buttonSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxHeight(0.85f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(ArcanePurple.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
                    .onGloballyPositioned { coordinates ->
                        buttonPositionInRoot = coordinates.positionInRoot()
                        buttonSize = coordinates.size
                    }
                    // Tocar hace un clic; mantener el dedo sigue haciendo clics
                    .clickAndHold { tapOffset ->
                        // Cálculos de geometría
                        val globalClickX = buttonPositionInRoot.x + tapOffset.x
                        val globalClickY = buttonPositionInRoot.y + tapOffset.y

                        val centerX = buttonPositionInRoot.x + (buttonSize.width / 2f)
                        val centerY = buttonPositionInRoot.y + (buttonSize.height / 2f)

                        val currentRadius = buttonSize.width / 2f // Radio actual en pixeles

                        // Enviamos: Dónde tocó, Dónde está el centro, Qué tan grande es
                        currentOnManualClick(
                            Offset(globalClickX, globalClickY),
                            Offset(centerX, centerY),
                            currentRadius
                        )
                    }
                    .testTag(RESEARCH_BUTTON_TAG)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.85f)
                        .clip(CircleShape)
                        .background(ArcanePurple.copy(alpha = 0.2f))
                )

                BoxWithConstraints(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val size = maxWidth
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.research_button),
                            color = ArcanePurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = (size.value * 0.13f).sp
                        )
                        Text(
                            text = "+$clickMultiplier",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = (size.value * 0.11f).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

const val RESEARCH_BUTTON_TAG = "research_button"
const val COMPACT_RESEARCH_BUTTON_TAG = "research_button_compact"

/**
 * INVESTIGAR en pequeño (v0.18.2), para la cabecera compacta con el menú abierto:
 * una píldora que hace clic al tocarla y sigue mientras se mantiene pulsada.
 */
@Composable
fun CompactResearchButton(
    clickMultiplier: String,
    onManualClick: (clickOffset: Offset, buttonCenter: Offset, buttonRadius: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val currentOnManualClick by rememberUpdatedState(onManualClick)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(ArcanePurple.copy(alpha = 0.25f))
            .onGloballyPositioned {
                position = it.positionInRoot()
                size = it.size
            }
            .clickAndHold { tap ->
                currentOnManualClick(
                    Offset(position.x + tap.x, position.y + tap.y),
                    Offset(position.x + size.width / 2f, position.y + size.height / 2f),
                    size.height / 2f
                )
            }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag(COMPACT_RESEARCH_BUTTON_TAG)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.research_button), color = ArcanePurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("+$clickMultiplier", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
        }
    }
}

/** Clics por segundo al mantener el dedo: algo menos que el autoclic de las runas (10/s). */
const val HOLD_CLICKS_PER_SECOND = 8
private const val HOLD_INTERVAL_MS = 1_000L / HOLD_CLICKS_PER_SECOND
/** Lo que hay que mantener el dedo antes de que empiecen los clics automáticos. */
private const val HOLD_DELAY_MS = 300L

/**
 * Un toque hace un clic al apoyar el dedo. Si el dedo sigue apoyado pasado
 * [HOLD_DELAY_MS], se repite a [HOLD_CLICKS_PER_SECOND] clics por segundo en la
 * posición actual del dedo, hasta que se levanta. El clic frenético cansa y en un
 * teléfono es incómodo: así la fase activa no castiga la mano.
 */
internal fun Modifier.clickAndHold(onClick: (Offset) -> Unit): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown()
        var position = down.position
        onClick(position)

        // Si se suelta antes de la espera, fue un toque normal
        val endedEarly = withTimeoutOrNull(HOLD_DELAY_MS) {
            waitForUpOrCancellation()
            true
        }
        if (endedEarly != null) return@awaitEachGesture

        while (true) {
            val released = withTimeoutOrNull(HOLD_INTERVAL_MS) {
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                    if (change == null || !change.pressed) break
                    position = change.position
                }
                true
            }
            if (released != null) break
            onClick(position)
        }
    }
}
