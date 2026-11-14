package com.EGomez.idleecosystem.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Iconos dibujados del juego (v0.19.10): sustituyen a los emojis, que cambian de aspecto
 * según el teléfono y rompían la estética. Trazos finos de 24×24 que toman el color del texto.
 */
object GameIcons {
    private fun icon(name: String, stroke: Boolean = true, build: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            if (stroke) {
                path(
                    fill = null,
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathBuilder = build
                )
            } else {
                path(fill = SolidColor(Color.White), pathBuilder = build)
            }
        }.build()

    /** Calavera: Locura y colapso. */
    val Skull: ImageVector by lazy {
        icon("Skull") {
            moveTo(12f, 3f)
            curveTo(7f, 3f, 4f, 6.5f, 4f, 10.5f)
            curveTo(4f, 13f, 5.2f, 14.8f, 7f, 15.8f)
            lineTo(7f, 19f)
            lineTo(17f, 19f)
            lineTo(17f, 15.8f)
            curveTo(18.8f, 14.8f, 20f, 13f, 20f, 10.5f)
            curveTo(20f, 6.5f, 17f, 3f, 12f, 3f)
            close()
            moveTo(9f, 10f); lineTo(9.01f, 10f)
            moveTo(15f, 10f); lineTo(15.01f, 10f)
            moveTo(10.5f, 19f); lineTo(10.5f, 21f)
            moveTo(13.5f, 19f); lineTo(13.5f, 21f)
            moveTo(12f, 13f); lineTo(11f, 15f); lineTo(13f, 15f); close()
        }
    }

    /** Espadas cruzadas: Pruebas. */
    val Swords: ImageVector by lazy {
        icon("Swords") {
            moveTo(4f, 4f); lineTo(15f, 15f)
            moveTo(20f, 4f); lineTo(9f, 15f)
            moveTo(13f, 17f); lineTo(17f, 13f)
            moveTo(7f, 13f); lineTo(11f, 17f)
            moveTo(16f, 16f); lineTo(20f, 20f)
            moveTo(8f, 16f); lineTo(4f, 20f)
        }
    }

    /** Ola: la Marea. */
    val Wave: ImageVector by lazy {
        icon("Wave") {
            moveTo(2f, 10f)
            curveTo(5f, 6f, 8f, 6f, 10f, 10f)
            curveTo(12f, 14f, 15f, 14f, 17f, 10f)
            curveTo(18.5f, 7f, 20.5f, 7f, 22f, 9f)
            moveTo(2f, 16f)
            curveTo(5f, 12f, 8f, 12f, 10f, 16f)
            curveTo(12f, 20f, 15f, 20f, 17f, 16f)
            curveTo(18.5f, 13f, 20.5f, 13f, 22f, 15f)
        }
    }

    /** Doble hélice: Mutaciones. */
    val Helix: ImageVector by lazy {
        icon("Helix") {
            moveTo(7f, 2f)
            curveTo(7f, 8f, 17f, 8f, 17f, 12f)
            curveTo(17f, 16f, 7f, 16f, 7f, 22f)
            moveTo(17f, 2f)
            curveTo(17f, 8f, 7f, 8f, 7f, 12f)
            curveTo(7f, 16f, 17f, 16f, 17f, 22f)
            moveTo(9f, 5f); lineTo(15f, 5f)
            moveTo(9f, 19f); lineTo(15f, 19f)
        }
    }

    /** Ojo tras un velo: el Velo. */
    val Veil: ImageVector by lazy {
        icon("Veil") {
            moveTo(2f, 12f)
            curveTo(5f, 7f, 9f, 5f, 12f, 5f)
            curveTo(15f, 5f, 19f, 7f, 22f, 12f)
            curveTo(19f, 17f, 15f, 19f, 12f, 19f)
            curveTo(9f, 19f, 5f, 17f, 2f, 12f)
            close()
            moveTo(12f, 9f)
            curveTo(13.7f, 9f, 15f, 10.3f, 15f, 12f)
            curveTo(15f, 13.7f, 13.7f, 15f, 12f, 15f)
            curveTo(10.3f, 15f, 9f, 13.7f, 9f, 12f)
            curveTo(9f, 10.3f, 10.3f, 9f, 12f, 9f)
            close()
            moveTo(4f, 3f); lineTo(4f, 21f)
            moveTo(20f, 3f); lineTo(20f, 21f)
        }
    }

    /** Matraz: el Laboratorio. */
    val Flask: ImageVector by lazy {
        icon("Flask") {
            moveTo(9f, 3f); lineTo(15f, 3f)
            moveTo(10f, 3f); lineTo(10f, 9f); lineTo(4.5f, 19f)
            curveTo(4f, 20f, 4.7f, 21f, 5.8f, 21f)
            lineTo(18.2f, 21f)
            curveTo(19.3f, 21f, 20f, 20f, 19.5f, 19f)
            lineTo(14f, 9f); lineTo(14f, 3f)
            moveTo(7f, 15f); lineTo(17f, 15f)
        }
    }

    /** Luna creciente con una estrella: el Sueño Profundo. */
    val Moon: ImageVector by lazy {
        icon("Moon") {
            moveTo(20f, 14.5f)
            curveTo(18.8f, 15.1f, 17.5f, 15.4f, 16f, 15.4f)
            curveTo(11.3f, 15.4f, 7.6f, 11.7f, 7.6f, 7f)
            curveTo(7.6f, 5.5f, 7.9f, 4.2f, 8.5f, 3f)
            curveTo(5.2f, 4.4f, 3f, 7.6f, 3f, 11.4f)
            curveTo(3f, 16.7f, 7.3f, 21f, 12.6f, 21f)
            curveTo(16.4f, 21f, 19.6f, 18.8f, 20f, 14.5f)
            close()
            moveTo(17f, 3f); lineTo(17f, 7f)
            moveTo(15f, 5f); lineTo(19f, 5f)
        }
    }

    /** Dos eslabones: la Cadena de Invocación. */
    val Chain: ImageVector by lazy {
        icon("Chain") {
            // Eslabón de arriba a la izquierda
            moveTo(10f, 14f); lineTo(6.5f, 17.5f)
            curveTo(5.1f, 18.9f, 2.9f, 16.9f, 4.3f, 15.5f)
            lineTo(8.5f, 11.3f)
            curveTo(9.9f, 9.9f, 11.9f, 10.1f, 12.7f, 11.3f)
            // Eslabón de abajo a la derecha
            moveTo(14f, 10f); lineTo(17.5f, 6.5f)
            curveTo(18.9f, 5.1f, 20.9f, 7.1f, 19.7f, 8.5f)
            lineTo(15.5f, 12.7f)
            curveTo(14.1f, 14.1f, 12.1f, 13.9f, 11.3f, 12.7f)
        }
    }

    /** Candado: algo que aún no se alcanza. */
    val Lock: ImageVector by lazy {
        icon("Lock") {
            moveTo(6f, 11f); lineTo(18f, 11f); lineTo(18f, 21f); lineTo(6f, 21f); close()
            moveTo(8f, 11f); lineTo(8f, 7f)
            curveTo(8f, 4.8f, 9.8f, 3f, 12f, 3f)
            curveTo(14.2f, 3f, 16f, 4.8f, 16f, 7f)
            lineTo(16f, 11f)
        }
    }

    /** Sello Antiguo: la estrella de cinco puntas con el ojo, moneda del Bestiario. */
    val ElderSeal: ImageVector by lazy {
        icon("ElderSeal") {
            moveTo(12f, 2f); lineTo(14.6f, 9.2f); lineTo(22f, 9.3f); lineTo(16.1f, 13.9f)
            lineTo(18.2f, 21f); lineTo(12f, 16.8f); lineTo(5.8f, 21f); lineTo(7.9f, 13.9f)
            lineTo(2f, 9.3f); lineTo(9.4f, 9.2f); close()
            moveTo(10f, 12f)
            curveTo(11f, 10.8f, 13f, 10.8f, 14f, 12f)
            curveTo(13f, 13.2f, 11f, 13.2f, 10f, 12f)
            close()
        }
    }

    /** Fragmento: lo que dan las criaturas repetidas al máximo. */
    val Shard: ImageVector by lazy {
        icon("Shard") {
            moveTo(12f, 2f); lineTo(18f, 9f); lineTo(14f, 22f); lineTo(10f, 22f); lineTo(6f, 9f); close()
            moveTo(6f, 9f); lineTo(18f, 9f)
            moveTo(12f, 2f); lineTo(12f, 22f)
        }
    }
}

/** Un icono dibujado seguido de un texto, en una línea, del mismo color. */
@Composable
fun IconLabel(
    icon: ImageVector,
    text: String,
    color: Color,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size((fontSize.value + 2f).dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = color, fontSize = fontSize, fontWeight = fontWeight)
    }
}
