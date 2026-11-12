package com.EGomez.idleecosystem.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.EGomez.idleecosystem.core.locale.AppLanguage
import com.EGomez.idleecosystem.core.math.NumberFormatting
import com.EGomez.idleecosystem.core.math.NumberNotation
import com.EGomez.idleecosystem.core.math.formatSecondsToTime
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.data.preferences.GamePreferences
import com.EGomez.idleecosystem.domain.logic.Attention
import com.EGomez.idleecosystem.domain.logic.AutomationSettings
import com.EGomez.idleecosystem.domain.engine.AchievementProgress
import com.EGomez.idleecosystem.domain.engine.EconomyState
import com.EGomez.idleecosystem.domain.engine.EngineEffect
import com.EGomez.idleecosystem.domain.engine.GameEngine
import com.EGomez.idleecosystem.domain.logic.RuneState
import com.EGomez.idleecosystem.domain.logic.SaveManager
import com.EGomez.idleecosystem.domain.logic.SoundManager
import com.EGomez.idleecosystem.domain.logic.SoundType
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.domain.model.Mutation
import com.EGomez.idleecosystem.domain.logic.MenuTab
import com.EGomez.idleecosystem.domain.model.GameStatistics
import com.EGomez.idleecosystem.domain.model.Ritual
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.RuneVariant
import com.EGomez.idleecosystem.domain.model.Upgrade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

data class ManualClickResult(
    val knowledgeGained: BigDecimal,
    val echoFound: Boolean
)

/**
 * Traduce el estado del motor a lo que dibuja la pantalla y reenvía las acciones del
 * jugador. No contiene reglas del juego: esas viven en [GameEngine].
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val engine: GameEngine,
    private val saveManager: SaveManager,
    private val soundManager: SoundManager,
    private val preferences: GamePreferences
) : ViewModel() {

    private var debugTierIndex = 0

    private val _uiState = MutableStateFlow(
        GameUiState(
            isMusicEnabled = soundManager.isMusicEnabled,
            isSfxEnabled = soundManager.isSfxEnabled,
            areParticlesEnabled = preferences.areParticlesEnabled,
            isFloatingTextEnabled = preferences.isFloatingTextEnabled,
            numberNotation = preferences.numberNotation,
            language = AppLanguage.fromTag(preferences.languageTag) ?: AppLanguage.systemDefault(),
            automation = preferences.automation
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val buildingsState: StateFlow<List<Building>> = engine.buildings

    /** Hitos de cada edificio, para el botón "Hito" y la barra de cada tarjeta. */
    val buildingMilestones: Map<String, List<Int>> = engine.buildingMilestones

    private val _showPurchasedState = MutableStateFlow(false)
    val showPurchasedState: StateFlow<Boolean> = _showPurchasedState.asStateFlow()

    // Filtrar las compradas es una decisión de la vista: ya no obliga a recalcular el juego
    val upgradesState: StateFlow<List<Upgrade>> =
        combine(engine.upgrades, _showPurchasedState) { upgrades, showPurchased ->
            if (showPurchased) upgrades else upgrades.filter { !it.isBought }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val achievementsState: StateFlow<List<Achievement>> = engine.achievements
    val ritualsState: StateFlow<List<Ritual>> = engine.rituals
    val statisticsState: StateFlow<GameStatistics> = engine.statistics

    init {
        NumberFormatting.notation = preferences.numberNotation
        engine.setAutomation(preferences.automation)
        engine.setAttention(preferences.attention)
        observeEngine()
        engine.start(viewModelScope)
        if (GameEngine.DEBUG_RUNES) startDebugRuneCycle()
    }

    // =====================================================================
    // ESTADO DEL MOTOR -> UI
    // =====================================================================

    private fun observeEngine() {
        // Formatear números enormes 10 veces por segundo no le corresponde al hilo de la UI.
        // MutableStateFlow.update es seguro entre hilos.
        viewModelScope.launch(Dispatchers.Default) {
            engine.economy.collect { economy -> _uiState.update { it.withEconomy(economy) } }
        }
        viewModelScope.launch {
            engine.achievementProgress.collect { progress -> _uiState.update { it.withAchievements(progress) } }
        }
        viewModelScope.launch {
            engine.nextGoal.collect { goal -> _uiState.update { it.copy(nextGoal = goal) } }
        }
        viewModelScope.launch {
            engine.prestigeUpgrades.collect { upgrades -> _uiState.update { it.copy(prestigeUpgrades = upgrades) } }
        }
        viewModelScope.launch {
            engine.events.collect { events ->
                _uiState.update {
                    it.copy(
                        currentEvent = events.currentEvent,
                        eventResultText = events.resultText,
                        activeBuffMultiplier = events.buffMultiplier,
                        buffTimeRemaining = events.buffSecondsRemaining
                    )
                }
            }
        }
        viewModelScope.launch {
            engine.tutorialStep.collect { step ->
                _uiState.update { it.copy(currentTutorialStep = step) }
                if (step != null) soundManager.playSound(SoundType.TUTORIAL_POPUP)
            }
        }
        viewModelScope.launch {
            engine.isTutorialEnabled.collect { enabled -> _uiState.update { it.copy(isTutorialEnabled = enabled) } }
        }
        if (!GameEngine.DEBUG_RUNES) {
            viewModelScope.launch {
                engine.currentRune.collect { rune -> _uiState.update { it.copy(activeRune = rune) } }
            }
            viewModelScope.launch {
                engine.runeState.collect { state -> _uiState.update { it.copy(runeState = state) } }
            }
            viewModelScope.launch {
                engine.activeRuneBuffs.collect { buffs -> _uiState.update { it.copy(activeRuneBuffs = buffs) } }
            }
        }
        viewModelScope.launch {
            engine.effects.collect { effect -> onEngineEffect(effect) }
        }
    }

    private fun GameUiState.withEconomy(economy: EconomyState) = copy(
        knowledgeFormatted = economy.knowledge.toBeautifulString(),
        knowledgePerSecond = economy.productionPerSecond.toBeautifulString(),
        rawKnowledge = economy.knowledge,
        clickMultiplier = economy.clickPower.toBeautifulString(),
        prestigeCurrencyFormatted = economy.sanity.toBeautifulString(),
        rawSanity = economy.sanity,
        potentialPrestigeFormatted = economy.potentialPrestige.toBeautifulString(),
        knowledgeThisRunFormatted = economy.knowledgeThisRun.toBeautifulString(),
        nextSanityGainFormatted = economy.prestigePacing.nextGain.toBeautifulString(),
        nextSanityAtFormatted = economy.prestigePacing.nextAt.toBeautifulString(),
        doubleSanityGainFormatted = economy.prestigePacing.doubleGain?.toBeautifulString(),
        doubleSanityAtFormatted = economy.prestigePacing.doubleAt?.toBeautifulString(),
        canPrestige = economy.canPrestige,
        globalMultiplier = economy.globalMultiplier,
        buildingCostMultiplier = economy.buildingCostMultiplier,
        echoes = economy.echoes,
        echoesFormatted = economy.echoes.toBeautifulString(),
        revealed = economy.revealed,
        menuUnlocks = economy.menuUnlocks,
        newTabs = economy.newTabs,
        seenMutations = economy.seenMutations,
        ascensions = economy.ascensions,
        activePact = economy.activePact,
        chaosMultiplier = economy.chaosMultiplier,
        attention = economy.attention,
        attentionPoints = economy.attentionPoints,
        activeTrial = economy.activeTrial,
        trialProgress = economy.trialProgress,
        completedTrials = economy.completedTrials,
        deliriumMultiplier = economy.deliriumMultiplier,
        earlyPacts = economy.earlyPacts,
        madness = economy.madness,
        activeMutation = economy.activeMutation,
        mutationOffer = economy.mutationOffer,
        mutationsSeen = economy.mutationsSeen,
        transcendRevealed = economy.transcendRevealed,
        cycleAscensions = economy.cycleAscensions,
        etherGain = economy.etherGain,
        ether = economy.ether,
        transcendences = economy.transcendences,
        milestoneAscensions = economy.milestoneAscensions,
        veilBroken = economy.veilBroken,
        veilPressing = economy.veilPressing,
        veilFactor = economy.veilFactor,
        transcendRequired = economy.transcendRequired,
        eternalLevels = economy.eternalLevels,
        consecrationRevealed = economy.consecrationRevealed,
        consecrations = economy.consecrations,
        tide = economy.tide,
        tideSecondsLeft = economy.tideSecondsLeft,
        collapseSeconds = economy.collapseSeconds,
        possessedBuilding = economy.possessedBuilding?.let { id -> engine.buildings.value.firstOrNull { it.id == id }?.name ?: id },
        runeSlots = economy.runeSlots,
        engravedRunes = economy.engravedRunes,
        runeCatches = economy.runeCatches,
        runeMastery = economy.runeMastery
    )

    private fun GameUiState.withAchievements(progress: AchievementProgress) = copy(
        achievementsProgress = "${progress.unlocked} / ${progress.total}",
        secretsFound = progress.secretsFound,
        secretsTotal = progress.secretsTotal,
        setsCompleted = progress.setsCompleted,
        setsTotal = progress.setsTotal
    )

    private fun onEngineEffect(effect: EngineEffect) {
        when (effect) {
            is EngineEffect.OfflineGains -> _uiState.update {
                it.copy(
                    showOfflineDialog = true,
                    offlineGains = effect.gains.toBeautifulString(),
                    offlineTimeFormatted = formatSecondsToTime(effect.seconds)
                )
            }
            is EngineEffect.LegacyRefund -> _uiState.update {
                it.copy(showRefundDialog = true, refundAmountString = effect.amount.toBeautifulString())
            }
            is EngineEffect.AutoAscended -> {
                val gain = effect.sanity.toBeautifulString()
                _uiState.update { it.copy(autoAscensionNotice = gain) }
                viewModelScope.launch {
                    delay(AUTO_ASCENSION_NOTICE_MS)
                    _uiState.update { if (it.autoAscensionNotice == gain) it.copy(autoAscensionNotice = null) else it }
                }
            }
            is EngineEffect.MadnessOmen -> showMadnessNotice(
                MadnessNotice(possessedBuilding = effect.possessedBuilding, echoesLost = effect.echoesLost?.toBeautifulString())
            )
            EngineEffect.MadnessCollapse -> showMadnessNotice(MadnessNotice(collapse = true))
            is EngineEffect.Transcended -> {
                val gain = effect.ether
                _uiState.update { it.copy(transcendNotice = gain) }
                viewModelScope.launch {
                    delay(TRIAL_NOTICE_MS)
                    _uiState.update { if (it.transcendNotice == gain) it.copy(transcendNotice = null) else it }
                }
            }
            is EngineEffect.TrialCompleted -> {
                val trial = effect.trial
                _uiState.update { it.copy(trialNotice = trial) }
                viewModelScope.launch {
                    delay(TRIAL_NOTICE_MS)
                    _uiState.update { if (it.trialNotice == trial) it.copy(trialNotice = null) else it }
                }
            }
            is EngineEffect.AchievementUnlocked -> {
                val achievement = effect.achievement
                _uiState.update { it.copy(lastUnlockedAchievement = achievement) }
                viewModelScope.launch {
                    delay(ACHIEVEMENT_POPUP_MS)
                    _uiState.update {
                        if (it.lastUnlockedAchievement == achievement) it.copy(lastUnlockedAchievement = null) else it
                    }
                }
            }
        }
    }

    private fun showMadnessNotice(notice: MadnessNotice) {
        _uiState.update { it.copy(madnessNotice = notice) }
        viewModelScope.launch {
            delay(MADNESS_NOTICE_MS)
            _uiState.update { if (it.madnessNotice == notice) it.copy(madnessNotice = null) else it }
        }
    }

    // =====================================================================
    // ACCIONES DEL JUGADOR
    // =====================================================================

    /**
     * El texto flotante y las partículas necesitan el resultado al instante: se decide
     * aquí con los últimos valores publicados y el motor aplica el clic.
     */
    fun onManualClick(): ManualClickResult {
        val economy = engine.economy.value
        val echoFound = Random.nextDouble() < economy.echoChance
        engine.click(echoFound)
        return ManualClickResult(economy.clickPower, echoFound)
    }

    fun setBuyMultiplier(value: Int) = _uiState.update { it.copy(buyMultiplier = value) }

    fun toggleShowPurchased() = _showPurchasedState.update { !it }

    fun onBuyBuilding(buildingId: String) = engine.buyBuilding(buildingId, _uiState.value.buyMultiplier)

    fun onBuyUpgrade(upgradeId: String) = engine.buyUpgrade(upgradeId)

    fun onBuyAllUpgrades() = engine.buyAllAffordableUpgrades()

    fun onBuyPrestigeUpgrade(id: String) = engine.buyPrestigeUpgrade(id)

    fun onActivateRitual(ritualId: String) = engine.activateRitual(ritualId)

    fun onEventOptionSelected(optionIndex: Int) = engine.chooseEventOption(optionIndex)

    fun onAscendClicked(nextPact: Pact? = null, abandonPath: Boolean = false, nextTrial: Trial? = null, nextMutation: Mutation? = null) =
        engine.ascend(nextPact, abandonPath, nextTrial, nextMutation)

    fun onAbandonTrial() = engine.abandonTrial()

    fun onTranscend() = engine.transcend()

    fun onBuyEternal(id: String) = engine.buyEternal(id)

    fun onConsecrate(buildingId: String) = engine.consecrate(buildingId)

    fun onEngraveRune(runeId: String) = engine.engraveRune(runeId)

    fun onRemoveEngraving(runeId: String) = engine.removeEngraving(runeId)

    fun onHardResetClicked() = engine.hardReset()

    fun onRuneInteractionSuccess() {
        if (!GameEngine.DEBUG_RUNES) {
            engine.completeRune()
            return
        }
        _uiState.update { it.copy(runeState = RuneState.SUCCESS) }
        soundManager.playSound(SoundType.RUNE_SUCCESS)
        engine.grantKnowledge(BigDecimal("10000"))
        viewModelScope.launch {
            delay(1500)
            _uiState.update { it.copy(runeState = RuneState.HIDDEN, activeRune = null) }
        }
    }

    /** Lo llama la Activity en onStart/onStop. */
    fun onAppForegroundChanged(inForeground: Boolean) = engine.setForeground(inForeground)

    fun dismissOfflineDialog() = _uiState.update { it.copy(showOfflineDialog = false) }

    fun dismissRefundDialog() = _uiState.update { it.copy(showRefundDialog = false) }

    // --- TUTORIAL ---

    fun dismissTutorial() = engine.dismissTutorial()

    fun toggleTutorial(enabled: Boolean) = engine.setTutorialEnabled(enabled)

    fun onMenuTabSelected(tab: MenuTab) = engine.onMenuTabSelected(tab)

    fun onPrestigeShopOpened() = engine.onPrestigeShopOpened()

    fun onAscensionDialogOpened() = engine.onAscensionDialogOpened()

    // --- AJUSTES ---

    fun toggleMusic() {
        val newState = !soundManager.isMusicEnabled
        soundManager.toggleMusic(newState)
        _uiState.update { it.copy(isMusicEnabled = newState) }
    }

    fun toggleSfx() {
        val newState = !soundManager.isSfxEnabled
        soundManager.toggleSfx(newState)
        _uiState.update { it.copy(isSfxEnabled = newState) }
    }

    fun toggleAutoBuildings() = updateAutomation { it.copy(buildings = !it.buildings) }

    fun toggleAutoGrimoire() = updateAutomation { it.copy(grimoire = !it.grimoire) }

    /** Elige el ritual automático; tocar el mismo otra vez lo apaga. */
    fun toggleAutoRitual(ritualId: String) =
        updateAutomation { it.copy(ritualId = if (it.ritualId == ritualId) null else ritualId) }

    fun selectAutoRitual(ritualId: String?) = updateAutomation { it.copy(ritualId = ritualId) }

    /** Pone un punto de Atención en [target] si queda alguno libre. */
    fun addAttention(target: String) {
        val state = _uiState.value
        if (Attention.used(state.attention) >= state.attentionPoints) return
        updateAttention(state.attention + (target to (state.attention[target] ?: 0) + 1))
    }

    /** Quita un punto de Atención de [target]. */
    fun removeAttention(target: String) {
        val current = _uiState.value.attention[target] ?: return
        updateAttention(_uiState.value.attention + (target to current - 1))
    }

    fun clearAttention() = updateAttention(emptyMap())

    private fun updateAttention(allocation: Map<String, Int>) {
        val clean = allocation.filterValues { it > 0 }
        preferences.attention = clean
        engine.setAttention(clean)
        // Se ve al instante; el motor confirma el reparto en su próxima publicación
        _uiState.update { it.copy(attention = clean) }
    }

    fun setAutomationBudget(percent: Int) = updateAutomation { it.copy(budgetPercent = percent) }

    fun toggleAutoAscend() = updateAutomation { it.copy(autoAscend = !it.autoAscend) }

    fun toggleAutoTranscend() = updateAutomation { it.copy(autoTranscend = !it.autoTranscend) }

    fun breakVeil() = engine.breakVeil()

    /** Marca o desmarca un edificio para el autocomprador. */
    fun toggleBuildingAutobuy(buildingId: String) = updateAutomation {
        val excluded = it.excludedBuildings
        it.copy(excludedBuildings = if (buildingId in excluded) excluded - buildingId else excluded + buildingId)
    }

    private fun updateAutomation(change: (AutomationSettings) -> AutomationSettings) {
        val settings = change(_uiState.value.automation)
        preferences.automation = settings
        engine.setAutomation(settings)
        _uiState.update { it.copy(automation = settings) }
    }

    fun toggleParticles() {
        val newState = !_uiState.value.areParticlesEnabled
        preferences.areParticlesEnabled = newState
        _uiState.update { it.copy(areParticlesEnabled = newState) }
    }

    /** Guarda el idioma; la pantalla recrea la Activity para aplicarlo. */
    fun setLanguage(language: AppLanguage) {
        preferences.languageTag = language.tag
        _uiState.update { it.copy(language = language) }
    }

    fun setNumberNotation(notation: NumberNotation) {
        preferences.numberNotation = notation
        NumberFormatting.notation = notation
        // Los textos ya formateados se rehacen al instante (con el juego en pausa el
        // motor no vuelve a publicar la economía hasta reanudar)
        _uiState.update { it.copy(numberNotation = notation).withEconomy(engine.economy.value) }
    }

    fun toggleFloatingText() {
        val newState = !_uiState.value.isFloatingTextEnabled
        preferences.isFloatingTextEnabled = newState
        _uiState.update { it.copy(isFloatingTextEnabled = newState) }
    }

    // --- EXPORTAR / IMPORTAR ---

    fun onExportSaveClicked() {
        viewModelScope.launch {
            val exported = engine.exportSave()
            val preview = SavePreview(
                date = formatDate(System.currentTimeMillis()),
                money = exported.knowledge.toBeautifulString(),
                achievements = "${exported.unlockedAchievements} / ${exported.totalAchievements}"
            )
            _uiState.update { it.copy(saveStringExport = exported.code, exportPreview = preview, importFailed = false) }
        }
    }

    fun onPasteSaveClicked(saveString: String) {
        viewModelScope.launch {
            val data = saveManager.importSave(saveString)
            if (data == null) {
                _uiState.update { it.copy(importFailed = true, importPreview = null, pendingImportString = "") }
                return@launch
            }
            val known = engine.countKnownAchievements(data.achievements)
            val total = engine.achievementProgress.value.total
            val preview = SavePreview(
                date = formatDate(data.timestamp),
                money = data.knowledge.toBeautifulString(),
                achievements = "$known / $total"
            )
            _uiState.update { it.copy(importPreview = preview, pendingImportString = saveString, importFailed = false) }
        }
    }

    fun onConfirmImportClicked() {
        val saveString = _uiState.value.pendingImportString
        if (saveString.isEmpty()) return
        viewModelScope.launch {
            val data = saveManager.importSave(saveString)
            if (data == null) {
                _uiState.update { it.copy(importFailed = true) }
                return@launch
            }
            engine.importSave(data)
            clearExportString()
        }
    }

    fun clearExportString() = _uiState.update {
        it.copy(saveStringExport = "", exportPreview = null, importPreview = null, pendingImportString = "", importFailed = false)
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

    // --- DEPURACIÓN ---

    private fun startDebugRuneCycle() {
        viewModelScope.launch {
            val tiers = listOf(RuneTier.TIER_1_SWIPE, RuneTier.TIER_2_HOLD, RuneTier.TIER_3_TRACE)
            while (true) {
                delay(5000)
                val nextTier = tiers[debugTierIndex % tiers.size]
                debugTierIndex++
                val debugRune = RuneVariant(
                    id = UUID.randomUUID().toString(),
                    name = "Runa Debug $debugTierIndex",
                    tier = nextTier,
                    weight = 100,
                    color = Color.Cyan,
                    description = "Runa generada por modo Debug"
                )
                _uiState.update { it.copy(activeRune = debugRune, runeState = RuneState.APPEARING) }
                soundManager.playSound(SoundType.RUNE_SPAWN)
                delay(500)
                _uiState.update { it.copy(runeState = RuneState.ACTIVE) }
            }
        }
    }

    private companion object {
        const val ACHIEVEMENT_POPUP_MS = 4_000L

        const val AUTO_ASCENSION_NOTICE_MS = 4_000L
        const val TRIAL_NOTICE_MS = 5_000L
        const val MADNESS_NOTICE_MS = 4_000L
    }
}
