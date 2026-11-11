package com.EGomez.idleecosystem.ui.screens.main.components

import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple
import com.EGomez.idleecosystem.ui.theme.VoidBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    isMenuExpanded: Boolean,
    menuWeight: Float,
    achievements: List<Achievement>,
    achievementsProgress: String,
    nextGoalProgress: Float? = null,
    /** Con el menú abierto (v0.18.2): el Conocimiento y su producción, en lugar del título. */
    knowledgeFormatted: String? = null,
    knowledgePerSecond: String? = null,
    /** Interfaz por capas (v0.19.0): las estadísticas y los logros aparecen al descubrirse. */
    showStats: Boolean = true,
    showAchievements: Boolean = true,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onAchievementsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            if (isMenuExpanded && knowledgeFormatted != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.testTag(COMPACT_KNOWLEDGE_TAG)) {
                    Text(knowledgeFormatted, color = ArcanePurple, fontWeight = FontWeight.Bold, fontSize = 20.sp, maxLines = 1)
                    if (knowledgePerSecond != null) {
                        Text(stringResource(R.string.per_second, knowledgePerSecond), color = Color(0xFF4CAF50), fontSize = 11.sp, maxLines = 1)
                    }
                }
            } else if (!isMenuExpanded || menuWeight < 0.8f) {
                Text(
                    stringResource(R.string.app_title),
                    color = ArcanePurple,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = VoidBlack),
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, stringResource(R.string.cd_settings), tint = Color.Gray)
                }
                if (showStats) {
                    IconButton(onClick = onStatsClick, modifier = Modifier.testTag(STATS_BUTTON_TAG)) {
                        Icon(Icons.Default.Assessment, stringResource(R.string.cd_statistics), tint = AncientGold)
                    }
                }
            }
        },
        actions = {
            if (!showAchievements) return@CenterAlignedTopAppBar
            val hasUnlocked = achievements.any { it.isUnlocked }
            val iconColor = if (hasUnlocked) AncientGold else Color.Gray

            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, iconColor),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { onAchievementsClick() }
            ) {
                Row(
                    modifier = Modifier
                        // La próxima meta: una línea fina dentro del botón de logros
                        .drawBehind {
                            if (nextGoalProgress == null) return@drawBehind
                            val inset = 10.dp.toPx()
                            val y = size.height - 2.dp.toPx()
                            val stroke = 2.dp.toPx()
                            val end = size.width - inset
                            drawLine(Color.White.copy(alpha = 0.12f), Offset(inset, y), Offset(end, y), stroke, StrokeCap.Round)
                            val filled = inset + (end - inset) * nextGoalProgress.coerceIn(0f, 1f)
                            if (filled > inset) {
                                drawLine(AncientGold, Offset(inset, y), Offset(filled, y), stroke, StrokeCap.Round)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        stringResource(R.string.achievements),
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = achievementsProgress,
                        color = iconColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}

const val COMPACT_KNOWLEDGE_TAG = "compact_knowledge"
const val STATS_BUTTON_TAG = "stats_button"
