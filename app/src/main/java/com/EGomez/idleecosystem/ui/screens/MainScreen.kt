package com.EGomez.idleecosystem.ui.screens

import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.Trial
import com.EGomez.idleecosystem.domain.model.Mutation
import com.EGomez.idleecosystem.domain.model.RuneVariant
import com.EGomez.idleecosystem.domain.logic.MenuTab
import com.EGomez.idleecosystem.domain.logic.RuneEngraving
import com.EGomez.idleecosystem.ui.components.RuneCaughtNotice
import com.EGomez.idleecosystem.ui.components.readableRuneColor
import com.EGomez.idleecosystem.ui.components.multiplierLabel
import com.EGomez.idleecosystem.ui.screens.main.components.EncounterBanner
import com.EGomez.idleecosystem.ui.screens.main.components.EtherColor
import com.EGomez.idleecosystem.ui.screens.main.components.color
import com.EGomez.idleecosystem.ui.screens.main.components.title
import com.EGomez.idleecosystem.ui.screens.main.components.CompactResearchButton
import androidx.compose.ui.geometry.Offset
import com.EGomez.idleecosystem.ui.screens.main.components.MadnessInfoDialog
import com.EGomez.idleecosystem.ui.screens.main.components.MadnessMeterBar
import com.EGomez.idleecosystem.ui.screens.main.components.TideIndicator
import com.EGomez.idleecosystem.ui.screens.main.components.TideInfoDialog
import androidx.compose.ui.platform.testTag
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.domain.logic.BuildingCatalog
import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import androidx.compose.material3.Surface
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import com.EGomez.idleecosystem.domain.logic.RuneBuff
import com.EGomez.idleecosystem.ui.MainViewModel
import com.EGomez.idleecosystem.ui.components.AchievementPopup
import com.EGomez.idleecosystem.ui.components.ParticleOverlay
import com.EGomez.idleecosystem.ui.components.ParticleSystem
import com.EGomez.idleecosystem.ui.components.ParticleType
import com.EGomez.idleecosystem.ui.components.RuneOverlay
import com.EGomez.idleecosystem.ui.components.TutorialOverlay
import com.EGomez.idleecosystem.ui.screens.main.components.InfoSection
import com.EGomez.idleecosystem.ui.screens.main.components.MainTopBar
import com.EGomez.idleecosystem.ui.screens.main.components.MenuSection
import com.EGomez.idleecosystem.ui.screens.main.dialogs.*
import com.EGomez.idleecosystem.ui.screens.StatisticsScreen
import com.EGomez.idleecosystem.ui.theme.VoidBlack
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random

data class FloatingText(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val color: Color = Color.White,
    val startX: Float = 0f,
    val startY: Float = 0f,
    val driftX: Float = Random.nextFloat() * 80f - 40f,
    val animatable: Animatable<Float, androidx.compose.animation.core.AnimationVector1D> = Animatable(0f)
)

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    buildingCalculator: BuildingCalculator = BuildingCalculator()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val currentDensity = LocalDensity.current
    val responsiveFontScale = ((screenWidth / 380f) * 0.75f).coerceIn(0.6f, 1.1f)
    // Se combina con el tamaño de texto que el usuario eligió en Ajustes del sistema.
    // Antes se reemplazaba y la opción de accesibilidad no tenía efecto. El tope evita
    // que los textos más grandes rompan el diseño.
    val finalFontScale = (responsiveFontScale * currentDensity.fontScale).coerceIn(0.6f, 1.5f)

    CompositionLocalProvider(LocalDensity provides Density(density = currentDensity.density, fontScale = finalFontScale)) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val buildings by viewModel.buildingsState.collectAsStateWithLifecycle()
        val upgrades by viewModel.upgradesState.collectAsStateWithLifecycle()
        val achievements by viewModel.achievementsState.collectAsStateWithLifecycle()
        val rituals by viewModel.ritualsState.collectAsStateWithLifecycle()
        val showPurchased by viewModel.showPurchasedState.collectAsStateWithLifecycle()
        // Se resuelve aquí: el clic se maneja fuera de la composición
        val floatingEchoText = stringResource(R.string.floating_echo)
        val activity = LocalContext.current as? Activity

        var showAchievementsDialog by rememberSaveable { mutableStateOf(false) }
        var showPrestigeShopDialog by rememberSaveable { mutableStateOf(false) }
        var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
        var showHardResetConfirmation by rememberSaveable { mutableStateOf(false) }
        var showStatisticsDialog by rememberSaveable { mutableStateOf(false) }
        var showAscendConfirmation by rememberSaveable { mutableStateOf(false) }
        var showAutomationDialog by rememberSaveable { mutableStateOf(false) }
        var showAttentionDialog by rememberSaveable { mutableStateOf(false) }
        // Ficha del Panteón abierta y pacto elegido para la próxima vida (por id)
        var pactDetailId by rememberSaveable { mutableStateOf<String?>(null) }
        var nextPactId by rememberSaveable { mutableStateOf<String?>(null) }
        var abandonPath by rememberSaveable { mutableStateOf(false) }
        var nextTrialId by rememberSaveable { mutableStateOf<String?>(null) }
        var nextMutationId by rememberSaveable { mutableStateOf<String?>(null) }
        var showMadnessInfo by rememberSaveable { mutableStateOf(false) }
        var showTideInfo by rememberSaveable { mutableStateOf(false) }
        // Nombres de TODOS los edificios (los pactos nombran también a los que aún no se ven)
        val buildingNames = remember { BuildingCatalog.create().associate { it.id to it.name } }

        // --- CAMBIO AQUI: Iniciamos en 'false' (contraído) ---
        var isMenuExpanded by rememberSaveable { mutableStateOf(false) }

        // Animamos el peso: si está contraído es 0.15f (solo la barrita), si expandido 0.65f
        val menuWeight by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isMenuExpanded) 0.65f else 0.15f, label = "menu")
        // Pestaña elegida (por id): si todavía no se ve, se muestra Investigación
        var selectedTabId by rememberSaveable { mutableStateOf(MenuTab.RESEARCH.id) }
        val selectedTab = uiState.menuUnlocks.resolve(MenuTab.fromId(selectedTabId))
        // Runa recién capturada: un aviso con su nombre y lo que dio
        var caughtRune by remember { mutableStateOf<RuneVariant?>(null) }
        // Sistema recién descubierto: un aviso breve con su nombre
        var unlockNotice by remember { mutableStateOf<MenuTab?>(null) }
        var knownNewTabs by remember { mutableStateOf<Set<MenuTab>?>(null) }
        LaunchedEffect(uiState.newTabs) {
            val previous = knownNewTabs
            if (previous != null) {
                (uiState.newTabs - previous).firstOrNull()?.let { unlockNotice = it }
            }
            knownNewTabs = uiState.newTabs
        }
        LaunchedEffect(unlockNotice) {
            if (unlockNotice != null) {
                delay(NOTICE_MS)
                unlockNotice = null
            }
        }
        LaunchedEffect(caughtRune) {
            if (caughtRune != null) {
                delay(NOTICE_MS)
                caughtRune = null
            }
        }

        val floatingTexts = remember { mutableStateListOf<FloatingText>() }
        val particleSystem = remember { ParticleSystem() }
        val scope = rememberCoroutineScope()

        fun spawnFloatingText(value: String, color: Color, x: Float, y: Float) {
            if (!uiState.isFloatingTextEnabled) return
            val newItem = FloatingText(text = value, color = color, startX = x, startY = y)
            floatingTexts.add(newItem)
            scope.launch {
                newItem.animatable.animateTo(targetValue = 1f, animationSpec = tween(1000))
                floatingTexts.remove(newItem)
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {

            // --- CAPA 0: Partículas ---
            ParticleOverlay(
                system = particleSystem,
                isParticlesEnabled = uiState.areParticlesEnabled,
                modifier = Modifier.fillMaxSize().zIndex(0f)
            )

            // --- CAPA 1: UI Principal ---
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    MainTopBar(
                        isMenuExpanded = isMenuExpanded,
                        menuWeight = menuWeight,
                        achievements = achievements,
                        achievementsProgress = uiState.achievementsProgress,
                        nextGoalProgress = uiState.nextGoal?.progress,
                        knowledgeFormatted = uiState.knowledgeFormatted,
                        knowledgePerSecond = uiState.knowledgePerSecond,
                        // Las estadísticas aparecen con el Grimorio; los logros, con el primero
                        showStats = uiState.menuUnlocks.has(MenuTab.GRIMOIRE),
                        showAchievements = achievements.any { it.isUnlocked },
                        onSettingsClick = { showSettingsDialog = true },
                        onStatsClick = { showStatisticsDialog = true },
                        onAchievementsClick = { showAchievementsDialog = true }
                    )
                },
                modifier = Modifier.zIndex(1f)
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // El clic manual (INVESTIGAR grande o compacto)
                        val onManualClick: (Offset, Offset, Float) -> Unit = { clickOffset, buttonCenter, buttonRadius ->
                                val result = viewModel.onManualClick()
                                if (result.knowledgeGained > BigDecimal.ZERO && uiState.isFloatingTextEnabled) {
                                    spawnFloatingText("+${result.knowledgeGained.toBeautifulString()}", Color.White, clickOffset.x, clickOffset.y - 80f)
                                }
                                if (result.echoFound && uiState.isFloatingTextEnabled) {
                                    spawnFloatingText(floatingEchoText, Color(0xFF00E5FF), buttonCenter.x, buttonCenter.y - 120f)
                                }
                                if (uiState.areParticlesEnabled) {
                                    if (result.knowledgeGained > BigDecimal.ZERO) particleSystem.spawnExplosion(clickOffset.x, clickOffset.y, ParticleType.KNOWLEDGE, baseRadius = buttonRadius)
                                    if (result.echoFound) particleSystem.spawnExplosion(buttonCenter.x, buttonCenter.y, ParticleType.ECHO, count = 30, baseRadius = buttonRadius)
                                }
                            }

                        if (isMenuExpanded) {
                            // Cabecera compacta (v0.18.2): potenciadores, Locura, Marea e INVESTIGAR
                            // en dos filas finas; el Conocimiento va en la barra superior
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (uiState.activeRuneBuffs.isNotEmpty()) ActiveBuffsDisplay(buffs = uiState.activeRuneBuffs)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                CompactResearchButton(uiState.clickMultiplier, onManualClick)
                            }
                            if (uiState.madness != null || uiState.tide != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                    uiState.madness?.let { value ->
                                        MadnessMeterBar(value, uiState.collapseSeconds, uiState.possessedBuilding, Modifier.weight(1f), compact = true) { showMadnessInfo = true }
                                    }
                                    if (uiState.madness != null && uiState.tide != null) Spacer(modifier = Modifier.width(8.dp))
                                    uiState.tide?.let { tide ->
                                        TideIndicator(tide, uiState.tideSecondsLeft, Modifier.weight(1f), compact = true) { showTideInfo = true }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        } else {
                            if (uiState.activeRuneBuffs.isNotEmpty()) {
                                ActiveBuffsDisplay(buffs = uiState.activeRuneBuffs)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Medidor de Locura (desde la quinta Ascensión)
                            uiState.madness?.let { value ->
                                MadnessMeterBar(value, uiState.collapseSeconds, uiState.possessedBuilding) { showMadnessInfo = true }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            // Fases de la Marea (desde la décima Ascensión)
                            uiState.tide?.let { tide ->
                                TideIndicator(tide, uiState.tideSecondsLeft) { showTideInfo = true }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            InfoSection(
                                knowledgeFormatted = uiState.knowledgeFormatted,
                                knowledgePerSecond = uiState.knowledgePerSecond,
                                globalMultiplier = uiState.globalMultiplier,
                                activeBuffMultiplier = uiState.activeBuffMultiplier,
                                buffTimeRemaining = uiState.buffTimeRemaining,
                                clickMultiplier = uiState.clickMultiplier,
                                onManualClick = onManualClick,
                                modifier = Modifier.weight(1f - menuWeight)
                            )
                        }

                        MenuSection(
                            isMenuExpanded = isMenuExpanded, onToggleMenu = { isMenuExpanded = !isMenuExpanded },
                            menuWeight = menuWeight, selectedTab = selectedTab,
                            onTabSelected = {
                                selectedTabId = it.id
                                viewModel.onMenuTabSelected(it)
                            },
                            buyMultiplier = uiState.buyMultiplier, onBuyMultiplierChanged = { viewModel.setBuyMultiplier(it) },
                            showPurchased = showPurchased, onToggleShowPurchased = { viewModel.toggleShowPurchased() },
                            buildings = buildings, upgrades = upgrades, rituals = rituals,
                            currentKnowledge = uiState.rawKnowledge, currentEchoes = uiState.echoes,
                            globalMultiplier = uiState.globalMultiplier, buildingCalculator = buildingCalculator,
                            buildingCostMultiplier = uiState.buildingCostMultiplier,
                            buildingMilestones = viewModel.buildingMilestones,
                            onBuyBuilding = { viewModel.onBuyBuilding(it) }, onBuyUpgrade = { viewModel.onBuyUpgrade(it) },
                            onBuyAllUpgrades = { viewModel.onBuyAllUpgrades() },
                            onActivateRitual = { viewModel.onActivateRitual(it) },
                            prestigeCurrencyFormatted = uiState.prestigeCurrencyFormatted, canPrestige = uiState.canPrestige,
                            potentialPrestigeFormatted = uiState.potentialPrestigeFormatted,
                            doubleSanityAtFormatted = uiState.doubleSanityAtFormatted,
                            unlocks = uiState.menuUnlocks,
                            newTabs = uiState.newTabs,
                            ascensions = uiState.ascensions,
                            milestoneAscensions = uiState.milestoneAscensions,
                            automation = uiState.automation,
                            onToggleAutoBuildings = { viewModel.toggleAutoBuildings() },
                            onToggleAutoGrimoire = { viewModel.toggleAutoGrimoire() },
                            onToggleAutoRitual = { viewModel.toggleAutoRitual(it) },
                            onOpenAutomation = { showAutomationDialog = true },
                            activePact = uiState.activePact,
                            chaosMultiplier = uiState.chaosMultiplier,
                            onPactSelected = { pactDetailId = it.id },
                            attention = uiState.attention,
                            attentionPoints = uiState.attentionPoints,
                            onOpenAttention = { showAttentionDialog = true },
                            activeTrial = uiState.activeTrial,
                            trialProgress = uiState.trialProgress,
                            completedTrials = uiState.completedTrials,
                            deliriumMultiplier = uiState.deliriumMultiplier,
                            earlyPacts = uiState.earlyPacts,
                            onAbandonTrial = { viewModel.onAbandonTrial() },
                            veilBroken = uiState.veilBroken,
                            veilPressing = uiState.veilPressing,
                            veilFactor = uiState.veilFactor,
                            onBreakVeil = { viewModel.breakVeil() },
                            starsUi = uiState.starsUi,
                            bestiaryUi = uiState.bestiaryUi,
                            labLevels = uiState.labLevels,
                            onBuyLab = { viewModel.buyLab(it) },
                            dreamUi = uiState.dreamUi,
                            onSetDreamNext = { viewModel.setDreamNext(it) },
                            onWakeUp = { viewModel.wakeUp() },
                            onBuyDreamNode = { viewModel.buyDreamNode(it) },
                            onRespecDream = { viewModel.respecDream() },
                            onSummon = { viewModel.summon(it) },
                            onBuySeals = { viewModel.buySeals() },
                            onPickWithShards = { viewModel.pickWithShards(it) },
                            onBuyStar = { viewModel.buyStarUpgrade(it) },
                            runeSlots = uiState.runeSlots,
                            engravedRunes = uiState.engravedRunes,
                            runeCatches = uiState.runeCatches,
                            onEngraveRune = { viewModel.onEngraveRune(it) },
                            onRemoveEngraving = { viewModel.onRemoveEngraving(it) },
                            runeMastery = uiState.runeMastery,
                            activeMutation = uiState.activeMutation,
                            seenMutations = uiState.seenMutations,
                            cycleAscensions = uiState.cycleAscensions,
                            etherGain = uiState.etherGain,
                            ether = uiState.ether,
                            eternalLevels = uiState.eternalLevels,
                            transcendences = uiState.transcendences,
                            transcendRequired = uiState.transcendRequired,
                            onBuyEternal = { viewModel.onBuyEternal(it) },
                            onTranscend = { viewModel.onTranscend() },
                            consecrationRevealed = uiState.consecrationRevealed,
                            consecrations = uiState.consecrations,
                            onConsecrate = { viewModel.onConsecrate(it) },
                            onAscendClicked = {
                                // Primero se confirma; el tutorial "El Ciclo" se muestra sobre la confirmación
                                showAscendConfirmation = true
                                viewModel.onAscensionDialogOpened()
                            },
                            onOpenMadnessMap = {
                                showPrestigeShopDialog = true
                                viewModel.onPrestigeShopOpened()
                            },
                            // Con el menú abierto, la lista ocupa todo lo que deja la cabecera compacta
                            modifier = if (isMenuExpanded) Modifier.weight(1f) else Modifier.weight(menuWeight)
                        )
                    }

                    // Bestiario (v0.19.8): la criatura del encuentro flota arriba de todo
                    uiState.bestiaryUi.encounterId?.let { id ->
                        EncounterBanner(
                            beastId = id,
                            secondsLeft = uiState.bestiaryUi.encounterSecondsLeft,
                            onCatch = { viewModel.catchEncounter() },
                            modifier = Modifier.align(Alignment.TopCenter).padding(paddingValues).padding(top = 4.dp).zIndex(150f)
                        )
                    }

                    if (floatingTexts.isNotEmpty()) {
                        floatingTexts.forEach { item ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = item.text, color = item.color.copy(alpha = 1f - item.animatable.value),
                                    fontWeight = FontWeight.Bold, fontSize = 24.sp,
                                    modifier = Modifier.graphicsLayer {
                                        translationX = item.startX + (item.driftX * item.animatable.value)
                                        translationY = item.startY - (150f * item.animatable.value)
                                    }.zIndex(200f)
                                )
                            }
                        }
                    }
                }
            }

            // --- CAPA 2: RUNAS (OPTIMIZADA) ---
            if (uiState.activeRune != null) {
                val rune = uiState.activeRune
                RuneOverlay(
                    activeRune = rune,
                    runeState = uiState.runeState,
                    onSuccess = {
                        caughtRune = rune
                        viewModel.onRuneInteractionSuccess()
                    },
                    onFail = { },
                    modifier = Modifier.zIndex(50f),
                    masteryLevel = runeMasteryLevel(rune, uiState.runeMastery)
                )
            }

            // --- CAPA 3: Popups y Dialogos ---
            AnimatedVisibility(
                visible = uiState.lastUnlockedAchievement != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp).zIndex(100f)
            ) {
                uiState.lastUnlockedAchievement?.let { AchievementPopup(achievement = it) }
            }

            if (showStatisticsDialog) {
                // Se recolecta solo con el diálogo abierto: no recompone la pantalla principal
                val statistics by viewModel.statisticsState.collectAsStateWithLifecycle()
                StatisticsScreen(statistics) { showStatisticsDialog = false }
            }

            if (showAscendConfirmation) {
                AscendDialog(
                    sanityGainFormatted = uiState.potentialPrestigeFormatted,
                    pacing = AscendPacing(
                        generated = uiState.knowledgeThisRunFormatted,
                        nextGain = uiState.nextSanityGainFormatted,
                        nextAt = uiState.nextSanityAtFormatted,
                        doubleGain = uiState.doubleSanityGainFormatted,
                        doubleAt = uiState.doubleSanityAtFormatted
                    ),
                    ascensions = uiState.ascensions,
                    pacts = Pact.available(uiState.ascensions + 1, uiState.earlyPacts),
                    selectedPact = Pact.fromId(nextPactId ?: uiState.activePact.id),
                    onPactSelected = { nextPactId = it.id },
                    buildingNames = buildingNames,
                    activePath = MadnessPaths.active(uiState.prestigeUpgrades),
                    abandonPath = abandonPath,
                    onAbandonPathChanged = { abandonPath = it },
                    trials = Trial.entries.filter { it.isUnlocked(uiState.ascensions + 1) },
                    completedTrials = uiState.completedTrials,
                    selectedTrial = Trial.fromId(nextTrialId),
                    onTrialSelected = { nextTrialId = it?.id },
                    mutationOffer = uiState.mutationOffer,
                    selectedMutation = Mutation.fromId(nextMutationId)?.takeIf { it in uiState.mutationOffer },
                    onMutationSelected = { nextMutationId = it?.id },
                    onConfirm = {
                        viewModel.onAscendClicked(
                            Pact.fromId(nextPactId ?: uiState.activePact.id), abandonPath, Trial.fromId(nextTrialId),
                            Mutation.fromId(nextMutationId)?.takeIf { it in uiState.mutationOffer }
                        )
                        nextPactId = null
                        nextTrialId = null
                        nextMutationId = null
                        abandonPath = false
                        showAscendConfirmation = false
                    },
                    onDismiss = { showAscendConfirmation = false; abandonPath = false }
                )
            }

            if (showHardResetConfirmation) HardResetDialog({ viewModel.onHardResetClicked(); showHardResetConfirmation = false; showSettingsDialog = false }, { showHardResetConfirmation = false })

            if (showSettingsDialog) {
                SettingsDialog(
                    uiState = uiState,
                    onDismiss = { showSettingsDialog = false; viewModel.clearExportString() },
                    onStatsClick = { showStatisticsDialog = true },
                    onToggleMusic = { viewModel.toggleMusic() },
                    onToggleSfx = { viewModel.toggleSfx() },
                    onToggleParticles = { viewModel.toggleParticles() },
                    onToggleFloatingText = { viewModel.toggleFloatingText() },
                    onNotationSelected = { viewModel.setNumberNotation(it) },
                    onLanguageSelected = { language ->
                        if (language != uiState.language) {
                            viewModel.setLanguage(language)
                            // Se recrea la Activity para cargar los textos del idioma nuevo;
                            // los diálogos abiertos y la sección se conservan (rememberSaveable)
                            activity?.recreate()
                        }
                    },
                    onExportClick = { viewModel.onExportSaveClicked() },
                    onImportClick = { viewModel.onConfirmImportClicked(); showSettingsDialog = false },
                    onCancelImport = { viewModel.clearExportString() },
                    onPasteClick = { viewModel.onPasteSaveClicked(it) },
                    onHardResetClick = { showHardResetConfirmation = true },
                    onToggleTutorial = { viewModel.toggleTutorial(it) }
                )
            }

            pactDetailId?.let { id ->
                val pact = Pact.fromId(id)
                PactDetailDialog(
                    pact = pact,
                    active = pact == uiState.activePact,
                    buildingNames = buildingNames,
                    onDismiss = { pactDetailId = null }
                )
            }

            if (showAttentionDialog) {
                AttentionDialog(
                    allocation = uiState.attention,
                    points = uiState.attentionPoints,
                    ascensions = uiState.ascensions,
                    buildings = buildings,
                    onAdd = { viewModel.addAttention(it) },
                    onRemove = { viewModel.removeAttention(it) },
                    onClear = { viewModel.clearAttention() },
                    onDismiss = { showAttentionDialog = false }
                )
            }

            if (showAutomationDialog) {
                AutomationDialog(
                    settings = uiState.automation,
                    ascensions = uiState.milestoneAscensions,
                    buildings = buildings,
                    rituals = rituals,
                    onBudgetSelected = { viewModel.setAutomationBudget(it) },
                    onToggleBuildings = { viewModel.toggleAutoBuildings() },
                    onToggleBuildingExcluded = { viewModel.toggleBuildingAutobuy(it) },
                    onToggleGrimoire = { viewModel.toggleAutoGrimoire() },
                    onToggleAutoAscend = { viewModel.toggleAutoAscend() },
                    onRitualSelected = { viewModel.selectAutoRitual(it) },
                    onDismiss = { showAutomationDialog = false },
                    transcendences = uiState.transcendences,
                    onToggleAutoTranscend = { viewModel.toggleAutoTranscend() }
                )
            }

            // Aviso al Trascender
            // Bestiario: lo que salió al invocar y los Sellos encontrados
            uiState.summonResults?.let { results ->
                com.EGomez.idleecosystem.ui.screens.main.components.SummonResultsDialog(results, onDismiss = { viewModel.dismissSummonResults() })
            }
            uiState.sealNotice?.let { notice ->
                Surface(
                    color = Color(0xFF101A19),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, com.EGomez.idleecosystem.ui.screens.main.components.SealTeal),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 190.dp)
                        .zIndex(5f)
                ) {
                    com.EGomez.idleecosystem.ui.components.IconLabel(
                        com.EGomez.idleecosystem.ui.components.GameIcons.ElderSeal,
                        when (notice.source) {
                            com.EGomez.idleecosystem.domain.engine.SealSource.CLICK -> stringResource(R.string.seals_found_click)
                            com.EGomez.idleecosystem.domain.engine.SealSource.DAILY -> stringResource(R.string.seals_found_daily, notice.amount)
                            com.EGomez.idleecosystem.domain.engine.SealSource.ENCOUNTER -> stringResource(R.string.seals_found_encounter, notice.amount)
                        },
                        com.EGomez.idleecosystem.ui.screens.main.components.SealTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            uiState.transcendNotice?.let { gain ->
                Surface(
                    color = Color(0xFF12161C),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, EtherColor),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 140.dp)
                        .zIndex(5f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.transcend_notice, gain),
                            color = EtherColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        uiState.dreamNotice?.let { points ->
                            Text(
                                stringResource(R.string.dream_notice, points),
                                color = com.EGomez.idleecosystem.ui.screens.main.components.DreamBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Runa capturada: su nombre y lo que dio
            caughtRune?.let { rune ->
                RuneCaughtNotice(
                    rune = rune,
                    masteryLevel = runeMasteryLevel(rune, uiState.runeMastery),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp).zIndex(60f)
                )
            }

            // Un sistema nuevo: su nombre, en el color de su capa
            unlockNotice?.let { tab ->
                Surface(
                    color = Color(0xFF15121C),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, tab.layer.color()),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 110.dp)
                        .zIndex(61f)
                        .testTag(UNLOCK_NOTICE_TAG)
                ) {
                    Text(
                        stringResource(R.string.unlock_notice, tab.title()),
                        color = tab.layer.color(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            if (showTideInfo) {
                uiState.tide?.let { TideInfoDialog(it) { showTideInfo = false } } ?: run { showTideInfo = false }
            }

            if (showMadnessInfo) {
                MadnessInfoDialog(uiState.madness ?: 0.0) { showMadnessInfo = false }
            }

            // Presagios y colapso de la Locura
            uiState.madnessNotice?.let { notice ->
                val text = when {
                    notice.collapse -> stringResource(R.string.madness_notice_collapse)
                    notice.possessedBuilding != null -> stringResource(R.string.madness_notice_possessed, tr(notice.possessedBuilding))
                    else -> stringResource(R.string.madness_notice_echoes, notice.echoesLost ?: "0")
                }
                Surface(
                    color = Color(0xFF2A0A0A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD50000)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 190.dp)
                        .zIndex(5f)
                        .testTag(MADNESS_NOTICE_TAG)
                ) {
                    Text(
                        text,
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Prueba de Locura superada
            uiState.trialNotice?.let { trial ->
                Surface(
                    color = Color(0xFF2A1218),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCF6679)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 140.dp)
                        .zIndex(5f)
                        .testTag(TRIAL_NOTICE_TAG)
                ) {
                    Text(
                        stringResource(R.string.trial_completed_notice, tr(trial.title)),
                        color = Color(0xFFCF6679),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            uiState.autoAscensionNotice?.let { gain ->
                Surface(
                    color = Color(0xFF2A1A2E),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AncientGold),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 96.dp)
                        .zIndex(5f)
                ) {
                    Text(
                        stringResource(R.string.auto_ascended, gain),
                        color = AncientGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            if (showAchievementsDialog) {
                AchievementsDialog(
                    achievements = achievements,
                    achievementsProgress = uiState.achievementsProgress,
                    secretsProgress = if (uiState.secretsFound > 0) stringResource(R.string.secrets_progress, uiState.secretsFound, uiState.secretsTotal) else null,
                    nextGoal = uiState.nextGoal,
                    setsCompleted = uiState.setsCompleted,
                    setsTotal = uiState.setsTotal,
                    onDismiss = { showAchievementsDialog = false }
                )
            }
            if (showPrestigeShopDialog) PrestigeShopDialog(uiState.prestigeUpgrades, uiState.prestigeCurrencyFormatted, uiState.rawSanity, { viewModel.onBuyPrestigeUpgrade(it) }) { showPrestigeShopDialog = false }
            if (uiState.showOfflineDialog) OfflineGainsDialog(uiState.offlineTimeFormatted, uiState.offlineGains) { viewModel.dismissOfflineDialog() }
            if (uiState.showRefundDialog) RefundDialog(uiState.refundAmountString) { viewModel.dismissRefundDialog() }
            uiState.currentEvent?.let { EventDialog(it, uiState.eventResultText) { opt -> viewModel.onEventOptionSelected(opt) } }

            // --- CAPA 4: TUTORIAL (OPTIMIZADA) ---
            if (uiState.currentTutorialStep != null) {
                TutorialOverlay(
                    currentStep = uiState.currentTutorialStep,
                    onDismiss = { viewModel.dismissTutorial() }
                )
            }
        }
    }
}

/** Nivel de maestría de una runa (0 si la maestría todavía no se ve). */
fun runeMasteryLevel(rune: RuneVariant?, mastery: Map<String, Int>): Int =
    rune?.let { r -> mastery[r.id]?.let { RuneEngraving.masteryLevel(r.tier, it) } } ?: 0

/**
 * Los potenciadores activos (v0.19.0): cada chip lleva el color y el nombre de su runa,
 * lo que da, el tiempo que queda y una barra que se vacía. La lista solo se publica
 * cuando cambia (al empezar o terminar un efecto), así que el reloj vive aquí.
 */
@Composable
fun ActiveBuffsDisplay(buffs: List<RuneBuff>, clock: () -> Long = System::currentTimeMillis) {
    val now by produceState(clock()) {
        while (true) {
            value = clock()
            delay(BUFF_TICK_MS)
        }
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(buffs) { buff ->
            // Redondeo hacia arriba: muestra "20s" al empezar y "1s" en el último segundo
            val remainingMs = (buff.expirationTime - now).coerceAtLeast(0)
            val remainingSeconds = (remainingMs + 999) / 1000
            val rune = RuneEngraving.variant(buff.id.removeSuffix("_auto"))
            val accent = rune?.let { readableRuneColor(it.color) } ?: when {
                buff.costReduction > 0 -> Color(0xFF90CAF9)
                buff.isAutoClick -> Color(0xFFFFEB3B)
                else -> Color(0xFF00E5FF)
            }
            val effect = when {
                buff.freezesBuffs -> stringResource(R.string.buff_short_freeze)
                buff.costReduction > 0 -> stringResource(R.string.buff_short_costs, (buff.costReduction * 100).toInt())
                buff.echoChanceBonus > 0 -> stringResource(R.string.buff_short_echo, (buff.echoChanceBonus * 100).toInt())
                buff.multiplier > 1.0 -> stringResource(R.string.buff_short_production, multiplierLabel(buff.multiplier))
                buff.clickMultiplier > 1.0 -> stringResource(R.string.buff_short_click, multiplierLabel(buff.clickMultiplier))
                buff.isAutoClick -> stringResource(R.string.buff_short_auto)
                else -> stringResource(R.string.buff_active)
            }
            val fraction = if (buff.durationMs > 0) (remainingMs.toFloat() / buff.durationMs).coerceIn(0f, 1f) else null
            Surface(
                color = Color(0xFF1C1A22),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
                modifier = Modifier.testTag(BUFF_CHIP_TAG + buff.id)
            ) {
                Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 3.dp, bottom = 2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(accent, androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            rune?.let { tr(it.name) } ?: stringResource(R.string.buff_active),
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        "$effect · ${remainingSeconds}s",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (fraction != null) {
                        Box(modifier = Modifier.padding(top = 1.dp).width(60.dp).height(2.dp).background(Color.White.copy(alpha = 0.1f))) {
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction).background(accent))
                        }
                    }
                }
            }
        }
    }
}

/** Cada cuánto se refresca la cuenta regresiva de las runas. */
private const val BUFF_TICK_MS = 250L

const val TRIAL_NOTICE_TAG = "trial_notice"
const val UNLOCK_NOTICE_TAG = "unlock_notice"
const val BUFF_CHIP_TAG = "buff_chip_"
/** Cuánto duran los avisos de runa capturada y de sistema nuevo. */
private const val NOTICE_MS = 2_800L
const val MADNESS_NOTICE_TAG = "madness_notice"
