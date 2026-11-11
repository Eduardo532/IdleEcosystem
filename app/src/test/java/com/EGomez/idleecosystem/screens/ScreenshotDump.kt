package com.EGomez.idleecosystem.screens

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.EGomez.idleecosystem.domain.logic.BuildingCalculator
import com.EGomez.idleecosystem.domain.logic.BuildingCatalog
import com.EGomez.idleecosystem.domain.logic.MenuTab
import com.EGomez.idleecosystem.domain.logic.MenuUnlocks
import com.EGomez.idleecosystem.domain.logic.RuneBuff
import com.EGomez.idleecosystem.domain.model.Mutation
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.RuneCatalog
import com.EGomez.idleecosystem.ui.components.RuneCaughtNotice
import com.EGomez.idleecosystem.ui.components.RunePlate
import com.EGomez.idleecosystem.ui.screens.ActiveBuffsDisplay
import com.EGomez.idleecosystem.ui.screens.main.components.InfoSection
import com.EGomez.idleecosystem.ui.screens.main.components.MainTopBar
import com.EGomez.idleecosystem.ui.screens.main.components.MenuSection
import com.EGomez.idleecosystem.ui.theme.IdleEcosystemTheme
import com.EGomez.idleecosystem.ui.theme.VoidBlack
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "es-w360dp-h760dp-xhdpi")
class ScreenshotDump {
    @get:Rule
    val compose = createComposeRule()

    private fun shot(name: String, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { IdleEcosystemTheme { Box(Modifier.fillMaxSize().background(VoidBlack)) { content() } } }
        compose.mainClock.advanceTimeBy(600)
        val bmp = compose.onRoot().captureToImage().asAndroidBitmap()
        val dir = File("build/screens").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun buildings(owned: List<Int>) = BuildingCatalog.create().take(owned.size).mapIndexed { i, b ->
        b.amountOwned = owned[i]; b.isVisible = true; b
    }

    @Composable
    private fun Screen(
        tab: MenuTab,
        unlocks: MenuUnlocks,
        newTabs: Set<MenuTab> = emptySet(),
        owned: List<Int> = listOf(3, 0),
        ascensions: Int = 0,
        buffs: List<RuneBuff> = emptyList(),
        expanded: Boolean = true
    ) {
        Column(Modifier.fillMaxSize()) {
            MainTopBar(
                isMenuExpanded = expanded, menuWeight = 0.65f, achievements = emptyList(), achievementsProgress = "3/485",
                knowledgeFormatted = "12.4K", knowledgePerSecond = "350",
                showStats = unlocks.has(MenuTab.GRIMOIRE), showAchievements = ascensions > 0,
                onSettingsClick = {}, onStatsClick = {}, onAchievementsClick = {}
            )
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp).weight(1f)) {
                if (buffs.isNotEmpty()) ActiveBuffsDisplay(buffs, clock = { 0L })
                if (!expanded) {
                    InfoSection("12.4K", "350", BigDecimal.ONE, 1.0, 0, "1", { _, _, _ -> }, Modifier.weight(0.85f))
                }
                MenuSection(
                    isMenuExpanded = expanded, onToggleMenu = {}, menuWeight = if (expanded) 0.65f else 0.15f,
                    selectedTab = tab, onTabSelected = {},
                    buyMultiplier = 1, onBuyMultiplierChanged = {}, showPurchased = false, onToggleShowPurchased = {},
                    buildings = buildings(owned), upgrades = emptyList(), rituals = emptyList(),
                    currentKnowledge = BigDecimal("12400"), currentEchoes = BigDecimal("3"), globalMultiplier = BigDecimal.ONE,
                    buildingCalculator = BuildingCalculator(), buildingCostMultiplier = 1.0,
                    onBuyBuilding = {}, onBuyUpgrade = {}, onActivateRitual = {},
                    prestigeCurrencyFormatted = "1.25K", canPrestige = ascensions > 0, potentialPrestigeFormatted = "84",
                    unlocks = unlocks, newTabs = newTabs, ascensions = ascensions,
                    activePact = if (ascensions > 0) Pact.CTHULHU else Pact.NONE,
                    attentionPoints = if (ascensions > 0) 3 else 0, attention = mapOf("b_notes" to 1),
                    activeMutation = if (ascensions > 50) Mutation.FEVERISH_NOTES else null,
                    seenMutations = setOf("feverish_notes", "hungry_void", "burning_synapse"),
                    cycleAscensions = 52, etherGain = 14, ether = 6, eternalLevels = mapOf("fire" to 1),
                    onAscendClicked = {}, onOpenMadnessMap = {},
                    modifier = Modifier.weight(if (expanded) 1f else 0.15f)
                )
            }
        }
    }

    private val life = MenuUnlocks(setOf(MenuTab.RESEARCH, MenuTab.GRIMOIRE, MenuTab.RITUALS))
    private val asc = MenuUnlocks(setOf(MenuTab.RESEARCH, MenuTab.GRIMOIRE, MenuTab.RITUALS, MenuTab.SANITY, MenuTab.PANTHEON, MenuTab.TRIALS))
    private val buffs = listOf(
        RuneBuff(id = "t1_blood", multiplier = 10.0, expirationTime = 12_000, description = "", durationMs = 15_000),
        RuneBuff(id = "t1_shadow", multiplier = 5.0, expirationTime = 25_000, description = "", durationMs = 30_000),
        RuneBuff(id = "t2_barrier", costReduction = 0.15, expirationTime = 9_000, description = "", durationMs = 30_000)
    )

    @Test fun s01() = shot("01_new_player") { Screen(MenuTab.RESEARCH, MenuUnlocks(), owned = listOf(3, 1)) }
    @Test fun s02() = shot("02_new_player_closed") { Screen(MenuTab.RESEARCH, MenuUnlocks(), owned = listOf(3, 1), expanded = false) }
    @Test fun s03() = shot("03_life_new_tab") { Screen(MenuTab.RESEARCH, life, newTabs = setOf(MenuTab.RITUALS), owned = listOf(12, 8, 3, 1)) }
    @Test fun s04() = shot("04_ascension_research") { Screen(MenuTab.RESEARCH, asc, newTabs = setOf(MenuTab.TRIALS), owned = listOf(40, 30, 20, 10, 5), ascensions = 5, buffs = buffs) }
    @Test fun s05() = shot("05_sanity_tab") { Screen(MenuTab.SANITY, asc, owned = listOf(40, 30, 20, 10, 5), ascensions = 5) }
    @Test fun s06() = shot("06_ether_tab") { Screen(MenuTab.ETHER, MenuUnlocks.ALL, owned = listOf(40, 30, 20, 10, 5), ascensions = 60) }
    @Test fun s07() = shot("07_mutations_tab") { Screen(MenuTab.MUTATIONS, MenuUnlocks.ALL, owned = listOf(40, 30, 20, 10, 5), ascensions = 60) }
    @Test fun s08() = shot("08_runes") {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier.height(60.dp))
            RunePlate(RuneCatalog.T1_SHADOW, 0)
            RunePlate(RuneCatalog.T2_BARRIER, 2)
            RunePlate(RuneCatalog.T3_CTHULHU, 5)
            RuneCaughtNotice(RuneCatalog.T1_BONE, 0)
            ActiveBuffsDisplay(buffs, clock = { 0L })
        }
    }
}
