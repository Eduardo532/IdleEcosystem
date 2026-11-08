package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import kotlinx.coroutines.flow.first
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleType {
    KNOWLEDGE,
    ECHO
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val decay: Float,
    val color: Color,
    val size: Float,
    val type: ParticleType,
    var angle: Float = 0f,
    var radius: Float = 0f,
    var angularVelocity: Float = 0f,
    val centerX: Float = 0f,
    val centerY: Float = 0f
)

class ParticleSystem {
    val particles = mutableStateListOf<Particle>()

    // Las partículas se mutan en el lugar (sus campos no son estado observable), así que
    // este contador es lo que le avisa al Canvas que debe redibujar en cada frame.
    var frame by mutableLongStateOf(0L)
        private set

    fun spawnExplosion(x: Float, y: Float, type: ParticleType, count: Int = 10, baseRadius: Float = 50f) {
        val scaleFactor = (baseRadius / 85f).coerceAtLeast(0.4f)

        repeat(count) {
            val isEcho = type == ParticleType.ECHO

            val color = if (isEcho) {
                if (Random.nextBoolean()) Color(0xFF00E5FF) else AncientGold
            } else {
                if (Random.nextFloat() > 0.7f) ArcanePurple else Color.White.copy(alpha = 0.8f)
            }

            val lifeDecay = if (isEcho) Random.nextFloat() * 0.01f + 0.005f else Random.nextFloat() * 0.02f + 0.015f

            val baseSize = if (isEcho) {
                Random.nextFloat() * 3f + 2f
            } else {
                Random.nextFloat() * 2f + 1f
            }

            val finalSize = baseSize * scaleFactor

            if (isEcho) {
                val initialAngle = Random.nextFloat() * 6.28f
                val spawnRadius = baseRadius + (Random.nextFloat() * 5f * scaleFactor)
                val orbitSpeed = (Random.nextFloat() * 0.05f + 0.02f) * (if (Random.nextBoolean()) 1 else -1)

                particles.add(
                    Particle(
                        x = x + cos(initialAngle) * spawnRadius,
                        y = y + sin(initialAngle) * spawnRadius,
                        vx = 0f, vy = 0f,
                        life = 1f,
                        decay = lifeDecay,
                        color = color,
                        size = finalSize,
                        type = type,
                        angle = initialAngle,
                        radius = spawnRadius,
                        angularVelocity = orbitSpeed,
                        centerX = x,
                        centerY = y
                    )
                )
            } else {
                val angle = Random.nextFloat() * 6.28f
                val speed = (Random.nextFloat() * 2.5f + 0.5f) * scaleFactor

                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        life = 1f,
                        decay = lifeDecay,
                        color = color,
                        size = finalSize,
                        type = type
                    )
                )
            }
        }
    }

    /**
     * Avanza la simulación. [dt] se mide en "frames de 60 fps": así la animación dura lo
     * mismo en pantallas de 60, 90 o 120 Hz (antes iba al doble de rápido a 120 Hz).
     */
    fun update(dt: Float = 1f) {
        val damping = 0.95.pow(dt.toDouble()).toFloat()
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.life -= p.decay * dt

            if (p.type == ParticleType.KNOWLEDGE) {
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.vy -= 0.15f * dt
                p.vx *= damping
            } else {
                p.angle += p.angularVelocity * dt
                p.radius += 1.0f * dt
                p.x = p.centerX + cos(p.angle) * p.radius
                p.y = p.centerY + sin(p.angle) * p.radius
            }

            if (p.life <= 0f) {
                iterator.remove()
            }
        }
        frame++
    }

    fun clear() {
        particles.clear()
    }
}

@Composable
fun ParticleOverlay(
    system: ParticleSystem,
    isParticlesEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    // Si está desactivado, limpiamos y NO renderizamos nada ni iniciamos el loop
    if (!isParticlesEnabled) {
        LaunchedEffect(Unit) { system.clear() }
        return
    }

    // El loop solo corre mientras haya partículas: sin ellas espera (sin pedir frames)
    // hasta que aparezca la siguiente, en lugar de despertar la CPU 60+ veces por segundo.
    LaunchedEffect(system) {
        while (true) {
            if (system.particles.isEmpty()) {
                snapshotFlow { system.particles.size }.first { it > 0 }
            }
            var lastFrameNanos = 0L
            while (system.particles.isNotEmpty()) {
                withFrameNanos { now ->
                    val dt = if (lastFrameNanos == 0L) 1f else ((now - lastFrameNanos) / FRAME_60FPS_NANOS).coerceIn(0f, 3f)
                    lastFrameNanos = now
                    system.update(dt)
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        system.frame // Suscribe el dibujo al contador de frames
        system.particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                radius = p.size * p.life,
                center = Offset(p.x, p.y)
            )
        }
    }
}

private const val FRAME_60FPS_NANOS = 16_666_667f
