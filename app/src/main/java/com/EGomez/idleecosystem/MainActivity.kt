package com.EGomez.idleecosystem

import android.os.Bundle
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
        viewModel.onAppForegroundChanged(true)
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
        // Pausa eventos y efectos en segundo plano y guarda el progreso
        viewModel.onAppForegroundChanged(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Rotar la pantalla también destruye la Activity: solo se libera la música
        // cuando el usuario realmente cierra la app. El SoundPool vive con el proceso.
        if (isFinishing) soundManager.stopMusic()
    }
}
