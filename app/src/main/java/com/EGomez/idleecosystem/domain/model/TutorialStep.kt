package com.EGomez.idleecosystem.domain.model

enum class TutorialStep(
    val id: String,
    val title: String,
    val message: String,
    val triggerDescription: String
) {
    // --- FASE 1: BÁSICOS ---
    WELCOME(
        "welcome",
        "El Despertar",
        "Has despertado en el vacío. Tu tarea es generar energía. Toca repetidamente la Zona Central de la pantalla para recolectar Conocimiento Arcano.",
        "Inicio del juego"
    ),
    MENU_INTRO(
        "menu_intro",
        "Gestión Arcana",
        "Necesitas expandirte. Despliega el Menú Inferior tocando la barra de título o la flecha para ver tus opciones de construcción. Las pestañas marcadas con ??? se descubren más adelante: el candado indica qué te falta.",
        "Al tener > 10 de conocimiento"
    ),
    FIRST_BUILDING(
        "first_building",
        "Automatización",
        "El trabajo manual cansa. En la primera pestaña (Investigación), compra tu primera estructura. Esto generará Conocimiento automáticamente cada segundo.",
        "Al tener dinero para el primer edificio"
    ),
    BUY_MULTIPLIERS(
        "buy_multipliers",
        "Producción en Masa",
        "Usa los botones sobre las pestañas para comprar varios edificios a la vez: de 'x1' a 'x100', 'MAX' (todo lo que alcance tu saldo) y 'HITO' (justo lo que falta para el siguiente hito del edificio, donde se desbloquean mejoras y logros). La barra de cada edificio muestra cuánto falta.",
        "Tras comprar 5 edificios totales"
    ),

    // --- FASE 2: PROFUNDIZACIÓN ---
    UPGRADES_TAB(
        "upgrades_tab",
        "Potenciación",
        "Has desbloqueado la segunda pestaña (Grimorio). Aquí puedes adquirir tecnologías que multiplican la eficiencia de tus edificios y tus clicks.",
        "Al entrar a la pestaña 1 (Mejoras) por primera vez"
    ),
    TOP_BAR_INFO(
        "top_bar_info",
        "Panel de Control",
        "En la barra superior encontrarás:\n- Trofeo: Tus Logros desbloqueados.\n- Gráfica: Estadísticas de tu imperio.\n- Engranaje: Ajustes y guardado.",
        "Al desbloquear el primer logro o tras X tiempo"
    ),

    // --- FASE 3: ECOS Y RITUALES ---
    ECHO_FOUND(
        "echo_found",
        "Hallazgo Raro",
        "¡Has encontrado un Eco! Es una divisa especial y muy rara (1% de chance al clickear). No los pierdes al reiniciar el mundo.",
        "Al obtener el primer Eco"
    ),
    RITUALS_TAB(
        "rituals_tab",
        "Rituales",
        "En la tercera pestaña (Rituales), puedes gastar tus Ecos para activar bonificaciones temporales masivas. Úsalos para romper límites de producción.",
        "Al entrar a la pestaña 2 (Rituales) con Ecos > 0"
    ),

    // --- FASE 4: EVENTOS Y RUNAS ---
    EVENT_APPEAR(
        "event_appear",
        "Anomalía Detectada",
        "Ha ocurrido un Evento Aleatorio. Estas situaciones requieren una decisión inmediata: elige con sabiduría, pues hay riesgos y recompensas.",
        "Al aparecer el primer evento (que salta automático)"
    ),
    RUNE_INTRO_GENERIC(
        "rune_intro_generic",
        "Señales del Vacío",
        "Las Runas aparecerán flotando en pantalla. ¡Interactúa con ellas antes de que se desvanezcan!",
        "Al aparecer la primera runa de cualquier tipo"
    ),

    // Tutoriales específicos por Gesto
    RUNE_TUTORIAL_SWIPE(
        "rune_tutorial_swipe",
        "Runa Común",
        "Para reclamar esta runa, debes DESLIZAR (Swipe) tu dedo sobre ella rápidamente como si la cortaras.",
        "Primera vez que aparece una Runa Tier 1"
    ),
    RUNE_TUTORIAL_HOLD(
        "rune_tutorial_hold",
        "Runa Rara",
        "Esta runa es inestable. Debes MANTENER PULSADO (Hold) sobre ella hasta que se estabilice y complete su carga.",
        "Primera vez que aparece una Runa Tier 2"
    ),
    RUNE_TUTORIAL_TRACE(
        "rune_tutorial_trace",
        "Runa Mítica",
        "¡Una runa de gran poder! Requiere precisión. DIBUJA (Trace) siguiendo los puntos conectados en la pantalla.",
        "Primera vez que aparece una Runa Tier 3"
    ),

    // --- FASE 5: PRESTIGIO ---
    PRESTIGE_READY(
        "prestige_ready",
        "Ascensión Disponible",
        "Tu conocimiento ha alcanzado un nuevo horizonte. La 'Ascensión' está disponible en el panel inferior del menú desplegado. Reiniciar te hará más fuerte.",
        "Cuando canAscend cambia a true"
    ),
    PRESTIGE_MECHANICS(
        "prestige_mechanics",
        "El Ciclo",
        "Al ascender:\n- Reinicias: Edificios, Mejoras y Dinero.\n- Ganas: Cordura Residual.\n\nCada punto de Cordura aumenta tu producción permanentemente. La Cordura crece con la raíz cuadrada: duplicarla exige 4 veces más Conocimiento, y la confirmación te muestra cuánto falta.",
        "Al hacer click en el botón de Ascender (antes de confirmar)"
    ),
    PRESTIGE_SHOP(
        "prestige_shop",
        "Tienda de Prestigio",
        "Aquí puedes gastar tu Cordura Residual en mejoras eternas. Gastar Cordura reduce tu bono actual, pero las mejoras suelen valer la inversión.",
        "Al abrir el diálogo de la Tienda de Prestigio"
    ),

    // --- EXTRAS ---
    OFFLINE_GAINS(
        "offline_gains",
        "Producción Pasiva",
        "El universo siguió funcionando en tu ausencia. Has recolectado Conocimiento basado en tu producción automática y el tiempo transcurrido.",
        "Al abrir la app y recibir ganancias offline"
    ),

    // --- FASE 6: METAS Y PROFUNDIDAD (v0.14) ---
    HOLD_TO_CLICK(
        "hold_to_click",
        "Mano Incansable",
        "No hace falta tocar sin descanso: mantén el dedo sobre la Zona Central y seguirás investigando solo, 8 veces por segundo.",
        "Tras 25 clics manuales"
    ),
    NEXT_GOAL(
        "next_goal",
        "La Próxima Meta",
        "La línea dorada bajo el Trofeo avanza hacia tu próxima meta: el logro que tienes más cerca. Toca el Trofeo para verla completa. Cada logro aumenta tu producción para siempre.",
        "Al desbloquear el primer logro"
    ),
    ACHIEVEMENT_SETS(
        "achievement_sets",
        "Colecciones",
        "Los logros de cantidad de un mismo edificio forman un conjunto. Complétalo entero y ese edificio producirá el doble para siempre, incluso después de Ascender. En los Logros verás cuántos conjuntos llevas.",
        "Al tener 2 logros del mismo conjunto"
    ),
    SYNERGIES(
        "synergies",
        "Lazos Prohibidos",
        "Ha aparecido una Sinergia en el Grimorio. Una vez comprada, cada unidad de un edificio suma un 1% a la producción del siguiente: tus edificios antiguos vuelven a importar.",
        "Al aparecer la primera sinergia en el Grimorio"
    ),

    ASCENSION_MILESTONES(
        "ascension_milestones",
        "Hitos de Ascensión",
        "Cada Ascensión te acerca a un hito. Con 1 empiezas cada vida con 10 Sinapsis, y más adelante conservarás mejoras y desbloquearás autocompradores. La confirmación de Ascender muestra la lista completa.",
        "Tras la primera Ascensión"
    ),

    AUTOMATION(
        "automation",
        "Manos Ajenas",
        "Tus sectarios ya saben qué hacer. El botón 'AUTO' sobre las pestañas compra edificios solo, una unidad por segundo, sin gastar más de la mitad de tu Conocimiento. El engranaje de al lado abre el panel de automatización: qué edificios comprar y cuánto gastar.",
        "Al alcanzar el hito del autocomprador de edificios (5 Ascensiones)"
    ),

    // --- FASE 7: EL PANTEÓN (v0.16) ---
    PACTS(
        "pacts",
        "Pactos con los Dioses",
        "Al Ascender puedes sellar un pacto para tu próxima vida. Cada dios da ventajas y exige un precio según su mito; 'Sin Pacto' te deja más Cordura. En la pestaña Panteón verás a los dioses que ya puedes invocar y, con más Ascensiones, aparecerán otros más poderosos.",
        "Al poder Ascender por primera vez"
    ),

    ATTENTION(
        "attention",
        "La Mirada",
        "Tu primera Ascensión te dio un punto de Atención. Toca la barra ◎ Atención, sobre la lista de edificios, y ponlo en un edificio (+25% a su producción), en el Sueño (progreso sin conexión) o en la Vigilia (más runas). Moverlo es gratis y con más Ascensiones tendrás más puntos.",
        "Al tener el primer punto de Atención"
    ),

    PATHS(
        "paths",
        "Las Tres Sendas",
        "Al final de las ramas de clic, edificios y sueño del Mapa de Locura esperan tres Sendas. Solo puedes recorrer una: al comprarla, las otras dos quedan selladas. Si quieres cambiar, al Ascender puedes abandonarla y recuperar toda la Cordura que pusiste en ella.",
        "Al poder comprar la primera Senda"
    ),

    CONVERGENCE(
        "convergence",
        "Convergencia",
        "Ya tienes Ecos para la Convergencia. Durante un minuto tus potenciadores se multiplican en vez de sumarse, hasta x1.000: invócala cuando tengas runas y rituales activos a la vez.",
        "Al reunir 75 Ecos"
    ),

    TRIALS(
        "trials",
        "Pruebas de Locura",
        "Ya puedes enfrentar Pruebas de Locura: vidas con una regla alterada y una meta de Conocimiento. Elígela al Ascender, junto con el pacto. Si llegas a la meta, la Prueba se supera y su recompensa queda para siempre; algunas adelantan a los Dioses Exteriores. En la pestaña Panteón verás todas las Pruebas.",
        "Al poder elegir la primera Prueba"
    ),

    RUNE_SLOTS(
        "rune_slots",
        "El Altar de Runas",
        "Superar la Prueba te dio un Espacio de Runa. En la pestaña Panteón, vista Runas, graba una runa que hayas atrapado varias veces: su bono queda activo para siempre y cambiarla es gratis. Cada runa da un bono distinto.",
        "Al ganar el primer espacio de runa"
    ),

    MADNESS(
        "madness",
        "El Medidor de Locura",
        "Tu mente ya no es la de antes. La barra de Locura sube cuando juegas fuerte (clics, runas, rituales) y baja sola con el tiempo. Cada punto da +1% de producción, pero desde la mitad llegan presagios y al 100% la mente colapsa: pierdes tus potenciadores y produces la mitad un minuto. Tócala para ver los detalles; la Letanía de Calma la baja de golpe.",
        "Al tener 5 Ascensiones"
    ),

    TIDES(
        "tides",
        "Fases de la Marea",
        "El mar del fondo del mundo empezó a moverse. Cada 10 minutos de juego cambia la Marea: la Alta trae más runas y eventos (y más Locura), la Baja da más producción y calma, y la Muerta abarata los rituales. Toca el indicador para ver las tres y planea cuándo usar cada cosa.",
        "Al tener 10 Ascensiones"
    ),

    TRANSCEND(
        "transcend",
        "Trascender",
        "Cincuenta vidas y el ciclo pesa. Ahora puedes Trascender: reinicias casi todo (Cordura, Mapa, Ascensiones, Pruebas y runas grabadas) a cambio de Éter, que compra mejoras eternas que nunca se pierden. Los logros se quedan contigo. Toca \"Éter · Trascender\" en el panel de Cordura para ver qué ganarías.",
        "Al tener 50 Ascensiones en el ciclo"
    )
}
