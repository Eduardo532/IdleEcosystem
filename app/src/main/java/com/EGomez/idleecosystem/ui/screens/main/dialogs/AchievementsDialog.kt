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
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                val displayList = remember(achievements) {
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
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (achievements.isEmpty()) { item { Text(stringResource(R.string.empty), color = Color.Gray) } }
                    else { items(displayList, key = { it.first.id }) { (ach, isRevealed) -> AchievementItem(achievement = ach, isRevealed = isRevealed) } }
                }
            }
        }
    }
}

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
