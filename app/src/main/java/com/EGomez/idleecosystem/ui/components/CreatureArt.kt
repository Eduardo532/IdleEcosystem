package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * El dibujo de cada criatura del Bestiario (v0.19.10), hecho por código a partir de una
 * receta corta: un arquetipo ("fishman", "squid", "hound"…) y sus rasgos (ojos, tentáculos,
 * alas, cuernos, corona, colmillos, púas, aura, bocas, anillos, goteo, capucha). Las
 * proporciones y el tono varían con el id de la criatura, así que dos criaturas del mismo
 * arquetipo no se ven iguales. Estilo de grabado: cuerpo oscuro, contorno del color de su
 * Dominio y ojos que brillan con el color de su rareza.
 */
data class ArtSpec(
    val archetype: String,
    val eyes: Int = 2,
    val tentacles: Int = 0,
    val wings: Boolean = false,
    val horns: Boolean = false,
    val crown: Boolean = false,
    val fangs: Boolean = false,
    val spikes: Boolean = false,
    val glow: Boolean = false,
    val mouths: Boolean = false,
    val rings: Boolean = false,
    val drips: Boolean = false,
    val hood: Boolean = false,
    /** Lo que lleva en la mano: bastón, libro, farol, daga, matraz, orbe, vela, pergamino, llave… */
    val prop: String? = null
) {
    companion object {
        /** "squid e2 t6 c g" -> arquetipo squid, 2 ojos, 6 tentáculos, corona y aura. */
        fun parse(recipe: String): ArtSpec {
            val parts = recipe.trim().split(Regex("\\s+"))
            var spec = ArtSpec(parts.firstOrNull() ?: "blob")
            parts.drop(1).forEach { token ->
                val number = token.drop(1).toIntOrNull()
                spec = when (token.first()) {
                    'e' -> spec.copy(eyes = number ?: 2)
                    't' -> spec.copy(tentacles = number ?: 4)
                    'w' -> spec.copy(wings = true)
                    'h' -> spec.copy(horns = true)
                    'c' -> spec.copy(crown = true)
                    'f' -> spec.copy(fangs = true)
                    's' -> spec.copy(spikes = true)
                    'g' -> spec.copy(glow = true)
                    'm' -> spec.copy(mouths = true)
                    'r' -> spec.copy(rings = true)
                    'd' -> spec.copy(drips = true)
                    'k' -> spec.copy(hood = true)
                    'p' -> spec.copy(prop = token.drop(1))
                    else -> spec
                }
            }
            return spec
        }
    }
}

/** Dónde quedan la cabeza, las alas, los tentáculos y la boca de cada arquetipo (en 0..100). */
private data class Anchors(
    val head: Offset,
    val headR: Float,
    val wingRoot: Offset,
    val wingSpan: Float,
    val tentacleRoot: Offset,
    val tentacleSpan: Float,
    val mouth: Offset,
    val body: Offset,
    val bodyR: Float,
    /** Los ojos se reparten por el cuerpo y no solo por la cabeza (masas, enjambres). */
    val eyesOnBody: Boolean = false,
    /** La mano derecha, donde va el objeto que lleva. */
    val hand: Offset = Offset(66f, 64f)
)

@Composable
fun CreatureArt(recipe: String, seedKey: String, color: Color, eyeColor: Color, modifier: Modifier = Modifier) {
    val spec = remember(recipe) { ArtSpec.parse(recipe) }
    val seed = remember(seedKey) { seedKey.hashCode() }
    Canvas(modifier = modifier) { drawCreature(spec, seed, color, eyeColor) }
}

/** Para pruebas y vistas previas: dibuja la criatura en cualquier DrawScope. */
fun DrawScope.drawCreature(spec: ArtSpec, seed: Int, color: Color, eyeColor: Color) {
    CreaturePainter(this, spec, seed, color, eyeColor).draw()
}

private class CreaturePainter(
    val scope: DrawScope,
    val spec: ArtSpec,
    seed: Int,
    baseColor: Color,
    val eyeColor: Color
) {
    val rnd = Random(seed)
    val unit = min(scope.size.width, scope.size.height) / 100f
    val origin = Offset((scope.size.width - 100f * unit) / 2f, (scope.size.height - 100f * unit) / 2f)

    /** Pequeñas variaciones por criatura: tono, tamaño de cabeza y ancho del cuerpo. */
    val tint = lerp(baseColor, if (rnd.nextBoolean()) Color.White else Color(0xFF8E24AA), rnd.nextFloat() * 0.18f)
    val headScale = 0.88f + rnd.nextFloat() * 0.24f
    val widthScale = 0.9f + rnd.nextFloat() * 0.2f

    val line = tint
    val fill = lerp(Color(0xFF0B0B0F), tint, 0.30f)
    val detail = lerp(tint, Color.White, 0.35f)
    val shade = lerp(Color(0xFF050507), tint, 0.12f)
    val strokeW get() = 2.1f * unit

    fun p(x: Float, y: Float) = Offset(origin.x + x * unit, origin.y + y * unit)
    fun pt(o: Offset) = p(o.x, o.y)
    fun r(v: Float) = v * unit

    fun path(block: Path.() -> Unit) = Path().apply(block)
    fun Path.m(x: Float, y: Float) = moveTo(origin.x + x * unit, origin.y + y * unit)
    fun Path.l(x: Float, y: Float) = lineTo(origin.x + x * unit, origin.y + y * unit)
    fun Path.q(cx: Float, cy: Float, x: Float, y: Float) =
        quadraticBezierTo(origin.x + cx * unit, origin.y + cy * unit, origin.x + x * unit, origin.y + y * unit)
    fun Path.c(c1x: Float, c1y: Float, c2x: Float, c2y: Float, x: Float, y: Float) = cubicTo(
        origin.x + c1x * unit, origin.y + c1y * unit, origin.x + c2x * unit, origin.y + c2y * unit,
        origin.x + x * unit, origin.y + y * unit
    )

    fun shape(p: Path, fillColor: Color = fill, stroke: Color = line, width: Float = strokeW) {
        scope.drawPath(p, fillColor)
        scope.drawPath(p, stroke, style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    fun strokeOnly(p: Path, color: Color = line, width: Float = strokeW) =
        scope.drawPath(p, color, style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))

    fun oval(cx: Float, cy: Float, rx: Float, ry: Float, fillColor: Color = fill, stroke: Color = line) {
        val rect = Rect(p(cx - rx, cy - ry), Size(r(rx * 2), r(ry * 2)))
        scope.drawOval(fillColor, rect.topLeft, rect.size)
        scope.drawOval(stroke, rect.topLeft, rect.size, style = Stroke(width = strokeW))
    }

    fun circle(cx: Float, cy: Float, radius: Float, fillColor: Color = fill, stroke: Color? = line) {
        scope.drawCircle(fillColor, r(radius), p(cx, cy))
        if (stroke != null) scope.drawCircle(stroke, r(radius), p(cx, cy), style = Stroke(width = strokeW))
    }

    fun draw() {
        // Fondo: un halo tenue del color de la criatura (más fuerte con aura)
        scope.drawCircle(
            Brush.radialGradient(
                listOf(tint.copy(alpha = if (spec.glow) 0.42f else 0.16f), Color.Transparent),
                center = p(50f, 52f),
                radius = r(if (spec.glow) 54f else 46f)
            ),
            radius = r(54f),
            center = p(50f, 52f)
        )
        if (spec.glow) sparkles()
        val a = anchorsFor(spec.archetype)
        if (spec.rings) ringsBack(a)
        if (spec.wings || spec.archetype == "bat") wings(a)
        if (spec.tentacles > 0) tentacles(a)
        body(spec.archetype)
        if (spec.hood) hood(a)
        if (spec.horns || spec.archetype == "goat") horns(a)
        if (spec.spikes) spikes(a)
        if (spec.crown) crown(a)
        if (spec.mouths) extraMouths(a)
        if (spec.fangs) fangs(a)
        eyes(a)
        if (spec.drips) drips(a)
        spec.prop?.let { prop(it, a.hand) }
        if (spec.rings) ringsFront(a)
    }

    // ------------------------------------------------------------------ anclas
    fun anchorsFor(arch: String): Anchors = when (arch) {
        "humanoid" -> Anchors(Offset(50f, 30f), 9f * headScale, Offset(50f, 46f), 34f, Offset(50f, 86f), 34f, Offset(50f, 34f), Offset(50f, 62f), 20f, hand = Offset(62f, 70f))
        "figure" -> Anchors(Offset(50f, 22f), 8f * headScale, Offset(50f, 38f), 32f, Offset(50f, 90f), 24f, Offset(50f, 26f), Offset(50f, 52f), 14f, hand = Offset(68f, 62f))
        "fishman" -> Anchors(Offset(50f, 30f), 11f * headScale, Offset(50f, 48f), 34f, Offset(50f, 86f), 30f, Offset(50f, 36f), Offset(50f, 62f), 18f, hand = Offset(74f, 72f))
        "ghoul" -> Anchors(Offset(40f, 40f), 10f * headScale, Offset(55f, 46f), 30f, Offset(55f, 84f), 30f, Offset(32f, 44f), Offset(56f, 58f), 18f, hand = Offset(36f, 86f))
        "blob" -> Anchors(Offset(50f, 52f), 22f, Offset(50f, 46f), 36f, Offset(50f, 78f), 50f, Offset(50f, 66f), Offset(50f, 56f), 28f, eyesOnBody = true)
        "worm" -> Anchors(Offset(38f, 30f), 12f * headScale, Offset(50f, 50f), 30f, Offset(50f, 62f), 40f, Offset(30f, 32f), Offset(50f, 60f), 20f)
        "bat" -> Anchors(Offset(50f, 38f), 8f * headScale, Offset(50f, 52f), 44f, Offset(50f, 74f), 18f, Offset(50f, 42f), Offset(50f, 58f), 12f)
        "bird" -> Anchors(Offset(64f, 38f), 8f * headScale, Offset(46f, 52f), 38f, Offset(46f, 72f), 20f, Offset(72f, 40f), Offset(48f, 58f), 16f)
        "hound", "goat" -> Anchors(Offset(72f, 44f), 9f * headScale, Offset(48f, 50f), 34f, Offset(46f, 70f), 30f, Offset(80f, 48f), Offset(48f, 58f), 20f)
        "cat" -> Anchors(Offset(50f, 36f), 12f * headScale, Offset(50f, 54f), 34f, Offset(50f, 84f), 26f, Offset(50f, 42f), Offset(50f, 66f), 18f)
        "rat" -> Anchors(Offset(66f, 52f), 8f * headScale, Offset(46f, 54f), 26f, Offset(46f, 70f), 20f, Offset(76f, 56f), Offset(46f, 60f), 16f)
        "serpent" -> Anchors(Offset(50f, 22f), 9f * headScale, Offset(50f, 44f), 34f, Offset(50f, 80f), 28f, Offset(50f, 27f), Offset(50f, 58f), 18f)
        "insect" -> Anchors(Offset(50f, 30f), 9f * headScale, Offset(50f, 46f), 38f, Offset(50f, 60f), 34f, Offset(50f, 36f), Offset(50f, 58f), 16f)
        "squid" -> Anchors(Offset(50f, 40f), 18f, Offset(50f, 40f), 40f, Offset(50f, 58f), 34f, Offset(50f, 54f), Offset(50f, 40f), 20f)
        "jelly" -> Anchors(Offset(50f, 38f), 16f, Offset(50f, 40f), 40f, Offset(50f, 50f), 36f, Offset(50f, 48f), Offset(50f, 38f), 20f)
        "tree" -> Anchors(Offset(50f, 34f), 16f, Offset(50f, 36f), 38f, Offset(50f, 88f), 40f, Offset(50f, 56f), Offset(50f, 56f), 16f, eyesOnBody = true)
        "eye" -> Anchors(Offset(50f, 50f), 26f, Offset(50f, 50f), 44f, Offset(50f, 70f), 40f, Offset(50f, 72f), Offset(50f, 50f), 26f)
        "flame" -> Anchors(Offset(50f, 56f), 14f, Offset(50f, 56f), 36f, Offset(50f, 84f), 30f, Offset(50f, 66f), Offset(50f, 58f), 20f)
        "crystal" -> Anchors(Offset(50f, 50f), 14f, Offset(50f, 50f), 40f, Offset(50f, 80f), 30f, Offset(50f, 62f), Offset(50f, 50f), 22f)
        "star" -> Anchors(Offset(50f, 50f), 12f, Offset(50f, 50f), 40f, Offset(50f, 74f), 30f, Offset(50f, 58f), Offset(50f, 50f), 22f)
        "cone" -> Anchors(Offset(50f, 22f), 8f, Offset(50f, 50f), 34f, Offset(50f, 34f), 30f, Offset(50f, 26f), Offset(50f, 62f), 20f)
        "barrel" -> Anchors(Offset(50f, 24f), 9f, Offset(50f, 52f), 38f, Offset(50f, 80f), 28f, Offset(50f, 26f), Offset(50f, 56f), 16f)
        "mask" -> Anchors(Offset(50f, 46f), 22f, Offset(50f, 46f), 40f, Offset(50f, 74f), 30f, Offset(50f, 62f), Offset(50f, 48f), 22f)
        "book" -> Anchors(Offset(50f, 46f), 16f, Offset(50f, 46f), 40f, Offset(50f, 74f), 36f, Offset(50f, 60f), Offset(50f, 52f), 24f, eyesOnBody = true)
        "swarm" -> Anchors(Offset(50f, 50f), 28f, Offset(50f, 46f), 40f, Offset(50f, 76f), 40f, Offset(50f, 60f), Offset(50f, 50f), 30f, eyesOnBody = true)
        "golem" -> Anchors(Offset(50f, 26f), 9f * headScale, Offset(50f, 44f), 36f, Offset(50f, 88f), 30f, Offset(50f, 30f), Offset(50f, 58f), 20f, hand = Offset(74f, 66f))
        "colossus" -> Anchors(Offset(50f, 30f), 8f * headScale, Offset(50f, 40f), 46f, Offset(50f, 92f), 40f, Offset(50f, 34f), Offset(50f, 56f), 28f, hand = Offset(86f, 84f))
        "planet" -> Anchors(Offset(50f, 50f), 24f, Offset(50f, 50f), 46f, Offset(50f, 72f), 34f, Offset(50f, 60f), Offset(50f, 50f), 24f, eyesOnBody = true)
        "vortex" -> Anchors(Offset(50f, 50f), 10f, Offset(50f, 50f), 44f, Offset(50f, 70f), 40f, Offset(50f, 56f), Offset(50f, 50f), 28f)
        "wraith" -> Anchors(Offset(50f, 30f), 10f * headScale, Offset(50f, 44f), 38f, Offset(50f, 80f), 30f, Offset(50f, 36f), Offset(50f, 60f), 20f, hand = Offset(76f, 68f))
        "toad" -> Anchors(Offset(50f, 46f), 16f, Offset(50f, 50f), 36f, Offset(50f, 66f), 36f, Offset(50f, 60f), Offset(50f, 62f), 24f)
        "spider" -> Anchors(Offset(50f, 40f), 10f, Offset(50f, 52f), 40f, Offset(50f, 66f), 30f, Offset(50f, 46f), Offset(50f, 58f), 16f)
        "mouth" -> Anchors(Offset(50f, 30f), 12f, Offset(50f, 52f), 44f, Offset(50f, 78f), 40f, Offset(50f, 54f), Offset(50f, 54f), 26f)
        "crab" -> Anchors(Offset(50f, 46f), 10f, Offset(50f, 50f), 40f, Offset(50f, 64f), 34f, Offset(50f, 56f), Offset(50f, 56f), 22f)
        "fish" -> Anchors(Offset(66f, 48f), 9f, Offset(46f, 46f), 36f, Offset(44f, 62f), 30f, Offset(76f, 54f), Offset(48f, 52f), 22f)
        else -> Anchors(Offset(50f, 50f), 16f, Offset(50f, 50f), 36f, Offset(50f, 74f), 30f, Offset(50f, 60f), Offset(50f, 52f), 22f)
    }

    // ------------------------------------------------------------------ cuerpos
    fun body(arch: String) {
        val w = widthScale
        when (arch) {
            "humanoid" -> {
                shape(path { m(50 - 10 * w, 40f); c(42 - 14 * w, 52f, 36 - 18 * w, 74f, 32 - 16 * w, 90f); l(68 + 16 * w, 90f); c(64 + 18 * w, 74f, 58 + 14 * w, 52f, 50 + 10 * w, 40f); close() })
                strokeOnly(path { m(50f, 46f); l(50f, 88f) }, detail, strokeW * 0.6f)
                strokeOnly(path { m(50 - 10 * w, 44f); q(38f, 60f, 40f, 70f) }, line)
                strokeOnly(path { m(50 + 10 * w, 44f); q(62f, 60f, 60f, 70f) }, line)
                circle(50f, 30f, 9f * headScale)
            }
            "fishman" -> {
                // cuerpo encorvado de hombros anchos, piernas cortas y pies palmeados
                shape(path { m(44f, 42f); c(36f, 42f, 30f, 46f, 32f, 54f); c(34f, 64f, 38f, 72f, 40f, 78f); l(38f, 90f); l(46f, 90f); l(48f, 78f); l(52f, 78f); l(54f, 90f); l(62f, 90f); l(60f, 78f); c(62f, 72f, 66f, 64f, 68f, 54f); c(70f, 46f, 64f, 42f, 56f, 42f); close() })
                shape(path { m(36f, 90f); l(32f, 93f); l(48f, 93f); l(46f, 90f); close() })
                shape(path { m(54f, 90f); l(52f, 93f); l(68f, 93f); l(64f, 90f); close() })
                // brazos largos con manos palmeadas
                strokeOnly(path { m(34f, 50f); q(24f, 62f, 26f, 72f) }, line, strokeW * 1.6f)
                strokeOnly(path { m(66f, 50f); q(76f, 62f, 74f, 72f) }, line, strokeW * 1.6f)
                shape(path { m(26f, 72f); l(20f, 78f); l(25f, 76f); l(26f, 80f); l(28f, 75f); close() })
                shape(path { m(74f, 72f); l(80f, 78f); l(75f, 76f); l(74f, 80f); l(72f, 75f); close() })
                // escamas en el pecho
                for (row in 0..2) for (col in 0..2) {
                    val x = 43f + col * 7f - row % 2 * 3.5f
                    val y = 52f + row * 7f
                    strokeOnly(path { m(x - 3f, y); q(x, y + 3.5f, x + 3f, y) }, detail, strokeW * 0.45f)
                }
                val hr = 11f * headScale
                // aleta dorsal y cabeza ancha de pez con agallas y boca enorme
                shape(path { m(42f, 30f - hr * 0.7f); l(45f, 30f - hr * 1.5f); l(49f, 30f - hr * 0.95f); l(53f, 30f - hr * 1.6f); l(58f, 30f - hr * 0.7f); close() })
                oval(50f, 30f, hr * 1.3f, hr * 0.95f)
                strokeOnly(path { m(50f - hr * 1.15f, 32f); l(50f - hr * 0.95f, 36f); m(50f + hr * 1.15f, 32f); l(50f + hr * 0.95f, 36f) }, detail, strokeW * 0.6f)
                shape(path { m(50f - hr * 0.8f, 34f); q(50f, 34f + hr * 0.75f, 50f + hr * 0.8f, 34f); q(50f, 36f, 50f - hr * 0.8f, 34f); close() }, Color(0xFF07090A), detail, strokeW * 0.6f)
            }
            "figure" -> {
                // piernas y pies
                shape(path { m(43f, 64f); l(41f, 90f); l(47f, 90f); l(49f, 66f); close() })
                shape(path { m(51f, 66f); l(53f, 90f); l(59f, 90f); l(57f, 64f); close() })
                // abrigo
                shape(path { m(46f, 30f); c(38f, 31f, 37f, 34f, 37f, 40f); l(38f, 70f); l(62f, 70f); l(63f, 40f); c(63f, 34f, 62f, 31f, 54f, 30f); close() })
                strokeOnly(path { m(50f, 32f); l(50f, 70f); m(46f, 30f); l(50f, 42f); l(54f, 30f) }, detail, strokeW * 0.5f)
                // brazos
                strokeOnly(path { m(38f, 36f); q(32f, 50f, 33f, 62f) }, line, strokeW * 1.8f)
                strokeOnly(path { m(62f, 36f); q(68f, 48f, 68f, 62f) }, line, strokeW * 1.8f)
                circle(50f, 22f, 8f * headScale)
            }
            "ghoul" -> {
                // lomo encorvado, brazos largos hasta el suelo
                shape(path { m(46f, 42f); c(56f, 30f, 74f, 40f, 72f, 60f); c(70f, 72f, 66f, 80f, 64f, 88f); l(54f, 88f); c(54f, 76f, 50f, 66f, 46f, 60f); close() })
                strokeOnly(path { m(50f, 50f); q(40f, 66f, 36f, 86f) })
                strokeOnly(path { m(36f, 86f); l(32f, 90f); m(36f, 86f); l(37f, 91f); m(36f, 86f); l(40f, 90f) }, detail, strokeW * 0.6f)
                strokeOnly(path { m(66f, 60f); q(76f, 72f, 74f, 88f) })
                // cabeza de perro adelantada
                val hr = 10f * headScale
                shape(path { m(48f, 34f); c(44f, 28f, 34f, 30f, 32f, 38f); l(22f, 44f); l(30f, 48f); c(36f, 50f, 46f, 50f, 50f, 44f); close() })
                shape(path { m(42f, 32f); l(44f, 32f - hr); l(48f, 34f); close() })
            }
            "blob" -> {
                val bumps = 9 + rnd.nextInt(5)
                val pts = (0 until bumps).map { i ->
                    val ang = 2 * PI * i / bumps
                    val rad = (26f + rnd.nextFloat() * 8f) * w
                    Offset(50f + (cos(ang) * rad).toFloat(), 58f + (sin(ang) * rad * 0.82f).toFloat())
                }
                shape(smoothClosed(pts))
                // pliegues
                repeat(3) {
                    val x = 34f + rnd.nextFloat() * 32f
                    val y = 52f + rnd.nextFloat() * 18f
                    strokeOnly(path { m(x - 5, y); q(x, y + 3, x + 5, y) }, detail, strokeW * 0.5f)
                }
            }
            "worm" -> {
                val segs = 6
                for (i in segs - 1 downTo 0) {
                    val t = i / (segs - 1f)
                    val x = 38f + 26f * sin(t * 2.6f)
                    val y = 30f + t * 56f
                    val rad = (12f - t * 6f) * headScale
                    circle(x, y, rad)
                }
                // boca circular
                circle(38f - 4f, 30f, 6f * headScale, shade, detail)
            }
            "bat" -> {
                oval(50f, 58f, 9f * w, 14f)
                circle(50f, 38f, 8f * headScale)
                shape(path { m(44f, 34f); l(42f, 24f); l(48f, 31f); close() })
                shape(path { m(56f, 34f); l(58f, 24f); l(52f, 31f); close() })
                strokeOnly(path { m(46f, 72f); l(44f, 80f); m(54f, 72f); l(56f, 80f) })
            }
            "bird" -> {
                // cola
                shape(path { m(32f, 60f); l(16f, 70f); l(20f, 62f); l(14f, 58f); l(32f, 54f); close() })
                oval(48f, 58f, 18f * w, 13f)
                circle(64f, 40f, 8f * headScale)
                strokeOnly(path { m(56f, 48f); q(60f, 46f, 62f, 47f) })
                shape(path { m(71f, 38f); l(82f, 41f); l(71f, 44f); close() }, detail, line)
                strokeOnly(path { m(44f, 70f); l(42f, 82f); l(38f, 84f); m(42f, 82f); l(45f, 84f) })
                strokeOnly(path { m(54f, 70f); l(56f, 82f); l(52f, 84f); m(56f, 82f); l(59f, 84f) })
                // ala plegada
                strokeOnly(path { m(40f, 52f); q(50f, 50f, 60f, 60f) }, detail, strokeW * 0.6f)
            }
            "hound", "goat" -> {
                val angular = arch == "hound"
                if (angular) {
                    shape(path { m(28f, 52f); l(40f, 46f); l(62f, 46f); l(70f, 52f); l(66f, 64f); l(32f, 64f); close() })
                } else oval(48f, 56f, 22f * w, 11f)
                // patas
                listOf(32f, 40f, 58f, 64f).forEach { x ->
                    if (angular) strokeOnly(path { m(x, 62f); l(x - 3f, 72f); l(x + 1f, 82f) }, line, strokeW * 1.7f)
                    else strokeOnly(path { m(x, 62f); l(x, 82f) }, line, strokeW * 1.7f)
                }
                // cola
                strokeOnly(path { m(28f, 52f); if (angular) { l(18f, 44f); l(14f, 48f) } else q(20f, 50f, 22f, 44f) })
                // cuello y cabeza
                shape(path { m(62f, 48f); l(68f, 40f); l(74f, 46f); l(70f, 54f); close() })
                if (angular) {
                    shape(path { m(66f, 38f); l(76f, 36f); l(88f, 46f); l(80f, 52f); l(68f, 50f); close() })
                } else {
                    oval(74f, 44f, 9f * headScale, 7f * headScale)
                    shape(path { m(80f, 44f); l(88f, 48f); l(80f, 50f); close() })
                }
                shape(path { m(70f, 38f); l(70f, 30f); l(74f, 37f); close() })
            }
            "cat" -> {
                shape(path { m(36f, 86f); c(30f, 70f, 36f, 52f, 50f, 50f); c(64f, 52f, 70f, 70f, 64f, 86f); close() })
                strokeOnly(path { m(64f, 84f); c(80f, 84f, 82f, 70f, 74f, 64f) })
                val hr = 12f * headScale
                circle(50f, 36f, hr)
                shape(path { m(40f, 30f); l(38f, 36f - hr * 1.5f); l(46f, 26f); close() })
                shape(path { m(60f, 30f); l(62f, 36f - hr * 1.5f); l(54f, 26f); close() })
                strokeOnly(path { m(40f, 42f); l(30f, 40f); m(40f, 44f); l(30f, 46f); m(60f, 42f); l(70f, 40f); m(60f, 44f); l(70f, 46f) }, detail, strokeW * 0.4f)
            }
            "rat" -> {
                strokeOnly(path { m(30f, 62f); c(16f, 66f, 14f, 50f, 22f, 44f) }, line, strokeW * 0.8f)
                shape(path { m(30f, 62f); c(30f, 50f, 46f, 44f, 60f, 50f); l(80f, 56f); l(60f, 64f); c(50f, 68f, 36f, 68f, 30f, 62f); close() })
                circle(62f, 44f, 5f * headScale)
                strokeOnly(path { m(40f, 66f); l(38f, 72f); m(54f, 66f); l(56f, 72f) })
            }
            "serpent" -> {
                val body = path { m(50f, 26f); c(76f, 34f, 70f, 50f, 50f, 54f); c(28f, 58f, 26f, 74f, 50f, 78f); c(64f, 80f, 70f, 86f, 64f, 90f) }
                scope.drawPath(body, line, style = Stroke(width = r(13f * w), cap = StrokeCap.Round))
                scope.drawPath(body, fill, style = Stroke(width = r(13f * w) - strokeW * 2, cap = StrokeCap.Round))
                strokeOnly(body, detail.copy(alpha = 0.5f), strokeW * 0.5f)
                shape(path { m(50f, 12f); l(60f, 20f); l(56f, 28f); l(44f, 28f); l(40f, 20f); close() })
                strokeOnly(path { m(50f, 28f); l(50f, 33f); l(47f, 36f); m(50f, 33f); l(53f, 36f) }, Color(0xFFE57373), strokeW * 0.5f)
            }
            "insect" -> {
                // patas
                for (i in 0..2) {
                    val y = 48f + i * 8f
                    strokeOnly(path { m(44f, y); l(32f, y - 4f); l(26f, y + 6f) })
                    strokeOnly(path { m(56f, y); l(68f, y - 4f); l(74f, y + 6f) })
                }
                oval(50f, 66f, 11f * w, 16f)
                for (i in 0..2) strokeOnly(path { m(40f, 62f + i * 6f); q(50f, 65f + i * 6f, 60f, 62f + i * 6f) }, detail, strokeW * 0.5f)
                oval(50f, 46f, 8f * w, 7f)
                // cabeza ovalada con pliegues (como los Mi-Go)
                oval(50f, 30f, 9f * headScale, 8f * headScale)
                for (i in 0..1) strokeOnly(path { m(43f, 28f + i * 4f); q(50f, 25f + i * 4f, 57f, 28f + i * 4f) }, detail, strokeW * 0.5f)
                strokeOnly(path { m(46f, 22f); q(42f, 14f, 38f, 12f); m(54f, 22f); q(58f, 14f, 62f, 12f) })
            }
            "squid" -> {
                if (spec.tentacles == 0) tentacles(anchorsFor("squid"), 6)
                shape(path { m(50f, 18f); c(72f, 18f, 72f, 50f, 64f, 58f); l(36f, 58f); c(28f, 50f, 28f, 18f, 50f, 18f); close() })
                strokeOnly(path { m(40f, 30f); q(50f, 26f, 60f, 30f) }, detail, strokeW * 0.5f)
            }
            "jelly" -> {
                val n = if (spec.tentacles > 0) 0 else 5
                for (i in 0 until n) {
                    val x = 38f + i * 6f
                    strokeOnly(path { m(x, 48f); c(x - 4f, 60f, x + 4f, 70f, x - 2f, 86f) }, detail, strokeW * 0.6f)
                }
                shape(path { m(30f, 48f); c(30f, 22f, 70f, 22f, 70f, 48f); q(65f, 52f, 60f, 48f); q(55f, 52f, 50f, 48f); q(45f, 52f, 40f, 48f); q(35f, 52f, 30f, 48f); close() })
            }
            "tree" -> {
                // raíces y tronco retorcido
                shape(path { m(42f, 90f); c(44f, 74f, 40f, 60f, 44f, 44f); l(56f, 44f); c(60f, 60f, 56f, 74f, 58f, 90f); l(66f, 92f); l(34f, 92f); close() })
                listOf(-1f, 1f).forEach { s ->
                    strokeOnly(path { m(50f + s * 4f, 46f); c(50f + s * 14f, 36f, 50f + s * 26f, 34f, 50f + s * 30f, 22f) })
                    strokeOnly(path { m(50f + s * 6f, 52f); c(50f + s * 18f, 50f, 50f + s * 28f, 56f, 50f + s * 34f, 50f) })
                }
                // copa de zarcillos
                shape(smoothClosed(listOf(Offset(30f, 34f), Offset(40f, 18f), Offset(56f, 14f), Offset(70f, 26f), Offset(68f, 40f), Offset(50f, 46f), Offset(34f, 44f))))
            }
            "eye" -> {
                shape(path { m(18f, 50f); c(30f, 28f, 70f, 28f, 82f, 50f); c(70f, 72f, 30f, 72f, 18f, 50f); close() })
                circle(50f, 50f, 14f, lerp(fill, eyeColor, 0.35f), detail)
                circle(50f, 50f, 6f, Color(0xFF050505), null)
                scope.drawCircle(Color.White.copy(alpha = 0.7f), r(2f), p(46f, 46f))
                // pestañas
                for (i in 0..6) {
                    val x = 26f + i * 8f
                    strokeOnly(path { m(x, 38f - abs(3 - i) * -1.5f); l(x - 2f, 30f + abs(3 - i) * 2f) }, line, strokeW * 0.6f)
                }
            }
            "flame" -> {
                shape(path { m(50f, 14f); c(64f, 34f, 76f, 48f, 70f, 66f); c(66f, 80f, 34f, 80f, 30f, 66f); c(26f, 50f, 40f, 40f, 50f, 14f); close() })
                shape(path { m(50f, 38f); c(58f, 50f, 62f, 58f, 58f, 68f); c(54f, 74f, 46f, 74f, 42f, 68f); c(38f, 58f, 44f, 50f, 50f, 38f); close() }, lerp(fill, eyeColor, 0.3f), detail)
            }
            "crystal" -> {
                shape(path { m(50f, 12f); l(70f, 34f); l(64f, 80f); l(50f, 90f); l(36f, 80f); l(30f, 34f); close() })
                strokeOnly(path { m(50f, 12f); l(50f, 90f); m(30f, 34f); l(50f, 46f); l(70f, 34f); m(36f, 80f); l(50f, 46f); l(64f, 80f) }, detail, strokeW * 0.5f)
            }
            "star" -> {
                val pts = (0 until 16).map { i ->
                    val ang = -PI / 2 + PI * i / 8
                    val rad = if (i % 2 == 0) 34f else 14f
                    Offset(50f + (cos(ang) * rad).toFloat(), 50f + (sin(ang) * rad).toFloat())
                }
                shape(path { m(pts[0].x, pts[0].y); pts.drop(1).forEach { l(it.x, it.y) }; close() })
                circle(50f, 50f, 12f, lerp(fill, tint, 0.25f), detail)
            }
            "cone" -> {
                shape(path { m(50f, 32f); l(70f, 88f); q(50f, 94f, 30f, 88f); close() })
                for (i in 1..3) strokeOnly(path { m(50f - i * 5f, 40f + i * 14f); q(50f, 44f + i * 14f, 50f + i * 5f, 40f + i * 14f) }, detail, strokeW * 0.5f)
                // tres apéndices: cabeza, pinza y trompeta
                strokeOnly(path { m(50f, 34f); q(50f, 26f, 50f, 22f) })
                circle(50f, 22f, 7f)
                strokeOnly(path { m(48f, 36f); q(34f, 30f, 28f, 22f) })
                shape(path { m(28f, 22f); l(22f, 18f); l(26f, 26f); close() })
                strokeOnly(path { m(52f, 36f); q(66f, 30f, 72f, 22f) })
                shape(path { m(72f, 22f); l(78f, 16f); l(80f, 24f); close() })
            }
            "barrel" -> {
                shape(path { m(40f, 34f); c(30f, 46f, 30f, 66f, 40f, 78f); l(60f, 78f); c(70f, 66f, 70f, 46f, 60f, 34f); close() })
                for (x in listOf(43f, 50f, 57f)) strokeOnly(path { m(x, 36f); q(x + (x - 50f) * 0.4f, 56f, x, 76f) }, detail, strokeW * 0.5f)
                // cabeza de estrella de cinco puntas
                val pts = (0 until 10).map { i ->
                    val ang = -PI / 2 + PI * i / 5
                    val rad = if (i % 2 == 0) 12f else 5f
                    Offset(50f + (cos(ang) * rad).toFloat(), 24f + (sin(ang) * rad).toFloat())
                }
                shape(path { m(pts[0].x, pts[0].y); pts.drop(1).forEach { l(it.x, it.y) }; close() })
                // patas de estrella abajo
                strokeOnly(path { m(44f, 78f); l(38f, 90f); m(50f, 78f); l(50f, 92f); m(56f, 78f); l(62f, 90f) })
            }
            "mask" -> {
                shape(path { m(50f, 22f); c(70f, 22f, 74f, 40f, 72f, 52f); c(70f, 66f, 60f, 76f, 50f, 78f); c(40f, 76f, 30f, 66f, 28f, 52f); c(26f, 40f, 30f, 22f, 50f, 22f); close() })
                strokeOnly(path { m(36f, 40f); q(42f, 36f, 46f, 40f); m(54f, 40f); q(58f, 36f, 64f, 40f) }, detail, strokeW * 0.6f)
                strokeOnly(path { m(42f, 62f); q(50f, 66f, 58f, 62f) }, detail, strokeW * 0.7f)
                strokeOnly(path { m(50f, 46f); l(48f, 54f); l(52f, 54f) }, detail, strokeW * 0.5f)
            }
            "book" -> {
                shape(path { m(50f, 36f); c(40f, 30f, 26f, 30f, 18f, 34f); l(18f, 72f); c(26f, 68f, 40f, 68f, 50f, 74f); c(60f, 68f, 74f, 68f, 82f, 72f); l(82f, 34f); c(74f, 30f, 60f, 30f, 50f, 36f); close() })
                strokeOnly(path { m(50f, 36f); l(50f, 74f) }, line)
                for (i in 0..3) {
                    val y = 42f + i * 6f
                    strokeOnly(path { m(24f, y); q(36f, y - 2f, 46f, y + 1f) }, detail, strokeW * 0.4f)
                    strokeOnly(path { m(54f, y + 1f); q(64f, y - 2f, 76f, y) }, detail, strokeW * 0.4f)
                }
            }
            "swarm" -> {
                repeat(34) {
                    val ang = rnd.nextFloat() * 2 * PI
                    val rad = rnd.nextFloat() * 30f
                    val x = 50f + (cos(ang) * rad).toFloat()
                    val y = 52f + (sin(ang) * rad * 0.8f).toFloat()
                    scope.drawCircle(if (it % 3 == 0) detail else line, r(1.4f + rnd.nextFloat() * 1.6f), p(x, y))
                }
            }
            "golem" -> {
                shape(path { m(34f, 36f); l(66f, 36f); l(62f, 70f); l(38f, 70f); close() })
                shape(path { m(34f, 38f); l(24f, 40f); l(22f, 66f); l(30f, 66f); l(32f, 44f); close() })
                shape(path { m(66f, 38f); l(76f, 40f); l(78f, 66f); l(70f, 66f); l(68f, 44f); close() })
                shape(path { m(40f, 70f); l(40f, 88f); l(48f, 88f); l(48f, 70f); close() })
                shape(path { m(52f, 70f); l(52f, 88f); l(60f, 88f); l(60f, 70f); close() })
                strokeOnly(path { m(42f, 48f); l(48f, 52f); l(46f, 60f); m(56f, 44f); l(58f, 54f) }, detail, strokeW * 0.5f)
                val hr = 9f * headScale
                shape(path { m(50f - hr, 26f - hr); l(50f + hr, 26f - hr); l(50f + hr, 26f + hr * 0.9f); l(50f - hr, 26f + hr * 0.9f); close() })
            }
            "colossus" -> {
                // brazos larguísimos que llegan al suelo
                strokeOnly(path { m(24f, 40f); c(14f, 54f, 12f, 70f, 14f, 84f) }, line, strokeW * 3.6f)
                strokeOnly(path { m(24f, 40f); c(14f, 54f, 12f, 70f, 14f, 84f) }, fill, strokeW * 2.2f)
                strokeOnly(path { m(76f, 40f); c(86f, 54f, 88f, 70f, 86f, 84f) }, line, strokeW * 3.6f)
                strokeOnly(path { m(76f, 40f); c(86f, 54f, 88f, 70f, 86f, 84f) }, fill, strokeW * 2.2f)
                circle(14f, 86f, 4.5f)
                circle(86f, 86f, 4.5f)
                // torso enorme y encorvado con la cabeza hundida entre los hombros
                shape(path { m(50f, 24f); c(32f, 24f, 20f, 30f, 22f, 46f); c(24f, 58f, 32f, 70f, 36f, 74f); l(34f, 92f); l(44f, 92f); l(46f, 76f); l(54f, 76f); l(56f, 92f); l(66f, 92f); l(64f, 74f); c(68f, 70f, 76f, 58f, 78f, 46f); c(80f, 30f, 68f, 24f, 50f, 24f); close() })
                strokeOnly(path { m(36f, 48f); q(50f, 56f, 64f, 48f); m(40f, 60f); q(50f, 64f, 60f, 60f) }, detail, strokeW * 0.6f)
                circle(50f, 30f, 8f * headScale)
            }
            "planet" -> {
                circle(50f, 50f, 24f)
                for (i in -1..1) strokeOnly(path { m(28f, 50f + i * 9f); q(50f, 54f + i * 9f, 72f, 50f + i * 9f) }, detail, strokeW * 0.5f)
            }
            "vortex" -> {
                for (arm in 0..2) {
                    val pathArm = path {
                        val start = arm * 2 * PI / 3
                        for (k in 0..40) {
                            val t = k / 40.0
                            val ang = start + t * 3.4 * PI
                            val rad = 4 + t * 34
                            val x = 50f + (cos(ang) * rad).toFloat()
                            val y = 50f + (sin(ang) * rad * 0.8).toFloat()
                            if (k == 0) m(x, y) else l(x, y)
                        }
                    }
                    strokeOnly(pathArm, if (arm == 0) detail else line, strokeW * 1.4f)
                }
                circle(50f, 50f, 9f, shade, line)
            }
            "wraith" -> {
                shape(path { m(50f, 18f); c(66f, 18f, 68f, 40f, 66f, 56f); c(66f, 70f, 72f, 80f, 74f, 90f); l(66f, 84f); l(60f, 92f); l(54f, 84f); l(48f, 92f); l(42f, 84f); l(36f, 92f); l(30f, 84f); l(26f, 90f); c(28f, 80f, 34f, 70f, 34f, 56f); c(32f, 40f, 34f, 18f, 50f, 18f); close() })
                oval(50f, 32f, 9f * headScale, 10f * headScale, shade, line)
                strokeOnly(path { m(34f, 50f); q(26f, 58f, 22f, 70f); m(66f, 50f); q(74f, 58f, 78f, 70f) }, line)
            }
            "toad" -> {
                shape(path { m(24f, 74f); c(20f, 50f, 34f, 36f, 50f, 36f); c(66f, 36f, 80f, 50f, 76f, 74f); c(64f, 82f, 36f, 82f, 24f, 74f); close() })
                strokeOnly(path { m(30f, 74f); l(20f, 82f); l(28f, 84f); m(70f, 74f); l(80f, 82f); l(72f, 84f) })
                strokeOnly(path { m(32f, 60f); q(50f, 68f, 68f, 60f) }, detail, strokeW * 0.8f)
                circle(38f, 38f, 6f)
                circle(62f, 38f, 6f)
            }
            "spider" -> {
                for (i in 0..3) {
                    val y = 50f + i * 5f
                    val spread = 18f + i * 4f
                    strokeOnly(path { m(46f, y); l(50f - spread, y - 14f + i * 3f); l(50f - spread - 6f, y + 16f) })
                    strokeOnly(path { m(54f, y); l(50f + spread, y - 14f + i * 3f); l(50f + spread + 6f, y + 16f) })
                }
                oval(50f, 66f, 14f * w, 14f)
                strokeOnly(path { m(44f, 64f); l(50f, 70f); l(56f, 64f); m(50f, 58f); l(50f, 74f) }, Color(0xFFE57373), strokeW * 0.6f)
                circle(50f, 46f, 9f)
            }
            "mouth" -> {
                circle(50f, 54f, 28f)
                circle(50f, 54f, 18f, Color(0xFF080306), detail)
                for (i in 0 until 14) {
                    val ang = 2 * PI * i / 14
                    val ox = 50f + (cos(ang) * 18f).toFloat()
                    val oy = 54f + (sin(ang) * 18f).toFloat()
                    val ix = 50f + (cos(ang) * 11f).toFloat()
                    val iy = 54f + (sin(ang) * 11f).toFloat()
                    val px = (-sin(ang) * 2.5f).toFloat()
                    val py = (cos(ang) * 2.5f).toFloat()
                    shape(path { m(ox - px, oy - py); l(ix, iy); l(ox + px, oy + py); close() }, Color(0xFFEDE7D9), detail, strokeW * 0.3f)
                }
            }
            "crab" -> {
                for (i in 0..2) {
                    strokeOnly(path { m(36f, 60f + i * 4f); l(22f, 66f + i * 4f); l(18f, 78f + i * 2f) })
                    strokeOnly(path { m(64f, 60f + i * 4f); l(78f, 66f + i * 4f); l(82f, 78f + i * 2f) })
                }
                strokeOnly(path { m(34f, 54f); l(22f, 44f) })
                strokeOnly(path { m(66f, 54f); l(78f, 44f) })
                shape(path { m(22f, 44f); c(10f, 40f, 12f, 28f, 22f, 30f); l(18f, 36f); l(26f, 38f); close() })
                shape(path { m(78f, 44f); c(90f, 40f, 88f, 28f, 78f, 30f); l(82f, 36f); l(74f, 38f); close() })
                shape(path { m(28f, 58f); c(28f, 42f, 72f, 42f, 72f, 58f); c(66f, 66f, 34f, 66f, 28f, 58f); close() })
                strokeOnly(path { m(44f, 46f); l(42f, 38f); m(56f, 46f); l(58f, 38f) })
            }
            "fish" -> {
                shape(path { m(24f, 50f); l(10f, 38f); l(14f, 50f); l(10f, 62f); close() })
                shape(path { m(24f, 50f); c(36f, 30f, 64f, 30f, 80f, 50f); c(64f, 70f, 36f, 70f, 24f, 50f); close() })
                shape(path { m(46f, 36f); l(52f, 24f); l(58f, 36f); close() })
                strokeOnly(path { m(60f, 40f); q(56f, 50f, 60f, 60f) }, detail, strokeW * 0.6f)
            }
            else -> circle(50f, 50f, 24f)
        }
    }

    // ------------------------------------------------------------------ rasgos
    fun eyes(a: Anchors) {
        val n = spec.eyes
        if (n <= 0) return
        val glowColor = eyeColor
        fun eye(x: Float, y: Float, size: Float) {
            scope.drawCircle(Brush.radialGradient(listOf(glowColor.copy(alpha = 0.55f), Color.Transparent), center = p(x, y), radius = r(size * 2.4f)), r(size * 2.4f), p(x, y))
            scope.drawCircle(lerp(glowColor, Color.White, 0.45f), r(size), p(x, y))
            scope.drawOval(Color(0xFF0A0A0A), p(x - size * 0.25f, y - size * 0.8f), Size(r(size * 0.5f), r(size * 1.6f)))
        }
        val (cx, cy) = if (a.eyesOnBody) a.body else a.head
        val radius = if (a.eyesOnBody) a.bodyR * 0.7f else a.headR * 0.55f
        val base = when {
            n == 1 -> listOf(Offset(cx, cy))
            n == 2 && spec.archetype == "fishman" -> listOf(Offset(cx - a.headR * 0.85f, cy - a.headR * 0.25f), Offset(cx + a.headR * 0.85f, cy - a.headR * 0.25f))
            n == 2 -> listOf(Offset(cx - radius * 0.6f, cy - radius * 0.1f), Offset(cx + radius * 0.6f, cy - radius * 0.1f))
            n == 3 -> listOf(Offset(cx - radius * 0.6f, cy), Offset(cx + radius * 0.6f, cy), Offset(cx, cy - radius * 0.6f))
            n <= 6 -> (0 until n).map { i ->
                val row = i / 3
                val col = i % 3
                Offset(cx + (col - 1) * radius * 0.6f, cy - radius * 0.35f + row * radius * 0.6f)
            }
            else -> (0 until n).map {
                val ang = rnd.nextFloat() * 2 * PI
                val rad = radius * (0.2f + rnd.nextFloat() * 0.8f)
                Offset(cx + (cos(ang) * rad).toFloat(), cy + (sin(ang) * rad * 0.8f).toFloat())
            }
        }
        val size = when {
            spec.archetype == "eye" -> 2.4f
            n <= 2 && spec.archetype == "fishman" -> (a.headR * 0.34f).coerceIn(2.6f, 4.6f)
            n <= 2 -> (a.headR * 0.27f).coerceIn(2.0f, 4.2f)
            n <= 6 -> 2.0f
            else -> 1.6f + rnd.nextFloat()
        }
        if (spec.archetype == "eye") {
            // ojos menores alrededor del gran ojo
            (0 until n).forEach { i ->
                val ang = -PI / 2 + 2 * PI * i / max(1, n) + 0.4
                eye(50f + (cos(ang) * 34f).toFloat(), 50f + (sin(ang) * 30f).toFloat(), size)
            }
        } else base.forEach { eye(it.x, it.y, size) }
    }

    fun tentacles(a: Anchors, count: Int = spec.tentacles) {
        val n = count.coerceIn(1, 10)
        for (i in 0 until n) {
            val t = if (n == 1) 0.5f else i / (n - 1f)
            val x0 = a.tentacleRoot.x - a.tentacleSpan / 2 + t * a.tentacleSpan
            val y0 = a.tentacleRoot.y
            val dir = (t - 0.5f) * 2f
            val curl = (rnd.nextFloat() - 0.5f) * 18f
            val len = 26f + rnd.nextFloat() * 10f
            val endX = (x0 + dir * 14f + curl).coerceIn(4f, 96f)
            val endY = (y0 + len).coerceAtMost(98f)
            val tentacle = path { m(x0, y0); c(x0 + dir * 4f, y0 + len * 0.4f, endX - curl, y0 + len * 0.6f, endX, endY) }
            scope.drawPath(tentacle, line, style = Stroke(width = r(4.2f), cap = StrokeCap.Round))
            scope.drawPath(tentacle, fill, style = Stroke(width = r(4.2f) - strokeW, cap = StrokeCap.Round))
            // ventosas
            scope.drawCircle(detail, r(0.9f), p((x0 + endX) / 2f, (y0 + endY) / 2f))
        }
    }

    fun wings(a: Anchors) {
        val (x, y) = a.wingRoot
        val span = a.wingSpan
        listOf(-1f, 1f).forEach { s ->
            val tip = Offset(x + s * span, y - span * 0.55f)
            val wing = path {
                m(x + s * 4f, y)
                l(tip.x, tip.y)
                q(x + s * span * 0.85f, y + span * 0.05f, x + s * span * 0.95f, y + span * 0.25f)
                q(x + s * span * 0.65f, y + span * 0.05f, x + s * span * 0.6f, y + span * 0.3f)
                q(x + s * span * 0.4f, y + span * 0.12f, x + s * span * 0.3f, y + span * 0.32f)
                q(x + s * span * 0.2f, y + span * 0.15f, x + s * 4f, y + 8f)
                close()
            }
            shape(wing, shade, line)
            strokeOnly(path { m(x + s * 4f, y + 2f); l(x + s * span * 0.6f, y + span * 0.3f) }, detail, strokeW * 0.4f)
            strokeOnly(path { m(x + s * 4f, y + 2f); l(x + s * span * 0.95f, y + span * 0.25f) }, detail, strokeW * 0.4f)
        }
    }

    fun horns(a: Anchors) {
        val (x, y) = a.head
        val hr = a.headR
        listOf(-1f, 1f).forEach { s ->
            shape(path {
                m(x + s * hr * 0.4f, y - hr * 0.7f)
                q(x + s * hr * 1.6f, y - hr * 1.4f, x + s * hr * 1.2f, y - hr * 2.4f)
                q(x + s * hr * 1.1f, y - hr * 1.5f, x + s * hr * 0.8f, y - hr * 0.5f)
                close()
            }, detail, line, strokeW * 0.7f)
        }
    }

    fun crown(a: Anchors) {
        val (x, y) = a.head
        val hr = a.headR
        val top = y - hr - 2f
        val crown = path {
            m(x - hr * 0.9f, top)
            l(x - hr * 0.9f, top - 5f); l(x - hr * 0.45f, top - 2f); l(x, top - 8f)
            l(x + hr * 0.45f, top - 2f); l(x + hr * 0.9f, top - 5f); l(x + hr * 0.9f, top)
            close()
        }
        shape(crown, Color(0xFFFFD54F).copy(alpha = 0.9f), Color(0xFFFFA000), strokeW * 0.6f)
        scope.drawCircle(eyeColor, r(1.2f), p(x, top - 3f))
    }

    fun hood(a: Anchors) {
        val (x, y) = a.head
        val hr = a.headR
        shape(path {
            m(x - hr * 1.5f, y + hr * 1.3f)
            c(x - hr * 1.6f, y - hr * 0.6f, x - hr * 0.6f, y - hr * 1.8f, x, y - hr * 2.2f)
            c(x + hr * 0.6f, y - hr * 1.8f, x + hr * 1.6f, y - hr * 0.6f, x + hr * 1.5f, y + hr * 1.3f)
            c(x + hr * 0.8f, y + hr * 0.4f, x - hr * 0.8f, y + hr * 0.4f, x - hr * 1.5f, y + hr * 1.3f)
            close()
        }, shade, line)
        oval(x, y + hr * 0.1f, hr * 0.85f, hr * 0.9f, Color(0xFF050505), shade)
    }

    fun spikes(a: Anchors) {
        val (bx, by) = a.body
        val br = a.bodyR
        for (i in 0 until 7) {
            val ang = PI + PI * (i + 0.5) / 7
            val x = bx + (cos(ang) * br).toFloat()
            val y = by + (sin(ang) * br * 0.9f).toFloat()
            val ox = (cos(ang) * 7f).toFloat()
            val oy = (sin(ang) * 7f).toFloat()
            val px = (-sin(ang) * 2.2f).toFloat()
            val py = (cos(ang) * 2.2f).toFloat()
            shape(path { m(x - px, y - py); l(x + ox, y + oy); l(x + px, y + py); close() }, detail, line, strokeW * 0.5f)
        }
    }

    fun fangs(a: Anchors) {
        val (x, y) = a.mouth
        listOf(-2.2f, 2.2f).forEach { dx ->
            shape(path { m(x + dx - 1.4f, y); l(x + dx, y + 5f); l(x + dx + 1.4f, y); close() }, Color(0xFFF5F0E6), line, strokeW * 0.4f)
        }
    }

    fun extraMouths(a: Anchors) {
        val (bx, by) = a.body
        repeat(3) {
            val x = bx + (rnd.nextFloat() - 0.5f) * a.bodyR * 1.2f
            val y = by + (rnd.nextFloat() - 0.2f) * a.bodyR * 0.9f
            oval(x, y, 4f, 2.2f, Color(0xFF120508), detail)
            strokeOnly(path { m(x - 3f, y); l(x - 2f, y + 1.2f); l(x - 1f, y); l(x, y + 1.2f); l(x + 1f, y); l(x + 2f, y + 1.2f); l(x + 3f, y) }, Color(0xFFEDE7D9), strokeW * 0.3f)
        }
    }

    fun drips(a: Anchors) {
        val (bx, by) = a.body
        repeat(4) {
            val x = bx + (rnd.nextFloat() - 0.5f) * a.bodyR * 1.6f
            val y = by + a.bodyR * 0.6f + rnd.nextFloat() * 8f
            shape(path { m(x, y); c(x + 2f, y + 3f, x + 2f, y + 6f, x, y + 7f); c(x - 2f, y + 6f, x - 2f, y + 3f, x, y); close() }, lerp(tint, Color.Black, 0.3f), line, strokeW * 0.3f)
        }
    }

    fun ringsBack(a: Anchors) {
        val (bx, by) = a.body
        val rx = a.bodyR + 16f
        scope.drawArc(detail, 180f, 180f, false, p(bx - rx, by - 7f), Size(r(rx * 2), r(14f)), style = Stroke(width = strokeW * 0.8f))
    }

    fun ringsFront(a: Anchors) {
        val (bx, by) = a.body
        val rx = a.bodyR + 16f
        scope.drawArc(detail, 0f, 180f, false, p(bx - rx, by - 7f), Size(r(rx * 2), r(14f)), style = Stroke(width = strokeW * 0.8f))
    }

    /** El objeto que lleva en la mano, dibujado a partir de [hand]. */
    fun prop(name: String, hand: Offset) {
        val (x, y) = hand
        val glow = lerp(eyeColor, Color.White, 0.3f)
        val metal = lerp(detail, Color(0xFFB0BEC5), 0.4f)
        when (name) {
            "staff" -> {
                strokeOnly(path { m(x, y - 34f); l(x + 2f, y + 22f) }, Color(0xFF8D6E63), strokeW * 1.4f)
                scope.drawCircle(glow, r(3.6f), p(x, y - 36f))
            }
            "scepter" -> {
                strokeOnly(path { m(x, y - 16f); l(x + 1f, y + 12f) }, Color(0xFFFFC107), strokeW * 1.2f)
                shape(path { m(x - 4f, y - 18f); l(x, y - 26f); l(x + 4f, y - 18f); l(x, y - 14f); close() }, Color(0xFFFFD54F), Color(0xFFFFA000), strokeW * 0.6f)
            }
            "trident" -> {
                strokeOnly(path { m(x, y - 30f); l(x + 2f, y + 22f) }, metal, strokeW * 1.2f)
                strokeOnly(path { m(x - 6f, y - 38f); l(x - 6f, y - 30f); l(x + 6f, y - 30f); l(x + 6f, y - 38f); m(x, y - 40f); l(x, y - 30f) }, metal, strokeW)
            }
            "book" -> shape(path { m(x - 6f, y - 4f); l(x + 6f, y - 6f); l(x + 7f, y + 6f); l(x - 5f, y + 8f); close() }, Color(0xFF4E342E), detail, strokeW * 0.7f)
            "lantern" -> {
                strokeOnly(path { m(x, y); l(x, y + 4f) }, metal, strokeW * 0.6f)
                shape(path { m(x - 4f, y + 4f); l(x + 4f, y + 4f); l(x + 5f, y + 14f); l(x - 5f, y + 14f); close() }, glow.copy(alpha = 0.85f), metal, strokeW * 0.6f)
                scope.drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = 0.5f), Color.Transparent), center = p(x, y + 9f), radius = r(14f)), r(14f), p(x, y + 9f))
            }
            "dagger" -> {
                shape(path { m(x - 1.5f, y); l(x, y - 16f); l(x + 1.5f, y); close() }, metal, line, strokeW * 0.4f)
                strokeOnly(path { m(x - 4f, y); l(x + 4f, y); m(x, y); l(x, y + 5f) }, Color(0xFF8D6E63), strokeW * 0.8f)
            }
            "flask" -> {
                shape(path { m(x - 1.5f, y - 10f); l(x + 1.5f, y - 10f); l(x + 1.5f, y - 5f); l(x + 6f, y + 4f); q(x, y + 8f, x - 6f, y + 4f); l(x - 1.5f, y - 5f); close() }, glow.copy(alpha = 0.7f), detail, strokeW * 0.6f)
            }
            "syringe" -> {
                shape(path { m(x - 2f, y - 10f); l(x + 2f, y - 10f); l(x + 2f, y + 4f); l(x - 2f, y + 4f); close() }, Color(0xFF9CCC65), detail, strokeW * 0.5f)
                strokeOnly(path { m(x, y + 4f); l(x, y + 10f); m(x - 3f, y - 10f); l(x + 3f, y - 10f); m(x, y - 10f); l(x, y - 14f) }, metal, strokeW * 0.6f)
            }
            "orb" -> {
                scope.drawCircle(Brush.radialGradient(listOf(glow, glow.copy(alpha = 0f)), center = p(x, y - 4f), radius = r(9f)), r(9f), p(x, y - 4f))
                circle(x, y - 4f, 4.5f, glow.copy(alpha = 0.8f), detail)
            }
            "candle" -> {
                shape(path { m(x - 2.5f, y - 6f); l(x + 2.5f, y - 6f); l(x + 2.5f, y + 6f); l(x - 2.5f, y + 6f); close() }, Color(0xFFEDE7D9), detail, strokeW * 0.4f)
                shape(path { m(x, y - 13f); q(x + 3f, y - 9f, x, y - 7f); q(x - 3f, y - 9f, x, y - 13f); close() }, Color(0xFFFFB74D), Color(0xFFFFE082), strokeW * 0.3f)
            }
            "scroll" -> {
                shape(path { m(x - 6f, y - 6f); l(x + 6f, y - 6f); l(x + 6f, y + 6f); l(x - 6f, y + 6f); close() }, Color(0xFFD7CCC8), Color(0xFF8D6E63), strokeW * 0.5f)
                strokeOnly(path { m(x - 4f, y - 2f); l(x + 4f, y - 2f); m(x - 4f, y + 1f); l(x + 3f, y + 1f) }, Color(0xFF5D4037), strokeW * 0.3f)
            }
            "key" -> {
                scope.drawCircle(Color(0xFFCFD8DC), r(3f), p(x, y - 8f), style = Stroke(width = strokeW * 0.8f))
                strokeOnly(path { m(x, y - 5f); l(x, y + 8f); m(x, y + 4f); l(x + 3f, y + 4f); m(x, y + 7f); l(x + 3f, y + 7f) }, Color(0xFFCFD8DC), strokeW * 0.8f)
            }
            "mirror" -> {
                oval(x, y - 6f, 4.5f, 6f, glow.copy(alpha = 0.35f), Color(0xFFFFD54F))
                strokeOnly(path { m(x, y); l(x, y + 8f) }, Color(0xFFFFD54F), strokeW * 0.8f)
            }
            "broom" -> {
                strokeOnly(path { m(x - 10f, y - 24f); l(x + 6f, y + 14f) }, Color(0xFF8D6E63), strokeW * 1.1f)
                shape(path { m(x + 4f, y + 10f); l(x + 14f, y + 22f); l(x + 4f, y + 24f); l(x, y + 14f); close() }, Color(0xFFBCAAA4), Color(0xFF8D6E63), strokeW * 0.5f)
            }
            "shovel" -> {
                strokeOnly(path { m(x, y - 20f); l(x, y + 10f) }, Color(0xFF8D6E63), strokeW * 1.1f)
                shape(path { m(x - 5f, y + 10f); l(x + 5f, y + 10f); l(x + 4f, y + 20f); l(x, y + 23f); l(x - 4f, y + 20f); close() }, metal, line, strokeW * 0.5f)
            }
            "flute" -> {
                strokeOnly(path { m(x - 8f, y - 10f); l(x + 6f, y + 4f) }, Color(0xFFD7CCC8), strokeW * 1.2f)
                for (i in 0..2) scope.drawCircle(Color(0xFF3E2723), r(0.7f), p(x - 4f + i * 3.5f, y - 6f + i * 3.5f))
            }
            "drum" -> {
                oval(x, y + 2f, 7f, 2.5f, Color(0xFFD7CCC8), line)
                shape(path { m(x - 7f, y + 2f); l(x - 7f, y + 12f); q(x, y + 16f, x + 7f, y + 12f); l(x + 7f, y + 2f); q(x, y + 6f, x - 7f, y + 2f); close() }, Color(0xFF6D4C41), line, strokeW * 0.6f)
            }
            "harp" -> {
                strokeOnly(path { m(x - 6f, y + 10f); q(x - 8f, y - 12f, x + 6f, y - 14f); l(x + 6f, y + 10f); close() }, Color(0xFFFFD54F), strokeW * 0.9f)
                for (i in 0..3) strokeOnly(path { m(x - 4f + i * 3f, y - 8f + i); l(x - 4f + i * 3f, y + 9f) }, Color(0xFFFFF8E1), strokeW * 0.25f)
            }
            "bell" -> {
                shape(path { m(x - 5f, y + 6f); q(x - 5f, y - 6f, x, y - 7f); q(x + 5f, y - 6f, x + 5f, y + 6f); close() }, Color(0xFFFFD54F), Color(0xFFFFA000), strokeW * 0.6f)
                scope.drawCircle(Color(0xFFFFA000), r(1.4f), p(x, y + 7f))
            }
            "net" -> {
                strokeOnly(path { m(x - 6f, y - 6f); l(x + 6f, y + 10f); m(x + 6f, y - 6f); l(x - 6f, y + 10f); m(x - 6f, y + 2f); l(x + 6f, y + 2f); m(x, y - 6f); l(x, y + 10f) }, detail, strokeW * 0.4f)
            }
            "quill" -> {
                shape(path { m(x, y + 6f); q(x + 2f, y - 8f, x + 8f, y - 16f); q(x + 4f, y - 4f, x, y + 6f); close() }, Color(0xFFEDE7D9), detail, strokeW * 0.4f)
            }
            "brush" -> {
                strokeOnly(path { m(x - 4f, y + 8f); l(x + 4f, y - 8f) }, Color(0xFF8D6E63), strokeW * 0.9f)
                scope.drawCircle(Color(0xFFE57373), r(2f), p(x + 4.5f, y - 9f))
            }
            "spear" -> {
                strokeOnly(path { m(x, y - 28f); l(x + 1f, y + 20f) }, Color(0xFF8D6E63), strokeW * 1.1f)
                shape(path { m(x - 2.5f, y - 28f); l(x, y - 36f); l(x + 2.5f, y - 28f); close() }, metal, line, strokeW * 0.4f)
            }
            else -> {}
        }
    }

    fun sparkles() {
        repeat(6) {
            val x = 12f + rnd.nextFloat() * 76f
            val y = 10f + rnd.nextFloat() * 80f
            val s = 1.2f + rnd.nextFloat() * 1.6f
            strokeOnly(path { m(x - s, y); l(x + s, y); m(x, y - s); l(x, y + s) }, detail.copy(alpha = 0.7f), strokeW * 0.4f)
        }
    }

    /** Un contorno cerrado y suave que pasa por los puntos (curvas cuadráticas por los puntos medios). */
    fun smoothClosed(pts: List<Offset>): Path = path {
        val n = pts.size
        val mid0 = Offset((pts[n - 1].x + pts[0].x) / 2, (pts[n - 1].y + pts[0].y) / 2)
        m(mid0.x, mid0.y)
        for (i in 0 until n) {
            val cur = pts[i]
            val next = pts[(i + 1) % n]
            q(cur.x, cur.y, (cur.x + next.x) / 2, (cur.y + next.y) / 2)
        }
        close()
    }
}
