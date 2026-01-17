package com.EGomez.idleecosystem.core.math

import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Locale

fun BigDecimal.toBeautifulString(): String {
    // Si es 0, retornamos "0" simple
    if (this.compareTo(BigDecimal.ZERO) == 0) return "0"

    // Si es menor a 1000, mostramos el número normal (ej: "150.5")
    if (this.abs() < BigDecimal("1000")) {
        return DecimalFormat("0.##").format(this)
    }

    // Para todo lo demás, forzamos Notación Científica (ej: "1.50e+06")
    // Usamos Locale.US para asegurar que use puntos "." y no comas ","
    return String.format(Locale.US, "%.2e", this)
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