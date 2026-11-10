package com.EGomez.idleecosystem.ui.screens.main.dialogs

import androidx.compose.ui.res.stringResource
import com.EGomez.idleecosystem.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.EGomez.idleecosystem.domain.logic.AchievementCategory
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import com.EGomez.idleecosystem.domain.logic.NextGoal
import com.EGomez.idleecosystem.domain.model.Achievement
import com.EGomez.idleecosystem.ui.i18n.tr
import com.EGomez.idleecosystem.ui.components.AchievementItem
import com.EGomez.idleecosystem.ui.theme.AncientGold
import com.EGomez.idleecosystem.ui.theme.ArcanePurple

@Composable
fun AchievementsDialog(
    achievements: List<Achievement>,
    achievementsProgress: String,
    secretsProgress: String?,
    nextGoal: NextGoal? = null,
    setsCompleted: Int = 0,
    setsTotal: Int = 0,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            border = BorderStroke(1.dp, AncientGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.milestones, achievementsProgress), color = AncientGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        secretsProgress?.let { Text(it, color = ArcanePurple, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, stringResource(R.string.close), tint = Color.Gray) }
                }
                nextGoal?.let { NextGoalCard(it) }
                if (setsTotal > 0) {
                    // Conjuntos: todos los logros de un edificio duplican su producción
                    Text(
                        stringResource(R.string.achievement_sets, setsCompleted, setsTotal),
                        color = AncientGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(stringResource(R.string.achievement_sets_hint), color = Color.Gray, fontSize = 11.sp)
                }
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                // Categorías: con cientos de logros, la lista se filtra (null = todos)
                var category by rememberSaveable { mutableStateOf<AchievementCategory?>(null) }
                CategoryChips(achievements, category) { category = it }

                val displayList = remember(achievements, category) {
                    val result = mutableListOf<Pair<Achievement, Boolean>>()
                    val grouped = achievements.filter { it.groupId != null }.groupBy { it.groupId!! }
                    grouped.values.forEach { chain ->
                        val sortedChain = chain.sortedBy { it.order }
                        var foundLocked = false
                        for (ach in sortedChain) {
                            if (ach.isUnlocked) {
                                result.add(ach to true)
                            } else {
                                if (!foundLocked) {
                                    result.add(ach to true); foundLocked = true
                                } else {
                                    result.add(ach to false)
                                }
                            }
                        }
                    }
                    achievements.filter { it.groupId == null }.forEach { result.add(it to false) }
                    result.sortedBy { it.first.order }
                        .filter { category == null || AchievementCategory.of(it.first) == category }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (achievements.isEmpty()) { item { Text(stringResource(R.string.empty), color = Color.Gray) } }
                    else { items(displayList, key = { it.first.id }) { (ach, isRevealed) -> AchievementItem(achievement = ach, isRevealed = isRevealed) } }
                }
            }
        }
    }
}

/** Fila de filtros: "Todos" y una categoría por tipo, con logrados / total. */
@Composable
private fun CategoryChips(
    achievements: List<Achievement>,
    selected: AchievementCategory?,
    onSelect: (AchievementCategory?) -> Unit
) {
    val counts = remember(achievements) {
        achievements.groupBy { AchievementCategory.of(it) }
            .mapValues { (_, list) -> list.count { it.isUnlocked } to list.size }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 6.dp)
    ) {
        CategoryChip(stringResource(R.string.achievements_cat_all), selected == null, Modifier.testTag(ACHIEVEMENT_CATEGORY_TAG + "all")) {
            onSelect(null)
        }
        AchievementCategory.entries.forEach { cat ->
            val (done, total) = counts[cat] ?: return@forEach
            // Los secretos solo cuentan los encontrados: el total los revelaría
            val label = if (cat == AchievementCategory.SECRET) "${categoryName(cat)} $done" else "${categoryName(cat)} $done/$total"
            CategoryChip(label, selected == cat, Modifier.testTag(ACHIEVEMENT_CATEGORY_TAG + cat.name)) { onSelect(cat) }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AncientGold,
            selectedLabelColor = Color.Black,
            containerColor = Color(0xFF2A2A2A),
            labelColor = Color.LightGray
        ),
        border = null,
        modifier = modifier
    )
}

@Composable
private fun categoryName(category: AchievementCategory): String = stringResource(
    when (category) {
        AchievementCategory.PROGRESS -> R.string.achievements_cat_progress
        AchievementCategory.BUILDINGS -> R.string.achievements_cat_buildings
        AchievementCategory.ASCENSION -> R.string.achievements_cat_ascension
        AchievementCategory.PANTHEON -> R.string.achievements_cat_pantheon
        AchievementCategory.TRIALS -> R.string.achievements_cat_trials
        AchievementCategory.ARCANE -> R.string.achievements_cat_arcane
        AchievementCategory.SECRET -> R.string.achievements_cat_secret
    }
)

const val ACHIEVEMENT_CATEGORY_TAG = "achievement_category_"

/** La meta más cercana, con su avance, arriba de la lista de logros. */
@Composable
private fun NextGoalCard(goal: NextGoal) {
    val percent = (goal.progress * 100).toInt().coerceIn(0, 99)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .testTag(NEXT_GOAL_TAG)
    ) {
        Text(
            text = stringResource(R.string.next_goal, tr(goal.requirement), percent),
            color = Color.LightGray,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { goal.progress },
            color = AncientGold,
            trackColor = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

const val NEXT_GOAL_TAG = "next_goal"
