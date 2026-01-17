package com.EGomez.idleecosystem.domain.logic

import android.util.Base64
import com.EGomez.idleecosystem.data.database.GameStatisticsEntity
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.Upgrade
import com.google.gson.Gson // Asegúrate de tener Gson o usa construcción manual si no.
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

// Clase de datos para estructurar el guardado completo
data class FullSaveData(
    val knowledge: BigDecimal,
    val sanity: BigDecimal,
    val buildings: List<BuildingSaveModel>,
    val upgrades: List<String>,
    val achievements: List<String>,
    val prestigeUpgrades: List<PrestigeUpgradeSaveModel>,
    val timestamp: Long,
    val statistics: GameStatisticsEntity // <--- NUEVO CAMPO
)

data class BuildingSaveModel(val id: String, val amount: Int, val isVisible: Boolean)
data class PrestigeUpgradeSaveModel(val id: String, val level: Int)

@Singleton
class SaveManager @Inject constructor() {

    private val gson = Gson()

    fun exportSave(
        knowledge: BigDecimal,
        sanity: BigDecimal,
        buildings: List<Building>,
        upgrades: List<Upgrade>,
        achievements: List<Achievement>,
        prestigeUpgrades: List<PrestigeUpgrade>,
        statistics: GameStatisticsEntity // <--- RECIBIMOS ESTADÍSTICAS
    ): String {
        try {
            // 1. Convertimos los modelos de dominio a modelos de guardado ligeros
            val buildingSaves = buildings.map { BuildingSaveModel(it.id, it.amountOwned, it.isVisible) }
            val upgradeIds = upgrades.filter { it.isBought }.map { it.id }
            val achievementIds = achievements.filter { it.isUnlocked }.map { it.id }
            val prestigeSaves = prestigeUpgrades.filter { it.level > 0 }.map { PrestigeUpgradeSaveModel(it.id, it.level) }

            // 2. Creamos el objeto completo
            val saveData = FullSaveData(
                knowledge = knowledge,
                sanity = sanity,
                buildings = buildingSaves,
                upgrades = upgradeIds,
                achievements = achievementIds,
                prestigeUpgrades = prestigeSaves,
                timestamp = System.currentTimeMillis(),
                statistics = statistics // <--- GUARDAMOS
            )

            // 3. Serializamos a JSON
            val jsonString = gson.toJson(saveData)

            // 4. Encriptamos en Base64 para que parezca un "código de guardado"
            return Base64.encodeToString(jsonString.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun importSave(saveString: String): FullSaveData? {
        return try {
            // 1. Decodificar Base64
            val decodedBytes = Base64.decode(saveString, Base64.NO_WRAP)
            val jsonString = String(decodedBytes, Charsets.UTF_8)

            // 2. Deserializar con Gson
            gson.fromJson(jsonString, FullSaveData::class.java)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}