package com.EGomez.idleecosystem.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier
) {
    // Si no hay runa o está oculta, no renderizamos nada para evitar bloqueos fantasma
    if (activeRune == null || runeState == RuneState.HIDDEN) return

    // La runa solo es interactiva si está en estado ACTIVE.
    // Si está apareciendo (APPEARING) o explotando (SUCCESS), bloqueamos la interacción
    // y en el caso de SUCCESS permitimos que los clicks pasen al fondo.
    val isInteractive = runeState == RuneState.ACTIVE

    val transition = updateTransition(targetState = runeState, label = "RuneTransition")
    val alpha by transition.animateFloat(label = "Alpha") { state ->
        when (state) {
            RuneState.HIDDEN -> 0f
            RuneState.APPEARING, RuneState.ACTIVE -> 1f
            RuneState.SUCCESS -> 0f // Se desvanece al ganar
            RuneState.FAILED -> 0f
        }
    }

    // Escala dinámica para feedback visual
    val scale by transition.animateFloat(label = "Scale") { state ->
        when (state) {
            RuneState.HIDDEN -> 0.2f
            RuneState.APPEARING -> 1.1f // Efecto pop
            RuneState.ACTIVE -> 1f
            RuneState.SUCCESS -> 1.8f // Explosión grande
            RuneState.FAILED -> 0.8f
        }
    }

    // Animaciones continuas de "vida"
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

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val runeSize = 250.dp
        val runeSizePx = with(density) { runeSize.toPx() }
        val maxW = constraints.maxWidth
        val maxH = constraints.maxHeight

        // Calculamos posición aleatoria solo cuando cambia la runa activa
        val positionOffset = remember(activeRune) {
            val randomX = Random.nextInt(0, (maxW - runeSizePx).toInt().coerceAtLeast(1))
            val randomY = Random.nextInt(0, (maxH - runeSizePx).toInt().coerceAtLeast(1))
            IntOffset(randomX, randomY)
        }

        Box(
            modifier = Modifier
                .offset { positionOffset }
                .size(runeSize)
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                }
                // --- VISIBILIDAD: HALO DE FONDO ---
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                // Borde sutil
                .border(width = 2.dp, color = activeRune.color.copy(alpha = 0.3f), shape = CircleShape)

                // --- INTERACTIVIDAD ---
                // Solo si es interactiva ponemos el fondo transparente que captura clicks.
                // Si isInteractive es false, este modificador se ignora y los clicks pasan.
                .then(
                    if (isInteractive) Modifier.background(Color.Transparent) else Modifier
                )
        ) {
            when (activeRune.tier) {
                RuneTier.TIER_1_SWIPE -> SwipeRune(activeRune, isInteractive, onSuccess)
                RuneTier.TIER_2_HOLD -> HoldRune(activeRune, isInteractive, pulse, rotation, onSuccess)
                RuneTier.TIER_3_TRACE -> TraceRune(activeRune, isInteractive, pulse, onSuccess)
            }
        }
    }
}

// ============================================================================================
// COMPONENTES DE RUNAS (Gestos condicionales)
// ============================================================================================

@Composable
fun SwipeRune(rune: RuneVariant, isInteractive: Boolean, onSuccess: () -> Unit) {
    var hasSwiped by remember { mutableStateOf(false) }
    val pathColor = rune.color

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isInteractive) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { hasSwiped = false }
                        ) { change, dragAmount ->
                            change.consume()
                            if (!hasSwiped && dragAmount.x.absoluteValue > 30 && dragAmount.y.absoluteValue < 30) {
                                hasSwiped = true
                                onSuccess()
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        val width = size.width
        val height = size.height
        val cx = width / 2
        val cy = height / 2

        val path = Path().apply {
            moveTo(cx - (width * 0.4f), cy)
            quadraticBezierTo(cx - (width * 0.1f), cy - 20f, cx, cy)
            quadraticBezierTo(cx + (width * 0.1f), cy + 20f, cx + (width * 0.4f), cy)
        }

        drawPath(path, pathColor.copy(alpha = 0.5f), style = Stroke(20f, cap = StrokeCap.Round))
        drawPath(path, Color.White, style = Stroke(6f, cap = StrokeCap.Round))
    }
}

@Composable
fun HoldRune(rune: RuneVariant, isInteractive: Boolean, pulse: Float, rotation: Float, onSuccess: () -> Unit) {
    var isHolding by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val targetDuration = 1000f

    // Si deja de ser interactiva, soltamos
    LaunchedEffect(isInteractive) {
        if (!isInteractive) isHolding = false
    }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            while (isHolding && progress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed / targetDuration).coerceAtMost(1f)
                if (progress >= 1f) onSuccess()
                withFrameNanos { }
            }
        } else {
            progress = 0f
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isInteractive) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHolding = true
                                tryAwaitRelease()
                                isHolding = false
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        val cx = size.width / 2
        val cy = size.height / 2
        val baseRadius = (size.minDimension / 2) * 0.7f * pulse

        rotate(rotation, pivot = Offset(cx, cy)) {
            drawCircle(rune.color.copy(alpha = 0.3f), baseRadius + 10f, style = Stroke(4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f))))
            val trianglePath = Path().apply {
                val r = baseRadius
                moveTo(cx, cy - r); lineTo(cx + r * 0.866f, cy + r * 0.5f); lineTo(cx - r * 0.866f, cy + r * 0.5f); close()
            }
            drawPath(trianglePath, rune.color.copy(alpha = 0.5f), style = Stroke(3f))
        }

        drawCircle(Color.Black, baseRadius * 0.9f)
        drawCircle(rune.color, baseRadius * 0.9f, style = Stroke(6f))
        val pupilRadius = if (isHolding) baseRadius * 0.9f * progress else baseRadius * 0.2f
        drawCircle(Color.White, pupilRadius)
    }
}

@Composable
fun TraceRune(rune: RuneVariant, isInteractive: Boolean, pulse: Float, onSuccess: () -> Unit) {
    val points = remember { listOf(Offset(0f, -0.9f), Offset(0.6f, 0.8f), Offset(-0.9f, -0.3f), Offset(0.9f, -0.3f), Offset(-0.6f, 0.8f)) }
    val starOrder = listOf(0, 1, 2, 3, 4, 0)
    var currentTargetIndex by remember { mutableIntStateOf(0) }
    val pathDrawn = remember { Path() }
    var absolutePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (isInteractive) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (currentTargetIndex < starOrder.size && absolutePoints.isNotEmpty()) {
                                    val startPoint = absolutePoints[starOrder[0]]
                                    if ((offset - startPoint).getDistance() < 80f) {
                                        pathDrawn.reset(); pathDrawn.moveTo(startPoint.x, startPoint.y); currentTargetIndex = 1
                                    }
                                }
                            },
                            onDragEnd = { if (currentTargetIndex < starOrder.size) { currentTargetIndex = 0; pathDrawn.reset() } }
                        ) { change, _ ->
                            change.consume()
                            val pos = change.position
                            if (absolutePoints.isNotEmpty() && currentTargetIndex > 0 && currentTargetIndex < starOrder.size) {
                                pathDrawn.lineTo(pos.x, pos.y)
                                if ((pos - absolutePoints[starOrder[currentTargetIndex]]).getDistance() < 60f) {
                                    currentTargetIndex++
                                    if (currentTargetIndex >= starOrder.size) onSuccess()
                                }
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        val cx = size.width / 2; val cy = size.height / 2; val radius = size.minDimension * 0.45f
        if (absolutePoints.isEmpty()) absolutePoints = points.map { Offset(cx + it.x * radius, cy + it.y * radius) }

        val guidePath = Path().apply {
            if (absolutePoints.isNotEmpty()) { moveTo(absolutePoints[starOrder[0]].x, absolutePoints[starOrder[0]].y); for (i in 1 until starOrder.size) { val p = absolutePoints[starOrder[i]]; lineTo(p.x, p.y) } }
        }
        drawPath(guidePath, rune.color.copy(alpha = 0.2f), style = Stroke(8f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        starOrder.forEachIndexed { i, idx ->
            val p = absolutePoints[idx]; val isNext = i == currentTargetIndex
            drawCircle(if (i < currentTargetIndex) rune.color else if (isNext) Color.White else Color.Gray.copy(0.5f), if (isNext) 15f * pulse else 8f, p)
        }
        drawPath(pathDrawn, rune.color, style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(pathDrawn, Color.White.copy(0.6f), style = Stroke(2f))
    }
}