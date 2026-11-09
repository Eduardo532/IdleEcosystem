package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.annotation.StringRes
import androidx.compose.material.icons.filled.Language
import com.EGomez.idleecosystem.core.locale.AppLanguage
import com.EGomez.idleecosystem.core.math.NumberNotation
import com.EGomez.idleecosystem.core.math.toBeautifulString
import com.EGomez.idleecosystem.ui.GameUiState
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import com.EGomez.idleecosystem.ui.theme.CrimsonRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

/** Secciones del menú de ajustes. */
enum class SettingsSection(@StringRes val titleRes: Int, @StringRes val subtitleRes: Int, val icon: ImageVector) {
    SOUND(R.string.settings_sound, R.string.settings_sound_sub, Icons.Default.MusicNote),
    DISPLAY(R.string.settings_display, R.string.settings_display_sub, Icons.Default.Visibility),
    LANGUAGE(R.string.settings_language, R.string.settings_language_sub, Icons.Default.Language),
    GAME(R.string.settings_game, R.string.settings_game_sub, Icons.Default.SportsEsports),
    SAVE(R.string.settings_save, R.string.settings_save_sub, Icons.Default.Save),
    DANGER(R.string.settings_danger, R.string.settings_danger_sub, Icons.Default.Warning)
}

/**
 * Ajustes en dos niveles: una lista de secciones y, al tocar una, su submenú.
 *
 * Antes todo estaba en una sola columna (audio, interfaz, exportar, importar y el
 * reinicio total juntos), con interruptores de tres en fila y el botón de borrar la
 * partida al final de la lista.
 */
@Composable
fun SettingsDialog(
    uiState: GameUiState,
    onDismiss: () -> Unit,
    onStatsClick: () -> Unit,
    onToggleMusic: () -> Unit,
    onToggleSfx: () -> Unit,
    onToggleParticles: () -> Unit,
    onToggleFloatingText: () -> Unit,
    onNotationSelected: (NumberNotation) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onCancelImport: () -> Unit,
    onPasteClick: (String) -> Unit,
    onHardResetClick: () -> Unit,
    onToggleTutorial: (Boolean) -> Unit
) {
    // Se recuerda al girar la pantalla
    var section by rememberSaveable { mutableStateOf<SettingsSection?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                SettingsHeader(
                    title = section?.let { stringResource(it.titleRes) } ?: stringResource(R.string.settings_title),
                    onBack = if (section != null) ({ section = null }) else null
                )
                Spacer(modifier = Modifier.height(12.dp))

                when (section) {
                    null -> SettingsSection.entries.forEach { item ->
                        SectionRow(item) { section = item }
                    }
                    SettingsSection.SOUND -> {
                        ToggleRow(stringResource(R.string.music), uiState.isMusicEnabled) { onToggleMusic() }
                        ToggleRow(stringResource(R.string.sfx), uiState.isSfxEnabled) { onToggleSfx() }
                    }
                    SettingsSection.DISPLAY -> {
                        ToggleRow(stringResource(R.string.particles), uiState.areParticlesEnabled) { onToggleParticles() }
                        ToggleRow(stringResource(R.string.floating_text), uiState.isFloatingTextEnabled) { onToggleFloatingText() }
                        Spacer(modifier = Modifier.height(8.dp))
                        NotationPicker(uiState.numberNotation, onNotationSelected)
                    }
                    SettingsSection.LANGUAGE -> AppLanguage.entries.forEach { language ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onLanguageSelected(language) }.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == uiState.language,
                                onClick = { onLanguageSelected(language) },
                                colors = RadioButtonDefaults.colors(selectedColor = ArcanePurple)
                            )
                            Text(language.nativeName, color = Color.White, fontSize = 14.sp)
                        }
                    }
                    SettingsSection.GAME -> {
                        ToggleRow(stringResource(R.string.tutorial), uiState.isTutorialEnabled) { onToggleTutorial(it) }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onStatsClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AncientGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.view_stats), color = Color.White)
                        }
                    }
                    SettingsSection.SAVE -> SaveSection(uiState, onExportClick, onImportClick, onCancelImport, onPasteClick)
                    SettingsSection.DANGER -> {
                        Text(
                            stringResource(R.string.danger_text),
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onHardResetClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.full_reset), color = Color.Black, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(title: String, onBack: (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
            }
        }
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionRow(section: SettingsSection, onClick: () -> Unit) {
    val accent = if (section == SettingsSection.DANGER) CrimsonRed else AncientGold
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(section.icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(section.titleRes), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(stringResource(section.subtitleRes), color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedThumbColor = ArcanePurple, checkedTrackColor = ArcanePurple.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun NotationPicker(selected: NumberNotation, onSelected: (NumberNotation) -> Unit) {
    val sample = BigDecimal("1234567")
    Text(stringResource(R.string.big_numbers), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    NumberNotation.entries.forEach { notation ->
        val label = when (notation) {
            NumberNotation.SCIENTIFIC -> stringResource(R.string.notation_scientific)
            NumberNotation.SUFFIX -> stringResource(R.string.notation_suffix)
        }
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onSelected(notation) }.padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = notation == selected,
                onClick = { onSelected(notation) },
                colors = RadioButtonDefaults.colors(selectedColor = ArcanePurple)
            )
            Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(sample.toBeautifulString(notation), color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SaveSection(
    uiState: GameUiState,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onCancelImport: () -> Unit,
    onPasteClick: (String) -> Unit
) {
    // LocalClipboard reemplaza a LocalClipboardManager (obsoleto): sus operaciones
    // son suspend, así que se lanzan en el scope del diálogo.
    val clipboard = LocalClipboard.current
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // --- EXPORTAR ---
    Text(stringResource(R.string.export_title), color = AncientGold, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    val exportPreview = uiState.exportPreview
    if (uiState.saveStringExport.isNotEmpty() && exportPreview != null) {
        SavePreviewCard(stringResource(R.string.your_file), AncientGold, exportPreview.date, exportPreview.money, exportPreview.achievements)
        Button(
            onClick = {
                scope.launch {
                    clipboard.setClipEntry(ClipData.newPlainText(CLIP_LABEL, uiState.saveStringExport).toClipEntry())
                    isCopied = true
                    delay(2000)
                    isCopied = false
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (isCopied) Color(0xFF4CAF50) else AncientGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy, null, tint = if (isCopied) Color.White else Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isCopied) stringResource(R.string.copied) else stringResource(R.string.copy_clipboard), color = if (isCopied) Color.White else Color.Black, fontWeight = FontWeight.Bold)
        }
    } else {
        Button(
            onClick = onExportClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.generate_code)) }
    }

    HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))

    // --- IMPORTAR ---
    Text(stringResource(R.string.import_title), color = AncientGold, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    val importPreview = uiState.importPreview
    if (importPreview != null) {
        SavePreviewCard(stringResource(R.string.file_found), ArcanePurple, importPreview.date, importPreview.money, importPreview.achievements)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCancelImport, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White), modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel), fontSize = 10.sp) }
            Button(onClick = onImportClick, colors = ButtonDefaults.buttonColors(containerColor = ArcanePurple, contentColor = Color.White), modifier = Modifier.weight(1f)) { Text(stringResource(R.string.load_data), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    } else {
        Button(
            onClick = {
                scope.launch {
                    val text = clipboard.getClipEntry()?.clipData
                        ?.takeIf { it.itemCount > 0 }
                        ?.getItemAt(0)?.text?.toString()
                    if (!text.isNullOrEmpty()) onPasteClick(text)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentPaste, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.paste_code))
        }
        if (uiState.importFailed) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.import_invalid), color = CrimsonRed, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SavePreviewCard(title: String, accent: Color, date: String, money: String, achievements: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, accent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = accent, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.preview_date, date), color = Color.Gray, fontSize = 12.sp)
            Text(stringResource(R.string.preview_knowledge, money), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(stringResource(R.string.preview_achievements, achievements), color = Color.White, fontSize = 12.sp)
        }
    }
}

private const val CLIP_LABEL = "IdleEcosystem"
