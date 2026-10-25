package com.EGomez.idleecosystem.domain.logic

import android.util.Base64
import com.EGomez.idleecosystem.data.database.GameStatisticsEntity
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.Upgrade
import com.google.gson.Gson
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

// Estructura completa del JSON de guardado (lo que se exporta)
data class FullSaveData(
    val knowledge: BigDecimal,
    val sanity: BigDecimal,
    val echoes: BigDecimal,
    val buildings: List<BuildingSaveModel>,
    val upgrades: List<String>,
    val achievements: List<String>,
    val prestigeUpgrades: List<PrestigeUpgradeSaveModel>,
    val timestamp: Long,
    val statistics: GameStatisticsEntity
)

data class BuildingSaveModel(val id: String, val amount: Int, val isVisible: Boolean)
data class PrestigeUpgradeSaveModel(val id: String, val level: Int)

/**
 * Versión "tolerante" del JSON para importar. Gson ignora la nulabilidad de Kotlin:
 * si un código antiguo no trae un campo (por ejemplo "echoes"), lo deja en null y
 * la app crasheaba más tarde. Aquí todo es opcional y se normaliza al importar.
 */
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
    val totalEventsFound: Int?,
    val totalEchoes: String?
)

/** Permite cambiar la implementación de Base64 en tests unitarios (android.util no existe en la JVM). */
interface Base64Codec {
    fun encode(bytes: ByteArray): String
    fun decode(text: String): ByteArray
}

object AndroidBase64Codec : Base64Codec {
    override fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    override fun decode(text: String): ByteArray = Base64.decode(text.trim(), Base64.NO_WRAP)
}

@Singleton
class SaveManager internal constructor(private val codec: Base64Codec) {

    @Inject
    constructor() : this(AndroidBase64Codec)

    private val gson = Gson()

    fun exportSave(
        knowledge: BigDecimal,
        sanity: BigDecimal,
        echoes: BigDecimal,
        buildings: List<Building>,
        upgrades: List<Upgrade>,
        achievements: List<Achievement>,
        prestigeUpgrades: List<PrestigeUpgrade>,
        statistics: GameStatisticsEntity
    ): String {
        return try {
            val saveData = FullSaveData(
                knowledge = knowledge,
                sanity = sanity,
                echoes = echoes,
                buildings = buildings.map { BuildingSaveModel(it.id, it.amountOwned, it.isVisible) },
                upgrades = upgrades.filter { it.isBought }.map { it.id },
                achievements = achievements.filter { it.isUnlocked }.map { it.id },
                prestigeUpgrades = prestigeUpgrades.filter { it.level > 0 }.map { PrestigeUpgradeSaveModel(it.id, it.level) },
                timestamp = System.currentTimeMillis(),
                statistics = statistics
            )
            codec.encode(toJson(saveData).toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun importSave(saveString: String): FullSaveData? {
        return try {
            val decodedBytes = codec.decode(saveString)
            parseJson(String(decodedBytes, Charsets.UTF_8))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    internal fun toJson(data: FullSaveData): String = gson.toJson(data)

    /** Convierte el JSON en datos válidos: sin nulls, sin negativos, sin duplicados. */
    internal fun parseJson(json: String): FullSaveData? {
        val raw = try {
            gson.fromJson(json, RawSaveData::class.java)
        } catch (e: Exception) {
            null
        } ?: return null

        // Un código sin conocimiento ni edificios no es una partida válida
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
        try { BigDecimal(this ?: "0").max(BigDecimal.ZERO).toString() } catch (e: Exception) { "0" }

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
            totalEventsFound = (s?.totalEventsFound ?: 0).coerceAtLeast(0),
            totalEchoes = s?.totalEchoes.numberOrZero()
        )
    }
}
