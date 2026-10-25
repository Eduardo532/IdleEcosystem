package com.EGomez.idleecosystem.core.math

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Formas de mostrar números grandes. Se elige en Configuración. */
enum class NumberNotation {
    SCIENTIFIC, // 1.50e+06
    SUFFIX      // 1.50M
}

/** Preferencia global de notación (la carga y guarda GamePreferences). */
object NotationSettings {
    @Volatile
    var mode: NumberNotation = NumberNotation.SCIENTIFIC
}

private val SUFFIXES = listOf(
    "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No",
    "Dc", "UDc", "DDc", "TDc", "QaDc", "QiDc", "SxDc", "SpDc", "OcDc", "NoDc", "Vg"
)

// Locale.US en todos los formatos para que el separador decimal sea siempre "."
// (antes los números < 1000 usaban "," en dispositivos en español y "." en el resto).
private val smallFormatSymbols = DecimalFormatSymbols(Locale.US)

fun BigDecimal.toBeautifulString(notation: NumberNotation = NotationSettings.mode): String {
    if (this.signum() == 0) return "0"

    val abs = this.abs()
    val sign = if (this.signum() < 0) "-" else ""

    if (abs < BigDecimal("1000")) {
        return sign + DecimalFormat("0.##", smallFormatSymbols).format(abs)
    }

    if (notation == NumberNotation.SUFFIX) {
        var group = BigMath.exponent10(abs) / 3
        var mantissa = abs.movePointLeft(group * 3).setScale(2, RoundingMode.HALF_UP)
        // 999.995K se redondea a 1000.00K: pasamos al siguiente sufijo
        if (mantissa >= BigDecimal("1000")) {
            group += 1
            mantissa = abs.movePointLeft(group * 3).setScale(2, RoundingMode.HALF_UP)
        }
        val suffix = SUFFIXES.getOrNull(group - 1)
        if (suffix != null) {
            return sign + mantissa.toPlainString() + suffix
        }
        // Más allá de los sufijos conocidos caemos a notación científica
    }

    return String.format(Locale.US, "%.2e", this)
}

fun formatSecondsToTime(totalSeconds: Long): String {
    if (totalSeconds <= 0) return "0s"

    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val sb = StringBuilder()
    if (days > 0) sb.append("${days}d ")
    if (hours > 0 || days > 0) sb.append("${hours}h ")
    if (minutes > 0 || hours > 0 || days > 0) sb.append("${minutes}m ")
    sb.append("${seconds}s")

    return sb.toString().trim()
}
