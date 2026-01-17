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
    lateinit var soundManager: SoundManager // <--- INYECCIÓN DE AUDIO

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
        viewModel.saveProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}