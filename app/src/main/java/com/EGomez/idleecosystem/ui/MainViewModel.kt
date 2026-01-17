package com.EGomez.idleecosystem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.data.repository.GameRepository
import com.EGomez.idleecosystem.domain.logic.*
import com.EGomez.idleecosystem.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ExportPreview(val money: String, val achievements: String, val date: String)

data class GameUiState(
    val knowledgeFormatted: String = "0",
    val knowledgePerSecond: String = "0",
    val rawKnowledge: BigDecimal = BigDecimal.ZERO,
    val clickMultiplier: String = "1.0",
    val globalMultiplier: BigDecimal = BigDecimal.ONE,
    val buyMultiplier: Int = 1,
    val prestigeCurrencyFormatted: String = "0",
    val potentialPrestigeFormatted: String = "0",
    val canPrestige: Boolean = false,
    val prestigeUpgrades: List<PrestigeUpgrade> = emptyList(),

    // --- ESTADÍSTICAS DE LOGROS ---
    val achievementsProgress: String = "0 / 100",
    val secretsProgress: String? = null,

    // Offline / Reembolso
    val showOfflineDialog: Boolean = false,
    val offlineGains: String = "0",
    val offlineTimeFormatted: String = "0s",
    val showRefundDialog: Boolean = false,
    val refundAmountString: String = "0",

    // Notificación de Logros
    val lastUnlockedAchievement: Achievement? = null,

    // Guardado / Configuración
    val saveStringExport: String = "",
    val exportPreview: ExportPreview? = null,
    val importPreview: ExportPreview? = null,
    val pendingImportString: String = "",
    val importError: String? = null,

    // Audio
    val isMusicEnabled: Boolean = true,
    val isSfxEnabled: Boolean = true,

    // Eventos
    val currentEvent: RandomEvent? = null,
    val eventResultText: String? = null,
    val activeBuffMultiplier: Double = 1.0,
    val buffTimeRemaining: Int = 0
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: GameRepository,
    private val buildingManager: BuildingManager,
    private val upgradeManager: UpgradeManager,
    private val prestigeManager: PrestigeManager,
    private val achievementManager: AchievementManager,
    private val prestigeShopManager: PrestigeShopManager,
    private val saveManager: SaveManager,
    private val randomEventManager: RandomEventManager,
    private val soundManager: SoundManager,
    private val statisticsManager: StatisticsManager,
    private val ritualManager: RitualManager // <--- NUEVA INYECCIÓN
) : ViewModel() {

    private var arcaneKnowledge = BigDecimal.ZERO
    private var productionPerSecond = BigDecimal.ZERO
    private val transactionMutex = Mutex()
    private var timeUntilNextEvent = 0L
    private var activeBuffExpiration = 0L

    // --- STATS: Acumulador de tiempo ---
    private var oneSecondAccumulator = 0L

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _buildingsState = MutableStateFlow<List<Building>>(emptyList())
    val buildingsState = _buildingsState.asStateFlow()

    private val _upgradesState = MutableStateFlow<List<Upgrade>>(emptyList())
    val upgradesState = _upgradesState.asStateFlow()

    private val _achievementsState = MutableStateFlow<List<Achievement>>(emptyList())
    val achievementsState = _achievementsState.asStateFlow()

    private val _showPurchasedState = MutableStateFlow(false)
    val showPurchasedState: StateFlow<Boolean> = _showPurchasedState.asStateFlow()

    // --- RITUALES: Exponer Estado ---
    val ritualsState = ritualManager.rituals

    // --- STATS: Exponer el Manager para la UI ---
    val statsManager: StatisticsManager get() = statisticsManager

    init {
        // Sincronizar estado inicial de audio
        _uiState.value = _uiState.value.copy(
            isMusicEnabled = soundManager.isMusicEnabled,
            isSfxEnabled = soundManager.isSfxEnabled
        )
        loadGameDataAndStart()
        startAutoSaveLoop()
        startEventSystem()
    }

    private fun loadGameDataAndStart() {
        viewModelScope.launch {
            transactionMutex.withLock {
                // 1. Cargar datos crudos
                val entity = repository.gameState.firstOrNull()
                val savedBuildings = repository.loadBuildings()
                val savedUpgrades = repository.loadUpgrades()
                val savedAchievements = repository.loadAchievements()
                val savedPrestigeShop = repository.loadPrestigeUpgrades()

                // --- Cargar estadísticas ---
                val savedStats = repository.gameStatistics.firstOrNull()
                statisticsManager.loadFromEntity(savedStats)

                // 2. Calcular Reembolso (Legacy)
                val refundAmount = prestigeShopManager.calculateLegacyRefund(savedPrestigeShop)
                var initialSanity = if (entity != null) BigDecimal(entity.prestigeCurrency) else BigDecimal.ZERO
                var showRefund = false

                // 3. LIMPIEZA DE MEMORIA OBLIGATORIA
                buildingManager.resetForPrestige()
                upgradeManager.resetForPrestige()
                achievementManager.resetAchievements()
                prestigeShopManager.upgrades.forEach { it.level = 0 }

                // 4. Lógica de Carga / Reembolso
                if (refundAmount > BigDecimal.ZERO) {
                    initialSanity = initialSanity.add(refundAmount)
                    prestigeManager.residualSanity = initialSanity
                    showRefund = true

                    repository.clearAllPrestigeUpgrades()
                    prestigeShopManager.upgrades.forEach { repository.savePrestigeUpgrade(it.id, 0) }
                    repository.saveGame(if (entity != null) BigDecimal(entity.money) else BigDecimal.ZERO, BigDecimal.ZERO, initialSanity)
                } else {
                    if (savedPrestigeShop.isNotEmpty()) prestigeShopManager.loadFromSave(savedPrestigeShop)
                    if (savedBuildings.isNotEmpty()) buildingManager.loadFromSave(savedBuildings)
                    if (savedUpgrades.isNotEmpty()) upgradeManager.loadFromSave(savedUpgrades)
                    if (savedAchievements.isNotEmpty()) achievementManager.loadFromSave(savedAchievements)

                    prestigeManager.residualSanity = initialSanity
                }

                // 5. Offline / Nueva Partida
                if (entity != null) {
                    arcaneKnowledge = BigDecimal(entity.money)
                    val currentTime = System.currentTimeMillis()
                    val lastTime = entity.lastLoginTime
                    if (lastTime > 0) {
                        val diffMillis = currentTime - lastTime
                        if (diffMillis > 10000) calculateOfflineGains(diffMillis)
                    }
                } else {
                    repository.createNewGame()
                    arcaneKnowledge = BigDecimal.ZERO.add(prestigeShopManager.getStartingKnowledgeBonus())
                }

                recalculateState()

                if (showRefund) {
                    _uiState.value = _uiState.value.copy(
                        showRefundDialog = true,
                        refundAmountString = refundAmount.toBeautifulString()
                    )
                }
                updateUi()
            }
            startGameLoop()
        }
    }

    private fun calculateOfflineGains(diffMillis: Long) {
        val diffSeconds = diffMillis / 1000
        val secondsBD = BigDecimal(diffSeconds)
        recalculateState()

        val shopOfflineBonus = prestigeShopManager.getOfflinePrestigeMultiplier()
        val finalOfflineProd = productionPerSecond.multiply(shopOfflineBonus)
        val gains = finalOfflineProd.multiply(secondsBD)

        if (gains > BigDecimal.ZERO) {
            addKnowledgeInternal(gains, isManual = false) // Offline cuenta como pasivo

            statisticsManager.trackOfflineTime(diffSeconds)

            val timeFormatted = com.EGomez.idleecosystem.core.math.formatSecondsToTime(diffSeconds)
            _uiState.value = _uiState.value.copy(
                showOfflineDialog = true,
                offlineGains = gains.toBeautifulString(),
                offlineTimeFormatted = timeFormatted
            )
        }
    }

    fun dismissOfflineDialog() { _uiState.value = _uiState.value.copy(showOfflineDialog = false) }
    fun dismissRefundDialog() { _uiState.value = _uiState.value.copy(showRefundDialog = false) }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (true) {
                delay(100)

                // --- STATS: Rastrear Tiempo (1 segundo cada 10 ciclos de 100ms) ---
                oneSecondAccumulator += 100
                if (oneSecondAccumulator >= 1000) {
                    statisticsManager.trackPlayTime(1)
                    // --- STATS: Rastrear Pico de Producción ---
                    statisticsManager.trackProductionPeak(productionPerSecond)

                    // --- RITUALES: Tick de 1 segundo ---
                    ritualManager.tick(1)

                    oneSecondAccumulator -= 1000
                }

                buildingManager.checkUnlocks(arcaneKnowledge)

                // Chequeo de logros
                val newUnlockList = achievementManager.checkAchievements(arcaneKnowledge, buildingManager.availableBuildings)

                if (newUnlockList.isNotEmpty()) {
                    soundManager.playSound(SoundType.ACHIEVEMENT)
                    saveAchievementsToDb()
                    val achievementToShow = newUnlockList.last()
                    _uiState.value = _uiState.value.copy(lastUnlockedAchievement = achievementToShow)
                    launch {
                        delay(4000)
                        if (_uiState.value.lastUnlockedAchievement == achievementToShow) {
                            _uiState.value = _uiState.value.copy(lastUnlockedAchievement = null)
                        }
                    }
                }

                recalculateState()
                val productionTick = productionPerSecond.divide(BigDecimal.TEN)
                if (productionTick > BigDecimal.ZERO) {
                    // --- STATS: Pasamos false porque es producción automática ---
                    addKnowledgeInternal(productionTick, isManual = false)
                }
            }
        }
    }

    private fun startAutoSaveLoop() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                saveProgress()
            }
        }
    }

    // --- EVENTOS ---
    private fun startEventSystem() {
        scheduleNextEvent()
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentTime = System.currentTimeMillis()
                if (currentTime < activeBuffExpiration) {
                    val remaining = ((activeBuffExpiration - currentTime) / 1000).toInt()
                    _uiState.value = _uiState.value.copy(buffTimeRemaining = remaining)
                } else if (_uiState.value.activeBuffMultiplier > 1.0) {
                    _uiState.value = _uiState.value.copy(activeBuffMultiplier = 1.0, buffTimeRemaining = 0)
                    recalculateState()
                }
                if (_uiState.value.currentEvent == null && currentTime >= timeUntilNextEvent) {
                    triggerRandomEvent()
                }
            }
        }
    }

    private fun scheduleNextEvent() {
        val delayMs = kotlin.random.Random.nextLong(90_000, 240_000)
        timeUntilNextEvent = System.currentTimeMillis() + delayMs
    }

    private fun triggerRandomEvent() {
        val event = randomEventManager.getRandomEvent(arcaneKnowledge, prestigeManager.residualSanity)

        if (event != null) {
            soundManager.playSound(SoundType.EVENT_APPEAR)
            // --- STATS: Rastrear Evento Encontrado ---
            statisticsManager.trackEventFound()

            _uiState.value = _uiState.value.copy(currentEvent = event, eventResultText = null)
        } else {
            scheduleNextEvent()
        }
    }

    fun onEventOptionSelected(optionIndex: Int) {
        val event = _uiState.value.currentEvent ?: return
        val option = event.options.getOrNull(optionIndex) ?: return

        option.effects.forEach { effect ->
            when (effect.type) {
                EffectType.ADD_KNOWLEDGE_PERCENT -> {
                    val percent = BigDecimal.valueOf(effect.value)
                    val bonus = arcaneKnowledge.multiply(percent)
                    if (bonus != BigDecimal.ZERO) addKnowledgeInternal(bonus, isManual = false)
                }
                EffectType.ADD_SANITY_PERCENT -> {
                    val percent = BigDecimal.valueOf(effect.value)
                    val amount = prestigeManager.residualSanity.multiply(percent)
                    prestigeManager.residualSanity = prestigeManager.residualSanity.add(amount)
                    if (prestigeManager.residualSanity < BigDecimal.ZERO) prestigeManager.residualSanity = BigDecimal.ZERO
                }
                EffectType.GLOBAL_BUFF -> {
                    _uiState.value = _uiState.value.copy(activeBuffMultiplier = effect.value)
                    activeBuffExpiration = System.currentTimeMillis() + (effect.durationSeconds * 1000)
                    recalculateState()
                }
                EffectType.NOTHING -> { }
            }
        }
        _uiState.value = _uiState.value.copy(eventResultText = option.flavorResult)
        viewModelScope.launch {
            delay(2500)
            _uiState.value = _uiState.value.copy(currentEvent = null, eventResultText = null)
            scheduleNextEvent()
            updateUi()
        }
    }

    private fun recalculateState() {
        val upgradesGlobal = upgradeManager.getGlobalMultiplier()
        val prestigeGlobal = prestigeManager.getPrestigeMultiplier()
        val achievementGlobal = achievementManager.getGlobalAchievementMultiplier()
        val shopGlobal = prestigeShopManager.getPermanentGlobalMultiplier()
        val buffMult = BigDecimal(_uiState.value.activeBuffMultiplier)

        // --- RITUALES: Multiplicador Global ---
        val ritualProdMult = BigDecimal.valueOf(ritualManager.getActiveProductionMultiplier())

        val finalGlobalMult = upgradesGlobal
            .multiply(prestigeGlobal)
            .multiply(achievementGlobal)
            .multiply(shopGlobal)
            .multiply(buffMult)
            .multiply(ritualProdMult) // APLICADO AQUÍ

        val shopClickBonus = prestigeShopManager.getClickPrestigeMultiplier()

        // --- RITUALES: Multiplicador de Click ---
        val ritualClickMult = BigDecimal.valueOf(ritualManager.getActiveClickMultiplier())

        buildingManager.availableBuildings.forEachIndexed { index, building ->
            val shopTierBonus = prestigeShopManager.getBuildingTierMultiplier(index)
            val specificMult = if (building.type == BuildingType.CLICK) {
                upgradeManager.getRawClickMultiplier()
                    .multiply(shopClickBonus)
                    .multiply(ritualClickMult) // APLICADO AQUÍ PARA CLICKS
            } else {
                upgradeManager.getSpecificBuildingMultiplier(building.id).multiply(shopTierBonus)
            }
            building.currentMultiplier = specificMult.multiply(finalGlobalMult)
        }

        productionPerSecond = buildingManager.getTotalProductionPerSecond()
        val basePotential = prestigeManager.calculatePotentialPrestige(arcaneKnowledge)
        val shopPrestigeBonus = prestigeShopManager.getPrestigeGainMultiplier()
        val finalPotential = basePotential.multiply(shopPrestigeBonus)
        val canAscend = finalPotential > BigDecimal.ZERO

        _buildingsState.value = buildingManager.availableBuildings.filter { it.isVisible }
        _upgradesState.value = upgradeManager.availableUpgrades.filter { if (_showPurchasedState.value) true else !it.isBought }.sortedBy { it.cost }

        // LÓGICA DE FILTRADO Y CONTEO DE LOGROS
        val allAchievements = achievementManager.availableAchievements
        val unlockedTotal = allAchievements.count { it.isUnlocked }

        val secretsFound = allAchievements.count { it.isSecret && it.isUnlocked }
        val secretsTotal = 13

        val visibleAchievements = allAchievements
            .filter { !it.isSecret || it.isUnlocked }
            .sortedBy { it.order }

        _achievementsState.value = visibleAchievements

        val progressText = "$unlockedTotal / 100"
        val secretsText = if (secretsFound > 0) "Secretos: $secretsFound / $secretsTotal" else null

        val currentUi = _uiState.value
        _uiState.value = currentUi.copy(
            potentialPrestigeFormatted = finalPotential.toBeautifulString(),
            canPrestige = canAscend,
            globalMultiplier = finalGlobalMult,
            prestigeUpgrades = prestigeShopManager.upgrades.toList(),
            achievementsProgress = progressText,
            secretsProgress = secretsText
        )
    }

    fun setBuyMultiplier(value: Int) {
        _uiState.value = _uiState.value.copy(buyMultiplier = value)
    }

    fun onManualClick(): BigDecimal {
        val totalClickPower = buildingManager.getClickBuildingBonus().max(BigDecimal.ONE)
        val finalPower = totalClickPower
        if (finalPower > BigDecimal.ZERO) {
            soundManager.playSound(SoundType.CLICK, 250)

            // --- STATS: Registrar Click y ganancia manual ---
            statisticsManager.trackClick()
            addKnowledgeInternal(finalPower, isManual = true)
        }
        return finalPower
    }

    // --- RITUALES: Acción de Activación ---
    fun onActivateRitual(ritualId: String) {
        viewModelScope.launch {
            transactionMutex.withLock {
                val ritual = ritualManager.rituals.value.find { it.id == ritualId } ?: return@withLock
                if (ritualManager.tryActivateRitual(ritualId, arcaneKnowledge)) {
                    soundManager.playSound(SoundType.BUY)
                    arcaneKnowledge = arcaneKnowledge.subtract(ritual.baseCost)
                    recalculateState() // Actualiza multiplicadores inmediatamente
                    updateUi()
                }
            }
        }
    }

    fun onBuyBuilding(buildingId: String) {
        viewModelScope.launch {
            transactionMutex.withLock {
                val multiplier = _uiState.value.buyMultiplier
                val costBefore = buildingManager.getCost(buildingId, multiplier, arcaneKnowledge)
                val amountBought = buildingManager.tryBuyBuilding(buildingId, arcaneKnowledge, multiplier)
                if (amountBought > 0) {
                    soundManager.playSound(SoundType.BUY)
                    arcaneKnowledge = arcaneKnowledge.subtract(costBefore)
                    recalculateState()
                    updateUi()
                    saveProgressInternal()
                    saveBuildingToDb(buildingId)
                }
            }
        }
    }

    fun onBuyUpgrade(upgradeId: String) {
        viewModelScope.launch {
            transactionMutex.withLock {
                val upgrade = upgradeManager.availableUpgrades.find { it.id == upgradeId } ?: return@withLock
                val success = upgradeManager.tryBuyUpgrade(upgradeId, arcaneKnowledge)
                if (success) {
                    soundManager.playSound(SoundType.BUY)
                    arcaneKnowledge = arcaneKnowledge.subtract(upgrade.cost)
                    recalculateState()
                    updateUi()
                    saveProgressInternal()
                    repository.saveUpgrade(upgradeId, true)
                }
            }
        }
    }

    fun onBuyPrestigeUpgrade(id: String) {
        viewModelScope.launch {
            transactionMutex.withLock {
                val cost = prestigeShopManager.getCost(id) ?: return@withLock
                if (prestigeManager.residualSanity >= cost) {
                    val success = prestigeShopManager.tryBuy(id, prestigeManager.residualSanity)
                    if (success) {
                        soundManager.playSound(SoundType.BUY)
                        prestigeManager.residualSanity = prestigeManager.residualSanity.subtract(cost)

                        // --- STATS: Registrar Gasto de Cordura ---
                        statisticsManager.trackSanitySpent(cost)

                        val upgrade = prestigeShopManager.upgrades.find { it.id == id }
                        if (upgrade != null) repository.savePrestigeUpgrade(id, upgrade.level)
                        recalculateState()
                        updateUi()
                        saveProgressInternal()
                    }
                }
            }
        }
    }

    fun toggleShowPurchased() {
        _showPurchasedState.value = !_showPurchasedState.value
        recalculateState()
    }

    fun onAscendClicked() {
        viewModelScope.launch {
            transactionMutex.withLock {
                val basePotential = prestigeManager.calculatePotentialPrestige(arcaneKnowledge)
                val shopBonus = prestigeShopManager.getPrestigeGainMultiplier()
                val finalPotential = basePotential.multiply(shopBonus)

                if (finalPotential > BigDecimal.ZERO) {
                    soundManager.playSound(SoundType.BUY)
                    prestigeManager.addPrestige(finalPotential)

                    // --- STATS: Registrar Ascensión ---
                    // IMPORTANTE: Llamar antes de reiniciar las variables locales
                    statisticsManager.trackPrestige(finalPotential)

                    buildingManager.resetForPrestige()
                    upgradeManager.resetForPrestige()

                    repository.wipeBuildingsAndUpgrades()
                    repository.performPrestigeReset(prestigeManager.residualSanity)
                    buildingManager.availableBuildings.forEach { repository.saveBuilding(it.id, it.amountOwned, it.isVisible) }

                    arcaneKnowledge = prestigeShopManager.getStartingKnowledgeBonus()
                    _uiState.value = _uiState.value.copy(activeBuffMultiplier = 1.0, buffTimeRemaining = 0)
                    recalculateState()
                    updateUi()

                    // Guardamos inmediatamente para actualizar las estadísticas de "nuevo run"
                    saveProgressInternal()
                }
            }
        }
    }

    fun onHardResetClicked() {
        viewModelScope.launch {
            transactionMutex.withLock {
                arcaneKnowledge = BigDecimal.ZERO
                prestigeManager.residualSanity = BigDecimal.ZERO

                buildingManager.resetForPrestige()
                upgradeManager.resetForPrestige()
                achievementManager.resetAchievements()
                prestigeShopManager.upgrades.forEach { it.level = 0 }

                // --- STATS: Reinicio Total ---
                statisticsManager.hardReset()

                repository.clearAllAchievements()
                repository.clearAllPrestigeUpgrades()
                repository.wipeBuildingsAndUpgrades()

                saveProgressInternal()
                prestigeShopManager.upgrades.forEach { repository.savePrestigeUpgrade(it.id, 0) }

                _uiState.value = _uiState.value.copy(activeBuffMultiplier = 1.0, buffTimeRemaining = 0)
                recalculateState()
                updateUi()
            }
        }
    }

    // --- AUDIO & CONFIG ---
    fun toggleMusic() {
        val newState = !soundManager.isMusicEnabled
        soundManager.toggleMusic(newState)
        _uiState.value = _uiState.value.copy(isMusicEnabled = newState)
    }

    fun toggleSfx() {
        val newState = !soundManager.isSfxEnabled
        soundManager.toggleSfx(newState)
        _uiState.value = _uiState.value.copy(isSfxEnabled = newState)
    }

    fun onExportSaveClicked() {
        // Obtenemos la entidad actual de estadísticas
        val currentStats = statisticsManager.getEntityForSave()

        val code = saveManager.exportSave(
            arcaneKnowledge,
            prestigeManager.residualSanity,
            buildingManager.availableBuildings,
            upgradeManager.availableUpgrades,
            achievementManager.availableAchievements,
            prestigeShopManager.upgrades,
            currentStats // <--- PASAMOS LAS ESTADÍSTICAS
        )

        val unlockedAch = achievementManager.availableAchievements.count { it.isUnlocked }
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val preview = ExportPreview(arcaneKnowledge.toBeautifulString(), "$unlockedAch / ${achievementManager.availableAchievements.size}", date)

        _uiState.value = _uiState.value.copy(
            saveStringExport = code,
            exportPreview = preview,
            importError = null
        )
    }

    fun onPasteSaveClicked(saveString: String) {
        viewModelScope.launch {
            val data = saveManager.importSave(saveString)
            if (data != null) {
                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(data.timestamp))
                val preview = ExportPreview(data.knowledge.toBeautifulString(), "${data.achievements.size} / ${achievementManager.availableAchievements.size}", date)
                _uiState.value = _uiState.value.copy(importPreview = preview, pendingImportString = saveString, importError = null)
            } else {
                _uiState.value = _uiState.value.copy(importError = "Código inválido o corrupto.", importPreview = null, pendingImportString = "")
            }
        }
    }

    fun onConfirmImportClicked() {
        val saveString = _uiState.value.pendingImportString
        if (saveString.isEmpty()) return

        viewModelScope.launch {
            val data = saveManager.importSave(saveString)
            if (data != null) {
                transactionMutex.withLock {
                    // 1. Limpieza previa
                    repository.clearAllAchievements()
                    achievementManager.resetAchievements()

                    // 2. Carga de datos básicos
                    arcaneKnowledge = data.knowledge
                    prestigeManager.residualSanity = data.sanity

                    // 3. Restaurar Edificios, Mejoras y Logros
                    data.buildings.forEach { b ->
                        buildingManager.availableBuildings.find { it.id == b.id }?.let {
                            it.amountOwned = b.amount
                            it.isVisible = b.isVisible
                        }
                    }
                    upgradeManager.availableUpgrades.forEach { u -> u.isBought = data.upgrades.contains(u.id) }
                    achievementManager.availableAchievements.forEach { a -> a.isUnlocked = data.achievements.contains(a.id) }

                    // 4. Restaurar Tienda de Prestigio
                    prestigeShopManager.upgrades.forEach { it.level = 0 }
                    prestigeShopManager.upgrades.forEach { p ->
                        p.level = data.prestigeUpgrades.find { it.id == p.id }?.level ?: 0
                    }

                    // 5. --- RESTAURAR ESTADÍSTICAS (NUEVO) ---
                    // Cargamos en memoria
                    statisticsManager.loadFromEntity(data.statistics)
                    // Guardamos en base de datos inmediatamente
                    repository.saveStatistics(data.statistics)

                    // 6. Guardado final y actualización de UI
                    saveProgressInternal()
                    prestigeShopManager.upgrades.forEach { repository.savePrestigeUpgrade(it.id, it.level) }

                    recalculateState()
                    updateUi()
                    clearExportString()
                }
            }
        }
    }

    fun clearExportString() {
        _uiState.value = _uiState.value.copy(saveStringExport = "", exportPreview = null, importPreview = null, pendingImportString = "", importError = null)
    }

    // --- MODIFICADO: Acepta flag isManual ---
    private fun addKnowledgeInternal(amount: BigDecimal, isManual: Boolean) {
        arcaneKnowledge = arcaneKnowledge.add(amount)

        // --- STATS: Rastrear ganancia ---
        statisticsManager.trackKnowledgeGenerated(amount, isManual)

        updateUi()
    }

    private fun updateUi() {
        val finalClick = buildingManager.getClickBuildingBonus().max(BigDecimal.ONE)
        _uiState.value = _uiState.value.copy(
            knowledgeFormatted = arcaneKnowledge.toBeautifulString(),
            knowledgePerSecond = productionPerSecond.toBeautifulString(),
            rawKnowledge = arcaneKnowledge,
            clickMultiplier = finalClick.toBeautifulString(),
            prestigeCurrencyFormatted = prestigeManager.residualSanity.toBeautifulString(),
            globalMultiplier = _uiState.value.globalMultiplier
        )
    }

    private fun saveBuildingToDb(buildingId: String) {
        val building = buildingManager.availableBuildings.find { it.id == buildingId }
        if (building != null) viewModelScope.launch { repository.saveBuilding(building.id, building.amountOwned, building.isVisible) }
    }

    private fun saveAchievementsToDb() {
        viewModelScope.launch { achievementManager.availableAchievements.forEach { if (it.isUnlocked) repository.saveAchievement(it.id, true) } }
    }

    fun saveProgress() {
        viewModelScope.launch {
            if (transactionMutex.tryLock()) {
                try {
                    saveProgressInternal()
                } finally {
                    transactionMutex.unlock()
                }
            }
        }
    }

    private suspend fun saveProgressInternal() {
        repository.saveGame(arcaneKnowledge, BigDecimal.ZERO, prestigeManager.residualSanity)
        buildingManager.availableBuildings.forEach { repository.saveBuilding(it.id, it.amountOwned, it.isVisible) }
        achievementManager.availableAchievements.forEach { if (it.isUnlocked) repository.saveAchievement(it.id, true) }

        // --- STATS: Guardar Estadísticas ---
        repository.saveStatistics(statisticsManager.getEntityForSave())
    }
}