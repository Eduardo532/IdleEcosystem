package com.EGomez.idleecosystem.domain.logic

import android.util.Base64
import android.util.Log
import com.EGomez.idleecosystem.data.database.GameStatisticsEntity
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.Upgrade
import com.google.gson.Gson
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

// Estructura completa del JSON de guardado
data class FullSaveData(
    val knowledge: BigDecimal,
    val sanity: BigDecimal,
    val echoes: BigDecimal, // <--- CAMPO OBLIGATORIO AGREGADO
    val buildings: List<BuildingSaveModel>,
    val upgrades: List<String>,
    val achievements: List<String>,
    val prestigeUpgrades: List<PrestigeUpgradeSaveModel>,
    val timestamp: Long,
    val statistics: GameStatisticsEntity
)

data class BuildingSaveModel(val id: String, val amount: Int, val isVisible: Boolean)
data class PrestigeUpgradeSaveModel(val id: String, val level: Int)

@Singleton
class SaveManager @Inject constructor() {

    private val gson = Gson()

    fun exportSave(
        knowledge: BigDecimal,
        sanity: BigDecimal,
        echoes: BigDecimal, // <--- PARAMETRO AGREGADO
        buildings: List<Building>,
        upgrades: List<Upgrade>,
        achievements: List<Achievement>,
        prestigeUpgrades: List<PrestigeUpgrade>,
        statistics: GameStatisticsEntity
    ): String {
        try {
            val buildingSaves = buildings.map { BuildingSaveModel(it.id, it.amountOwned, it.isVisible) }
            val upgradeIds = upgrades.filter { it.isBought }.map { it.id }
            val achievementIds = achievements.filter { it.isUnlocked }.map { it.id }
            val prestigeSaves = prestigeUpgrades.filter { it.level > 0 }.map { PrestigeUpgradeSaveModel(it.id, it.level) }

            val saveData = FullSaveData(
                knowledge = knowledge,
                sanity = sanity,
                echoes = echoes, // <--- GUARDANDO ECOS
                buildings = buildingSaves,
                upgrades = upgradeIds,
                achievements = achievementIds,
                prestigeUpgrades = prestigeSaves,
                timestamp = System.currentTimeMillis(),
                statistics = statistics
            )

            val jsonString = gson.toJson(saveData)
            return Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        } catch (e: Exception) {
            Log.e(TAG, "No se pudo exportar la partida", e)
            return ""
        }
    }

    fun importSave(saveString: String): FullSaveData? {
        return try {
            val decodedBytes = Base64.decode(saveString.trim(), Base64.NO_WRAP)
            parseJson(String(decodedBytes, Charsets.UTF_8))
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo decodificar el código de partida", e)
            null
        }
    }

    /**
     * Convierte el JSON en datos válidos: sin nulls, sin negativos, sin duplicados.
     *
     * Gson crea los objetos sin pasar por el constructor de Kotlin, así que un campo
     * ausente (por ejemplo "echoes" en un código antiguo) quedaba en null dentro de un
     * tipo no nulo y la app crasheaba más tarde. Por eso se lee primero en [RawSaveData],
     * donde todo es opcional, y luego se normaliza.
     */
    fun parseJson(json: String): FullSaveData? {
        val raw = try {
            gson.fromJson(json, RawSaveData::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "JSON de partida inválido", e)
            null
        } ?: return null

        // Un código sin conocimiento ni edificios no es una partida
        if (raw.knowledge == null && raw.buildings == null) return null

        return FullSaveData(
            knowledge = raw.knowledge.nonNegative(),
            sanity = raw.sanity.nonNegative(),
            echoes = raw.echoes.nonNegative(),
            buildings = raw.buildings.orEmpty().filterNotNull().mapNotNull { b ->
                val id = b.id ?: return@mapNotNull null
                BuildingSaveModel(id, (b.amount ?: 0).coerceAtLeast(0), b.isVisible ?: false)
            },
            upgrades = raw.upgrades.orEmpty().filterNotNull().distinct(),
            achievements = raw.achievements.orEmpty().filterNotNull().distinct(),
            prestigeUpgrades = raw.prestigeUpgrades.orEmpty().filterNotNull().mapNotNull { p ->
                val id = p.id ?: return@mapNotNull null
                PrestigeUpgradeSaveModel(id, (p.level ?: 0).coerceAtLeast(0))
            },
            timestamp = raw.timestamp ?: System.currentTimeMillis(),
            statistics = raw.statistics.toEntity()
        )
    }

    private fun BigDecimal?.nonNegative(): BigDecimal =
        if (this == null || this.signum() < 0) BigDecimal.ZERO else this

    private fun String?.numberOrZero(): String =
        try { BigDecimal(this ?: "0").max(BigDecimal.ZERO).toString() } catch (e: NumberFormatException) { "0" }

    private fun RawStatistics?.toEntity(): GameStatisticsEntity {
        val s = this
        return GameStatisticsEntity(
            id = 1,
            totalPlayTimeSeconds = (s?.totalPlayTimeSeconds ?: 0).coerceAtLeast(0),
            startTime = s?.startTime ?: System.currentTimeMillis(),
            runTimeSeconds = (s?.runTimeSeconds ?: 0).coerceAtLeast(0),
            totalOfflineSeconds = (s?.totalOfflineSeconds ?: 0).coerceAtLeast(0),
            lifetimeKnowledge = s?.lifetimeKnowledge.numberOrZero(),
            runKnowledge = s?.runKnowledge.numberOrZero(),
            maxKnowledgePerSecond = s?.maxKnowledgePerSecond.numberOrZero(),
            totalClicks = (s?.totalClicks ?: 0).coerceAtLeast(0),
            manualKnowledgeGenerated = s?.manualKnowledgeGenerated.numberOrZero(),
            totalPrestiges = (s?.totalPrestiges ?: 0).coerceAtLeast(0),
            lifetimeSanity = s?.lifetimeSanity.numberOrZero(),
            spentSanity = s?.spentSanity.numberOrZero(),
            totalEchoes = s?.totalEchoes.numberOrZero(),
            totalEventsFound = (s?.totalEventsFound ?: 0).coerceAtLeast(0),
            runesSevered = (s?.runesSevered ?: 0).coerceAtLeast(0),
            runesStabilized = (s?.runesStabilized ?: 0).coerceAtLeast(0),
            runesInscribed = (s?.runesInscribed ?: 0).coerceAtLeast(0)
        )
    }

    private companion object {
        const val TAG = "SaveManager"
    }
}

// Versión tolerante del JSON (todo opcional) usada solo para importar
private data class RawSaveData(
    val knowledge: BigDecimal?,
    val sanity: BigDecimal?,
    val echoes: BigDecimal?,
    val buildings: List<RawBuilding?>?,
    val upgrades: List<String?>?,
    val achievements: List<String?>?,
    val prestigeUpgrades: List<RawPrestigeUpgrade?>?,
    val timestamp: Long?,
    val statistics: RawStatistics?
)

private data class RawBuilding(val id: String?, val amount: Int?, val isVisible: Boolean?)
private data class RawPrestigeUpgrade(val id: String?, val level: Int?)

private data class RawStatistics(
    val totalPlayTimeSeconds: Long?,
    val startTime: Long?,
    val runTimeSeconds: Long?,
    val totalOfflineSeconds: Long?,
    val lifetimeKnowledge: String?,
    val runKnowledge: String?,
    val maxKnowledgePerSecond: String?,
    val totalClicks: Long?,
    val manualKnowledgeGenerated: String?,
    val totalPrestiges: Int?,
    val lifetimeSanity: String?,
    val spentSanity: String?,
    val totalEchoes: String?,
    val totalEventsFound: Int?,
    val runesSevered: Int?,
    val runesStabilized: Int?,
    val runesInscribed: Int?
)
