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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * El cielo del Mapa de Locura (v0.19.2; figuras reales desde la v0.19.3): siete
 * constelaciones alrededor de El Despertar, cada una con su Senda en el centro. Se acerca con dos dedos, se mueve arrastrando y se
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

        // Figuras reales: las líneas de cada constelación, sus estrellas de adorno, el
        // hilo de su Senda a la estrella más cercana y el hilo desde El Despertar
        SkyLayout.figures.forEach { (c, figure) ->
            val stars = upgrades.filter { it.constellation == c && !MadnessPaths.isPath(it.id) }
            if (stars.isEmpty()) return@forEach
            val color = c.color()
            val progress = stars.count { it.level > 0 }.toFloat() / stars.size
            val pathNode = upgrades.firstOrNull { it.constellation == c && MadnessPaths.isPath(it.id) }
            val nodeAt = figure.nodes.zip(stars).toMap()
            fun world(name: String) = figure.world(name)
            // Un extremo "encendido": estrella comprada, Senda recorrida o adorno de una
            // constelación completa
            fun lit(name: String): Boolean? = when {
                name == figure.path -> pathNode?.let { it.level > 0 }
                nodeAt[name] != null -> nodeAt.getValue(name).level > 0
                else -> null
            }
            figure.edges.forEach { (from, to) ->
                val ends = listOfNotNull(lit(from), lit(to))
                val on = if (ends.isEmpty()) progress >= 1f else ends.all { it }
                drawLine(
                    color.copy(alpha = if (on) 0.85f else 0.14f + 0.3f * progress),
                    at(world(from)), at(world(to)),
                    strokeWidth = if (on) 2.5f else 1.3f
                )
            }
            // La Senda se une a la estrella más cercana (si una línea no las une ya)
            val pathPoint = world(figure.path)
            val nearest = figure.nodes.minByOrNull { (world(it) - pathPoint).getDistance() }
            if (nearest != null && figure.edges.none { setOf(it.first, it.second) == setOf(nearest, figure.path) }) {
                drawLine(
                    color.copy(alpha = if ((pathNode?.level ?: 0) > 0) 0.85f else 0.35f),
                    at(world(nearest)), at(pathPoint),
                    strokeWidth = 1.6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }
            // El hilo desde El Despertar llega a la estrella más cercana al centro del cielo
            core?.let { centerNode ->
                val first = stars.first()
                drawLine(
                    color.copy(alpha = if (first.level > 0) 0.35f else if (centerNode.level > 0) 0.22f else 0.1f),
                    at(SkyLayout.CORE), at(positions[first.id] ?: return@let),
                    strokeWidth = 1.2f,
                    pathEffect = if (first.level > 0) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
            // Estrellas de adorno
            figure.points.keys.filter { it != figure.path && it !in nodeAt }.forEach { name ->
                drawCircle(color.copy(alpha = 0.3f + 0.45f * progress), r * 0.3f, at(world(name)))
            }
        }

        // Nombres de las constelaciones: su tema y su figura real
        SkyLayout.figures.forEach { (c, figure) ->
            val stars = upgrades.filter { it.constellation == c && !MadnessPaths.isPath(it.id) }
            if (stars.isEmpty()) return@forEach
            val lit = stars.count { it.level > 0 }
            val size = zoom.coerceIn(1f, 1.6f)
            val label = textMeasurer.measure(
                "${t(c.title).uppercase()}  $lit/${stars.size}",
                TextStyle(color = c.color().copy(alpha = if (lit > 0) 0.95f else 0.55f), fontSize = (10 * size).sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
            val subtitle = textMeasurer.measure(
                t(figure.star),
                TextStyle(color = Color.Gray, fontSize = (8 * size).sp, letterSpacing = 1.sp)
            )
            // (sin salirse por arriba del cielo)
            val top = at(Offset(figure.center.x, (figure.center.y - SkyLayout.LABEL_OFFSET).coerceAtLeast(40f)))
            drawText(label, topLeft = Offset(top.x - label.size.width / 2f, top.y - label.size.height / 2f))
            drawText(subtitle, topLeft = Offset(top.x - subtitle.size.width / 2f, top.y + label.size.height / 2f))
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
            val radius = if (isPath) r * 1.35f else if (node == core) r * 1.4f else r
            val fill = when {
                sealed -> null
                maxed || (isPath && node.level > 0) -> AncientGold
                node.level > 0 -> base
                else -> null
            }
            // Halo: más fuerte si se puede comprar ya
            if (fill != null || affordable) {
                drawCircle((fill ?: base).copy(alpha = if (affordable) 0.4f else 0.22f), radius * 1.7f, center)
            }
            when {
                // Sellada: apagada, con un anillo rojo fino y una cruz pequeña
                sealed -> drawCircle(Color(0xFF1A1418), radius, center)
                fill != null -> drawCircle(fill, radius, center)
                reachable -> {
                    drawCircle(Color(0xFF15121C), radius, center)
                    drawCircle(base.copy(alpha = if (affordable) 1f else 0.7f), radius, center, style = Stroke(width = 3f))
                }
                // Todavía fuera de alcance: un anillo gris (las de adorno son puntos sin anillo)
                else -> {
                    drawCircle(Color(0xFF15121C), radius * 0.85f, center)
                    drawCircle(Color(0xFF55555F), radius * 0.85f, center, style = Stroke(width = 1.5f))
                }
            }
            // La Senda: doble anillo (gris si su constelación todavía no está completa)
            if (isPath) {
                drawCircle(
                    if (sealed) MadnessRed.copy(alpha = 0.6f) else if (reachable) base else Color(0xFF3A3A44),
                    radius * 1.4f, center, style = Stroke(width = if (sealed) 1.5f else 2.5f)
                )
            }
            if (sealed) {
                val d = radius * 0.45f
                drawLine(MadnessRed.copy(alpha = 0.8f), Offset(center.x - d, center.y - d), Offset(center.x + d, center.y + d), strokeWidth = 2.5f)
                drawLine(MadnessRed.copy(alpha = 0.8f), Offset(center.x - d, center.y + d), Offset(center.x + d, center.y - d), strokeWidth = 2.5f)
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

/**
 * Una constelación real (v0.19.3): sus estrellas con nombre (en unidades del cielo, con
 * respecto a su centro), las líneas de su figura, sus nodos del Mapa (desde la v0.19.4,
 * todas sus estrellas; en el orden de la lista del Mapa, la primera es la más cercana a
 * El Despertar) y el punto vacío del centro donde va su Senda.
 */
class SkyFigure(
    val star: String,
    val center: Offset,
    val points: Map<String, Offset>,
    val edges: List<Pair<String, String>>,
    val nodes: List<String>,
    val path: String
) {
    /** Posición en el cielo de una estrella de la figura. */
    fun world(name: String): Offset = center + points.getValue(name) * SkyLayout.FIGURE_SCALE
}

/** Dónde va cada estrella: siete constelaciones reales alrededor de El Despertar. */
object SkyLayout {
    const val WIDTH = 1200f
    const val HEIGHT = 1800f
    const val LABEL_OFFSET = 265f
    /** Las figuras se dibujan un 30 % más grandes que sus coordenadas, para que respiren. */
    const val FIGURE_SCALE = 1.3f
    val CORE = Offset(600f, 880f)

    private fun o(x: Int, y: Int) = Offset(x.toFloat(), y.toFloat())

    val figures: Map<Constellation, SkyFigure> = linkedMapOf(
        // La Mano: Orión, el cazador. Su Senda va en el pecho, entre los hombros y el cinturón.
        Constellation.HAND to SkyFigure(
            "Orión", o(220, 540),
            mapOf(
                "Rigel" to o(70, 100), "Saiph" to o(-55, 110), "Alnitak" to o(-34, 12), "Alnilam" to o(0, 0),
                "Mintaka" to o(34, -10), "Bellatrix" to o(62, -95), "Betelgeuse" to o(-72, -108), "Meissa" to o(-4, -150),
                "★" to o(-3, -52)
            ),
            listOf(
                "Betelgeuse" to "Bellatrix", "Betelgeuse" to "Alnitak", "Bellatrix" to "Mintaka", "Alnitak" to "Alnilam",
                "Alnilam" to "Mintaka", "Alnitak" to "Saiph", "Mintaka" to "Rigel", "Meissa" to "Betelgeuse", "Meissa" to "Bellatrix"
            ),
            listOf("Rigel", "Mintaka", "Bellatrix", "Betelgeuse", "Alnilam", "Alnitak", "Saiph", "Meissa"), "★"
        ),
        // La Mente: Casiopea, la reina en su trono (la W).
        Constellation.MIND to SkyFigure(
            "Casiopea", o(600, 230),
            mapOf("Caph" to o(-125, -45), "Schedar" to o(-62, 20), "Navi" to o(-5, -28), "Ruchbah" to o(48, 32), "Segin" to o(118, 72), "★" to o(-6, 52)),
            listOf("Caph" to "Schedar", "Schedar" to "Navi", "Navi" to "Ruchbah", "Ruchbah" to "Segin"),
            listOf("Segin", "Ruchbah", "Navi", "Schedar", "Caph"), "★"
        ),
        // El Engranaje: la Osa Mayor, el Carro. La Senda va en el centro del cazo.
        Constellation.GEARS to SkyFigure(
            "Osa Mayor", o(980, 540),
            mapOf(
                "Alkaid" to o(-150, 48), "Mizar" to o(-100, 12), "Alioth" to o(-52, -6), "Megrez" to o(0, -12),
                "Dubhe" to o(92, -50), "Merak" to o(108, 32), "Phecda" to o(22, 42), "★" to o(56, 2)
            ),
            listOf(
                "Alkaid" to "Mizar", "Mizar" to "Alioth", "Alioth" to "Megrez", "Megrez" to "Dubhe",
                "Dubhe" to "Merak", "Merak" to "Phecda", "Phecda" to "Megrez"
            ),
            listOf("Alkaid", "Mizar", "Alioth", "Megrez", "Phecda", "Merak", "Dubhe"), "★"
        ),
        // El Sueño: las Pléyades, las siete hermanas.
        Constellation.DREAM to SkyFigure(
            "Las Pléyades", o(990, 1080),
            mapOf(
                "Atlas" to o(-105, 18), "Pleione" to o(-118, -22), "Alcyone" to o(0, 0), "Merope" to o(48, 52),
                "Electra" to o(92, -30), "Maia" to o(68, -92), "Taygeta" to o(14, -118), "★" to o(34, -44)
            ),
            listOf(
                "Pleione" to "Atlas", "Atlas" to "Alcyone", "Alcyone" to "Merope", "Merope" to "Electra",
                "Electra" to "Maia", "Maia" to "Taygeta", "Taygeta" to "Alcyone"
            ),
            listOf("Pleione", "Atlas", "Alcyone", "Merope", "Electra", "Maia", "Taygeta"), "★"
        ),
        // El Vacío: el Pez Austral y Fomalhaut, la estrella solitaria.
        Constellation.VOID to SkyFigure(
            "Pez Austral", o(780, 1530),
            mapOf(
                "Epsilon" to o(-72, -34), "Fomalhaut" to o(-125, 8), "Delta" to o(-72, 42), "Gamma" to o(-8, 50),
                "Beta" to o(55, 34), "Theta" to o(118, 6), "Iota" to o(88, -28), "Mu" to o(12, -44), "★" to o(0, 4)
            ),
            listOf(
                "Epsilon" to "Fomalhaut", "Fomalhaut" to "Delta", "Delta" to "Gamma", "Gamma" to "Beta",
                "Beta" to "Theta", "Theta" to "Iota", "Iota" to "Mu", "Mu" to "Epsilon"
            ),
            listOf("Epsilon", "Fomalhaut", "Delta", "Gamma", "Beta", "Theta", "Iota", "Mu"), "★"
        ),
        // El Culto: Tauro, el toro del sacrificio. La Senda va en la boca de la V de las Híades.
        Constellation.CULT to SkyFigure(
            "Tauro", o(200, 1080),
            mapOf(
                "Elnath" to o(128, -118), "Ain" to o(58, -32), "Delta" to o(36, 10), "Gamma" to o(8, 48), "Theta" to o(-24, 16),
                "Aldebaran" to o(-48, -22), "Zeta" to o(-70, -132), "Lambda" to o(-12, 108), "Omicron" to o(-112, 122), "★" to o(5, -42)
            ),
            listOf(
                "Elnath" to "Ain", "Ain" to "Delta", "Delta" to "Gamma", "Gamma" to "Theta", "Theta" to "Aldebaran",
                "Aldebaran" to "Zeta", "Gamma" to "Lambda", "Lambda" to "Omicron"
            ),
            listOf("Elnath", "Ain", "Delta", "Gamma", "Theta", "Aldebaran", "Zeta", "Lambda", "Omicron"), "★"
        ),
        // Lo Arcano: Perseo, con la cabeza de Medusa (Algol). Su Senda va entre sus piernas.
        Constellation.ARCANE to SkyFigure(
            "Perseo", o(400, 1530),
            mapOf(
                "Eta" to o(-12, -112), "Gamma" to o(-6, -62), "Mirfak" to o(0, 0), "Delta" to o(26, 46), "Epsilon" to o(42, 98),
                "Zeta" to o(18, 148), "Kappa" to o(-42, 30), "Algol" to o(-80, 62), "Rho" to o(-98, 108),
                "★" to o(-22, 72)
            ),
            listOf(
                "Eta" to "Gamma", "Gamma" to "Mirfak", "Mirfak" to "Delta", "Delta" to "Epsilon", "Epsilon" to "Zeta",
                "Mirfak" to "Kappa", "Kappa" to "Algol", "Algol" to "Rho"
            ),
            listOf("Eta", "Gamma", "Mirfak", "Kappa", "Algol", "Rho", "Delta", "Epsilon", "Zeta"), "★"
        )
    )

    fun center(c: Constellation): Offset = figures.getValue(c).center

    fun positions(upgrades: List<PrestigeUpgrade>): Map<String, Offset> {
        val result = HashMap<String, Offset>()
        upgrades.forEach { node ->
            val c = node.constellation
            val figure = c?.let { figures[it] }
            when {
                figure == null -> result[node.id] = CORE
                MadnessPaths.isPath(node.id) -> result[node.id] = figure.world(figure.path)
                else -> {
                    val stars = upgrades.filter { it.constellation == c && !MadnessPaths.isPath(it.id) }
                    val name = figure.nodes.getOrNull(stars.indexOf(node))
                    result[node.id] = name?.let { figure.world(it) } ?: figure.center
                }
            }
        }
        return result
    }

    /** El nombre real de la estrella de un nodo (o null si es El Despertar). */
    fun starName(node: PrestigeUpgrade, upgrades: List<PrestigeUpgrade>): String? {
        val figure = node.constellation?.let { figures[it] } ?: return null
        if (MadnessPaths.isPath(node.id)) return figure.path.takeIf { it != "★" }
        val stars = upgrades.filter { it.constellation == node.constellation && !MadnessPaths.isPath(it.id) }
        return figure.nodes.getOrNull(stars.indexOf(node))
    }

    fun backgroundStars(): List<Pair<Offset, Float>> {
        val random = Random(1890)
        return List(220) {
            Offset(random.nextFloat() * WIDTH, random.nextFloat() * HEIGHT) to (0.08f + random.nextFloat() * 0.3f)
        }
    }
}

private const val NODE_RADIUS = 12f
private const val TOUCH_RADIUS_PX = 60f
private const val MIN_ZOOM = 0.8f
private const val MAX_ZOOM = 4f
private const val FOCUS_ZOOM = 2.2f
private const val NAMES_ZOOM = 1.8f

