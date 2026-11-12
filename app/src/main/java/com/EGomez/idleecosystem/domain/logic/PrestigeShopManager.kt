package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.data.database.PrestigeUpgradeEntity
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.Constellation
import com.EGomez.idleecosystem.domain.model.PactEffects
import com.EGomez.idleecosystem.domain.model.UpgradeType
import java.math.BigDecimal
import java.math.MathContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrestigeShopManager @Inject constructor() {

    // --- EL CIELO DE LA LOCURA (v0.19.2): siete constelaciones alrededor de El Despertar ---
    // Dentro de cada constelación los nodos se compran en cadena (cada uno pide el
    // anterior); la Senda de su centro se abre al iluminarlos todos.
    val upgrades = mutableListOf(
        // EL DESPERTAR: el centro del cielo
        PrestigeUpgrade("pu_core", "El Despertar", "Aumenta la Producción Global.", BigDecimal("1"), BigDecimal("1.5"), BigDecimal("0.10"), UpgradeType.GLOBAL_MULTIPLIER, parentId = null),

        // LA MANO: el clic
        PrestigeUpgrade("pu_click_1", "Dedos Inquietos", "Multiplica el poder de clic.", BigDecimal("5"), BigDecimal("1.5"), BigDecimal("0.50"), UpgradeType.CLICK_MULTIPLIER, parentId = "pu_core", constellation = Constellation.HAND),
        PrestigeUpgrade("pu_click_2", "Furia Táctil", "Clics más potentes.", BigDecimal("25"), BigDecimal("1.6"), BigDecimal("1.0"), UpgradeType.CLICK_MULTIPLIER, parentId = "pu_click_1", constellation = Constellation.HAND),
        PrestigeUpgrade("pu_click_3", "Tormenta Psiónica", "Clics devastadores.", BigDecimal("150"), BigDecimal("1.8"), BigDecimal("2.0"), UpgradeType.CLICK_MULTIPLIER, parentId = "pu_click_2", constellation = Constellation.HAND),
        PrestigeUpgrade("pu_click_4", "La Mano de Dios", "Poder de clic definitivo.", BigDecimal("1000"), BigDecimal("2.5"), BigDecimal("5.0"), UpgradeType.CLICK_MULTIPLIER, parentId = "pu_click_3", constellation = Constellation.HAND),
        // v0.19.4: una estrella por cada estrella de Orión
        PrestigeUpgrade("pu_hand_belt", "Cinturón del Cazador", "Multiplica el poder de clic.", BigDecimal("60"), BigDecimal("1.6"), BigDecimal("0.40"), UpgradeType.CLICK_MULTIPLIER, parentId = "pu_click_2", constellation = Constellation.HAND),
        PrestigeUpgrade("pu_hand_knuckles", "Nudillos de Piedra", "Clics todavía más fuertes.", BigDecimal("300"), BigDecimal("1.8"), BigDecimal("0.80"), UpgradeType.CLICK_MULTIPLIER, parentId = "pu_hand_belt", constellation = Constellation.HAND),
        PrestigeUpgrade("pu_hand_resonant", "Golpe Resonante", "Un 5% más de probabilidad de Eco por nivel.", BigDecimal("800"), BigDecimal("2.2"), BigDecimal("0.05"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_hand_knuckles", constellation = Constellation.HAND, maxLevel = 5, effectsPerLevel = PactEffects(echoChance = 1.05)),
        PrestigeUpgrade("pu_hand_head", "Cabeza del Cazador", "Clics brutales.", BigDecimal("3000"), BigDecimal("2.6"), BigDecimal("2.5"), UpgradeType.CLICK_MULTIPLIER, parentId = "pu_click_3", constellation = Constellation.HAND),

        // EL ENGRANAJE: los edificios (v0.19.2: más fuertes y con Planos Imposibles)
        PrestigeUpgrade("pu_idle_1", "Engranajes Mentales", "Multiplica los seis primeros edificios.", BigDecimal("10"), BigDecimal("1.5"), BigDecimal("0.30"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_core", constellation = Constellation.GEARS),
        PrestigeUpgrade("pu_idle_2", "Sincronía", "Multiplica TODOS los edificios.", BigDecimal("50"), BigDecimal("1.6"), BigDecimal("0.25"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_idle_1", constellation = Constellation.GEARS),
        PrestigeUpgrade("pu_gear_plans", "Planos Imposibles", "Los edificios cuestan un 3% menos por nivel.", BigDecimal("150"), BigDecimal("1.8"), BigDecimal("0.03"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_idle_2", constellation = Constellation.GEARS, maxLevel = 10, effectsPerLevel = PactEffects(buildingCost = 0.97)),
        // v0.19.4: el resto de la Osa Mayor
        PrestigeUpgrade("pu_gear_wheels", "Ruedas Dentadas", "Multiplica los edificios 1 a 6.", BigDecimal("80"), BigDecimal("1.7"), BigDecimal("0.25"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_gear_plans", constellation = Constellation.GEARS),
        PrestigeUpgrade("pu_gear_double", "Engranajes Dobles", "Multiplica los edificios 7 a 11.", BigDecimal("400"), BigDecimal("1.9"), BigDecimal("0.25"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_gear_wheels", constellation = Constellation.GEARS),
        PrestigeUpgrade("pu_gear_engine", "Motor Primordial", "Multiplica los edificios 12 a 16.", BigDecimal("2500"), BigDecimal("2.2"), BigDecimal("0.25"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_gear_double", constellation = Constellation.GEARS),
        PrestigeUpgrade("pu_gear_alloy", "Aleación Impía", "Los edificios cuestan un 2% menos por nivel.", BigDecimal("600"), BigDecimal("2.0"), BigDecimal("0.02"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_gear_engine", constellation = Constellation.GEARS, maxLevel = 10, effectsPerLevel = PactEffects(buildingCost = 0.98)),

        // EL SUEÑO: sin conexión y los eventos
        PrestigeUpgrade("pu_offline_1", "Sueño Lúcido", "Multiplica ganancia Offline.", BigDecimal("200"), BigDecimal("1.8"), BigDecimal("0.50"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_core", constellation = Constellation.DREAM),
        PrestigeUpgrade("pu_offline_2", "Eternidad en un Segundo", "Ganancia Offline masiva.", BigDecimal("1500"), BigDecimal("2.5"), BigDecimal("2.0"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_offline_1", constellation = Constellation.DREAM),
        PrestigeUpgrade("pu_dream_visions", "Visiones Nocturnas", "Más eventos, y sus potenciadores más fuertes.", BigDecimal("300"), BigDecimal("2.0"), BigDecimal("0.15"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_offline_2", constellation = Constellation.DREAM, maxLevel = 5, effectsPerLevel = PactEffects(eventFrequency = 1.15, eventBuffPower = 1.10)),
        // v0.19.4: el resto de las Pléyades
        PrestigeUpgrade("pu_dream_shared", "Sueño Compartido", "Multiplica la ganancia sin conexión.", BigDecimal("250"), BigDecimal("1.8"), BigDecimal("0.30"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_dream_visions", constellation = Constellation.DREAM, effectsPerLevel = PactEffects(offline = 1.30)),
        PrestigeUpgrade("pu_dream_nightmare", "Pesadilla Útil", "Los potenciadores de los eventos, un 15% más fuertes por nivel.", BigDecimal("500"), BigDecimal("2.0"), BigDecimal("0.15"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_dream_shared", constellation = Constellation.DREAM, maxLevel = 5, effectsPerLevel = PactEffects(eventBuffPower = 1.15)),
        PrestigeUpgrade("pu_dream_sisters", "Siete Hermanas", "Los eventos llegan un 10% más seguido por nivel.", BigDecimal("900"), BigDecimal("2.2"), BigDecimal("0.10"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_dream_nightmare", constellation = Constellation.DREAM, maxLevel = 5, effectsPerLevel = PactEffects(eventFrequency = 1.10)),
        PrestigeUpgrade("pu_dream_vigil", "Desvelo Infinito", "Ganancia sin conexión masiva.", BigDecimal("3000"), BigDecimal("2.5"), BigDecimal("0.60"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_dream_sisters", constellation = Constellation.DREAM, effectsPerLevel = PactEffects(offline = 1.60)),

        // LA MENTE: la producción de todo (Concentración, el doble que antes)
        PrestigeUpgrade("pu_mind_1", "Concentración", "Multiplicador Global.", BigDecimal("15"), BigDecimal("1.4"), BigDecimal("0.10"), UpgradeType.GLOBAL_MULTIPLIER, parentId = "pu_core", constellation = Constellation.MIND),
        PrestigeUpgrade("pu_mind_2", "Lógica Alienígena", "Aumento Global considerable.", BigDecimal("80"), BigDecimal("1.7"), BigDecimal("0.25"), UpgradeType.GLOBAL_MULTIPLIER, parentId = "pu_mind_1", constellation = Constellation.MIND),
        PrestigeUpgrade("pu_mind_3", "Iluminación Oscura", "Potenciador Global avanzado.", BigDecimal("400"), BigDecimal("2.0"), BigDecimal("0.50"), UpgradeType.GLOBAL_MULTIPLIER, parentId = "pu_mind_2", constellation = Constellation.MIND),
        PrestigeUpgrade("pu_mind_4", "Omnisciencia", "Multiplicador Global divino.", BigDecimal("2500"), BigDecimal("3.0"), BigDecimal("1.5"), UpgradeType.GLOBAL_MULTIPLIER, parentId = "pu_mind_3", constellation = Constellation.MIND),
        PrestigeUpgrade("pu_star_final", "Ascensión Cósmica", "El Multiplicador Supremo.", BigDecimal("5000"), BigDecimal("5.0"), BigDecimal("1.00"), UpgradeType.GLOBAL_MULTIPLIER, parentId = "pu_mind_4", constellation = Constellation.MIND),

        // EL VACÍO: la Cordura (v0.19.2: bonos mucho más altos; antes no valían su precio)
        PrestigeUpgrade("pu_void_1", "Llamada del Vacío", "Multiplica la Cordura obtenida.", BigDecimal("100"), BigDecimal("2.0"), BigDecimal("0.25"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_core", constellation = Constellation.VOID),
        PrestigeUpgrade("pu_void_2", "Abismo", "Gran bono de Cordura.", BigDecimal("500"), BigDecimal("2.5"), BigDecimal("0.40"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_void_1", constellation = Constellation.VOID),
        PrestigeUpgrade("pu_void_3", "Singularidad", "Multiplicador masivo de Cordura.", BigDecimal("5000"), BigDecimal("4.0"), BigDecimal("1.00"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_void_2", constellation = Constellation.VOID),
        // v0.19.4: el resto del Pez Austral
        PrestigeUpgrade("pu_void_tide", "Marea Negra", "Un 10% más de Cordura por nivel.", BigDecimal("800"), BigDecimal("2.0"), BigDecimal("0.10"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_void_3", constellation = Constellation.VOID, effectsPerLevel = PactEffects(sanityGain = 1.10)),
        PrestigeUpgrade("pu_void_dark", "Agua sin Luz", "Un 15% más de Cordura por nivel.", BigDecimal("2000"), BigDecimal("2.3"), BigDecimal("0.15"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_void_tide", constellation = Constellation.VOID, effectsPerLevel = PactEffects(sanityGain = 1.15)),
        PrestigeUpgrade("pu_void_mouth", "Boca del Pez", "Un 10% más de probabilidad de Eco por nivel.", BigDecimal("3500"), BigDecimal("2.4"), BigDecimal("0.10"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_void_dark", constellation = Constellation.VOID, maxLevel = 5, effectsPerLevel = PactEffects(echoChance = 1.10)),
        PrestigeUpgrade("pu_void_scales", "Escamas de Sombra", "Un 20% más de Cordura por nivel.", BigDecimal("8000"), BigDecimal("2.6"), BigDecimal("0.20"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_void_mouth", constellation = Constellation.VOID, effectsPerLevel = PactEffects(sanityGain = 1.20)),
        PrestigeUpgrade("pu_void_eye", "Ojo de Fomalhaut", "Un 30% más de Cordura por nivel.", BigDecimal("20000"), BigDecimal("3.0"), BigDecimal("0.30"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_void_scales", constellation = Constellation.VOID, effectsPerLevel = PactEffects(sanityGain = 1.30)),

        // EL CULTO: con qué empieza cada vida
        PrestigeUpgrade(STARTING_NOTES_ID, "Legado del Culto", "Cada vida empieza con 5 Notas Desordenadas por nivel.", BigDecimal("3"), BigDecimal("2.0"), BigDecimal(NOTES_PER_LEVEL), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_core", constellation = Constellation.CULT, startingBuilding = "b_notes"),
        PrestigeUpgrade("pu_cult_scrolls", "Biblioteca Heredada", "Cada vida empieza con 5 Pergaminos Prohibidos por nivel.", BigDecimal("20"), BigDecimal("2.0"), BigDecimal("5"), UpgradeType.BUILDING_MULTIPLIER, parentId = STARTING_NOTES_ID, constellation = Constellation.CULT, maxLevel = 10, startingBuilding = "b_scrolls"),
        PrestigeUpgrade("pu_cult_artifacts", "Reliquias de Familia", "Cada vida empieza con 3 Artefactos Susurrantes por nivel.", BigDecimal("120"), BigDecimal("2.2"), BigDecimal("3"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_cult_scrolls", constellation = Constellation.CULT, maxLevel = 10, startingBuilding = "b_artifact"),
        PrestigeUpgrade("pu_cult_cultists", "Iniciados Fieles", "Cada vida empieza con 2 Sectarios Novatos por nivel.", BigDecimal("600"), BigDecimal("2.5"), BigDecimal("2"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_cult_artifacts", constellation = Constellation.CULT, maxLevel = 10, startingBuilding = "b_cultist"),
        // v0.19.4: el resto de Tauro
        PrestigeUpgrade("pu_cult_tithe", "Diezmo de Sangre", "Cada vida empieza con 1 Sacerdote Demente por nivel.", BigDecimal("1500"), BigDecimal("2.5"), BigDecimal("1"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_cult_cultists", constellation = Constellation.CULT, maxLevel = 10, startingBuilding = "b_priest"),
        PrestigeUpgrade("pu_cult_eye", "Ojo del Toro", "Cada vida empieza con 1 Santuario Subterráneo por nivel.", BigDecimal("4000"), BigDecimal("2.7"), BigDecimal("1"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_cult_tithe", constellation = Constellation.CULT, maxLevel = 10, startingBuilding = "b_sanctuary"),
        PrestigeUpgrade("pu_cult_horn", "Cuerno Sagrado", "Cada vida empieza con 1 Altar de Hueso por nivel.", BigDecimal("10000"), BigDecimal("3.0"), BigDecimal("1"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_cult_eye", constellation = Constellation.CULT, maxLevel = 10, startingBuilding = "b_altar"),
        PrestigeUpgrade("pu_cult_offering", "Ofrendas", "Los rituales cuestan un 5% menos por nivel.", BigDecimal("700"), BigDecimal("2.0"), BigDecimal("0.05"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_cult_cultists", constellation = Constellation.CULT, maxLevel = 6, effectsPerLevel = PactEffects(ritualCost = 0.95)),
        PrestigeUpgrade("pu_cult_flock", "Rebaño Fiel", "Cada vida empieza con 5 Sinapsis Arcanas por nivel.", BigDecimal("1200"), BigDecimal("2.2"), BigDecimal("5"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_cult_offering", constellation = Constellation.CULT, maxLevel = 10, startingBuilding = "b_synapse"),

        // LO ARCANO: runas, rituales, Ecos, Atención y Locura
        PrestigeUpgrade("pu_arcane_runes", "Ojos del Vacío", "Las runas aparecen un 10% más seguido por nivel.", BigDecimal("40"), BigDecimal("1.8"), BigDecimal("0.10"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_core", constellation = Constellation.ARCANE, maxLevel = 5, effectsPerLevel = PactEffects(runeFrequency = 1.10)),
        PrestigeUpgrade("pu_arcane_rituals", "Liturgia Breve", "Los rituales cuestan y tardan en recargar un 5% menos por nivel.", BigDecimal("150"), BigDecimal("2.0"), BigDecimal("0.05"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_arcane_runes", constellation = Constellation.ARCANE, maxLevel = 6, effectsPerLevel = PactEffects(ritualCost = 0.95, ritualCooldown = 0.95)),
        PrestigeUpgrade("pu_arcane_echoes", "Resonancia", "Un 10% más de probabilidad de Eco por nivel.", BigDecimal("400"), BigDecimal("2.2"), BigDecimal("0.10"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_arcane_rituals", constellation = Constellation.ARCANE, maxLevel = 5, effectsPerLevel = PactEffects(echoChance = 1.10)),
        PrestigeUpgrade(ATTENTION_NODE_ID, "Ojo Interior", "Un punto más de Atención por nivel.", BigDecimal("1000"), BigDecimal("4.0"), BigDecimal("1"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_arcane_echoes", constellation = Constellation.ARCANE, maxLevel = 3),
        PrestigeUpgrade(MADNESS_NODE_ID, "Calma Antigua", "La Locura sube un 8% menos por nivel.", BigDecimal("2500"), BigDecimal("3.0"), BigDecimal("0.08"), UpgradeType.BUILDING_MULTIPLIER, parentId = ATTENTION_NODE_ID, constellation = Constellation.ARCANE, maxLevel = 5),
        // v0.19.4: el resto de Perseo
        PrestigeUpgrade("pu_arcane_medusa", "Mirada de Medusa", "Las runas aparecen un 8% más seguido por nivel.", BigDecimal("4000"), BigDecimal("2.5"), BigDecimal("0.08"), UpgradeType.BUILDING_MULTIPLIER, parentId = MADNESS_NODE_ID, constellation = Constellation.ARCANE, maxLevel = 5, effectsPerLevel = PactEffects(runeFrequency = 1.08)),
        PrestigeUpgrade("pu_arcane_rite", "Rito Prolongado", "Los rituales duran un 10% más por nivel.", BigDecimal("600"), BigDecimal("2.0"), BigDecimal("0.10"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_arcane_echoes", constellation = Constellation.ARCANE, maxLevel = 5, effectsPerLevel = PactEffects(ritualDuration = 1.10)),
        PrestigeUpgrade("pu_arcane_echo", "Eco Persistente", "Un 8% más de probabilidad de Eco por nivel.", BigDecimal("1500"), BigDecimal("2.2"), BigDecimal("0.08"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_arcane_rite", constellation = Constellation.ARCANE, maxLevel = 5, effectsPerLevel = PactEffects(echoChance = 1.08)),
        PrestigeUpgrade("pu_arcane_flame", "Llama Errante", "Los rituales recargan un 5% más rápido por nivel.", BigDecimal("3500"), BigDecimal("2.4"), BigDecimal("0.05"), UpgradeType.BUILDING_MULTIPLIER, parentId = "pu_arcane_echo", constellation = Constellation.ARCANE, maxLevel = 6, effectsPerLevel = PactEffects(ritualCooldown = 0.95)),

        // SENDAS: el centro de cada constelación. Excluyentes (solo una con niveles, ver
        // MadnessPaths) y abiertas al iluminar toda su constelación.
        PrestigeUpgrade(MadnessPaths.HAND, "Senda de la Mano", "Senda excluyente. +300% de clic por nivel.", BigDecimal("2000"), BigDecimal("2.5"), BigDecimal("3.0"), UpgradeType.CLICK_MULTIPLIER, constellation = Constellation.HAND),
        PrestigeUpgrade(MadnessPaths.GEARS, "Senda del Engranaje", "Senda excluyente. +100% a todos los edificios por nivel.", BigDecimal("2000"), BigDecimal("2.5"), BigDecimal("1.0"), UpgradeType.BUILDING_MULTIPLIER, constellation = Constellation.GEARS),
        PrestigeUpgrade(MadnessPaths.DREAM, "Senda del Sueño", "Senda excluyente. +300% de ganancia sin conexión por nivel.", BigDecimal("2000"), BigDecimal("2.5"), BigDecimal("3.0"), UpgradeType.BUILDING_MULTIPLIER, constellation = Constellation.DREAM),
        PrestigeUpgrade(MadnessPaths.MIND, "Senda de la Mente", "Senda excluyente. +50% de producción por nivel.", BigDecimal("2000"), BigDecimal("2.5"), BigDecimal("0.5"), UpgradeType.GLOBAL_MULTIPLIER, constellation = Constellation.MIND),
        PrestigeUpgrade(MadnessPaths.VOID, "Senda del Vacío", "Senda excluyente. +50% de Cordura ganada por nivel.", BigDecimal("2000"), BigDecimal("2.5"), BigDecimal("0.5"), UpgradeType.BUILDING_MULTIPLIER, constellation = Constellation.VOID),
        PrestigeUpgrade(MadnessPaths.CULT, "Senda del Culto", "Senda excluyente. Cada vida empieza con 10 unidades de los cinco primeros edificios de producción por nivel.", BigDecimal("2000"), BigDecimal("2.5"), BigDecimal(CULT_PATH_UNITS), UpgradeType.BUILDING_MULTIPLIER, constellation = Constellation.CULT),
        PrestigeUpgrade(MadnessPaths.ARCANE, "Senda Arcana", "Senda excluyente. Por nivel: runas un 25% más seguido, +25% de probabilidad de Eco y rituales un 10% más baratos.", BigDecimal("2000"), BigDecimal("2.5"), BigDecimal("0.25"), UpgradeType.BUILDING_MULTIPLIER, constellation = Constellation.ARCANE, effectsPerLevel = PactEffects(runeFrequency = 1.25, echoChance = 1.25, ritualCost = 0.9))
    )

    // --- CÁLCULO LEGACY ---
    data class LegacyUpgradeInfo(val baseCost: BigDecimal, val scaling: BigDecimal)
    private val legacyDefinitions = mapOf(
        "p_start" to LegacyUpgradeInfo(BigDecimal("1"), BigDecimal("2.0")),
        "p_global" to LegacyUpgradeInfo(BigDecimal("5"), BigDecimal("3.0")),
        "p_prestige" to LegacyUpgradeInfo(BigDecimal("10"), BigDecimal("4.0")),
        "p_click" to LegacyUpgradeInfo(BigDecimal("3"), BigDecimal("2.5")),
        "p_offline" to LegacyUpgradeInfo(BigDecimal("8"), BigDecimal("3.0"))
    )

    fun calculateLegacyRefund(savedEntities: List<PrestigeUpgradeEntity>): BigDecimal {
        var totalRefund = BigDecimal.ZERO
        for (saved in savedEntities) {
            val legacyDef = legacyDefinitions[saved.id]

            // SOLO si está en la lista de antiguas, calculamos reembolso.
            if (legacyDef != null) {
                val r = legacyDef.scaling
                val a = legacyDef.baseCost
                val n = saved.level
                if (n > 0) {
                    val numerator = r.pow(n).subtract(BigDecimal.ONE)
                    val denominator = r.subtract(BigDecimal.ONE)
                    val totalCost = a.multiply(numerator).divide(denominator, MathContext.DECIMAL64)
                    totalRefund = totalRefund.add(totalCost)
                }
            }
        }
        return totalRefund
    }

    fun loadFromSave(saved: List<PrestigeUpgradeEntity>) {
        // 1. Limpieza de memoria (Reset)
        upgrades.forEach { it.level = 0 }

        // 2. Cargar estado guardado
        saved.forEach { s -> upgrades.find { it.id == s.id }?.level = s.level }
    }

    fun getCost(id: String): BigDecimal? = upgrades.find { it.id == id }?.currentCost

    fun tryBuy(id: String, currentSanity: BigDecimal): Boolean {
        val upgrade = upgrades.find { it.id == id } ?: return false
        if (!canReach(upgrade)) return false
        if (upgrade.isMaxed) return false
        if (MadnessPaths.isSealed(upgrade, upgrades)) return false
        if (currentSanity >= upgrade.currentCost) {
            upgrade.level++
            return true
        }
        return false
    }

    // =========================================================
    // LÓGICA MULTIPLICATIVA (POTENCIA REAL)
    // =========================================================

    // 1. GLOBAL
    fun getPermanentGlobalMultiplier(): BigDecimal {
        var multiplier = BigDecimal.ONE
        upgrades.filter { it.type == UpgradeType.GLOBAL_MULTIPLIER }.forEach {
            val nodeBonus = it.effectMultiplier.multiply(BigDecimal(it.level))
            val nodeMult = BigDecimal.ONE.add(nodeBonus)
            multiplier = multiplier.multiply(nodeMult)
        }
        return multiplier
    }

    // 2. CLIC
    fun getClickPrestigeMultiplier(): BigDecimal {
        var multiplier = BigDecimal.ONE
        upgrades.filter { it.type == UpgradeType.CLICK_MULTIPLIER }.forEach {
            val nodeBonus = it.effectMultiplier.multiply(BigDecimal(it.level))
            val nodeMult = BigDecimal.ONE.add(nodeBonus)
            multiplier = multiplier.multiply(nodeMult)
        }
        return multiplier
    }

    // 3. EDIFICIOS
    fun getBuildingTierMultiplier(buildingIndex: Int): BigDecimal {
        var multiplier = BigDecimal.ONE

        // A. Engranajes Mentales (Tier 1-6)
        if (buildingIndex <= 5) {
            val u = upgrades.find { it.id == "pu_idle_1" }
            if (u != null && u.level > 0) {
                val nodeBonus = u.effectMultiplier.multiply(BigDecimal(u.level))
                val nodeMult = BigDecimal.ONE.add(nodeBonus)
                multiplier = multiplier.multiply(nodeMult)
            }
        }

        // A2. Ruedas Dentadas, Engranajes Dobles y Motor Primordial (v0.19.4): por tramos
        TIER_NODES.forEach { (id, range) ->
            val u = upgrades.find { it.id == id }
            if (u != null && u.level > 0 && buildingIndex in range) {
                multiplier = multiplier.multiply(BigDecimal.ONE.add(u.effectMultiplier.multiply(BigDecimal(u.level))))
            }
        }

        // B. Sincronía y Senda del Engranaje (todos)
        listOf("pu_idle_2", MadnessPaths.GEARS).forEach { id ->
            val u = upgrades.find { it.id == id }
            if (u != null && u.level > 0) {
                val nodeBonus = u.effectMultiplier.multiply(BigDecimal(u.level))
                multiplier = multiplier.multiply(BigDecimal.ONE.add(nodeBonus))
            }
        }

        return multiplier
    }

    // 4. OFFLINE
    fun getOfflinePrestigeMultiplier(): BigDecimal {
        val u1 = upgrades.find { it.id == "pu_offline_1" }
        val u2 = upgrades.find { it.id == "pu_offline_2" }
        val path = upgrades.find { it.id == MadnessPaths.DREAM }
        var mult = BigDecimal.ONE

        if (path != null && path.level > 0) {
            mult = mult.multiply(BigDecimal.ONE.add(path.effectMultiplier.multiply(BigDecimal(path.level))))
        }
        if (u1 != null && u1.level > 0) {
            mult = mult.multiply(BigDecimal.ONE.add(u1.effectMultiplier.multiply(BigDecimal(u1.level))))
        }
        if (u2 != null && u2.level > 0) {
            mult = mult.multiply(BigDecimal.ONE.add(u2.effectMultiplier.multiply(BigDecimal(u2.level))))
        }
        return mult
    }

    // 5. PRESTIGIO
    fun getPrestigeGainMultiplier(): BigDecimal {
        val u1 = upgrades.find { it.id == "pu_void_1" }
        val u2 = upgrades.find { it.id == "pu_void_2" }
        val u3 = upgrades.find { it.id == "pu_void_3" }
        var mult = BigDecimal.ONE

        val path = upgrades.find { it.id == MadnessPaths.VOID }
        listOfNotNull(u1, u2, u3, path).filter { it.level > 0 }.forEach {
            mult = mult.multiply(BigDecimal.ONE.add(it.effectMultiplier.multiply(BigDecimal(it.level))))
        }

        return mult
    }

    /**
     * Abandona la Senda actual (al Ascender): vuelve a nivel 0 y devuelve la Cordura
     * que se gastó en ella, para que el jugador pueda elegir otra.
     */
    /**
     * Deja la Senda y devuelve la Cordura gastada en ella. Los [keptLevels] que vinieron
     * del ciclo anterior (Senda Eterna) no se pagaron en este y no se devuelven.
     */
    fun abandonPath(keptLevels: Int = 0): BigDecimal {
        val path = MadnessPaths.active(upgrades) ?: return BigDecimal.ZERO
        val refund = MadnessPaths.spent(path, keptLevels)
        path.level = 0
        return refund
    }

    /** Legado del Culto: Notas Desordenadas con las que empieza cada vida. */
    fun getStartingNotes(): Int = (upgrades.find { it.id == STARTING_NOTES_ID }?.level ?: 0) * NOTES_PER_LEVEL

    /**
     * Si el nodo se puede comprar por su posición en el cielo: su anterior en la cadena
     * tiene nivel o, si es una Senda, su constelación está completa (o ya la recorre).
     */
    /**
     * Cielo Recordado (hito de Trascendencia): El Despertar y la primera estrella de cada
     * constelación empiezan el ciclo a nivel 1.
     */
    fun rememberSky() {
        upgrades.filter { it.id == CORE_NODE_ID || it.parentId == CORE_NODE_ID }
            .filter { !MadnessPaths.isPath(it.id) }
            .forEach { if (it.level < 1) it.level = 1 }
    }

    fun canReach(node: PrestigeUpgrade): Boolean {
        if (MadnessPaths.isPath(node.id)) return MadnessPaths.isAvailable(node, upgrades)
        val parentId = node.parentId ?: return true
        return (upgrades.find { it.id == parentId }?.level ?: 0) > 0
    }

    /** Edificios con los que empieza cada vida (El Culto y su Senda), por id. */
    fun getStartingBuildings(): Map<String, Int> {
        val start = HashMap<String, Int>()
        upgrades.filter { it.startingBuilding != null && it.level > 0 }.forEach { node ->
            val id = node.startingBuilding ?: return@forEach
            start[id] = (start[id] ?: 0) + node.effectMultiplier.toInt() * node.level
        }
        val cult = upgrades.find { it.id == MadnessPaths.CULT }?.level ?: 0
        if (cult > 0) CULT_PATH_BUILDINGS.forEach { id -> start[id] = (start[id] ?: 0) + CULT_PATH_UNITS * cult }
        return start
    }

    /** Los efectos de tipo pacto del Mapa (runas, rituales, Ecos, costos, eventos). */
    fun getMapEffects(): PactEffects = upgrades
        .filter { it.effectsPerLevel != null && it.level > 0 }
        .fold(PactEffects()) { acc, node -> acc.combinedWith(node.effectsPerLevel!!.scaled(node.level.toDouble())) }

    /** Ojo Interior: puntos de Atención extra. */
    fun getAttentionBonus(): Int = upgrades.find { it.id == ATTENTION_NODE_ID }?.level ?: 0

    /** Mente Templada: cuánto de la Locura que se gana se conserva (0,92 por nivel menos). */
    fun getMadnessGainMultiplier(): Double {
        val node = upgrades.find { it.id == MADNESS_NODE_ID } ?: return 1.0
        return (1.0 - node.effectMultiplier.toDouble() * node.level).coerceAtLeast(0.1)
    }

    /** Si toda la constelación (sin contar su Senda) tiene al menos un nivel. */
    fun isComplete(constellation: Constellation): Boolean =
        MadnessPaths.isComplete(constellation, upgrades)

    fun completedConstellations(): Int = Constellation.entries.count { isComplete(it) }

    fun getStartingKnowledgeBonus(): BigDecimal = BigDecimal.ZERO

    companion object {
        const val CORE_NODE_ID = "pu_core"
        const val STARTING_NOTES_ID = "pu_legacy"
        const val NOTES_PER_LEVEL = 5
        const val ATTENTION_NODE_ID = "pu_arcane_attention"
        /** Tramos de edificios (índice en el catálogo, 0 = Sinapsis) de los nodos del Engranaje. */
        val TIER_NODES = mapOf("pu_gear_wheels" to 0..5, "pu_gear_double" to 6..10, "pu_gear_engine" to 11..15)
        const val MADNESS_NODE_ID = "pu_arcane_madness"
        /** Senda del Culto: unidades de cada uno de estos edificios por nivel. */
        const val CULT_PATH_UNITS = 10
        val CULT_PATH_BUILDINGS = listOf("b_notes", "b_scrolls", "b_artifact", "b_cultist", "b_priest")
    }
}
