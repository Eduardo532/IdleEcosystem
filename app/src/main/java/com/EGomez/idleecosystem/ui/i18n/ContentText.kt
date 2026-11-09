package com.EGomez.idleecosystem.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import com.EGomez.idleecosystem.core.locale.AppLanguage

/**
 * Idioma de la pantalla actual. Sale de la configuración del contexto (la Activity
 * la fija con el idioma elegido, ver AppLocale), así que coincide siempre con el de
 * los textos de stringResource().
 */
@Composable
@ReadOnlyComposable
fun contentLanguage(): AppLanguage =
    if (LocalConfiguration.current.locales[0].language == AppLanguage.ENGLISH.tag) AppLanguage.ENGLISH
    else AppLanguage.SPANISH

/** Texto de contenido del juego (nombre, descripción...) en el idioma de la pantalla. */
@Composable
@ReadOnlyComposable
fun tr(text: String): String = ContentTranslations.translate(text, contentLanguage())
