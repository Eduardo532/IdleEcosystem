package com.EGomez.idleecosystem.domain.engine

import com.EGomez.idleecosystem.core.di.EngineDispatcher
import com.EGomez.idleecosystem.data.database.AchievementEntity
import com.EGomez.idleecosystem.data.database.BuildingEntity
import com.EGomez.idleecosystem.data.database.PrestigeUpgradeEntity
import com.EGomez.idleecosystem.data.database.UpgradeEntity
import com.EGomez.idleecosystem.data.repository.GameSnapshot
import com.EGomez.idleecosystem.data.repository.SaveCoordinator
import com.EGomez.idleecosystem.data.repository.SaveRequest
import com.EGomez.idleecosystem.domain.logic.AchievementManager
import com.EGomez.idleecosystem.domain.logic.BuffMath
import com.EGomez.idleecosystem.domain.logic.BuildingManager
import com.EGomez.idleecosystem.domain.logic.FullSaveData
import com.EGomez.idleecosystem.domain.logic.GrimoireShopping
import com.EGomez.idleecosystem.domain.logic.PrestigeManager
import com.EGomez.idleecosystem.domain.logic.PrestigeShopManager
import com.EGomez.idleecosystem.domain.logic.RitualManager
import com.EGomez.idleecosystem.domain.logic.RuneBuff
import com.EGomez.idleecosystem.domain.logic.RuneManager
import com.EGomez.idleecosystem.domain.logic.RuneReward
import com.EGomez.idleecosystem.domain.logic.RuneState
import com.EGomez.idleecosystem.domain.logic.SaveManager
import com.EGomez.idleecosystem.domain.logic.SoundManager
import com.EGomez.idleecosystem.domain.logic.SoundType
import com.EGomez.idleecosystem.domain.logic.StatisticsManager
import com.EGomez.idleecosystem.domain.logic.TutorialManager
import com.EGomez.idleecosystem.domain.logic.TutorialProgress
import com.EGomez.idleecosystem.domain.logic.UpgradeManager
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.domain.model.Building
import com.EGomez.idleecosystem.domain.model.BuildingType
import com.EGomez.idleecosystem.domain.model.EffectType
import com.EGomez.idleecosystem.domain.model.GameStatistics
import com.EGomez.idleecosystem.domain.model.PrestigeUpgrade
import com.EGomez.idleecosystem.domain.model.Ritual
import com.EGomez.idleecosystem.domain.model.RitualEffectType
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.RuneVariant
import com.EGomez.idleecosystem.domain.model.TutorialStep
import com.EGomez.idleecosystem.domain.model.Upgrade
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.random.Random

/** Economía publicada para la UI. Valores crudos: el formato lo decide el ViewModel. */
data class EconomyState(
    val knowledge: BigDecimal = BigDecimal.ZERO,
    val productionPerSecond: BigDecimal = BigDecimal.ZERO,
    val clickPower: BigDecimal = BigDecimal.ONE,
    val sanity: BigDecimal = BigDecimal.ZERO,
    val echoes: BigDecimal = BigDecimal.ZERO,
    val globalMultiplier: BigDecimal = BigDecimal.ONE,
    val potentialPrestige: BigDecimal = BigDecimal.ZERO,
    val canPrestige: Boolean = false,
    val buildingCostMultiplier: Double = 1.0,
    val echoChance: Double = GameEngine.BASE_ECHO_CHANCE
)

data class AchievementProgress(
    val unlocked: Int = 0,
    val total: Int = 0,
    val secretsFound: Int = 0,
    val secretsTotal: Int = 0
)

/** Avisos de una sola vez (diálogos y popups), que no forman parte del estado. */
sealed interface EngineEffect {
    data class OfflineGains(val gains: BigDecimal, val seconds: Long) : EngineEffect
    data class LegacyRefund(val amount: BigDecimal) : EngineEffect
    data class AchievementUnlocked(val achievement: Achievement) : EngineEffect
}

data class ExportedSave(
    val code: String,
    val knowledge: BigDecimal,
    val unlockedAchievements: Int,
    val totalAchievements: Int
)

/**
 * El motor del juego: dueño de la economía, el bucle, las acciones del jugador y el
 * recálculo de multiplicadores.
 *
 * Antes todo esto vivía en un MainViewModel de más de mil líneas. Aquí se aplica una
 * regla simple: TODO el estado mutable (este y el de los managers) se toca solo desde
 * [dispatcher], un único hilo lógico. Las acciones públicas no devuelven nada: encolan
 * su trabajo en ese hilo y el resultado sale por los StateFlow publicados, siempre como
 * copias inmutables. Así ninguna acción puede intercalarse con otra a medias y la UI
 * nunca lee un objeto que el motor está modificando.
 *
 * El guardado no bloquea: el motor arma la foto de la partida y se la pasa a
 * [SaveCoordinator], que escribe en segundo plano.
 */
class GameEngine @Inject constructor(
    private val buildingManager: BuildingManager,
    private val upgradeManager: UpgradeManager,
    private val prestigeManager: PrestigeManager,
    private val achievementManager: AchievementManager,
    private val prestigeShopManager: PrestigeShopManager,
    private val saveManager: SaveManager,
    private val soundManager: SoundManager,
    private val statsManager: StatisticsManager,
    private val ritualManager: RitualManager,
    private val runeManager: RuneManager,
    private val tutorialManager: TutorialManager,
    private val eventScheduler: EventScheduler,
    private val saveCoordinator: SaveCoordinator,
    @EngineDispatcher private val dispatcher: CoroutineDispatcher
) {
    // --- ESTADO INTERNO (solo se toca desde `dispatcher`) ---
    private var knowledge = BigDecimal.ZERO
    private var echoes = BigDecimal.ZERO
    private var productionPerSecond = BigDecimal.ZERO

    // Producción sin potenciadores temporales (runas, rituales, eventos): la usan los
    // saltos de tiempo y el progreso offline.
    private var baseProductionPerSecond = BigDecimal.ZERO
    private var globalMultiplier = BigDecimal.ONE
    private var potentialPrestige = BigDecimal.ZERO
    private var costMultiplier = 1.0

    // Evita que el autoguardado escriba un estado vacío antes de terminar la carga
    private var isLoaded = false
    private var isInForeground = true
    private var backgroundSinceMillis = 0L
    private var oneSecondAccumulator = 0L

    private var scope: CoroutineScope? = null

    // --- ESTADO PUBLICADO ---
    private val _economy = MutableStateFlow(EconomyState())
    val economy: StateFlow<EconomyState> = _economy.asStateFlow()

    private val _buildings = MutableStateFlow<List<Building>>(emptyList())
    val buildings: StateFlow<List<Building>> = _buildings.asStateFlow()

    /** Todas las mejoras ordenadas por costo; la UI decide si oculta las compradas. */
    private val _upgrades = MutableStateFlow<List<Upgrade>>(emptyList())
    val upgrades: StateFlow<List<Upgrade>> = _upgrades.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _achievementProgress = MutableStateFlow(AchievementProgress())
    val achievementProgress: StateFlow<AchievementProgress> = _achievementProgress.asStateFlow()

    private val _prestigeUpgrades = MutableStateFlow<List<PrestigeUpgrade>>(emptyList())
    val prestigeUpgrades: StateFlow<List<PrestigeUpgrade>> = _prestigeUpgrades.asStateFlow()

    private val _statistics = MutableStateFlow(GameStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    private val _effects = Channel<EngineEffect>(Channel.BUFFERED)
    val effects: Flow<EngineEffect> = _effects.receiveAsFlow()

    val events: StateFlow<EventState> = eventScheduler.state
    val rituals: StateFlow<List<Ritual>> = ritualManager.rituals
    val currentRune: StateFlow<RuneVariant?> = runeManager.currentRune
    val runeState: StateFlow<RuneState> = runeManager.runeState
    val activeRuneBuffs: StateFlow<List<RuneBuff>> = runeManager.activeBuffs
    val tutorialStep: StateFlow<TutorialStep?> = tutorialManager.currentStep
    val isTutorialEnabled: StateFlow<Boolean> = tutorialManager.isTutorialEnabled

    /** Arranca el motor dentro del ámbito de quien lo usa (el ViewModel). */
    fun start(scope: CoroutineScope) {
        check(this.scope == null) { "El motor ya está en marcha" }
        this.scope = scope
        scope.launch(dispatcher) {
            observeRunes(this)
            eventScheduler.scheduleNext(System.currentTimeMillis())
            load()
            launch { autoSaveLoop() }
            gameLoop()
        }
    }

    // =====================================================================
    // ACCIONES (se encolan en el hilo del motor)
    // =====================================================================

    /**
     * Clic manual. La UI ya mostró el texto flotante con los últimos valores
     * publicados ([EconomyState.clickPower] y [EconomyState.echoChance]); aquí se aplica.
     */
    fun click(echoFound: Boolean) = post {
        applyClick(echoFound)
        publishEconomy()
    }

    fun buyBuilding(buildingId: String, amount: Int) = post {
        val discount = BigDecimal.valueOf(runeManager.getCostReductionMultiplier())
        val cost = buildingManager.getCost(buildingId, amount, knowledge, discount)
        val bought = buildingManager.tryBuyBuilding(buildingId, knowledge, amount, discount)
        if (bought > 0) {
            soundManager.playSound(SoundType.BUY)
            knowledge = knowledge.subtract(cost)
            recalculate()
            requestSave()
        }
    }

    fun buyUpgrade(upgradeId: String) = post {
        val upgrade = upgradeManager.availableUpgrades.find { it.id == upgradeId } ?: return@post
        // Una mejora que todavía no aparece en el Grimorio no se puede comprar
        if (!upgradeManager.isUnlocked(upgrade, buildingManager.availableBuildings)) return@post
        if (upgradeManager.tryBuyUpgrade(upgradeId, knowledge)) {
            soundManager.playSound(SoundType.BUY)
            knowledge = knowledge.subtract(upgrade.cost)
            recalculate()
            requestSave()
        }
    }

    /**
     * Compra, de la más barata a la más cara, todas las mejoras del Grimorio que
     * alcance el Conocimiento actual. Un solo recálculo y un solo guardado al final.
     */
    fun buyAllAffordableUpgrades() = post {
        val unlocked = upgradeManager.availableUpgrades
            .filter { upgradeManager.isUnlocked(it, buildingManager.availableBuildings) }
        var bought = 0
        for (upgrade in GrimoireShopping.plan(unlocked, knowledge)) {
            if (upgradeManager.tryBuyUpgrade(upgrade.id, knowledge)) {
                knowledge = knowledge.subtract(upgrade.cost)
                bought++
            }
        }
        if (bought > 0) {
            soundManager.playSound(SoundType.BUY)
            recalculate()
            requestSave()
        }
    }

    fun buyPrestigeUpgrade(id: String) = post {
        val cost = prestigeShopManager.getCost(id) ?: return@post
        if (prestigeManager.residualSanity < cost) return@post
        if (prestigeShopManager.tryBuy(id, prestigeManager.residualSanity)) {
            soundManager.playSound(SoundType.BUY)
            prestigeManager.residualSanity = prestigeManager.residualSanity.subtract(cost)
            statsManager.trackSanitySpent(cost)
            recalculate()
            requestSave()
        }
    }

    fun activateRitual(ritualId: String) = post {
        if (ritualManager.tryActivateRitual(ritualId, echoes)) {
            val ritual = ritualManager.rituals.value.find { it.id == ritualId } ?: return@post
            soundManager.playSound(SoundType.BUY)
            echoes = echoes.subtract(ritual.baseCost)
            if (ritual.effectType == RitualEffectType.INSTANT_KNOWLEDGE) {
                // Producción base, igual que los saltos de tiempo de las runas: los
                // potenciadores activos no se multiplican sobre este regalo
                val gain = baseProductionPerSecond.multiply(BigDecimal.valueOf(ritual.effectValue))
                if (gain.signum() > 0) addKnowledge(gain, isManual = false)
            }
            recalculate()
        }
    }

    fun completeRune() = post {
        runeManager.completeRuneInteraction()
        delay(RUNE_SUCCESS_DISPLAY_MS)
        runeManager.hideRune()
    }

    fun chooseEventOption(optionIndex: Int) = post {
        val option = eventScheduler.choose(optionIndex, System.currentTimeMillis()) ?: return@post
        option.effects.forEach { effect ->
            when (effect.type) {
                EffectType.ADD_KNOWLEDGE_PERCENT -> {
                    val bonus = knowledge.multiply(BigDecimal.valueOf(effect.value))
                    if (bonus.signum() != 0) addKnowledge(bonus, isManual = false)
                }
                EffectType.ADD_SANITY_PERCENT -> applySanityPercent(effect.value)
                // El potenciador global lo activa EventScheduler.choose()
                EffectType.GLOBAL_BUFF, EffectType.NOTHING -> Unit
            }
        }
        recalculate()
        delay(EVENT_RESULT_DISPLAY_MS)
        eventScheduler.close(System.currentTimeMillis())
    }

    fun ascend() = post {
        val finalPotential = prestigeManager.calculatePotentialPrestige(knowledgeThisRun())
            .multiply(prestigeShopManager.getPrestigeGainMultiplier())
        if (finalPotential.signum() <= 0) return@post

        soundManager.playSound(SoundType.BUY)
        prestigeManager.addPrestige(finalPotential)
        statsManager.trackPrestige(finalPotential)

        buildingManager.resetForPrestige()
        upgradeManager.resetForPrestige()
        knowledge = prestigeShopManager.getStartingKnowledgeBonus()
        eventScheduler.clearBuff()

        recalculate()
        // Reemplazo completo: no quedan filas de edificios o mejoras de la vida anterior
        requestSave(replace = true)
    }

    fun hardReset() = post {
        knowledge = BigDecimal.ZERO
        prestigeManager.residualSanity = BigDecimal.ZERO
        echoes = BigDecimal.ZERO

        buildingManager.resetForPrestige()
        upgradeManager.resetForPrestige()
        achievementManager.resetAchievements()
        prestigeShopManager.upgrades.forEach { it.level = 0 }
        statsManager.hardReset()
        tutorialManager.resetTutorial()
        runeManager.hideRune()
        ritualManager.reset()
        eventScheduler.clearBuff()

        recalculate()
        requestSave(replace = true)
    }

    fun importSave(data: FullSaveData) = post {
        knowledge = data.knowledge
        prestigeManager.residualSanity = data.sanity
        echoes = data.echoes

        // Se parte de cero para que nada de la partida actual se mezcle con la importada
        buildingManager.resetForPrestige()
        data.buildings.forEach { saved ->
            buildingManager.availableBuildings.find { it.id == saved.id }?.let {
                it.amountOwned = saved.amount
                it.isVisible = saved.isVisible || saved.amount > 0
            }
        }
        upgradeManager.availableUpgrades.forEach { it.isBought = data.upgrades.contains(it.id) }
        achievementManager.resetAchievements()
        achievementManager.availableAchievements.forEach { it.isUnlocked = data.achievements.contains(it.id) }
        prestigeShopManager.upgrades.forEach { upgrade ->
            upgrade.level = data.prestigeUpgrades.find { it.id == upgrade.id }?.level ?: 0
        }
        statsManager.loadFromEntity(data.statistics)

        // Los tutoriales que la partida importada ya vio no se vuelven a mostrar
        tutorialManager.markStepsAsCompleted(TutorialProgress.stepsSeenIn(data))

        recalculate()
        requestSave(replace = true)
    }

    suspend fun exportSave(): ExportedSave = withContext(dispatcher) {
        val code = saveManager.exportSave(
            knowledge,
            prestigeManager.residualSanity,
            echoes,
            buildingManager.availableBuildings,
            upgradeManager.availableUpgrades,
            achievementManager.availableAchievements,
            prestigeShopManager.upgrades,
            statsManager.getEntityForSave(),
            tutorialManager.getCompletedStepsList()
        )
        ExportedSave(
            code = code,
            knowledge = knowledge,
            unlockedAchievements = achievementManager.availableAchievements.count { it.isUnlocked },
            totalAchievements = achievementManager.availableAchievements.size
        )
    }

    /** Cuántos logros del catálogo trae una partida importada. */
    suspend fun countKnownAchievements(ids: Collection<String>): Int = withContext(dispatcher) {
        achievementManager.availableAchievements.count { it.id in ids }
    }

    /** Solo para el modo de depuración de runas. */
    fun grantKnowledge(amount: BigDecimal) = post {
        addKnowledge(amount, isManual = false)
        publishEconomy()
    }

    fun save() = post { requestSave() }

    /**
     * Con la app en segundo plano el juego se pausa (runas, eventos, sonidos) y el
     * tiempo transcurrido se acredita como tiempo offline al volver.
     */
    fun setForeground(inForeground: Boolean) = post(requiresLoad = false) {
        if (inForeground == isInForeground) return@post
        isInForeground = inForeground
        val now = System.currentTimeMillis()

        if (!inForeground) {
            backgroundSinceMillis = now
            requestSave()
            return@post
        }

        val since = backgroundSinceMillis
        backgroundSinceMillis = 0L
        if (since <= 0L || !isLoaded) return@post
        val elapsedMillis = now - since
        if (elapsedMillis <= 0) return@post
        ritualManager.tick(elapsedMillis / 1000)
        if (elapsedMillis > OFFLINE_THRESHOLD_MS) grantOfflineGains(elapsedMillis)
        recalculate()
    }

    // --- TUTORIAL ---

    fun dismissTutorial() = post {
        tutorialManager.dismissCurrentStep()
        requestSave()
    }

    fun setTutorialEnabled(enabled: Boolean) = post {
        tutorialManager.setTutorialEnabled(enabled)
        requestSave()
    }

    fun onMenuTabSelected(index: Int) = post {
        if (index == 1) tutorialManager.triggerStep(TutorialStep.UPGRADES_TAB)
        if (index == 2 && echoes.signum() > 0) tutorialManager.triggerStep(TutorialStep.RITUALS_TAB)
    }

    fun onPrestigeShopOpened() = post { tutorialManager.triggerStep(TutorialStep.PRESTIGE_SHOP) }

    fun onAscensionDialogOpened() = post { tutorialManager.triggerStep(TutorialStep.PRESTIGE_MECHANICS) }

    // =====================================================================
    // CARGA
    // =====================================================================

    private suspend fun load() {
        val saved = saveCoordinator.load()
        val entity = saved.state

        statsManager.loadFromEntity(saved.statistics)

        val tutorialEnabled = entity?.tutorialEnabled ?: true
        val tutorialSteps = entity?.completedTutorialSteps?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        tutorialManager.loadProgress(tutorialSteps, tutorialEnabled)
        if (tutorialEnabled && !tutorialSteps.contains(TutorialStep.WELCOME.id)) {
            tutorialManager.triggerStep(TutorialStep.WELCOME)
        }

        val refundAmount = prestigeShopManager.calculateLegacyRefund(saved.prestigeUpgrades)
        var initialSanity = parseStoredNumber(entity?.prestigeCurrency)

        buildingManager.resetForPrestige()
        upgradeManager.resetForPrestige()
        achievementManager.resetAchievements()
        prestigeShopManager.upgrades.forEach { it.level = 0 }

        // El progreso normal se carga SIEMPRE (antes la migración de la tienda antigua
        // se saltaba esta carga y el autoguardado escribía los edificios en 0)
        if (saved.buildings.isNotEmpty()) buildingManager.loadFromSave(saved.buildings)
        if (saved.upgrades.isNotEmpty()) upgradeManager.loadFromSave(saved.upgrades)
        if (saved.achievements.isNotEmpty()) achievementManager.loadFromSave(saved.achievements)

        val hasRefund = refundAmount.signum() > 0
        if (hasRefund) {
            // Migración legacy: la tienda antigua se reembolsa en Cordura
            initialSanity = initialSanity.add(refundAmount)
        } else if (saved.prestigeUpgrades.isNotEmpty()) {
            prestigeShopManager.loadFromSave(saved.prestigeUpgrades)
        }
        prestigeManager.residualSanity = initialSanity

        if (entity != null) {
            knowledge = parseStoredNumber(entity.money)
            echoes = parseStoredNumber(entity.echoes)
            val lastTime = entity.lastLoginTime
            if (lastTime > 0) {
                val diffMillis = System.currentTimeMillis() - lastTime
                if (diffMillis > OFFLINE_THRESHOLD_MS) grantOfflineGains(diffMillis)
            }
        } else {
            saveCoordinator.createNewGame()
            knowledge = prestigeShopManager.getStartingKnowledgeBonus()
            echoes = BigDecimal.ZERO
            tutorialManager.triggerStep(TutorialStep.WELCOME)
        }

        // Una cadena de eventos abierta sigue donde quedó
        saved.pendingFollowUpEventId?.let { eventScheduler.restoreFollowUp(it, System.currentTimeMillis()) }

        // Rituales: se restauran descontando el tiempo que la app estuvo cerrada
        if (saved.rituals.isNotEmpty() && saved.ritualsSavedAtMillis > 0) {
            val elapsedSeconds = ((System.currentTimeMillis() - saved.ritualsSavedAtMillis) / 1000).coerceAtLeast(0)
            ritualManager.restore(saved.rituals, elapsedSeconds)
        }

        isLoaded = true
        recalculate()

        if (hasRefund) {
            _effects.trySend(EngineEffect.LegacyRefund(refundAmount))
            // Reemplazo: borra las filas de la tienda antigua para no reembolsarla otra vez
            requestSave(replace = true)
        }
    }

    private fun grantOfflineGains(diffMillis: Long) {
        val diffSeconds = diffMillis / 1000
        recalculate()
        // Producción base: los potenciadores temporales no se extienden al tiempo fuera
        val gains = baseProductionPerSecond
            .multiply(prestigeShopManager.getOfflinePrestigeMultiplier())
            .multiply(BigDecimal(diffSeconds))

        if (gains.signum() > 0) {
            addKnowledge(gains, isManual = false)
            statsManager.trackOfflineTime(diffSeconds)
            _effects.trySend(EngineEffect.OfflineGains(gains, diffSeconds))
            tutorialManager.triggerStep(TutorialStep.OFFLINE_GAINS)
        }
    }

    // =====================================================================
    // BUCLE
    // =====================================================================

    private suspend fun gameLoop() {
        while (true) {
            delay(TICK_MS)
            // PAUSA TOTAL: tutorial activo o app en segundo plano
            if (!isInForeground || tutorialManager.isTutorialActive()) continue

            if (!DEBUG_RUNES) runeManager.tick(TICK_MS)

            if (isAutoClicking()) applyClick(echoFound = rollEcho())

            oneSecondAccumulator += TICK_MS
            if (oneSecondAccumulator >= 1000) {
                oneSecondAccumulator -= 1000
                onSecond()
            }

            val productionTick = productionPerSecond.divide(BigDecimal.TEN)
            if (productionTick.signum() > 0) addKnowledge(productionTick, isManual = false)
            publishEconomy()
        }
    }

    private fun onSecond() {
        val now = System.currentTimeMillis()
        statsManager.trackPlayTime(1)
        statsManager.trackProductionPeak(productionPerSecond)
        ritualManager.tick(1)

        // Tutoriales basados en recursos
        if (knowledge >= BigDecimal.TEN) tutorialManager.triggerStep(TutorialStep.MENU_INTRO)
        val cheapest = buildingManager.availableBuildings.minByOrNull { it.baseCost }?.baseCost ?: BigDecimal.ZERO
        if (cheapest.signum() > 0 && knowledge >= cheapest) tutorialManager.triggerStep(TutorialStep.FIRST_BUILDING)
        if (buildingManager.availableBuildings.sumOf { it.amountOwned } >= 5) tutorialManager.triggerStep(TutorialStep.BUY_MULTIPLIERS)
        if (statsManager.getActivePlayTime() > 5) tutorialManager.triggerStep(TutorialStep.TOP_BAR_INFO)

        eventScheduler.tick(now, knowledge, prestigeManager.residualSanity, statsManager.totalPrestiges)?.let { onEventShown() }

        // Desbloqueos, logros y recálculo completo: una vez por segundo. Las acciones
        // del jugador siguen recalculando al instante.
        buildingManager.checkUnlocks(knowledge)
        val newUnlocks = achievementManager.checkAchievements(knowledge, buildingManager.availableBuildings)
        if (newUnlocks.isNotEmpty()) {
            soundManager.playSound(SoundType.ACHIEVEMENT)
            requestSave()
            _effects.trySend(EngineEffect.AchievementUnlocked(newUnlocks.last().copy()))
        }

        recalculate()
    }

    private suspend fun autoSaveLoop() {
        while (true) {
            delay(AUTOSAVE_INTERVAL_MS)
            requestSave()
        }
    }

    private fun observeRunes(scope: CoroutineScope) {
        scope.launch {
            runeManager.currentRune.collect { rune ->
                if (rune == null) return@collect
                // Prioridad de tutorial: primero la intro genérica, luego la de su tipo (en cola)
                tutorialManager.triggerStep(TutorialStep.RUNE_INTRO_GENERIC)
                when (rune.tier) {
                    RuneTier.TIER_1_SWIPE -> tutorialManager.triggerStep(TutorialStep.RUNE_TUTORIAL_SWIPE)
                    RuneTier.TIER_2_HOLD -> tutorialManager.triggerStep(TutorialStep.RUNE_TUTORIAL_HOLD)
                    RuneTier.TIER_3_TRACE -> tutorialManager.triggerStep(TutorialStep.RUNE_TUTORIAL_TRACE)
                }
            }
        }
        scope.launch {
            runeManager.activeBuffs.collect { if (isLoaded) recalculate() }
        }
        scope.launch {
            runeManager.runeRewards.collect { reward ->
                when (reward) {
                    is RuneReward.TimeWarp -> {
                        val gain = baseProductionPerSecond.multiply(BigDecimal(reward.seconds))
                        if (gain.signum() > 0) addKnowledge(gain, isManual = false)
                    }
                    is RuneReward.InstantEchoes -> {
                        val amount = BigDecimal(reward.amount)
                        echoes = echoes.add(amount)
                        statsManager.trackEchoesFound(amount)
                        soundManager.playSound(SoundType.ECHO_FOUND)
                    }
                    is RuneReward.SanityBonus -> applySanityPercent(reward.percent)
                    is RuneReward.TriggerRandomEvent -> {
                        eventScheduler.trigger(System.currentTimeMillis(), knowledge, prestigeManager.residualSanity, statsManager.totalPrestiges)
                            ?.let { onEventShown() }
                    }
                }
                publishEconomy()
            }
        }
    }

    private fun onEventShown() {
        soundManager.playSound(SoundType.EVENT_APPEAR)
        statsManager.trackEventFound()
        tutorialManager.triggerStep(TutorialStep.EVENT_APPEAR)
    }

    // =====================================================================
    // CÁLCULOS
    // =====================================================================

    private fun applyClick(echoFound: Boolean) {
        if (!isAutoClicking()) soundManager.playSound(SoundType.CLICK, 250)
        statsManager.trackClick()
        addKnowledge(currentClickPower(), isManual = true)

        if (echoFound) {
            echoes = echoes.add(BigDecimal.ONE)
            statsManager.trackEchoesFound(BigDecimal.ONE)
            soundManager.playSound(SoundType.ECHO_FOUND)
            tutorialManager.triggerStep(TutorialStep.ECHO_FOUND)
        }
    }

    private fun currentClickPower(): BigDecimal = buildingManager.getClickBuildingBonus().max(BigDecimal.ONE)

    private fun echoChance(): Double = BASE_ECHO_CHANCE + runeManager.getEchoChanceBonus()

    private fun rollEcho(): Boolean = Random.nextDouble() < echoChance()

    private fun isAutoClicking(): Boolean = runeManager.activeBuffs.value.any { it.isAutoClick }

    private fun addKnowledge(amount: BigDecimal, isManual: Boolean) {
        knowledge = knowledge.add(amount)
        statsManager.trackKnowledgeGenerated(amount, isManual)
    }

    /**
     * Cambia la Cordura en un porcentaje manteniéndola ENTERA (con BigDecimal(Double)
     * la Runa de Mente dejaba 0E-55 y ningún evento volvía a cumplir su condición).
     */
    private fun applySanityPercent(percent: Double) {
        val current = prestigeManager.residualSanity
        if (current.signum() <= 0) return
        val raw = current.multiply(BigDecimal.valueOf(percent))
        // Ganancias redondeadas hacia arriba (mínimo +1), pérdidas hacia cero
        val delta = if (percent > 0) raw.setScale(0, RoundingMode.CEILING) else raw.setScale(0, RoundingMode.DOWN)
        prestigeManager.residualSanity = current.add(delta).max(BigDecimal.ZERO)
    }

    /** Conocimiento generado en la vida actual; el saldo es el respaldo para partidas antiguas. */
    private fun knowledgeThisRun(): BigDecimal = statsManager.runKnowledge.max(knowledge)

    private fun recalculate() {
        val upgradesGlobal = upgradeManager.getGlobalMultiplier()
        val prestigeGlobal = prestigeManager.getPrestigeMultiplier()
        val achievementGlobal = achievementManager.getGlobalAchievementMultiplier()
        val shopGlobal = prestigeShopManager.getPermanentGlobalMultiplier()
        costMultiplier = runeManager.getCostReductionMultiplier()

        // Potenciadores temporales (evento + rituales + runas): se SUMAN entre sí.
        // Los permanentes se siguen multiplicando.
        val temporaryMult = BuffMath.additive(
            listOf(eventScheduler.buffMultiplier) +
                ritualManager.getActiveProductionMultipliers() +
                runeManager.getActiveProductionMultipliers()
        )
        val temporaryClickMult = BuffMath.additive(
            ritualManager.getActiveClickMultipliers() + runeManager.getActiveClickMultipliers()
        )

        globalMultiplier = upgradesGlobal
            .multiply(prestigeGlobal)
            .multiply(achievementGlobal)
            .multiply(shopGlobal)
            .multiply(temporaryMult)
            .round(MathContext.DECIMAL64)

        val shopClickBonus = prestigeShopManager.getClickPrestigeMultiplier()
        buildingManager.availableBuildings.forEachIndexed { index, building ->
            val specificMult = if (building.type == BuildingType.CLICK) {
                upgradeManager.getRawClickMultiplier()
                    .multiply(shopClickBonus)
                    .multiply(temporaryClickMult)
            } else {
                upgradeManager.getSpecificBuildingMultiplier(building.id)
                    .multiply(prestigeShopManager.getBuildingTierMultiplier(index))
            }
            building.currentMultiplier = specificMult.multiply(globalMultiplier, MathContext.DECIMAL64)
        }

        productionPerSecond = buildingManager.getTotalProductionPerSecond()
        baseProductionPerSecond = if (temporaryMult.signum() > 0) {
            productionPerSecond.divide(temporaryMult, MathContext.DECIMAL64)
        } else productionPerSecond

        potentialPrestige = prestigeManager.calculatePotentialPrestige(knowledgeThisRun())
            .multiply(prestigeShopManager.getPrestigeGainMultiplier())

        publishCollections()
        publishEconomy()

        if (potentialPrestige.signum() > 0) tutorialManager.triggerStep(TutorialStep.PRESTIGE_READY)
    }

    /**
     * Publica COPIAS: los managers mutan sus objetos en el sitio, y si la UI recibiera
     * las mismas instancias, StateFlow vería la lista "igual" y no emitiría.
     */
    private fun publishCollections() {
        _buildings.value = buildingManager.availableBuildings.filter { it.isVisible }.map { it.copy() }
        _upgrades.value = upgradeManager.availableUpgrades
            .filter { upgradeManager.isUnlocked(it, buildingManager.availableBuildings) }
            .sortedBy { it.cost }
            .map { it.copy() }

        val all = achievementManager.availableAchievements
        _achievements.value = all.filter { !it.isSecret || it.isUnlocked }.sortedBy { it.order }.map { it.copy() }
        _achievementProgress.value = AchievementProgress(
            unlocked = all.count { it.isUnlocked },
            total = all.size,
            secretsFound = all.count { it.isSecret && it.isUnlocked },
            secretsTotal = all.count { it.isSecret }
        )
        _prestigeUpgrades.value = prestigeShopManager.upgrades.map { it.copy() }
        _statistics.value = statsManager.snapshot()
    }

    private fun publishEconomy() {
        _economy.value = EconomyState(
            knowledge = knowledge,
            productionPerSecond = productionPerSecond,
            clickPower = currentClickPower(),
            sanity = prestigeManager.residualSanity,
            echoes = echoes,
            globalMultiplier = globalMultiplier,
            potentialPrestige = potentialPrestige,
            canPrestige = potentialPrestige.signum() > 0,
            buildingCostMultiplier = costMultiplier,
            echoChance = echoChance()
        )
    }

    // =====================================================================
    // GUARDADO
    // =====================================================================

    private fun requestSave(replace: Boolean = false) {
        if (!isLoaded) return
        saveCoordinator.save(
            SaveRequest(
                snapshot = buildSnapshot(),
                rituals = ritualManager.snapshot(),
                takenAtMillis = System.currentTimeMillis(),
                replace = replace,
                pendingFollowUpEventId = eventScheduler.followUpToPersist()
            )
        )
    }

    /** Foto completa del estado actual (se arma sin suspender, así es consistente). */
    private fun buildSnapshot() = GameSnapshot(
        knowledge = knowledge,
        sanity = prestigeManager.residualSanity,
        echoes = echoes,
        tutorialEnabled = tutorialManager.isTutorialEnabled.value,
        completedTutorialSteps = tutorialManager.getCompletedStepsList().joinToString(","),
        buildings = buildingManager.availableBuildings.map { BuildingEntity(it.id, it.amountOwned, it.isVisible) },
        upgrades = upgradeManager.availableUpgrades.map { UpgradeEntity(it.id, it.isBought) },
        unlockedAchievements = achievementManager.availableAchievements.filter { it.isUnlocked }.map { AchievementEntity(it.id, true) },
        prestigeUpgrades = prestigeShopManager.upgrades.map { PrestigeUpgradeEntity(it.id, it.level) },
        statistics = statsManager.getEntityForSave()
    )

    /** Lee un número guardado como texto; un valor corrupto ya no crashea la app al iniciar. */
    private fun parseStoredNumber(value: String?): BigDecimal =
        try {
            if (value.isNullOrBlank()) BigDecimal.ZERO else BigDecimal(value).max(BigDecimal.ZERO)
        } catch (e: NumberFormatException) {
            BigDecimal.ZERO
        }

    /**
     * Encola trabajo en el hilo del motor. Las acciones del jugador esperan a que la
     * partida termine de cargar: antes de eso no hay nada que comprar.
     */
    private fun post(requiresLoad: Boolean = true, block: suspend CoroutineScope.() -> Unit) {
        val engineScope = this.scope ?: return
        engineScope.launch(dispatcher) {
            if (requiresLoad && !isLoaded) return@launch
            block()
        }
    }

    companion object {
        /** Ciclo de runas cada 5 s para probarlas (lo maneja el ViewModel). */
        const val DEBUG_RUNES = false

        const val BASE_ECHO_CHANCE = 0.01
        private const val TICK_MS = 100L
        private const val AUTOSAVE_INTERVAL_MS = 5_000L
        private const val OFFLINE_THRESHOLD_MS = 10_000L
        private const val RUNE_SUCCESS_DISPLAY_MS = 2_000L
        private const val EVENT_RESULT_DISPLAY_MS = 2_500L
    }
}
