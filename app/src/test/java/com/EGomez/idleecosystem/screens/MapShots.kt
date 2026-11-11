package com.EGomez.idleecosystem.screens

import android.app.Application
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.EGomez.idleecosystem.domain.logic.MadnessPaths
import com.EGomez.idleecosystem.domain.logic.PrestigeShopManager
import com.EGomez.idleecosystem.domain.model.Constellation
import com.EGomez.idleecosystem.ui.components.ConstellationMap
import com.EGomez.idleecosystem.ui.theme.IdleEcosystemTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "es-w360dp-h640dp-xhdpi")
class MapShots {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun shot(name: String, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { IdleEcosystemTheme { Box(Modifier.fillMaxSize()) { content() } } }
        compose.mainClock.advanceTimeBy(600)
        val view = compose.activity.window.decorView
        val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(android.graphics.Canvas(bmp))
        val small = Bitmap.createScaledBitmap(bmp, view.width / 2, view.height / 2, true)
        val out = java.io.ByteArrayOutputStream()
        small.compress(Bitmap.CompressFormat.PNG, 100, out)
        java.util.Base64.getEncoder().encodeToString(out.toByteArray()).chunked(4000).forEachIndexed { i, c -> println("SHOT|$name|$i|$c") }
    }

    private fun sample() = PrestigeShopManager().upgrades.map { it.copy() }.onEach { n ->
        when {
            n.id == "pu_core" -> n.level = 6
            n.constellation == Constellation.MIND && !MadnessPaths.isPath(n.id) -> n.level = 2
            n.constellation == Constellation.HAND && n.id != "pu_click_4" && !MadnessPaths.isPath(n.id) -> n.level = 3
            n.id == "pu_legacy" || n.id == "pu_cult_scrolls" -> n.level = 4
            n.id == "pu_void_1" -> n.level = 1
            n.id == MadnessPaths.MIND -> n.level = 1
        }
    }

    @Test fun sky() = shot("sky") { ConstellationMap(sample(), {}, currency = BigDecimal(300)) }
    @Test fun focus() = shot("focus_mind") { ConstellationMap(sample(), {}, currency = BigDecimal(300), focus = Constellation.MIND, selectedId = "pu_mind_3") }
    @Test fun focusCult() = shot("focus_cult") { ConstellationMap(sample(), {}, currency = BigDecimal(300), focus = Constellation.CULT) }
    @Test fun focusHand() = shot("focus_hand") { ConstellationMap(sample(), {}, currency = BigDecimal(300), focus = Constellation.HAND) }
    @Test fun focusArcane() = shot("focus_arcane") { ConstellationMap(sample(), {}, currency = BigDecimal(300), focus = Constellation.ARCANE) }
    @Test fun focusGears() = shot("focus_gears") { ConstellationMap(sample(), {}, currency = BigDecimal(300), focus = Constellation.GEARS) }
}
