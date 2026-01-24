package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import kotlin.math.cos
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
    // Física Orbital
    var angle: Float = 0f,
    var radius: Float = 0f,
    var angularVelocity: Float = 0f,
    val centerX: Float = 0f,
    val centerY: Float = 0f
)

class ParticleSystem {
    val particles = mutableStateListOf<Particle>()

    /**
     * @param baseRadius El radio actual del botón (para que las partículas nazcan en el borde y escalen)
     */
    fun spawnExplosion(x: Float, y: Float, type: ParticleType, count: Int = 10, baseRadius: Float = 50f) {
        // Calculamos un factor de escala basado en un tamaño "estándar" de botón (ej: 150px)
        // Reducimos ligeramente la influencia del escalado del botón para que no sean gigantes en pantallas grandes
        val scaleFactor = (baseRadius / 85f).coerceAtLeast(0.4f)

        repeat(count) {
            val isEcho = type == ParticleType.ECHO

            val color = if (isEcho) {
                if (Random.nextBoolean()) Color(0xFF00E5FF) else AncientGold
            } else {
                if (Random.nextFloat() > 0.7f) ArcanePurple else Color.White.copy(alpha = 0.8f)
            }

            val lifeDecay = if (isEcho) Random.nextFloat() * 0.01f + 0.005f else Random.nextFloat() * 0.02f + 0.015f

            // AJUSTE DE TAMAÑO (Reducido ~50% respecto a la versión anterior)
            val baseSize = if (isEcho) {
                Random.nextFloat() * 3f + 2f // Antes: 6f + 3f
            } else {
                Random.nextFloat() * 2f + 1f // Antes: 4f + 2f
            }

            val finalSize = baseSize * scaleFactor

            if (isEcho) {
                // FÍSICA ORBITAL
                val initialAngle = Random.nextFloat() * 6.28f
                val spawnRadius = baseRadius + (Random.nextFloat() * 5f * scaleFactor) // Margen reducido también
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
                // CONOCIMIENTO
                val angle = Random.nextFloat() * 6.28f
                val speed = (Random.nextFloat() * 2.5f + 0.5f) * scaleFactor // Velocidad ajustada

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

    fun update() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.life -= p.decay

            if (p.type == ParticleType.KNOWLEDGE) {
                p.x += p.vx
                p.y += p.vy
                p.vy -= 0.15f
                p.vx *= 0.95f
            } else {
                // Expansión orbital
                p.angle += p.angularVelocity
                p.radius += 1.0f // Expansión del anillo un poco más lenta para elegancia

                p.x = p.centerX + cos(p.angle) * p.radius
                p.y = p.centerY + sin(p.angle) * p.radius
            }

            if (p.life <= 0f) {
                iterator.remove()
            }
        }
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
    if (!isParticlesEnabled) {
        LaunchedEffect(Unit) { system.clear() }
        return
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { system.update() }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        system.particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                radius = p.size * p.life,
                center = Offset(p.x, p.y)
            )
        }
    }
}