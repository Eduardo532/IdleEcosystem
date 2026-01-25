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

    CompositionLocalProvider(LocalDensity provides Density(density = currentDensity.density, fontScale = responsiveFontScale)) {
        val uiState by viewModel.uiState.collectAsState()
        val buildings by viewModel.buildingsState.collectAsState()
        val upgrades by viewModel.upgradesState.collectAsState()
        val achievements by viewModel.achievementsState.collectAsState()
        val rituals by viewModel.ritualsState.collectAsState()
        val showPurchased by viewModel.showPurchasedState.collectAsState()

        // Dialog states...
        var showAchievementsDialog by remember { mutableStateOf(false) }
        var showPrestigeShopDialog by remember { mutableStateOf(false) }
        var showSettingsDialog by remember { mutableStateOf(false) }
        var showHardResetConfirmation by remember { mutableStateOf(false) }
        var showStatisticsDialog by remember { mutableStateOf(false) }

        // Menu Animation
        var isMenuExpanded by remember { mutableStateOf(true) }
        val menuWeight by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isMenuExpanded) 0.65f else 0.15f, label = "menu")
        var selectedTabIndex by remember { mutableIntStateOf(0) }

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

            // CAPA 0: Partículas (Fondo)
            ParticleOverlay(
                system = particleSystem,
                isParticlesEnabled = uiState.areParticlesEnabled,
                modifier = Modifier.fillMaxSize().zIndex(0f)
            )

            // CAPA 1: UI Principal
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
                        // --- NUEVO: BARRA DE BUFFS ACTIVOS ---
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
                            menuWeight = menuWeight, selectedTabIndex = selectedTabIndex, onTabSelected = { selectedTabIndex = it },
                            buyMultiplier = uiState.buyMultiplier, onBuyMultiplierChanged = { viewModel.setBuyMultiplier(it) },
                            showPurchased = showPurchased, onToggleShowPurchased = { viewModel.toggleShowPurchased() },
                            buildings = buildings, upgrades = upgrades, rituals = rituals,
                            currentKnowledge = uiState.rawKnowledge, currentEchoes = uiState.echoes,
                            globalMultiplier = uiState.globalMultiplier, buildingCalculator = buildingCalculator,
                            onBuyBuilding = { viewModel.onBuyBuilding(it) }, onBuyUpgrade = { viewModel.onBuyUpgrade(it) },
                            onActivateRitual = { viewModel.onActivateRitual(it) },
                            prestigeCurrencyFormatted = uiState.prestigeCurrencyFormatted, canPrestige = uiState.canPrestige,
                            potentialPrestigeFormatted = uiState.potentialPrestigeFormatted, onAscendClicked = { viewModel.onAscendClicked() },
                            onShowPrestigeShop = { showPrestigeShopDialog = true },
                            modifier = Modifier.weight(menuWeight)
                        )
                    }

                    // Texto Flotante
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

            // CAPA 2: RUNAS (Z-Index ALTO para que se vean y se toquen)
            RuneOverlay(
                activeRune = uiState.activeRune,
                runeState = uiState.runeState,
                onSuccess = { viewModel.onRuneInteractionSuccess() },
                onFail = { },
                modifier = Modifier.zIndex(50f) // CORRECCIÓN CLAVE
            )

            // CAPA 3: Popups y Dialogos
            AnimatedVisibility(
                visible = uiState.lastUnlockedAchievement != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp).zIndex(100f)
            ) {
                uiState.lastUnlockedAchievement?.let { AchievementPopup(achievement = it) }
            }

            // Dialogs
            if (showStatisticsDialog) StatisticsScreen(viewModel.statsManager) { showStatisticsDialog = false }
            if (showHardResetConfirmation) HardResetDialog({ viewModel.onHardResetClicked(); showHardResetConfirmation = false; showSettingsDialog = false }, { showHardResetConfirmation = false })
            if (showSettingsDialog) SettingsDialog(uiState, { showSettingsDialog = false; viewModel.clearExportString() }, { showStatisticsDialog = true }, { viewModel.toggleMusic() }, { viewModel.toggleSfx() }, { viewModel.toggleParticles() }, { viewModel.toggleFloatingText() }, { viewModel.onExportSaveClicked() }, { viewModel.onConfirmImportClicked(); showSettingsDialog = false }, { viewModel.clearExportString() }, { viewModel.onPasteSaveClicked(it) }, { showHardResetConfirmation = true })
            if (showAchievementsDialog) AchievementsDialog(achievements, uiState.achievementsProgress, uiState.secretsProgress) { showAchievementsDialog = false }
            if (showPrestigeShopDialog) PrestigeShopDialog(uiState.prestigeUpgrades, uiState.prestigeCurrencyFormatted, { viewModel.onBuyPrestigeUpgrade(it) }) { showPrestigeShopDialog = false }
            if (uiState.showOfflineDialog) OfflineGainsDialog(uiState.offlineTimeFormatted, uiState.offlineGains) { viewModel.dismissOfflineDialog() }
            if (uiState.showRefundDialog) RefundDialog(uiState.refundAmountString) { viewModel.dismissRefundDialog() }
            uiState.currentEvent?.let { EventDialog(it, uiState.eventResultText) { opt -> viewModel.onEventOptionSelected(opt) } }
        }
    }
}

@Composable
fun ActiveBuffsDisplay(buffs: List<RuneBuff>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().height(40.dp)
    ) {
        items(buffs) { buff ->
            val remainingSeconds = ((buff.expirationTime - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            Box(
                modifier = Modifier
                    .background(Color(0xFF333333), RoundedCornerShape(4.dp))
                    .border(1.dp, if(buff.isAutoClick) Color.Yellow else Color.Cyan, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${if(buff.isAutoClick) "AUTO" else "x${buff.multiplier.toInt()}"}: ${remainingSeconds}s",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}