package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.Trial

/**
 * La interfaz por capas (v0.19.0), al estilo de Revolution Idle: el menú empieza con una
 * sola pestaña y cada sistema aparece recién cuando el jugador llega a él. Las pestañas
 * se agrupan en capas (Vida, Ascensión, Trascendencia) y la fila de capas solo se ve
 * cuando hay más de una.
 */
/** Despertar: la tercera capa (v0.21.0). */
enum class MenuLayer { LIFE, ASCENSION, TRANSCENDENCE, AWAKENING }

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
    /** Grupo (v0.20.4): no tiene contenido propio; reúne los cuatro desafíos de ciclo. */
    CYCLES("cycles", MenuLayer.TRANSCENDENCE),
    DREAM("dream", MenuLayer.TRANSCENDENCE),
    CHAIN("chain", MenuLayer.TRANSCENDENCE),
    LETHARGY("lethargy", MenuLayer.TRANSCENDENCE),
    TTRIALS("ttrials", MenuLayer.TRANSCENDENCE),
    AWAKEN("awaken", MenuLayer.AWAKENING),
    VISIONS("visions", MenuLayer.AWAKENING),
    SIGNS("signs", MenuLayer.AWAKENING),
    EXPEDITION("expedition", MenuLayer.AWAKENING),
    CRYSTALS("crystals", MenuLayer.AWAKENING),
    ELEMENTALS("elementals", MenuLayer.AWAKENING),
    CARDS("cards", MenuLayer.AWAKENING);

    /**
     * La pestaña bajo la que se agrupa (v0.20.4): Mutaciones va dentro de Éter y los cuatro
     * desafíos de ciclo dentro de Ciclos, para que la capa Trascendencia tenga solo cuatro
     * pestañas arriba.
     */
    val group: MenuTab?
        get() = when (this) {
            MUTATIONS -> ETHER
            DREAM, CHAIN, LETHARGY, TTRIALS -> CYCLES
            else -> null
        }

    /** Un grupo sin contenido propio: abrirlo abre su primera pestaña. */
    val isGroupOnly: Boolean get() = this == CYCLES

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

    /** Las pestañas visibles agrupadas bajo [top], en orden. */
    fun membersOf(top: MenuTab): List<MenuTab> = MenuTab.entries.filter { it.group == top && it in tabs }

    /**
     * Las pestañas de arriba de una capa: las que no van dentro de un grupo, y cada grupo
     * si tiene algo visible (v0.20.4).
     */
    fun topTabsOf(layer: MenuLayer): List<MenuTab> = MenuTab.entries.filter { tab ->
        tab.layer == layer && tab.group == null &&
            ((tab in tabs && !tab.isGroupOnly) || membersOf(tab).isNotEmpty())
    }

    /** Las pestañas que se eligen dentro del grupo de [top]: él mismo (si tiene contenido) y sus miembros. */
    fun choicesOf(top: MenuTab): List<MenuTab> {
        val members = membersOf(top)
        if (members.isEmpty()) return emptyList()
        return (if (top in tabs && !top.isGroupOnly) listOf(top) else emptyList()) + members
    }

    /** La pestaña que abre [top]: la primera con algo nuevo, o la primera del grupo. */
    fun entryOf(top: MenuTab, newTabs: Set<MenuTab> = emptySet()): MenuTab {
        val choices = choicesOf(top)
        if (choices.isEmpty()) return top
        return choices.firstOrNull { it in newTabs } ?: choices.first()
    }

    /** La pestaña a mostrar: la pedida si se ve; si no, Investigación. */
    fun resolve(tab: MenuTab?): MenuTab {
        val visible = tab?.takeIf { it in tabs } ?: return MenuTab.RESEARCH
        // Un grupo sin contenido propio muestra su primera pestaña
        return if (visible.isGroupOnly) choicesOf(visible).firstOrNull() ?: MenuTab.RESEARCH else visible
    }

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

        /** Investigación nunca se anuncia como nueva (es la de siempre), ni Ciclos (es un grupo). */
        val NEVER_NEW = setOf(MenuTab.RESEARCH, MenuTab.CYCLES)

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
            chainRevealed: Boolean = false,
            lethargyRevealed: Boolean = false,
            ttrialRevealed: Boolean = false,
            awakeningRevealed: Boolean = false,
            signsRevealed: Boolean = false,
            expeditionRevealed: Boolean = false,
            crystalsRevealed: Boolean = false,
            elementalsRevealed: Boolean = false,
            cardsRevealed: Boolean = false
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
            if (lethargyRevealed) tabs += MenuTab.LETHARGY
            if (ttrialRevealed) tabs += MenuTab.TTRIALS
            if (dreamRevealed || chainRevealed || lethargyRevealed || ttrialRevealed) tabs += MenuTab.CYCLES
            if (awakeningRevealed) {
                tabs += MenuTab.AWAKEN
                tabs += MenuTab.VISIONS
            }
            if (signsRevealed) tabs += MenuTab.SIGNS
            if (expeditionRevealed) tabs += MenuTab.EXPEDITION
            if (crystalsRevealed) tabs += MenuTab.CRYSTALS
            if (elementalsRevealed) tabs += MenuTab.ELEMENTALS
            if (cardsRevealed) tabs += MenuTab.CARDS
            return MenuUnlocks(tabs)
        }
    }
}
