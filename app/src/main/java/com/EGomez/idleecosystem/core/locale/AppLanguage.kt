package com.EGomez.idleecosystem.core.locale

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.EGomez.idleecosystem.data.preferences.GamePreferences
import java.util.Locale

/** Idiomas disponibles. El nombre se muestra siempre en su propio idioma. */
enum class AppLanguage(val tag: String, val nativeName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English");

    companion object {
        fun fromTag(tag: String?): AppLanguage? = entries.firstOrNull { it.tag == tag }

        /** Sin elección guardada: inglés si el teléfono está en inglés; si no, español. */
        fun systemDefault(): AppLanguage =
            if (Resources.getSystem().configuration.locales[0].language == ENGLISH.tag) ENGLISH else SPANISH
    }
}

/**
 * Aplica el idioma elegido a la Activity.
 *
 * Se envuelve el contexto en attachBaseContext (antes de que exista Hilt, por eso se
 * lee SharedPreferences directamente): Compose toma los textos de los recursos de
 * ese contexto, así que stringResource() devuelve el idioma elegido sin depender de
 * AppCompat ni del idioma del teléfono. Al cambiarlo, la Activity se recrea.
 */
object AppLocale {

    fun current(context: Context): AppLanguage =
        AppLanguage.fromTag(GamePreferences.readLanguageTag(context)) ?: AppLanguage.systemDefault()

    fun wrap(base: Context): Context {
        val locale = Locale.forLanguageTag(current(base).tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
