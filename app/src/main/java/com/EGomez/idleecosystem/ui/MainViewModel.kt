package com.EGomez.idleecosystem.ui

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.EGomez.idleecosystem.core.math.BigMath
import com.EGomez.idleecosystem.core.math.NotationSettings
import com.EGomez.idleecosystem.core.math.NumberNotation
import com.EGomez.idleecosystem.core.math.formatSecondsToTime
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.data.database.AchievementEntity
import com.EGomez.idleecosystem.data.database.BuildingEntity
import com.EGomez.idleecosystem.data.database.PrestigeUpgradeEntity
import com.EGomez.idleecosystem.data.database.UpgradeEntity
import com.EGomez.idleecosystem.data.preferences.GamePreferences
import com.EGomez.idleecosystem.data.repository.GameRepository
import com.EGomez.idleecosystem.data.repository.GameSnapshot
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
import java.math.MathContext
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

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
    private val ritualManager: RitualManager,
    private val preferences: GamePreferences
) : ViewModel() {

    private var arcaneKnowledge = BigDecimal.ZERO
    private var echoesBalance = BigDecimal.ZERO

    /** Producción por segundo actual, incluyendo potenciadores temporales. */
    private var productionPerSecond = BigDecimal.ZERO

    /** Producción sin potenciadores temporales (rituales y eventos). Base de offline y eventos. */
    private var baseProductionPerSecond = BigDecimal.ZERO

    private val transactionMutex = Mutex()
    private var isLoaded = false
    private var isInForeground = true

    // Reloj del bucle principal (tiempo real, incluye el tiempo con el proceso congelado)
    private var lastTickRealtime = 0L
    private var secondAccumulatorMs = 0L

    // Eventos y potenciadores (tiempo de pared)
    private var nextEventAtMillis = 0L
    private var pendingEventExpiresAt = 0L
    private var activeBuffExpiration = 0L

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
        NotationSettings.mode = preferences.notation
        _uiState.value = _uiState.value.copy(
            isMusicEnabled = soundManager.isMusicEnabled,
            isSfxEnabled = soundManager.isSfxEnabled,
            notation = NotationSettings.mode,
            prestigeRequirementFormatted = PrestigeManager.PRESTIGE_THRESHOLD.toBeautifulString()
        )
        loadGameDataAndStart()
    }

    // =========================================================
    // CARGA
    // =========================================================

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

                buildingManager.resetForPrestige()
                upgradeManager.resetForPrestige()
                achievementManager.resetAchievements()
                prestigeShopManager.upgrades.forEach { it.level = 0 }

                // El progreso normal se carga siempre. Antes, la migración del sistema de
                // prestigio antiguo se saltaba esta carga y el autoguardado borraba los edificios.
                if (savedBuildings.isNotEmpty()) buildingManager.loadFromSave(savedBuildings)
                if (savedUpgrades.isNotEmpty()) upgradeManager.loadFromSave(savedUpgrades)
                if (savedAchievements.isNotEmpty()) achievementManager.loadFromSave(savedAchievements)

                // Migración legacy: la tienda de prestigio antigua se reembolsa en Cordura
                val refundAmount = prestigeShopManager.calculateLegacyRefund(savedPrestigeShop)
                var initialSanity = entity?.prestigeCurrency.toBigDecimalOrZero()
                val showRefund = refundAmount > BigDecimal.ZERO
                if (showRefund) {
                    initialSanity = initialSanity.add(refundAmount)
                    repository.clearAllPrestigeUpgrades()
                } else if (savedPrestigeShop.isNotEmpty()) {
                    prestigeShopManager.loadFromSave(savedPrestigeShop)
                }
                // La Cordura es una moneda entera (versiones anteriores podían dejar decimales)
                prestigeManager.residualSanity = initialSanity.setScale(0, RoundingMode.FLOOR)

                if (entity != null) {
                    arcaneKnowledge = entity.money.toBigDecimalOrZero()
                    echoesBalance = entity.echoes.toBigDecimalOrZero()
                } else {
                    repository.createNewGame()
                    arcaneKnowledge = BigDecimal.ZERO
                    echoesBalance = BigDecimal.ZERO
                }
                buildingManager.checkUnlocks()

                // 1) Ganancias offline con la producción base (sin rituales ni eventos)
                val now = System.currentTimeMillis()
                val lastTime = entity?.lastLoginTime ?: 0L
                if (lastTime > 0) {
                    val diffMillis = now - lastTime
                    if (diffMillis > OFFLINE_THRESHOLD_MS) grantOfflineProgress(diffMillis)
                }

                // 2) Rituales: se restauran descontando el tiempo que la app estuvo cerrada
                val (ritualSnapshots, ritualsSavedAt) = preferences.loadRituals()
                if (ritualSnapshots.isNotEmpty()) {
                    val elapsedSeconds = if (ritualsSavedAt > 0) ((now - ritualsSavedAt) / 1000).coerceAtLeast(0) else 0L
                    ritualManager.restore(ritualSnapshots, elapsedSeconds)
                }

                recalculateState()
                checkAchievements()

                if (showRefund) {
                    _uiState.value = _uiState.value.copy(
                        showRefundDialog = true,
                        refundAmountString = refundAmount.toBeautifulString()
                    )
                    saveProgressInternal()
                }

                scheduleNextEvent()
                isLoaded = true
                updateUi()
            }
            startGameLoop()
            startAutoSaveLoop()
        }
    }

    /**
     * Otorga el progreso de un periodo sin jugar: al abrir la app, o al volver después de
     * que Android congelara el proceso en segundo plano (antes ese tiempo se perdía).
     */
    private fun grantOfflineProgress(diffMillis: Long) {
        val seconds = diffMillis / 1000
        if (seconds <= 0) return

        recalculateState(includeTemporaryBoosts = false)
        val offlineMultiplier = prestigeShopManager.getOfflinePrestigeMultiplier()
        val gains = baseProductionPerSecond.multiply(offlineMultiplier).multiply(BigDecimal(seconds))
        val echoesGained = BigDecimal((seconds / SECONDS_PER_OFFLINE_ECHO).coerceAtMost(MAX_OFFLINE_ECHOES))

        statisticsManager.trackOfflineTime(seconds)
        if (gains > BigDecimal.ZERO) addKnowledgeInternal(gains, isManual = false)
        if (echoesGained > BigDecimal.ZERO) {
            echoesBalance = echoesBalance.add(echoesGained)
            statisticsManager.trackEchoesFound(echoesGained)
        }

        if (gains > BigDecimal.ZERO || echoesGained > BigDecimal.ZERO) {
            _uiState.value = _uiState.value.copy(
                showOfflineDialog = true,
                offlineGains = gains.toBeautifulString(),
                offlineEchoes = if (echoesGained > BigDecimal.ZERO) echoesGained.toBeautifulString() else "",
                offlineTimeFormatted = formatSecondsToTime(seconds)
            )
        }
        recalculateState()
    }

    fun dismissOfflineDialog() { _uiState.value = _uiState.value.copy(showOfflineDialog = false) }
    fun dismissRefundDialog() { _uiState.value = _uiState.value.copy(showRefundDialog = false) }

    // =========================================================
    // BUCLE PRINCIPAL
    // =========================================================

    private fun startGameLoop() {
        viewModelScope.launch {
            lastTickRealtime = SystemClock.elapsedRealtime()
            while (true) {
                delay(TICK_MS)
                val now = SystemClock.elapsedRealtime()
                val elapsed = now - lastTickRealtime
                lastTickRealtime = now

                if (elapsed >= OFFLINE_THRESHOLD_MS) {
                    // El proceso estuvo congelado: se trata como tiempo offline
                    ritualManager.tick(elapsed / 1000)
                    grantOfflineProgress(elapsed)
                    updateUi()
                    continue
                }
                processTick(elapsed)
            }
        }
    }

    /** Avanza el juego según el tiempo real transcurrido (no asume ticks exactos de 100 ms). */
    private fun processTick(elapsedMs: Long) {
        secondAccumulatorMs += elapsedMs
        while (secondAccumulatorMs >= 1000) {
            secondAccumulatorMs -= 1000
            onSecondElapsed()
        }

        val gain = productionPerSecond.multiply(BigDecimal(elapsedMs)).divide(THOUSAND)
        if (gain > BigDecimal.ZERO) addKnowledgeInternal(gain, isManual = false, refreshUi = false)
        updateUi()
    }

    /** Tareas que no necesitan correr 10 veces por segundo. */
    private fun onSecondElapsed() {
        statisticsManager.trackPlayTime(1)
        statisticsManager.trackProductionPeak(productionPerSecond)
        ritualManager.tick(1)

        val now = System.currentTimeMillis()
        if (_uiState.value.activeBuffMultiplier > 1.0) {
            if (now >= activeBuffExpiration) {
                activeBuffExpiration = 0L
                _uiState.value = _uiState.value.copy(activeBuffMultiplier = 1.0, buffTimeRemaining = 0)
            } else {
                _uiState.value = _uiState.value.copy(buffTimeRemaining = ((activeBuffExpiration - now) / 1000).toInt())
            }
        }

        buildingManager.checkUnlocks()
        checkAchievements()
        updateEvents(now)

        // Recalcular una vez por segundo cubre los cambios de rituales, potenciadores y
        // visibilidad de mejoras. Antes se recalculaba todo (listas, filtros, orden) cada 100 ms.
        recalculateState()
    }

    private fun startAutoSaveLoop() {
        viewModelScope.launch {
            while (true) {
                delay(AUTOSAVE_INTERVAL_MS)
                saveProgress()
            }
        }
    }

    // =========================================================
    // EVENTOS ALEATORIOS
    // =========================================================

    private fun scheduleNextEvent() {
        nextEventAtMillis = System.currentTimeMillis() + Random.nextLong(EVENT_MIN_DELAY_MS, EVENT_MAX_DELAY_MS)
    }

    /**
     * Los eventos ya no interrumpen con un diálogo: aparece un presagio que el jugador
     * puede abrir cuando quiera (o ignorar; desaparece al minuto).
     */
    private fun updateEvents(now: Long) {
        val state = _uiState.value
        if (state.currentEvent != null) return

        if (state.pendingEvent != null) {
            if (now >= pendingEventExpiresAt) {
                _uiState.value = state.copy(pendingEvent = null, pendingEventSecondsLeft = 0)
                scheduleNextEvent()
            } else {
                _uiState.value = state.copy(pendingEventSecondsLeft = ((pendingEventExpiresAt - now) / 1000).toInt())
            }
            return
        }

        // No generamos eventos mientras la app está en segundo plano
        if (!isInForeground || now < nextEventAtMillis) return

        val event = randomEventManager.getRandomEvent(arcaneKnowledge, sanityProgress())
        if (event == null) {
            scheduleNextEvent()
            return
        }
        soundManager.playSound(SoundType.EVENT_APPEAR)
        pendingEventExpiresAt = now + PENDING_EVENT_LIFETIME_MS
        _uiState.value = state.copy(
            pendingEvent = event,
            pendingEventSecondsLeft = (PENDING_EVENT_LIFETIME_MS / 1000).toInt()
        )
    }

    /** Cordura histórica: indica si el jugador ya ascendió alguna vez, aunque haya gastado todo. */
    private fun sanityProgress(): BigDecimal =
        statisticsManager.lifetimeSanity
            .max(statisticsManager.spentSanity)
            .max(prestigeManager.residualSanity)

    fun onOpenPendingEvent() {
        val event = _uiState.value.pendingEvent ?: return
        statisticsManager.trackEventFound()
        _uiState.value = _uiState.value.copy(
            currentEvent = event,
            eventResultText = null,
            pendingEvent = null,
            pendingEventSecondsLeft = 0
        )
    }

    fun onEventOptionSelected(optionIndex: Int) {
        val state = _uiState.value
        val event = state.currentEvent ?: return
        if (state.eventResultText != null) return // evita aplicar el efecto dos veces
        val option = event.options.getOrNull(optionIndex) ?: return

        option.effects.forEach { effect ->
            when (effect.type) {
                EffectType.ADD_PRODUCTION_MINUTES -> applyProductionMinutes(effect.value)
                EffectType.ADD_SANITY_PERCENT -> applySanityPercent(effect.value)
                EffectType.GLOBAL_BUFF -> {
                    activeBuffExpiration = System.currentTimeMillis() + effect.durationSeconds * 1000L
                    _uiState.value = _uiState.value.copy(
                        activeBuffMultiplier = effect.value,
                        buffTimeRemaining = effect.durationSeconds
                    )
                }
                EffectType.NOTHING -> { }
            }
        }
        _uiState.value = _uiState.value.copy(eventResultText = option.flavorResult)
        recalculateState()
        updateUi()

        viewModelScope.launch {
            delay(EVENT_RESULT_DISPLAY_MS)
            _uiState.value = _uiState.value.copy(currentEvent = null, eventResultText = null)
            scheduleNextEvent()
            updateUi()
        }
    }

    /**
     * Gana (o pierde) X minutos de producción base. Antes era un % del conocimiento
     * acumulado, lo que premiaba no gastar nunca. Al inicio, cuando la producción es casi
     * nula, se usa como mínimo el valor de unos clics por minuto.
     */
    private fun applyProductionMinutes(minutes: Double) {
        val clickPower = buildingManager.getClickBuildingBonus().max(BigDecimal.ONE)
        val perMinute = baseProductionPerSecond.multiply(SIXTY)
            .max(clickPower.multiply(EVENT_MIN_CLICKS_PER_MINUTE))
        var amount = perMinute.multiply(BigDecimal.valueOf(minutes))
        if (amount.signum() < 0) amount = amount.max(arcaneKnowledge.negate())
        if (amount.signum() != 0) addKnowledgeInternal(amount, isManual = false)
    }

    /** Cambia la Cordura en un porcentaje, manteniéndola entera. */
    private fun applySanityPercent(percent: Double) {
        val current = prestigeManager.residualSanity
        if (current.signum() == 0) return
        val raw = current.multiply(BigDecimal.valueOf(percent))
        val delta = if (percent > 0) {
            raw.setScale(0, RoundingMode.CEILING).max(BigDecimal.ONE)
        } else {
            raw.setScale(0, RoundingMode.CEILING) // las pérdidas se redondean hacia cero
        }
        prestigeManager.residualSanity = current.add(delta).max(BigDecimal.ZERO)
    }

    // =========================================================
    // CÁLCULO DE ESTADO
    // =========================================================

    private fun recalculateState(includeTemporaryBoosts: Boolean = true) {
        val upgradesGlobal = upgradeManager.getGlobalMultiplier()
        val prestigeGlobal = prestigeManager.getPrestigeMultiplier()
        val achievementGlobal = achievementManager.getGlobalAchievementMultiplier()
        val shopGlobal = prestigeShopManager.getPermanentGlobalMultiplier()

        val buffActive = includeTemporaryBoosts && System.currentTimeMillis() < activeBuffExpiration
        val buffMult = if (buffActive) BigDecimal.valueOf(_uiState.value.activeBuffMultiplier) else BigDecimal.ONE
        val ritualProdMult = if (includeTemporaryBoosts) BigDecimal.valueOf(ritualManager.getActiveProductionMultiplier()) else BigDecimal.ONE
        val ritualClickMult = if (includeTemporaryBoosts) BigDecimal.valueOf(ritualManager.getActiveClickMultiplier()) else BigDecimal.ONE
        val temporaryMult = buffMult.multiply(ritualProdMult)

        // Se redondea a 16 dígitos significativos: multiplicar ~100 bonos de logros con
        // decimales exactos generaba números con cientos de decimales que se arrastraban
        // al conocimiento en cada tick.
        val permanentGlobalMult = upgradesGlobal
            .multiply(prestigeGlobal)
            .multiply(achievementGlobal)
            .multiply(shopGlobal)
            .round(MathContext.DECIMAL64)
        val finalGlobalMult = permanentGlobalMult.multiply(temporaryMult).round(MathContext.DECIMAL64)

        val shopClickBonus = prestigeShopManager.getClickPrestigeMultiplier()

        buildingManager.availableBuildings.forEachIndexed { index, building ->
            val specificMult = if (building.type == BuildingType.CLICK) {
                upgradeManager.getRawClickMultiplier()
                    .multiply(shopClickBonus)
                    .multiply(ritualClickMult)
            } else {
                upgradeManager.getSpecificBuildingMultiplier(building.id)
                    .multiply(prestigeShopManager.getBuildingTierMultiplier(index))
            }
            building.currentMultiplier = specificMult.multiply(finalGlobalMult).round(MathContext.DECIMAL64)
        }

        productionPerSecond = buildingManager.getTotalProductionPerSecond()
        baseProductionPerSecond = if (temporaryMult.signum() > 0) {
            productionPerSecond.divide(temporaryMult, MathContext.DECIMAL64)
        } else productionPerSecond

        val knowledgeThisRun = knowledgeThisRun()
        _buildingsState.value = buildingManager.availableBuildings.filter { it.isVisible }
        _upgradesState.value = upgradeManager.availableUpgrades
            .filter { upgradeManager.isUpgradeVisible(it, buildingManager.availableBuildings, knowledgeThisRun) }
            .filter { _showPurchasedState.value || !it.isBought }
            .sortedBy { it.cost }

        val allAchievements = achievementManager.availableAchievements
        val unlockedTotal = allAchievements.count { it.isUnlocked }
        val secretsTotal = allAchievements.count { it.isSecret }
        val secretsFound = allAchievements.count { it.isSecret && it.isUnlocked }
        _achievementsState.value = allAchievements.filter { !it.isSecret || it.isUnlocked }.sortedBy { it.order }

        _uiState.value = _uiState.value.copy(
            globalMultiplier = finalGlobalMult,
            prestigeUpgrades = prestigeShopManager.upgrades.toList(),
            // Antes decía "/ 100" fijo aunque hay más logros
            achievementsProgress = "$unlockedTotal / ${allAchievements.size}",
            secretsProgress = if (secretsFound > 0) "Secretos: $secretsFound / $secretsTotal" else null
        )
    }

    /** Conocimiento generado en esta vida (con respaldo para partidas antiguas sin estadísticas). */
    private fun knowledgeThisRun(): BigDecimal = statisticsManager.runKnowledge.max(arcaneKnowledge)

    private fun currentPotentialPrestige(): BigDecimal =
        prestigeManager.calculatePotentialPrestige(knowledgeThisRun())
            .multiply(prestigeShopManager.getPrestigeGainMultiplier())
            .setScale(0, RoundingMode.FLOOR)

    private fun checkAchievements() {
        val newUnlockList = achievementManager.checkAchievements(arcaneKnowledge, buildingManager.availableBuildings)
        if (newUnlockList.isEmpty()) return

        soundManager.playSound(SoundType.ACHIEVEMENT)
        val achievementToShow = newUnlockList.last()
        _uiState.value = _uiState.value.copy(lastUnlockedAchievement = achievementToShow)
        recalculateState()
        viewModelScope.launch {
            delay(ACHIEVEMENT_POPUP_MS)
            if (_uiState.value.lastUnlockedAchievement == achievementToShow) {
                _uiState.value = _uiState.value.copy(lastUnlockedAchievement = null)
            }
        }
    }

    // =========================================================
    // ACCIONES DEL JUGADOR
    // =========================================================

    fun setBuyMultiplier(value: Int) {
        _uiState.value = _uiState.value.copy(buyMultiplier = value)
    }

    fun onManualClick(): BigDecimal {
        val clickPower = buildingManager.getClickBuildingBonus().max(BigDecimal.ONE)
        soundManager.playSound(SoundType.CLICK, 250)
        statisticsManager.trackClick()
        addKnowledgeInternal(clickPower, isManual = true)

        if (Random.nextFloat() < ECHO_CHANCE_PER_CLICK) {
            echoesBalance = echoesBalance.add(BigDecimal.ONE)
            statisticsManager.trackEchoesFound(BigDecimal.ONE)
            updateUi()
        }
        return clickPower
    }

    fun onActivateRitual(ritualId: String) {
        viewModelScope.launch {
            transactionMutex.withLock {
                val ritual = ritualManager.rituals.value.find { it.id == ritualId } ?: return@withLock
                if (ritualManager.tryActivateRitual(ritualId, echoesBalance)) {
                    soundManager.playSound(SoundType.BUY)
                    echoesBalance = echoesBalance.subtract(ritual.baseCost)
                    recalculateState()
                    updateUi()
                }
            }
        }
    }

    fun onBuyBuilding(buildingId: String) {
        viewModelScope.launch {
            transactionMutex.withLock {
                val purchase = buildingManager.tryBuyBuilding(buildingId, arcaneKnowledge, _uiState.value.buyMultiplier)
                    ?: return@withLock
                soundManager.playSound(SoundType.BUY)
                // Se descuenta exactamente el costo de lo que se compró
                arcaneKnowledge = arcaneKnowledge.subtract(purchase.cost).max(BigDecimal.ZERO)
                buildingManager.checkUnlocks()
                recalculateState()
                checkAchievements()
                updateUi()
            }
        }
    }

    fun onBuyUpgrade(upgradeId: String) {
        viewModelScope.launch {
            transactionMutex.withLock {
                val upgrade = upgradeManager.availableUpgrades.find { it.id == upgradeId } ?: return@withLock
                if (upgradeManager.tryBuyUpgrade(upgradeId, arcaneKnowledge)) {
                    soundManager.playSound(SoundType.BUY)
                    arcaneKnowledge = arcaneKnowledge.subtract(upgrade.cost)
                    recalculateState()
                    updateUi()
                }
            }
        }
    }

    fun onBuyPrestigeUpgrade(id: String) {
        viewModelScope.launch {
            transactionMutex.withLock {
                val cost = prestigeShopManager.getCost(id) ?: return@withLock
                if (prestigeManager.residualSanity < cost) return@withLock
                if (prestigeShopManager.tryBuy(id, prestigeManager.residualSanity)) {
                    soundManager.playSound(SoundType.BUY)
                    prestigeManager.residualSanity = prestigeManager.residualSanity.subtract(cost)
                    statisticsManager.trackSanitySpent(cost)
                    recalculateState()
                    updateUi()
                    saveProgressInternal()
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
                val gain = currentPotentialPrestige()
                if (gain <= BigDecimal.ZERO) return@withLock

                // Todo el estado en memoria se cambia primero, sin suspender, para que el
                // bucle principal nunca vea una mezcla de la vida anterior y la nueva.
                soundManager.playSound(SoundType.BUY)
                prestigeManager.addPrestige(gain)
                statisticsManager.trackPrestige(gain)

                // Ascender también otorga Ecos, para que los rituales no dependan solo de clics
                val echoesGained = BigMath.floorSqrt(gain).max(BigDecimal.ONE)
                echoesBalance = echoesBalance.add(echoesGained)
                statisticsManager.trackEchoesFound(echoesGained)

                buildingManager.resetForPrestige()
                upgradeManager.resetForPrestige()
                arcaneKnowledge = BigDecimal.ZERO
                activeBuffExpiration = 0L
                _uiState.value = _uiState.value.copy(activeBuffMultiplier = 1.0, buffTimeRemaining = 0)

                recalculateState()
                updateUi()

                repository.wipeBuildingsAndUpgrades()
                saveProgressInternal()
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
                ritualManager.reset()

                activeBuffExpiration = 0L
                _uiState.value = _uiState.value.copy(
                    activeBuffMultiplier = 1.0,
                    buffTimeRemaining = 0,
                    currentEvent = null,
                    eventResultText = null,
                    pendingEvent = null,
                    pendingEventSecondsLeft = 0
                )
                scheduleNextEvent()
                recalculateState()
                updateUi()

                repository.wipeProgressTables()
                preferences.clearRituals()
                saveProgressInternal()
            }
        }
    }

    // =========================================================
    // CONFIGURACIÓN
    // =========================================================

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

    fun toggleNotation() {
        val newMode = if (NotationSettings.mode == NumberNotation.SCIENTIFIC) NumberNotation.SUFFIX else NumberNotation.SCIENTIFIC
        NotationSettings.mode = newMode
        preferences.notation = newMode
        _uiState.value = _uiState.value.copy(
            notation = newMode,
            prestigeRequirementFormatted = PrestigeManager.PRESTIGE_THRESHOLD.toBeautifulString()
        )
        recalculateState()
        updateUi()
    }

    /** Lo llama la Activity en onStart/onStop. */
    fun onAppForegroundChanged(inForeground: Boolean) {
        isInForeground = inForeground
        soundManager.setAppInForeground(inForeground)
        if (!inForeground) saveProgress()
    }

    // =========================================================
    // EXPORTAR / IMPORTAR
    // =========================================================

    fun onExportSaveClicked() {
        val code = saveManager.exportSave(
            arcaneKnowledge,
            prestigeManager.residualSanity,
            echoesBalance,
            buildingManager.availableBuildings,
            upgradeManager.availableUpgrades,
            achievementManager.availableAchievements,
            prestigeShopManager.upgrades,
            statisticsManager.getEntityForSave()
        )
        if (code.isEmpty()) {
            _uiState.value = _uiState.value.copy(importError = "No se pudo generar el código.")
            return
        }

        val unlockedAch = achievementManager.availableAchievements.count { it.isUnlocked }
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        _uiState.value = _uiState.value.copy(
            saveStringExport = code,
            exportPreview = SavePreview(
                date = date,
                money = arcaneKnowledge.toBeautifulString(),
                achievements = "$unlockedAch / ${achievementManager.availableAchievements.size}"
            ),
            importError = null
        )
    }

    fun onPasteSaveClicked(saveString: String) {
        val data = saveManager.importSave(saveString)
        if (data != null) {
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(data.timestamp))
            val knownAchievements = achievementManager.availableAchievements.count { it.id in data.achievements }
            val preview = SavePreview(
                date = date,
                money = data.knowledge.toBeautifulString(),
                achievements = "$knownAchievements / ${achievementManager.availableAchievements.size}"
            )
            _uiState.value = _uiState.value.copy(importPreview = preview, pendingImportString = saveString, importError = null)
        } else {
            _uiState.value = _uiState.value.copy(importError = "Código inválido o corrupto.", importPreview = null, pendingImportString = "")
        }
    }

    fun onConfirmImportClicked() {
        val saveString = _uiState.value.pendingImportString
        if (saveString.isEmpty()) return
        val data = saveManager.importSave(saveString) ?: return

        viewModelScope.launch {
            transactionMutex.withLock {
                arcaneKnowledge = data.knowledge
                prestigeManager.residualSanity = data.sanity.setScale(0, RoundingMode.FLOOR)
                echoesBalance = data.echoes

                // Se parte de cero para que nada de la partida actual se mezcle con la importada
                buildingManager.resetForPrestige()
                data.buildings.forEach { b ->
                    buildingManager.availableBuildings.find { it.id == b.id }?.let {
                        it.amountOwned = b.amount
                        it.isVisible = b.isVisible || b.amount > 0
                    }
                }
                buildingManager.checkUnlocks()
                upgradeManager.availableUpgrades.forEach { u -> u.isBought = u.id in data.upgrades }
                achievementManager.availableAchievements.forEach { a -> a.isUnlocked = a.id in data.achievements }
                prestigeShopManager.upgrades.forEach { p ->
                    p.level = data.prestigeUpgrades.find { it.id == p.id }?.level ?: 0
                }
                statisticsManager.loadFromEntity(data.statistics)

                recalculateState()
                updateUi()
                clearExportString()

                // Se reemplaza TODO lo guardado (antes las mejoras importadas no se guardaban)
                repository.wipeProgressTables()
                saveProgressInternal()
            }
        }
    }

    fun clearExportString() {
        _uiState.value = _uiState.value.copy(saveStringExport = "", exportPreview = null, importPreview = null, pendingImportString = "", importError = null)
    }

    // =========================================================
    // INTERNOS
    // =========================================================

    private fun addKnowledgeInternal(amount: BigDecimal, isManual: Boolean, refreshUi: Boolean = true) {
        arcaneKnowledge = arcaneKnowledge.add(amount).max(BigDecimal.ZERO)
        statisticsManager.trackKnowledgeGenerated(amount, isManual)
        if (refreshUi) updateUi()
    }

    private fun updateUi() {
        val clickPower = buildingManager.getClickBuildingBonus().max(BigDecimal.ONE)
        val potential = currentPotentialPrestige()
        _uiState.value = _uiState.value.copy(
            knowledgeFormatted = arcaneKnowledge.toBeautifulString(),
            knowledgePerSecond = productionPerSecond.toBeautifulString(),
            rawKnowledge = arcaneKnowledge,
            clickMultiplier = clickPower.toBeautifulString(),
            prestigeCurrency = prestigeManager.residualSanity,
            prestigeCurrencyFormatted = prestigeManager.residualSanity.toBeautifulString(),
            potentialPrestige = potential,
            potentialPrestigeFormatted = potential.toBeautifulString(),
            canPrestige = potential > BigDecimal.ZERO,
            echoes = echoesBalance,
            echoesFormatted = echoesBalance.toBeautifulString()
        )
    }

    fun saveProgress() {
        viewModelScope.launch {
            if (!isLoaded) return@launch
            transactionMutex.withLock { saveProgressInternal() }
        }
    }

    private suspend fun saveProgressInternal() {
        // La foto se arma sin suspender; después se escribe
        val snapshot = GameSnapshot(
            knowledge = arcaneKnowledge,
            sanity = prestigeManager.residualSanity,
            echoes = echoesBalance,
            buildings = buildingManager.availableBuildings.map { BuildingEntity(it.id, it.amountOwned, it.isVisible) },
            upgrades = upgradeManager.availableUpgrades.map { UpgradeEntity(it.id, it.isBought) },
            unlockedAchievements = achievementManager.availableAchievements.filter { it.isUnlocked }.map { AchievementEntity(it.id, true) },
            prestigeUpgrades = prestigeShopManager.upgrades.map { PrestigeUpgradeEntity(it.id, it.level) },
            statistics = statisticsManager.getEntityForSave()
        )
        preferences.saveRituals(ritualManager.snapshot(), System.currentTimeMillis())
        repository.saveSnapshot(snapshot)
    }

    private fun String?.toBigDecimalOrZero(): BigDecimal =
        try {
            if (this.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(this).max(BigDecimal.ZERO)
        } catch (e: NumberFormatException) {
            BigDecimal.ZERO
        }

    companion object {
        private const val TICK_MS = 100L
        private const val AUTOSAVE_INTERVAL_MS = 5_000L
        private const val OFFLINE_THRESHOLD_MS = 10_000L

        private const val EVENT_MIN_DELAY_MS = 90_000L
        private const val EVENT_MAX_DELAY_MS = 240_000L
        private const val PENDING_EVENT_LIFETIME_MS = 60_000L
        private const val EVENT_RESULT_DISPLAY_MS = 2_500L
        private const val ACHIEVEMENT_POPUP_MS = 4_000L

        private const val ECHO_CHANCE_PER_CLICK = 0.01f
        private const val SECONDS_PER_OFFLINE_ECHO = 3_600L
        private const val MAX_OFFLINE_ECHOES = 12L

        private val THOUSAND = BigDecimal(1000)
        private val SIXTY = BigDecimal(60)
        private val EVENT_MIN_CLICKS_PER_MINUTE = BigDecimal(10)
    }
}
