package com.EGomez.idleecosystem.ui.components

import com.EGomez.idleecosystem.ui.i18n.tr
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.domain.logic.RuneState
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.RuneVariant
import kotlin.math.*
import kotlin.random.Random

@Composable
fun RuneOverlay(
    activeRune: RuneVariant?,
    runeState: RuneState,
    onSuccess: () -> Unit,
    onFail: () -> Unit,
    modifier: Modifier = Modifier,
    /** Nivel de maestría de la runa: alarga la duración que se muestra. */
    masteryLevel: Int = 0
) {
    if (activeRune == null || runeState == RuneState.HIDDEN) return

    val isInteractive = runeState == RuneState.ACTIVE

    val transition = updateTransition(targetState = runeState, label = "RuneTransition")
    val alpha by transition.animateFloat(label = "Alpha") { state ->
        when (state) {
            RuneState.HIDDEN -> 0f
            RuneState.APPEARING, RuneState.ACTIVE -> 1f
            RuneState.SUCCESS -> 0f
            RuneState.FAILED -> 0f
        }
    }

    val scale by transition.animateFloat(label = "Scale") { state ->
        when (state) {
            RuneState.HIDDEN -> 0.2f
            RuneState.APPEARING -> 1.1f
            RuneState.ACTIVE -> 1f
            RuneState.SUCCESS -> 1.8f
            RuneState.FAILED -> 0.8f
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Pulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ), label = "Rotation"
    )

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val runeSize = 280.dp
    val runeSizePx = with(density) { runeSize.toPx() }
    val topSafeAreaPx = with(density) { 100.dp.toPx() }

    val positionOffset = remember(activeRune) {
        val maxX = (screenWidthPx - runeSizePx).toInt().coerceAtLeast(0)
        val maxY = (screenHeightPx - runeSizePx).toInt().coerceAtLeast(topSafeAreaPx.toInt())
        val randomX = Random.nextInt(0, maxX + 1)
        val randomY = Random.nextInt(topSafeAreaPx.toInt(), maxY + 1)
        IntOffset(randomX, randomY)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset { positionOffset }
                .size(runeSize)
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                }
                .background(
                    brush = Brush.radialGradient(colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)),
                    shape = CircleShape
                )
                .then(if (isInteractive) Modifier.background(Color.Transparent) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            // --- PLACA DE LA RUNA (v0.19.0) ---
            // Antes: nombre y descripción en neón del color de la runa, ilegibles con las
            // runas oscuras. Ahora una placa oscura con el nivel, el nombre y el efecto real.
            RunePlate(
                rune = activeRune,
                masteryLevel = masteryLevel,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-78).dp)
                    .widthIn(max = runeSize + 40.dp)
            )

            // --- GRÁFICOS ---
            when (activeRune.tier) {
                RuneTier.TIER_1_SWIPE -> SwipeRune(activeRune, isInteractive, onSuccess)
                RuneTier.TIER_2_HOLD -> HoldRune(activeRune, isInteractive, pulse, rotation, onSuccess)
                RuneTier.TIER_3_TRACE -> TraceRune(activeRune, isInteractive, pulse, onSuccess)
            }
        }
    }
}

/**
 * La placa de una runa: su nivel y gesto, su nombre y lo que da, más una barra con el
 * tiempo que queda para atraparla. Fondo oscuro y borde del color de la runa.
 */
@Composable
fun RunePlate(rune: RuneVariant, masteryLevel: Int, modifier: Modifier = Modifier) {
    val accent = readableRuneColor(rune.color)
    val lifetimeMs = ((15 + rune.tier.ordinal * 5) * 1000)
    val remaining = remember(rune) { Animatable(1f) }
    LaunchedEffect(rune) { remaining.animateTo(0f, tween(lifetimeMs, easing = LinearEasing)) }
    androidx.compose.material3.Surface(
        color = Color(0xF0101014),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.8f)),
        modifier = modifier.testTag(RUNE_PLATE_TAG)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
        ) {
            Text(runeTierLabel(rune.tier).uppercase(), color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(tr(rune.name), color = accent, fontSize = 16.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(
                runeEffectText(rune.id, masteryLevel),
                color = Color(0xFFEDE7F6),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(remaining.value.coerceIn(0f, 1f))
                        .background(accent, CircleShape)
                )
            }
        }
    }
}

const val RUNE_PLATE_TAG = "rune_plate"
const val RUNE_CAUGHT_TAG = "rune_caught"

/** Aviso al capturar una runa: su nombre y lo que acaba de dar. */
@Composable
fun RuneCaughtNotice(rune: RuneVariant, masteryLevel: Int, modifier: Modifier = Modifier) {
    val accent = readableRuneColor(rune.color)
    androidx.compose.material3.Surface(
        color = Color(0xF0101014),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent),
        modifier = modifier.testTag(RUNE_CAUGHT_TAG)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Box(modifier = Modifier.size(10.dp).background(accent, CircleShape))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    androidx.compose.ui.res.stringResource(com.EGomez.idleecosystem.R.string.rune_caught_title).uppercase() + " · " + tr(rune.name),
                    color = accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Text(runeEffectText(rune.id, masteryLevel), color = Color(0xFFEDE7F6), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SwipeRune(rune: RuneVariant, isInteractive: Boolean, onSuccess: () -> Unit) {
    var hasSwiped by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "Guide")
    val guideAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse)
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isInteractive) Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { hasSwiped = false; dragOffset = 0f }
                ) { change, dragAmount ->
                    change.consume()
                    dragOffset += dragAmount.x
                    if (!hasSwiped && dragAmount.x.absoluteValue > 10 && dragOffset.absoluteValue > 100) {
                        hasSwiped = true; onSuccess()
                    }
                }
            } else Modifier)
    ) {
        val cx = size.width / 2; val cy = size.height / 2
        val activeColor = if (dragOffset.absoluteValue > 20) Color.White else rune.color

        val path = Path().apply { moveTo(cx - 100f, cy); quadraticTo(cx, cy + 20f, cx + 100f, cy) }
        drawPath(path, activeColor, style = Stroke(width = 15f, cap = StrokeCap.Round))

        val guidePath = Path().apply {
            moveTo(cx - 120f, cy - 40f); lineTo(cx + 120f, cy - 40f); lineTo(cx + 100f, cy - 55f)
            moveTo(cx + 120f, cy - 40f); lineTo(cx + 100f, cy - 25f)
        }
        drawPath(guidePath, rune.color.copy(alpha = guideAlpha), style = Stroke(width = 4f, cap = StrokeCap.Round))
    }
}

@Composable
fun HoldRune(rune: RuneVariant, isInteractive: Boolean, pulse: Float, rotation: Float, onSuccess: () -> Unit) {
    var isHolding by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val targetDuration = 1000f

    LaunchedEffect(isInteractive) { if (!isInteractive) isHolding = false }
    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            while (isHolding && progress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed / targetDuration).coerceAtMost(1f)
                if (progress >= 1f) onSuccess()
                withFrameNanos { }
            }
        } else { progress = 0f }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isInteractive) Modifier.pointerInput(Unit) {
                detectTapGestures(onPress = { isHolding = true; tryAwaitRelease(); isHolding = false })
            } else Modifier)
    ) {
        val cx = size.width / 2; val cy = size.height / 2; val baseRadius = 80f * pulse
        rotate(rotation, pivot = Offset(cx, cy)) {
            drawCircle(rune.color.copy(alpha = 0.3f), baseRadius + 30f, style = Stroke(4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))))
        }
        drawCircle(Color.Black, baseRadius); drawCircle(rune.color, baseRadius, style = Stroke(6f))
        drawCircle(Color.White, if (isHolding) baseRadius * progress else baseRadius * 0.2f)
    }
}

@Composable
fun TraceRune(rune: RuneVariant, isInteractive: Boolean, pulse: Float, onSuccess: () -> Unit) {
    val (points, starOrder) = remember(rune.id) {
        when {
            rune.id.contains("nyarlathotep") -> {
                val p = listOf(Offset(-0.8f, -0.8f), Offset(0.8f, -0.8f), Offset(-0.8f, 0f), Offset(0.8f, 0f), Offset(0f, 0.8f))
                val o = listOf(0, 1, 2, 3, 4); Pair(p, o)
            }
            rune.id.contains("azathoth") -> {
                val p = listOf(Offset(0f, -0.8f), Offset(0.8f, -0.2f), Offset(0.5f, 0.7f), Offset(-0.5f, 0.7f), Offset(-0.8f, -0.2f))
                val o = listOf(0, 1, 2, 3, 4, 0); Pair(p, o)
            }
            else -> {
                val p = listOf(Offset(0f, -0.9f), Offset(0.6f, 0.8f), Offset(-0.9f, -0.3f), Offset(0.9f, -0.3f), Offset(-0.6f, 0.8f))
                val o = listOf(0, 1, 2, 3, 4, 0); Pair(p, o)
            }
        }
    }

    var currentTargetIndex by remember { mutableIntStateOf(0) }
    val pathDrawn = remember { Path() }
    var absolutePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isInteractive) Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (absolutePoints.isNotEmpty()) {
                            val startPoint = absolutePoints[starOrder[0]]
                            if ((offset - startPoint).getDistance() < 100f) {
                                pathDrawn.reset(); pathDrawn.moveTo(startPoint.x, startPoint.y); currentTargetIndex = 1
                            }
                        }
                    },
                    onDragEnd = { if (currentTargetIndex < starOrder.size) { currentTargetIndex = 0; pathDrawn.reset() } }
                ) { change, _ ->
                    change.consume()
                    if (absolutePoints.isNotEmpty() && currentTargetIndex > 0 && currentTargetIndex < starOrder.size) {
                        pathDrawn.lineTo(change.position.x, change.position.y)
                        if ((change.position - absolutePoints[starOrder[currentTargetIndex]]).getDistance() < 80f) {
                            currentTargetIndex++; if (currentTargetIndex >= starOrder.size) onSuccess()
                        }
                    }
                }
            } else Modifier)
    ) {
        val cx = size.width / 2; val cy = size.height / 2; val radius = size.minDimension * 0.4f
        if (absolutePoints.isEmpty()) absolutePoints = points.map { Offset(cx + it.x * radius, cy + it.y * radius) }

        val guidePath = Path().apply {
            if (absolutePoints.isNotEmpty()) {
                moveTo(absolutePoints[starOrder[0]].x, absolutePoints[starOrder[0]].y)
                for (i in 1 until starOrder.size) lineTo(absolutePoints[starOrder[i]].x, absolutePoints[starOrder[i]].y)
            }
        }
        drawPath(guidePath, rune.color.copy(alpha = 0.2f), style = Stroke(10f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        starOrder.forEachIndexed { i, idx ->
            val p = absolutePoints[idx]; val isNext = i == currentTargetIndex
            drawCircle(if (i < currentTargetIndex) rune.color else if (isNext) Color.White else Color.Gray.copy(0.5f), if (isNext) 20f * pulse else 12f, p)
        }
        drawPath(pathDrawn, rune.color, style = Stroke(8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}