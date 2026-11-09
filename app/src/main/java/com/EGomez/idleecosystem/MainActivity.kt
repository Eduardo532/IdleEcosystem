package com.EGomez.idleecosystem

import android.content.Context
import android.os.Bundle
import com.EGomez.idleecosystem.core.locale.AppLocale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.EGomez.idleecosystem.domain.logic.SoundManager
import com.EGomez.idleecosystem.ui.MainViewModel
import com.EGomez.idleecosystem.ui.screens.MainScreen
import com.EGomez.idleecosystem.ui.theme.IdleEcosystemTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var soundManager: SoundManager

    // Idioma elegido en Configuración (ver AppLocale)
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciar música si está habilitada
        soundManager.playMusic()

        setContent {
            IdleEcosystemTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegroundChanged(inForeground = true)
    }

    override fun onResume() {
        super.onResume()
        soundManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        soundManager.pauseMusic()
    }

    override fun onStop() {
        super.onStop()
        // Pausa el juego en segundo plano (runas, eventos, sonidos) y guarda
        viewModel.onAppForegroundChanged(inForeground = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        // SoundManager es un singleton que vive con el proceso: liberar su SoundPool
        // aquí dejaba la app sin efectos tras rotar la pantalla o salir con "Atrás".
        // Solo se libera la música cuando el usuario cierra la Activity de verdad.
        if (isFinishing) soundManager.stopMusic()
    }
}