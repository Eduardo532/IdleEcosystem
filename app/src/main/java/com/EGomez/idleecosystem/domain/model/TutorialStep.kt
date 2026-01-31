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
        "Necesitas expandirte. Despliega el Menú Inferior tocando la barra de título o la flecha para ver tus opciones de construcción.",
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
        "Usa los botones 'x1', 'x5', 'x10', 'x100', 'MAX' a la derecha para comprar múltiples edificios a la vez. 'MAX' compra el máximo posible con tu saldo actual.",
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
        "Tu conocimiento ha alcanzado un nuevo horizonte. La 'Ascensión' está disponible en la última pestaña (Estrella). Reiniciar te hará más fuerte.",
        "Cuando canAscend cambia a true"
    ),
    PRESTIGE_MECHANICS(
        "prestige_mechanics",
        "El Ciclo",
        "Al ascender:\n- Reinicias: Edificios, Mejoras y Dinero.\n- Ganas: Cordura Residual.\n\nCada punto de Cordura aumenta tu producción permanentemente.",
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
    )
}