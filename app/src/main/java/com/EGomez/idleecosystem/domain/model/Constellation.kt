package com.EGomez.idleecosystem.domain.model

/**
 * Las constelaciones del Mapa de Locura (v0.19.2): cada una agrupa los nodos de un tipo de
 * progreso y tiene en su centro una Senda, que se abre al iluminar toda la constelación.
 */
enum class Constellation(val id: String, val title: String, val description: String) {
    HAND("hand", "La Mano", "El poder del gesto: el clic."),
    GEARS("gears", "El Engranaje", "La máquina del culto: los edificios."),
    DREAM("dream", "El Sueño", "Lo que pasa mientras duermes: el progreso sin conexión y los eventos."),
    MIND("mind", "La Mente", "La producción de todo."),
    VOID("void", "El Vacío", "La Cordura que traes de cada vida."),
    CULT("cult", "El Culto", "Con qué empieza cada vida."),
    ARCANE("arcane", "Lo Arcano", "Runas, rituales, Ecos, Atención y Locura.");

    companion object {
        fun fromId(id: String?): Constellation? = entries.firstOrNull { it.id == id }
    }
}
