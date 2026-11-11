package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import com.EGomez.idleecosystem.domain.model.Constellation
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.ui.i18n.ContentTranslations
import com.EGomez.idleecosystem.ui.i18n.contentLanguage
import com.EGomez.idleecosystem.ui.screens.main.dialogs.color
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.MadnessRed
import java.math.BigDecimal
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.pow
import kotlin.random.Random

/**
 * El cielo del Mapa de Locura (v0.19.2): siete constelaciones alrededor de El Despertar,
 * cada una con su Senda en el centro. Se acerca con dos dedos, se mueve arrastrando y se
 * toca una estrella para ver su ficha. Los colores dicen el estado de cada estrella:
 * gris (todavía no se alcanza), contorno del color de su constelación (se puede comprar;
 * brilla más si alcanza la Cordura), relleno (comprada), dorado (al máximo o Senda
 * recorrida) y rojo (Senda sellada).
 */
@Composable
fun ConstellationMap(
    upgrades: List<PrestigeUpgrade>,
    onNodeClick: (PrestigeUpgrade) -> Unit,
    currency: BigDecimal = BigDecimal.ZERO,
    selectedId: String? = null,
    /** Al cambiar, la vista se acerca a esa constelación (null = todo el cielo). */
    focus: Constellation? = null,
    focusKey: Int = 0
) {
    val textMeasurer = rememberTextMeasurer()
    // pointerInput(Unit) no se reinicia al recomponer: los toques leen la lista ACTUAL
    val currentUpgrades by rememberUpdatedState(upgrades)
    val currentOnNodeClick by rememberUpdatedState(onNodeClick)
    val positions = remember(upgrades.map { it.id }) { SkyLayout.positions(upgrades) }
    val currentPositions by rememberUpdatedState(positions)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var origin by remember { mutableStateOf<Offset?>(null) }

    fun fit(): Float = if (canvasSize.width == 0) 1f
    else min(canvasSize.width / SkyLayout.WIDTH, canvasSize.height / SkyLayout.HEIGHT)

    fun centered(scale: Float, world: Offset): Offset =
        Offset(canvasSize.width / 2f - world.x * scale, canvasSize.height / 2f - world.y * scale)

    // Acercarse a la constelación elegida (o volver al cielo entero)
    LaunchedEffect(focus, focusKey, canvasSize) {
        if (canvasSize.width == 0) return@LaunchedEffect
        if (focus == null) {
            zoom = 1f
            origin = centered(fit(), Offset(SkyLayout.WIDTH / 2, SkyLayout.HEIGHT / 2))
        } else {
            zoom = FOCUS_ZOOM
            origin = centered(fit() * zoom, SkyLayout.center(focus))
        }
    }

    val background = remember { SkyLayout.backgroundStars() }
    // Los nombres se traducen fuera del dibujo (tr() lee la configuración de la pantalla)
    val language = contentLanguage()
    fun t(text: String) = ContentTranslations.translate(text, language)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, gestureZoom, _ ->
                    val o = origin ?: return@detectTransformGestures
                    val newZoom = (zoom * gestureZoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                    val ratio = newZoom / zoom
                    origin = centroid - (centroid - o) * ratio + pan
                    zoom = newZoom
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    val o = origin ?: return@detectTapGestures
                    val scale = fit() * zoom
                    val touch = max(TOUCH_RADIUS_PX, NODE_RADIUS * 2.2f * scale)
                    val hit = currentUpgrades
                        .mapNotNull { node -> currentPositions[node.id]?.let { p -> node to (o + p * scale - tap).getDistance() } }
                        .filter { it.second < touch }
                        .minByOrNull { it.second }
                        ?.first
                    hit?.let { currentOnNodeClick(it) }
                }
            }
    ) {
        val o = origin ?: return@Canvas
        val scale = fit() * zoom
        fun at(p: Offset) = o + p * scale
        // Las estrellas crecen con el zoom, pero menos que el cielo
        val r = NODE_RADIUS * fit() * zoom.pow(0.6f)

        // Fondo: degradado y estrellas lejanas
        drawRect(Brush.verticalGradient(listOf(Color(0xFF07060D), Color(0xFF0E0A1A), Color(0xFF07060D))))
        background.forEach { (p, alpha) -> drawCircle(Color.White.copy(alpha = alpha), radius = 1.3f, center = at(p)) }

        val byId = upgrades.associateBy { it.id }
        val core = upgrades.firstOrNull { it.parentId == null && it.constellation == null }

        // Líneas: la cadena de cada constelación y los rayos hacia su Senda
        upgrades.forEach { node ->
            val p = positions[node.id] ?: return@forEach
            val color = node.constellation.color()
            if (MadnessPaths.isPath(node.id)) {
                upgrades.filter { it.constellation == node.constellation && !MadnessPaths.isPath(it.id) }.forEach { star ->
                    val q = positions[star.id] ?: return@forEach
                    drawLine(
                        color.copy(alpha = if (star.level > 0) 0.35f else 0.12f), at(q), at(p),
                        strokeWidth = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
                    )
                }
            } else {
                val parent = node.parentId?.let { byId[it] } ?: return@forEach
                val q = positions[parent.id] ?: return@forEach
                val lit = parent.level > 0 && node.level > 0
                // El hilo desde El Despertar cruza el cielo: siempre fino y tenue
                val fromCore = parent.constellation == null
                drawLine(
                    if (lit) color.copy(alpha = if (fromCore) 0.35f else 0.8f) else color.copy(alpha = if (parent.level > 0) 0.3f else 0.12f),
                    at(q), at(p),
                    strokeWidth = if (lit && !fromCore) 3f else 1.5f,
                    pathEffect = if (parent.level > 0) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
        }

        // Nombres de las constelaciones
        Constellation.entries.forEach { c ->
            val stars = upgrades.filter { it.constellation == c && !MadnessPaths.isPath(it.id) }
            if (stars.isEmpty()) return@forEach
            val lit = stars.count { it.level > 0 }
            val label = textMeasurer.measure(
                "${t(c.title).uppercase()}  $lit/${stars.size}",
                TextStyle(color = c.color().copy(alpha = if (lit > 0) 0.95f else 0.55f), fontSize = (10 * zoom.coerceIn(1f, 1.6f)).sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
            val top = at(SkyLayout.center(c) + Offset(0f, -SkyLayout.LABEL_OFFSET))
            drawText(label, topLeft = Offset(top.x - label.size.width / 2f, top.y - label.size.height / 2f))
        }

        // Estrellas
        upgrades.forEach { node ->
            val p = positions[node.id] ?: return@forEach
            val center = at(p)
            val isPath = MadnessPaths.isPath(node.id)
            val sealed = MadnessPaths.isSealed(node, upgrades)
            val reachable = !sealed && when {
                isPath -> MadnessPaths.isAvailable(node, upgrades)
                node.parentId == null -> true
                else -> (byId[node.parentId]?.level ?: 0) > 0
            }
            val maxed = node.isMaxed
            val affordable = reachable && !maxed && currency >= node.currentCost
            val base = node.constellation.color()
            val radius = if (isPath) r * 1.5f else if (node == core) r * 1.4f else r
            val fill = when {
                sealed -> MadnessRed.copy(alpha = 0.6f)
                maxed || (isPath && node.level > 0) -> AncientGold
                node.level > 0 -> base
                else -> null
            }
            // Halo: más fuerte si se puede comprar ya
            if (fill != null || affordable) {
                drawCircle((fill ?: base).copy(alpha = if (affordable) 0.45f else 0.25f), radius * 2.1f, center)
            }
            when {
                fill != null -> drawCircle(fill, radius, center)
                reachable -> {
                    drawCircle(Color(0xFF15121C), radius, center)
                    drawCircle(base.copy(alpha = if (affordable) 1f else 0.7f), radius, center, style = Stroke(width = 3f))
                }
                else -> drawCircle(Color(0xFF3A3A44), radius * 0.75f, center)
            }
            // La Senda: doble anillo (gris si su constelación todavía no está completa)
            if (isPath) {
                drawCircle(
                    if (sealed) MadnessRed else if (reachable) base else Color(0xFF3A3A44),
                    radius * 1.45f, center, style = Stroke(width = 2.5f)
                )
            }
            if (sealed) {
                val d = radius * 0.8f
                drawLine(MadnessRed, Offset(center.x - d, center.y - d), Offset(center.x + d, center.y + d), strokeWidth = 4f)
                drawLine(MadnessRed, Offset(center.x - d, center.y + d), Offset(center.x + d, center.y - d), strokeWidth = 4f)
            }
            // Selección
            if (node.id == selectedId) {
                drawCircle(Color.White, radius * 1.9f, center, style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))))
            }
            // Nivel dentro de la estrella
            if (node.level > 0) {
                val text = textMeasurer.measure(
                    "${node.level}",
                    TextStyle(color = Color.Black, fontSize = (9 * zoom.coerceIn(1f, 1.8f)).sp, fontWeight = FontWeight.Black)
                )
                drawText(text, topLeft = Offset(center.x - text.size.width / 2f, center.y - text.size.height / 2f))
            }
            // Con zoom, el nombre de cada estrella
            if (zoom >= NAMES_ZOOM) {
                val name = textMeasurer.measure(
                    t(node.name),
                    TextStyle(color = if (reachable || node.level > 0) Color(0xFFE0E0E0) else Color.Gray, fontSize = 9.sp)
                )
                drawText(name, topLeft = Offset(center.x - name.size.width / 2f, center.y + radius * 1.6f + 2f))
            }
        }
    }
}

/** Dónde va cada estrella: un patrón por constelación alrededor de su Senda. */
object SkyLayout {
    const val WIDTH = 1000f
    const val HEIGHT = 1500f
    const val LABEL_OFFSET = 205f
    private val CORE = Offset(500f, 735f)

    private val centers = mapOf(
        Constellation.MIND to Offset(500f, 200f),
        Constellation.GEARS to Offset(810f, 450f),
        Constellation.DREAM to Offset(820f, 900f),
        Constellation.VOID to Offset(650f, 1270f),
        Constellation.ARCANE to Offset(330f, 1270f),
        Constellation.CULT to Offset(170f, 900f),
        Constellation.HAND to Offset(190f, 450f)
    )

    /** Ángulos (grados) de las estrellas alrededor de la Senda, en el orden de la cadena. */
    private val patterns = mapOf(
        Constellation.HAND to listOf(250f, 300f, 350f, 40f),
        Constellation.GEARS to listOf(150f, 270f, 30f),
        Constellation.DREAM to listOf(200f, 320f, 90f),
        Constellation.MIND to listOf(160f, 200f, 270f, 340f, 20f),
        Constellation.VOID to listOf(180f, 270f, 0f),
        Constellation.CULT to listOf(240f, 300f, 20f, 120f),
        Constellation.ARCANE to listOf(225f, 315f, 0f, 90f, 160f)
    )

    fun center(c: Constellation): Offset = centers.getValue(c)

    fun positions(upgrades: List<PrestigeUpgrade>): Map<String, Offset> {
        val result = HashMap<String, Offset>()
        upgrades.forEach { node ->
            val c = node.constellation
            when {
                c == null -> result[node.id] = CORE
                MadnessPaths.isPath(node.id) -> result[node.id] = center(c)
                else -> {
                    val stars = upgrades.filter { it.constellation == c && !MadnessPaths.isPath(it.id) }
                    val index = stars.indexOf(node)
                    val angles = patterns.getValue(c)
                    val angle = Math.toRadians(angles[index % angles.size].toDouble())
                    // Radio alterno: la figura no es un círculo perfecto
                    val radius = if (index % 2 == 0) 125f else 150f
                    result[node.id] = center(c) + Offset((cos(angle) * radius).toFloat(), (sin(angle) * radius).toFloat())
                }
            }
        }
        return result
    }

    fun backgroundStars(): List<Pair<Offset, Float>> {
        val random = Random(1890)
        return List(220) {
            Offset(random.nextFloat() * WIDTH, random.nextFloat() * HEIGHT) to (0.08f + random.nextFloat() * 0.3f)
        }
    }
}

private const val NODE_RADIUS = 16f
private const val TOUCH_RADIUS_PX = 60f
private const val MIN_ZOOM = 0.8f
private const val MAX_ZOOM = 4f
private const val FOCUS_ZOOM = 2.2f
private const val NAMES_ZOOM = 1.8f

