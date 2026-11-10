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
import com.EGomez.idleecosystem.domain.logic.AchievementSets
import com.EGomez.idleecosystem.domain.logic.AscensionMilestone
import com.EGomez.idleecosystem.domain.logic.Attention
import com.EGomez.idleecosystem.domain.logic.AutomationSettings
import com.EGomez.idleecosystem.domain.logic.BuffMath
import com.EGomez.idleecosystem.domain.logic.AchievementContext
import com.EGomez.idleecosystem.domain.logic.AchievementStat
import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import com.EGomez.idleecosystem.domain.logic.BuildingManager
import com.EGomez.idleecosystem.domain.logic.BuildingMilestones
import com.EGomez.idleecosystem.domain.logic.FullSaveData
import com.EGomez.idleecosystem.domain.logic.GrimoireShopping
import com.EGomez.idleecosystem.domain.logic.NextGoal
import com.EGomez.idleecosystem.domain.logic.NextGoalFinder
import com.EGomez.idleecosystem.domain.logic.PrestigeManager
import com.EGomez.idleecosystem.domain.logic.PrestigePacing
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
import com.EGomez.idleecosystem.domain.logic.SystemReveal
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
import com.EGomez.idleecosystem.domain.model.RitualState
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.TrialReward
import com.EGomez.idleecosystem.domain.model.TrialRules
import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.domain.model.PactEffects
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.RuneVariant
import com.EGomez.idleecosystem.domain.model.TutorialStep
import com.EGomez.idleecosystem.domain.model.UpgradeType
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
    /** Conocimiento generado en esta vida: el que cuenta para la Cordura. */
    val knowledgeThisRun: BigDecimal = BigDecimal.ZERO,
    val prestigePacing: PrestigePacing = PrestigePacing(),
    val buildingCostMultiplier: Double = 1.0,
    /** Pestañas y paneles que el jugador ya descubrió. */
    val revealed: SystemReveal = SystemReveal(),
    /** Ascensiones hechas en toda la partida: deciden los hitos de Ascensión. */
    val ascensions: Int = 0,
    /** El pacto de esta vida y, con Azathoth, el multiplicador de caos actual. */
    val activePact: Pact = Pact.NONE,
    val chaosMultiplier: Int = 1,
    /** Atención repartida (ya ajustada a los puntos disponibles) y puntos totales. */
    val attention: Map<String, Int> = emptyMap(),
    val attentionPoints: Int = 0,
    val echoChance: Double = GameEngine.BASE_ECHO_CHANCE,
    /** Pruebas de Locura: la de esta vida, su avance (0..1) y las superadas. */
    val activeTrial: Trial? = null,
    val trialProgress: Float = 0f,
    val completedTrials: Set<Trial> = emptySet(),
    /** Delirio: el multiplicador de producción actual. */
    val deliriumMultiplier: Double = 1.0,
    /** Dioses que las Pruebas superadas permiten elegir antes de tiempo. */
    val earlyPacts: Set<Pact> = emptySet()
)

data class AchievementProgress(
    val unlocked: Int = 0,
    val total: Int = 0,
    val secretsFound: Int = 0,
    val secretsTotal: Int = 0,
    /** Conjuntos de logros completos (cada uno duplica su edificio) y cuántos hay. */
    val setsCompleted: Int = 0,
    val setsTotal: Int = 0
)

/** Avisos de una sola vez (diálogos y popups), que no forman parte del estado. */
sealed interface EngineEffect {
    data class OfflineGains(val gains: BigDecimal, val seconds: Long) : EngineEffect
    data class LegacyRefund(val amount: BigDecimal) : EngineEffect
    data class AchievementUnlocked(val achievement: Achievement) : EngineEffect
    /** La automatización ascendió sola y ganó [sanity] de Cordura. */
    data class AutoAscended(val sanity: BigDecimal) : EngineEffect
    /** Se superó una Prueba de Locura. */
    data class TrialCompleted(val trial: Trial) : EngineEffect
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
    private var prestigePacing = PrestigePacing()
    private var automation = AutomationSettings()
    private var activePact = Pact.NONE
    /** El reparto que eligió el jugador; se ajusta a los puntos al usarlo. */
    private var wantedAttention: Map<String, Int> = emptyMap()
    private var chaosMultiplier = 1
    /** Delirio (Prueba): multiplicador de producción que cambia cada 30 s. */
    private var deliriumMultiplier = 1.0
    private var deliriumSeconds = 0
    private val pact: PactEffects get() = activePact.effects
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

    /** El logro pendiente más avanzado, o null si ya no quedan. */
    private val _nextGoal = MutableStateFlow<NextGoal?>(null)
    val nextGoal: StateFlow<NextGoal?> = _nextGoal.asStateFlow()

    /** Hitos de cada edificio (escalera del Grimorio y logros); no cambian en la partida. */
    val buildingMilestones: Map<String, List<Int>> = BuildingMilestones.build(
        buildingManager.availableBuildings.map { it.id },
        achievementManager.availableAchievements
    )

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

    fun buyBuilding(buildingId: String, requested: Int) = post {
        val amount = if (requested == BuildingMilestones.BUY_TO_MILESTONE) {
            val owned = buildingManager.availableBuildings.find { it.id == buildingId }?.amountOwned ?: return@post
            BuildingMilestones.amountToNext(buildingMilestones[buildingId].orEmpty(), owned)
        } else requested
        if (!canOwnBuilding(buildingId)) return@post
        val discount = BigDecimal.valueOf(buildingCostFactor())
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
        if (trialRules().noGrimoire) return@post
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
        if (trialRules().noGrimoire) return@post
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
            if (MadnessPaths.isPath(id)) statsManager.flag(AchievementStat.walked(id))
            recalculate()
            requestSave()
        }
    }

    fun activateRitual(ritualId: String) = post {
        if (performRitual(ritualId)) soundManager.playSound(SoundType.BUY)
    }

    /** Reparte la Atención. Lo que no alcance con los puntos actuales se ignora. */
    fun setAttention(allocation: Map<String, Int>) = post {
        wantedAttention = allocation
        applyPactModifiers()
        recalculate()
    }

    /** Elige qué automatizaciones están encendidas (solo actúan con su hito alcanzado). */
    fun setAutomation(settings: AutomationSettings) = post { automation = settings }

    private fun performRitual(ritualId: String): Boolean {
        if (trialRules().noRituals) return false
        if (ritualManager.tryActivateRitual(ritualId, echoes)) {
            val ritual = ritualManager.rituals.value.find { it.id == ritualId } ?: return false
            echoes = echoes.subtract(ritual.cost)
            statsManager.count(AchievementStat.RITUALS)
            statsManager.count(AchievementStat.ritual(ritualId))
            statsManager.count(StatisticsManager.RUN_RITUALS)
            if (ritual.effectType == RitualEffectType.INSTANT_KNOWLEDGE) {
                // Producción base, igual que los saltos de tiempo de las runas: los
                // potenciadores activos no se multiplican sobre este regalo
                val gain = baseProductionPerSecond.multiply(BigDecimal.valueOf(ritual.effectValue))
                if (gain.signum() > 0) addKnowledge(gain, isManual = false)
            }
            recalculate()
            return true
        }
        return false
    }

    /**
     * Las automatizaciones de los hitos de Ascensión, una vez por segundo. Los
     * autocompradores gastan como máximo la mitad del Conocimiento, para dejar margen
     * a las decisiones del jugador, y compran de a una unidad: empiezan lentos.
     */
    private fun runAutomation() {
        val ascensions = statsManager.totalPrestiges
        var changed = false
        if (automation.buildings && AscensionMilestone.BUILDING_AUTOBUYER.isReached(ascensions)) {
            changed = autoBuyBuildings() || changed
        }
        if (automation.grimoire && AscensionMilestone.GRIMOIRE_AUTOBUYER.isReached(ascensions)) {
            changed = autoBuyUpgrades() || changed
        }
        val ritualId = automation.ritualId
        if (ritualId != null && AscensionMilestone.AUTO_RITUAL.isReached(ascensions)) {
            val ready = ritualManager.rituals.value.find { it.id == ritualId }?.state == RitualState.READY
            if (ready) changed = performRitual(ritualId) || changed
        }
        if (changed) recalculate()

        // Ascensión automática: al duplicar la Cordura (desde el hito del Grimorio)
        if (automation.autoAscend && AscensionMilestone.GRIMOIRE_AUTOBUYER.isReached(ascensions) &&
            AutomationSettings.shouldAutoAscend(potentialPrestige, prestigeManager.residualSanity)
        ) {
            performAscension(activePact)?.let {
                statsManager.count(AchievementStat.AUTO_ASCENSIONS)
                _effects.trySend(EngineEffect.AutoAscended(it))
            }
            return
        }
        if (changed) requestSave()
    }

    /** Una unidad de cada edificio que entre en el presupuesto, del más caro al más barato. */
    private fun autoBuyBuildings(): Boolean {
        var budget = knowledge.multiply(BigDecimal.valueOf(automation.budgetFraction))
        val discount = BigDecimal.valueOf(buildingCostFactor())
        var bought = false
        val candidates = buildingManager.availableBuildings
            .filter { it.isVisible && it.id !in automation.excludedBuildings }
            .reversed()
        for (building in candidates) {
            if (!canOwnBuilding(building.id)) continue
            val cost = buildingManager.getCost(building.id, 1, knowledge, discount)
            if (cost.signum() <= 0 || cost > budget) continue
            if (buildingManager.tryBuyBuilding(building.id, knowledge, 1, discount) > 0) {
                knowledge = knowledge.subtract(cost)
                budget = budget.subtract(cost)
                bought = true
                statsManager.count(AchievementStat.AUTO_UNITS)
            }
        }
        return bought
    }

    /** Las mejoras del Grimorio que entren en el presupuesto, de la más barata a la más cara. */
    private fun autoBuyUpgrades(): Boolean {
        if (trialRules().noGrimoire) return false
        val budget = knowledge.multiply(BigDecimal.valueOf(automation.budgetFraction))
        val unlocked = upgradeManager.availableUpgrades
            .filter { upgradeManager.isUnlocked(it, buildingManager.availableBuildings) }
        var bought = false
        for (upgrade in GrimoireShopping.plan(unlocked, budget)) {
            if (upgradeManager.tryBuyUpgrade(upgrade.id, knowledge)) {
                knowledge = knowledge.subtract(upgrade.cost)
                bought = true
            }
        }
        return bought
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

    /**
     * Asciende y empieza la vida nueva con [nextPact] (el actual si no se elige otro).
     * Con [abandonPath] se deja la Senda del Mapa de Locura y se recupera su Cordura.
     */
    fun ascend(nextPact: Pact? = null, abandonPath: Boolean = false, nextTrial: Trial? = null) = post {
        if (performAscension(nextPact ?: activePact, abandonPath, nextTrial) != null) soundManager.playSound(SoundType.BUY)
    }

    /** Asciende y devuelve la Cordura ganada, o null si todavía no se puede. */
    private fun performAscension(nextPact: Pact, abandonPath: Boolean = false, nextTrial: Trial? = null): BigDecimal? {
        val finalPotential = prestigeManager.calculatePotentialPrestige(knowledgeThisRun())
            .multiply(sanityGainMultiplier())
        if (finalPotential.signum() <= 0) return null

        // Logros: la vida que termina (pacto, Senda, hazañas) antes de reiniciarla
        recordFinishedLife(finalPotential, abandonPath)
        prestigeManager.addPrestige(finalPotential)
        statsManager.trackPrestige(finalPotential)
        statsManager.resetRunCounters()
        deliriumMultiplier = 1.0
        // La Prueba de la vida nueva (una bloqueada no se puede elegir)
        if (nextTrial != null && nextTrial.isUnlocked(statsManager.totalPrestiges)) startTrial(nextTrial)
        // Cambiar de Senda solo se puede al Ascender: devuelve todo lo gastado en ella
        if (abandonPath) {
            prestigeManager.residualSanity = prestigeManager.residualSanity.add(prestigeShopManager.abandonPath())
        }

        // Hitos de Ascensión: lo que la nueva vida conserva o trae de entrada
        val ascensions = statsManager.totalPrestiges
        // El pacto de la vida nueva (uno bloqueado no se puede elegir)
        setPact(if (nextPact.isUnlocked(ascensions, earlyPacts())) nextPact else Pact.NONE)
        buildingManager.resetForPrestige()
        upgradeManager.resetForPrestige(keep = { AscensionMilestone.keepsOnAscension(it, ascensions) })
        if (AscensionMilestone.STARTING_SYNAPSES.isReached(ascensions)) {
            buildingManager.availableBuildings.firstOrNull { it.type == BuildingType.CLICK }
                ?.amountOwned = AscensionMilestone.SYNAPSES_AT_START
        }
        knowledge = prestigeShopManager.getStartingKnowledgeBonus()
        eventScheduler.clearBuff()

        recalculate()
        // Reemplazo completo: no quedan filas de edificios o mejoras de la vida anterior
        requestSave(replace = true)
        return finalPotential
    }

    /** Cuenta para los logros la vida que termina: con qué pacto y Senda, y sus hazañas. */
    private fun recordFinishedLife(sanityGained: BigDecimal, abandonPath: Boolean) {
        val path = MadnessPaths.active(prestigeShopManager.upgrades)
        statsManager.count(AchievementStat.life(activePact.id))
        if (path != null) {
            statsManager.count(AchievementStat.lifeOnPath(path.id))
            statsManager.count(AchievementStat.lifeWith(activePact.id, path.id))
            if (abandonPath) {
                statsManager.count(AchievementStat.PATHS_ABANDONED)
                if (path.level >= LONG_PATH_LEVEL) statsManager.flag(AchievementStat.FEAT_LONG_PATH)
            }
        }
        statsManager.record(AchievementStat.MAX_SANITY_GAIN, sanityGained)

        val runSeconds = statsManager.runTimeSeconds
        if (runSeconds < FAST_LIFE_SECONDS) statsManager.flag(AchievementStat.FEAT_FAST_15)
        if (runSeconds < FASTEST_LIFE_SECONDS) statsManager.flag(AchievementStat.FEAT_FAST_5)
        if (statsManager.counter(StatisticsManager.RUN_CLICKS).signum() == 0) statsManager.flag(AchievementStat.FEAT_SILENT_LIFE)
        if (upgradeManager.availableUpgrades.none { it.isBought }) statsManager.flag(AchievementStat.FEAT_ASCETIC)
        if (statsManager.counter(StatisticsManager.RUN_RUNES).signum() == 0 && knowledgeThisRun() >= BLIND_LIFE_KNOWLEDGE) {
            statsManager.flag(AchievementStat.FEAT_BLIND)
        }
        val points = attentionPoints()
        if (points >= BLANK_MIND_POINTS && Attention.used(effectiveAttention()) == 0) statsManager.flag(AchievementStat.FEAT_BLANK_MIND)
    }

    /** Las claves de los logros STAT: lo que se calcula al momento y los contadores guardados. */
    private fun achievementContext(): AchievementContext {
        val stats = HashMap<String, BigDecimal>(statsManager.counters)
        fun put(key: String, value: Number) {
            stats[key] = if (value is BigDecimal) value else BigDecimal(value.toString())
        }

        val ascensions = statsManager.totalPrestiges
        put(AchievementStat.ASCENSIONS, ascensions)
        put(AchievementStat.SANITY, prestigeManager.residualSanity)
        put(AchievementStat.LIFETIME_SANITY, statsManager.lifetimeSanity)
        val upgrades = upgradeManager.availableUpgrades
        put(AchievementStat.SYNERGIES, upgrades.count { it.type == UpgradeType.SYNERGY && it.isBought })
        put(AchievementStat.GRIMOIRE, upgrades.count { it.isBought })
        put(AchievementStat.SETS, AchievementSets.completed(achievementManager.availableAchievements).size)

        val shop = prestigeShopManager.upgrades
        val baseNodes = shop.filter { !MadnessPaths.isPath(it.id) }
        put(AchievementStat.MAP_LEVELS, shop.sumOf { it.level })
        put(AchievementStat.MAP_NODES, baseNodes.count { it.level > 0 })
        put(AchievementStat.MAP_MAX_LEVEL, shop.maxOfOrNull { it.level } ?: 0)
        put(AchievementStat.MAP_STAR, shop.firstOrNull { it.id == "pu_star_final" }?.level ?: 0)
        val path = MadnessPaths.active(shop)
        put(AchievementStat.PATH_LEVEL, path?.level ?: 0)
        put(AchievementStat.PATHS_WALKED, MadnessPaths.IDS.count { statsManager.counter(AchievementStat.walked(it)).signum() > 0 })

        val attention = effectiveAttention()
        put(AchievementStat.ATTENTION_POINTS, attentionPoints())
        put(AchievementStat.TRIALS_COMPLETED, completedTrials().size)
        put(AchievementStat.ATTENTION_USED, Attention.used(attention))
        put(AchievementStat.ATTENTION_DREAM, attention[Attention.DREAM] ?: 0)
        put(AchievementStat.ATTENTION_VIGIL, attention[Attention.VIGIL] ?: 0)
        put(AchievementStat.ATTENTION_FOCUS, attention.values.maxOrNull() ?: 0)
        put(AchievementStat.ATTENTION_TARGETS, attention.count { it.value > 0 })

        put(AchievementStat.PACTS_UNLOCKED, Pact.available(ascensions).count { it != Pact.NONE })
        put(AchievementStat.PACTS_LIVED, Pact.entries.count { statsManager.counter(AchievementStat.life(it.id)).signum() > 0 })

        put(AchievementStat.CLICKS, statsManager.totalClicks)
        put(AchievementStat.RUNES_T1, statsManager.runesSevered)
        put(AchievementStat.RUNES_T2, statsManager.runesStabilized)
        put(AchievementStat.RUNES_T3, statsManager.runesInscribed)
        put(AchievementStat.RUNES, statsManager.runesSevered + statsManager.runesStabilized + statsManager.runesInscribed)
        put(AchievementStat.EVENTS, statsManager.totalEventsFound)
        put(AchievementStat.ECHOES_FOUND, statsManager.totalEchoes)
        put(AchievementStat.ECHOES_HELD, echoes)
        put(AchievementStat.PLAY_TIME, statsManager.totalPlayTimeSeconds)
        put(AchievementStat.OFFLINE_TIME, statsManager.totalOfflineSeconds)
        put(AchievementStat.KPS_MAX, statsManager.maxKnowledgePerSecond)
        put(AchievementStat.RUN_KNOWLEDGE, knowledgeThisRun())
        put(AchievementStat.CHAOS, if (pact.chaos) chaosMultiplier else 0)
        put(AchievementStat.ACTIVE_RITUALS, ritualManager.rituals.value.count { it.state == RitualState.ACTIVE })

        val buildings = buildingManager.availableBuildings
        put(AchievementStat.BUILDINGS_TOTAL, buildings.sumOf { it.amountOwned })
        put(AchievementStat.BUILDING_TYPES, buildings.count { it.amountOwned > 0 })
        buildings.forEach { put(AchievementStat.owned(it.id), it.amountOwned) }

        return AchievementContext(stats, activePact.id, path?.id)
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
        setPact(Pact.NONE)

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
        setPact(Pact.fromId(data.activePact))

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
            tutorialManager.getCompletedStepsList(),
            activePact.id
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
        // Una pestaña sin descubrir muestra su requisito, no su tutorial
        val revealed = currentReveal()
        if (index == 1 && revealed.grimoire) tutorialManager.triggerStep(TutorialStep.UPGRADES_TAB)
        if (index == 2 && revealed.rituals && echoes.signum() > 0) tutorialManager.triggerStep(TutorialStep.RITUALS_TAB)
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

        // El pacto de esta vida (las partidas anteriores a la v0.16 no tienen: Sin pacto)
        setPact(Pact.fromId(entity?.activePact))

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
            .multiply(BigDecimal.valueOf(pact.offline))
            .multiply(BigDecimal.valueOf(attentionMultiplier(Attention.DREAM)))
            .multiply(BigDecimal.valueOf(trialReward().offline * if (trialRules().noOffline) 0.0 else 1.0))
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
        rollChaos()
        checkTrial()

        // Tutoriales basados en recursos
        if (knowledge >= BigDecimal.TEN) tutorialManager.triggerStep(TutorialStep.MENU_INTRO)
        val cheapest = buildingManager.availableBuildings.minByOrNull { it.baseCost }?.baseCost ?: BigDecimal.ZERO
        if (cheapest.signum() > 0 && knowledge >= cheapest) tutorialManager.triggerStep(TutorialStep.FIRST_BUILDING)
        if (buildingManager.availableBuildings.sumOf { it.amountOwned } >= 5) tutorialManager.triggerStep(TutorialStep.BUY_MULTIPLIERS)
        if (statsManager.getActivePlayTime() > 5) tutorialManager.triggerStep(TutorialStep.TOP_BAR_INFO)
        triggerDepthTutorials()
        runAutomation()

        eventScheduler.tick(now, knowledge, prestigeManager.residualSanity, statsManager.totalPrestiges)?.let { onEventShown() }

        // Desbloqueos, logros y recálculo completo: una vez por segundo. Las acciones
        // del jugador siguen recalculando al instante.
        buildingManager.checkUnlocks(knowledge)
        val newUnlocks = achievementManager.checkAchievements(knowledge, buildingManager.availableBuildings, achievementContext())
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
                        if (!trialRules().noRituals) echoes = echoes.add(amount)
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
        val rules = trialRules()
        // Manos Atadas: el clic no produce
        if (!rules.noClickKnowledge) addKnowledge(currentClickPower(), isManual = true)

        // Silencio: no hay Ecos
        if (echoFound && !rules.noRituals) {
            echoes = echoes.add(BigDecimal.ONE)
            statsManager.trackEchoesFound(BigDecimal.ONE)
            soundManager.playSound(SoundType.ECHO_FOUND)
            tutorialManager.triggerStep(TutorialStep.ECHO_FOUND)
        }
    }

    private fun currentClickPower(): BigDecimal = buildingManager.getClickBuildingBonus().max(BigDecimal.ONE)

    /** Tutoriales de las mecánicas de la v0.14, cada uno cuando su sistema entra en juego. */
    private fun triggerDepthTutorials() {
        if (statsManager.totalClicks >= HOLD_TUTORIAL_CLICKS) tutorialManager.triggerStep(TutorialStep.HOLD_TO_CLICK)
        val achievements = achievementManager.availableAchievements
        if (achievements.any { it.isUnlocked && !it.isSecret }) tutorialManager.triggerStep(TutorialStep.NEXT_GOAL)
        if (AchievementSets.byBuilding(achievements).values.any { set -> set.count { it.isUnlocked } >= 2 }) {
            tutorialManager.triggerStep(TutorialStep.ACHIEVEMENT_SETS)
        }
        val buildings = buildingManager.availableBuildings
        val synergyAvailable = upgradeManager.availableUpgrades.any {
            it.type == UpgradeType.SYNERGY && !it.isBought && upgradeManager.isUnlocked(it, buildings)
        }
        if (synergyAvailable) tutorialManager.triggerStep(TutorialStep.SYNERGIES)
        val ascensions = statsManager.totalPrestiges
        if (ascensions >= 1) tutorialManager.triggerStep(TutorialStep.ASCENSION_MILESTONES)
        if (AscensionMilestone.BUILDING_AUTOBUYER.isReached(ascensions)) tutorialManager.triggerStep(TutorialStep.AUTOMATION)
        // Los pactos se eligen al Ascender: se presentan cuando la Ascensión está a mano
        if (potentialPrestige.signum() > 0) tutorialManager.triggerStep(TutorialStep.PACTS)
        if (Attention.available(ascensions) > 0) tutorialManager.triggerStep(TutorialStep.ATTENTION)
        // Las Sendas, cuando alguna ya se puede comprar
        val shop = prestigeShopManager.upgrades
        val pathOpen = shop.any { node ->
            MadnessPaths.isPath(node.id) && shop.any { it.id == node.parentId && it.level > 0 }
        }
        if (pathOpen) tutorialManager.triggerStep(TutorialStep.PATHS)
        // Las Pruebas se eligen al Ascender: se presentan cuando la próxima ya puede tener una
        if (ascensions + 1 >= Trial.REVEAL_AT) tutorialManager.triggerStep(TutorialStep.TRIALS)
        val convergence = ritualManager.rituals.value.firstOrNull { it.id == RitualManager.CONVERGENCE_ID }
        if (convergence != null && echoes >= convergence.cost) tutorialManager.triggerStep(TutorialStep.CONVERGENCE)
    }

    private fun currentReveal(): SystemReveal = SystemReveal.compute(
        lifetimeKnowledge = statsManager.lifetimeKnowledge,
        anyUpgradeBought = upgradeManager.availableUpgrades.any { it.isBought },
        lifetimeEchoes = statsManager.totalEchoes,
        echoes = echoes,
        canAscend = potentialPrestige.signum() > 0,
        ascensions = statsManager.totalPrestiges,
        sanity = prestigeManager.residualSanity
    )

    private fun echoChance(): Double =
        if (trialRules().noRituals) 0.0 else (BASE_ECHO_CHANCE + runeManager.getEchoChanceBonus()) * pact.echoChance

    // =====================================================================
    // PACTOS
    // =====================================================================

    /** Cambia el pacto de la vida actual y aplica sus reglas a runas, eventos y rituales. */
    // =====================================================================
    // PRUEBAS DE LOCURA (v0.17.1)
    // La Prueba activa se guarda como contador de la vida ("run.trial.<id>"): se borra
    // sola al Ascender. Las superadas, como "trial.<id>".
    // =====================================================================

    private fun activeTrial(): Trial? = statsManager.counters.keys
        .firstOrNull { it.startsWith(AchievementStat.ACTIVE_TRIAL_PREFIX) }
        ?.let { Trial.fromId(it.removePrefix(AchievementStat.ACTIVE_TRIAL_PREFIX)) }

    private fun trialRules(): TrialRules = activeTrial()?.rules ?: NO_TRIAL_RULES

    private fun completedTrials(): Set<Trial> =
        Trial.entries.filter { statsManager.counter(AchievementStat.trial(it.id)).signum() > 0 }.toSet()

    private fun trialReward(): TrialReward = Trial.combinedReward(completedTrials())

    private fun earlyPacts(): Set<Pact> = Trial.earlyPacts(completedTrials())

    /** Puntos de Atención: los de las Ascensiones y, desde la primera, los de las Pruebas. */
    private fun attentionPoints(): Int {
        val base = Attention.available(statsManager.totalPrestiges)
        return if (base > 0) base + trialReward().attentionPoints else 0
    }

    private fun startTrial(trial: Trial) {
        statsManager.flag(AchievementStat.ACTIVE_TRIAL_PREFIX + trial.id)
        statsManager.count(AchievementStat.TRIALS_STARTED)
        deliriumMultiplier = 1.0
        deliriumSeconds = 0
    }

    /** Abandona la Prueba de esta vida: sus reglas se levantan, sin recompensa. */
    fun abandonTrial() = post {
        val trial = activeTrial() ?: return@post
        statsManager.counters.remove(AchievementStat.ACTIVE_TRIAL_PREFIX + trial.id)
        deliriumMultiplier = 1.0
        applyPactModifiers()
        recalculate()
        requestSave()
    }

    /** Una vez por segundo: la Prueba se supera al llegar a su meta en esta vida. */
    private fun checkTrial() {
        val trial = activeTrial() ?: return
        if (trial.rules.delirium) rollDelirium()
        if (knowledgeThisRun() < trial.goal) return
        statsManager.counters.remove(AchievementStat.ACTIVE_TRIAL_PREFIX + trial.id)
        statsManager.flag(AchievementStat.trial(trial.id))
        statsManager.flag(AchievementStat.trialWith(trial.id, activePact.id))
        if (statsManager.runTimeSeconds < FAST_TRIAL_SECONDS) statsManager.flag(AchievementStat.FEAT_TRIAL_FAST)
        deliriumMultiplier = 1.0
        applyPactModifiers()
        soundManager.playSound(SoundType.ACHIEVEMENT)
        _effects.trySend(EngineEffect.TrialCompleted(trial))
        recalculate()
        requestSave()
    }

    private fun rollDelirium() {
        deliriumSeconds++
        if (deliriumSeconds % Trial.DELIRIUM_PERIOD_SECONDS != 0) return
        deliriumMultiplier = Trial.DELIRIUM_STEPS.random()
        recalculate()
    }

    /** Monomanía: un edificio de un tipo nuevo solo si no se pasa del límite. */
    private fun canOwnBuilding(buildingId: String): Boolean {
        val limit = trialRules().maxBuildingTypes ?: return true
        val buildings = buildingManager.availableBuildings
        val target = buildings.find { it.id == buildingId } ?: return false
        if (target.type == BuildingType.CLICK || target.amountOwned > 0) return true
        return buildings.count { it.type != BuildingType.CLICK && it.amountOwned > 0 } < limit
    }

    private fun setPact(newPact: Pact) {
        activePact = newPact
        chaosMultiplier = 1
        applyPactModifiers()
    }

    private fun effectiveAttention(): Map<String, Int> =
        Attention.clamp(wantedAttention, attentionPoints())

    private fun attentionMultiplier(target: String): Double =
        Attention.multiplier(effectiveAttention()[target] ?: 0)

    private fun applyPactModifiers() {
        // Las runas: el pacto y la Atención puesta en la Vigilia
        val reward = trialReward()
        runeManager.spawnFrequency = if (trialRules().noRunes) 0.0
        else pact.runeFrequency * attentionMultiplier(Attention.VIGIL) * reward.runeFrequency
        eventScheduler.frequencyMultiplier = pact.eventFrequency
        eventScheduler.buffPower = pact.eventBuffPower
        ritualManager.applyPact(pact.ritualCost, pact.ritualDuration, pact.ritualCooldown * reward.ritualCooldown)
    }

    /** Azathoth: la producción cambia al azar cada [PactEffects.CHAOS_PERIOD_SECONDS]. */
    private fun rollChaos() {
        if (!pact.chaos) return
        chaosSeconds++
        if (chaosSeconds % PactEffects.CHAOS_PERIOD_SECONDS != 0) return
        chaosMultiplier = Random.nextInt(PactEffects.CHAOS_MIN, PactEffects.CHAOS_MAX + 1)
        if (chaosMultiplier == PactEffects.CHAOS_MAX) statsManager.count(AchievementStat.FEAT_CHAOS_20)
        if (chaosMultiplier == PactEffects.CHAOS_MIN) statsManager.count(AchievementStat.FEAT_CHAOS_1)
        recalculate()
    }

    private var chaosSeconds = 0

    /** El factor de costo de los edificios: Runa de Barrera y pacto. */
    private fun buildingCostFactor(): Double =
        runeManager.getCostReductionMultiplier() * pact.buildingCost * trialRules().buildingCost * trialReward().buildingCost

    /** Lo que multiplica la Cordura ganada al Ascender: Mapa de Locura y pacto de esta vida. */
    private fun sanityGainMultiplier(): BigDecimal =
        prestigeShopManager.getPrestigeGainMultiplier().multiply(BigDecimal.valueOf(pact.sanityGain * trialReward().sanityGain))

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
        // Pruebas: Mortalidad apaga la Cordura y el Mapa; Amnesia, los logros
        val rules = trialRules()
        val reward = trialReward()
        val prestigeGlobal = if (rules.noSanityBonus) BigDecimal.ONE else prestigeManager.getPrestigeMultiplier()
        val achievementGlobal = if (rules.noAchievementBonus) BigDecimal.ONE else achievementManager.getGlobalAchievementMultiplier()
        val shopGlobal = if (rules.noSanityBonus) BigDecimal.ONE else prestigeShopManager.getPermanentGlobalMultiplier()
        costMultiplier = buildingCostFactor()
        val pactProduction = BigDecimal.valueOf(pact.production * if (pact.chaos) chaosMultiplier else 1)
        val trialProduction = BigDecimal.valueOf(rules.production * reward.production * deliriumMultiplier)

        // Potenciadores temporales (evento + rituales + runas): se SUMAN entre sí.
        // Los permanentes se siguen multiplicando.
        // Con el ritual Convergencia activo se MULTIPLICAN (con tope).
        val convergence = ritualManager.isConvergenceActive()
        val temporaryBuffs = listOf(eventScheduler.buffMultiplier) +
            ritualManager.getActiveProductionMultipliers() +
            runeManager.getActiveProductionMultipliers()
        val temporaryMult = BuffMath.combine(temporaryBuffs, convergence)
        if (convergence) {
            // Logros de la Convergencia: el mejor combo logrado
            statsManager.record(AchievementStat.MAX_CONVERGENCE, temporaryMult)
            statsManager.record(AchievementStat.MAX_CONVERGENCE_BUFFS, BigDecimal(temporaryBuffs.count { it > 1.0 }))
        }
        val temporaryClickMult = BuffMath.combine(
            ritualManager.getActiveClickMultipliers() + runeManager.getActiveClickMultipliers(),
            convergence
        )

        globalMultiplier = upgradesGlobal
            .multiply(prestigeGlobal)
            .multiply(achievementGlobal)
            .multiply(shopGlobal)
            .multiply(pactProduction)
            .multiply(trialProduction)
            .multiply(temporaryMult)
            .round(MathContext.DECIMAL64)

        val shopClickBonus = if (rules.noSanityBonus) BigDecimal.ONE else prestigeShopManager.getClickPrestigeMultiplier()
        val completedSets = if (rules.noAchievementBonus) emptySet() else AchievementSets.completed(achievementManager.availableAchievements)
        val attention = effectiveAttention()
        val buildings = buildingManager.availableBuildings
        buildings.forEachIndexed { index, building ->
            val setMult = AchievementSets.multiplier(building.id, completedSets)
                .multiply(BigDecimal.valueOf(Attention.multiplier(attention[building.id] ?: 0)))
            val specificMult = if (building.type == BuildingType.CLICK) {
                upgradeManager.getRawClickMultiplier()
                    .multiply(shopClickBonus)
                    .multiply(temporaryClickMult)
                    .multiply(setMult)
                    .multiply(BigDecimal.valueOf(pact.click * reward.click))
            } else {
                upgradeManager.getSpecificBuildingMultiplier(building.id)
                    .multiply(upgradeManager.getSynergyMultiplier(building.id, buildings))
                    .multiply(if (rules.noSanityBonus) BigDecimal.ONE else prestigeShopManager.getBuildingTierMultiplier(index))
                    .multiply(setMult)
                    .multiply(BigDecimal.valueOf(pact.buildingMultiplier(building.id)))
            }
            building.currentMultiplier = specificMult.multiply(globalMultiplier, MathContext.DECIMAL64)
        }

        productionPerSecond = buildingManager.getTotalProductionPerSecond()
        baseProductionPerSecond = if (temporaryMult.signum() > 0) {
            productionPerSecond.divide(temporaryMult, MathContext.DECIMAL64)
        } else productionPerSecond

        val gainMultiplier = sanityGainMultiplier()
        potentialPrestige = prestigeManager.calculatePotentialPrestige(knowledgeThisRun()).multiply(gainMultiplier)
        // Una vez por segundo basta: solo cambia al cruzar un punto de Cordura
        prestigePacing = prestigeManager.pacing(knowledgeThisRun(), gainMultiplier)

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
            secretsTotal = all.count { it.isSecret },
            setsCompleted = AchievementSets.completed(all).size,
            setsTotal = AchievementSets.byBuilding(all).size
        )
        _nextGoal.value = NextGoalFinder.find(all, knowledge, buildingManager.availableBuildings, achievementContext())
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
            knowledgeThisRun = knowledgeThisRun(),
            prestigePacing = prestigePacing,
            buildingCostMultiplier = costMultiplier,
            revealed = currentReveal(),
            ascensions = statsManager.totalPrestiges,
            activePact = activePact,
            chaosMultiplier = chaosMultiplier,
            attention = effectiveAttention(),
            attentionPoints = attentionPoints(),
            activeTrial = activeTrial(),
            trialProgress = activeTrial()?.let { t ->
                knowledgeThisRun().divide(t.goal, MathContext.DECIMAL64).toFloat().coerceIn(0f, 1f)
            } ?: 0f,
            completedTrials = completedTrials(),
            deliriumMultiplier = deliriumMultiplier,
            earlyPacts = earlyPacts(),
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
        statistics = statsManager.getEntityForSave(),
        activePact = activePact.id
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
        /** Clics manuales tras los que se enseña a mantener pulsado. */
        private const val HOLD_TUTORIAL_CLICKS = 25L
        private const val TICK_MS = 100L
        private const val AUTOSAVE_INTERVAL_MS = 5_000L
        private const val OFFLINE_THRESHOLD_MS = 10_000L
        private const val RUNE_SUCCESS_DISPLAY_MS = 2_000L
        private const val EVENT_RESULT_DISPLAY_MS = 2_500L

        // Hazañas de los logros (v0.17)
        private const val LONG_PATH_LEVEL = 5
        private const val FAST_LIFE_SECONDS = 15 * 60L
        private const val FASTEST_LIFE_SECONDS = 5 * 60L
        private val BLIND_LIFE_KNOWLEDGE = BigDecimal("1000000000")
        private const val BLANK_MIND_POINTS = 4
        private const val FAST_TRIAL_SECONDS = 20 * 60L
        private val NO_TRIAL_RULES = TrialRules()
    }
}
