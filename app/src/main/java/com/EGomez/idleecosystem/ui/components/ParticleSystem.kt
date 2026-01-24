package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import com.EGomez.idleecosystem.ui.theme.CrimsonRed
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
    // Variables para física orbital (Solo Echo)
    var angle: Float = 0f,
    var radius: Float = 0f,
    var angularVelocity: Float = 0f,
    val centerX: Float = 0f,
    val centerY: Float = 0f
)

class ParticleSystem {
    val particles = mutableStateListOf<Particle>()

    /**
     * @param x Coordenada X del centro del botón/click
     * @param y Coordenada Y del centro del botón/click
     */
    fun spawnExplosion(x: Float, y: Float, type: ParticleType, count: Int = 10) {
        repeat(count) {
            val isEcho = type == ParticleType.ECHO

            // Colores
            val color = if (isEcho) {
                if (Random.nextBoolean()) Color(0xFF00E5FF) else AncientGold // Cian neón o Oro
            } else {
                if (Random.nextFloat() > 0.7f) ArcanePurple else Color.White.copy(alpha = 0.8f)
            }

            // Vida y Tamaño
            val lifeDecay = if (isEcho) Random.nextFloat() * 0.01f + 0.005f else Random.nextFloat() * 0.02f + 0.015f
            val startSize = if (isEcho) Random.nextFloat() * 6f + 3f else Random.nextFloat() * 4f + 2f

            if (isEcho) {
                // FÍSICA ORBITAL PARA ECOS
                val initialAngle = Random.nextFloat() * 6.28f
                val initialRadius = Random.nextFloat() * 20f + 50f
                val orbitSpeed = (Random.nextFloat() * 0.05f + 0.02f) * (if (Random.nextBoolean()) 1 else -1)

                particles.add(
                    Particle(
                        x = x + cos(initialAngle) * initialRadius,
                        y = y + sin(initialAngle) * initialRadius,
                        vx = 0f, vy = 0f,
                        life = 1f,
                        decay = lifeDecay,
                        color = color,
                        size = startSize,
                        type = type,
                        angle = initialAngle,
                        radius = initialRadius,
                        angularVelocity = orbitSpeed,
                        centerX = x,
                        centerY = y
                    )
                )
            } else {
                // FÍSICA ESTÁNDAR PARA CONOCIMIENTO
                val angle = Random.nextFloat() * 6.28f
                val speed = Random.nextFloat() * 3f + 1f

                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        life = 1f,
                        decay = lifeDecay,
                        color = color,
                        size = startSize,
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
                p.angle += p.angularVelocity
                p.radius += 1.5f

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
            withFrameNanos {
                system.update()
            }
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