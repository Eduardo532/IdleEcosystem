package com.EGomez.idleecosystem.domain.model

import androidx.compose.ui.graphics.Color
import com.EGomez.idleecosystem.ui.theme.*

enum class RuneTier {
    TIER_1_SWIPE,
    TIER_2_HOLD,
    TIER_3_TRACE
}

data class RuneVariant(
    val id: String,
    val name: String,
    val tier: RuneTier,
    val weight: Int, // Probabilidad relativa
    val color: Color,
    val description: String
)

object RuneCatalog {

    // --- TIER 1: SWIPE (11 Runas) ---
    val T1_BLOOD = RuneVariant("t1_blood", "Runa de Sangre", RuneTier.TIER_1_SWIPE, 100, CrimsonRed, "x50 Producción (15s)")
    val T1_ASH = RuneVariant("t1_ash", "Runa de Ceniza", RuneTier.TIER_1_SWIPE, 90, Color.Gray, "x40 Producción (20s)")
    val T1_SALT = RuneVariant("t1_salt", "Runa de Sal", RuneTier.TIER_1_SWIPE, 90, Color.White, "x60 Producción (10s)")
    val T1_BONE = RuneVariant("t1_bone", "Runa de Hueso", RuneTier.TIER_1_SWIPE, 80, Color(0xFFEFEBE9), "+2 Minutos de Tiempo")
    val T1_SHADOW = RuneVariant("t1_shadow", "Runa de Sombra", RuneTier.TIER_1_SWIPE, 80, Color(0xFF212121), "x30 Producción (30s)")
    val T1_WHISPER = RuneVariant("t1_whisper", "Runa de Susurro", RuneTier.TIER_1_SWIPE, 70, Color(0xFFB39DDB), "x10 Click Power + Auto (30s)")
    val T1_RUST = RuneVariant("t1_rust", "Runa de Óxido", RuneTier.TIER_1_SWIPE, 70, Color(0xFF8D6E63), "x45 Producción (18s)")
    val T1_FROST = RuneVariant("t1_frost", "Runa de Escarcha", RuneTier.TIER_1_SWIPE, 60, Color(0xFF80DEEA), "Pausa Buffs (5s)")
    val T1_VERMIN = RuneVariant("t1_vermin", "Runa de Plaga", RuneTier.TIER_1_SWIPE, 60, Color(0xFFC6FF00), "x55 Producción (12s)")
    val T1_RIFT = RuneVariant("t1_rift", "Runa de Grieta", RuneTier.TIER_1_SWIPE, 40, Color(0xFFD50000), "+5 Minutos de Tiempo")
    val T1_VOID_MINOR = RuneVariant("t1_void_minor", "Runa de Vacío Menor", RuneTier.TIER_1_SWIPE, 30, Color(0xFF6200EE), "+1% Chance Ecos")

    // --- TIER 2: HOLD (7 Runas) ---
    val T2_CHRONOS = RuneVariant("t2_chronos", "Runa del Tiempo", RuneTier.TIER_2_HOLD, 80, AncientGold, "Autoclicker (20s)")
    val T2_GRAVITY = RuneVariant("t2_gravity", "Runa de Gravedad", RuneTier.TIER_2_HOLD, 70, Color(0xFF3F51B5), "x200 Producción (20s)")
    val T2_BARRIER = RuneVariant("t2_barrier", "Runa de Barrera", RuneTier.TIER_2_HOLD, 60, Color(0xFF90CAF9), "-15% Costos (30s)")
    val T2_TIDAL = RuneVariant("t2_tidal", "Runa de Marea", RuneTier.TIER_2_HOLD, 50, Color(0xFF0277BD), "Ola de Producción x500")
    val T2_PSYCHE = RuneVariant("t2_psyche", "Runa de Mente", RuneTier.TIER_2_HOLD, 40, Color(0xFF9C27B0), "+5% Cordura Residual")
    val T2_VISION = RuneVariant("t2_vision", "Runa de Visión", RuneTier.TIER_2_HOLD, 30, Color(0xFFCDDC39), "Invocar Evento Random")
    val T2_ENTROPY = RuneVariant("t2_entropy", "Runa de Entropía", RuneTier.TIER_2_HOLD, 20, Color(0xFFFF5722), "Reinicio Ciclo Runa")

    // --- TIER 3: TRACE (3 Runas) ---
    val T3_CTHULHU = RuneVariant("t3_cthulhu", "Sigilo de Cthulhu", RuneTier.TIER_3_TRACE, 60, ArcanePurple, "x777 Prod + 1 Eco")
    val T3_NYARLATHOTEP = RuneVariant("t3_nyarlathotep", "El Caos Reptante", RuneTier.TIER_3_TRACE, 30, CrimsonRed, "Caos Total (x666 + Auto)")
    val T3_AZATHOTH = RuneVariant("t3_azathoth", "Sigilo de Azathoth", RuneTier.TIER_3_TRACE, 10, EtherealCyan, "+50 ECOS DEL VACÍO")

    fun getVariantsForTier(tier: RuneTier): List<RuneVariant> {
        return when(tier) {
            RuneTier.TIER_1_SWIPE -> listOf(T1_BLOOD, T1_ASH, T1_SALT, T1_BONE, T1_SHADOW, T1_WHISPER, T1_RUST, T1_FROST, T1_VERMIN, T1_RIFT, T1_VOID_MINOR)
            RuneTier.TIER_2_HOLD -> listOf(T2_CHRONOS, T2_GRAVITY, T2_BARRIER, T2_TIDAL, T2_PSYCHE, T2_VISION, T2_ENTROPY)
            RuneTier.TIER_3_TRACE -> listOf(T3_CTHULHU, T3_NYARLATHOTEP, T3_AZATHOTH)
        }
    }
}