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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleType {
    KNOWLEDGE, // Flota hacia arriba, suave
    ECHO       // Explosión, brillante, cae o se dispersa
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,      // 1.0f a 0.0f
    val decay: Float,     // Cuánto baja la vida por frame
    val color: Color,
    val size: Float,
    val type: ParticleType
)

class ParticleSystem {
    val particles = mutableStateListOf<Particle>()

    fun spawnExplosion(x: Float, y: Float, type: ParticleType, count: Int = 10) {
        repeat(count) {
            val angle = Random.nextFloat() * 6.28f // 0 a 2PI
            val speed = if (type == ParticleType.ECHO) Random.nextFloat() * 15f + 5f else Random.nextFloat() * 5f + 2f

            // Variación de color
            val color = if (type == ParticleType.ECHO) {
                if (Random.nextBoolean()) Color(0xFF00E5FF) else AncientGold // Cian o Dorado
            } else {
                if (Random.nextBoolean()) Color.White else ArcanePurple // Blanco o Morado
            }

            val size = if (type == ParticleType.ECHO) Random.nextFloat() * 8f + 4f else Random.nextFloat() * 5f + 2f
            val decay = if (type == ParticleType.ECHO) Random.nextFloat() * 0.015f + 0.005f else Random.nextFloat() * 0.02f + 0.01f

            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    life = 1.0f,
                    decay = decay,
                    color = color,
                    size = size,
                    type = type
                )
            )
        }
    }

    fun update() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()

            // Física
            p.x += p.vx
            p.y += p.vy
            p.life -= p.decay

            // Comportamiento específico
            if (p.type == ParticleType.KNOWLEDGE) {
                p.vy -= 0.1f // Anti-gravedad suave (flota)
                p.vx *= 0.95f // Fricción aire
            } else {
                p.vy += 0.2f // Gravedad (cae)
                p.vx *= 0.92f // Fricción fuerte
            }

            if (p.life <= 0f) {
                iterator.remove()
            }
        }
    }
}

@Composable
fun ParticleOverlay(
    system: ParticleSystem,
    modifier: Modifier = Modifier
) {
    // Bucle de juego (Game Loop) sincronizado con los frames de la pantalla
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                system.update()
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        system.particles.forEach { p ->
            drawParticle(p)
        }
    }
}

private fun DrawScope.drawParticle(p: Particle) {
    drawCircle(
        color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
        radius = p.size * p.life, // Se encoge al morir
        center = Offset(p.x, p.y)
    )
}