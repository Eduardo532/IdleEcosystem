package com.EGomez.idleecosystem.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.EGomez.idleecosystem.ui.MainViewModel
import com.EGomez.idleecosystem.ui.components.AchievementPopup
import com.EGomez.idleecosystem.ui.components.ParticleOverlay
import com.EGomez.idleecosystem.ui.components.ParticleSystem
import com.EGomez.idleecosystem.ui.components.ParticleType
import com.EGomez.idleecosystem.ui.screens.main.components.InfoSection
import com.EGomez.idleecosystem.ui.screens.main.components.MainTopBar
import com.EGomez.idleecosystem.ui.screens.main.components.MenuSection
import com.EGomez.idleecosystem.ui.screens.main.dialogs.*
import com.EGomez.idleecosystem.ui.screens.StatisticsScreen
import com.EGomez.idleecosystem.ui.theme.VoidBlack
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

data class FloatingText(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val color: Color = Color.White, // NUEVO: Soporte para color dinámico
    val x: Float = 0f,
    val y: Float = 0f,
    val animatable: Animatable<Float, androidx.compose.animation.core.AnimationVector1D> = Animatable(0f)
)

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    buildingCalculator: BuildingCalculator = BuildingCalculator()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp // Necesario para centrar partículas
    val currentDensity = LocalDensity.current
    val responsiveFontScale = ((screenWidth / 380f) * 0.75f).coerceIn(0.6f, 1.1f)

    // Conversión de coordenadas para el Canvas (Pixeles)
    val density = currentDensity.density
    val centerXPx = (screenWidth / 2f) * density
    val centerYPx = (screenHeight * 0.4f) * density // Un poco más arriba del centro geométrico

    CompositionLocalProvider(
        LocalDensity provides Density(density = currentDensity.density, fontScale = responsiveFontScale)
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val buildings by viewModel.buildingsState.collectAsState()
        val upgrades by viewModel.upgradesState.collectAsState()
        val achievements by viewModel.achievementsState.collectAsState()
        val rituals by viewModel.ritualsState.collectAsState()
        val showPurchased by viewModel.showPurchasedState.collectAsState()

        var showAchievementsDialog by remember { mutableStateOf(false) }
        var showPrestigeShopDialog by remember { mutableStateOf(false) }
        var showSettingsDialog by remember { mutableStateOf(false) }
        var showHardResetConfirmation by remember { mutableStateOf(false) }
        var showStatisticsDialog by remember { mutableStateOf(false) }

        var isMenuExpanded by remember { mutableStateOf(true) }
        val menuWeight by animateFloatAsState(
            targetValue = if (isMenuExpanded) 0.65f else 0.15f,
            label = "menuAnimation"
        )

        var selectedTabIndex by remember { mutableIntStateOf(0) }

        // --- SISTEMAS VISUALES ---
        val floatingTexts = remember { mutableStateListOf<FloatingText>() }
        val particleSystem = remember { ParticleSystem() } // Instancia del sistema de partículas
        val scope = rememberCoroutineScope()

        fun spawnFloatingText(value: String, color: Color = Color.White) {
            val newItem = FloatingText(text = value, color = color)
            floatingTexts.add(newItem)
            scope.launch {
                newItem.animatable.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 800))
                floatingTexts.remove(newItem)
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {

            // 1. CAPA DE PARTÍCULAS (Fondo dinámico sobre el negro, pero bajo la UI interactiva)
            ParticleOverlay(
                system = particleSystem,
                modifier = Modifier.fillMaxSize().zIndex(1f)
            )

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
                modifier = Modifier.zIndex(2f) // UI sobre partículas
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InfoSection(
                            knowledgeFormatted = uiState.knowledgeFormatted,
                            knowledgePerSecond = uiState.knowledgePerSecond,
                            globalMultiplier = uiState.globalMultiplier,
                            activeBuffMultiplier = uiState.activeBuffMultiplier,
                            buffTimeRemaining = uiState.buffTimeRemaining,
                            clickMultiplier = uiState.clickMultiplier,
                            onManualClick = {
                                // --- LÓGICA VISUAL DEL CLICK ---
                                val result = viewModel.onManualClick()

                                if (result.knowledgeGained > BigDecimal.ZERO) {
                                    spawnFloatingText("+${result.knowledgeGained.toBeautifulString()}", Color.White)
                                    // Explosión suave de conocimiento
                                    particleSystem.spawnExplosion(centerXPx, centerYPx, ParticleType.KNOWLEDGE)
                                }

                                if (result.echoFound) {
                                    // FEEDBACK ESPECIAL PARA ECOS
                                    spawnFloatingText("+1 ECO", Color(0xFF00E5FF)) // Cian
                                    // Explosión fuerte de Eco
                                    particleSystem.spawnExplosion(centerXPx, centerYPx, ParticleType.ECHO, count = 25)
                                }
                            },
                            modifier = Modifier.weight(1f - menuWeight)
                        )

                        // AQUÍ CONECTAMOS LA UI
                        MenuSection(
                            isMenuExpanded = isMenuExpanded,
                            onToggleMenu = { isMenuExpanded = !isMenuExpanded },
                            menuWeight = menuWeight,
                            selectedTabIndex = selectedTabIndex,
                            onTabSelected = { selectedTabIndex = it },
                            buyMultiplier = uiState.buyMultiplier,
                            onBuyMultiplierChanged = { viewModel.setBuyMultiplier(it) },
                            showPurchased = showPurchased,
                            onToggleShowPurchased = { viewModel.toggleShowPurchased() },
                            buildings = buildings,
                            upgrades = upgrades,
                            rituals = rituals,
                            currentKnowledge = uiState.rawKnowledge,

                            // --- CONEXIÓN DE ECOS ---
                            currentEchoes = uiState.echoes,

                            globalMultiplier = uiState.globalMultiplier,
                            buildingCalculator = buildingCalculator,
                            onBuyBuilding = { viewModel.onBuyBuilding(it) },
                            onBuyUpgrade = { viewModel.onBuyUpgrade(it) },
                            onActivateRitual = { viewModel.onActivateRitual(it) },
                            prestigeCurrencyFormatted = uiState.prestigeCurrencyFormatted,
                            canPrestige = uiState.canPrestige,
                            potentialPrestigeFormatted = uiState.potentialPrestigeFormatted,
                            onAscendClicked = { viewModel.onAscendClicked() },
                            onShowPrestigeShop = { showPrestigeShopDialog = true },
                            modifier = Modifier.weight(menuWeight)
                        )
                    }

                    // RENDERIZADO DE TEXTO FLOTANTE
                    floatingTexts.forEach { item ->
                        Text(
                            text = item.text,
                            color = item.color, // Usamos el color dinámico
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 220.dp)
                                .graphicsLayer {
                                    translationY = -150f * item.animatable.value
                                    alpha = 1f - item.animatable.value
                                    scaleX = 1f + (item.animatable.value * 0.2f) // Ligero zoom al subir
                                    scaleY = 1f + (item.animatable.value * 0.2f)
                                }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.lastUnlockedAchievement != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
                    .zIndex(10f)
            ) {
                uiState.lastUnlockedAchievement?.let { achievement ->
                    AchievementPopup(achievement = achievement)
                }
            }

            if (showStatisticsDialog) {
                StatisticsScreen(
                    statisticsManager = viewModel.statsManager,
                    onDismiss = { showStatisticsDialog = false }
                )
            }

            if (showHardResetConfirmation) {
                HardResetDialog(
                    onConfirm = {
                        viewModel.onHardResetClicked()
                        showHardResetConfirmation = false
                        showSettingsDialog = false
                    },
                    onDismiss = { showHardResetConfirmation = false }
                )
            }

            if (showSettingsDialog) {
                SettingsDialog(
                    uiState = uiState,
                    onDismiss = {
                        showSettingsDialog = false
                        viewModel.clearExportString()
                    },
                    onStatsClick = { showStatisticsDialog = true },
                    onToggleMusic = { viewModel.toggleMusic() },
                    onToggleSfx = { viewModel.toggleSfx() },
                    onExportClick = { viewModel.onExportSaveClicked() },
                    onImportClick = { viewModel.onConfirmImportClicked(); showSettingsDialog = false },
                    onCancelImport = { viewModel.clearExportString() },
                    onPasteClick = { viewModel.onPasteSaveClicked(it) },
                    onHardResetClick = { showHardResetConfirmation = true }
                )
            }

            if (showAchievementsDialog) {
                AchievementsDialog(
                    achievements = achievements,
                    achievementsProgress = uiState.achievementsProgress,
                    secretsProgress = uiState.secretsProgress,
                    onDismiss = { showAchievementsDialog = false }
                )
            }

            if (showPrestigeShopDialog) {
                PrestigeShopDialog(
                    upgrades = uiState.prestigeUpgrades,
                    currencyFormatted = uiState.prestigeCurrencyFormatted,
                    onBuy = { viewModel.onBuyPrestigeUpgrade(it) },
                    onDismiss = { showPrestigeShopDialog = false }
                )
            }

            if (uiState.showOfflineDialog) {
                OfflineGainsDialog(
                    timeFormatted = uiState.offlineTimeFormatted,
                    gainsFormatted = uiState.offlineGains,
                    onDismiss = { viewModel.dismissOfflineDialog() }
                )
            }

            if (uiState.showRefundDialog) {
                RefundDialog(
                    refundAmount = uiState.refundAmountString,
                    onDismiss = { viewModel.dismissRefundDialog() }
                )
            }

            uiState.currentEvent?.let { event ->
                EventDialog(
                    event = event,
                    resultText = uiState.eventResultText,
                    onOptionSelected = { viewModel.onEventOptionSelected(it) }
                )
            }
        }
    }
}