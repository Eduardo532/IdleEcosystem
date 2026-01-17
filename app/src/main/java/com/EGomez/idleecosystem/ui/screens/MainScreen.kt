package com.EGomez.idleecosystem.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment // <--- NUEVO ICONO DE ESTADÍSTICAS
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import com.EGomez.idleecosystem.domain.model.UpgradeType
import com.EGomez.idleecosystem.ui.MainViewModel
import com.EGomez.idleecosystem.ui.components.AchievementItem
import com.EGomez.idleecosystem.ui.components.BuildingItem
import com.EGomez.idleecosystem.ui.components.UpgradeItem
import com.EGomez.idleecosystem.ui.components.ConstellationMap
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import com.EGomez.idleecosystem.ui.theme.VoidBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

data class FloatingText(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val animatable: Animatable<Float, androidx.compose.animation.core.AnimationVector1D> = Animatable(0f)
)

// --- COMPONENTE POPUP DE LOGROS ---
@Composable
fun AchievementPopup(achievement: com.EGomez.idleecosystem.domain.model.Achievement) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E).copy(alpha = 0.95f)),
        border = BorderStroke(2.dp, if(achievement.isSecret) ArcanePurple else AncientGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if(achievement.isSecret) ArcanePurple.copy(alpha = 0.2f) else AncientGold.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, if(achievement.isSecret) ArcanePurple.copy(alpha = 0.5f) else AncientGold.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if(achievement.isSecret) ArcanePurple else AncientGold,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if(achievement.isSecret) "¡SECRETO REVELADO!" else "¡LOGRO DESBLOQUEADO!",
                    color = if(achievement.isSecret) ArcanePurple else AncientGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = achievement.name,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Bonus Global: x${achievement.bonusMultiplier}",
                    color = Color(0xFF81C784),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    buildingCalculator: BuildingCalculator = BuildingCalculator()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val currentDensity = LocalDensity.current
    val responsiveFontScale = ((screenWidth / 380f) * 0.75f).coerceIn(0.6f, 1.1f)

    CompositionLocalProvider(
        LocalDensity provides Density(density = currentDensity.density, fontScale = responsiveFontScale)
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val buildings by viewModel.buildingsState.collectAsState()
        val upgrades by viewModel.upgradesState.collectAsState()
        val achievements by viewModel.achievementsState.collectAsState()
        val showPurchased by viewModel.showPurchasedState.collectAsState()

        var showAchievementsDialog by remember { mutableStateOf(false) }
        var showPrestigeShopDialog by remember { mutableStateOf(false) }
        var showSettingsDialog by remember { mutableStateOf(false) }
        var showHardResetConfirmation by remember { mutableStateOf(false) }
        var showStatisticsDialog by remember { mutableStateOf(false) }

        val clipboardManager = LocalClipboardManager.current
        var isCopied by remember { mutableStateOf(false) }

        var isMenuExpanded by remember { mutableStateOf(true) }
        val menuWeight by animateFloatAsState(
            targetValue = if (isMenuExpanded) 0.65f else 0.15f,
            label = "menuAnimation"
        )

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("Investigación", "Grimorio")

        val floatingTexts = remember { mutableStateListOf<FloatingText>() }
        val scope = rememberCoroutineScope()

        fun spawnFloatingText(value: String) {
            val newItem = FloatingText(text = "+$value")
            floatingTexts.add(newItem)
            scope.launch {
                newItem.animatable.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 800))
                floatingTexts.remove(newItem)
            }
        }

        // ================== CONTENEDOR PRINCIPAL ==================
        Box(modifier = Modifier.fillMaxSize().background(VoidBlack)) {

            // 1. EL JUEGO (SCAFFOLD)
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { if (!isMenuExpanded || menuWeight < 0.8f) Text("Ecosistema Arcano", color = ArcanePurple, fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VoidBlack),
                        navigationIcon = {
                            // --- MODIFICACIÓN: Fila con Configuración y Estadísticas ---
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showSettingsDialog = true }) {
                                    Icon(Icons.Default.Settings, "Configuración", tint = Color.Gray)
                                }
                                IconButton(onClick = { showStatisticsDialog = true }) {
                                    Icon(Icons.Default.Assessment, "Estadísticas", tint = AncientGold) // Icono de reporte/estadísticas
                                }
                            }
                        },
                        actions = {
                            // CONTADOR DE LOGROS EN TOP BAR
                            Surface(color = Color.Transparent, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, if(achievements.any { it.isUnlocked }) AncientGold else Color.Gray), modifier = Modifier.padding(end = 8.dp).clickable { showAchievementsDialog = true }) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EmojiEvents, "Logros", tint = if(achievements.any { it.isUnlocked }) AncientGold else Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(uiState.achievementsProgress, color = if(achievements.any { it.isUnlocked }) AncientGold else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->
                // CONTENIDO DEL JUEGO
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                        // ZONA 1 (INFO)
                        Column(modifier = Modifier.fillMaxWidth().weight(1f - menuWeight), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Conocimiento Arcano", color = Color.Gray, fontSize = 14.sp)
                                Text(text = uiState.knowledgeFormatted, color = ArcanePurple, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                                Text(text = "+${uiState.knowledgePerSecond}/seg", color = Color.Green, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (uiState.globalMultiplier > BigDecimal.ONE) {
                                Surface(color = AncientGold.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, AncientGold), modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text(text = "Global: x${uiState.globalMultiplier.toBeautifulString()}", color = AncientGold, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }

                            if (uiState.activeBuffMultiplier > 1.0) {
                                Surface(color = Color(0xFF2E7D32).copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFF4CAF50)), modifier = Modifier.padding(bottom = 16.dp)) {
                                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "BONUS ACTIVO: x${uiState.activeBuffMultiplier} (${uiState.buffTimeRemaining}s)", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                if (uiState.globalMultiplier <= BigDecimal.ONE) Spacer(modifier = Modifier.height(32.dp))
                            }

                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(8.dp).weight(1f)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight(0.85f).aspectRatio(1f).clip(CircleShape).background(ArcanePurple.copy(alpha = 0.1f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { val added = viewModel.onManualClick(); spawnFloatingText(added.toBeautifulString()) }) {
                                    Box(modifier = Modifier.fillMaxSize(0.85f).clip(CircleShape).background(ArcanePurple.copy(alpha = 0.2f)))
                                    BoxWithConstraints(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        val size = maxWidth
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("INVESTIGAR", color = ArcanePurple, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.12f).sp)
                                            Text(text = "+${uiState.clickMultiplier}", color = Color.White.copy(alpha = 0.9f), fontSize = (size.value * 0.10f).sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // ZONA 2 (MENÚ)
                        Column(modifier = Modifier.fillMaxWidth().weight(menuWeight).background(Color(0xFF161616), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))) {
                            Box(modifier = Modifier.fillMaxWidth().clickable { isMenuExpanded = !isMenuExpanded }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) { Icon(imageVector = if (isMenuExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp)) }

                            if (menuWeight > 0.2f) {
                                if (selectedTabIndex == 0) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        listOf(1, 5, 10, 100, -1).forEach { mult ->
                                            val label = if (mult == -1) "MAX" else "x$mult"
                                            FilterChip(selected = uiState.buyMultiplier == mult, onClick = { viewModel.setBuyMultiplier(mult) }, label = { Text(label) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ArcanePurple, selectedLabelColor = Color.Black, containerColor = Color(0xFF333333), labelColor = Color.Gray), border = null)
                                        }
                                    }
                                }

                                TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent, contentColor = ArcanePurple, indicator = { TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(it[selectedTabIndex]), color = ArcanePurple) }) {
                                    tabs.forEachIndexed { index, title -> Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title, fontWeight = FontWeight.Bold) }, selectedContentColor = ArcanePurple, unselectedContentColor = Color.Gray) }
                                }

                                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)) {
                                    when (selectedTabIndex) {
                                        0 -> { items(buildings) { building -> BuildingItem(building, uiState.rawKnowledge, uiState.globalMultiplier, buildingCalculator, uiState.buyMultiplier) { viewModel.onBuyBuilding(building.id) } }; item { Spacer(modifier = Modifier.height(16.dp)) } }
                                        1 -> {
                                            item { Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) { Text("Ver Comprados", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp)); Switch(checked = showPurchased, onCheckedChange = { viewModel.toggleShowPurchased() }, colors = SwitchDefaults.colors(checkedThumbColor = ArcanePurple, checkedTrackColor = ArcanePurple.copy(alpha = 0.5f)), modifier = Modifier.scale(0.8f)) } }
                                            if (upgrades.isEmpty()) item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No hay secretos...", color = Color.Gray) } }
                                            else items(upgrades) { upgrade -> UpgradeItem(upgrade = upgrade, targetInfo = "Afecta: ${if(upgrade.type == UpgradeType.CLICK_MULTIPLIER) "Tu Mente" else if (upgrade.type == UpgradeType.GLOBAL_MULTIPLIER) "Todo" else buildings.find { it.id == upgrade.targetBuildingId }?.name ?: "?"}", currentFunds = uiState.rawKnowledge) { viewModel.onBuyUpgrade(upgrade.id) } }
                                        }
                                    }
                                }

                                if (isMenuExpanded) {
                                    HorizontalDivider(color = Color(0xFF333333))
                                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF0D0D0D)).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column { Text("CORDURA RESIDUAL", color = Color(0xFFCF6679), fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(uiState.prestigeCurrencyFormatted, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                                        Row {
                                            Button(onClick = { showPrestigeShopDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C), contentColor = Color.White), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(end = 8.dp)) { Text("TIENDA", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                            Button(onClick = { viewModel.onAscendClicked() }, enabled = uiState.canPrestige, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679), disabledContainerColor = Color(0xFF333333), contentColor = Color.Black, disabledContentColor = Color.Gray), shape = RoundedCornerShape(8.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("ASCENDER", fontWeight = FontWeight.Black); if (uiState.canPrestige) Text("+${uiState.potentialPrestigeFormatted}", fontSize = 10.sp, fontWeight = FontWeight.Bold) else Text("Req: 1.00e+06", fontSize = 9.sp) } }
                                        }
                                    }
                                }
                            } else { Text("Desliza para ver contenido", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)) }
                        }
                    }
                    floatingTexts.forEach { item -> Text(text = item.text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 220.dp).graphicsLayer { translationY = -150f * item.animatable.value; alpha = 1f - item.animatable.value }) }
                }
            }

            // 2. POPUP NOTIFICACIÓN
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.lastUnlockedAchievement != null,
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }) + androidx.compose.animation.fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp).zIndex(10f)
            ) {
                uiState.lastUnlockedAchievement?.let { achievement -> AchievementPopup(achievement = achievement) }
            }

            // 3. DIÁLOGOS

            // --- PANTALLA DE ESTADÍSTICAS ---
            if (showStatisticsDialog) {
                StatisticsScreen(
                    statisticsManager = viewModel.statsManager,
                    onDismiss = { showStatisticsDialog = false }
                )
            }

            if (showHardResetConfirmation) {
                AlertDialog(onDismissRequest = { showHardResetConfirmation = false }, icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) }, title = { Text("¿ESTÁS SEGURO?", color = Color.Red, fontWeight = FontWeight.Black) }, text = { Text("Esta acción borrará PERMANENTEMENTE todo tu progreso.\n\nNo se puede deshacer.", color = Color.White) }, containerColor = Color(0xFF1E1E1E), titleContentColor = Color.Red, textContentColor = Color.White, confirmButton = { Button(onClick = { viewModel.onHardResetClicked(); showHardResetConfirmation = false; showSettingsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("SÍ, BORRAR TODO", color = Color.White, fontWeight = FontWeight.Bold) } }, dismissButton = { TextButton(onClick = { showHardResetConfirmation = false }) { Text("CANCELAR", color = Color.Gray) } })
            }

            if (showSettingsDialog) {
                Dialog(onDismissRequest = { showSettingsDialog = false; viewModel.clearExportString(); isCopied = false }) {
                    Surface(modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF1E1E1E), border = BorderStroke(1.dp, Color.Gray)) {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Configuración", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(16.dp))

                            // Botón de estadísticas (Duplicado aquí por si acaso el usuario prefiere este menú)
                            Button(
                                onClick = { showStatisticsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = AncientGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("VER ESTADÍSTICAS", color = Color.White)
                            }

                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Música", color = Color.Gray, fontSize = 12.sp); Switch(checked = uiState.isMusicEnabled, onCheckedChange = { viewModel.toggleMusic() }, colors = SwitchDefaults.colors(checkedThumbColor = ArcanePurple, checkedTrackColor = ArcanePurple.copy(alpha = 0.5f))) }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Efectos", color = Color.Gray, fontSize = 12.sp); Switch(checked = uiState.isSfxEnabled, onCheckedChange = { viewModel.toggleSfx() }, colors = SwitchDefaults.colors(checkedThumbColor = AncientGold, checkedTrackColor = AncientGold.copy(alpha = 0.5f))) }
                            }
                            Divider(color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp)); Text("Exportar Partida", color = AncientGold, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                            if (uiState.saveStringExport.isNotEmpty() && uiState.exportPreview != null) {
                                val preview = uiState.exportPreview!!
                                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)), border = BorderStroke(1.dp, AncientGold)) { Column(modifier = Modifier.padding(16.dp)) { Text("TU ARCHIVO ACTUAL", color = AncientGold, fontWeight = FontWeight.Black, fontSize = 12.sp); Spacer(modifier = Modifier.height(8.dp)); Text("Fecha: ${preview.date}", color = Color.Gray, fontSize = 12.sp); Text("Conocimiento: ${preview.money}", color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Logros: ${preview.achievements}", color = Color.White, fontSize = 12.sp) } }
                                Button(onClick = { clipboardManager.setText(AnnotatedString(uiState.saveStringExport)); isCopied = true; scope.launch { delay(2000); isCopied = false } }, colors = ButtonDefaults.buttonColors(containerColor = if (isCopied) Color(0xFF4CAF50) else AncientGold), modifier = Modifier.fillMaxWidth()) { Icon(imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null, tint = if (isCopied) Color.White else Color.Black, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(text = if (isCopied) "¡COPIADO!" else "COPIAR AL PORTAPAPELES", color = if (isCopied) Color.White else Color.Black, fontWeight = FontWeight.Bold) }
                            } else { Button(onClick = { viewModel.onExportSaveClicked() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White), modifier = Modifier.fillMaxWidth()) { Text("GENERAR CÓDIGO") } }
                            Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp)); Text("Importar Partida", color = AncientGold, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                            if (uiState.importPreview != null) {
                                val preview = uiState.importPreview!!
                                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)), border = BorderStroke(1.dp, ArcanePurple)) { Column(modifier = Modifier.padding(16.dp)) { Text("ARCHIVO ENCONTRADO", color = ArcanePurple, fontWeight = FontWeight.Black, fontSize = 12.sp); Spacer(modifier = Modifier.height(8.dp)); Text("Fecha: ${preview.date}", color = Color.Gray, fontSize = 12.sp); Text("Conocimiento: ${preview.money}", color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text("Logros: ${preview.achievements}", color = Color.White, fontSize = 12.sp) } }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { viewModel.clearExportString() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White), modifier = Modifier.weight(1f)) { Text("CANCELAR", fontSize = 10.sp) }; Button(onClick = { viewModel.onConfirmImportClicked(); showSettingsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple, contentColor = Color.White), modifier = Modifier.weight(1f)) { Text("CARGAR DATOS", fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
                            } else { Button(onClick = { val text = clipboardManager.getText()?.text; if (!text.isNullOrEmpty()) viewModel.onPasteSaveClicked(text) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("PEGAR CÓDIGO") }; if (uiState.importError != null) { Spacer(modifier = Modifier.height(8.dp)); Text(uiState.importError!!, color = Color.Red, fontSize = 12.sp) } }
                            Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp)); Button(onClick = { showHardResetConfirmation = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679)), modifier = Modifier.fillMaxWidth()) { Text("REINICIO TOTAL", color = Color.Black, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }

            if (showAchievementsDialog) {
                Dialog(onDismissRequest = { showAchievementsDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                    Surface(modifier = Modifier.fillMaxSize().padding(16.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF1E1E1E), border = BorderStroke(1.dp, AncientGold.copy(alpha = 0.5f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Hitos (${uiState.achievementsProgress})", color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    uiState.secretsProgress?.let {
                                        Text(it, color = ArcanePurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                IconButton(onClick = { showAchievementsDialog = false }) { Icon(Icons.Default.Close, "Cerrar", tint = Color.Gray) }
                            }
                            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                            val displayList = remember(achievements) {
                                val result = mutableListOf<Pair<com.EGomez.idleecosystem.domain.model.Achievement, Boolean>>()
                                val grouped = achievements.filter { it.groupId != null }.groupBy { it.groupId!! }
                                grouped.values.forEach { chain ->
                                    val sortedChain = chain.sortedBy { it.order }
                                    var foundLocked = false
                                    for (ach in sortedChain) {
                                        if (ach.isUnlocked) {
                                            result.add(ach to true)
                                        } else {
                                            if (!foundLocked) {
                                                result.add(ach to true)
                                                foundLocked = true
                                            } else {
                                                result.add(ach to false)
                                            }
                                        }
                                    }
                                }
                                achievements.filter { it.groupId == null }.forEach { result.add(it to false) }
                                result.sortedBy { it.first.order }
                            }

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                if (achievements.isEmpty()) { item { Text("Vacío...", color = Color.Gray) } }
                                else { items(displayList) { (ach, isRevealed) -> AchievementItem(achievement = ach, isRevealed = isRevealed) } }
                            }
                        }
                    }
                }
            }

            if (showPrestigeShopDialog) {
                var selectedNode by remember { mutableStateOf<com.EGomez.idleecosystem.domain.model.PrestigeUpgrade?>(null) }
                Dialog(onDismissRequest = { showPrestigeShopDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF050505))) {
                        Box(modifier = Modifier.fillMaxSize().padding(top = 60.dp, bottom = 150.dp, start = 20.dp, end = 20.dp)) { ConstellationMap(upgrades = uiState.prestigeUpgrades, onNodeClick = { node -> selectedNode = node }) }
                        Column(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.8f)).padding(16.dp)) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("MAPA DE LOCURA", color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 20.sp); Text("Cordura: ${uiState.prestigeCurrencyFormatted}", color = ArcanePurple, fontWeight = FontWeight.Bold) }; IconButton(onClick = { showPrestigeShopDialog = false }) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White) } } }
                        if (selectedNode != null) {
                            val node = selectedNode!!; val parentUnlocked = if (node.parentId == null) true else { val parent = uiState.prestigeUpgrades.find { it.id == node.parentId }; parent != null && parent.level > 0 }
                            Card(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}, colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), border = BorderStroke(1.dp, if (parentUnlocked) ArcanePurple else Color.Gray), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(node.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp); if (node.level > 0) Text("Nivel ${node.level}", color = AncientGold, fontWeight = FontWeight.Bold) }
                                    Spacer(modifier = Modifier.height(8.dp)); Text(node.description, color = Color.LightGray, fontSize = 14.sp); Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) { Text("Efecto Actual: ", color = Color.Gray, fontSize = 12.sp); Text(node.currentEffectDisplay, color = Color(0xFF81C784), fontWeight = FontWeight.Bold) }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    if (parentUnlocked) { Button(onClick = { viewModel.onBuyPrestigeUpgrade(node.id) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple, contentColor = Color.White)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("MEJORAR", fontWeight = FontWeight.Bold); Text("Coste: ${node.currentCost.toBeautifulString()} Cordura", fontSize = 10.sp) } } } else { Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.DarkGray)) { Text("BLOQUEADO (Requiere conexión previa)", color = Color.Gray) } }
                                }
                            }
                        } else { Text("Toca una estrella para ver detalles", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) }
                    }
                }
            }

            if (uiState.showOfflineDialog) { AlertDialog(onDismissRequest = { viewModel.dismissOfflineDialog() }, title = { Text("Mientras dormías...", color = ArcanePurple, fontWeight = FontWeight.Bold) }, text = { Column { Text("Tu locura creció en las sombras.", color = Color.Gray); Spacer(modifier = Modifier.height(8.dp)); Text(text = "Tiempo: ${uiState.offlineTimeFormatted}", color = Color.White); Text(text = "Ganancia: +${uiState.offlineGains}", color = Color(0xFF4CAF50), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) } }, confirmButton = { Button(onClick = { viewModel.dismissOfflineDialog() }, colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple)) { Text("Aceptar") } }, containerColor = Color(0xFF1E1E1E), textContentColor = Color.White) }

            if (uiState.showRefundDialog) { AlertDialog(onDismissRequest = { viewModel.dismissRefundDialog() }, title = { Text("Actualización del Cosmos", color = AncientGold, fontWeight = FontWeight.Bold) }, text = { Column { Text("El sistema de Prestigio ha evolucionado a un Mapa Estelar.", color = Color.Gray); Spacer(modifier = Modifier.height(16.dp)); Text("Tus mejoras anteriores han sido reiniciadas y tu inversión ha sido devuelta íntegramente.", color = Color.White); Spacer(modifier = Modifier.height(8.dp)); Text("Cordura Recuperada: +${uiState.refundAmountString}", color = ArcanePurple, fontWeight = FontWeight.Bold, fontSize = 18.sp) } }, confirmButton = { Button(onClick = { viewModel.dismissRefundDialog() }, colors = ButtonDefaults.buttonColors(containerColor = AncientGold, contentColor = Color.Black)) { Text("EXPLORAR") } }, containerColor = Color(0xFF1E1E1E), shape = RoundedCornerShape(16.dp), modifier = Modifier.border(1.dp, AncientGold, RoundedCornerShape(16.dp))) }

            uiState.currentEvent?.let { event ->
                if (uiState.eventResultText != null) {
                    AlertDialog(onDismissRequest = {}, title = { Text(event.title, color = ArcanePurple, fontWeight = FontWeight.Bold) }, text = { Text(uiState.eventResultText!!, color = Color.White, fontStyle = FontStyle.Italic) }, confirmButton = {}, containerColor = Color(0xFF1E1E1E), shape = RoundedCornerShape(16.dp), modifier = Modifier.border(1.dp, ArcanePurple, RoundedCornerShape(16.dp)))
                } else {
                    var areButtonsEnabled by remember { mutableStateOf(false) }
                    LaunchedEffect(event.id) { areButtonsEnabled = false; delay(1000); areButtonsEnabled = true }
                    AlertDialog(onDismissRequest = {}, title = { Text(event.title, color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 20.sp) }, text = {
                        Column {
                            Text(event.description, color = Color.LightGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(24.dp))
                            event.options.forEachIndexed { index, option ->
                                Button(
                                    onClick = { viewModel.onEventOptionSelected(index) },
                                    enabled = areButtonsEnabled,
                                    colors = ButtonDefaults.buttonColors(containerColor = if (areButtonsEnabled) Color(0xFF2D2D2D) else Color(0xFF444444), disabledContainerColor = Color(0xFF444444), contentColor = Color.White, disabledContentColor = Color.Gray),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    border = BorderStroke(1.dp, if (areButtonsEnabled) Color.Gray.copy(alpha = 0.5f) else Color.Transparent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(option.text, color = if (areButtonsEnabled) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            if (option.effects.all { it.type == com.EGomez.idleecosystem.domain.model.EffectType.NOTHING }) {
                                                Text("Sin consecuencias", color = Color.Gray, fontSize = 11.sp)
                                            } else {
                                                option.effects.forEachIndexed { i, effect ->
                                                    if (i > 0) Spacer(modifier = Modifier.width(12.dp))
                                                    val alpha = if (areButtonsEnabled) 1f else 0.5f
                                                    when (effect.type) {
                                                        com.EGomez.idleecosystem.domain.model.EffectType.ADD_KNOWLEDGE_PERCENT -> {
                                                            val percent = (effect.value * 100).toInt()
                                                            val color = if (percent >= 0) Color(0xFF81C784) else Color(0xFFE57373)
                                                            Text("${if (percent >= 0) "+" else ""}$percent% Conocimiento", color = color.copy(alpha = alpha), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        com.EGomez.idleecosystem.domain.model.EffectType.ADD_SANITY_PERCENT -> {
                                                            val percent = (effect.value * 100).toInt()
                                                            val color = if (percent >= 0) Color(0xFF64B5F6) else Color(0xFFE57373)
                                                            Text("${if (percent >= 0) "+" else ""}$percent% Cordura", color = color.copy(alpha = alpha), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        com.EGomez.idleecosystem.domain.model.EffectType.GLOBAL_BUFF -> {
                                                            Text("Potenciador x${effect.value}", color = AncientGold.copy(alpha = alpha), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        else -> {}
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }, confirmButton = {}, containerColor = Color(0xFF121212), shape = RoundedCornerShape(16.dp), modifier = Modifier.border(2.dp, AncientGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp)))
                }
            }
        }
    }
}