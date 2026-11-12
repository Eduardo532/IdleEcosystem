package com.EGomez.idleecosystem.ui.i18n

import com.EGomez.idleecosystem.core.locale.AppLanguage
import com.EGomez.idleecosystem.domain.logic.AchievementManager
import com.EGomez.idleecosystem.domain.logic.BuildingCatalog
import com.EGomez.idleecosystem.domain.logic.PrestigeShopManager
import com.EGomez.idleecosystem.domain.logic.RandomEventManager
import com.EGomez.idleecosystem.domain.logic.RitualManager
import com.EGomez.idleecosystem.domain.logic.UpgradeManager
import com.EGomez.idleecosystem.domain.model.Pact
import com.EGomez.idleecosystem.domain.model.RuneCatalog
import com.EGomez.idleecosystem.domain.model.RuneTier
import com.EGomez.idleecosystem.domain.model.TutorialStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Todo texto de contenido que el juego puede mostrar tiene su versión en inglés.
 * Si alguien agrega un edificio, un logro o un evento sin traducirlo, falla aquí.
 */
class ContentTranslationsTest {

    private fun visibleTexts(): Map<String, List<String>> = mapOf(
        "Edificios" to BuildingCatalog.create().flatMap { listOf(it.name, it.description) },
        "Mejoras" to UpgradeManager().availableUpgrades.flatMap { listOf(it.name, it.description) },
        "Logros" to AchievementManager().availableAchievements.flatMap { listOf(it.name, it.description, it.requirement) },
        "Eventos" to RandomEventManager().allEvents().flatMap { event ->
            listOf(event.title, event.description) + event.options.flatMap { listOf(it.text, it.flavorResult) }
        },
        "Runas" to RuneTier.entries.flatMap { RuneCatalog.getVariantsForTier(it) }.flatMap { listOf(it.name, it.description) },
        "Rituales" to RitualManager().rituals.value.flatMap { listOf(it.name, it.description) },
        "Tienda de Cordura" to PrestigeShopManager().upgrades.flatMap { listOf(it.name, it.description) },
        "Tutoriales" to TutorialStep.entries.flatMap { listOf(it.title, it.message) },
        "Panteón" to Pact.entries.flatMap { listOf(it.godName, it.epithet, it.lore) },
        "Pruebas de Locura" to com.EGomez.idleecosystem.domain.model.Trial.entries.flatMap { listOf(it.title, it.rule) },
        "Mutaciones" to com.EGomez.idleecosystem.domain.model.Mutation.entries.flatMap { listOf(it.title, it.description) },
        "Constelaciones" to com.EGomez.idleecosystem.domain.model.Constellation.entries.flatMap { listOf(it.title, it.description) },
        "Hitos de Trascendencia" to com.EGomez.idleecosystem.domain.logic.TranscendenceMilestone.entries.flatMap { listOf(it.title, it.description) },
        "Mejoras estelares" to com.EGomez.idleecosystem.domain.logic.StarUpgrade.entries.flatMap { listOf(it.title, it.description) },
        "Laboratorio" to com.EGomez.idleecosystem.domain.logic.LabExperiment.entries.flatMap { listOf(it.title, it.description) },
        "Bestiario" to com.EGomez.idleecosystem.domain.logic.Bestiary.beasts.flatMap { listOf(it.name, it.lore) } +
            com.EGomez.idleecosystem.domain.logic.Bestiary.families.flatMap { listOf(it.title, it.description) } +
            com.EGomez.idleecosystem.domain.logic.BeastRarity.entries.map { it.title }
    )

    @Test
    fun `every visible content text has an english translation`() {
        val missing = visibleTexts().flatMap { (group, texts) ->
            texts.filter { it.isNotBlank() && ContentTranslations.english(it) == null }.map { "$group: $it" }
        }
        assertTrue("Sin traducir (${missing.size}):\n" + missing.joinToString("\n"), missing.isEmpty())
    }

    @Test
    fun `spanish is returned untouched`() {
        assertEquals("Notas Desordenadas", ContentTranslations.translate("Notas Desordenadas", AppLanguage.SPANISH))
    }

    @Test
    fun `grimoire ladder names are built from both parts`() {
        assertEquals("Messy Notes: Obsession", ContentTranslations.english("Notas Desordenadas: Obsesión"))
    }

    @Test
    fun `unknown text falls back to the original`() {
        assertEquals("Texto nuevo", ContentTranslations.translate("Texto nuevo", AppLanguage.ENGLISH))
    }

    @Test
    fun `multi line tutorial texts keep their line breaks`() {
        val english = ContentTranslations.english(TutorialStep.PRESTIGE_MECHANICS.message)!!
        assertTrue(english.contains("\n- You reset"))
    }
}
