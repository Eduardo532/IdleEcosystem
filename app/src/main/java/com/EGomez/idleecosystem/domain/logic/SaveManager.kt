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
            e.printStackTrace()
            return ""
        }
    }

    fun importSave(saveString: String): FullSaveData? {
        return try {
            val decodedBytes = Base64.decode(saveString, Base64.NO_WRAP)
            val jsonString = String(decodedBytes, Charsets.UTF_8)
            gson.fromJson(jsonString, FullSaveData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}