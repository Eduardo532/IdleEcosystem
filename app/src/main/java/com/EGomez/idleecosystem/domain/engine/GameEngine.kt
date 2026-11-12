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
import com.EGomez.idleecosystem.domain.logic.MenuTab
import com.EGomez.idleecosystem.domain.logic.MenuUnlocks
import com.EGomez.idleecosystem.domain.logic.AscensionMilestone
import com.EGomez.idleecosystem.domain.logic.Attention
import com.EGomez.idleecosystem.domain.logic.AutomationSettings
import com.EGomez.idleecosystem.domain.logic.BuffMath
import com.EGomez.idleecosystem.domain.logic.Consecration
import com.EGomez.idleecosystem.domain.logic.EternalUpgrade
import com.EGomez.idleecosystem.domain.logic.Transcendence
import com.EGomez.idleecosystem.domain.logic.TranscendenceMilestone
import com.EGomez.idleecosystem.domain.logic.Veil
import com.EGomez.idleecosystem.domain.logic.StarUpgrade
import com.EGomez.idleecosystem.domain.logic.Stars
import com.EGomez.idleecosystem.domain.logic.Beast
import com.EGomez.idleecosystem.domain.logic.BeastKind
import com.EGomez.idleecosystem.domain.logic.BeastRarity
import com.EGomez.idleecosystem.domain.logic.Bestiary
import com.EGomez.idleecosystem.domain.logic.MadnessMeter
import com.EGomez.idleecosystem.domain.logic.RuneEngraving
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
import com.EGomez.idleecosystem.domain.model.Mutation
import com.EGomez.idleecosystem.domain.model.TidePhase
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
    val earlyPacts: Set<Pact> = emptySet(),
    /** Trascender: si ya se ve, Ascensiones del ciclo, Éter que daría y Éter disponible. */
    val transcendRevealed: Boolean = false,
    val cycleAscensions: Int = 0,
    val etherGain: Int = 0,
    val ether: Int = 0,
    val transcendences: Int = 0,
    val milestoneAscensions: Int = 0,
    val transcendRequired: Int = Transcendence.REQUIRED_ASCENSIONS,
    /** Romper el Velo (v0.19.6). */
    val veilBroken: Boolean = false,
    val veilPressing: Boolean = false,
    val veilFactor: BigDecimal = BigDecimal.ONE,
    val veilApproach: Float = 0f,
    /** Cuando las Estrellas Estén en Posición (v0.19.7). */
    val stars: Double = 0.0,
    val stardust: Double = 0.0,
    val starsPerHour: Double = 0.0,
    val dustPerSecond: Double = 0.0,
    val starLevels: Map<String, Int> = emptyMap(),
    val starsAligned: Boolean = false,
    val alignmentSecondsLeft: Long = 0,
    /** Bestiario (v0.19.8). */
    val beastLevels: Map<String, Int> = emptyMap(),
    val essence: Int = 0,
    val encounterId: String? = null,
    val encounterSecondsLeft: Int = 0,
    val beastBonuses: Map<BeastKind, Double> = emptyMap(),
    val eternalLevels: Map<String, Int> = emptyMap(),
    /** Consagración: si ya se ve y el nivel de cada edificio consagrado. */
    val consecrationRevealed: Boolean = false,
    val consecrations: Map<String, Int> = emptyMap(),
    /** Fase de la Marea (null = todavía oculta) y segundos que le quedan. */
    val tide: TidePhase? = null,
    val tideSecondsLeft: Int = 0,
    /** Mutaciones (v0.18.4): la de esta vida, las que ofrece la próxima Ascensión y las descubiertas. */
    val mutationsRevealed: Boolean = false,
    val activeMutation: Mutation? = null,
    val mutationOffer: List<Mutation> = emptyList(),
    val mutationsSeen: Int = 0,
    /** Mutaciones descubiertas, por id (para su catálogo). */
    val seenMutations: Set<String> = emptySet(),
    /** Interfaz por capas (v0.19.0): pestañas visibles y las que aún no se abrieron. */
    val menuUnlocks: MenuUnlocks = MenuUnlocks(),
    val newTabs: Set<MenuTab> = emptySet(),
    /** Medidor de Locura (null = todavía oculto), segundos de colapso y edificio poseído. */
    val madness: Double? = null,
    val collapseSeconds: Int = 0,
    val possessedBuilding: String? = null,
    /** Espacios de Runa: cuántos hay, qué runas están grabadas y cuántas veces se atrapó cada una. */
    val runeSlots: Int = 0,
    val engravedRunes: List<String> = emptyList(),
    val runeCatches: Map<String, Int> = emptyMap(),
    /** Maestría de runas (v0.18.3): capturas de cada runa desde siempre (vacío = oculta). */
    val runeMastery: Map<String, Int> = emptyMap()
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
    /** Presagio de la Locura: un edificio poseído (su nombre) o Ecos dispersos (cuántos). */
    data class MadnessOmen(val possessedBuilding: String?, val echoesLost: BigDecimal?) : EngineEffect
    /** Se Trascendió y se ganó [ether] de Éter. */
    data class Transcended(val ether: Int, val auto: Boolean = false) : EngineEffect
    /** La Locura llegó a 100: la mente colapsó. */
    object MadnessCollapse : EngineEffect
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

    // Medidor de Locura (v0.17.3)
    private var madness = MadnessMeter()
    private var collapseSeconds = 0
    private var possessedBuildingId: String? = null
    private var possessedSeconds = 0
    private var madnessHighStreak = 0
    private var lastRecalcMadness = 0.0
    /** Runas atrapadas por nivel en el último segundo (para sumar Locura). */
    private var lastRuneCounts: Triple<Int, Int, Int>? = null
    /** Efectos del pacto de esta vida combinados con las runas grabadas (v0.17.2). */
    private var pactWithRunes = PactEffects()
    private val pact: PactEffects get() = pactWithRunes
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
            refreshEngravings()
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
            if (currentTide() == TidePhase.DEAD) statsManager.count(AchievementStat.TIDE_DEAD_RITUALS)
            // Locura: los rituales la suben (la Convergencia mucho); la Letanía la calma
            when (ritual.effectType) {
                RitualEffectType.CALM -> { madness.calm(ritual.effectValue); syncMadness() }
                RitualEffectType.CONVERGENCE -> {
                    addMadness(MadnessMeter.PER_CONVERGENCE)
                    if (currentTide() == TidePhase.HIGH) statsManager.count(AchievementStat.TIDE_HIGH_CONVERGENCE)
                }
                else -> addMadness(MadnessMeter.PER_RITUAL)
            }
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
        val ascensions = milestoneAscensions()
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
        // Trascendencia automática (hito): cuando el Éter ganado iguale al que se tiene
        if (automation.autoTranscend && TranscendenceMilestone.AUTO_TRANSCEND.isReached(transcendences()) &&
            TranscendenceMilestone.shouldAutoTranscend(etherGainNow(), statsManager.counter(AchievementStat.ETHER).toInt())
        ) {
            performTranscend(auto = true)
            return
        }
        if (changed) requestSave()
    }

    private fun transcendences(): Int = statsManager.counter(AchievementStat.TRANSCENDENCES).toInt()

    /**
     * Las Ascensiones que cuentan para los hitos de Ascensión. Con Memoria de la Máquina
     * (hito de Trascendencia) todos están alcanzados desde el inicio del ciclo.
     */
    private fun milestoneAscensions(): Int {
        val real = statsManager.totalPrestiges
        return if (TranscendenceMilestone.MACHINE_MEMORY.isReached(transcendences())) {
            maxOf(real, AscensionMilestone.entries.maxOf { it.ascensions })
        } else real
    }

    /** Ascensiones del ciclo que pide Trascender (El Atajo las baja). */
    private fun transcendRequired(): Int = TranscendenceMilestone.requiredAscensions(transcendences())

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
    fun ascend(nextPact: Pact? = null, abandonPath: Boolean = false, nextTrial: Trial? = null, nextMutation: Mutation? = null) = post {
        if (performAscension(nextPact ?: activePact, abandonPath, nextTrial, nextMutation) != null) soundManager.playSound(SoundType.BUY)
    }

    /** Asciende y devuelve la Cordura ganada, o null si todavía no se puede. */
    private fun performAscension(
        nextPact: Pact,
        abandonPath: Boolean = false,
        nextTrial: Trial? = null,
        nextMutation: Mutation? = null
    ): BigDecimal? {
        val finalPotential = prestigeManager.calculatePotentialPrestige(knowledgeThisRun())
            .multiply(sanityGainMultiplier())
        if (finalPotential.signum() <= 0) return null

        // Logros: la vida que termina (pacto, Senda, hazañas) antes de reiniciarla
        recordFinishedLife(finalPotential, abandonPath)
        // La mutación elegida tiene que estar entre las ofrecidas por esta Ascensión
        val offeredMutations = mutationOffer()
        statsManager.count(AchievementStat.CYCLE_SANITY, finalPotential)
        prestigeManager.addPrestige(finalPotential)
        statsManager.trackPrestige(finalPotential)
        if (madness.value >= HIGH_MADNESS) statsManager.flag(AchievementStat.FEAT_STEEL_NERVES)
        statsManager.resetRunCounters()
        madness.reset()
        syncMadness()
        loadMadness()
        deliriumMultiplier = 1.0
        // La Prueba de la vida nueva (una bloqueada no se puede elegir)
        if (nextTrial != null && nextTrial.isUnlocked(statsManager.totalPrestiges)) startTrial(nextTrial)
        if (nextMutation != null && nextMutation in offeredMutations) {
            statsManager.flag(AchievementStat.ACTIVE_MUTATION_PREFIX + nextMutation.id)
            statsManager.flag(AchievementStat.MUTATION_SEEN_PREFIX + nextMutation.id)
        }
        // Cambiar de Senda solo se puede al Ascender: devuelve todo lo gastado en ella
        if (abandonPath) {
            val kept = statsManager.counter(AchievementStat.CYCLE_PATH_KEPT).toInt()
            prestigeManager.residualSanity = prestigeManager.residualSanity.add(prestigeShopManager.abandonPath(kept))
            statsManager.counters.remove(AchievementStat.CYCLE_PATH_KEPT)
        }

        // Hitos de Ascensión: lo que la nueva vida conserva o trae de entrada
        val ascensions = statsManager.totalPrestiges
        val milestones = milestoneAscensions()
        // El pacto de la vida nueva (uno bloqueado no se puede elegir)
        setPact(if (nextPact.isUnlocked(ascensions, earlyPacts())) nextPact else Pact.NONE)
        buildingManager.resetForPrestige()
        upgradeManager.resetForPrestige(keep = { AscensionMilestone.keepsOnAscension(it, milestones) })
        if (AscensionMilestone.STARTING_SYNAPSES.isReached(milestones)) {
            buildingManager.availableBuildings.firstOrNull { it.type == BuildingType.CLICK }
                ?.amountOwned = AscensionMilestone.SYNAPSES_AT_START
        }
        applyStartingBuildings()
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
        activeMutation()?.let { mutation ->
            statsManager.count(AchievementStat.MUTATION_LIVES)
            statsManager.flag(AchievementStat.mutationWith(activePact.id, mutation.id))
            if (path != null) statsManager.flag(AchievementStat.mutationPath(path.id, mutation.id))
        }
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
        // Ascensiones de todos los ciclos: los logros de Ascensiones no retroceden al Trascender
        put(AchievementStat.ASCENSIONS, totalAscensionsEver())
        put(AchievementStat.ETERNAL_LEVELS, EternalUpgrade.entries.sumOf { eternalLevel(it) })
        val consecrations = buildingManager.availableBuildings.map { consecrationLevel(it.id) }
        put(AchievementStat.CONSECRATION_LEVELS, consecrations.sum())
        put(AchievementStat.CONSECRATED_BUILDINGS, consecrations.count { it > 0 })
        put(AchievementStat.CONSECRATED_MAX, consecrations.count { it >= Consecration.MAX_LEVEL })
        val mastery = RuneEngraving.ALL.map { RuneEngraving.masteryLevel(it.tier, statsManager.counter(AchievementStat.mastery(it.id)).toInt()) }
        put(AchievementStat.MASTERY_LEVELS, mastery.sum())
        put(AchievementStat.MASTERY_KINDS, mastery.count { it > 0 })
        put(AchievementStat.MASTERY_MAXED, mastery.count { it >= RuneEngraving.MAX_MASTERY })
        put(AchievementStat.TRANSCEND_MILESTONES, TranscendenceMilestone.reached(transcendences()))
        put(AchievementStat.VEIL_DEPTH, Veil.depth(knowledgeThisRun()))
        put(AchievementStat.STAR_LEVELS, StarUpgrade.entries.sumOf { starLevel(it) })
        val beasts = beastLevels()
        put(AchievementStat.BEASTS_SEEN, beasts.count { it.value > 0 })
        put(AchievementStat.BEAST_LEVELS, beasts.values.sum())
        put(AchievementStat.FAMILIES_COMPLETE, Bestiary.families.count { Bestiary.isFamilyComplete(it.id, beasts) })
        put(AchievementStat.PRIMORDIALS_SEEN, Bestiary.beasts.count { it.rarity == BeastRarity.PRIMORDIAL && (beasts[it.id] ?: 0) > 0 })
        put(AchievementStat.MUTATIONS_SEEN, Mutation.entries.count { statsManager.counter(AchievementStat.MUTATION_SEEN_PREFIX + it.id).signum() > 0 })
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
        put(AchievementStat.CONSTELLATIONS, prestigeShopManager.completedConstellations())

        val attention = effectiveAttention()
        put(AchievementStat.ATTENTION_POINTS, attentionPoints())
        put(AchievementStat.TRIALS_COMPLETED, completedTrials().size)
        put(AchievementStat.MADNESS, BigDecimal.valueOf(madness.value))
        put(AchievementStat.TIDE_PHASE, currentTide()?.ordinal ?: -1)
        val engraved = engravedRunes()
        put(AchievementStat.RUNE_SLOTS, runeSlots())
        put(AchievementStat.ENGRAVED, engraved.size)
        put(AchievementStat.ENGRAVED_T3, engraved.count { RuneEngraving.variant(it)?.tier == RuneTier.TIER_3_TRACE })
        put(AchievementStat.RUNE_KINDS, RuneEngraving.ALL.count { statsManager.counter(AchievementStat.runeCatches(it.id)).signum() > 0 })
        put(AchievementStat.ENGRAVED_KINDS, statsManager.counters.keys.count { it.startsWith(AchievementStat.ENGRAVED_EVER_PREFIX) })
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
        loadMadness()
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
        loadMadness()
        setPact(Pact.fromId(data.activePact))

        // Los tutoriales que la partida importada ya vio no se vuelven a mostrar
        tutorialManager.markStepsAsCompleted(TutorialProgress.stepsSeenIn(data))
        markVisibleTabsSeenOnce()

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

    fun onMenuTabSelected(tab: MenuTab) = post {
        // Una pestaña solo se puede abrir si ya se ve
        if (!menuUnlocks().has(tab)) return@post
        if (tab == MenuTab.GRIMOIRE) tutorialManager.triggerStep(TutorialStep.UPGRADES_TAB)
        if (tab == MenuTab.RITUALS && echoes.signum() > 0) tutorialManager.triggerStep(TutorialStep.RITUALS_TAB)
        val key = AchievementStat.SEEN_TAB_PREFIX + tab.id
        if (statsManager.counter(key).signum() == 0) {
            statsManager.flag(key)
            publishEconomy()
            requestSave()
        }
    }

    // =====================================================================
    // INTERFAZ POR CAPAS (v0.19.0)
    // =====================================================================

    private fun menuUnlocks(): MenuUnlocks = MenuUnlocks.compute(
        reveal = currentReveal(),
        ascensions = statsManager.totalPrestiges,
        runeSlots = runeSlots(),
        masteryRevealed = consecrationRevealed(),
        transcendRevealed = transcendRevealed(),
        mutationsRevealed = mutationsRevealed(),
        starsRevealed = starsRevealed(),
        bestiaryRevealed = bestiaryRevealed()
    )

    /** Pestañas visibles que el jugador todavía no abrió: llevan la marca de "nuevo". */
    private fun newTabs(): Set<MenuTab> = menuUnlocks().tabs
        .filter { it !in MenuUnlocks.NEVER_NEW && statsManager.counter(AchievementStat.SEEN_TAB_PREFIX + it.id).signum() == 0 }
        .toSet()

    /**
     * Al pasar a la interfaz por capas, lo que el jugador ya tenía no se anuncia como nuevo:
     * se marca una sola vez como visto todo lo visible en ese momento.
     */
    private fun markVisibleTabsSeenOnce() {
        if (statsManager.counter(AchievementStat.SEEN_TABS_INIT).signum() > 0) return
        menuUnlocks().tabs.forEach { statsManager.flag(AchievementStat.SEEN_TAB_PREFIX + it.id) }
        statsManager.flag(AchievementStat.SEEN_TABS_INIT)
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
        loadMadness()

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
        markVisibleTabsSeenOnce()

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
        // Las Estrellas siguen naciendo mientras no juegas
        if (diffSeconds > 0 && starsRevealed()) {
            val (stars, dust) = Stars.offline(starCount(), starRate(), diffSeconds)
            statsManager.counters[AchievementStat.STARS] = BigDecimal.valueOf(stars)
            addStardust(dust)
        }
        // La Locura también baja mientras no juegas
        if (diffSeconds > 0) {
            madness.decay(diffSeconds)
            syncMadness()
        }
        recalculate()
        // Producción base: los potenciadores temporales no se extienden al tiempo fuera
        val gains = baseProductionPerSecond
            .multiply(prestigeShopManager.getOfflinePrestigeMultiplier())
            .multiply(BigDecimal.valueOf(pact.offline))
            .multiply(BigDecimal.valueOf(attentionMultiplier(Attention.DREAM)))
            .multiply(BigDecimal.valueOf(trialReward().offline * if (trialRules().noOffline) 0.0 else 1.0))
            .let { rate -> Veil.offlineGain(knowledgeThisRun(), rate, diffSeconds, veilBroken()) }

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
        madnessTick()
        tideTick()
        masteryTick()
        veilTick()
        starsTick()
        encounterTick()

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
        addMadness(madness.clickGain())
        clicksThisTide++

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
        if (AscensionMilestone.BUILDING_AUTOBUYER.isReached(milestoneAscensions())) tutorialManager.triggerStep(TutorialStep.AUTOMATION)
        if (transcendences() >= 1) tutorialManager.triggerStep(TutorialStep.TRANSCEND_MILESTONES)
        // Los pactos se eligen al Ascender: se presentan cuando la Ascensión está a mano
        if (potentialPrestige.signum() > 0) tutorialManager.triggerStep(TutorialStep.PACTS)
        if (Attention.available(ascensions) > 0) tutorialManager.triggerStep(TutorialStep.ATTENTION)
        // Las Sendas, cuando alguna ya se puede comprar
        val shop = prestigeShopManager.upgrades
        val pathOpen = shop.any { node -> MadnessPaths.isPath(node.id) && MadnessPaths.isAvailable(node, shop) }
        if (pathOpen) tutorialManager.triggerStep(TutorialStep.PATHS)
        // Las Pruebas se eligen al Ascender: se presentan cuando la próxima ya puede tener una
        if (ascensions + 1 >= Trial.REVEAL_AT) tutorialManager.triggerStep(TutorialStep.TRIALS)
        if (runeSlots() > 0) tutorialManager.triggerStep(TutorialStep.RUNE_SLOTS)
        if (madnessRevealed()) tutorialManager.triggerStep(TutorialStep.MADNESS)
        if (tidesRevealed()) tutorialManager.triggerStep(TutorialStep.TIDES)
        if (transcendRevealed()) tutorialManager.triggerStep(TutorialStep.TRANSCEND)
        if (consecrationRevealed()) tutorialManager.triggerStep(TutorialStep.CONSECRATION)
        if (mutationsRevealed()) tutorialManager.triggerStep(TutorialStep.MUTATIONS)
        if (consecrationRevealed() && statsManager.counters.keys.any { it.startsWith(AchievementStat.MASTERY_PREFIX) }) {
            tutorialManager.triggerStep(TutorialStep.RUNE_MASTERY)
        }
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
        // Ojos Abiertos (hito de Trascendencia): cuentan todas las Ascensiones desde siempre
        val counted = if (TranscendenceMilestone.OPEN_EYES.isReached(transcendences())) totalAscensionsEver() else statsManager.totalPrestiges
        val base = Attention.available(counted)
        return if (base > 0) base + trialReward().attentionPoints + prestigeShopManager.getAttentionBonus() else 0
    }

    private fun startTrial(trial: Trial) {
        statsManager.flag(AchievementStat.ACTIVE_TRIAL_PREFIX + trial.id)
        statsManager.count(AchievementStat.TRIALS_STARTED)
        deliriumMultiplier = 1.0
        deliriumSeconds = 0
    }

    // =====================================================================
    // ESTRELLAS (v0.19.7)
    // =====================================================================

    private fun starsRevealed(): Boolean = transcendences() >= 1

    private fun starLevel(upgrade: StarUpgrade): Int = statsManager.counter(AchievementStat.starUpgrade(upgrade.id)).toInt()

    private fun starCount(): Double = statsManager.counter(AchievementStat.STARS).toDouble()

    private fun starRate(): Double = Stars.starsPerSecond(starLevel(StarUpgrade.NEBULA), transcendences())

    private fun nowSeconds(): Long = System.currentTimeMillis() / 1000

    private fun starsAligned(): Boolean = starsRevealed() && Stars.isAligned(nowSeconds(), starLevel(StarUpgrade.CONJUNCTION))

    private fun addStardust(amount: Double) {
        if (amount <= 0.0) return
        val add = BigDecimal.valueOf(amount * beastBonus(BeastKind.STARDUST))
        statsManager.counters[AchievementStat.STARDUST] = statsManager.counter(AchievementStat.STARDUST).add(add)
        statsManager.count(AchievementStat.STARDUST_EARNED, add)
    }

    /** Cada segundo nacen Estrellas y cada una da Polvo (x5 con las estrellas en posición). */
    private fun starsTick() {
        if (!starsRevealed()) return
        tutorialManager.triggerStep(TutorialStep.STARS)
        val stars = starCount() + starRate()
        statsManager.counters[AchievementStat.STARS] = BigDecimal.valueOf(stars)
        addStardust(Stars.dustPerSecond(stars, starsAligned()))
    }

    /** Compra un nivel de una mejora estelar con Polvo Estelar. */
    fun buyStarUpgrade(id: String) = post {
        val upgrade = StarUpgrade.fromId(id) ?: return@post
        if (!starsRevealed()) return@post
        val level = starLevel(upgrade)
        if (upgrade.isMaxed(level)) return@post
        val cost = BigDecimal.valueOf(upgrade.cost(level))
        val dust = statsManager.counter(AchievementStat.STARDUST)
        if (dust < cost) return@post
        statsManager.counters[AchievementStat.STARDUST] = dust.subtract(cost)
        statsManager.counters[AchievementStat.starUpgrade(upgrade.id)] = BigDecimal(level + 1)
        // Logros: con qué pacto, y durante la alineación (con qué Senda)
        statsManager.flag(AchievementStat.starBuyWith(activePact.id))
        if (starsAligned()) {
            statsManager.count(AchievementStat.ALIGNED_PURCHASES)
            MadnessPaths.active(prestigeShopManager.upgrades)?.let { statsManager.flag(AchievementStat.starAlignedPath(it.id)) }
        }
        soundManager.playSound(SoundType.BUY)
        recalculate()
        requestSave()
    }

    // =====================================================================
    // BESTIARIO (v0.19.8)
    // =====================================================================

    private var encounter: Beast? = null
    private var encounterSecondsLeft = 0
    private var nextEncounterIn = Bestiary.MIN_INTERVAL

    private fun beastLevels(): Map<String, Int> =
        Bestiary.beasts.associate { it.id to statsManager.counter(AchievementStat.beast(it.id)).toInt() }

    private fun beastBonus(kind: BeastKind): Double = Bestiary.bonuses(beastLevels()).getValue(kind)

    /** El Bestiario se abre al superar la primera Prueba (o si ya hay criaturas o Esencia). */
    private fun bestiaryRevealed(): Boolean =
        completedTrials().isNotEmpty() || statsManager.counter(AchievementStat.ESSENCE_EARNED).signum() > 0 ||
            Bestiary.beasts.any { statsManager.counter(AchievementStat.beast(it.id)).signum() > 0 }

    private fun addEssence(amount: Double) {
        val gained = BigDecimal.valueOf(kotlin.math.floor(amount * beastBonus(BeastKind.ESSENCE)))
        if (gained.signum() <= 0) return
        statsManager.counters[AchievementStat.ESSENCE] = statsManager.counter(AchievementStat.ESSENCE).add(gained)
        statsManager.count(AchievementStat.ESSENCE_EARNED, gained)
    }

    /** Cada segundo: el encuentro activo se escapa o llega uno nuevo. */
    private fun encounterTick() {
        if (!bestiaryRevealed()) return
        tutorialManager.triggerStep(TutorialStep.BESTIARY)
        if (encounter != null) {
            encounterSecondsLeft--
            if (encounterSecondsLeft <= 0) encounter = null
            return
        }
        nextEncounterIn--
        if (nextEncounterIn > 0) return
        encounter = Bestiary.rollEncounter(beastLevels())
        encounterSecondsLeft = Bestiary.ENCOUNTER_SECONDS
        nextEncounterIn = Bestiary.nextInterval(beastBonus(BeastKind.ENCOUNTER))
        soundManager.playSound(SoundType.EVENT_APPEAR)
    }

    /**
     * Cataloga la criatura del encuentro: la primera vez se descubre (nivel 1); repetida
     * da Esencia según su rareza.
     */
    fun catchEncounter() = post {
        val beast = encounter ?: return@post
        encounter = null
        val key = AchievementStat.beast(beast.id)
        val level = statsManager.counter(key).toInt()
        if (level == 0) statsManager.counters[key] = BigDecimal.ONE
        else addEssence(beast.rarity.duplicateEssence.toDouble())
        statsManager.count(AchievementStat.ENCOUNTERS)
        statsManager.flag(AchievementStat.beastCatchWith(activePact.id))
        MadnessPaths.active(prestigeShopManager.upgrades)?.let { statsManager.flag(AchievementStat.beastCatchPath(it.id)) }
        if (beast.rarity == BeastRarity.PRIMORDIAL && starsAligned()) statsManager.flag(AchievementStat.FEAT_PRIMORDIAL_ALIGNED)
        soundManager.playSound(SoundType.ACHIEVEMENT)
        refreshEngravings()
        recalculate()
        requestSave()
    }

    /** Para los tests: hace aparecer un encuentro con esa criatura. */
    internal fun forceEncounter(beastId: String) = post {
        encounter = Bestiary.beast(beastId) ?: return@post
        encounterSecondsLeft = Bestiary.ENCOUNTER_SECONDS
    }

    /** Sube de nivel una criatura descubierta con Esencia. */
    fun upgradeBeast(id: String) = post {
        val beast = Bestiary.beast(id) ?: return@post
        val key = AchievementStat.beast(beast.id)
        val level = statsManager.counter(key).toInt()
        if (level <= 0 || level >= Bestiary.MAX_LEVEL) return@post
        val cost = BigDecimal(Bestiary.upgradeCost(beast, level))
        val essence = statsManager.counter(AchievementStat.ESSENCE)
        if (essence < cost) return@post
        statsManager.counters[AchievementStat.ESSENCE] = essence.subtract(cost)
        statsManager.counters[key] = BigDecimal(level + 1)
        soundManager.playSound(SoundType.BUY)
        refreshEngravings()
        recalculate()
        requestSave()
    }

    private fun veilBroken(): Boolean = statsManager.counter(AchievementStat.VEIL_BROKEN).signum() > 0

    /** El Velo frena esta vida: se anota para el logro y se explica una vez. */
    private fun veilTick() {
        if (Veil.isPressing(knowledgeThisRun(), veilBroken())) {
            statsManager.flag(AchievementStat.VEIL_TOUCHED)
            tutorialManager.triggerStep(TutorialStep.VEIL)
        }
    }

    /**
     * Rompe el Velo para siempre (v0.19.6): pide las 10 Pruebas superadas. Desaparece el
     * tope y la producción se multiplica por [Veil.BROKEN_PRODUCTION].
     */
    fun breakVeil() = post {
        if (!Veil.canBreak(completedTrials().size, veilBroken())) return@post
        statsManager.counters[AchievementStat.VEIL_BROKEN] = BigDecimal.ONE
        statsManager.flag(AchievementStat.veilBrokenWith(activePact.id))
        if (activeTrial() != null) statsManager.flag(AchievementStat.FEAT_VEIL_IN_TRIAL)
        soundManager.playSound(SoundType.ACHIEVEMENT)
        recalculate()
        requestSave()
    }

    /** Abandona la Prueba de esta vida: sus reglas se levantan, sin recompensa. */
    fun abandonTrial() = post {
        val trial = activeTrial() ?: return@post
        statsManager.counters.remove(AchievementStat.ACTIVE_TRIAL_PREFIX + trial.id)
        deliriumMultiplier = 1.0
        // refreshEngravings también aplica los modificadores del pacto
        refreshEngravings()
        recalculate()
        requestSave()
    }

    /**
     * Edificios con los que empieza la vida: el Legado del Culto (Mapa de Locura) y la
     * Prueba activa (Manos Atadas). Sin ellos, una vida sin clic no podría arrancar.
     */
    private fun applyStartingBuildings() {
        // El Culto del Mapa de Locura (v0.19.2: varios edificios) y la Prueba activa
        val start = HashMap(if (trialRules().noSanityBonus) emptyMap() else prestigeShopManager.getStartingBuildings())
        trialRules().startingBuildings.forEach { (id, n) -> start[id] = maxOf(start[id] ?: 0, n) }
        start.forEach { (id, n) ->
            val building = buildingManager.availableBuildings.find { it.id == id } ?: return@forEach
            building.amountOwned = maxOf(building.amountOwned, n)
            building.isVisible = true
        }
        if (start.isNotEmpty()) buildingManager.checkUnlocks(knowledge)
    }

    /** Una vez por segundo: la Prueba se supera al llegar a su meta en esta vida. */
    private fun checkTrial() {
        val trial = activeTrial() ?: return
        if (trial.rules.delirium) rollDelirium()
        if (knowledgeThisRun() < trial.goal) return
        // Bestiario: la Prueba da Esencia (x3 la primera vez)
        addEssence(Bestiary.trialEssence(trial, statsManager.counter(AchievementStat.trial(trial.id)).signum() == 0).toDouble())
        statsManager.counters.remove(AchievementStat.ACTIVE_TRIAL_PREFIX + trial.id)
        statsManager.flag(AchievementStat.trial(trial.id))
        statsManager.flag(AchievementStat.trialWith(trial.id, activePact.id))
        activeMutation()?.let { statsManager.flag(AchievementStat.mutationTrial(trial.id, it.id)) }
        if (statsManager.runTimeSeconds < FAST_TRIAL_SECONDS) statsManager.flag(AchievementStat.FEAT_TRIAL_FAST)
        deliriumMultiplier = 1.0
        refreshEngravings()
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

    // =====================================================================
    // MEDIDOR DE LOCURA (v0.17.3)
    // =====================================================================

    private fun madnessRevealed(): Boolean = statsManager.totalPrestiges >= MadnessMeter.REVEAL_AT

    /** Bono de la Locura y castigo del colapso, sobre la producción. */
    private fun madnessFactor(): Double {
        if (!madnessRevealed()) return 1.0
        val collapse = if (collapseSeconds > 0) MadnessMeter.COLLAPSE_PRODUCTION else 1.0
        return madness.productionBonus() * collapse
    }

    private fun syncMadness() {
        statsManager.counters.remove(AchievementStat.MADNESS)
        if (madness.value > 0.0) statsManager.counters[AchievementStat.MADNESS] =
            BigDecimal.valueOf(madness.value).setScale(2, java.math.RoundingMode.HALF_UP)
        statsManager.record(AchievementStat.MAX_MADNESS, BigDecimal.valueOf(madness.value))
        if (madness.value >= EDGE_MADNESS && madness.value < MadnessMeter.MAX) statsManager.flag(AchievementStat.FEAT_EDGE)
    }

    private fun loadMadness() {
        madness = MadnessMeter(statsManager.counter(AchievementStat.MADNESS).toDouble())
        collapseSeconds = 0
        possessedBuildingId = null
        possessedSeconds = 0
        madnessHighStreak = 0
        lastRuneCounts = null
    }

    /** Suma Locura por jugar fuerte; si llega a 100, la mente colapsa. */
    private fun addMadness(amount: Double) {
        if (!madnessRevealed()) return
        // Marea Alta: la Locura sube más rápido
        // Mente Templada (Mapa de Locura): sube menos
        val gain = amount * (currentTide()?.madnessGain ?: 1.0) * prestigeShopManager.getMadnessGainMultiplier()
        if (madness.add(gain)) collapse() else syncMadness()
    }

    /** El colapso (leve): se pierden los potenciadores y la producción baja a la mitad 1 minuto. */
    private fun collapse() {
        syncMadness()
        runeManager.clearBuffs()
        ritualManager.endActive()
        eventScheduler.clearBuff()
        madness.reset()
        syncMadness()
        collapseSeconds = MadnessMeter.COLLAPSE_SECONDS
        statsManager.count(AchievementStat.MADNESS_COLLAPSES)
        if (currentTide() == TidePhase.HIGH) statsManager.flag(AchievementStat.FEAT_TSUNAMI)
        statsManager.count(AchievementStat.RUN_COLLAPSES)
        soundManager.playSound(SoundType.EVENT_APPEAR)
        _effects.trySend(EngineEffect.MadnessCollapse)
        recalculate()
    }

    /** Una vez por segundo: runas atrapadas, la Locura baja, presagios y castigos. */
    private fun madnessTick() {
        if (!madnessRevealed()) return
        val counts = Triple(statsManager.runesSevered, statsManager.runesStabilized, statsManager.runesInscribed)
        lastRuneCounts?.let { last ->
            val gained = (counts.first - last.first).coerceAtLeast(0) * MadnessMeter.perRune(RuneTier.TIER_1_SWIPE) +
                (counts.second - last.second).coerceAtLeast(0) * MadnessMeter.perRune(RuneTier.TIER_2_HOLD) +
                (counts.third - last.third).coerceAtLeast(0) * MadnessMeter.perRune(RuneTier.TIER_3_TRACE)
            if (gained > 0) addMadness(gained)
            val caught = (counts.first - last.first) + (counts.second - last.second) + (counts.third - last.third)
            if (caught > 0 && currentTide() == TidePhase.HIGH) statsManager.count(AchievementStat.TIDE_HIGH_RUNES, BigDecimal(caught))
        }
        lastRuneCounts = counts

        var changed = false
        if (collapseSeconds > 0 && --collapseSeconds == 0) changed = true
        if (possessedSeconds > 0 && --possessedSeconds == 0) {
            possessedBuildingId = null
            changed = true
        }
        madness.decay(1, currentTide()?.madnessDecay ?: 1.0)
        if (madness.rollOmen()) {
            omen()
            changed = true
        }
        madnessHighStreak = if (madness.value >= HIGH_MADNESS) madnessHighStreak + 1 else 0
        statsManager.record(AchievementStat.MAX_MADNESS_STREAK, BigDecimal(madnessHighStreak))
        syncMadness()
        if (changed || kotlin.math.abs(madness.value - lastRecalcMadness) >= MADNESS_RECALC_STEP) {
            lastRecalcMadness = madness.value
            recalculate()
        }
    }

    /** Un presagio malo: un edificio poseído deja de producir, o se dispersan Ecos. */
    private fun omen() {
        statsManager.count(AchievementStat.MADNESS_OMENS)
        val owned = buildingManager.availableBuildings.filter { it.type != BuildingType.CLICK && it.amountOwned > 0 }
        if (owned.isNotEmpty() && (echoes < BigDecimal.ONE || kotlin.random.Random.nextBoolean())) {
            val building = owned.random()
            possessedBuildingId = building.id
            possessedSeconds = MadnessMeter.POSSESSION_SECONDS
            _effects.trySend(EngineEffect.MadnessOmen(building.name, null))
        } else if (echoes >= BigDecimal.ONE) {
            val lost = echoes.multiply(BigDecimal.valueOf(MadnessMeter.SCATTERED_ECHOES)).setScale(0, java.math.RoundingMode.CEILING)
            echoes = echoes.subtract(lost).max(BigDecimal.ZERO)
            _effects.trySend(EngineEffect.MadnessOmen(null, lost))
        }
    }

    // =====================================================================
    // TRASCENDER (v0.18.0)
    // Todo vive en los contadores: Éter, mejoras eternas y datos del ciclo.
    // =====================================================================

    private fun eternalLevel(upgrade: EternalUpgrade): Int = statsManager.counter(AchievementStat.eternal(upgrade.id)).toInt()

    private fun cycleStart(): Int = statsManager.counter(AchievementStat.CYCLE_START).toInt()

    private fun cycleAscensions(): Int = (statsManager.totalPrestiges - cycleStart()).coerceAtLeast(0)

    private fun totalAscensionsEver(): Int = statsManager.counter(AchievementStat.ASCENSIONS_PAST).toInt() + cycleAscensions()

    /** Cordura ganada en el ciclo; en el primero, toda la histórica (se registraba desde antes). */
    private fun cycleSanity(): BigDecimal {
        val counted = statsManager.counter(AchievementStat.CYCLE_SANITY)
        return if (statsManager.counter(AchievementStat.TRANSCENDENCES).signum() == 0) counted.max(statsManager.lifetimeSanity) else counted
    }

    private fun etherGainNow(): Int = Transcendence.etherGain(
        cycleAscensions(),
        cycleSanity(),
        Transcendence.etherMultiplier(eternalLevel(EternalUpgrade.ETHER_ECHO)) * Stars.etherMultiplier(starLevel(StarUpgrade.ETHER_DUST)) *
            beastBonus(BeastKind.ETHER),
        transcendRequired()
    )

    private fun transcendRevealed(): Boolean =
        cycleAscensions() >= transcendRequired() || statsManager.counter(AchievementStat.TRANSCENDENCES).signum() > 0

    // =====================================================================
    // CONSAGRAR EDIFICIOS (v0.18.1)
    // =====================================================================

    private fun consecrationRevealed(): Boolean = statsManager.counter(AchievementStat.TRANSCENDENCES).signum() > 0

    private fun consecrationLevel(buildingId: String): Int =
        statsManager.counter(AchievementStat.consecrated(buildingId)).toInt()

    /** Consagra un edificio un nivel más: pide unidades en esta vida y Éter. */
    fun consecrate(buildingId: String) = post {
        if (!consecrationRevealed()) return@post
        val building = buildingManager.availableBuildings.find { it.id == buildingId } ?: return@post
        val level = consecrationLevel(buildingId)
        val ether = statsManager.counter(AchievementStat.ETHER).toInt()
        if (!Consecration.canConsecrate(level, building.amountOwned, ether)) return@post
        val left = ether - Consecration.etherCost(level)
        statsManager.counters.remove(AchievementStat.ETHER)
        if (left > 0) statsManager.counters[AchievementStat.ETHER] = BigDecimal(left)
        statsManager.counters[AchievementStat.consecrated(buildingId)] = BigDecimal(level + 1)
        statsManager.flag(AchievementStat.consecratedWith(activePact.id, buildingId))
        soundManager.playSound(SoundType.BUY)
        recalculate()
        requestSave()
    }

    /** Compra un nivel de una mejora eterna con Éter. */
    fun buyEternal(id: String) = post {
        val upgrade = EternalUpgrade.fromId(id) ?: return@post
        val level = eternalLevel(upgrade)
        if (upgrade.isMaxed(level)) return@post
        val cost = BigDecimal(upgrade.cost(level))
        val ether = statsManager.counter(AchievementStat.ETHER)
        if (ether < cost) return@post
        statsManager.counters[AchievementStat.ETHER] = ether.subtract(cost)
        if (statsManager.counters[AchievementStat.ETHER]!!.signum() == 0) statsManager.counters.remove(AchievementStat.ETHER)
        statsManager.counters[AchievementStat.eternal(upgrade.id)] = BigDecimal(level + 1)
        soundManager.playSound(SoundType.BUY)
        recalculate()
        requestSave()
    }

    /**
     * Trascender: con 50 Ascensiones en el ciclo se reinicia casi todo a cambio de Éter.
     * Se conservan los logros, las estadísticas, el Éter y las mejoras eternas; las
     * Pruebas y las runas grabadas solo si se compró la mejora que las recuerda.
     */
    fun transcend() = post { performTranscend(auto = false) }

    private fun performTranscend(auto: Boolean) {
        val gain = etherGainNow()
        if (gain <= 0) return
        val cycle = cycleAscensions()
        val shortcut = TranscendenceMilestone.SHORTCUT.isReached(transcendences())

        // Logros: con qué pacto y Senda se Trascendió
        statsManager.flag(AchievementStat.transcendWith(activePact.id))
        MadnessPaths.active(prestigeShopManager.upgrades)?.let { statsManager.flag(AchievementStat.transcendPath(it.id)) }
        if (cycle == Transcendence.REQUIRED_ASCENSIONS) statsManager.flag(AchievementStat.FEAT_EXACT_THRESHOLD)
        if (shortcut && cycle == TranscendenceMilestone.SHORTCUT_ASCENSIONS) statsManager.flag(AchievementStat.FEAT_SHORTCUT)
        if (auto) {
            statsManager.count(AchievementStat.AUTO_TRANSCENDS)
            statsManager.flag(AchievementStat.autoTranscendWith(activePact.id))
        }
        statsManager.count(AchievementStat.TRANSCENDENCES)
        statsManager.count(AchievementStat.ETHER, BigDecimal(gain))
        statsManager.count(AchievementStat.ETHER_EARNED, BigDecimal(gain))
        statsManager.record(AchievementStat.MAX_ETHER_GAIN, BigDecimal(gain))
        statsManager.count(AchievementStat.ASCENSIONS_PAST, BigDecimal(cycle))

        // El ciclo nuevo
        val start = eternalLevel(EternalUpgrade.MEMORY) * Transcendence.ASCENSIONS_PER_MEMORY
        statsManager.trackPrestige(BigDecimal.ZERO)
        statsManager.totalPrestiges = start
        statsManager.resetRunCounters()
        statsManager.counters.remove(AchievementStat.CYCLE_SANITY)
        statsManager.counters.remove(AchievementStat.CYCLE_START)
        if (start > 0) statsManager.counters[AchievementStat.CYCLE_START] = BigDecimal(start)
        if (eternalLevel(EternalUpgrade.REMEMBERED_TRIALS) == 0) {
            statsManager.counters.keys.removeAll { it.startsWith(AchievementStat.TRIAL_PREFIX) }
        }
        if (eternalLevel(EternalUpgrade.ETERNAL_RUNES) == 0) {
            statsManager.counters.keys.removeAll {
                it.startsWith(AchievementStat.RUNE_CATCH_PREFIX) || it.startsWith(AchievementStat.ENGRAVE_PREFIX)
            }
        }

        knowledge = BigDecimal.ZERO
        echoes = BigDecimal.ZERO
        prestigeManager.residualSanity = BigDecimal.ZERO
        // Hitos de Trascendencia: la Senda (Senda Eterna) y las primeras estrellas (Cielo Recordado)
        val reached = transcendences()
        val keptPath = MadnessPaths.active(prestigeShopManager.upgrades)
            ?.takeIf { TranscendenceMilestone.ETERNAL_PATH.isReached(reached) }
        prestigeShopManager.upgrades.forEach { if (it !== keptPath) it.level = 0 }
        statsManager.counters.remove(AchievementStat.CYCLE_PATH_KEPT)
        if (keptPath != null) statsManager.counters[AchievementStat.CYCLE_PATH_KEPT] = BigDecimal(keptPath.level)
        if (TranscendenceMilestone.REMEMBERED_SKY.isReached(reached)) prestigeShopManager.rememberSky()
        buildingManager.resetForPrestige()
        upgradeManager.resetForPrestige()
        runeManager.hideRune()
        runeManager.clearBuffs()
        ritualManager.reset()
        eventScheduler.clearBuff()
        madness.reset()
        syncMadness()
        loadMadness()
        deliriumMultiplier = 1.0
        setPact(Pact.NONE)
        if (AscensionMilestone.STARTING_SYNAPSES.isReached(maxOf(start, milestoneAscensions()))) {
            buildingManager.availableBuildings.firstOrNull { it.type == BuildingType.CLICK }
                ?.amountOwned = AscensionMilestone.SYNAPSES_AT_START
        }
        applyStartingBuildings()

        soundManager.playSound(SoundType.ACHIEVEMENT)
        _effects.trySend(EngineEffect.Transcended(gain, auto))
        recalculate()
        requestSave(replace = true)
    }

    private fun setPact(newPact: Pact) {
        activePact = newPact
        chaosMultiplier = 1
        refreshEngravings()
    }

    // =====================================================================
    // ESPACIOS DE RUNA (v0.17.2)
    // =====================================================================

    private fun runeSlots(): Int = trialReward().runeSlots

    /** Las runas grabadas, en el orden en que se grabaron y ajustadas a los espacios. */
    private fun engravedRunes(): List<String> = statsManager.counters.entries
        .filter { it.key.startsWith(AchievementStat.ENGRAVE_PREFIX) }
        .sortedBy { it.value }
        .map { it.key.removePrefix(AchievementStat.ENGRAVE_PREFIX) }
        .filter { RuneEngraving.variant(it) != null }
        .take(runeSlots())

    // =====================================================================
    // MUTACIONES (v0.18.4)
    // La de la vida actual es un contador de la vida ("run.mutation.<id>").
    // =====================================================================

    private fun mutationsRevealed(): Boolean = consecrationRevealed()

    private fun activeMutation(): Mutation? = statsManager.counters.keys
        .firstOrNull { it.startsWith(AchievementStat.ACTIVE_MUTATION_PREFIX) }
        ?.let { Mutation.fromId(it.removePrefix(AchievementStat.ACTIVE_MUTATION_PREFIX)) }

    /** Las tres mutaciones que ofrece la próxima Ascensión (dependen de las Ascensiones de siempre). */
    private fun mutationOffer(): List<Mutation> =
        if (mutationsRevealed()) Mutation.offer(totalAscensionsEver().toLong() * 7919L + 17L) else emptyList()

    private var lastMasterySum = -1

    /** Si sube la maestría de una runa grabada, su bono se recalcula. */
    private fun masteryTick() {
        if (!consecrationRevealed()) return
        val sum = engravedRunes().sumOf { masteryLevel(it) }
        if (sum == lastMasterySum) return
        val first = lastMasterySum < 0
        lastMasterySum = sum
        if (!first) {
            refreshEngravings()
            recalculate()
        }
    }

    /** Nivel de maestría de una runa (0 hasta la primera Trascendencia). */
    private fun masteryLevel(runeId: String): Int {
        if (!consecrationRevealed()) return 0
        val rune = RuneEngraving.variant(runeId) ?: return 0
        return RuneEngraving.masteryLevel(rune.tier, statsManager.counter(AchievementStat.mastery(runeId)).toInt())
    }

    private fun runeCatches(): Map<String, Int> = RuneEngraving.ALL.associate {
        it.id to statsManager.counter(AchievementStat.runeCatches(it.id)).toInt()
    }

    /** Recalcula el pacto con las runas grabadas y lo aplica a runas, eventos y rituales. */
    private fun refreshEngravings() {
        val tideEffects = currentTide()?.effects ?: PactEffects()
        pactWithRunes = activePact.effects
            .combinedWith(RuneEngraving.combined(engravedRunes()) { masteryLevel(it) })
            .combinedWith(tideEffects)
            .combinedWith(activeMutation()?.effects ?: PactEffects())
            // Nodos del Mapa de Locura con efectos de pacto (v0.19.2); la Prueba Mente en
            // Blanco los anula igual que al resto del Mapa
            .combinedWith(if (trialRules().noSanityBonus) PactEffects() else prestigeShopManager.getMapEffects())
            // Bestiario (v0.19.8): producción, clic, sin conexión y Cordura
            .combinedWith(Bestiary.effects(beastLevels()))
        applyPactModifiers()
    }

    // =====================================================================
    // FASES DE LA MAREA (v0.17.4)
    // Se calcula del tiempo jugado: no hace falta guardarla.
    // =====================================================================

    private fun tidesRevealed(): Boolean = statsManager.totalPrestiges >= TidePhase.REVEAL_AT

    private fun currentTide(): TidePhase? =
        if (tidesRevealed()) TidePhase.at(statsManager.totalPlayTimeSeconds) else null

    private var lastTide: TidePhase? = null
    private var clicksThisTide = 0

    /** Una vez por segundo: al cambiar de fase se aplican sus efectos y cuenta para los logros. */
    private fun tideTick() {
        val tide = currentTide()
        if (tide == lastTide) return
        val previous = lastTide
        lastTide = tide
        if (previous != null && tide != null) {
            statsManager.count(AchievementStat.TIDES_PASSED)
            statsManager.count(AchievementStat.RUN_TIDES)
            // Una Marea Baja entera sin un solo clic
            if (previous == TidePhase.LOW && clicksThisTide == 0) statsManager.flag(AchievementStat.FEAT_STILL_LOW_TIDE)
        }
        clicksThisTide = 0
        refreshEngravings()
        recalculate()
    }

    /** Graba una runa en un espacio libre (si se atrapó las veces necesarias). */
    fun engraveRune(runeId: String) = post {
        val current = engravedRunes()
        if (runeId in current || current.size >= runeSlots()) return@post
        if (!RuneEngraving.canEngrave(runeId, statsManager.counter(AchievementStat.runeCatches(runeId)).toInt())) return@post
        // Las grabadas que sobraban (sin espacio) se descartan al grabar una nueva
        statsManager.counters.keys.removeAll { it.startsWith(AchievementStat.ENGRAVE_PREFIX) && it.removePrefix(AchievementStat.ENGRAVE_PREFIX) !in current }
        val order = (statsManager.counters.filterKeys { it.startsWith(AchievementStat.ENGRAVE_PREFIX) }.values.maxOrNull() ?: BigDecimal.ZERO).add(BigDecimal.ONE)
        statsManager.counters[AchievementStat.engraved(runeId)] = order
        statsManager.flag(AchievementStat.ENGRAVED_EVER_PREFIX + runeId)
        soundManager.playSound(SoundType.BUY)
        refreshEngravings()
        recalculate()
        requestSave()
    }

    /** Quita una runa de su espacio. Es gratis: se puede volver a grabar cuando quieras. */
    fun removeEngraving(runeId: String) = post {
        if (statsManager.counters.remove(AchievementStat.engraved(runeId)) == null) return@post
        refreshEngravings()
        recalculate()
        requestSave()
    }

    private fun effectiveAttention(): Map<String, Int> =
        Attention.clamp(wantedAttention, attentionPoints())

    private fun attentionMultiplier(target: String): Double =
        Attention.multiplier(effectiveAttention()[target] ?: 0)

    private fun applyPactModifiers() {
        // Las runas: el pacto y la Atención puesta en la Vigilia
        val reward = trialReward()
        runeManager.masteryEnabled = consecrationRevealed()
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
        prestigeShopManager.getPrestigeGainMultiplier().multiply(BigDecimal.valueOf(
            pact.sanityGain * trialReward().sanityGain * Transcendence.sanityMultiplier(eternalLevel(EternalUpgrade.PRIMORDIAL_SANITY)) *
                Stars.sanityMultiplier(starLevel(StarUpgrade.STAR_SANITY))
        ))

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
        val trialProduction = BigDecimal.valueOf(
            rules.production * reward.production * deliriumMultiplier *
                Transcendence.productionMultiplier(eternalLevel(EternalUpgrade.ETERNAL_FIRE))
        ).multiply(Veil.productionFactor(knowledgeThisRun(), veilBroken()))
            .multiply(BigDecimal.valueOf(Stars.productionMultiplier(starLevel(StarUpgrade.STARLIGHT), starLevel(StarUpgrade.DARK_STARS), starCount())))

        // Potenciadores temporales (evento + rituales + runas): se SUMAN entre sí.
        // Los permanentes se siguen multiplicando.
        // Con el ritual Convergencia activo se MULTIPLICAN (con tope).
        val convergence = ritualManager.isConvergenceActive()
        val temporaryBuffs = listOf(eventScheduler.buffMultiplier) +
            ritualManager.getActiveProductionMultipliers() +
            runeManager.getActiveProductionMultipliers()
        val boostMult = BuffMath.combine(temporaryBuffs, convergence)
        // La Locura (bono y colapso) cuenta como temporal: no se extiende al tiempo sin conexión
        val temporaryMult = boostMult.multiply(BigDecimal.valueOf(madnessFactor()))
        if (convergence) {
            // Logros de la Convergencia: el mejor combo logrado
            statsManager.record(AchievementStat.MAX_CONVERGENCE, boostMult)
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
                // Consagración (eterna): x3 por nivel, también para la Sinapsis
                .multiply(BigDecimal.valueOf(Consecration.multiplier(consecrationLevel(building.id))))
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
                    // Presagio de la Locura: el edificio poseído no produce
                    .multiply(if (building.id == possessedBuildingId) BigDecimal.ZERO else BigDecimal.ONE)
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
            madness = if (madnessRevealed()) madness.value else null,
            transcendRevealed = transcendRevealed(),
            cycleAscensions = cycleAscensions(),
            etherGain = etherGainNow(),
            ether = statsManager.counter(AchievementStat.ETHER).toInt(),
            transcendences = statsManager.counter(AchievementStat.TRANSCENDENCES).toInt(),
            milestoneAscensions = milestoneAscensions(),
            transcendRequired = transcendRequired(),
            veilBroken = veilBroken(),
            veilPressing = Veil.isPressing(knowledgeThisRun(), veilBroken()),
            veilFactor = Veil.productionFactor(knowledgeThisRun(), veilBroken()),
            veilApproach = Veil.approach(knowledgeThisRun()),
            stars = starCount(),
            stardust = statsManager.counter(AchievementStat.STARDUST).toDouble(),
            starsPerHour = if (starsRevealed()) starRate() * 3600.0 else 0.0,
            dustPerSecond = if (starsRevealed()) Stars.dustPerSecond(starCount(), starsAligned()) else 0.0,
            starLevels = StarUpgrade.entries.associate { it.id to starLevel(it) },
            starsAligned = starsAligned(),
            alignmentSecondsLeft = Stars.secondsLeft(nowSeconds(), starLevel(StarUpgrade.CONJUNCTION)),
            beastLevels = beastLevels(),
            essence = statsManager.counter(AchievementStat.ESSENCE).toInt(),
            encounterId = encounter?.id,
            encounterSecondsLeft = encounterSecondsLeft,
            beastBonuses = Bestiary.bonuses(beastLevels()),
            eternalLevels = EternalUpgrade.entries.associate { it.id to eternalLevel(it) },
            consecrationRevealed = consecrationRevealed(),
            consecrations = buildingManager.availableBuildings.associate { it.id to consecrationLevel(it.id) }.filterValues { it > 0 },
            tide = currentTide(),
            tideSecondsLeft = TidePhase.secondsLeft(statsManager.totalPlayTimeSeconds),
            collapseSeconds = collapseSeconds,
            possessedBuilding = possessedBuildingId,
            mutationsRevealed = mutationsRevealed(),
            activeMutation = activeMutation(),
            mutationOffer = mutationOffer(),
            mutationsSeen = Mutation.entries.count { statsManager.counter(AchievementStat.MUTATION_SEEN_PREFIX + it.id).signum() > 0 },
            seenMutations = Mutation.entries.filter { statsManager.counter(AchievementStat.MUTATION_SEEN_PREFIX + it.id).signum() > 0 }.map { it.id }.toSet(),
            menuUnlocks = menuUnlocks(),
            newTabs = newTabs(),
            runeSlots = runeSlots(),
            engravedRunes = engravedRunes(),
            runeCatches = runeCatches(),
            runeMastery = if (consecrationRevealed()) RuneEngraving.ALL.associate {
                it.id to statsManager.counter(AchievementStat.mastery(it.id)).toInt()
            } else emptyMap(),
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
        private const val HIGH_MADNESS = 90.0
        private const val EDGE_MADNESS = 99.0
        private const val MADNESS_RECALC_STEP = 0.5
    }
}
