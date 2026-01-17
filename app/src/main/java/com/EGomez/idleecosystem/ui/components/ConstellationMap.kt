package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import com.EGomez.idleecosystem.ui.theme.MadnessRed

@Composable
fun ConstellationMap(
    upgrades: List<PrestigeUpgrade>,
    onNodeClick: (PrestigeUpgrade) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    // Definición de Colores para el mapa
    val lineColorLocked = Color.Gray.copy(alpha = 0.3f)
    val lineColorUnlocked = AncientGold.copy(alpha = 0.6f)

    val nodeColorLocked = Color.Gray
    val nodeColorAvailable = MadnessRed // Rojo para disponibles pero no comprados
    val nodeColorBought = ArcanePurple  // Violeta para los ya adquiridos

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val width = size.width
                    val height = size.height

                    // Detectar clic en un nodo (con radio de tolerancia)
                    val clickedNode = upgrades.find { node ->
                        val nodeX = node.x * width
                        val nodeY = node.y * height
                        val distance = Math.hypot((tapOffset.x - nodeX).toDouble(), (tapOffset.y - nodeY).toDouble())
                        distance < 60.0 // Radio de toque (aumentado para facilitar)
                    }

                    if (clickedNode != null) {
                        onNodeClick(clickedNode)
                    }
                }
            }
        ) {
            val width = size.width
            val height = size.height

            // 1. DIBUJAR CONEXIONES (Líneas)
            // Se dibujan primero para quedar detrás de las estrellas
            upgrades.forEach { node ->
                if (node.parentId != null) {
                    val parent = upgrades.find { it.id == node.parentId }
                    if (parent != null) {
                        val start = Offset(parent.x * width, parent.y * height)
                        val end = Offset(node.x * width, node.y * height)

                        val isPathUnlocked = parent.level > 0
                        val color = if (isPathUnlocked) lineColorUnlocked else lineColorLocked
                        val strokeWidth = if (isPathUnlocked) 5f else 3f

                        // Línea punteada si el camino no está activo aún
                        val pathEffect = if (!isPathUnlocked) PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f) else null

                        drawLine(
                            color = color,
                            start = start,
                            end = end,
                            strokeWidth = strokeWidth,
                            pathEffect = pathEffect
                        )
                    }
                }
            }

            // 2. DIBUJAR NODOS (Estrellas)
            upgrades.forEach { node ->
                val center = Offset(node.x * width, node.y * height)

                val isBought = node.level > 0

                // ¿Está disponible? (Si es raíz o su padre está comprado)
                var isAvailable = false
                if (node.parentId == null) {
                    isAvailable = true
                } else {
                    val parent = upgrades.find { it.id == node.parentId }
                    if (parent != null && parent.level > 0) isAvailable = true
                }

                val color = when {
                    isBought -> nodeColorBought
                    isAvailable -> nodeColorAvailable
                    else -> nodeColorLocked
                }

                val radius = if (isBought) 20f else 14f

                // Efecto de brillo (Halo)
                if (isBought || isAvailable) {
                    drawCircle(
                        color = color.copy(alpha = 0.4f),
                        center = center,
                        radius = radius * 2.0f
                    )
                }

                // Núcleo de la estrella
                drawCircle(
                    color = color,
                    center = center,
                    radius = radius
                )

                // Nivel de la mejora (Texto pequeño sobre la estrella)
                if (node.level > 0) {
                    val textLayoutResult = textMeasurer.measure(
                        text = "${node.level}",
                        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                    drawText(
                        textLayoutResult,
                        topLeft = Offset(center.x - textLayoutResult.size.width / 2, center.y - textLayoutResult.size.height / 2)
                    )
                }
            }
        }
    }
}