package com.EGomez.idleecosystem.domain.logic

import com.EGomez.idleecosystem.domain.model.TutorialStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import java.util.ArrayDeque

@Singleton
class TutorialManager @Inject constructor() {

    // Paso actual visible en la pantalla
    private val _currentStep = MutableStateFlow<TutorialStep?>(null)
    val currentStep: StateFlow<TutorialStep?> = _currentStep.asStateFlow()

    // Cola de espera para tutoriales que saltan mientras ves otro
    private val tutorialQueue = ArrayDeque<TutorialStep>()

    private val _completedSteps = MutableStateFlow<Set<String>>(emptySet())
    val completedSteps: StateFlow<Set<String>> = _completedSteps.asStateFlow()

    private val _isTutorialEnabled = MutableStateFlow(true)
    val isTutorialEnabled: StateFlow<Boolean> = _isTutorialEnabled.asStateFlow()

    fun loadProgress(completedIds: Set<String>, isEnabled: Boolean) {
        _completedSteps.value = completedIds
        _isTutorialEnabled.value = isEnabled
    }

    /**
     * Intenta disparar un paso. Si hay uno activo, lo pone en cola.
     */
    fun triggerStep(step: TutorialStep) {
        if (!_isTutorialEnabled.value) return
        if (_completedSteps.value.contains(step.id)) return

        // Evitamos duplicados en la cola
        if (tutorialQueue.contains(step)) return
        if (_currentStep.value == step) return

        if (_currentStep.value == null) {
            // Si no hay nada, mostramos directo
            _currentStep.value = step
        } else {
            // Si hay uno activo, a la cola
            tutorialQueue.add(step)
        }
    }

    /**
     * Cierra el actual y busca el siguiente en la cola.
     */
    fun dismissCurrentStep() {
        val step = _currentStep.value ?: return
        markStepAsCompleted(step)

        if (tutorialQueue.isNotEmpty()) {
            _currentStep.value = tutorialQueue.removeFirst()
        } else {
            _currentStep.value = null
        }
    }

    private fun markStepAsCompleted(step: TutorialStep) {
        val currentSet = _completedSteps.value.toMutableSet()
        currentSet.add(step.id)
        _completedSteps.value = currentSet
    }

    fun setTutorialEnabled(enabled: Boolean) {
        _isTutorialEnabled.value = enabled
        if (!enabled) {
            _currentStep.value = null
            tutorialQueue.clear()
        }
    }

    fun resetTutorial() {
        _completedSteps.value = emptySet()
        tutorialQueue.clear()
        _isTutorialEnabled.value = true
        triggerStep(TutorialStep.WELCOME)
    }

    fun getCompletedStepsList(): List<String> = _completedSteps.value.toList()

    // Helper para saber si el juego debe pausarse
    fun isTutorialActive(): Boolean {
        return _currentStep.value != null
    }
}