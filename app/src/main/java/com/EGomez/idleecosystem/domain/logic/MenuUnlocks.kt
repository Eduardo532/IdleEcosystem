package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Trial

/**
 * La interfaz por capas (v0.19.0), al estilo de Revolution Idle: el menú empieza con una
 * sola pestaña y cada sistema aparece recién cuando el jugador llega a él. Las pestañas
 * se agrupan en capas (Vida, Ascensión, Trascendencia) y la fila de capas solo se ve
 * cuando hay más de una.
 */
enum class MenuLayer { LIFE, ASCENSION, TRANSCENDENCE }

enum class MenuTab(val id: String, val layer: MenuLayer) {
    RESEARCH("research", MenuLayer.LIFE),
    GRIMOIRE("grimoire", MenuLayer.LIFE),
    RITUALS("rituals", MenuLayer.LIFE),
    SANITY("sanity", MenuLayer.ASCENSION),
    PANTHEON("pantheon", MenuLayer.ASCENSION),
    TRIALS("trials", MenuLayer.ASCENSION),
    RUNES("runes", MenuLayer.ASCENSION),
    BESTIARY("bestiary", MenuLayer.ASCENSION),
    ETHER("ether", MenuLayer.TRANSCENDENCE),
    MUTATIONS("mutations", MenuLayer.TRANSCENDENCE),
    STARS("stars", MenuLayer.TRANSCENDENCE),
    LAB("lab", MenuLayer.TRANSCENDENCE),
    DREAM("dream", MenuLayer.TRANSCENDENCE),
    CHAIN("chain", MenuLayer.TRANSCENDENCE);

    companion object {
        fun fromId(id: String?): MenuTab? = entries.firstOrNull { it.id == id }
    }
}

/** El próximo descubrimiento, que se anuncia como pista (solo uno a la vez). */
enum class NextUnlock { GRIMOIRE, RITUALS, ASCENSION, PANTHEON, TRIALS, TRANSCENDENCE }

data class MenuUnlocks(val tabs: Set<MenuTab> = setOf(MenuTab.RESEARCH)) {

    fun has(tab: MenuTab): Boolean = tab in tabs

    /** Capas con al menos una pestaña visible, en orden. */
    val layers: List<MenuLayer> get() = MenuLayer.entries.filter { layer -> tabs.any { it.layer == layer } }

    fun tabsOf(layer: MenuLayer): List<MenuTab> = MenuTab.entries.filter { it.layer == layer && it in tabs }

    /** La pestaña a mostrar: la pedida si se ve; si no, Investigación. */
    fun resolve(tab: MenuTab?): MenuTab = tab?.takeIf { it in tabs } ?: MenuTab.RESEARCH

    /** Lo siguiente por descubrir, en el orden en que suele llegar. */
    val next: NextUnlock?
        get() = when {
            MenuTab.GRIMOIRE !in tabs -> NextUnlock.GRIMOIRE
            MenuTab.RITUALS !in tabs -> NextUnlock.RITUALS
            MenuTab.SANITY !in tabs -> NextUnlock.ASCENSION
            MenuTab.PANTHEON !in tabs -> NextUnlock.PANTHEON
            MenuTab.TRIALS !in tabs -> NextUnlock.TRIALS
            MenuTab.ETHER !in tabs -> NextUnlock.TRANSCENDENCE
            else -> null
        }

    companion object {
        /** Todo visible: para vistas previas y pruebas de pantallas sueltas. */
        val ALL = MenuUnlocks(MenuTab.entries.toSet())

        /** Investigación nunca se anuncia como nueva: es la pestaña de siempre. */
        val NEVER_NEW = setOf(MenuTab.RESEARCH)

        fun compute(
            reveal: SystemReveal,
            ascensions: Int,
            runeSlots: Int,
            masteryRevealed: Boolean,
            transcendRevealed: Boolean,
            mutationsRevealed: Boolean,
            starsRevealed: Boolean = false,
            bestiaryRevealed: Boolean = false,
            labRevealed: Boolean = false,
            dreamRevealed: Boolean = false,
            chainRevealed: Boolean = false
        ): MenuUnlocks {
            val tabs = mutableSetOf(MenuTab.RESEARCH)
            if (reveal.grimoire) tabs += MenuTab.GRIMOIRE
            if (reveal.rituals) tabs += MenuTab.RITUALS
            if (reveal.ascension) tabs += MenuTab.SANITY
            if (ascensions > 0) tabs += MenuTab.PANTHEON
            if (ascensions + 1 >= Trial.REVEAL_AT) tabs += MenuTab.TRIALS
            if (runeSlots > 0 || masteryRevealed) tabs += MenuTab.RUNES
            if (bestiaryRevealed) tabs += MenuTab.BESTIARY
            if (transcendRevealed) tabs += MenuTab.ETHER
            if (mutationsRevealed) tabs += MenuTab.MUTATIONS
            if (starsRevealed) tabs += MenuTab.STARS
            if (labRevealed) tabs += MenuTab.LAB
            if (dreamRevealed) tabs += MenuTab.DREAM
            if (chainRevealed) tabs += MenuTab.CHAIN
            return MenuUnlocks(tabs)
        }
    }
}
