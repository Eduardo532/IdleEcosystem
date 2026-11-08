package com.EGomez.idleecosystem.ui.screens

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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
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

        var showAchievementsDialog by rememberSaveable { mutableStateOf(false) }
        var showPrestigeShopDialog by rememberSaveable { mutableStateOf(false) }
        var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
        var showHardResetConfirmation by rememberSaveable { mutableStateOf(false) }
        var showStatisticsDialog by rememberSaveable { mutableStateOf(false) }
        var showAscendConfirmation by rememberSaveable { mutableStateOf(false) }

        // --- CAMBIO AQUI: Iniciamos en 'false' (contraído) ---
        var isMenuExpanded by rememberSaveable { mutableStateOf(false) }

        // Animamos el peso: si está contraído es 0.15f (solo la barrita), si expandido 0.65f
        val menuWeight by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isMenuExpanded) 0.65f else 0.15f, label = "menu")
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

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

                        if (uiState.activeRuneBuffs.isNotEmpty()) {
                            ActiveBuffsDisplay(buffs = uiState.activeRuneBuffs)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        InfoSection(
                            knowledgeFormatted = uiState.knowledgeFormatted,
                            knowledgePerSecond = uiState.knowledgePerSecond,
                            globalMultiplier = uiState.globalMultiplier,
                            activeBuffMultiplier = uiState.activeBuffMultiplier,
                            buffTimeRemaining = uiState.buffTimeRemaining,
                            clickMultiplier = uiState.clickMultiplier,
                            onManualClick = { clickOffset, buttonCenter, buttonRadius ->
                                val result = viewModel.onManualClick()
                                if (result.knowledgeGained > BigDecimal.ZERO && uiState.isFloatingTextEnabled) {
                                    spawnFloatingText("+${result.knowledgeGained.toBeautifulString()}", Color.White, clickOffset.x, clickOffset.y - 80f)
                                }
                                if (result.echoFound && uiState.isFloatingTextEnabled) {
                                    spawnFloatingText("+1 ECO", Color(0xFF00E5FF), buttonCenter.x, buttonCenter.y - 120f)
                                }
                                if (uiState.areParticlesEnabled) {
                                    if (result.knowledgeGained > BigDecimal.ZERO) particleSystem.spawnExplosion(clickOffset.x, clickOffset.y, ParticleType.KNOWLEDGE, baseRadius = buttonRadius)
                                    if (result.echoFound) particleSystem.spawnExplosion(buttonCenter.x, buttonCenter.y, ParticleType.ECHO, count = 30, baseRadius = buttonRadius)
                                }
                            },
                            modifier = Modifier.weight(1f - menuWeight)
                        )

                        MenuSection(
                            isMenuExpanded = isMenuExpanded, onToggleMenu = { isMenuExpanded = !isMenuExpanded },
                            menuWeight = menuWeight, selectedTabIndex = selectedTabIndex,
                            onTabSelected = {
                                selectedTabIndex = it
                                viewModel.onMenuTabSelected(it)
                            },
                            buyMultiplier = uiState.buyMultiplier, onBuyMultiplierChanged = { viewModel.setBuyMultiplier(it) },
                            showPurchased = showPurchased, onToggleShowPurchased = { viewModel.toggleShowPurchased() },
                            buildings = buildings, upgrades = upgrades, rituals = rituals,
                            currentKnowledge = uiState.rawKnowledge, currentEchoes = uiState.echoes,
                            globalMultiplier = uiState.globalMultiplier, buildingCalculator = buildingCalculator,
                            buildingCostMultiplier = uiState.buildingCostMultiplier,
                            onBuyBuilding = { viewModel.onBuyBuilding(it) }, onBuyUpgrade = { viewModel.onBuyUpgrade(it) },
                            onActivateRitual = { viewModel.onActivateRitual(it) },
                            prestigeCurrencyFormatted = uiState.prestigeCurrencyFormatted, canPrestige = uiState.canPrestige,
                            potentialPrestigeFormatted = uiState.potentialPrestigeFormatted,
                            onAscendClicked = {
                                // Primero se confirma; el tutorial "El Ciclo" se muestra sobre la confirmación
                                showAscendConfirmation = true
                                viewModel.onAscensionDialogOpened()
                            },
                            onShowPrestigeShop = {
                                showPrestigeShopDialog = true
                                viewModel.onPrestigeShopOpened()
                            },
                            modifier = Modifier.weight(menuWeight)
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
                RuneOverlay(
                    activeRune = uiState.activeRune,
                    runeState = uiState.runeState,
                    onSuccess = { viewModel.onRuneInteractionSuccess() },
                    onFail = { },
                    modifier = Modifier.zIndex(50f)
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

            if (showStatisticsDialog) StatisticsScreen(viewModel.statsManager) { showStatisticsDialog = false }

            if (showAscendConfirmation) {
                AscendDialog(
                    sanityGainFormatted = uiState.potentialPrestigeFormatted,
                    onConfirm = {
                        viewModel.onAscendClicked()
                        showAscendConfirmation = false
                    },
                    onDismiss = { showAscendConfirmation = false }
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
                    onExportClick = { viewModel.onExportSaveClicked() },
                    onImportClick = { viewModel.onConfirmImportClicked(); showSettingsDialog = false },
                    onCancelImport = { viewModel.clearExportString() },
                    onPasteClick = { viewModel.onPasteSaveClicked(it) },
                    onHardResetClick = { showHardResetConfirmation = true },
                    onToggleTutorial = { viewModel.toggleTutorial(it) }
                )
            }

            if (showAchievementsDialog) AchievementsDialog(achievements, uiState.achievementsProgress, uiState.secretsProgress) { showAchievementsDialog = false }
            if (showPrestigeShopDialog) PrestigeShopDialog(uiState.prestigeUpgrades, uiState.prestigeCurrencyFormatted, { viewModel.onBuyPrestigeUpgrade(it) }) { showPrestigeShopDialog = false }
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

@Composable
fun ActiveBuffsDisplay(buffs: List<RuneBuff>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().height(30.dp)
    ) {
        items(buffs) { buff ->
            val remainingSeconds = ((buff.expirationTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)

            val labelText = when {
                buff.costReduction > 0 -> "-${(buff.costReduction * 100).toInt()}% Costos"
                buff.isAutoClick -> if(buff.multiplier > 1.0) "x${buff.multiplier.toInt()} + Auto" else "AutoClick"
                buff.echoChanceBonus > 0 -> "+Suerte Ecos"
                buff.multiplier > 1.0 -> "x${buff.multiplier.toInt()} Prod"
                else -> "Activo"
            }

            val borderColor = when {
                buff.costReduction > 0 -> Color(0xFF90CAF9)
                buff.isAutoClick -> Color(0xFFFFEB3B)
                else -> Color(0xFF00E5FF)
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$labelText: ${remainingSeconds}s",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}