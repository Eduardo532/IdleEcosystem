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
import kotlin.random.Random

// Estructura para comunicar el resultado del click a la UI
data class ManualClickResult(
    val knowledgeGained: BigDecimal,
    val echoFound: Boolean
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
    private val ritualManager: RitualManager
) : ViewModel() {

    private var arcaneKnowledge = BigDecimal.ZERO
    private var productionPerSecond = BigDecimal.ZERO
    private var echoesBalance = BigDecimal.ZERO

    private val transactionMutex = Mutex()
    private var timeUntilNextEvent = 0L
    private var activeBuffExpiration = 0L
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

    val ritualsState = ritualManager.rituals
    val statsManager: StatisticsManager get() = statisticsManager

    init {
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
                val entity = repository.gameState.firstOrNull()
                val savedBuildings = repository.loadBuildings()
                val savedUpgrades = repository.loadUpgrades()
                val savedAchievements = repository.loadAchievements()
                val savedPrestigeShop = repository.loadPrestigeUpgrades()

                val savedStats = repository.gameStatistics.firstOrNull()
                statisticsManager.loadFromEntity(savedStats)

                // Lógica de migración legacy
                val refundAmount = prestigeShopManager.calculateLegacyRefund(savedPrestigeShop)
                var initialSanity = if (entity != null) BigDecimal(entity.prestigeCurrency) else BigDecimal.ZERO
                var showRefund = false

                buildingManager.resetForPrestige()
                upgradeManager.resetForPrestige()
                achievementManager.resetAchievements()
                prestigeShopManager.upgrades.forEach { it.level = 0 }

                if (refundAmount > BigDecimal.ZERO) {
                    initialSanity = initialSanity.add(refundAmount)
                    prestigeManager.residualSanity = initialSanity
                    showRefund = true

                    repository.clearAllPrestigeUpgrades()
                    prestigeShopManager.upgrades.forEach { repository.savePrestigeUpgrade(it.id, 0) }

                    // Al migrar, asumimos 0 ecos
                    repository.saveGame(
                        if (entity != null) BigDecimal(entity.money) else BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        initialSanity,
                        BigDecimal.ZERO
                    )
                } else {
                    if (savedPrestigeShop.isNotEmpty()) prestigeShopManager.loadFromSave(savedPrestigeShop)
                    if (savedBuildings.isNotEmpty()) buildingManager.loadFromSave(savedBuildings)
                    if (savedUpgrades.isNotEmpty()) upgradeManager.loadFromSave(savedUpgrades)
                    if (savedAchievements.isNotEmpty()) achievementManager.loadFromSave(savedAchievements)

                    prestigeManager.residualSanity = initialSanity
                }

                if (entity != null) {
                    arcaneKnowledge = BigDecimal(entity.money)
                    echoesBalance = try {
                        if (entity.echoes.isNullOrEmpty()) BigDecimal.ZERO else BigDecimal(entity.echoes)
                    } catch (e: Exception) { BigDecimal.ZERO }

                    val currentTime = System.currentTimeMillis()
                    val lastTime = entity.lastLoginTime
                    if (lastTime > 0) {
                        val diffMillis = currentTime - lastTime
                        if (diffMillis > 10000) calculateOfflineGains(diffMillis)
                    }
                } else {
                    repository.createNewGame()
                    arcaneKnowledge = BigDecimal.ZERO.add(prestigeShopManager.getStartingKnowledgeBonus())
                    echoesBalance = BigDecimal.ZERO
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
            addKnowledgeInternal(gains, isManual = false)
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
                oneSecondAccumulator += 100
                if (oneSecondAccumulator >= 1000) {
                    statisticsManager.trackPlayTime(1)
                    statisticsManager.trackProductionPeak(productionPerSecond)
                    ritualManager.tick(1)
                    oneSecondAccumulator -= 1000
                }

                buildingManager.checkUnlocks(arcaneKnowledge)
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
        val ritualProdMult = BigDecimal.valueOf(ritualManager.getActiveProductionMultiplier())

        val finalGlobalMult = upgradesGlobal
            .multiply(prestigeGlobal)
            .multiply(achievementGlobal)
            .multiply(shopGlobal)
            .multiply(buffMult)
            .multiply(ritualProdMult)

        val shopClickBonus = prestigeShopManager.getClickPrestigeMultiplier()
        val ritualClickMult = BigDecimal.valueOf(ritualManager.getActiveClickMultiplier())

        buildingManager.availableBuildings.forEachIndexed { index, building ->
            val shopTierBonus = prestigeShopManager.getBuildingTierMultiplier(index)
            val specificMult = if (building.type == BuildingType.CLICK) {
                upgradeManager.getRawClickMultiplier()
                    .multiply(shopClickBonus)
                    .multiply(ritualClickMult)
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

        val allAchievements = achievementManager.availableAchievements
        val unlockedTotal = allAchievements.count { it.isUnlocked }
        val secretsFound = allAchievements.count { it.isSecret && it.isUnlocked }
        val secretsTotal = 13
        val visibleAchievements = allAchievements.filter { !it.isSecret || it.isUnlocked }.sortedBy { it.order }

        _achievementsState.value = visibleAchievements

        val progressText = "$unlockedTotal / 100"
        val secretsText = if (secretsFound > 0) "Secretos: $secretsFound / $secretsTotal" else null

        _uiState.value = _uiState.value.copy(
            potentialPrestigeFormatted = finalPotential.toBeautifulString(),
            canPrestige = canAscend,
            globalMultiplier = finalGlobalMult,
            prestigeUpgrades = prestigeShopManager.upgrades.toList(),
            achievementsProgress = progressText,
            secretsProgress = secretsText,
            echoes = echoesBalance,
            echoesFormatted = echoesBalance.toBeautifulString()
        )
    }

    fun setBuyMultiplier(value: Int) {
        _uiState.value = _uiState.value.copy(buyMultiplier = value)
    }

    fun onManualClick(): ManualClickResult {
        val totalClickPower = buildingManager.getClickBuildingBonus().max(BigDecimal.ONE)
        var echoFound = false

        if (totalClickPower > BigDecimal.ZERO) {
            soundManager.playSound(SoundType.CLICK, 250)
            statisticsManager.trackClick()
            addKnowledgeInternal(totalClickPower, isManual = true)

            if (Random.nextFloat() < 0.01) {
                echoFound = true
                echoesBalance = echoesBalance.add(BigDecimal.ONE)
                statisticsManager.trackEchoesFound(BigDecimal.ONE)
                soundManager.playSound(SoundType.ECHO_FOUND)
                updateUi()
            }
        }
        return ManualClickResult(totalClickPower, echoFound)
    }

    fun onActivateRitual(ritualId: String) {
        viewModelScope.launch {
            transactionMutex.withLock {
                if (ritualManager.tryActivateRitual(ritualId, echoesBalance)) {
                    val ritual = ritualManager.rituals.value.find { it.id == ritualId }
                    if (ritual != null) {
                        soundManager.playSound(SoundType.BUY)
                        echoesBalance = echoesBalance.subtract(ritual.baseCost)
                        recalculateState()
                        updateUi()
                    }
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

    fun toggleParticles() {
        val newState = !_uiState.value.areParticlesEnabled
        _uiState.value = _uiState.value.copy(areParticlesEnabled = newState)
    }

    fun toggleFloatingText() {
        val newState = !_uiState.value.isFloatingTextEnabled
        _uiState.value = _uiState.value.copy(isFloatingTextEnabled = newState)
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
                    statisticsManager.trackPrestige(finalPotential)

                    buildingManager.resetForPrestige()
                    upgradeManager.resetForPrestige()

                    repository.wipeBuildingsAndUpgrades()
                    repository.performPrestigeReset(prestigeManager.residualSanity, echoesBalance)
                    buildingManager.availableBuildings.forEach { repository.saveBuilding(it.id, it.amountOwned, it.isVisible) }

                    arcaneKnowledge = prestigeShopManager.getStartingKnowledgeBonus()

                    _uiState.value = _uiState.value.copy(activeBuffMultiplier = 1.0, buffTimeRemaining = 0)
                    recalculateState()
                    updateUi()

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
                echoesBalance = BigDecimal.ZERO

                buildingManager.resetForPrestige()
                upgradeManager.resetForPrestige()
                achievementManager.resetAchievements()
                prestigeShopManager.upgrades.forEach { it.level = 0 }
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

    fun onExportSaveClicked() {
        val currentStats = statisticsManager.getEntityForSave()
        val code = saveManager.exportSave(
            arcaneKnowledge,
            prestigeManager.residualSanity,
            echoesBalance,
            buildingManager.availableBuildings,
            upgradeManager.availableUpgrades,
            achievementManager.availableAchievements,
            prestigeShopManager.upgrades,
            currentStats
        )

        val unlockedAch = achievementManager.availableAchievements.count { it.isUnlocked }
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val preview = SavePreview(
            date = date,
            money = arcaneKnowledge.toBeautifulString(),
            achievements = "$unlockedAch / ${achievementManager.availableAchievements.size}"
        )

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
                val preview = SavePreview(
                    date = date,
                    money = data.knowledge.toBeautifulString(),
                    achievements = "${data.achievements.size} / ${achievementManager.availableAchievements.size}"
                )
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
                    repository.clearAllAchievements()
                    achievementManager.resetAchievements()

                    arcaneKnowledge = data.knowledge
                    prestigeManager.residualSanity = data.sanity
                    echoesBalance = data.echoes

                    data.buildings.forEach { b ->
                        buildingManager.availableBuildings.find { it.id == b.id }?.let {
                            it.amountOwned = b.amount
                            it.isVisible = b.isVisible
                        }
                    }
                    upgradeManager.availableUpgrades.forEach { u -> u.isBought = data.upgrades.contains(u.id) }
                    achievementManager.availableAchievements.forEach { a -> a.isUnlocked = data.achievements.contains(a.id) }

                    prestigeShopManager.upgrades.forEach { it.level = 0 }
                    prestigeShopManager.upgrades.forEach { p ->
                        p.level = data.prestigeUpgrades.find { it.id == p.id }?.level ?: 0
                    }

                    statisticsManager.loadFromEntity(data.statistics)
                    repository.saveStatistics(data.statistics)

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

    private fun addKnowledgeInternal(amount: BigDecimal, isManual: Boolean) {
        arcaneKnowledge = arcaneKnowledge.add(amount)
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
            globalMultiplier = _uiState.value.globalMultiplier,
            echoes = echoesBalance,
            echoesFormatted = echoesBalance.toBeautifulString()
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
        repository.saveGame(arcaneKnowledge, BigDecimal.ZERO, prestigeManager.residualSanity, echoesBalance)
        buildingManager.availableBuildings.forEach { repository.saveBuilding(it.id, it.amountOwned, it.isVisible) }
        achievementManager.availableAchievements.forEach { if (it.isUnlocked) repository.saveAchievement(it.id, true) }
        repository.saveStatistics(statisticsManager.getEntityForSave())
    }
}