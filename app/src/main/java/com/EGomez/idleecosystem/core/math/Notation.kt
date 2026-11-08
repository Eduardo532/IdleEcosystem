package com.EGomez.idleecosystem.core.math

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

/** Cómo se escriben los números grandes. */
enum class NumberNotation {
    /** 1.50e+06 (la de siempre) */
    SCIENTIFIC,

    /** 1.50 M, 2.30 B... y científica cuando se acaban los nombres */
    SUFFIX
}

/**
 * Notación elegida por el jugador.
 *
 * Es estado de Compose (snapshot): cada texto que se formatea durante la
 * composición "lee" este valor, así que al cambiarlo se redibujan todos los números
 * de la pantalla, incluso los de tarjetas que de otro modo se saltarían. Leerlo
 * desde otro hilo (el ViewModel formatea en segundo plano) es seguro.
 */
object NumberFormatting {
    var notation: NumberNotation by mutableStateOf(NumberNotation.SCIENTIFIC)

    /**
     * Escala corta, la habitual en juegos incrementales: K = 10^3, M = 10^6, B = 10^9...
     * El índice i corresponde a 10^(3 * (i + 1)).
     */
    val SUFFIXES = listOf(
        "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No",
        "Dc", "UDc", "DDc", "TDc", "QaDc", "QiDc", "SxDc", "SpDc", "OcDc", "NoDc", "Vg"
    )
}

private val THOUSAND = BigDecimal("1000")

fun BigDecimal.toBeautifulString(notation: NumberNotation = NumberFormatting.notation): String {
    // Si es 0, retornamos "0" simple
    if (this.compareTo(BigDecimal.ZERO) == 0) return "0"

    // Si es menor a 1000, mostramos el número normal (ej: "150.5")
    if (this.abs() < THOUSAND) {
        return DecimalFormat("0.##").format(this)
    }

    if (notation == NumberNotation.SUFFIX) {
        toSuffixString()?.let { return it }
    }

    // Notación científica (ej: "1.50e+06")
    // Usamos Locale.US para asegurar que use puntos "." y no comas ","
    return String.format(Locale.US, "%.2e", this)
}

/** "1.50 M"; null si el número supera el último nombre (se usa la científica). */
private fun BigDecimal.toSuffixString(): String? {
    val exponent = precision() - scale() - 1 // posición del dígito más alto: 6 para 1.234.567
    var group = exponent / 3                 // 1 = miles, 2 = millones...
    var mantissa = movePointLeft(group * 3).setScale(2, RoundingMode.HALF_UP)
    // 999.999 redondea a 1000.00: se muestra "1.00 M" y no "1000.00 K"
    if (mantissa.abs() >= THOUSAND) {
        group += 1
        mantissa = movePointLeft(group * 3).setScale(2, RoundingMode.HALF_UP)
    }
    val suffix = NumberFormatting.SUFFIXES.getOrNull(group - 1) ?: return null
    return String.format(Locale.US, "%.2f %s", mantissa, suffix)
}

fun formatSecondsToTime(totalSeconds: Long): String {
    if (totalSeconds <= 0) return "0s"

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val sb = StringBuilder()
    if (hours > 0) sb.append("${hours}h ")
    if (minutes > 0 || hours > 0) sb.append("${minutes}m ") // Muestra minutos si hay horas, aunque sean 0m
    sb.append("${seconds}s")

    return sb.toString().trim()
}
