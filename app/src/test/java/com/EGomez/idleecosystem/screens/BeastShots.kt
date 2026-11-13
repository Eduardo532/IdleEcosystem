package com.EGomez.idleecosystem.screens

import android.app.Application
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.EGomez.idleecosystem.domain.logic.Bestiary
import com.EGomez.idleecosystem.domain.logic.SummonResult
import com.EGomez.idleecosystem.ui.screens.main.components.*
import com.EGomez.idleecosystem.ui.theme.IdleEcosystemTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "es-w720dp-h960dp-mdpi")
class BeastShots {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun shot(name: String, scale: Int = 1, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { IdleEcosystemTheme { Box(Modifier.fillMaxSize().background(Color(0xFF0E0E12))) { content() } } }
        compose.mainClock.advanceTimeBy(1500)
        val view = compose.activity.window.decorView
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(android.graphics.Canvas(bmp))
        val out = java.io.ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        java.util.Base64.getEncoder().encodeToString(out.toByteArray()).chunked(4000).forEachIndexed { i, c -> println("SHOT|$name|$i|$c") }
    }

    private fun sheet(page: Int) = shot("sheet$page") {
        val beasts = Bestiary.beasts.drop(page * 48).take(48)
        Column(Modifier.padding(4.dp)) {
            beasts.chunked(6).forEach { row ->
                Row {
                    row.forEach { beast ->
                        Column(Modifier.size(118.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            BeastPortrait(beast, true, Modifier.size(100.dp).background(Color(0xFF16161C)))
                            Text(beast.name, color = beast.rarity.uiColor(), fontSize = 8.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }


    @Test fun panel() = shot("panel") {
        Box(Modifier.width(380.dp)) {
            BestiaryPanel(
                BestiaryUi(
                    levels = Bestiary.beasts.filterIndexed { i, _ -> i % 3 == 0 }.associate { it.id to (1 + it.id.length % 10) },
                    seals = 137, shards = 22, sealPrice = java.math.BigDecimal("4.8e7"), canBuySeals = true,
                    sinceMythic = 12, sincePrimordial = 57, dailyStreak = 4,
                    bonuses = mapOf(com.EGomez.idleecosystem.domain.logic.BeastKind.PRODUCTION to 1.42)
                ),
                {}, {}, {}
            )
        }
    }

    @Test fun card() = shot("card") {
        Column(Modifier.width(360.dp).padding(8.dp)) { CreatureCardContent(Bestiary.beast("deep_dagon")!!, 4, 10, {}, {}) }
    }

    @Test fun summon() = shot("summon") {
        Column(Modifier.width(360.dp).padding(8.dp)) { SummonResultsContent(
            Bestiary.beasts.shuffled(kotlin.random.Random(3)).take(10).mapIndexed { i, b -> SummonResult(b.id, i % 3 == 0, 1 + i % 4, if (i == 7) 4 else 0) },
            {}
        ) }
    }

    @Test fun unknownCard() = shot("unknown") {
        Column(Modifier.width(360.dp).padding(8.dp)) { CreatureCardContent(Bestiary.beast("arc_west")!!, 0, 200, {}, {}) }
    }

    @Test fun encounter() = shot("encounter") {
        Column(Modifier.width(360.dp).padding(top = 20.dp)) { EncounterBanner("sky_zhar", 31, {}) }
    }
}
