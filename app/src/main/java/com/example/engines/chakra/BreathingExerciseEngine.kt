package com.example.engines.chakra

import com.example.core.model.chakra.BreathingPattern
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class BreathingPhase(val label: String, val sanskritLabel: String) {
    INHALE("Inhale (Puraka)", "पूरक"),
    HOLD_IN("Hold (Antar Kumbhaka)", "अन्तर कुम्भक"),
    EXHALE("Exhale (Rechaka)", "रेचक"),
    HOLD_OUT("Rest (Bahir Kumbhaka)", "बाह्य कुम्भक")
}

data class BreathingState(
    val pattern: BreathingPattern = BreathingPattern.SAMA_VRITTI,
    val phase: BreathingPhase = BreathingPhase.INHALE,
    val phaseProgress: Float = 0f, // 0.0f to 1.0f for smooth animation
    val secondsRemainingInPhase: Int = 4,
    val totalPhaseSeconds: Int = 4,
    val cycleCount: Int = 1,
    val isRunning: Boolean = false
)

class BreathingExerciseEngine(
    private val onPhaseChanged: ((BreathingPhase) -> Unit)? = null
) {
    private val _state = MutableStateFlow(BreathingState())
    val state: StateFlow<BreathingState> = _state.asStateFlow()

    private var exerciseJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun setPattern(pattern: BreathingPattern) {
        val wasRunning = _state.value.isRunning
        stop()
        _state.value = BreathingState(
            pattern = pattern,
            phase = BreathingPhase.INHALE,
            phaseProgress = 0f,
            secondsRemainingInPhase = pattern.inhaleSeconds,
            totalPhaseSeconds = pattern.inhaleSeconds,
            cycleCount = 1,
            isRunning = false
        )
        if (wasRunning) {
            start()
        }
    }

    fun start() {
        if (_state.value.isRunning) return
        _state.value = _state.value.copy(isRunning = true)
        onPhaseChanged?.invoke(_state.value.phase)
        startTicking()
    }

    fun pause() {
        _state.value = _state.value.copy(isRunning = false)
        exerciseJob?.cancel()
        exerciseJob = null
    }

    fun stop() {
        pause()
        val curPattern = _state.value.pattern
        _state.value = BreathingState(
            pattern = curPattern,
            phase = BreathingPhase.INHALE,
            phaseProgress = 0f,
            secondsRemainingInPhase = curPattern.inhaleSeconds,
            totalPhaseSeconds = curPattern.inhaleSeconds,
            cycleCount = 1,
            isRunning = false
        )
    }

    private fun startTicking() {
        exerciseJob?.cancel()
        exerciseJob = scope.launch {
            val tickIntervalMs = 50L // 20 updates per second for silky smooth UI
            while (isActive && _state.value.isRunning) {
                delay(tickIntervalMs)
                updateTick(tickIntervalMs)
            }
        }
    }

    private var currentPhaseElapsedMs = 0L

    private fun updateTick(elapsedMs: Long) {
        val current = _state.value
        val totalMs = current.totalPhaseSeconds * 1000L
        currentPhaseElapsedMs += elapsedMs

        if (currentPhaseElapsedMs >= totalMs) {
            // Transition to next phase
            advancePhase(current)
        } else {
            val progress = (currentPhaseElapsedMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
            val secRemaining = ((totalMs - currentPhaseElapsedMs) / 1000L).toInt() + 1
            _state.value = current.copy(
                phaseProgress = progress,
                secondsRemainingInPhase = secRemaining
            )
        }
    }

    private fun advancePhase(current: BreathingState) {
        currentPhaseElapsedMs = 0L
        val pattern = current.pattern

        val (nextPhase, nextSec, nextCycle) = when (current.phase) {
            BreathingPhase.INHALE -> {
                if (pattern.holdAfterInhaleSeconds > 0) {
                    Triple(BreathingPhase.HOLD_IN, pattern.holdAfterInhaleSeconds, current.cycleCount)
                } else {
                    Triple(BreathingPhase.EXHALE, pattern.exhaleSeconds, current.cycleCount)
                }
            }
            BreathingPhase.HOLD_IN -> {
                Triple(BreathingPhase.EXHALE, pattern.exhaleSeconds, current.cycleCount)
            }
            BreathingPhase.EXHALE -> {
                if (pattern.holdAfterExhaleSeconds > 0) {
                    Triple(BreathingPhase.HOLD_OUT, pattern.holdAfterExhaleSeconds, current.cycleCount)
                } else {
                    Triple(BreathingPhase.INHALE, pattern.inhaleSeconds, current.cycleCount + 1)
                }
            }
            BreathingPhase.HOLD_OUT -> {
                Triple(BreathingPhase.INHALE, pattern.inhaleSeconds, current.cycleCount + 1)
            }
        }

        _state.value = current.copy(
            phase = nextPhase,
            phaseProgress = 0f,
            secondsRemainingInPhase = nextSec,
            totalPhaseSeconds = nextSec,
            cycleCount = nextCycle
        )
        onPhaseChanged?.invoke(nextPhase)
    }

    fun release() {
        exerciseJob?.cancel()
        scope.cancel()
    }
}
