package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.rotate
import com.EGomez.idleecosystem.domain.model.Pact
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** El color del sigilo de un pacto. */
fun Pact.sigilColor(): Color = Color(color)

/**
 * El sigilo de un dios: un símbolo propio, dibujado con trazos finos sobre un halo
 * de su color, con la estética de runas del juego. Ninguno copia un símbolo
 * existente; se inspiran en la forma que el mito le da a cada dios.
 *
 * [locked] dibuja solo la silueta del círculo con un signo de interrogación.
 */
@Composable
fun PactSigil(pact: Pact, modifier: Modifier = Modifier, locked: Boolean = false) {
    val color = if (locked) Color(0xFF555555) else pact.sigilColor()
    Canvas(modifier = modifier) {
        val s = size.minDimension
        val c = Offset(size.width / 2f, size.height / 2f)
        val stroke = Stroke(width = s * 0.035f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Halo y círculo ritual comunes a todos
        drawCircle(
            brush = Brush.radialGradient(listOf(color.copy(alpha = 0.28f), Color.Transparent), c, s * 0.5f),
            radius = s * 0.5f,
            center = c
        )
        drawCircle(color.copy(alpha = 0.55f), radius = s * 0.46f, center = c, style = Stroke(width = s * 0.015f))

        if (locked) {
            drawQuestion(c, s, color, stroke)
            return@Canvas
        }
        val g = Glyph(this, c, s, color, stroke)
        when (pact) {
            Pact.NONE -> g.openEye()
            Pact.CTHULHU -> g.tentacles()
            Pact.HASTUR -> g.yellowCrown()
            Pact.TULZSCHA -> g.greenFlame()
            Pact.UBBO_SATHLA -> g.primordialMass()
            Pact.ABHOTH -> g.spawningMass()
            Pact.NUG -> g.twinSpiral(clockwise = true)
            Pact.YEB -> g.twinSpiral(clockwise = false)
            Pact.DAOLOTH -> g.tornVeils()
            Pact.GHROTH -> g.harbingerPlanet()
            Pact.SHUB_NIGGURATH -> g.blackGoat()
            Pact.YOG_SOTHOTH -> g.spheresAndGate()
            Pact.NYARLATHOTEP -> g.threeLobedEye()
            Pact.AZATHOTH -> g.nuclearChaos()
        }
    }
}

private fun DrawScope.drawQuestion(c: Offset, s: Float, color: Color, stroke: Stroke) {
    val path = Path().apply {
        moveTo(c.x - s * 0.1f, c.y - s * 0.1f)
        cubicTo(c.x - s * 0.1f, c.y - s * 0.26f, c.x + s * 0.13f, c.y - s * 0.26f, c.x + s * 0.12f, c.y - s * 0.1f)
        cubicTo(c.x + s * 0.11f, c.y, c.x, c.y, c.x, c.y + s * 0.07f)
    }
    drawPath(path, color, style = stroke)
    drawCircle(color, radius = s * 0.025f, center = Offset(c.x, c.y + s * 0.17f))
}

/** Herramientas de dibujo en coordenadas relativas al tamaño del sigilo. */
private class Glyph(val scope: DrawScope, val c: Offset, val s: Float, val color: Color, val stroke: Stroke) {

    private fun p(x: Float, y: Float) = Offset(c.x + x * s, c.y + y * s)
    private val thin = Stroke(width = stroke.width * 0.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    private fun line(x1: Float, y1: Float, x2: Float, y2: Float, st: Stroke = stroke) =
        scope.drawLine(color, p(x1, y1), p(x2, y2), st.width, cap = StrokeCap.Round)

    private fun circle(x: Float, y: Float, r: Float, filled: Boolean = false, alpha: Float = 1f, st: Stroke = stroke) =
        scope.drawCircle(color.copy(alpha = alpha), radius = r * s, center = p(x, y), style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else st)

    private fun path(st: Stroke = stroke, block: Path.() -> Unit) = scope.drawPath(Path().apply(block), color, style = st)

    private fun Path.m(x: Float, y: Float) = moveTo(c.x + x * s, c.y + y * s)
    private fun Path.l(x: Float, y: Float) = lineTo(c.x + x * s, c.y + y * s)
    private fun Path.q(x1: Float, y1: Float, x2: Float, y2: Float) = quadraticBezierTo(c.x + x1 * s, c.y + y1 * s, c.x + x2 * s, c.y + y2 * s)
    private fun Path.cu(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
        cubicTo(c.x + x1 * s, c.y + y1 * s, c.x + x2 * s, c.y + y2 * s, c.x + x3 * s, c.y + y3 * s)

    private fun arc(x: Float, y: Float, r: Float, start: Float, sweep: Float, st: Stroke = stroke) =
        scope.drawArc(
            color, start, sweep, useCenter = false,
            topLeft = p(x - r, y - r), size = Size(r * 2 * s, r * 2 * s), style = st
        )

    /** Sin pacto: un ojo abierto, sereno, con la pupila entera. */
    fun openEye() {
        path { m(-0.3f, 0f); q(0f, -0.26f, 0.3f, 0f); q(0f, 0.26f, -0.3f, 0f) }
        circle(0f, 0f, 0.09f)
        circle(0f, 0f, 0.035f, filled = true)
    }

    /** Cthulhu: la cúpula de R'lyeh con tentáculos que caen enroscándose. */
    fun tentacles() {
        arc(0f, -0.04f, 0.2f, 180f, 180f)
        line(-0.2f, -0.04f, 0.2f, -0.04f, thin)
        val xs = listOf(-0.16f, -0.08f, 0f, 0.08f, 0.16f)
        xs.forEachIndexed { i, x ->
            val dir = if (i % 2 == 0) 1f else -1f
            path(thin) {
                m(x, -0.04f)
                cu(x + 0.06f * dir, 0.08f, x - 0.08f * dir, 0.16f, x + 0.03f * dir, 0.3f)
            }
        }
        circle(-0.07f, -0.12f, 0.02f, filled = true)
        circle(0.07f, -0.12f, 0.02f, filled = true)
    }

    /** Hastur: una corona de tres puntas sobre tres trazos curvos que giran (símbolo propio). */
    fun yellowCrown() {
        path { m(-0.16f, -0.12f); l(-0.1f, -0.28f); l(-0.03f, -0.16f); l(0f, -0.3f); l(0.03f, -0.16f); l(0.1f, -0.28f); l(0.16f, -0.12f); l(-0.16f, -0.12f) }
        for (k in 0 until 3) {
            scope.rotate(k * 120f, pivot = p(0f, 0.08f)) {
                val gp = Path().apply {
                    moveTo(c.x, c.y + 0.08f * s)
                    quadraticBezierTo(c.x + 0.18f * s, c.y + 0.02f * s, c.x + 0.16f * s, c.y + 0.2f * s)
                    quadraticBezierTo(c.x + 0.15f * s, c.y + 0.27f * s, c.x + 0.09f * s, c.y + 0.25f * s)
                }
                drawPath(gp, color, style = stroke)
            }
        }
        circle(0f, 0.08f, 0.025f, filled = true)
    }

    /** Tulzscha: una llama verde doble rodeada de tres danzantes. */
    fun greenFlame() {
        path { m(0f, 0.24f); cu(-0.2f, 0.2f, -0.18f, -0.02f, -0.04f, -0.1f); cu(-0.02f, -0.2f, 0.04f, -0.26f, 0f, -0.32f); cu(0.12f, -0.22f, 0.22f, -0.06f, 0.16f, 0.1f); cu(0.12f, 0.2f, 0.06f, 0.24f, 0f, 0.24f) }
        path(thin) { m(0f, 0.18f); cu(-0.08f, 0.14f, -0.06f, 0.02f, 0f, -0.08f); cu(0.06f, 0.02f, 0.08f, 0.14f, 0f, 0.18f) }
        for (k in 0 until 3) {
            val a = (-90f + k * 120f + 60f) * (PI / 180f).toFloat()
            circle(cos(a) * 0.33f, sin(a) * 0.33f, 0.028f, filled = true)
        }
    }

    /** Ubbo-Sathla: una masa sin forma que guarda dos tablillas. */
    fun primordialMass() {
        path {
            m(-0.28f, 0.02f)
            cu(-0.3f, -0.2f, -0.06f, -0.3f, 0.06f, -0.22f)
            cu(0.2f, -0.28f, 0.34f, -0.1f, 0.26f, 0.04f)
            cu(0.34f, 0.2f, 0.12f, 0.32f, 0f, 0.24f)
            cu(-0.14f, 0.32f, -0.32f, 0.2f, -0.28f, 0.02f)
        }
        scope.drawRect(color, topLeft = p(-0.13f, -0.09f), size = Size(0.1f * s, 0.15f * s), style = thin)
        scope.drawRect(color, topLeft = p(0.03f, -0.07f), size = Size(0.1f * s, 0.15f * s), style = thin)
        line(-0.11f, -0.04f, -0.05f, -0.04f, thin)
        line(0.05f, -0.02f, 0.11f, -0.02f, thin)
    }

    /** Abhoth: una masa central que brota crías por todo su borde. */
    fun spawningMass() {
        circle(0f, 0f, 0.16f)
        circle(0f, 0f, 0.07f, alpha = 0.6f, st = thin)
        for (k in 0 until 9) {
            val a = (k * 40f) * (PI / 180f).toFloat()
            val r = if (k % 2 == 0) 0.26f else 0.23f
            circle(cos(a) * r, sin(a) * r, if (k % 3 == 0) 0.045f else 0.03f, st = thin)
            line(cos(a) * 0.16f, sin(a) * 0.16f, cos(a) * (r - 0.03f), sin(a) * (r - 0.03f), thin)
        }
    }

    /** Nug y Yeb: dos espirales gemelas; giran en sentidos opuestos. */
    fun twinSpiral(clockwise: Boolean) {
        val dir = if (clockwise) 1f else -1f
        path {
            val turns = 2.4f
            val steps = 60
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val a = dir * t * turns * 2f * PI.toFloat()
                val r = 0.02f + t * 0.24f
                if (i == 0) m(cos(a) * r, sin(a) * r) else l(cos(a) * r, sin(a) * r)
            }
        }
        if (!clockwise) {
            // Las nieblas de Yeb
            line(-0.3f, 0.3f, -0.12f, 0.3f, thin)
            line(0.05f, 0.33f, 0.28f, 0.33f, thin)
        } else {
            circle(0f, -0.33f, 0.03f, filled = true)
        }
    }

    /** Daoloth: un sólido imposible cruzado por velos rasgados. */
    fun tornVeils() {
        val hex = (0 until 6).map { k ->
            val a = (k * 60f - 90f) * (PI / 180f).toFloat()
            cos(a) * 0.26f to sin(a) * 0.26f
        }
        path { m(hex[0].first, hex[0].second); hex.drop(1).forEach { (x, y) -> l(x, y) }; close() }
        listOf(0, 2, 4).forEach { k -> line(0f, 0f, hex[k].first, hex[k].second, thin) }
        path(thin) { m(-0.34f, -0.08f); q(-0.1f, -0.02f, 0.02f, -0.12f); q(0.14f, -0.2f, 0.34f, -0.1f) }
        path(thin) { m(-0.34f, 0.12f); q(-0.12f, 0.2f, 0.04f, 0.1f); q(0.18f, 0.02f, 0.34f, 0.14f) }
    }

    /** Ghroth: un astro errante con anillo y un único ojo que canta. */
    fun harbingerPlanet() {
        circle(0f, 0f, 0.17f)
        scope.rotate(-20f, pivot = c) {
            drawOval(color, topLeft = Offset(c.x - 0.32f * s, c.y - 0.08f * s), size = Size(0.64f * s, 0.16f * s), style = stroke)
        }
        path(thin) { m(-0.09f, 0f); q(0f, -0.08f, 0.09f, 0f); q(0f, 0.08f, -0.09f, 0f) }
        circle(0f, 0f, 0.03f, filled = true)
        // Ondas del canto
        arc(0f, 0f, 0.38f, -60f, 30f, thin)
        arc(0f, 0f, 0.38f, 120f, 30f, thin)
    }

    /** Shub-Niggurath: cuernos de cabra sobre un rostro triangular y sus mil retoños. */
    fun blackGoat() {
        path { m(-0.06f, -0.1f); cu(-0.14f, -0.3f, -0.34f, -0.3f, -0.3f, -0.12f); cu(-0.28f, -0.04f, -0.2f, -0.06f, -0.2f, -0.12f) }
        path { m(0.06f, -0.1f); cu(0.14f, -0.3f, 0.34f, -0.3f, 0.3f, -0.12f); cu(0.28f, -0.04f, 0.2f, -0.06f, 0.2f, -0.12f) }
        path { m(-0.12f, -0.08f); l(0.12f, -0.08f); l(0f, 0.16f); close() }
        circle(-0.045f, -0.03f, 0.018f, filled = true)
        circle(0.045f, -0.03f, 0.018f, filled = true)
        listOf(-0.2f, -0.1f, 0f, 0.1f, 0.2f).forEachIndexed { i, x ->
            circle(x, if (i % 2 == 0) 0.27f else 0.31f, 0.025f, filled = true, alpha = 0.8f)
        }
    }

    /** Yog-Sothoth: un racimo de esferas que es a la vez la llave y la puerta. */
    fun spheresAndGate() {
        path { m(-0.2f, 0.3f); l(-0.2f, -0.05f); q(-0.2f, -0.3f, 0f, -0.3f); q(0.2f, -0.3f, 0.2f, -0.05f); l(0.2f, 0.3f) }
        listOf(
            Triple(0f, -0.08f, 0.08f), Triple(-0.08f, 0.04f, 0.065f), Triple(0.08f, 0.04f, 0.065f),
            Triple(0f, 0.15f, 0.06f), Triple(-0.1f, -0.16f, 0.04f), Triple(0.1f, -0.16f, 0.04f)
        ).forEach { (x, y, r) -> circle(x, y, r, st = thin) }
        // Paletón de la llave en el umbral
        line(0f, 0.21f, 0f, 0.31f, thin)
        line(0f, 0.27f, 0.05f, 0.27f, thin)
    }

    /** Nyarlathotep: el ojo de tres lóbulos que arde. */
    fun threeLobedEye() {
        for (k in 0 until 3) {
            val a = (-90f + k * 120f) * (PI / 180f).toFloat()
            circle(cos(a) * 0.12f, sin(a) * 0.12f, 0.12f)
        }
        circle(0f, 0f, 0.05f, filled = true)
        for (k in 0 until 6) {
            val a = (-60f + k * 60f) * (PI / 180f).toFloat()
            line(cos(a) * 0.29f, sin(a) * 0.29f, cos(a) * 0.36f, sin(a) * 0.36f, thin)
        }
    }

    /** Azathoth: un núcleo caótico con rayos desiguales y órbitas rotas (las flautas). */
    fun nuclearChaos() {
        circle(0f, 0f, 0.07f, filled = true)
        val lengths = listOf(0.3f, 0.18f, 0.26f, 0.14f, 0.34f, 0.2f, 0.28f, 0.16f, 0.24f, 0.32f, 0.12f, 0.22f)
        lengths.forEachIndexed { k, len ->
            val a = (k * 30f + 7f * (k % 3)) * (PI / 180f).toFloat()
            line(cos(a) * 0.09f, sin(a) * 0.09f, cos(a) * len, sin(a) * len, thin)
        }
        arc(0f, 0f, 0.38f, 10f, 70f, thin)
        arc(0f, 0f, 0.38f, 130f, 50f, thin)
        arc(0f, 0f, 0.38f, 220f, 95f, thin)
    }
}
